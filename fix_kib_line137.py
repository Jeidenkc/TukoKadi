path = "app/src/main/java/com/tukokadi/app/WalletActivity.java"
with open(path, "r") as f:
    content = f.read()

old = 'tv.setText((amt >= 0 ? "+" : "") + amt + " KIB  " + t[1]'
new = 'tv.setText((amt >= 0 ? "+" : "") + amt + " Coins  " + t[1]'

if old in content:
    content = content.replace(old, new)
    with open(path, "w") as f:
        f.write(content)
    print("Fixed line 137 successfully.")
else:
    print("ERROR: exact pattern still not found. Paste the grep -n output above so I can adjust.")
