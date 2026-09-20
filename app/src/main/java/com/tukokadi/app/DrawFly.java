package com.tukokadi.app;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

public class DrawFly {
    public static void fly(final Activity act, View pile, final ViewGroup hand) {
        final Rect from = new Rect();
        if (!pile.getGlobalVisibleRect(from)) return;
        final ViewGroup content = act.findViewById(android.R.id.content);
        hand.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                hand.getViewTreeObserver().removeOnPreDrawListener(this);
                int n = hand.getChildCount();
                View target = n > 0 ? hand.getChildAt(n - 1) : hand;
                Rect to = new Rect();
                if (!target.getGlobalVisibleRect(to)) return true;
                int[] loc = new int[2];
                content.getLocationOnScreen(loc);
                final CardView card = new CardView(act);
                card.setFaceDown(true);
                content.addView(card, new FrameLayout.LayoutParams(from.width(), from.height()));
                card.setPivotX(0f);
                card.setPivotY(0f);
                card.setX(from.left - loc[0]);
                card.setY(from.top - loc[1]);
                float scale = to.width() / (float) from.width();
                card.animate()
                    .x(to.left - loc[0])
                    .y(to.top - loc[1])
                    .scaleX(scale)
                    .scaleY(scale)
                    .rotation(-6f)
                    .setDuration(320)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> content.removeView(card))
                    .start();
                return true;
            }
        });
    }
}
