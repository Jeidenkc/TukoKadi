package com.tukokadi.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameHistoryActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 48, 48, 48);
        root.setBackgroundColor(Color.parseColor("#1B5E20"));

        TextView title = new TextView(this);
        title.setText("GAME HISTORY");
        title.setTextSize(26);
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 48);
        root.addView(title);

        Button botHistoryButton = new Button(this);
        botHistoryButton.setText("Bot Play History");
        botHistoryButton.setTextColor(Color.WHITE);
        botHistoryButton.setTextSize(16);
        botHistoryButton.setPadding(24, 32, 24, 32);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.setMargins(0, 0, 0, 24);
        botHistoryButton.setLayoutParams(lp1);
        botHistoryButton.setOnClickListener(v ->
            startActivity(new Intent(GameHistoryActivity.this, BotHistoryActivity.class))
        );
        root.addView(botHistoryButton);

        Button rafikiHistoryButton = new Button(this);
        rafikiHistoryButton.setText("Rafiki Play History");
        rafikiHistoryButton.setTextColor(Color.WHITE);
        rafikiHistoryButton.setTextSize(16);
        rafikiHistoryButton.setPadding(24, 32, 24, 32);
        rafikiHistoryButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        rafikiHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameHistoryActivity.this, ComingSoonActivity.class);
            intent.putExtra("title", "Rafiki Play History");
            intent.putExtra("message", "Online match history is coming soon!");
            startActivity(intent);
        });
        root.addView(rafikiHistoryButton);

        setContentView(root);
    }
}
