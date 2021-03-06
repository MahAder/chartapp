package com.tarashor.chartlib;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

class YAxis {
    private static final int GRID_HORIZONTAL_LINE_COUNT = 6;
    private float width;
    private final Paint mGridPaint;
    private final Paint mTextPaint;

    private List<YAxisTick> ticks;
    private int textHeight;

    public YAxis(float ymin, float ymax,
                 float width, float height, float topOffset,
                 Paint gridPaint, Paint textPaint) {
        this.width = width;

        mGridPaint = gridPaint;
        mTextPaint = textPaint;

        Rect bounds = new Rect();
        mTextPaint.getTextBounds("9", 0, 1, bounds);
        textHeight = bounds.top - bounds.bottom;

        float delta = (height - topOffset) / (GRID_HORIZONTAL_LINE_COUNT - 1);

        ticks = new ArrayList<>();
        for (int i = 0; i < GRID_HORIZONTAL_LINE_COUNT; i++) {
            float y = height - delta * i;
            int v = Math.round(ymax - y * (ymax - ymin) / (height));
            YAxisTick tick = new YAxisTick(String.valueOf(v), y);
            ticks.add(tick);
        }
    }

    public void draw(Canvas canvas){
        for (YAxisTick tick : ticks) {
            float y = tick.getPixelOffset();
            canvas.drawLine(0, y, width, y, mGridPaint);
            canvas.drawText(tick.getText(), 0, y + textHeight + 10, mTextPaint);
        }
    }
}
