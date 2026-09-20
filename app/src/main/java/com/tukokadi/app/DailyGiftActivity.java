package com.tukokadi.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DailyGiftActivity extends Activity {

    private Button claimButton;
    private TextView subtext;
    private TextView resultText;
    private Handler tickHandler = new Handler(Looper.getMainLooper());
    private Runnable tickRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_gift);

        claimButton = findViewById(R.id.claimGiftButton);
        subtext = findViewById(R.id.giftSubtext);
        resultText = findViewById(R.id.giftResultText);

        claimButton.setOnClickListener(v -> {
            long reward = Wallet.claimDailyGift(this);
            if (reward > 0) {
                resultText.setText("You received " + String.format("%,d", reward) + " Coins!");
                Toast.makeText(this, "Gift claimed!", Toast.LENGTH_SHORT).show();
            }
            updateState();
        });

        updateState();
    }

    private void updateState() {
        if (Wallet.canClaimDailyGift(this)) {
            claimButton.setEnabled(true);
            claimButton.setText("Claim Gift");
            subtext.setText("Everyday you are eligible to claim a gift of between 1,000 and 100,000 Coins.");
            stopCountdown();
        } else {
            claimButton.setEnabled(false);
            startCountdown();
        }
    }

    private void startCountdown() {
        stopCountdown();
        tickRunnable = new Runnable() {
            @Override
            public void run() {
                long ms = Wallet.millisUntilNextClaim(DailyGiftActivity.this);
                if (ms <= 0) {
                    updateState();
                    return;
                }
                long hours = ms / (1000 * 60 * 60);
                long minutes = (ms / (1000 * 60)) % 60;
                long seconds = (ms / 1000) % 60;
                claimButton.setText(String.format("Next gift in %02d:%02d:%02d", hours, minutes, seconds));
                tickHandler.postDelayed(this, 1000);
            }
        };
        tickHandler.post(tickRunnable);
    }

    private void stopCountdown() {
        if (tickRunnable != null) tickHandler.removeCallbacks(tickRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCountdown();
    }
}
