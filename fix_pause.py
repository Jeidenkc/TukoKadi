path = "app/src/main/java/com/tukokadi/app/MainActivity.java"
with open(path, "r") as f:
    content = f.read()

marker = "    @Override\n    protected void onDestroy() {"
if marker not in content:
    print("ERROR: onDestroy marker not found, aborting.")
    exit(1)

onpause_method = '''    @Override
    protected void onPause() {
        super.onPause();
        soundHandler.removeCallbacksAndMessages(null);
        cancelTurnTimer();
        cancelBotTimer();
    }

'''

content = content.replace(marker, onpause_method + marker, 1)

with open(path, "w") as f:
    f.write(content)

print("Done. onPause() added successfully.")
