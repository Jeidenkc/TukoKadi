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
        cardImage.setImageBitmap(buildGameBack());

        cardImage.post(this::startAnimation);
    }

    private android.graphics.Bitmap buildGameBack() {
        int w = 450, h = 630;
        int red = 0xFFB71C1C, light = 0xFFE57373;
        try {
            java.lang.reflect.Field f1 = CardView.class.getDeclaredField("CARD_RED");
            f1.setAccessible(true);
            red = f1.getInt(null);
            java.lang.reflect.Field f2 = CardView.class.getDeclaredField("CARD_RED_LIGHT");
            f2.setAccessible(true);
            light = f2.getInt(null);
        } catch (Exception e) { }
        android.graphics.Bitmap bmp = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas c = new android.graphics.Canvas(bmp);
        android.graphics.RectF rect = new android.graphics.RectF(6, 6, w - 6, h - 6);
        android.graphics.Paint fill = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        fill.setColor(red);
        c.drawRoundRect(rect, 28, 28, fill);
        c.save();
        android.graphics.Path clip = new android.graphics.Path();
        clip.addRoundRect(rect, 28, 28, android.graphics.Path.Direction.CW);
        c.clipPath(clip);
        android.graphics.Paint line = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        line.setColor(light);
        line.setStrokeWidth(4);
        for (int d = -h; d < w + h; d += 34) {
            c.drawLine(d, 0, d + h, h, line);
            c.drawLine(d + h, 0, d, h, line);
        }
        c.restore();
        android.graphics.Paint border = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        border.setColor(android.graphics.Color.WHITE);
        border.setStyle(android.graphics.Paint.Style.STROKE);
        border.setStrokeWidth(12);
        c.drawRoundRect(rect, 28, 28, border);
        border.setStrokeWidth(4);
        c.drawRoundRect(new android.graphics.RectF(32, 32, w - 32, h - 32), 18, 18, border);
        return bmp;
    }

    private void startAnimation() {
        android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
        float fitScale = Math.min(dm.widthPixels / (140 * dm.density), dm.heightPixels / (200 * dm.density)) * 0.85f;

        int kRes = getResources().getIdentifier("k_spades", "drawable", getPackageName());
        if (kRes == 0) kRes = getResources().getIdentifier("splash_card_face", "drawable", getPackageName());
        if (kRes == 0) kRes = R.drawable.joker_front;
        final int faceRes = kRes;

        cardImage.setPivotX(cardImage.getWidth() / 2f);
        cardImage.setPivotY(cardImage.getHeight() / 2f);

        // Fast initial spin: several full rotations at constant speed.
        ObjectAnimator fastSpin = ObjectAnimator.ofFloat(cardImage, "rotation", 0f, 1080f);
        fastSpin.setDuration(600);
        fastSpin.setInterpolator(new android.view.animation.LinearInterpolator());

        // Spin decelerates into a clean stop.
        ObjectAnimator slowSpin = ObjectAnimator.ofFloat(cardImage, "rotation", 1080f, 1440f);
        slowSpin.setDuration(750);
        slowSpin.setInterpolator(new android.view.animation.DecelerateInterpolator(2.4f));

        AnimatorSet spinSequence = new AnimatorSet();
        spinSequence.playSequentially(fastSpin, slowSpin);

        // Card grows to fill the screen over the same total time as the spin.
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardImage, "scaleX", 0.15f, fitScale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardImage, "scaleY", 0.15f, fitScale);
        scaleX.setDuration(1350);
        scaleY.setDuration(1350);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet spinAndGrow = new AnimatorSet();
        spinAndGrow.playTogether(spinSequence, scaleX, scaleY);

        ObjectAnimator flipOut = ObjectAnimator.ofFloat(cardImage, "rotationY", 0f, 90f);
        flipOut.setDuration(350);
        flipOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                cardImage.setImageResource(faceRes);
                cardImage.setRotationY(-90f);
            }
        });
        ObjectAnimator flipIn = ObjectAnimator.ofFloat(cardImage, "rotationY", -90f, 0f);
        flipIn.setDuration(350);

        AnimatorSet all = new AnimatorSet();
        all.playSequentially(spinAndGrow, flipOut, flipIn);
        all.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                cardImage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashActivity.this, AuthActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                    }
                }, 1000);
            }
        });
        all.start();
    }
}
