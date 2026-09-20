package com.tukokadi.app;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

public class FitLayout extends FrameLayout {
    private float scale = 1f;

    public FitLayout(Context c) {
        super(c);
    }

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        int w = MeasureSpec.getSize(wSpec);
        int h = MeasureSpec.getSize(hSpec);
        setMeasuredDimension(w, h);
        View child = getChildAt(0);
        if (child == null || w == 0 || h == 0) return;
        int unspec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        child.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), unspec);
        int natural = child.getMeasuredHeight();
        if (natural <= h) {
            scale = 1f;
            child.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
            return;
        }
        float s1 = (float) h / natural;
        int contentW = Math.round(w / s1);
        child.measure(MeasureSpec.makeMeasureSpec(contentW, MeasureSpec.EXACTLY), unspec);
        int natural2 = child.getMeasuredHeight();
        scale = Math.min(s1, (float) h / natural2);
        child.measure(MeasureSpec.makeMeasureSpec(contentW, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(natural2, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View child = getChildAt(0);
        if (child == null) return;
        child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
        child.setPivotX(0f);
        child.setPivotY(0f);
        child.setScaleX(scale);
        child.setScaleY(scale);
        child.setTranslationX((getWidth() - child.getMeasuredWidth() * scale) / 2f);
    }
}
