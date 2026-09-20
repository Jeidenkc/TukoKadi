package com.tukokadi.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class GlowCardView extends CardView {
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator pulse;
    private float glow = 0f;

    public GlowCardView(Context c) {
        super(c);
        glowPaint.setStyle(Paint.Style.STROKE);
    }

    public void setGlow(boolean on) {
        if (pulse != null) {
            pulse.cancel();
            pulse = null;
        }
        if (!on) {
            glow = 0f;
            invalidate();
            return;
        }
        pulse = ValueAnimator.ofFloat(0.55f, 1f);
        pulse.setDuration(650);
        pulse.setRepeatMode(ValueAnimator.REVERSE);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.addUpdateListener(a -> {
            glow = (Float) a.getAnimatedValue();
            invalidate();
        });
        pulse.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (pulse != null) {
            pulse.cancel();
            pulse = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (glow <= 0f) return;
        RectF r = new RectF(4, 4, getWidth() - 4, getHeight() - 4);
        for (int i = 3; i >= 0; i--) {
            glowPaint.setStrokeWidth(5 + i * 5);
            glowPaint.setColor(Color.argb((int) (glow * (170 - i * 40)), 255, 215, 0));
            canvas.drawRoundRect(r, 16, 16, glowPaint);
        }
    }
}
