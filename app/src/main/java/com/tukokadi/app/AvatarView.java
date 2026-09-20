package com.tukokadi.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class AvatarView extends View {
    private Bitmap photo;
    private String name = "?";
    private boolean active = false;
    private ValueAnimator countdown;
    private float fraction = 0f;
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arc = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);

    public AvatarView(Context c) {
        super(c);
        ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeWidth(4);
        text.setColor(Color.WHITE);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        arc.setStyle(Paint.Style.STROKE);
        arc.setStrokeWidth(8);
        arc.setStrokeCap(Paint.Cap.ROUND);
        track.setStyle(Paint.Style.STROKE);
        track.setStrokeWidth(8);
        track.setColor(Color.argb(60, 255, 255, 255));
    }

    public void setPlayer(String name, Bitmap photo) {
        this.name = name;
        this.photo = photo;
        invalidate();
    }

    public void setActive(boolean a) {
        active = a;
        invalidate();
    }

    public void startCountdown(long ms) {
        stopCountdown();
        countdown = ValueAnimator.ofFloat(1f, 0f);
        countdown.setDuration(ms);
        countdown.setInterpolator(new LinearInterpolator());
        countdown.addUpdateListener(a -> {
            fraction = (Float) a.getAnimatedValue();
            invalidate();
        });
        countdown.start();
    }

    public void stopCountdown() {
        if (countdown != null) {
            countdown.cancel();
            countdown = null;
        }
        fraction = 0f;
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (countdown != null) countdown.cancel();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int s = Math.min(getWidth(), getHeight());
        float r = s / 2f - 12;
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        Path clip = new Path();
        clip.addCircle(cx, cy, r, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clip);
        if (photo != null) {
            int bw = photo.getWidth(), bh = photo.getHeight();
            int side = Math.min(bw, bh);
            Rect src = new Rect((bw - side) / 2, (bh - side) / 2, (bw + side) / 2, (bh + side) / 2);
            canvas.drawBitmap(photo, src, new RectF(cx - r, cy - r, cx + r, cy + r), null);
        } else {
            fill.setColor(Color.HSVToColor(new float[]{Math.abs(name.hashCode()) % 360, 0.5f, 0.6f}));
            canvas.drawCircle(cx, cy, r, fill);
            text.setTextSize(r);
            String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
            canvas.drawText(initial, cx, cy + r * 0.35f, text);
        }
        canvas.restore();
        ring.setColor(active ? Color.parseColor("#FFD700") : Color.WHITE);
        canvas.drawCircle(cx, cy, r, ring);

        if (countdown != null) {
            float rr = s / 2f - 5;
            RectF box = new RectF(cx - rr, cy - rr, cx + rr, cy + rr);
            canvas.drawCircle(cx, cy, rr, track);
            arc.setColor(fraction > 0.5f ? Color.parseColor("#FF3B30")
                    : fraction > 0.25f ? Color.parseColor("#FF3B30")
                    : Color.parseColor("#FF4444"));
            canvas.drawArc(box, -90, 360 * fraction, false, arc);
        }
    }
}
