import re

path = "app/src/main/java/com/tukokadi/app/GameEngine.java"
with open(path, "r") as f:
    c = f.read()

pattern = (
    r'if \(rank == Card\.Rank\.TWO\) \{\s*'
    r'stackPenalty\(Card\.Rank\.TWO, 2\);\s*'
    r'\} else if \(rank == Card\.Rank\.THREE\) \{\s*'
    r'stackPenalty\(Card\.Rank\.THREE, 3\);\s*'
    r'\} else \{'
)

replacement = (
    'if (rank == Card.Rank.TWO) {\n'
    '            stackPenalty(Card.Rank.TWO, 2 * cards.size());\n'
    '        } else if (rank == Card.Rank.THREE) {\n'
    '            stackPenalty(Card.Rank.THREE, 3 * cards.size());\n'
    '        } else {'
)

c, n = re.subn(pattern, replacement, c)
print(f"Applied {n} fix(es) to group penalty stacking (expected 1).")

with open(path, "w") as f:
    f.write(c)
