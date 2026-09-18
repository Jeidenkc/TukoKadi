package com.tukokadi.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

public class CardView extends View {

    private static final int GREEN_DARK = Color.parseColor("#185E20");
    private static final int CARD_RED = Color.parseColor("#8B0000");
    private static final int CARD_RED_LIGHT = Color.parseColor("#B71C1C");

    private String rankText = "";
    private String suitSymbol = "";
    private boolean red = false;
    private boolean faceDown = false;
    private int pipCount = 0;
    private Bitmap faceBitmap = null;

    private Paint bgPaint;
    private Paint borderPaint;
    private Paint cornerPaint;
    private Paint centerPaint;
    private Paint pipPaint;
    private Paint backFillPaint;
    private Paint backLinePaint;
    private Paint backBorderPaint;
    private Paint backInnerPaint;

    public CardView(Context context) {
        super(context);
        init();
    }

    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.WHITE);
        bgPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(GREEN_DARK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(5);

        cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cornerPaint.setTypeface(Typeface.DEFAULT_BOLD);
        cornerPaint.setTextAlign(Paint.Align.LEFT);

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setTypeface(Typeface.DEFAULT_BOLD);
        centerPaint.setTextAlign(Paint.Align.CENTER);

        pipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pipPaint.setTypeface(Typeface.DEFAULT_BOLD);
        pipPaint.setTextAlign(Paint.Align.CENTER);

        backFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backFillPaint.setColor(CARD_RED);
        backFillPaint.setStyle(Paint.Style.FILL);

        backLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backLinePaint.setColor(CARD_RED_LIGHT);
        backLinePaint.setStrokeWidth(2);

        backBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backBorderPaint.setColor(Color.WHITE);
        backBorderPaint.setStyle(Paint.Style.STROKE);
        backBorderPaint.setStrokeWidth(6);

        backInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backInnerPaint.setColor(Color.WHITE);
        backInnerPaint.setStyle(Paint.Style.STROKE);
        backInnerPaint.setStrokeWidth(2);
    }

    public void setCard(String rankText, String suitSymbol, boolean red) {
        int derivedPips;
        try {
            derivedPips = Integer.parseInt(rankText);
        } catch (NumberFormatException e) {
            derivedPips = 0;
        }
        setCard(rankText, suitSymbol, red, derivedPips);
    }

    public void setCard(String rankText, String suitSymbol, boolean red, int pipCount) {
        this.rankText = rankText;
        this.suitSymbol = suitSymbol;
        this.red = red;
        this.pipCount = pipCount;
        this.faceDown = false;
        this.faceBitmap = null;
        invalidate();
    }

    public void setFaceBitmap(Bitmap bitmap) {
        this.faceBitmap = bitmap;
        invalidate();
    }

    public void setFaceDown() {
        setFaceDown(true);
    }

    public void setFaceDown(boolean faceDown) {
        this.faceDown = faceDown;
        invalidate();
    }

    private static float[][] pipPositions(int n) {
        switch (n) {
<<<<<<< HEAD
            case 2: return new float[][]{{0.5f,0.20f},{0.5f,0.80f}};
            case 3: return new float[][]{{0.5f,0.18f},{0.5f,0.5f},{0.5f,0.82f}};
            case 4: return new float[][]{{0.26f,0.20f},{0.74f,0.20f},{0.26f,0.80f},{0.74f,0.80f}};
            case 5: return new float[][]{{0.26f,0.20f},{0.74f,0.20f},{0.5f,0.5f},{0.26f,0.80f},{0.74f,0.80f}};
            case 6: return new float[][]{{0.26f,0.17f},{0.74f,0.17f},{0.26f,0.5f},{0.74f,0.5f},{0.26f,0.83f},{0.74f,0.83f}};
            case 7: return new float[][]{{0.26f,0.13f},{0.74f,0.13f},{0.5f,0.30f},{0.26f,0.5f},{0.74f,0.5f},{0.26f,0.87f},{0.74f,0.87f}};
            case 8: return new float[][]{{0.26f,0.10f},{0.74f,0.10f},{0.5f,0.27f},{0.26f,0.44f},{0.74f,0.44f},{0.5f,0.61f},{0.26f,0.90f},{0.74f,0.90f}};
            case 9: return new float[][]{{0.26f,0.09f},{0.74f,0.09f},{0.26f,0.30f},{0.74f,0.30f},{0.5f,0.5f},{0.26f,0.70f},{0.74f,0.70f},{0.26f,0.91f},{0.74f,0.91f}};
            case 10: return new float[][]{{0.26f,0.07f},{0.74f,0.07f},{0.5f,0.21f},{0.26f,0.35f},{0.74f,0.35f},{0.26f,0.65f},{0.74f,0.65f},{0.5f,0.79f},{0.26f,0.93f},{0.74f,0.93f}};
=======
            case 2: return new float[][]{{0.5f,0.22f},{0.5f,0.78f}};
            case 3: return new float[][]{{0.5f,0.22f},{0.5f,0.5f},{0.5f,0.78f}};
            case 4: return new float[][]{{0.28f,0.22f},{0.72f,0.22f},{0.28f,0.78f},{0.72f,0.78f}};
            case 5: return new float[][]{{0.28f,0.22f},{0.72f,0.22f},{0.5f,0.5f},{0.28f,0.78f},{0.72f,0.78f}};
            case 6: return new float[][]{{0.28f,0.2f},{0.72f,0.2f},{0.28f,0.5f},{0.72f,0.5f},{0.28f,0.8f},{0.72f,0.8f}};
            case 7: return new float[][]{{0.28f,0.16f},{0.72f,0.16f},{0.5f,0.32f},{0.28f,0.5f},{0.72f,0.5f},{0.28f,0.82f},{0.72f,0.82f}};
            case 8: return new float[][]{{0.28f,0.14f},{0.72f,0.14f},{0.5f,0.3f},{0.28f,0.46f},{0.72f,0.46f},{0.5f,0.62f},{0.28f,0.84f},{0.72f,0.84f}};
            case 9: return new float[][]{{0.28f,0.12f},{0.72f,0.12f},{0.28f,0.32f},{0.72f,0.32f},{0.5f,0.5f},{0.28f,0.68f},{0.72f,0.68f},{0.28f,0.88f},{0.72f,0.88f}};
            case 10: return new float[][]{{0.28f,0.1f},{0.72f,0.1f},{0.5f,0.24f},{0.28f,0.38f},{0.72f,0.38f},{0.28f,0.62f},{0.72f,0.62f},{0.5f,0.76f},{0.28f,0.9f},{0.72f,0.9f}};
>>>>>>> eb91623d56fc30f1e43fc9e0c94b1e587a9d419e
            default: return new float[][]{};
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        RectF rect = new RectF(4, 4, w - 4, h - 4);

        if (faceDown) {
            Path clipPath = new Path();
            clipPath.addRoundRect(rect, 16, 16, Path.Direction.CW);
            canvas.save();
            canvas.clipPath(clipPath);
            canvas.drawRect(rect, backFillPaint);

            float spacing = 18;
            for (float d = 0; d < w + h; d += spacing) {
                canvas.drawLine(d, 0, d - h, h, backLinePaint);
                canvas.drawLine(d, h, d - h, 0, backLinePaint);
            }
            canvas.restore();

            RectF innerRect = new RectF(rect.left + 10, rect.top + 10, rect.right - 10, rect.bottom - 10);
            canvas.drawRoundRect(innerRect, 10, 10, backInnerPaint);
            canvas.drawRoundRect(rect, 16, 16, backBorderPaint);
            return;
        }

        canvas.drawRoundRect(rect, 16, 16, bgPaint);
        canvas.drawRoundRect(rect, 16, 16, borderPaint);

        int color = red ? Color.RED : Color.BLACK;
        cornerPaint.setColor(color);
        centerPaint.setColor(color);
        pipPaint.setColor(color);

<<<<<<< HEAD
        // --- Enlarged sizing: pips/symbols now fill roughly two-thirds of the card face ---
        float cornerSize = h * 0.085f;
        float centerSize = h * 0.66f;
        float pipSize = h * 0.26f;
=======
        // --- Enlarged sizing: scaled to this card's own height so pips/symbols
        // fill roughly two-thirds of the card instead of a small fixed size. ---
        float cornerSize = h * 0.085f;
        float centerSize = h * 0.62f;
        float pipSize = h * 0.20f;
>>>>>>> eb91623d56fc30f1e43fc9e0c94b1e587a9d419e
        cornerPaint.setTextSize(cornerSize);
        centerPaint.setTextSize(centerSize);
        pipPaint.setTextSize(pipSize);

        if (faceBitmap != null) {
            RectF imgRect = new RectF(rect.left + 8, rect.top + 8, rect.right - 8, rect.bottom - 8);
            canvas.drawBitmap(faceBitmap, null, imgRect, null);
            return;
        }

        String corner = rankText + suitSymbol;
        canvas.drawText(corner, 14, cornerSize + 4, cornerPaint);

        canvas.save();
        canvas.rotate(180, w - 14 - cornerPaint.measureText(corner) / 2f, h - cornerSize - 4 + 6);
        canvas.drawText(corner, w - 14 - cornerPaint.measureText(corner), h - cornerSize + 10, cornerPaint);
        canvas.restore();

        if (pipCount >= 2) {
            float[][] positions = pipPositions(pipCount);
            for (float[] pos : positions) {
                float px = pos[0] * w;
                float py = pos[1] * h + pipSize * 0.3f;
                canvas.drawText(suitSymbol, px, py, pipPaint);
            }
        } else {
            canvas.drawText(suitSymbol, w / 2f, h / 2f + centerSize * 0.3f, centerPaint);
        }
    }
}
