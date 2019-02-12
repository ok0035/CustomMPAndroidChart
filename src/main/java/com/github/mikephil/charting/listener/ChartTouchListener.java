package com.github.mikephil.charting.listener;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.highlight.Highlight;

import java.util.ArrayList;

import static java.sql.Types.NULL;

/**
 * Created by philipp on 12/06/15.
 */
public abstract class ChartTouchListener<T extends Chart<?>> extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {

    public enum ChartGesture {
        NONE, DRAG, X_ZOOM, Y_ZOOM, PINCH_ZOOM, ROTATE, SINGLE_TAP, DOUBLE_TAP, LONG_PRESS, FLING
    }

    /**
     * the last touch gesture that has been performed
     **/
    protected ChartGesture mLastGesture = ChartGesture.NONE;

    // states
    protected static final int NONE = 0;
    protected static final int DRAG = 1;
    protected static final int X_ZOOM = 2;
    protected static final int Y_ZOOM = 3;
    protected static final int PINCH_ZOOM = 4;
    protected static final int POST_ZOOM = 5;
    protected static final int ROTATE = 6;

    protected float min;
    protected int minIndex = 0;
    protected Highlight selectedHighlight;
    protected Highlight dragHighlight;
    protected int touchDist = 100;

    /**
     * integer field that holds the current touch-state
     */
    protected int mTouchMode = NONE;

    /**
     * the last highlighted object (via touch)
     */
    protected ArrayList<Highlight> mLastHighlighted = new ArrayList<>();

    /**
     * the gesturedetector used for detecting taps and longpresses, ...
     */
    protected GestureDetector mGestureDetector;

    /**
     * the chart the listener represents
     */
    protected T mChart;

    public ChartTouchListener(T chart) {
        this.mChart = chart;

        mGestureDetector = new GestureDetector(chart.getContext(), this);
    }

    /**
     * Calls the OnChartGestureListener to do the start callback
     *
     * @param me
     */
    public void startAction(MotionEvent me) {

        OnChartGestureListener l = mChart.getOnChartGestureListener();

        if (l != null)
            l.onChartGestureStart(me, mLastGesture);
    }

    /**
     * Calls the OnChartGestureListener to do the end callback
     *
     * @param me
     */
    public void endAction(MotionEvent me) {

        OnChartGestureListener l = mChart.getOnChartGestureListener();

        if (l != null)
            l.onChartGestureEnd(me, mLastGesture);
    }

    /**
     * Sets the last value that was highlighted via touch.
     *
     * @param high
     *
     *
     */

    public void setLastHighlighted(Highlight high) {
        mLastHighlighted.clear();
    }

    public void setLastHighlighted(ArrayList<Highlight> highs) {
        mLastHighlighted = highs;
    }

    public void setLastHighlighted(int index, Highlight high) {
        if(index < mLastHighlighted.size())
            mLastHighlighted.set(index, high);
        else Log.d("setLastHighlighted", "OutOfIndex");
    }

    public void setOffSelectHighlight() {
        selectedHighlight = null;
        minIndex = 0;
    }

    public void addLastHighlighted(Highlight high) {
        mLastHighlighted.add(high);
    }

    public int getXByIndex(int index) {
        if(mLastHighlighted != null && mLastHighlighted.size() != 0 && index < mLastHighlighted.size() && mLastHighlighted.get(index).getX() != NULL)
            return (int) mLastHighlighted.get(index).getX();
        else if(mLastHighlighted != null && index < mLastHighlighted.size() && mLastHighlighted.get(index).getX() == NULL) return 0;
        else return 0;
    }

    public ArrayList<Highlight> getLastHighlighted() {
        return mLastHighlighted;
    }

    public void clearLastHighlighted() {
        mLastHighlighted = null;
    }

    /**
     * returns the touch mode the listener is currently in
     *
     * @return
     */
    public int getTouchMode() {
        return mTouchMode;
    }

    /**
     * Returns the last gesture that has been performed on the chart.
     *
     * @return
     */
    public ChartGesture getLastGesture() {
        return mLastGesture;
    }


    /**
     * Perform a highlight operation.
     *
     * @param e
     */
    protected void performHighlight(Highlight h, MotionEvent e) {

        if(mLastHighlighted.size() == 0) mLastHighlighted.add(dragHighlight);
        if(mLastHighlighted.get(0) == null) return;

        dragHighlight = mChart.getHighlightByTouchPoint(e.getX(), e.getY());

        min = Math.abs(mLastHighlighted.get(0).getDrawX() - e.getX());
        minIndex = 0;

        Log.d("mLastHighlighted", "mLastHighlighted size : " + mLastHighlighted.size());

        Log.d("mLastHighlighted", "getDrawX() : " + mLastHighlighted.get(0).getDrawX());
        Log.d("mLastHighlighted", "index : " + 0 + " dist : " + Math.abs(mLastHighlighted.get(0).getDrawX() - e.getX()));

        for(int i=1; i<mLastHighlighted.size(); i++) {

            Log.d("mLastHighlighted", "getDrawX() : " + mLastHighlighted.get(i).getDrawX());
//            Log.d("mLastHighlighted", "getX() : " + mLastHighlighted.get(i).getX());
//            Log.d("performHighlightDrag", "Event getX() : " + e.getX());
            Log.d("mLastHighlighted", "index : " + i + " dist : " + Math.abs(mLastHighlighted.get(i).getDrawX() - e.getX()));

            if(mLastHighlighted.get(i) != null && Math.abs(mLastHighlighted.get(i).getDrawX() - e.getX()) < min) {
                min = Math.abs(mLastHighlighted.get(i).getDrawX() - e.getX());
                minIndex = i;
            }
        }

        if (dragHighlight != null && mLastHighlighted.get(minIndex) != null && !dragHighlight.equalTo(mLastHighlighted.get(minIndex)) && min < touchDist ) {
            mLastHighlighted.set(minIndex, dragHighlight);
            mChart.highlightValue(dragHighlight, minIndex, true);
            selectedHighlight = mLastHighlighted.get(minIndex);
            Log.d("getDataIndex", "Selected Data Index : " + mLastHighlighted.get(minIndex).getX());
        } else if(dragHighlight != null && mLastHighlighted.get(minIndex) != null && dragHighlight.equalTo(mLastHighlighted.get(minIndex)) && min < touchDist){
            selectedHighlight = mLastHighlighted.get(minIndex);
            Log.d("getDataIndex", "Selected Data Index : " + mLastHighlighted.get(minIndex).getX());
        }

//        if(mLastHighlighted == null && !h.equalTo(minHighlight)) {
//            mChart.highlightValue(h, true);
//            mLastHighlighted.set(minIndex, h);
//        }

//        if (h == null || h.equalTo(mLastHighlighted)) {
//            mChart.highlightValue(null, true);
//            mLastHighlighted = null;
//
//        } else {
//            mChart.highlightValue(h, true);
//            mLastHighlighted = h;
//        }
    }

    /**
     * returns the distance between two points
     *
     * @param eventX
     * @param startX
     * @param eventY
     * @param startY
     * @return
     */
    protected static float distance(float eventX, float startX, float eventY, float startY) {
        float dx = eventX - startX;
        float dy = eventY - startY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public Highlight getSelectedHighlight() {
        return selectedHighlight;
    }

    public int getSelectedHighlightIndex() {
        return minIndex;
    }

}
