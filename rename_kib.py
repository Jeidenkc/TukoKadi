files = {
    "app/src/main/java/com/tukokadi/app/AuthActivity.java": [
        (
            'Toast.makeText(this, "Account created! You received 1,000 KIB. Please log in.", Toast.LENGTH_LONG).show();',
            'Toast.makeText(this, "Account created! You received 1,000 Coins. Please log in.", Toast.LENGTH_LONG).show();'
        ),
    ],
    "app/src/main/java/com/tukokadi/app/HomeExtras.java": [
        (
            'if (ok) wallet.setText("KIB " + String.format("%,d", j.optLong("balance"))); else wallet.setText("KIB --");',
            'if (ok) wallet.setText(String.format("%,d", j.optLong("balance")) + " Coins"); else wallet.setText("-- Coins");'
        ),
    ],
    "app/src/main/java/com/tukokadi/app/WalletActivity.java": [
        (
            'balanceView.setText("KIB " + String.format("%,d", balance));',
            'balanceView.setText(String.format("%,d", balance) + " Coins");'
        ),
        (
            'tv.setText((amt >= 0 ? "+" : "") + amt + " KIB  " + t[1]',
            'tv.setText((amt >= 0 ? "+" : "") + amt + " Coins  " + t[1]'
        ),
    ],
}

for path, replacements in files.items():
    with open(path, "r") as f:
        content = f.read()
    changed = False
    for old, new in replacements:
        if old in content:
            content = content.replace(old, new)
            changed = True
        else:
            print(f"WARNING: pattern not found in {path}:\n  {old}")
    if changed:
        with open(path, "w") as f:
            f.write(content)
        print(f"Updated: {path}")
    else:
        print(f"No changes made to: {path}")

print("Done.")
