package com.tukokadi.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class HomeActivity extends Activity {

    private void roundView(android.view.View v, float dp) {
        final float r = dp * getResources().getDisplayMetrics().density;
        v.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline o) {
                o.setRoundRect(0, 0, view.getWidth(), view.getHeight(), r);
            }
        });
        v.setClipToOutline(true);
    }


    private android.view.View optView(String n) {
        int id = getResources().getIdentifier(n, "id", getPackageName());
        android.view.View x = id == 0 ? null : findViewById(id);
        return x != null ? x : new android.view.View(this);
    }


    public static final String APK_LINK = "https://github.com/Jeidenkc/TukoKadi/releases/download/v1.4.1/TukoKadi-v1.4.1.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final java.io.File crashFile = new java.io.File(getFilesDir(), CrashApp.CRASH_FILE);
        if (crashFile.exists()) {
            String trace;
            try {
                byte[] b = new byte[(int) crashFile.length()];
                java.io.FileInputStream fis = new java.io.FileInputStream(crashFile);
                fis.read(b);
                fis.close();
                trace = new String(b);
            } catch (Exception e) {
                trace = "(could not read crash log)";
            }
            android.widget.TextView traceView = new android.widget.TextView(this);
            traceView.setText(trace);
            traceView.setTextSize(12);
            int pad = (int) (16 * getResources().getDisplayMetrics().density);
            traceView.setPadding(pad, pad, pad, pad);
            android.widget.ScrollView scroll = new android.widget.ScrollView(this);
            scroll.addView(traceView);
            new android.app.AlertDialog.Builder(this)
                .setTitle("Crash log")
                .setView(scroll)
                .setPositiveButton("Close", (d, w) -> crashFile.delete())
                .show();
        }
        setContentView(R.layout.activity_home);
        HomeExtras.setup(this);
        optView("shareButton").setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, "Play TukoKadi, the Kadi card game! Download it here: " + APK_LINK);
            startActivity(Intent.createChooser(share, "Share TukoKadi"));
        });

        optView("dailyGiftButton").setOnClickListener(v ->
                startActivity(new android.content.Intent(this, DailyGiftActivity.class)));

        android.view.View botPlayButton = findViewById(R.id.botPlayButton);
        android.view.View rafikiPlayButton = findViewById(R.id.rafikiPlayButton);
        roundView(botPlayButton, 10);
        roundView(rafikiPlayButton, 10);
        final android.widget.ImageView rafikiImg = (android.widget.ImageView) rafikiPlayButton;
        rafikiImg.post(new Runnable() {
            @Override
            public void run() {
                android.graphics.drawable.Drawable dr = rafikiImg.getDrawable();
                if (dr == null || rafikiImg.getWidth() == 0) return;
                float sc = (float) rafikiImg.getWidth() / dr.getIntrinsicWidth();
                float dy = rafikiImg.getHeight() - dr.getIntrinsicHeight() * sc;
                rafikiImg.setScaleType(android.widget.ImageView.ScaleType.MATRIX);
                android.graphics.Matrix mx = new android.graphics.Matrix();
                mx.setScale(sc, sc);
                mx.postTranslate(0, Math.min(0f, dy));
                rafikiImg.setImageMatrix(mx);
            }
        });
        
        android.view.View homeLogoView = findViewById(R.id.homeLogo);
        if (homeLogoView != null) roundView(homeLogoView, 10);
        Button faqsButton = findViewById(R.id.faqsButton);

        botPlayButton.setOnClickListener(v ->
            startActivity(new Intent(HomeActivity.this, BotLevelActivity.class))
        );

        rafikiPlayButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, RafikiActivity.class)));

        faqsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ComingSoonActivity.class);
            intent.putExtra("title", "FAQs");
            intent.putExtra("message", "Frequently asked questions coming soon!");
            startActivity(intent);
        });

        Button gameHistoryButton = findViewById(R.id.gameHistoryButton);
        gameHistoryButton.setOnClickListener(v ->
            startActivity(new Intent(HomeActivity.this, GameHistoryActivity.class))
        );
    }
    @Override protected void onResume() { super.onResume(); HomeExtras.refreshBalance(this); }
}
