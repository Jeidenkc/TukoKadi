path = "app/src/main/java/com/tukokadi/app/HomeExtras.java"
with open(path, "r") as f:
    c = f.read()

old = '''    public static void refreshBalance(Activity a) {
        Button wallet = (Button) a.findViewById(R.id.walletButton);
        Api.get("/me", Api.token(a), (ok, j, e) -> { if (ok) wallet.setText(String.format("%,d", j.optLong("balance")) + " Coins"); else wallet.setText("-- Coins"); });
    }'''

new = '''    public static void refreshBalance(Activity a) {
        Button wallet = (Button) a.findViewById(R.id.walletButton);
        Wallet.grantStartingBonusIfNeeded(a);
        long balance = Wallet.getBalance(a);
        wallet.setText(String.format("%,d", balance) + " Coins");
    }'''

if old not in c:
    print("ERROR: exact refreshBalance block not found, aborting.")
    exit(1)

c = c.replace(old, new)
with open(path, "w") as f:
    f.write(c)
print("Done. HomeExtras now uses local Wallet.")
