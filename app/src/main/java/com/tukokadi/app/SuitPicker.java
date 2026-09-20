package com.tukokadi.app;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SuitPicker {
    public interface Callback {
        void onPick(String suitCode);
    }

    private static LayerDrawable dome(float d) {
        GradientDrawable base = new GradientDrawable();
        base.setShape(GradientDrawable.OVAL);
        base.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        base.setGradientRadius(60 * d);
        base.setGradientCenter(0.4f, 0.3f);
        base.setColors(new int[]{0xFFFFFFFF, 0xFFF0F0F0, 0xFFB8B8B8});
        base.setStroke((int) (3 * d), Color.parseColor("#FFD54A"));

        GradientDrawable shine = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.argb(190, 255, 255, 255), Color.argb(0, 255, 255, 255)});
        shine.setShape(GradientDrawable.OVAL);

        LayerDrawable ld = new LayerDrawable(new Drawable[]{base, shine});
        ld.setLayerInset(1, (int) (16 * d), (int) (7 * d), (int) (16 * d), (int) (48 * d));
        return ld;
    }

    public static void show(Activity act, final Callback cb) {
        final float d = act.getResources().getDisplayMetrics().density;
        final Dialog dialog = new Dialog(act);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        LinearLayout box = new LinearLayout(act);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        int pad = (int) (16 * d);
        box.setPadding(pad, pad, pad, pad);
        GradientDrawable boxBg = new GradientDrawable();
        boxBg.setColor(Color.argb(150, 0, 0, 0));
        boxBg.setCornerRadius(28 * d);
        box.setBackground(boxBg);

        final String[] codes = {"H", "D", "C", "S"};
        String[] symbols = {"\u2665", "\u2666", "\u2663", "\u2660"};
        for (int r = 0; r < 2; r++) {
            LinearLayout row = new LinearLayout(act);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int c = 0; c < 2; c++) {
                final int i = r * 2 + c;
                TextView tile = new TextView(act);
                tile.setText(symbols[i]);
                tile.setTextSize(44);
                tile.setTypeface(null, Typeface.BOLD);
                tile.setIncludeFontPadding(false);
                tile.setGravity(Gravity.CENTER);
                tile.setTextColor(i < 2 ? Color.parseColor("#E53935") : Color.BLACK);
                tile.setBackground(dome(d));
                tile.setElevation(8 * d);
                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams((int) (88 * d), (int) (88 * d));
                int m = (int) (8 * d);
                lp.setMargins(m, m, m, m);
                tile.setLayoutParams(lp);
                tile.setOnTouchListener((v, ev) -> {
                    int a = ev.getAction();
                    if (a == MotionEvent.ACTION_DOWN) {
                        v.setScaleX(0.92f);
                        v.setScaleY(0.92f);
                    } else if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) {
                        v.setScaleX(1f);
                        v.setScaleY(1f);
                    }
                    return false;
                });
                tile.setOnClickListener(v -> {
                    dialog.dismiss();
                    cb.onPick(codes[i]);
                });
                row.addView(tile);
            }
            box.addView(row);
        }
        dialog.setContentView(box);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setDimAmount(0.35f);
        }
        dialog.show();
    }
}
