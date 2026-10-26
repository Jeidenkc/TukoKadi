package com.tukokadi.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BotLevelActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
        float d = dm.density;
        int photoDp = (int) ((dm.heightPixels / d - 175) / 3);
        if (photoDp < 110) photoDp = 110;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundResource(R.drawable.bg_home_gradient);
        root.setPadding(0, (int) (12 * d), 0, (int) (8 * d));

        TextView title = new TextView(this);
        title.setText("Choose Level");
        title.setTextColor(Color.parseColor("#F3EFE6"));
        title.setTypeface(Typeface.SERIF, Typeface.BOLD);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tp.bottomMargin = (int) (12 * d);
        root.addView(title, tp);

        int[] pics = { R.drawable.level_easy, R.drawable.level_hard, R.drawable.level_difficult };
        String[] names = { "EASY", "HARD", "DIFFICULT" };
        for (int i = 0; i < names.length; i++) {
            final int level = i;
            FrameLayout card = new FrameLayout(this);
            ImageView iv = new ImageView(this);
            iv.setImageResource(pics[i]);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            card.addView(iv, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

            TextView label = new TextView(this);
            label.setText(names[i]);
            label.setTextColor(Color.parseColor("#F3EFE6"));
            label.setTypeface(Typeface.SERIF, Typeface.BOLD);
            label.setTextSize(20);
            label.setPadding((int) (16 * d), (int) (6 * d), (int) (16 * d), (int) (6 * d));
            GradientDrawable pill = new GradientDrawable();
            pill.setColor(Color.parseColor("#99000000"));
            pill.setCornerRadius(20 * d);
            label.setBackground(pill);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            lp.bottomMargin = (int) (12 * d);
            card.addView(label, lp);

            final float rr = 16 * d;
            card.setOutlineProvider(new android.view.ViewOutlineProvider() {
                @Override
                public void getOutline(android.view.View view, android.graphics.Outline o) {
                    o.setRoundRect(0, 0, view.getWidth(), view.getHeight(), rr);
                }
            });
            card.setClipToOutline(true);
            card.setClickable(true);
            card.setOnClickListener(v -> {
                Intent it = new Intent(BotLevelActivity.this, MainActivity.class);
                it.putExtra("level", level);
                startActivity(it);
                finish();
            });
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                    (int) (dm.widthPixels * 0.75f), (int) (photoDp * d));
            cp.gravity = Gravity.CENTER_HORIZONTAL;
            cp.bottomMargin = (int) (10 * d);
            root.addView(card, cp);
        }
        setContentView(root);
    }
}
