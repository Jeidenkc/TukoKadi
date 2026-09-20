path = "app/src/main/res/layout/activity_home.xml"
with open(path, "r") as f:
    c = f.read()

anchor = '''        <Button
            android:id="@+id/menuButton"'''

gift_button = '''        <Button
            android:id="@+id/dailyGiftButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:background="@android:color/transparent"
            android:text="&#127873; Gift"
            android:textColor="#F4D160"
            android:textStyle="bold"
            android:textSize="18sp" />

        <Button
            android:id="@+id/menuButton"'''

if anchor not in c:
    print("ERROR: menuButton anchor not found, aborting.")
else:
    c = c.replace(anchor, gift_button, 1)
    with open(path, "w") as f:
        f.write(c)
    print("Done. Daily Gift button added to layout.")
