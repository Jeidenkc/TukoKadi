import re

path = "app/src/main/java/com/tukokadi/app/RafikiGameActivity.java"
with open(path, "r") as f:
    c = f.read()

changes = 0

# Replace the base size + shrink block with Bot Play's screen-ratio formula
old_block = re.compile(
    r'float baseHandW = 92, baseHandH = 132;\s*'
    r'float basePileW = 84, basePileH = 120;\s*'
    r'float shrink = 1f - \(extra \* 0\.14f\);\s*'
    r'shrink = Math\.max\(shrink, 0\.62f\);\s*'
    r'cardW = \(int\) \(dp\(\(int\) baseHandW\) \* shrink\);\s*'
    r'cardH = \(int\) \(dp\(\(int\) baseHandH\) \* shrink\);\s*'
    r'pileCardW = \(int\) \(dp\(\(int\) basePileW\) \* shrink\);\s*'
    r'pileCardH = \(int\) \(dp\(\(int\) basePileH\) \* shrink\);'
)

new_block = (
    'int screenH = getResources().getDisplayMetrics().heightPixels;\n'
    '        cardH = (int) (screenH * 0.22f);\n'
    '        cardW = (int) (cardH * 0.7f);\n'
    '        pileCardW = cardW;\n'
    '        pileCardH = cardH;'
)

c, n = old_block.subn(new_block, c)
changes += n
print(f"Card sizing block replaced: {n} (expected 1)")

# Loosen hand overlap margin to match Bot Play (0.80f instead of 0.42f)
c2, n2 = re.subn(
    r'int overlapMargin = -\(int\) \(cardW \* 0\.42f\);',
    'int overlapMargin = -(int) (cardW * 0.80f);',
    c
)
changes += n2
print(f"Hand overlap margin updated: {n2} (expected 1)")

# Loosen opponent overlap margin to match (0.80f instead of 0.60f)
c3, n3 = re.subn(
    r'int oppOverlapMargin = -\(int\) \(cardW \* 0\.60f\);',
    'int oppOverlapMargin = -(int) (cardW * 0.80f);',
    c2
)
changes += n3
print(f"Opponent overlap margin updated: {n3} (expected 1)")

with open(path, "w") as f:
    f.write(c3)

print(f"Total changes: {changes} (expected 3)")
