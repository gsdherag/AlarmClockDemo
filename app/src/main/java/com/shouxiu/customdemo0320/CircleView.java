package com.shouxiu.customdemo0320;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yeping
 * @date 2018/3/20 11:45
 * TODO
 */

public class CircleView extends View {

    public static final int DEFAULT_SIZE = 250;
    public static final int DEFAULT_TEXT_SIZE = 14;
    /**
     * 文字距边框
     */
    public static final int DEFAULT_TEXT_MARGIN = 30;

    private Paint mCirclePaint;
    private Paint mTextPaint;
    private Context mContext;
    private int mDefaultSize;
    private float mTextSize;
    private int mColor;
    private int mTextColor;
    private int mPointerColor;
    private Paint mPointPaint;
    private Map<Integer, Point> mMap = new HashMap<>();
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private int mPaddingBottom;
    private int mWidth;
    private int mHeight;
    private int mHour;
    private float eventX;
    private float eventY;
    private int mCenterX;
    private int mCenterY;
    private int mR;

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleView);
        mColor = ta.getColor(R.styleable.CircleView_circle_color, Color.RED);
        mTextSize = ta.getDimension(R.styleable.CircleView_text_size, DEFAULT_TEXT_SIZE);
        mTextColor = ta.getColor(R.styleable.CircleView_text_color, Color.WHITE);
        mPointerColor = ta.getColor(R.styleable.CircleView_pointer_color, Color.YELLOW);
        ta.recycle();

        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mDefaultSize = dipToPx(mContext, DEFAULT_SIZE);
        mCirclePaint = new Paint();
        mCirclePaint.setColor(mColor);
        mCirclePaint.setStrokeWidth(5f);
        mCirclePaint.setStyle(Paint.Style.FILL);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mPointPaint = new Paint();
        mPointPaint.setColor(mPointerColor);
        mPointPaint.setStrokeWidth(5f);
        mPointPaint.setStyle(Paint.Style.FILL);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getDefault(widthMeasureSpec, mDefaultSize),
                getDefault(heightMeasureSpec, mDefaultSize));
    }

    public int getDefault(int measureSpec, int defaultSize) {
        int result = defaultSize;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);

        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    public int dipToPx(Context context, float dip) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPaddingLeft = getPaddingLeft();
        mPaddingRight = getPaddingRight();
        mPaddingTop = getPaddingTop();
        mPaddingBottom = getPaddingBottom();

        mWidth = getWidth() - mPaddingLeft - mPaddingRight;
        mHeight = getHeight() - mPaddingTop - mPaddingBottom;

        mR = Math.min(mWidth, mHeight) / 2;

        mHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        mCenterX = mPaddingLeft + mWidth / 2;
        mCenterY = mPaddingTop + mHeight / 2;

        if (mMap.isEmpty()) {
            for (int i = -90; i < 360 - 90; i += 30) {
                //            x1   =   x0   +   r   *   cos(ao   *   3.14   /180   )
                //            y1   =   y0   +   r   *   sin(ao   *   3.14   /180   )
                int text = i == -90 ? 12 : i / 30 + 3;
                float textX = getTextX(mCenterX, i, mR - DEFAULT_TEXT_MARGIN * 2);
                float textY = getTextY(mCenterY, i, mR - DEFAULT_TEXT_MARGIN * 2) + measureTextHeight(mTextPaint) / 2;
                mMap.put(text, new Point(textX, textY));
            }

            for (int i = -90; i < 360 - 90; i += 30) {
                //            x1   =   x0   +   r   *   cos(ao   *   3.14   /180   )
                //            y1   =   y0   +   r   *   sin(ao   *   3.14   /180   )
                String text = i == -90 ? "00" : i / 30 + 3 + 12 + "";
                float textX = getTextX(mCenterX, i, mR / 2 + DEFAULT_TEXT_MARGIN);
                float textY = getTextY(mCenterY, i, mR / 2 + DEFAULT_TEXT_MARGIN) + measureTextHeight(mTextPaint) / 2;

                mMap.put(Integer.valueOf(text), new Point(textX, textY));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制背景
        canvas.drawCircle(mCenterX, mCenterY, mR, mCirclePaint);
        //绘制圆心点
        canvas.drawCircle(mCenterX, mCenterY, 10, mPointPaint);
        //绘制指向的时间点的圆圈
        Point point1 = mMap.get(mHour);
        if (point1 == null) {
            return;
        }
        canvas.drawCircle(point1.x, point1.y, DEFAULT_TEXT_MARGIN, mPointPaint);
        //绘制线
        canvas.drawLine(mCenterX, mCenterY, point1.x, point1.y, mPointPaint);

        //绘制外圈文字
        for (int i = -90; i < 360 - 90; i += 30) {
            int text = i == -90 ? 12 : i / 30 + 3;
            Point point = mMap.get(text);
            canvas.drawText("" + text, point.x, point.y + measureTextHeight(mTextPaint) / 2, mTextPaint);
        }

        //绘制内圈文字
        for (int i = -90; i < 360 - 90; i += 30) {
            String text = i == -90 ? "00" : i / 30 + 3 + 12 + "";
            Point point = mMap.get(Integer.valueOf(text));
            canvas.drawText("" + text, point.x, point.y + measureTextHeight(mTextPaint) / 2, mTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                eventX = event.getX();
                eventY = event.getY();
                mHour = getAngle();
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                eventX = event.getX();
                eventY = event.getY();
                mHour = getAngle();
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:

                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private int getAngle() {
        double angle = Math.abs(Math.toDegrees(Math.atan((eventY - mCenterY) / (eventX - mCenterX))));
        float y = eventY - mCenterY;
        float x = eventX - mCenterX;
        double sqrt = Math.sqrt(y * y + x * x);
        float r = mR / 2 + DEFAULT_TEXT_MARGIN + measureTextHeight(mTextPaint) / 2;
        System.out.println(angle);
        int an = 0;
        if (eventY < mCenterY && eventX < mCenterX) {
            if (angle < 15) {
                an = 9;
            } else if (angle < 45) {
                an = 10;
            } else if (angle < 75) {
                an = 11;
            } else {
                an = 12;
            }
        }

        if (eventY < mCenterY && eventX > mCenterX) {
            if (angle < 15) {
                an = 3;
            } else if (angle < 45) {
                an = 2;
            } else if (angle < 75) {
                an = 1;
            } else {
                an = 12;
            }
        }

        if (eventY > mCenterY && eventX > mCenterX) {
            if (angle < 15) {
                an = 3;
            } else if (angle < 45) {
                an = 4;
            } else if (angle < 75) {
                an = 5;
            } else {
                an = 6;
            }
        }

        if (eventY > mCenterY && eventX < mCenterX) {
            if (angle < 15) {
                an = 9;
            } else if (angle < 45) {
                an = 8;
            } else if (angle < 75) {
                an = 7;
            } else {
                an = 6;
            }
        }
        if (sqrt > r) {
            return an;
        } else {
            int i = an + 12;
            return i == 24 ? 0 : i;
        }
    }

    /**
     * 获取text的坐标x
     *
     * @param x0 圆心
     * @param ao 角度
     * @param r  半径
     * @return 坐标x
     */
    public float getTextX(int x0, int ao, int r) {
        return (float) (x0 + r * Math.cos(ao * Math.PI / 180));
    }

    /**
     * 获取text的坐标y
     *
     * @param y0 圆心
     * @param ao 角度
     * @param r  半径
     * @return 坐标y
     */
    public float getTextY(int y0, int ao, int r) {
        return (float) (y0 + r * Math.sin(ao * Math.PI / 180));
    }

    /**
     * 计算文字高度
     *
     * @param paint
     * @return
     */
    public float measureTextHeight(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return (Math.abs(fontMetrics.ascent) - fontMetrics.descent);
    }

    class Point {
        float x;
        float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
