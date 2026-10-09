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




