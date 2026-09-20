import re

path = "app/src/main/res/layout/activity_auth.xml"
with open(path, "r") as f:
    c = f.read()

# 1. Root background -> gradient
c = c.replace(
    'android:background="#0B3D20"\n    android:fillViewport="true">',
    'android:background="@drawable/bg_login_gradient"\n    android:fillViewport="true">'
)

# 2. Title styling - add letter spacing + subtle shadow
c = c.replace(
    '''android:text="TUKO KADI"
        android:textColor="#D4AF37"
        android:textSize="32sp"
        android:textStyle="bold"/>''',
    '''android:text="TUKO KADI"
        android:textColor="#F4D160"
        android:textSize="36sp"
        android:textStyle="bold"
        android:letterSpacing="0.08"
        android:shadowColor="#66000000"
        android:shadowDx="0"
        android:shadowDy="3"
        android:shadowRadius="6"/>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="10dp"
        android:text="&#9824; &#9829; &#9830; &#9827;"
        android:textColor="#8AA88E"
        android:textSize="20sp"
        android:letterSpacing="0.3"/>'''
)

# 3. Style every EditText: background, text color, hint color, padding
c = c.replace(
    'android:background="#D4AF37"\n                android:padding="14dp"\n                android:textColorHint="#5C4A00"\n                android:textColor="#000000"',
    'android:background="@drawable/input_field_bg"\n                android:padding="16dp"\n                android:textColorHint="#8AA88E"\n                android:textColor="#F4E9C9"\n                android:textCursorDrawable="@null"'
)

# 4. Login button -> gradient gold, remove flat tint
c = c.replace(
    '''android:text="Login"
                android:textColor="#0B3D20"
                android:textStyle="bold"
                android:backgroundTint="#D4AF37"/>''',
    '''android:text="LOGIN"
                android:textColor="#0B3D20"
                android:textStyle="bold"
                android:textSize="17sp"
                android:letterSpacing="0.05"
                android:minHeight="56dp"
                android:layout_marginTop="6dp"
                android:background="@drawable/btn_login_gold"/>'''
)

# 5. Register/Create Account button -> same gold gradient style
c = c.replace(
    '''android:text="Create Account"
                android:textColor="#0B3D20"
                android:textStyle="bold"
                android:backgroundTint="#D4AF37"/>''',
    '''android:text="CREATE ACCOUNT"
                android:textColor="#0B3D20"
                android:textStyle="bold"
                android:textSize="17sp"
                android:letterSpacing="0.05"
                android:minHeight="56dp"
                android:background="@drawable/btn_login_gold"/>'''
)

# 6. "New here? Register" -> outlined secondary button with gold text
c = c.replace(
    '''android:text="New here? Register"
        android:textColor="#0B3D20"
        android:textStyle="bold"
        android:backgroundTint="#D4AF37"/>''',
    '''android:text="New here? Register"
        android:textColor="#F4D160"
        android:textStyle="bold"
        android:minHeight="52dp"
        android:background="@drawable/btn_register_outline"/>'''
)

# 7. Divider line -> softer gold with transparency
c = c.replace(
    '''android:background="#D4AF37"
        android:layout_marginTop="24dp"
        android:layout_marginBottom="12dp"/>''',
    '''android:background="#552F4A1A"
        android:layout_marginTop="28dp"
        android:layout_marginBottom="16dp"/>'''
)

# 8. Section headers (Login / Register) -> gold instead of plain white
c = c.replace(
    '''android:text="Login"
                android:textColor="#FFFFFF"
                android:textSize="22sp"''',
    '''android:text="Login"
                android:textColor="#F4D160"
                android:textSize="24sp"'''
)
c = c.replace(
    '''android:text="Register"
                android:textColor="#FFFFFF"
                android:textSize="22sp"''',
    '''android:text="Register"
                android:textColor="#F4D160"
                android:textSize="24sp"'''
)

with open(path, "w") as f:
    f.write(c)

print("Done. Login screen restyled successfully.")
