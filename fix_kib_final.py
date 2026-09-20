import re

path = "app/src/main/java/com/tukokadi/app/WalletActivity.java"
with open(path, "r") as f:
    content = f.read()

count_before = content.count("KIB")
if count_before == 0:
    print("No KIB found — already clean.")
else:
    # Replace KIB with Coins wherever it appears, regardless of surrounding whitespace
    content = content.replace("KIB", "Coins")
    with open(path, "w") as f:
        f.write(content)
    print(f"Replaced {count_before} occurrence(s) of 'KIB' with 'Coins'.")
