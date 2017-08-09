package com.secray.percentview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;


/**
 * @author secray
 * @date 2017-07-03
 */

public class PercentView extends View {
    private static final int DEFAULT_TEXT_SIZE = 30;
    private static final String CHARACTER_PERCENT = "%";
    // Default first color
    private static final int FIRST_COLOR = Color.parseColor("#0092cf");
    // Default second color
    private static final int SECOND_COLOR = Color.parseColor("#00f6ff");
    // Direction top as default
    public static final int TOP = 1, BOTTOM = 0;
    // Typeface Normal as default
    public static final int NORMAL = 0, SANS = 1, SERIF = 2, MONOSPACE = 3;

    public static final int GRAVITY_TOP = 0, GRAVITY_BOTTOM = 1,
            GRAVITY_CENTER = 2, GRAVITY_LEFT = 3, GRAVITY_RIGHT = 4;

    private Paint mPaint;
    private Paint mPercentPaint;
    private Rect mBound;
    private Context mContext;

    private String mText;
    private int mFirstColor;
    private int mSecondColor;
    private int mDirection;
    private int mGravity;
    private int mTextSize;
    private Typeface mTypeface;

    private float mPercent = 0f;
    private float mPadding;

    enum M {
        WIDTH,
        HEIGHT
    }

    enum VALUE {
        DP,
        SP
    }

    public PercentView(Context context) {
        this(context, null);
    }

    public PercentView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PercentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PercentView);

        mFirstColor = a.getColor(R.styleable.PercentView_firstColor, FIRST_COLOR);
        mSecondColor = a.getColor(R.styleable.PercentView_secondColor, SECOND_COLOR);
        mDirection = a.getInt(R.styleable.PercentView_direction, TOP);
        mTextSize = a.getDimensionPixelSize(R.styleable.PercentView_textSize, DEFAULT_TEXT_SIZE);
        mGravity = a.getInt(R.styleable.PercentView_gravity, GRAVITY_CENTER);
        mPercent = a.getFloat(R.styleable.PercentView_percent, 0f);
        int typeface = a.getInt(R.styleable.PercentView_typeface, NORMAL);

        a.recycle();

        mContext = context;
        mTypeface = obtainTypeface(typeface);
        mBound = new Rect();
        mText = (int)(mPercent * 100) + "";
        mPadding = value2px(0.5f, VALUE.DP);
        initPaint();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PercentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureSize(M.WIDTH, widthMeasureSpec);
        int height = measureSize(M.HEIGHT, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float textWidth = mPaint.measureText(mText);

        float startX = (getMeasuredWidth() - textWidth
                - mPercentPaint.measureText(CHARACTER_PERCENT)) / 2 + getPaddingLeft();
        float startY = (0f + getMeasuredHeight() + Math.abs(mBound.height())) / 2 + getPaddingTop();
        float clipStartY = (0f + getMeasuredHeight() - Math.abs(mBound.height())) / 2;
        switch (mGravity) {
            case GRAVITY_BOTTOM:
                startY = getMeasuredHeight() - getPaddingBottom() - mPadding;
                clipStartY = getMeasuredHeight() - Math.abs(mBound.height()) - 2 * mPadding;
                break;
            case GRAVITY_CENTER:
                break;
            case GRAVITY_TOP:
                startY = getPaddingTop() + Math.abs(mBound.height()) + mPadding;
                clipStartY = getPaddingTop();
                break;
            case GRAVITY_LEFT:
                startX = getPaddingLeft();
                Log.i("xk", "startY = " + startY + " " + clipStartY);
                break;
            case GRAVITY_RIGHT:
                startX = getMeasuredWidth() - getPaddingRight() -
                        mPaint.measureText(mText) - mPercentPaint.measureText(CHARACTER_PERCENT);
                break;
        }

        float clipEndY;
        float height = Math.abs(mBound.height());
        if (mDirection == TOP) {
            clipEndY = clipStartY + height * mPercent;
            drawText(canvas, mFirstColor, startX, startY, startX,
                    clipStartY, getMeasuredWidth(), clipEndY);

            drawText(canvas, mSecondColor, startX, startY, startX,
                    clipEndY, getMeasuredWidth(), startY + mPadding);
        } else if (mDirection == BOTTOM) {
            clipEndY = startY - height * mPercent;
            drawText(canvas, mFirstColor, startX, startY, startX,
                    clipEndY, getMeasuredWidth(), startY + mPadding);

            drawText(canvas, mSecondColor, startX, startY, startX,
                    clipStartY, getMeasuredWidth(), clipEndY);
        }
        canvas.drawText(CHARACTER_PERCENT, startX + textWidth, startY, mPercentPaint);
        canvas.drawLine(0, getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight(), mPaint);
        canvas.drawLine(0, clipStartY, getMeasuredWidth(), clipStartY, mPaint);
    }

    private void initPaint() {
        // init main paint
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTypeface(mTypeface);
        mPaint.setTextSize(value2px(mTextSize, VALUE.SP));

        // init percent character paint
        mPercentPaint = new Paint();
        mPercentPaint.setAntiAlias(true);
        mPercentPaint.setStyle(Paint.Style.FILL);
        mPercentPaint.setTypeface(mTypeface);
        mPercentPaint.setTextSize(value2px(mTextSize, VALUE.SP) * 4 / 25);
        mPercentPaint.setColor(mSecondColor);
    }

    private void drawText(Canvas canvas, int color,  float x, float y,
                          float left, float top, float right, float bottom) {
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        mPaint.setColor(color);
        canvas.clipRect(left, top, right, bottom);
        canvas.drawText(mText, x, y, mPaint);
        canvas.restore();
    }

    private int measureSize(M m, int measureSpec) {
        int measureSize = 0;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        mPaint.getTextBounds(mText, 0, mText.length(), mBound);

        switch (mode) {
            case MeasureSpec.EXACTLY:
                measureSize = size;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                float value;
                if (m == M.HEIGHT) {
                    value = Math.abs(mBound.height()) + 2 * mPadding;
                    measureSize = (int) value + getPaddingTop() + getPaddingBottom();
                } else {
                    value = mPaint.measureText(mText);
                    measureSize = (int) (value + getPaddingLeft() + getPaddingRight()
                            + mPercentPaint.measureText(CHARACTER_PERCENT));
                }
                break;
        }
        return measureSize;
    }

    private float value2px(float value, VALUE v) {
        return TypedValue.applyDimension(v == VALUE.DP ? TypedValue.COMPLEX_UNIT_DIP : TypedValue.COMPLEX_UNIT_SP,
                value, mContext.getResources().getDisplayMetrics());
    }

    private Typeface obtainTypeface(int typeface) {
        Typeface t;
        switch (typeface) {
            case NORMAL:
                t = Typeface.DEFAULT;
                break;
            case SANS:
                t = Typeface.SANS_SERIF;
                break;
            case SERIF:
                t = Typeface.SERIF;
                break;
            case MONOSPACE:
                t = Typeface.MONOSPACE;
                break;
            default:
                t = Typeface.DEFAULT;
                break;
        }
        return t;
    }

    @MainThread
    public void setPercent(float percent) {
        mPercent = percent;
        mText = (int) (percent * 100) + "";
        if (mText.length() >= 3) {
            mTextSize = 86;
            mPaint.setTextSize(value2px(mTextSize, VALUE.DP));
        }
        requestLayout();
        invalidate();
    }

    @MainThread
    public void setFirstColor(@ColorInt int firstColor) {
        this.mFirstColor = firstColor;
        invalidate();
    }

    @MainThread
    public void setSecondColor(@ColorInt int secondColor) {
        this.mSecondColor = secondColor;
        mPercentPaint.setColor(secondColor);
        invalidate();
    }


    /****
     * Sets the direction of the progress
     * @param direction 0 as bottom, 1 as top
     */
    @MainThread
    public void setDirection(int direction) {
        this.mDirection = direction;
        invalidate();
    }

    /***
     * Sets the text size, sp as unit
     * @param textSize
     */
    @MainThread
    public void setTextSize(int textSize) {
        mPaint.setTextSize(textSize);
        requestLayout();
        invalidate();
    }

    /**
     * Sets the typeface and style in which the text should be displayed.
     * Note that not all Typeface families actually have bold and italic
     * variants
     */
    @MainThread
    public void setTypeface(Typeface typeface) {
        mPaint.setTypeface(typeface);
        requestLayout();
        invalidate();
    }

    /****
     * Sets the typeface and style in which the text should be displayed.
     * Note that not all Typeface families actually have bold and italic
     * variants
     * @param typeface NORMAL 0,SAN 1,SERIF 2,MONOSPACE 3
     */
    public void setTypeface(int typeface) {
        mPaint.setTypeface(obtainTypeface(typeface));
        mPercentPaint.setTypeface(obtainTypeface(typeface));
        requestLayout();
        invalidate();
    }
}