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


    public static final String APK_LINK = "https://github.com/Jeidenkc/TukoKadi/releases/download/v1.3/TukoKadi-v1.3.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        java.io.File crashFile = new java.io.File(getFilesDir(), "crash.txt");
        if (crashFile.exists()) {
            try {
                byte[] b = new byte[(int) crashFile.length()];
                new java.io.FileInputStream(crashFile).read(b);
                new android.app.AlertDialog.Builder(this)
                    .setTitle("Crash log").setMessage(new String(b))
                    .setPositiveButton("OK", null).show();
            } catch (Exception ignored) {}
            crashFile.delete();
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
        float fitD = getResources().getDisplayMetrics().density;
        int fitSw = getResources().getDisplayMetrics().widthPixels;
        int fitSize = (int) ((fitSw - 56 * fitD) / 2 - 8 * fitD);
        android.view.View[] fitArr = { botPlayButton, rafikiPlayButton };
        for (android.view.View fv : fitArr) {
            android.view.ViewGroup.LayoutParams flp = fv.getLayoutParams();
            flp.width = fitSize;
            flp.height = fitSize;
            fv.setLayoutParams(flp);
        }
        android.view.View homeLogoView = findViewById(R.id.homeLogo);
        if (homeLogoView != null) roundView(homeLogoView, 10);
        Button faqsButton = findViewById(R.id.faqsButton);

        botPlayButton.setOnClickListener(v ->
            startActivity(new Intent(HomeActivity.this, MainActivity.class))
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
