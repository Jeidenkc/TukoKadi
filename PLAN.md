# RAFIKI Play coins — plan

1. Now: server.js holds per-room players with client-supplied `balance`/`token`; `ENTRY_FEE=10`, `startIfFull` rejects humans under 10 then deducts 10 each and sets `pot = humans*10`; `settle` pays `pot` once to a human winner, bots/leavers forfeit.
2. RafikiClient is a thin WebSocket relay (connect/send/close + last `game`); it never touches `Wallet`, and RafikiGameActivity ignores the server's `balance`/`pot`.
3. Gap: balance is trusted from the client (spoofable) and the real local `Wallet` is never debited/credited, so entry/payout live only in server memory and vanish on reconnect.
4. Change: authenticate each player by `token` and read balance from a server-side account store, not `msg.balance`.
5. Change: in `startIfFull`, atomically escrow each human's 10 into `pot` (reject/rollback when short) and persist the debit.
6. Change: in `settle`, credit `pot` once to the human winner in that store; forfeit entries on bot-win/leave; refund escrow on leave only if `!room.started`.
7. Change: RafikiClient surfaces the latest server `balance`; RafikiGameActivity syncs it via `Wallet.setBalance` so the home screen reflects entry and payout.
8. Keep `ENTRY_FEE=10` and 1 sh = 10 NETCOINS to match Bot Play.
