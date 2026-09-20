path = "app/src/main/java/com/tukokadi/app/MainActivity.java"
with open(path, "r") as f:
    c = f.read()

old = "        handScroll.addView(handLayout);"
new = ("        handScroll.setClipChildren(false);\n"
       "        handScroll.setClipToPadding(false);\n"
       "        handLayout.setClipChildren(false);\n"
       "        handLayout.setClipToPadding(false);\n"
       "        handScroll.addView(handLayout);")

if old not in c:
    print("ERROR: anchor not found")
else:
    c = c.replace(old, new, 1)
    with open(path, "w") as f:
        f.write(c)
    print("Done. Clip children disabled on hand row.")
