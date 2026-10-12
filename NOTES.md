# GameEngine.java fixes

## 1. Group-play suit-only check (`canSimPlay`)

`playCardGroup` validates each card in the group through `canSimPlay`, which is the
group-play counterpart of `canPlay`. Both match branches of `canSimPlay` only tested
suit, so a legal group was rejected whenever a card matched the top card by rank
rather than suit.

Concretely: with a top card of `SEVEN of DIAMONDS`, a group of `SEVEN of HEARTS` +
`SEVEN of CLUBS` was rejected, even though single-card `canPlay` allows it (rank
match). Play the first group card and the simulated top becomes that same-rank card,
so the second card was then compared suit-only against the first card too, breaking
any same-rank group with mixed suits.

Fix: made `canSimPlay` mirror `canPlay` exactly by adding the rank fallback:

```java
if (simDeclaredSuit != null) {
    return card.suit == simDeclaredSuit ||
            card.rank == top.rank;
}

return card.suit == top.suit ||
        card.rank == top.rank;
```

## 2. Draw soft-lock (`drawCard`)

In the non-penalty draw path, when `drawOneCard()` returned `null` (deck empty and
the discard pile worn down to a single card, so nothing can be recycled), the method
logged "No cards available." and returned `null` **without advancing the turn**. The
current player had no card to draw and no legal play, so `currentPlayerIndex` never
moved and the game froze on that player.

Fix: treat the exhausted-stock case as a pass by advancing the turn, so play is never
stuck on a player who cannot draw:

```java
if (card == null) {
    log.append("No cards available. ")
            .append(p.name)
            .append(" passes.\n");
    advanceTurn();
    return null;
}
```

## Files changed

- `app/src/main/java/com/tukokadi/app/GameEngine.java`
  - `canSimPlay(...)` — added rank fallback to both match branches.
  - `drawCard()` — advance the turn when no card can be drawn.

No other files were opened or modified (callers live in `MainActivity.java`, which
was left untouched). Gradle was not run.

---

# Bot Play entry fee & NETCOINS economy

Bot Play is reached Home → `BotLevelActivity` → `MainActivity` (extra `"level"`).
The game is fully offline. The player's visible balance on the home screen is the
local wallet `Wallet` (`tuko_wallet`, read by `HomeExtras.refreshBalance`), so the
entry fee and winnings use **`Wallet`**, not `KibWallet` (`kib_wallet`, only wired to
the Wallet screen's "Test top-up" button whose headline balance actually comes from
the server `/me`).

Units: **1 sh = 10 NETCOINS**. The word "NETCOINS" is introduced only in the new Bot
Play UI; existing "Coins" labels were left alone.

## 1. Entry fee (10 NETCOINS), blocked when unaffordable

A game costs `ENTRY_FEE = 10` NETCOINS, charged when the game actually starts.

- `onCreate` gates before any game UI is built: if `Wallet.getBalance(this) < 10`
  it calls `showInsufficientEntryDialog()` and returns, so `game` is never created
  (`startNewGame` is only reached with sufficient funds).
- `startNewGame()` also charges, so each replay from the win screen pays again. If
  the replay can't be afforded, it shows the dialog and returns without creating a
  game. `formatSh(...)` renders the sh value (15 NETCOINS → `1.5 sh`).

`showInsufficientEntryDialog()` shows the cost, the rate, and the current balance,
then `finish()`es — the game is not started.

## 2. AMOUNT TO WIN label

Added directly under the "TUKO KADI" title (`rootLayout.addView(title)`), same
programmatic-UI style as the rest of `MainActivity`:

```
AMOUNT TO WIN: 15 NETCOINS  ·  1.5 sh
1 sh = 10 NETCOINS
```

Cream text on `GREEN_LIGHT` for contrast against the felt.

## 3. Win pays once; loss/exit pays nothing

`WIN_PAYOUT = 15` = entry (10) + half-entry bonus (5), so a win nets **+5** and a
loss costs **-10**. The credit is inside the existing `if (!winAnnounced)` block and
additionally guarded by a `winCredited` flag (reset per game in `startNewGame`),
so `refresh()` can run many times after game over without crediting twice. Only
`winnerName.equals("You")` credits; when a bot wins, or when the player taps "Exit
Game" mid-game, the entry is already spent and nothing is credited.

On the farming rule: **agreed.** Paying back entry + half-entry keeps a win mildly
positive while a loss is twice that, so break-even needs a
`10 / 15 ≈ 66.7%` win rate. Against EASY bots a strong player can still slowly farm,
but only at a modest rate; anything stricter (e.g. refund-only) removes the
incentive to play at all.

## 4. "You:" label readability

`handLabel` ("You:") was `GREEN_DARK` (`#1B5E20`) on the green felt — dark on dark.
Changed to `GOLD`, matching the title and the bots' `#F4D160` name labels.

## Files changed

- `app/src/main/java/com/tukokadi/app/MainActivity.java`
  - constants `ENTRY_FEE`, `WIN_PAYOUT`, `NETCOINS_PER_SH`; field `winCredited`.
  - `onCreate` — affordability gate before UI build.
  - title area — new "AMOUNT TO WIN" label.
  - `handLabel` — color `GREEN_DARK` → `GOLD`.
  - `startNewGame` — deduct entry, reset `winCredited`; new `formatSh`,
    `showInsufficientEntryDialog` helpers.
  - `refresh` game-over block — credit `WIN_PAYOUT` once when the player wins.

No layout XML was needed: the Bot Play screen is built in code. `Wallet.java` was
read and reused as-is (no changes). Gradle was not run.

---

# Crash reporter

Crash reporter only; no other behavior touched. `MainActivity.java` was not opened.

## 1. `CrashApp` (Application)

`app/src/main/java/com/tukokadi/app/CrashApp.java` (already present, verified):

- `CRASH_FILE = "last_crash.txt"`.
- In `onCreate`, captures `Thread.getDefaultUncaughtExceptionHandler()` as
  `oldHandler`, then sets a new default handler.
- The handler renders the full stack trace via `throwable.printStackTrace(...)`
  into a `StringWriter`, writes it to `new File(getFilesDir(), CRASH_FILE)`, and
  swallows any I/O error so reporting never masks the original crash.
- Then delegates to `oldHandler.uncaughtException(...)` so normal crash behavior
  (process teardown / crash dialog) still runs.

## 2. Manifest registration

`app/src/main/AndroidManifest.xml` — added `android:name=".CrashApp"` to the
`<application>` tag so the custom `Application` is instantiated.

## 3. Crash log dialog on Home

`HomeActivity.onCreate` already had an early crash-log prompt, but it read a stale
filename (`crash.txt`), used a non-scrollable `setMessage(...)`, and deleted the
file before the user could read it. Changed to:

- Read `CrashApp.CRASH_FILE` (`last_crash.txt`) via a `FileInputStream` whose handle
  is closed; on failure the body falls back to `"(could not read crash log)"`.
- Show the trace in a monospace-less `TextView` (12sp, 16dp padding) wrapped in a
  `ScrollView`, so long traces scroll.
- `AlertDialog` title "Crash log" with a single "Close" button whose listener does
  `crashFile.delete()` — the file is now removed only after the user dismisses it.

## Files changed

- `app/src/main/AndroidManifest.xml` — `android:name=".CrashApp"`.
- `app/src/main/java/com/tukokadi/app/HomeActivity.java` — crash-log block rewritten.
- `app/src/main/java/com/tukokadi/app/CrashApp.java` — pre-existing, unchanged.

Gradle was not run. `MainActivity.java` was not opened.

---

# RAFIKI Play server: server-side coins + AUTOMATCH

Server side only. `~/rafiki-server/server.js` was edited; the Android app was not
touched. Backed up first to `server.js.bak-before-coins` (unchanged, 19744 bytes).
`node --check server.js` passes.

## 1. Server-side balances (PLAN step 4)

- New persistent store: map `token -> NETCOINS`, saved to `balances.json`
  (`fs.writeFileSync` to a `.tmp` file then `fs.renameSync` = atomic write).
- Identity is the **token** alone. Every `create` / `join` / `automatch` requires a
  non-empty `token`; missing -> "Authentication required".
- The app's `msg.balance` is gone entirely — balances are read with `balanceOf(token)`
  and never trusted from the wire. A token seen for the first time gets
  `STARTING_BALANCE = 1000` (mirrors `Wallet`'s starting bonus).

## 2. Entry escrow (PLAN step 5)

`startIfFull` verifies affordability for every human first, then debits all of them
`ENTRY_FEE` and writes the file once — so escrow is atomic and can't half-charge.
`room.pot = humans × 10`. A player who can't pay is dropped from the room (as before).

## 3. Payout (PLAN step 6)

`settle` credits `room.pot` to the winning human's token via `credit()`, guarded by
`room.settled`, so a game can never pay out twice. If a bot wins, or the winner
already left, entries stay spent. `leave` refunds the entry only while the game has
not started (`room.paid && !room.started`) — an invariant guard, since charging
always coincides with the start, so a mid-game leaver loses the entry.

## 4. AUTOMATCH

- New message `automatch` queues the player (`{ token, name, ws }` + 30 s timer).
  Replies `matchQueued`, else `matchTimeout` ("No match found"); nothing is charged
  on timeout.
- `cancelMatch` removes the socket (`matchCancelled`); `ws.on('close')` also drops it.
  A token can't be queued twice, and grouping is distinct tokens, so no self-match.
- `tryMatch` forms `MATCH_SIZE = 2` players, drops any who can't pay, opens a room
  and calls `startIfFull` — which escrows the 10 NETCOINS from each server balance.
- Only the display name is passed around; no phone/email is stored or broadcast.
  Duplicate display names are disambiguated (`"Name 2"`) so payouts stay unambiguous.

## 5. Display of balances

`sendGame` and the `started` message now report `balanceOf(p.token)` instead of the
removed `p.balance`. Client-side sync into `Wallet` (PLAN step 7) was deliberately
left out per the "do not touch the Android app yet" instruction.

Files changed: `~/rafiki-server/server.js` only. Added `~/rafiki-server/balances.json`
(at runtime). No `.html` or build files were opened.

---

# RAFIKI Play coins: Android client

Android side of PLAN step 7 (client-side sync), matching the new server protocol.
No balance is ever sent from the app; identity is the token and the server owns the
money. Gradle was not run (not available here). No build folder or `.html` opened.

## 1. RafikiClient

- `setToken(String)` stores the auth token; `connect(...)` now injects it into the
  first message (`Api.token(this)` — `tuko_session`/"token") unless already present.
  Every `create` / `join` / `automatch` therefore carries the token the server needs.
- `getLastBalance()` surfaces the latest server balance. The onMessage handler records
  any message whose `balance` is present and non-null; `close()` resets it to `-1`
  ("unknown"). No outgoing message contains a balance.

## 2. RafikiActivity (QUICK MATCH)

- New `QUICK MATCH` button (`btnQuickMatch`) in the menu sends
  `{type:"automatch", name}` via `connect(...)` and shows a new Searching panel
  (`searchingPanel` + `searchingText` + `btnCancelSearch`).
- `Cancel` (and Back) sends `{type:"cancelMatch"}` and returns to the menu.
- `handle()` now covers: `matchQueued` (stays on Searching, shows the queue depth),
  `matchTimeout` (menu + AlertDialog "No match found, nothing was charged."),
  `matchCancelled` (menu), and the existing `started` (hands off to
  `RafikiGameActivity`). An `error` while searching drops back to the menu and toasts
  the server text — so "Not enough NETCOINS to play (10 required)" is shown clearly.

## 3. RafikiGameActivity (AMOUNT TO WIN + balance sync)

- New `amountToWin` TextView under the header shows the server's `pot`:
  `AMOUNT TO WIN: <pot> NETCOINS · <sh> sh` plus `1 sh = 10 NETCOINS`
  (`formatSh`, 1 sh = 10 NETCOINS).
- `syncBalance(m)` mirrors the server's `balance` into the local `Wallet`
  (`Wallet.setBalance`) — the same wallet the home screen reads — so the home balance
  reflects the entry debit and the win payout whenever a `game` message arrives.
- If the server reports a balance under `ENTRY_FEE` (10), it shows once per game:
  "Server balance is X NETCOINS — under the 10 NETCOIN entry. Top up to play again."

## Files changed

- `app/src/main/java/com/tukokadi/app/RafikiClient.java` — token injection, balance surfacing.
- `app/src/main/java/com/tukokadi/app/RafikiActivity.java` — QUICK MATCH, Searching panel, cancel, match messages.
- `app/src/main/java/com/tukokadi/app/RafikiGameActivity.java` — AMOUNT TO WIN label, balance sync, low-balance message.
- `app/src/main/res/layout/activity_rafiki.xml` — `btnQuickMatch`, `searchingPanel`/`searchingText`/`btnCancelSearch`.

---

# RAFIKI Play server: AUTOMATCH rebuilt with stake tiers

Server side only. `~/rafiki-server/server.js` was edited; the Android app was not
touched. Backed up first to `server.js.bak-before-tiers` (unchanged, 24829 bytes).
`node --check server.js` passes. The old flat AUTOMATCH (single 10 NETCOIN entry,
first-two-in-queue) is gone, replaced by four stake tiers.

## 1. Stake tiers and queue rules

- `STAKE_TIERS = [100, 200, 500, 1000]` NETCOINS (1 sh = 10 NETCOINS), still
  `MATCH_SIZE = 2`. `automatch` now takes `{ token, name, stake }`; `stake` must be
  one of the tiers or the server replies "Choose a stake of 100, 200, 500, 1000".
- Queues are `queues: Map<stake, entry[]>`; an entry is `{ token, name, ws, stake }`.
- A player can queue only if `balanceOf(token) >= stake` (checked with `accountFor`
  so a first-timer is registered first).
- One token, one queue: `findEntry(token)` rejects a second queue, and
  `tokenInAnyRoom(token)` additionally rejects queuing while seated in any room, so a
  token can never be dealt into two games from two sockets.
- A waiter stays queued `MATCH_WAIT_MS = 10 min`, then gets `matchTimeout`. `cancelMatch`
  removes it (`matchCancelled`). On disconnect the entry stays for
  `DISCONNECT_GRACE_MS = 60 s`; a reconnect with the same token within that window
  reattaches to the original tier and keeps its place, otherwise the entry is dropped.

## 2. Live tier counts

- New helper `tierCounts()` → `[ { stake, count } ]`, counting only entries with a
  live socket (a disconnected entry kept for the 60 s grace can't be matched).
- `broadcastTierCounts()` sends `{ type: 'tierCounts', tiers }` to every connected
  client not in a started room, whenever the counts change (enqueue, match, cancel,
  disconnect, requeue). A client can also ask with `{ type: 'tierCounts' }` and gets a
  one-off reply.

## 3. matchFound handshake (nothing charged yet)

- When two live, distinct-token players wait in the same tier, `beginNegotiation`
  removes both from the queue and sends each
  `{ type: 'matchFound', matchId, stake, fee, winAmount }`
  (`fee = stake * FEE_PERCENT / 100`, `FEE_PERCENT = 10`, `winAmount = 2*stake - 2*fee`).
- `ACCEPT_WINDOW_MS = 30 s`: both must send `acceptMatch` with that `matchId` before
  the timer fires. Accepter gets an `matchAccepted` ack.
- `acceptMatch` / `declineMatch` validate the `matchId` and that the socket owns an
  entry in it; stale ids get an `error`.

## 4. Escrow on double-accept, fees

- Only when both accept does `startNegotiatedMatch` run: it re-checks affordability,
  then debits `stake` from both tokens in one synchronous block and writes once
  (`saveBalances`) — atomic, cannot half-charge. `totalFees += fee * 2`, kept as a
  running server total.
- Room carries `entry = stake`, `pot = 2*stake`, `fee`, `winAmount`. `settle` pays the
  human winner `winAmount` (pot minus both fees) instead of the whole pot; Create Room
  / Add Bot rooms have no `winAmount`, so they still pay the whole `pot` as before.
  Example: stake 100 → pot 200, fees 20, winner 180.

## 5. Decline / timeout

- `finishMatch(match, reason)` charges nobody. Whoever **accepted** is put back at the
  **front** of their tier queue (`enqueueFront`, fresh 10-min timer) and gets
  `{ type: 'matchEnded', requeued: true }`; whoever declined, timed out, or disconnected
  is dropped with `requeued: false`. If neither accepted, both are dropped.
- Declining mid-handshake, a socket closing during the handshake (`ws.match` set on
  close), and the 30 s timer all route through `finishMatch`.

## 6. Preserved invariants

- Leaving after the game starts loses the stake: the escrow→`startGame` path sets
  `started` in the same tick, so `leave`'s refund guard (`room.paid && !room.started`)
  never fires for a tier match.
- A game never pays twice: `settle` keeps its `room.settled` guard.
- No self-match: queue entries are one-per-token and pairing uses distinct tokens.
- Only display names travel; `uniqueName` still de-duplicates names in a room.
- Create Room / Add Bot are untouched and still use `ENTRY_FEE = 10` and `startIfFull`.

## Verification

- `node --check server.js` passes.
- An isolated two-client smoke test (server on a spare port, its own temp copy of
  `server.js`, real `balances.json` never opened) confirmed: both clients get the same
  `matchFound` (stake 100, fee 10, winAmount 180); a spectator gets live `tierCounts`;
  decline charges nobody (both stay at 1000) and requeues only the accepter, who is then
  re-matched; double-accept escrows 100 from each (900/900) and starts a pot-200 game; a
  token already in a room is refused a second queue. All 15 assertions passed.

## Answers

- `RafikiClient.java:19` uses `wss://rafiki-server-cir7.onrender.com`.
- `balances.json` would **not** survive a redeploy on Render: it is written next to
  `server.js` (`__dirname`), Render's default filesystem is ephemeral, and it is
  `.gitignore`d so it is not rebuilt from the repo. It only persists if a Render
  persistent disk is mounted at that path.

Files changed: `~/rafiki-server/server.js` only. No build folder or `.html` file was
opened. The Android app was not modified.

---

# Feature audit (TukoKadi app)

Read-only audit of `app/src/main` (Java + layouts) and `~/rafiki-server/server.js`
against a 15-item feature list. Nothing was edited. Search method: case-insensitive
grep for each feature's keywords across `*.java`, `res/`, `AndroidManifest.xml` and
the server; results confirmed by reading the files cited. (This environment's
bundled ripgrep binary is missing, so plain `grep` was used.)

| # | Feature | Status | Where |
|---|---------|--------|-------|
| 1 | Tournaments with tickets | Missing | no source match anywhere |
| 2 | Gamers tab with head-to-head history | Missing | only aggregate `GameHistory` (no tab, no per-opponent) |
| 3 | Multi-opponent selection | Partial | `RafikiActivity.java:78-81`, `activity_rafiki.xml:48-81` |
| 4 | Incoming challenge screen | Missing | no source match |
| 5 | Live winners ticker | Missing | no source match |
| 6 | Multiple wallet balances | Partial | `Wallet.java`, `KibWallet.java`, `WalletActivity.java` |
| 7 | deposit / withdraw / swap / send gift | Partial | `WalletActivity.java:68-94` |
| 8 | M-Pesa deposit | Missing | no source match |
| 9 | Low-balance top-up | Partial | `RafikiGameActivity.java:629`, `MainActivity.java:552` |
| 10 | Settings (night mode, notifications, DND, preferred stake, auto match) | Missing | `game_header.xml:45` button is dead; auto-match elsewhere |
| 11 | Support contacts | Partial | `AuthActivity.java:65-67`, `HomeExtras.java:44-47` |
| 12 | Terms / Privacy | Missing | no source match |
| 13 | Daily gift | Exists | `DailyGiftActivity.java`, `Wallet.java:63-79` |
| 14 | Referrals | Missing | only share-as-link exists |
| 15 | Onboarding | Missing | only `SplashActivity` animation |

## Detail

**1. Tournaments with tickets — Missing.** No `tournament` or `ticket` token in any
`.java`, layout, or `server.js`. Nothing resembling brackets, entry tickets, or
prize pools.

**2. Gamers tab with head-to-head history — Missing.** The home screen is a single
vertical column (`activity_home.xml` includes `home_content.xml`) with no tabs. The
only history is aggregate win/loss counters in `GameHistory.java` (`bot_wins`,
`bot_losses`, `rafiki_wins`, `rafiki_losses`) surfaced by `BotHistoryActivity.java`
and `GameHistoryActivity.java`. There is no per-opponent record and no "Gamers"
surface; the Rafiki history button itself opens the "coming soon" screen
(`GameHistoryActivity.java:52-57`).

**3. Multi-opponent selection — Partial.** Selecting opponents is limited to choosing
a room *size*: the `sizeGroup` radio (2/3/4 players) in `activity_rafiki.xml:48-81`
is read in `RafikiActivity.java:78-81` and sent as `max`. `RafikiGameActivity.java`
lays out up to three opponent seats (`:110`, `:157`). There is no UI to pick specific
opponents; matchmaking is by room code or QUICK MATCH.

**4. Incoming challenge screen — Missing.** No `challenge` token in the app or the
server protocol; no accept/decline-by-player flow.

**5. Live winners ticker — Missing.** No `ticker` or `live winner` token anywhere.

**6. Multiple wallet balances — Partial.** Two independent local stores exist —
`Wallet.java` (`tuko_wallet`, read by the home balance and Bot Play) and
`KibWallet.java` (`kib_wallet`, only the Wallet screen's "Test top-up") — plus a
third server-side balance fetched from `/me` (`WalletActivity.java:106-123`).
They are not presented as a set of separate balances, and the Wallet screen is
internally inconsistent: the headline shows the server `/me` balance while DEPOSIT
credits the local `KibWallet`, so a top-up does not move the displayed number.

**7. deposit / withdraw / swap / send gift — Partial, mostly stubs.**
`WalletActivity.java:68-94`: DEPOSIT calls `KibWallet.add(this, 500, "Test top-up")`
(a fixed fake credit, not a real payment); WITHDRAW shows the toast "Withdraw coming
soon". There is no swap and no send-gift code (the root-level `add_gift_button.py` /
`wire_gift_button.py` scripts are stale one-off tooling, not part of the app).

**8. M-Pesa deposit — Missing.** No `mpesa` / `m-pesa` / STK-push code. The only
Safaricom-adjacent artifact is an unused constant: `AuthActivity.java:18` declares
`SAFARICOM_PATTERN`, but nothing references it (the live check, `isSafaricomNumber`,
uses its own inline regex and in practice accepts any 9-15 digit number or any email).

**9. Low-balance top-up — Partial (detection only, no top-up).** The client can detect
a low balance and tells the user to top up — `RafikiGameActivity.java:629`
("...NETCOIN entry. Top up to play again.") and `MainActivity.java:552`
`showInsufficientEntryDialog()` for Bot Play entry — but neither offers a top-up
action; the insufficient-entry dialog just `finish()`es the activity.

**10. Settings (night mode, notifications, DND, preferred stake, auto match) —
Missing.** There is no settings screen or preferences UI. An in-game settings button
exists in `game_header.xml:45` (`btnSettings`) and `GameHeader.java:33` exposes
`setOnSettingsClick(...)`, but nothing calls it (dead code) and
`RafikiGameActivity.java:98` hides the button. No night/dark mode, no notification or
DND handling, no preferred-stake preference. The one sub-item that exists lives
elsewhere: auto-match is QUICK MATCH — `RafikiActivity.java:115-125` sends
`{type:"automatch"}`, with cancel and timeout handling (`:138-146`, `:162-172`). Note
the server's tiered automatch expects a `stake` field that the client never sends
(`grep -i stake` over the app is empty), so the client and server AUTOMATCH protocol
are out of sync.

**11. Support contacts — Partial (placeholder).** `AuthActivity.java:65-67` wires
"Contact Support" to a toast, "Support contact coming soon". `HomeExtras.java:44-47`
("Contact Us" menu item) opens a `mailto:` to the placeholder `youremail@gmail.com`.
No real support numbers/emails are configured.

**12. Terms / Privacy — Missing.** No `terms` or `privacy` token in any layout,
activity, or string resource.

**13. Daily gift — Exists.** `Wallet.claimDailyGift` with a 24 h cooldown and weighted
reward (1,000-100,000), `Wallet.java:51-79`; UI in `DailyGiftActivity.java` /
`activity_daily_gift.xml`; reachable from the wallet popup (`HomeExtras.java:35-36`)
and a dedicated button (`HomeActivity.java:68-69`).

**14. Referrals — Missing.** No referral code, invite link, or credit tracking. A
generic share of the APK link exists (`HomeActivity.java:61-66` and
`HomeExtras.java:37-41`) but it carries no referrer identity.

**15. Onboarding — Missing.** The only pre-auth experience is the card-flip animation
in `SplashActivity.java`, which then goes straight to `AuthActivity`. No intro slides,
tutorial, or first-run walkthrough (the register flow merely toasts "Account created!
You received 1,000 Coins." at `AuthActivity.java:149`).

## Summary

Exists 1 / Partial 5 / Missing 9. The three "Exists/Partial" areas that are real
functionality are the daily gift, multi-opponent *room-size* selection, and the
history screens; deposit/withdraw/settings/support are stubs or dead code. No file
was modified.




