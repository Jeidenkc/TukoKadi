import re

path = "app/src/main/java/com/tukokadi/app/GameEngine.java"
with open(path, "r") as f:
    c = f.read()

changes = 0

# canPlay(): declaredSuit branch - remove rank fallback
pattern1 = r'return card\.suit == declaredSuit\s*\|\|\s*card\.rank == topCard\(\)\.rank;'
c, n = re.subn(pattern1, 'return card.suit == declaredSuit;', c)
changes += n

# canPlay(): final fallback - suit only, not matches()
pattern2 = r'return card\.matches\(topCard\(\)\);'
c, n = re.subn(pattern2, 'return card.suit == topCard().suit;', c)
changes += n

# canSimPlay(): simDeclaredSuit branch - remove rank fallback
pattern3 = r'return card\.suit == simDeclaredSuit\s*\|\|\s*card\.rank == top\.rank;'
c, n = re.subn(pattern3, 'return card.suit == simDeclaredSuit;', c)
changes += n

# canSimPlay(): final fallback - suit only
pattern4 = r'return card\.matches\(top\);'
c, n = re.subn(pattern4, 'return card.suit == top.suit;', c)
changes += n

print(f"Applied {changes} suit-only fixes (expected 4).")

with open(path, "w") as f:
    f.write(c)
