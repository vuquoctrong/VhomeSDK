package com.viettel.vht.sdk.utils.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;


import com.viettel.vht.sdk.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class TimeLineView extends View {
    public static final long TIME_DELAY_TOUCH = 1000;
    public static final long SECOND = 1;
    public static final long MINUTES = 60 * SECOND;
    public static final long HOUR = 60 * MINUTES * SECOND;
    private int mMinVelocity;
    private Scroller mScroller; //Scroller is a tool class specially used to deal with scrolling effects. Use mScroller to record/calculate the position of View scrolling, and then rewrite View's computeScroll() to complete the actual scrolling.
    private VelocityTracker mVelocityTracker; //Mainly used to track the rate of touch screen events (flinging events and other gestures gesture events).
    private int mWidth;

    private float mSelectorValue = 50.0f; // The default value when not selected The value after sliding indicates the current middle pointer is pointing
    private float mMaxValue = 200; // maximum value
    private float mMinValue = 100.0f; //The smallest value
    private float mPerValue = 1; //The smallest unit such as 1: indicates that every two scale differences are 1. 0.1: indicates that every two scale differences are 0.1
    public float mScale = 1;

    // In the demo, the height mPerValue is 1 and the weight mPerValue is 0.1

    // Custom
    private int unit_hour_width = 6;
    private int unit_10_minute_width = 5;
    private int unit_1_minute_width = 10;
    private boolean show_10_minute;
    private boolean show_1_minute;

    private float mLineSpaceWidth = 5; // distance between 2 lines of ruler scale
    private float mLineWidth = 4; // width of ruler scale

    private float mLineMaxHeight = 420; // Ruler scale is divided into 3 different heights. mLineMaxHeight represents the longest root (that is, the height at a multiple of 10)
    private float mLineMidHeight = 30; // mLineMidHeight represents the middle height (that is, 5 15 25 isochronous height)
    private float mLineMinHeight = 17; // mLineMinHeight represents the shortest height (that is, 1 2 3 4 isochronous height)
    private float mEventHeight = 40;

    private float mTextMarginTop = 30;    //o
    private float mLineMarginTop = 30;    //o
    private float mTextSize = 30; //Digital textsize below the ruler scale

    private boolean mAlphaEnable = false; // Does the ruler need to be transparent on the far left side (transparency effect is better)

    private float mTextHeight; //The height of the number below the ruler scale

    private Paint mTextPaint; // The number below the ruler scale (that is, the value that appears every 10th) paint
    private Paint mLinePaint; // ruler scale paint
    private Paint mEventPaint;
    private Paint mFillEventPaint;
    private Paint mArrowPaint;

    private int mTotalLine; //How many scales there are
    private int mMaxOffset; //How long is the total scale
    private float mOffset; // By default, the position of mSelectorValue is at the position of the total scale of the ruler
    private int mLastX, mMove;
    private OnValueChangeListener mListener; // Value callback after sliding

    private int mLineColor = Color.GRAY; //The color of the scale
    private int mTextColor = Color.BLACK; //Text color
    float mLineSpaceWidthFirt = 0;
    private boolean isTouchable= true;

    public float getmScale() {
        return mScale;
    }

    public void setmScale(float mScale) {
        this.mScale = mScale;
    }

    public float getmSelectorValue() {
        return mSelectorValue;
    }

    public void setmSelectorValue(float mSelectorValue) {
        this.mSelectorValue = mSelectorValue;
    }

    public TimeLineView(Context context) {
        this(context, null);

    }

    public TimeLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private final List<Event> eventList = new ArrayList<>();

    public List<Event> getEventList() {
        return eventList;
    }

    public void setEvents(List<Event> list) {
        eventList.clear();
        eventList.addAll(list);
        invalidate();
    }

    protected void init(Context context, AttributeSet attrs) {
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        mScroller = new Scroller(context);

        final TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.TimeLineView);

        mAlphaEnable = typedArray.getBoolean(R.styleable.TimeLineView_alphaEnable, mAlphaEnable);

        mLineSpaceWidth = typedArray.getDimension(R.styleable.TimeLineView_lineSpaceWidth, mLineSpaceWidth);
        mLineWidth = typedArray.getDimension(R.styleable.TimeLineView_lineWidth, mLineWidth);
        mEventHeight = typedArray.getDimension(R.styleable.TimeLineView_eventHeight, mEventHeight);
        mLineMaxHeight = typedArray.getDimension(R.styleable.TimeLineView_lineMaxHeight, mLineMaxHeight);
        mLineMidHeight = typedArray.getDimension(R.styleable.TimeLineView_lineMidHeight, mLineMidHeight);
        mLineMinHeight = typedArray.getDimension(R.styleable.TimeLineView_lineMinHeight, mLineMinHeight);
        mLineColor = typedArray.getColor(R.styleable.TimeLineView_lineColor, mLineColor);
        mLineMarginTop = typedArray.getDimension(R.styleable.TimeLineView_lineMarginTop, mLineMarginTop);


        mTextSize = typedArray.getDimension(R.styleable.TimeLineView_textSize, mTextSize);
        mTextColor = typedArray.getColor(R.styleable.TimeLineView_textColor, mTextColor);
        mTextMarginTop = typedArray.getDimension(R.styleable.TimeLineView_textMarginTop, mTextMarginTop);

        mSelectorValue = typedArray.getFloat(R.styleable.TimeLineView_selectorValue, 0.0f);
        mMinValue = typedArray.getFloat(R.styleable.TimeLineView_minValue, 0.0f);
        mMaxValue = typedArray.getFloat(R.styleable.TimeLineView_maxValue, 100.0f);
        mPerValue = typedArray.getFloat(R.styleable.TimeLineView_perValue, 0.1f);

        mLineSpaceWidthFirt = mLineSpaceWidth;

        mMinVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mTextHeight = getFontHeight(mTextPaint);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(mLineWidth);
        mLinePaint.setColor(mLineColor);

        mEventPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEventPaint.setStyle(Paint.Style.FILL);
        mEventPaint.setColor(Color.RED);

        mFillEventPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillEventPaint.setStyle(Paint.Style.FILL);
        mFillEventPaint.setColor(Color.WHITE);

        mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPaint.setColor(Color.BLACK);
        mArrowPaint.setStyle(Paint.Style.FILL);

    }

    private float getFontHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }

    /**
     * @param selectorValue When not selected, the default value is the value that the current middle pointer is pointing after sliding
     * @param minValue      maximum value
     * @param maxValue      The smallest value
     */
    public void setValue(float selectorValue, float minValue, float maxValue/*, float per*/, float scale) {
        this.mSelectorValue = selectorValue;
        this.mMaxValue = maxValue;
        this.mMinValue = minValue;
        this.mScale = scale;
        show_10_minute = mScale > 3.5f && mScale < 35.0f;
        show_1_minute = mScale >= 35.0f;
        this.mPerValue = (60 * 10f * 10f) / (show_10_minute ? unit_10_minute_width : (show_1_minute ? unit_1_minute_width : 1));
        this.mTotalLine = (int) ((unit_hour_width * (show_10_minute ? unit_10_minute_width : (show_1_minute ? unit_1_minute_width : 1))) * ((maxValue - minValue) / 3600) + 1);

        this.mLineSpaceWidth = mLineSpaceWidthFirt * scale;

        mMaxOffset = (int) (-(mTotalLine - 1) * mLineSpaceWidth);
        mOffset = (mMinValue - mSelectorValue) / mPerValue * mLineSpaceWidth * 10;
        invalidate();
    }

    public void setOnValueChangeListener(OnValueChangeListener listener) {
        mListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            mWidth = w;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float left, height;
        String value;
        int alpha = 0;
        float scale;
        int srcPointX = mWidth / 2;

        // Draw event
        for (int j = 0; j < eventList.size(); j++) {
            boolean isNeedDraw = true;
            long start = eventList.get(j).getStart();
            long end = eventList.get(j).getEnd();
            if (start < mMinValue) {
                if (end < mMinValue) {
                    isNeedDraw = false;
                } else {
                    start = (long) mMinValue;
                    if (end >= mMaxValue) {
                        end = (long) mMaxValue;
                    }
                }
            } else {
                if (start > mMaxValue) {
                    isNeedDraw = false;
                } else {
                    if (end >= mMaxValue) {
                        end = (long) mMaxValue;
                    }
                }
            }
            if (isNeedDraw) {
                float iStart = (float) ((start - mMinValue) * 10 / mPerValue);
                float iEnd = (float) ((end - mMinValue) * 10 / mPerValue);
                float leftStart = srcPointX + mOffset / (show_10_minute ? unit_10_minute_width : (show_1_minute ? unit_1_minute_width : 1)) + iStart * mLineSpaceWidth / (show_10_minute ? unit_10_minute_width : (show_1_minute ? unit_1_minute_width : 1));
                float leftEnd = srcPointX + mOffset / (show_10_minute ? unit_10_minute_width : (show_1_minute ? unit_1_minute_width : 1)) + iEnd * mLineSpaceWidth / (show_10_minute ? unit_10_minute_width : (show_1_minute ? unit_1_minute_width : 1));
                if (eventList.get(j).getColor() == Color.WHITE) {
                    mFillEventPaint.setColor(Color.WHITE);
                    mEventPaint.setStyle(Paint.Style.STROKE);
                    mEventPaint.setStrokeWidth(2);
                    mEventPaint.setColor(Color.GRAY);
                    canvas.drawRect(leftStart, 1, leftEnd, mEventHeight - 1, mFillEventPaint);
                    canvas.drawRect(leftStart, 1, leftEnd, mEventHeight - 1, mEventPaint);
                } else {
                    mEventPaint.setStyle(Paint.Style.FILL);
                    mEventPaint.setColor(eventList.get(j).getColor());
                    canvas.drawRect(leftStart, 0, leftEnd, mEventHeight, mEventPaint);
                }
            }
        }

        for (int i = 0; i < mTotalLine; i++) {
            left = srcPointX + mOffset / (show_10_minute ? unit_10_minute_width : (show_1_minute ? unit_1_minute_width : 1)) + i * mLineSpaceWidth / (show_10_minute ? unit_10_minute_width : (show_1_minute ? unit_1_minute_width : 1));

            if (left < 0 || left > mWidth) {
                continue; // First draw the default value in the middle, half of the left and right views. The extra part is not drawn temporarily (that is, the default value is in the middle, and the left and right tick marks are drawn)
            }

            /*Line*/
            if (show_10_minute) {
                if (i % 30 == 0) {
                    height = mLineMinHeight;
                } else if (i % unit_10_minute_width == 0) {
                    height = mLineMidHeight;
                } else {
                    height = mLineMaxHeight;
                }
            } else {
                if (i % unit_hour_width == 0) {
                    height = mLineMinHeight;
                } else {
                    height = mLineMidHeight;
                }
            }

            if (mAlphaEnable) {
                scale = 1 - Math.abs(left - srcPointX) / srcPointX;
                alpha = (int) (255 * scale * scale);

                mLinePaint.setAlpha(alpha);
            }
            canvas.drawLine(left, mLineMarginTop + mEventHeight, left, height + mLineMarginTop + mEventHeight, mLinePaint);
//            canvas.drawLine(left, mLineMaxHeight + mTextMarginTop + mTextHeight, srcPointX + mOffset / (show_minute ? unit_minute_width : 1) + (mTotalLine - 1) * mLineSpaceWidth / (show_minute ? unit_minute_width : 1), mLineMaxHeight + mTextMarginTop + mTextHeight, mLinePaint);

            /*Text*/
            if (i % (unit_hour_width * (show_10_minute ? unit_10_minute_width : (show_1_minute ? unit_1_minute_width : 1))) == 0 || (show_10_minute && i % unit_10_minute_width == 0) || (show_1_minute && i % 1 == 0)) {
                value = convertToTime((long) (mMinValue + i * mPerValue / 10));
                if (mAlphaEnable) {
                    mTextPaint.setAlpha(alpha);
                }
                canvas.drawText(value, left - mTextPaint.measureText(value) / 2,
                        mLineMarginTop + mTextMarginTop + mTextHeight + mEventHeight + mLineMidHeight, mTextPaint); // When it is an integer, draw the value
            }
        }
    }

    private Boolean isTouching = false;
    private Handler handlerTouch = new Handler();
    private Runnable runableTouch = () -> {
        if (null != mListener) {
            mListener.onRealValueChange(mSelectorValue);
        }
    };

    public void setTouchable(boolean isTouchable) {
        this.isTouchable = isTouchable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isTouchable){
            scaleGestureDetector.onTouchEvent(event);
            if (event.getPointerCount() > 1) {
                scaleOngoing = true;
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 500);
            }
            if (scaleOngoing) {
                handlerTouch.removeCallbacks(runableTouch);
                handlerTouch.postDelayed(runableTouch, TIME_DELAY_TOUCH);
                return true;
            }
            int action = event.getAction();
            int xPosition = (int) event.getX();

            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(event);

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    isTouching = true;
                    mScroller.forceFinished(true);
                    mLastX = xPosition;
                    mMove = 0;
                    break;
                case MotionEvent.ACTION_MOVE:
                    isTouching = true;
                    mMove = (mLastX - xPosition);
                    changeMoveAndValue();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isTouching = false;
                    countMoveEnd();
                    countVelocityTracker();
                    if (!isTouching){
                        handlerTouch.removeCallbacks(runableTouch);
                        handlerTouch.postDelayed(runableTouch, TIME_DELAY_TOUCH);
                    }
                    return false;
            }

            mLastX = xPosition;
            return true;
        }else {
            return false;
        }

    }

    private void countVelocityTracker() {
        mVelocityTracker.computeCurrentVelocity(1000 * (show_10_minute ? 5 : (show_1_minute ? 15 : 1))); //The unit of initialization rate
        float xVelocity = mVelocityTracker.getXVelocity(); //current speed
        if (Math.abs(xVelocity) > mMinVelocity) {
            mScroller.fling(0, 0, (int) xVelocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
        }
    }


    /**
     * After sliding, if the pointer is between 2 scales, change mOffset so that the pointer is just on the scale.
     */
    private void countMoveEnd() {

        mOffset -= mMove;
        if (mOffset <= mMaxOffset) {
            mOffset = mMaxOffset;
        } else if (mOffset >= 0) {
            mOffset = 0;
        }

        mLastX = 0;
        mMove = 0;

        mSelectorValue = mMinValue + (Math.abs(mOffset) * 1.0f / mLineSpaceWidth) * mPerValue / 10.0f;
        mOffset = (mMinValue - mSelectorValue) * 10.0f / mPerValue * mLineSpaceWidth;

        notifyValueChange();
        postInvalidate();
    }


    /**
     * Operation after sliding
     */
    private void changeMoveAndValue() {
        mOffset -= mMove;

        if (mOffset <= mMaxOffset) {
            mOffset = mMaxOffset;
            mMove = 0;
            mScroller.forceFinished(true);
        } else if (mOffset >= 0) {
            mOffset = 0;
            mMove = 0;
            mScroller.forceFinished(true);
        }
        mSelectorValue = mMinValue + (Math.abs(mOffset) * 1.0f / mLineSpaceWidth) * mPerValue / 10.0f;
        notifyValueChange();
        postInvalidate();
    }

    private void notifyValueChange() {
        if (null != mListener) {
            mListener.onValueChange(mSelectorValue);
        }
    }


    /**
     * Callback after sliding
     */
    public interface OnValueChangeListener {
        void onValueChange(float value);

        void onRealValueChange(float value);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {//mScroller.computeScrollOffset() returns true to indicate that the slide has not ended
            if (mScroller.getCurrX() == mScroller.getFinalX()) {
                countMoveEnd();
            } else {
                int xPosition = mScroller.getCurrX();
                mMove = (mLastX - xPosition);
                changeMoveAndValue();
                mLastX = xPosition;
            }
            handlerTouch.removeCallbacks(runableTouch);
            handlerTouch.postDelayed(runableTouch, TIME_DELAY_TOUCH);
        }
    }

    /**
     * Convert time second to hh:mm
     *
     * @param callSecond time second
     */
    public String convertToTime(Long callSecond) {
        if (callSecond == null) {
            return "00:00";
        }
        long oneHourSecond = TimeUnit.HOURS.toSeconds(1);
        long oneMinuteSecond = TimeUnit.MINUTES.toSeconds(1);

        int hour = (int) (callSecond / oneHourSecond);
        callSecond = callSecond % oneHourSecond;
        int minute = (int) (callSecond / oneMinuteSecond);
        return String.format("%02d:%02d", hour, minute);
    }

    /**
     * Convert time second to hh:mm:ss
     *
     * @param callSecond time second
     */
    public String convertToTimeSecond(Long callSecond) {
        if (callSecond == null) {
            return "00:00:00";
        }
        long oneHourSecond = TimeUnit.HOURS.toSeconds(1);
        long oneMinuteSecond = TimeUnit.MINUTES.toSeconds(1);

        int hour = (int) (callSecond / oneHourSecond);
        callSecond = callSecond % oneHourSecond;
        int minute = (int) (callSecond / oneMinuteSecond);
        int seconds = (int) (callSecond % 60);
        return String.format("%02d:%02d:%02d", hour, minute, seconds);
    }

    // Pinch zoom
    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;
    private boolean scaleOngoing = false;
    private final Handler handler = new Handler();
    private OnScaleChanged onScaleChanged;

    public void setOnScaleChanged(OnScaleChanged onScaleChanged) {
        this.onScaleChanged = onScaleChanged;
    }

    Runnable runnable = () -> scaleOngoing = false;

    class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (detector.getScaleFactor() >= 1) {
                scaleFactor = detector.getScaleFactor() * scaleFactor * 1.02f;
            } else {
                scaleFactor = detector.getScaleFactor() * scaleFactor * 0.96f;
            }
            scaleFactor = Math.max(0.9f, Math.min(scaleFactor, 50.0f));
            setValue(mSelectorValue, mMinValue, mMaxValue, scaleFactor);
            onScaleChanged.onScale(mScale);
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, 500);
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            scaleOngoing = true;
            return true;
        }
    }

    public static class Event {
        private String eventId;
        private long start;
        private long end;
        private int color;

        public Event(String eventId, long start, long end, int color) {
            this.eventId = eventId;
            this.start = start;
            this.end = end;
            this.color = color;
        }

        public String getEventId() {
            return eventId;
        }

        public void setEventId(String eventId) {
            this.eventId = eventId;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public long getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public long getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }
    }

    interface OnScaleChanged {
        void onScale(float value);
    }
}

