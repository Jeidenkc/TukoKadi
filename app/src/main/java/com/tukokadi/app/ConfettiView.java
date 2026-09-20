package com.tukokadi.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConfettiView extends View {

    private final Random random = new Random();
    private final List<ConfettiPiece> confetti = new ArrayList<>();
    private final List<Spark> sparks = new ArrayList<>();
    private ValueAnimator animator;
    private long lastFrameTime;
    private long lastFireworkTime;
    private final Paint paint = new Paint();

    private final int[] palette = {
            Color.parseColor("#FFD700"),
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#FF4444"),
            Color.parseColor("#44DD66"),
            Color.parseColor("#4499FF"),
            Color.parseColor("#FF88CC")
    };

    public ConfettiView(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(true);
    }

    public void start() {
        confetti.clear();
        sparks.clear();
        lastFrameTime = System.currentTimeMillis();
        lastFireworkTime = lastFrameTime;
        post(this::spawnConfettiBatch);
        if (animator != null) animator.cancel();
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(Long.MAX_VALUE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(a -> tick());
        animator.start();
    }

    public void stop() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    private void spawnConfettiBatch() {
        int w = getWidth();
        if (w == 0) { post(this::spawnConfettiBatch); return; }
        for (int i = 0; i < 140; i++) {
            confetti.add(newConfettiPiece(w));
        }
    }

    private ConfettiPiece newConfettiPiece(int w) {
        ConfettiPiece p = new ConfettiPiece();
        p.x = random.nextFloat() * w;
        p.y = -random.nextFloat() * getHeight();
        p.vx = (random.nextFloat() - 0.5f) * 2.5f;
        p.vy = 3f + random.nextFloat() * 4f;
        p.size = 14f + random.nextFloat() * 12f;
        p.color = palette[random.nextInt(palette.length)];
        p.rotation = random.nextFloat() * 360f;
        p.rotationSpeed = (random.nextFloat() - 0.5f) * 12f;
        return p;
    }

    private void tick() {
        long now = System.currentTimeMillis();
        float dt = Math.min(now - lastFrameTime, 40) / 16f;
        lastFrameTime = now;

        int w = getWidth();
        int h = getHeight();

        for (ConfettiPiece p : confetti) {
            p.y += p.vy * dt;
            p.x += p.vx * dt + Math.sin(p.y / 40f) * 1.2f;
            p.rotation += p.rotationSpeed * dt;
            if (p.y > h + 30) {
                p.y = -30;
                p.x = random.nextFloat() * w;
            }
        }

        if (now - lastFireworkTime > 700 + random.nextInt(600)) {
            lastFireworkTime = now;
            spawnFirework(w, h);
        }

        List<Spark> dead = new ArrayList<>();
        for (Spark s : sparks) {
            s.vy += 0.12f * dt;
            s.x += s.vx * dt;
            s.y += s.vy * dt;
            s.life -= dt;
            if (s.life <= 0) dead.add(s);
        }
        sparks.removeAll(dead);

        invalidate();
    }

    private void spawnFirework(int w, int h) {
        float cx = w * (0.2f + random.nextFloat() * 0.6f);
        float cy = h * (0.15f + random.nextFloat() * 0.35f);
        int color = palette[random.nextInt(palette.length)];
        int count = 34;
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2 * i) / count;
            float speed = 4f + random.nextFloat() * 3f;
            Spark s = new Spark();
            s.x = cx;
            s.y = cy;
            s.vx = (float) Math.cos(angle) * speed;
            s.vy = (float) Math.sin(angle) * speed;
            s.color = color;
            s.life = 28 + random.nextInt(10);
            s.maxLife = s.life;
            sparks.add(s);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (ConfettiPiece p : confetti) {
            paint.setColor(p.color);
            canvas.save();
            canvas.translate(p.x, p.y);
            canvas.rotate(p.rotation);
            canvas.drawRect(-p.size / 2f, -p.size / 4f, p.size / 2f, p.size / 4f, paint);
            canvas.restore();
        }
        for (Spark s : sparks) {
            int alpha = (int) (255 * Math.max(0f, s.life / (float) s.maxLife));
            paint.setColor(s.color);
            paint.setAlpha(alpha);
            canvas.drawCircle(s.x, s.y, 5f, paint);
        }
        paint.setAlpha(255);
    }

    private static class ConfettiPiece {
        float x, y, vx, vy, size, rotation, rotationSpeed;
        int color;
    }

    private static class Spark {
        float x, y, vx, vy, life, maxLife;
        int color;
    }
}
