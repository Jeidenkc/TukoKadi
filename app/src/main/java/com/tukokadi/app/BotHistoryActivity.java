package com.tukokadi.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BotHistoryActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int wins = GameHistory.getBotWins(this);
        int losses = GameHistory.getBotLosses(this);
        int total = wins + losses;
        int winRate = total == 0 ? 0 : Math.round((wins * 100f) / total);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 48, 48, 48);
        root.setBackgroundColor(Color.parseColor("#1B5E20"));

        TextView title = new TextView(this);
        title.setText("BOT PLAY HISTORY");
        title.setTextSize(24);
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 48);
        root.addView(title);

        TextView winsText = new TextView(this);
        winsText.setText("Wins: " + wins);
        winsText.setTextSize(20);
        winsText.setTextColor(Color.parseColor("#4CAF50"));
        winsText.setGravity(Gravity.CENTER);
        winsText.setPadding(0, 0, 0, 16);
        root.addView(winsText);

        TextView lossesText = new TextView(this);
        lossesText.setText("Losses: " + losses);
        lossesText.setTextSize(20);
        lossesText.setTextColor(Color.parseColor("#F44336"));
        lossesText.setGravity(Gravity.CENTER);
        lossesText.setPadding(0, 0, 0, 16);
        root.addView(lossesText);

        TextView rateText = new TextView(this);
        rateText.setText("Win Rate: " + winRate + "%");
        rateText.setTextSize(18);
        rateText.setTextColor(Color.WHITE);
        rateText.setGravity(Gravity.CENTER);
        root.addView(rateText);

        setContentView(root);
    }
}
