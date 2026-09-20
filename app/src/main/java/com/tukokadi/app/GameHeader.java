package com.tukokadi.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

public class GameHeader {
    public static final long MAX_BOT_PLAYTIME_MS = 5 * 60 * 1000; // 5 minutes

    private final Activity activity;
    private final boolean botGame;
    private final TextView botTimer, turnInfo, btnSettings;
    private CountDownTimer timer;
    private long timeLeftMs = MAX_BOT_PLAYTIME_MS;

    public GameHeader(Activity activity, boolean botGame) {
        this.activity = activity;
        this.botGame = botGame;
        botTimer = activity.findViewById(R.id.botTimer);
        turnInfo = activity.findViewById(R.id.turnInfo);
        btnSettings = activity.findViewById(R.id.btnSettings);
        if (botGame) botTimer.setVisibility(View.VISIBLE);
    }

    public void setTurn(String playerName, boolean clockwise) {
        turnInfo.setText("Turn: " + playerName + "  " + (clockwise ? "➜" : "⬅"));
        turnInfo.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200)
                .withEndAction(() -> turnInfo.animate().scaleX(1f).scaleY(1f).setDuration(200));
    }

    public void setOnSettingsClick(View.OnClickListener l) {
        btnSettings.setOnClickListener(l);
    }

    public void onResume() {
        if (!botGame || timeLeftMs <= 0) return;
        timer = new CountDownTimer(timeLeftMs, 1000) {
            @Override public void onTick(long ms) {
                timeLeftMs = ms;
                long s = ms / 1000;
                botTimer.setText(String.format("⏱ %02d:%02d", s / 60, s % 60));
                if (ms <= 60_000) botTimer.setTextColor(0xFFFF5252);
            }
            @Override public void onFinish() {
                timeLeftMs = 0;
                botTimer.setText("⏱ 00:00");
                new AlertDialog.Builder(activity)
                        .setTitle("Time's up!")
                        .setMessage("Maximum bot play time reached.")
                        .setCancelable(false)
                        .setPositiveButton("OK", (d, w) -> activity.finish())
                        .show();
            }
        }.start();
    }

    public void onPause() {
        if (timer != null) timer.cancel();
    }
}
