path = "app/src/main/java/com/tukokadi/app/HomeActivity.java"
with open(path, "r") as f:
    c = f.read()

anchor = "        HomeExtras.setup(this);"

addition = '''        HomeExtras.setup(this);

        findViewById(R.id.dailyGiftButton).setOnClickListener(v ->
                startActivity(new android.content.Intent(this, DailyGiftActivity.class)));'''

if anchor not in c:
    print("ERROR: anchor line not found, aborting.")
else:
    c = c.replace(anchor, addition, 1)
    with open(path, "w") as f:
        f.write(c)
    print("Done. Daily Gift button wired to launch DailyGiftActivity.")
