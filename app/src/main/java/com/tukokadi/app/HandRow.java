package com.tukokadi.app;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

public class HandRow extends LinearLayout {
    public HandRow(Context c) {
        super(c);
        setOrientation(HORIZONTAL);
    }

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        super.onMeasure(wSpec, hSpec);
        int n = getChildCount();
        int avail = MeasureSpec.getSize(wSpec);
        if (n < 2 || avail <= 0) return;
        int total = getPaddingLeft() + getPaddingRight();
        for (int i = 0; i < n; i++) {
            View ch = getChildAt(i);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ch.getLayoutParams();
            total += ch.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
        }
        int deficit = total - avail;
        if (deficit > 0) {
            int extra = deficit / (n - 1) + 1;
            for (int i = 1; i < n; i++) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) getChildAt(i).getLayoutParams();
                lp.leftMargin -= extra;
            }
            super.onMeasure(wSpec, hSpec);
        }
    }
}
