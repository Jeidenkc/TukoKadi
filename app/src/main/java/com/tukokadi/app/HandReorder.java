package com.tukokadi.app;

import android.view.DragEvent;
import android.view.View;
import android.widget.LinearLayout;
import java.util.List;

public class HandReorder {
    public static void attach(final LinearLayout row, final List<String> handCards,
                              final List<String> handOrder, final int overlapMargin,
                              final int firstMargin) {
        row.setOnDragListener((v, e) -> {
            switch (e.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DROP: {
                    Object state = e.getLocalState();
                    if (!(state instanceof View)) return true;
                    View dragged = (View) state;
                    int from = row.indexOfChild(dragged);
                    if (from < 0) return true;
                    int to = 0;
                    for (int k = 0; k < row.getChildCount(); k++) {
                        View ch = row.getChildAt(k);
                        if (ch == dragged) continue;
                        if (ch.getX() + ch.getWidth() * 0.1f < e.getX()) to++;
                    }
                    row.removeView(dragged);
                    row.addView(dragged, to);
                    String moved = handCards.remove(from);
                    handCards.add(to, moved);
                    handOrder.clear();
                    handOrder.addAll(handCards);
                    for (int k = 0; k < row.getChildCount(); k++) {
                        View ch = row.getChildAt(k);
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ch.getLayoutParams();
                        lp.leftMargin = (k == 0) ? firstMargin : overlapMargin;
                        ch.setLayoutParams(lp);
                    }
                    dragged.setVisibility(View.VISIBLE);
                    return true;
                }
                case DragEvent.ACTION_DRAG_ENDED: {
                    Object state = e.getLocalState();
                    if (state instanceof View) ((View) state).setVisibility(View.VISIBLE);
                    return true;
                }
                default:
                    return true;
            }
        });
    }
}
