path = "app/src/main/java/com/tukokadi/app/RafikiActivity.java"
with open(path, "r") as f:
    c = f.read()

old = "        nameInput = findViewById(R.id.nameInput);"

new = '''        nameInput = findViewById(R.id.nameInput);

        String __phone = getSharedPreferences("tuko_session", MODE_PRIVATE).getString("phone", null);
        String __savedName = __phone != null
                ? getSharedPreferences("tuko_kadi_users", MODE_PRIVATE).getString(__phone + "_name", "")
                : "";
        if (!__savedName.isEmpty()) {
            nameInput.setText(__savedName);
            nameInput.setEnabled(false);
            nameInput.setFocusable(false);
        }'''

if old not in c:
    print("ERROR: anchor line not found, aborting.")
    exit(1)

c = c.replace(old, new, 1)
with open(path, "w") as f:
    f.write(c)
print("Done. RafikiActivity now auto-fills the registered name.")
