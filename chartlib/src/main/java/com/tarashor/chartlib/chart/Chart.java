package com.tarashor.chartlib.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.tarashor.chartlib.BaseChartView;
import com.tarashor.chartlib.ChartViewPort;
import com.tarashor.chartlib.Utils;

import java.util.Date;


public class Chart extends BaseChartView {
    protected final static int AXIS_TEXT_SIZE_DP = 16;
    protected final static int AXIS_TEXT_AREA_HEIGHT_DP = AXIS_TEXT_SIZE_DP + 4;

    private int mGridColor;
    private int mMarksTextColor;
    private int mPointerLineColor;
    private int mPopupTextHeaderColor;
    private int mPopupBackground;
    private int mPopupBorderColor;

    protected Paint mYTextPaint;
    protected Paint mXTextPaint;

    protected Paint mGridPaint;

    private Paint mPointerLinePaint;

    private YAxis yAxis;
    private XAxis xAxis;

    private float mTopLineOffsetPixels;
    private GestureDetector mDetector;
    private float mPointerCircleRadius;
    private Paint mPointerBorderPaint;


    public Chart(Context context) {
        super(context);

    }

    public Chart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Chart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    protected void init() {
        super.init();

        viewPortBuilder.setBottomOffsetPixels(Utils.convertDpToPixel(getContext(), AXIS_TEXT_AREA_HEIGHT_DP));
        viewPortBuilder.setTopOffsetPixels(Utils.convertDpToPixel(getContext(), 6));

        mPointerCircleRadius = Utils.convertDpToPixel(getContext(), 4);

        mTopLineOffsetPixels = Utils.convertDpToPixel(getContext(), AXIS_TEXT_AREA_HEIGHT_DP);

        mYTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mYTextPaint.setColor(Color.rgb(150, 162, 170));
        mYTextPaint.setTextAlign(Paint.Align.LEFT);
        mYTextPaint.setTextSize(Utils.convertDpToPixel(getContext(), AXIS_TEXT_SIZE_DP));

        mXTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXTextPaint.setColor(Color.rgb(150, 162, 170));
        mXTextPaint.setTextAlign(Paint.Align.LEFT);
        mXTextPaint.setTextSize(Utils.convertDpToPixel(getContext(), AXIS_TEXT_SIZE_DP));

        mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGridPaint.setStrokeWidth(Utils.convertDpToPixel(getContext(), 2));
        mGridPaint.setColor(Color.rgb(241, 241, 242));

        mPointerLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerLinePaint.setStrokeWidth(Utils.convertDpToPixel(getContext(), 1.5f));
        mPointerLinePaint.setColor(Color.BLACK);

        mPointerBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerBorderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPointerBorderPaint.setStrokeWidth(Utils.convertDpToPixel(getContext(), 3));

        xAxis = new XAxis(mXTextPaint, new DateValueFormatter(), this);
        yAxis = new YAxis(mTopLineOffsetPixels, mGridPaint, mYTextPaint, new IntegerValueFormatter());

    }

    private float currentPointer;
    private float previousPointer;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setPressed(true);
                showPointerAt(x);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                showPointerAt(x);
                invalidate();
                return false;
            case MotionEvent.ACTION_UP:
                setPressed(false);
                getParent().requestDisallowInterceptTouchEvent(false);
                hidePointer();
                invalidate();
                return true;
        }

        return false;
    }

    private void hidePointer() {

    }

    private RectF popup;
    private DateToIntDataPoint[] points;
    private Path[] pointsOut;

    private void showPointerAt(float x) {
        Date date = viewPort.xPixelsToValue(x);
        Date closestDate = null;
        for(int i =0; i < dataLines.length; i++){
            pointsOut[i].reset();
            if (dataLines[i].isVisible){
                points[i] = dataLines[i].getClosestPoint(date);
                pointsOut[i].addCircle(viewPort.xValueToPixels(points[i].getX()),
                        viewPort.yValueToPixels(points[i].getY()),
                        mPointerCircleRadius - 1, Path.Direction.CCW);
                closestDate = points[i].getX();
            } else {
                points[i] = null;
            }
        }

        currentPointer = viewPort.xValueToPixels(closestDate);
    }

    public void setColorsForPaints(
            int gridColor,
            int marksTextColor,
            int pointerLineColor,
            int popupTextHeaderColor,
            int popupBackground,
            int popupBorderColor){
        mYTextPaint.setColor(marksTextColor);
        mXTextPaint.setColor(marksTextColor);
        mGridPaint.setColor(gridColor);
        mPointerLinePaint.setColor(pointerLineColor);
    }

    @Override
    protected void onDataChanged() {
        super.onDataChanged();
        points = new DateToIntDataPoint[dataLines.length];
        pointsOut = new Path[dataLines.length];
        for (int i = 0; i < dataLines.length; i++) {
            pointsOut[i] = new Path();
        }
    }

    @Override
    protected void drawUnderView(Canvas canvas) {
        if (isPressed()) {
            for (int i = 0; i < pointsOut.length; i++) {
                canvas.clipPath(pointsOut[i], Region.Op.DIFFERENCE);
            }
        }
        super.drawUnderView(canvas);
        drawYAxis(canvas);
        drawXAxis(canvas);
    }

    @Override
    protected void drawOverView(Canvas canvas) {
        if (isPressed()) {
            canvas.drawLine(currentPointer, viewPort.getHeight() - viewPort.getBottomOffsetPixels(),
                    currentPointer, viewPort.getTopOffsetPixels(), mPointerLinePaint);

            for (int i = 0; i < points.length; i++) {
                if (points[i] != null) {
                    mPointerBorderPaint.setColor(mLineColors[i]);
                    float x = viewPort.xValueToPixels(points[i].getX());
                    float y = viewPort.yValueToPixels(points[i].getY());
                    canvas.drawCircle(x, y, mPointerCircleRadius, mPointerBorderPaint);
                }

            }

        }
    }

    @Override
    protected void setNewViewPort(ChartViewPort newViewPort) {
        int yMax = getRealTop(newViewPort.getYmax(), newViewPort);
        viewPortBuilder.setYmax(yMax);
        super.setNewViewPort(viewPortBuilder.build());
        xAxis.viewPortChanged(viewPort, xmin, xmax);
        yAxis.viewPortChanged(viewPort);
    }

    private int getRealTop(int yMax, ChartViewPort newViewPort) {
        if (newViewPort != null) {
            if (yMax == 0) return 0;
            if (!newViewPort.isValid()) return yMax;

            int div = 10;
            int preDiv = 1;

            float heightFromZeroToLastHorizontalLine = newViewPort.getHeight() - newViewPort.getBottomOffsetPixels() - newViewPort.getTopOffsetPixels() - mTopLineOffsetPixels;

            while (yMax % div <= (yMax / div * div) * mTopLineOffsetPixels / (heightFromZeroToLastHorizontalLine)) {
                preDiv = div;
                div *= 10;
            }

            int lastHorizontalLineValue = yMax / preDiv * preDiv;
            if (preDiv == 1) lastHorizontalLineValue = (yMax / 10 + 1) * 10;

            int topRealOffset = Math.round(lastHorizontalLineValue * mTopLineOffsetPixels / heightFromZeroToLastHorizontalLine);

            return lastHorizontalLineValue + topRealOffset;
        }

        return 0;

    }


    protected void drawXAxis(Canvas canvas) {
        if (xAxis != null)
            xAxis.draw(canvas);
    }

    protected void drawYAxis(Canvas canvas) {
        if (yAxis != null)
            yAxis.draw(canvas);
    }

}
