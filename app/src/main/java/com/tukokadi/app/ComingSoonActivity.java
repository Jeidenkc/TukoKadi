package com.tukokadi.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ComingSoonActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coming_soon);

        TextView titleView = findViewById(R.id.comingSoonTitle);
        TextView messageView = findViewById(R.id.comingSoonMessage);
        Button backButton = findViewById(R.id.backButton);

        String title = getIntent().getStringExtra("title");
        String message = getIntent().getStringExtra("message");

        if (title != null) {
            titleView.setText(title);
        }
        if (message != null) {
            messageView.setText(message);
        }

        backButton.setOnClickListener(v -> finish());
    }
}
