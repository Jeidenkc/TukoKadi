package com.tukokadi.app;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RafikiGroupPicker {

    public interface Callback {
        void onPlay(List<String> group); // the LAST card in the list ends up on top of the pile
    }

    private static final int GOLD = 0xFFFFD54A;

    private static ViewGroup contentRef;
    private static ViewGroup handRef;
    private static View bar;
    private static Callback callback;
    private static String first;
    private static List<String> handCardsRef = new ArrayList<>();
    private static List<String> candidates = new ArrayList<>();
    private static final List<String> sequence = new ArrayList<>();
    private static final Map<String, ObjectAnimator> shakes = new HashMap<>();
    private static ObjectAnimator playPulse;
    private static float density = 1f;

    private static int dp(int v) {
        return (int) (v * density);
    }

    public static void start(Activity act, ViewGroup hand, List<String> handCards,
                             String firstCard, List<String> otherCards, Callback cb) {
        dismiss();
        density = act.getResources().getDisplayMetrics().density;
        handRef = hand;
        handCardsRef = new ArrayList<>(handCards);
        first = firstCard;
        callback = cb;
        candidates = new ArrayList<>();
        candidates.add(firstCard);
        candidates.addAll(otherCards);
        sequence.clear();
        sequence.addAll(candidates);

        ViewParent p = hand;
        while (p instanceof ViewGroup) {
            ((ViewGroup) p).setClipChildren(false);
            ((ViewGroup) p).setClipToPadding(false);
            p = p.getParent();
        }
        refresh();
        showBar(act);
    }

    // returns true if the tap was used by the picker
    public static boolean handleTap(String card) {
        if (first == null) return false;
        if (!candidates.contains(card)) {
            dismiss();
            return false;
        }
        if (card.equals(first)) return true; // the tapped card always plays first
        String top = sequence.isEmpty() ? null : sequence.get(sequence.size() - 1);
        if (card.equals(top)) {
            sequence.remove(card);   // tapping the top card again leaves it out
        } else {
            sequence.remove(card);
            sequence.add(card);      // becomes the new top card
        }
        refresh();
        return true;
    }

    public static void dismiss() {
        for (String c : new ArrayList<>(shakes.keySet())) stopShake(c);
        if (playPulse != null) {
            playPulse.cancel();
            playPulse = null;
        }
        if (handRef != null) {
            for (String c : candidates) {
                View v = viewOf(c);
                if (v != null) {
                    v.animate().translationY(0f).scaleX(1f).scaleY(1f).setDuration(120).start();
                    v.setTranslationZ(0f);
                    v.setRotation(0f);
                    clearGlow(v);
                }
            }
        }
        if (bar != null && contentRef != null) contentRef.removeView(bar);
        bar = null;
        contentRef = null;
        handRef = null;
        first = null;
        callback = null;
        candidates = new ArrayList<>();
        sequence.clear();
    }

    // ---------- looks ----------
    private static View viewOf(String card) {
        int idx = handCardsRef.indexOf(card);
        if (handRef == null || idx < 0 || idx >= handRef.getChildCount()) return null;
        return handRef.getChildAt(idx);
    }

    private static void refresh() {
        if (sequence.isEmpty()) return;
        String top = sequence.get(sequence.size() - 1);
        for (String c : candidates) {
            View v = viewOf(c);
            if (v == null) continue;
            stopShake(c);
            boolean included = sequence.contains(c);
            if (!included) {
                style(v, 0, 1f, 0, false, false);
            } else if (c.equals(top)) {
                style(v, 46, 1.08f, 16, true, true);
            } else {
                style(v, 16, 1f, 8, true, false);
                startShake(c, v);
            }
        }
    }

    private static void style(View v, int liftDp, float scale, int zDp, boolean glow, boolean strong) {
        v.animate().translationY(-liftDp * density).scaleX(scale).scaleY(scale).setDuration(160).start();
        v.setTranslationZ(zDp * density);
        if (glow) setGlow(v, strong);
        else clearGlow(v);
    }

    private static void setGlow(View v, boolean strong) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.TRANSPARENT);
        g.setCornerRadius(10 * density);
        g.setStroke((int) ((strong ? 4 : 3) * density), strong ? 0xFFFFF176 : GOLD);
        v.setForeground(g);
        if (Build.VERSION.SDK_INT >= 28) {
            v.setOutlineSpotShadowColor(GOLD);
            v.setOutlineAmbientShadowColor(GOLD);
        }
        v.setElevation((strong ? 14 : 8) * density);
    }

    private static void clearGlow(View v) {
        v.setForeground(null);
        v.setElevation(0f);
    }

    private static void startShake(String card, View v) {
        ObjectAnimator a = ObjectAnimator.ofFloat(v, View.ROTATION, -1.8f, 1.8f);
        a.setDuration(90);
        a.setRepeatCount(ObjectAnimator.INFINITE);
        a.setRepeatMode(ObjectAnimator.REVERSE);
        a.start();
        shakes.put(card, a);
    }

    private static void stopShake(String card) {
        ObjectAnimator a = shakes.remove(card);
        if (a != null) a.cancel();
        View v = viewOf(card);
        if (v != null) v.setRotation(0f);
    }

    // ---------- Ignore / Play buttons ----------
    private static void showBar(Activity act) {
        final ViewGroup content = (ViewGroup) act.findViewById(android.R.id.content);
        contentRef = content;

        LinearLayout row = new LinearLayout(act);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        Button ignore = makeButton(act, "Ignore", false);
        Button play = makeButton(act, "Play", true);

        ignore.setOnClickListener(v -> {
            Callback cb = callback;
            List<String> single = new ArrayList<>();
            single.add(first);
            dismiss();
            if (cb != null) cb.onPlay(single);
        });
        play.setOnClickListener(v -> {
            Callback cb = callback;
            List<String> group = new ArrayList<>(sequence);
            dismiss();
            if (cb != null) cb.onPlay(group);
        });

        row.addView(ignore);
        row.addView(play);

        Rect r = new Rect();
        int[] loc = new int[2];
        content.getLocationOnScreen(loc);
        int top = 0;
        if (handRef != null && handRef.getGlobalVisibleRect(r)) top = r.top - loc[1] - dp(112);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        lp.topMargin = Math.max(top, 0);
        content.addView(row, lp);
        bar = row;

        playPulse = ObjectAnimator.ofPropertyValuesHolder(play,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.08f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.08f));
        playPulse.setDuration(650);
        playPulse.setRepeatCount(ObjectAnimator.INFINITE);
        playPulse.setRepeatMode(ObjectAnimator.REVERSE);
        playPulse.start();
    }

    private static Button makeButton(Activity act, String label, boolean isPlay) {
        Button b = new Button(act);
        b.setText(label);
        b.setTypeface(null, Typeface.BOLD);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(100 * density);
        if (isPlay) {
            bg.setColor(Color.parseColor("#FFD700"));
            b.setTextColor(Color.parseColor("#0B3D20"));
        } else {
            bg.setColor(Color.parseColor("#33FFFFFF"));
            bg.setStroke((int) (2 * density), Color.WHITE);
            b.setTextColor(Color.WHITE);
        }
        b.setBackground(bg);
        b.setStateListAnimator(null);
        b.setPadding(dp(32), dp(10), dp(32), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(48));
        lp.setMargins(dp(8), 0, dp(8), 0);
        b.setLayoutParams(lp);
        if (isPlay) {
            b.setElevation(12 * density);
            if (Build.VERSION.SDK_INT >= 28) {
                b.setOutlineSpotShadowColor(GOLD);
                b.setOutlineAmbientShadowColor(GOLD);
            }
        }
        return b;
    }
}
