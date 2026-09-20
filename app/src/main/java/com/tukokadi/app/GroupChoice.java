package com.tukokadi.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupChoice {
    public interface Callback {
        void onPlay(List<String> group);
    }

    private static ViewGroup contentRef;
    private static ViewGroup handRef;
    private static View bar;
    private static String first;
    private static List<String> cardsRef = new ArrayList<>();
    private static List<String> others = new ArrayList<>();
    private static final Set<String> selected = new HashSet<>();
    private static float density = 1f;

    private static int dp(int v) {
        return (int) (v * density);
    }

    public static void start(final Activity act, ViewGroup hand, List<String> handCards,
                             String firstCard, List<String> otherCards, final Callback cb) {
        dismiss();
        density = act.getResources().getDisplayMetrics().density;
        handRef = hand;
        cardsRef = new ArrayList<>(handCards);
        first = firstCard;
        others = new ArrayList<>(otherCards);
        selected.addAll(otherCards);

        ViewParent p = hand;
        while (p instanceof ViewGroup) {
            ((ViewGroup) p).setClipChildren(false);
            ((ViewGroup) p).setClipToPadding(false);
            p = p.getParent();
        }

        setLift(firstCard, true, false);
        for (String c : others) setLift(c, true, true);
        showBar(act, cb);
    }

    public static boolean handleTap(String card) {
        if (first == null) return false;
        if (card.equals(first)) return true;
        if (others.contains(card)) {
            if (selected.contains(card)) {
                selected.remove(card);
                setLift(card, false, false);
            } else {
                selected.add(card);
                setLift(card, true, true);
            }
            return true;
        }
        dismiss();
        return false;
    }

    public static void dismiss() {
        if (bar != null && contentRef != null) contentRef.removeView(bar);
        bar = null;
        if (handRef != null && first != null) {
            setLift(first, false, false);
            for (String c : others) setLift(c, false, false);
        }
        first = null;
        others = new ArrayList<>();
        selected.clear();
        handRef = null;
        contentRef = null;
    }

    private static void setLift(String card, boolean lifted, boolean glow) {
        int idx = cardsRef.indexOf(card);
        if (handRef == null || idx < 0 || idx >= handRef.getChildCount()) return;
        View v = handRef.getChildAt(idx);
        v.animate().translationY(lifted ? -dp(glow ? 24 : 12) : 0f).setDuration(160).start();
        v.setTranslationZ(lifted ? dp(8) : 0f);
        if (v instanceof GlowCardView) ((GlowCardView) v).setGlow(glow && lifted);
    }

    private static void showBar(Activity act, final Callback cb) {
        final ViewGroup content = act.findViewById(android.R.id.content);
        contentRef = content;
        LinearLayout row = new LinearLayout(act);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.addView(makeButton(act, "Play", "#2E7D32", v -> {
            List<String> group = new ArrayList<>();
            group.add(first);
            for (String c : others) if (selected.contains(c)) group.add(c);
            dismiss();
            cb.onPlay(group);
        }));
        row.addView(makeButton(act, "Ignore", "#C62828", v -> {
            List<String> single = new ArrayList<>();
            single.add(first);
            dismiss();
            cb.onPlay(single);
        }));
        Rect r = new Rect();
        int[] loc = new int[2];
        content.getLocationOnScreen(loc);
        int top = 0;
        if (handRef != null && handRef.getGlobalVisibleRect(r)) top = r.top - loc[1] - dp(84);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        lp.topMargin = Math.max(top, 0);
        content.addView(row, lp);
        bar = row;
    }

    private static Button makeButton(Activity act, String label, String color, View.OnClickListener l) {
        Button b = new Button(act);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setTypeface(null, Typeface.BOLD);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(color));
        bg.setCornerRadius(dp(24));
        bg.setStroke(dp(2), Color.WHITE);
        b.setBackground(bg);
        b.setPadding(dp(28), dp(8), dp(28), dp(8));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(48));
        lp.setMargins(dp(8), 0, dp(8), 0);
        b.setLayoutParams(lp);
        b.setOnClickListener(l);
        return b;
    }
}
