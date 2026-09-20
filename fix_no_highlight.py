path = "app/src/main/java/com/tukokadi/app/MainActivity.java"
with open(path, "r") as f:
    c = f.read()

old = '''    private void highlightGroupCards(Card firstCard, List<Card> others) {
        clearGroupHighlights();
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < handOrder.size(); i++) {
            Card c = handOrder.get(i);
            if (c == firstCard || others.contains(c)) {
                View v = handLayout.getChildAt(i);
                if (v == null) continue;
                android.graphics.drawable.GradientDrawable glow =
                        new android.graphics.drawable.GradientDrawable();
                glow.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                glow.setStroke((int) (4 * density), Color.parseColor("#FFD700"));
                glow.setCornerRadius(16);
                glow.setColor(Color.TRANSPARENT);
                v.setForeground(glow);
                v.setElevation(24f);
                v.animate()
                        .translationY(-24f)
                        .scaleX(1.08f)
                        .scaleY(1.08f)
                        .setDuration(180)
                        .start();
                highlightedGroupViews.add(v);
            }
        }
    }'''

new = '''    private void highlightGroupCards(Card firstCard, List<Card> others) {
        clearGroupHighlights();
        // Highlighting disabled intentionally - player chooses freely without visual hints.
    }'''

if old not in c:
    print("ERROR: exact block not found, aborting.")
else:
    c = c.replace(old, new, 1)
    with open(path, "w") as f:
        f.write(c)
    print("Done. Group-play highlighting fully disabled.")
