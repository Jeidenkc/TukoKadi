package com.tukokadi.app;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;

public class WinScreenManager {

    public interface OnNewGame {
        void onNewGame();
    }

    public static void show(Activity activity, String winnerName, OnNewGame callback) {
        ViewGroup root = activity.findViewById(android.R.id.content);
        View overlay = LayoutInflater.from(activity).inflate(R.layout.overlay_win_screen, root, false);
        root.addView(overlay);

        ConfettiView confettiView = overlay.findViewById(R.id.confettiView);
        TextView congratsText = overlay.findViewById(R.id.congratsText);
        TextView winnerNameText = overlay.findViewById(R.id.winnerNameText);
        Button newGameButton = overlay.findViewById(R.id.newGameButton);

        winnerNameText.setText(winnerName + " WINS!");

        confettiView.post(confettiView::start);

        congratsText.setScaleX(0f);
        congratsText.setScaleY(0f);
        AnimatorSet popIn = new AnimatorSet();
        ObjectAnimator sx = ObjectAnimator.ofFloat(congratsText, "scaleX", 0f, 1.15f, 1f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(congratsText, "scaleY", 0f, 1.15f, 1f);
        popIn.playTogether(sx, sy);
        popIn.setDuration(600);
        popIn.setInterpolator(new OvershootInterpolator());
        popIn.start();

        newGameButton.postDelayed(() -> {
            newGameButton.setVisibility(View.VISIBLE);
            newGameButton.animate().alpha(1f).setDuration(400).start();
        }, 1200);

        newGameButton.setOnClickListener(v -> {
            confettiView.stop();
            root.removeView(overlay);
            if (callback != null) callback.onNewGame();
        });
    }
}
