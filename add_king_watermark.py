path = "app/src/main/res/layout/activity_auth.xml"
with open(path, "r") as f:
    c = f.read()

old_open = '''<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/bg_login_gradient"
    android:fillViewport="true">'''

new_open = '''<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/bg_login_gradient">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="K&#9824;"
        android:textColor="#1FD4AF37"
        android:textSize="260sp"
        android:textStyle="bold"
        android:rotation="-18"
        android:fontFamily="serif"/>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="top|end"
        android:layout_marginTop="60dp"
        android:layout_marginEnd="-40dp"
        android:text="K&#9829;"
        android:textColor="#14D4AF37"
        android:textSize="140sp"
        android:textStyle="bold"
        android:rotation="14"
        android:fontFamily="serif"/>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|start"
        android:layout_marginBottom="80dp"
        android:layout_marginStart="-30dp"
        android:text="K&#9830;"
        android:textColor="#14D4AF37"
        android:textSize="140sp"
        android:textStyle="bold"
        android:rotation="-10"
        android:fontFamily="serif"/>

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@android:color/transparent"
        android:fillViewport="true">'''

if old_open not in c:
    print("ERROR: root ScrollView opening not found, aborting.")
    exit(1)

c = c.replace(old_open, new_open, 1)

# Close FrameLayout at the very end, after the last </ScrollView>
idx = c.rfind("</ScrollView>")
if idx == -1:
    print("ERROR: closing ScrollView not found, aborting.")
    exit(1)

c = c[:idx] + "</ScrollView>\n\n</FrameLayout>" + c[idx+len("</ScrollView>"):]

with open(path, "w") as f:
    f.write(c)

print("Done. King card watermark added successfully.")
