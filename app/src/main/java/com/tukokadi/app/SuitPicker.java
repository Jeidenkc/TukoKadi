package com.tukokadi.app;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SuitPicker {
    public interface Callback {
        void onPick(String suitCode);
    }

    public static void show(Activity act, final Callback cb) {
        final float d = act.getResources().getDisplayMetrics().density;
        final Dialog dialog = new Dialog(act);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        LinearLayout box = new LinearLayout(act);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        int pad = (int) (12 * d);
        box.setPadding(pad, pad, pad, pad);
        GradientDrawable boxBg = new GradientDrawable();
        boxBg.setColor(Color.argb(70, 0, 0, 0));
        boxBg.setCornerRadius(24 * d);
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
                tile.setTextSize(48);
                tile.setTypeface(null, Typeface.BOLD);
                tile.setGravity(Gravity.CENTER);
                tile.setTextColor(i < 2 ? Color.parseColor("#E53935") : Color.BLACK);
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(Color.argb(90, 255, 255, 255));
                bg.setCornerRadius(20 * d);
                bg.setStroke((int) (2 * d), Color.parseColor("#FFD54A"));
                tile.setBackground(bg);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (96 * d), (int) (96 * d));
                int m = (int) (6 * d);
                lp.setMargins(m, m, m, m);
                tile.setLayoutParams(lp);
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
            w.setDimAmount(0.15f);
        }
        dialog.show();
    }
}
