package com.tukokadi.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

public class SplashActivity extends Activity {
    private ImageView cardImage;
    private boolean flipped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                java.io.File file = new java.io.File("/sdcard/tukokadi_crash.txt");
                java.io.PrintWriter writer = new java.io.PrintWriter(file);
                throwable.printStackTrace(writer);
                writer.close();
            } catch (Exception e) {
                // ignore
            }
            android.os.Process.killProcess(android.os.Process.myPid());
        });

        setContentView(R.layout.activity_splash);

        cardImage = findViewById(R.id.splashCardImage);
        float density = getResources().getDisplayMetrics().density;
        cardImage.setCameraDistance(8000 * density);

        cardImage.setRotation(90f);
        cardImage.setScaleX(0.15f);
        cardImage.setScaleY(0.15f);
        cardImage.setRotationY(0f);

        cardImage.post(this::startAnimation);
    }

    private void startAnimation() {
        ObjectAnimator rotateZ = ObjectAnimator.ofFloat(cardImage, "rotation", 90f, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardImage, "scaleX", 0.15f, 6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardImage, "scaleY", 0.15f, 6f);

        ObjectAnimator flip = ObjectAnimator.ofFloat(cardImage, "rotationY", 0f, 180f);
        flip.addUpdateListener(anim -> {
            float value = (float) anim.getAnimatedValue();
            if (value >= 90f && !flipped) {
                cardImage.setImageResource(R.drawable.joker_front);
                flipped = true;
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playTogether(rotateZ, scaleX, scaleY, flip);
        set.setDuration(1800);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startActivity(new Intent(SplashActivity.this, AuthActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });
        set.start();
    }
}
