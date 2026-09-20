path = "app/src/main/java/com/tukokadi/app/MainActivity.java"
with open(path, "r") as f:
    c = f.read()

changes = 0

# Add a name label for Bob, right after bobColumn.addView(bobAvatar, bobAvLp);
old_bob = "        bobColumn.addView(bobAvatar, bobAvLp);\n        bobColumn.addView(bobTimerText);"
new_bob = '''        bobColumn.addView(bobAvatar, bobAvLp);
        TextView bobNameLabel = new TextView(this);
        bobNameLabel.setText("Bob");
        bobNameLabel.setTextColor(Color.parseColor("#F4D160"));
        bobNameLabel.setTextSize(14);
        bobNameLabel.setGravity(Gravity.CENTER);
        bobNameLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams bobNameLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bobNameLp.topMargin = dp(4);
        bobColumn.addView(bobNameLabel, bobNameLp);
        bobColumn.addView(bobTimerText);'''

c, n = (c.replace(old_bob, new_bob, 1), 1 if old_bob in c else 0)
changes += n
print(f"Bob label added: {n} (expected 1)")

# Same for Carol
old_carol = "        carolColumn.addView(carolAvatar, carolAvLp);\n        carolColumn.addView(carolTimerText);"
new_carol = '''        carolColumn.addView(carolAvatar, carolAvLp);
        TextView carolNameLabel = new TextView(this);
        carolNameLabel.setText("Carol");
        carolNameLabel.setTextColor(Color.parseColor("#F4D160"));
        carolNameLabel.setTextSize(14);
        carolNameLabel.setGravity(Gravity.CENTER);
        carolNameLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams carolNameLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        carolNameLp.topMargin = dp(4);
        carolColumn.addView(carolNameLabel, carolNameLp);
        carolColumn.addView(carolTimerText);'''

c, n = (c.replace(old_carol, new_carol, 1), 1 if old_carol in c else 0)
changes += n
print(f"Carol label added: {n} (expected 1)")

with open(path, "w") as f:
    f.write(c)

print(f"Total: {changes} (expected 2)")
