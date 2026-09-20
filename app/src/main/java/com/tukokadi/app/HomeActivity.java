package com.tukokadi.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class HomeActivity extends Activity {

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

        findViewById(R.id.dailyGiftButton).setOnClickListener(v ->
                startActivity(new android.content.Intent(this, DailyGiftActivity.class)));

        Button botPlayButton = findViewById(R.id.botPlayButton);
        Button rafikiPlayButton = findViewById(R.id.rafikiPlayButton);
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
    }
    @Override protected void onResume() { super.onResume(); HomeExtras.refreshBalance(this); }
}
