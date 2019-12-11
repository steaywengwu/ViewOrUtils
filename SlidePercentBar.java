package com.kelin.cvimonitor.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.kelin.cvimonitor.R;
import com.kelin.cvimonitor.utils.Logout;
import com.kelin.cvimonitor.utils.ToolUtils;

import java.text.DecimalFormat;

/**
 * author: steaywengwu
 * time: 2019/12/9 0009 14:24:24
 * class description:            手指左右滑动进度条
 *
 * android:layout_height="35dp"，控件高度35dp时不用调参数
 *
 */
public class SlidePercentBar extends View {
    private int paddingTop = 10;//10dp
    private int paddingBottom = 10;
    private int paddingLeft = 15;
    private int paddingRight = 15;
    private float leftBarX, rightBarX;//左右阀值滑动控制点
    private float barHeight, barWidth;//bar 的宽高
    private boolean isSetMode = false;//设置初始值模式
    private int bgColor, leftColor, rightColor;//背景色，左，右颜色
    private Paint bgPaint, leftPaint, rightPaint, innerCirclePaint, outterCirclePaint, textPaint;
    private float innerRadius, outterRadius;//拖动点内外圆半径
    private String leftPercent, rightPercent;//左右百分比数，
    private DecimalFormat mDecimalFormat;
    private int mWidth, mHeight;
    private double leftPer, rightPer;//设置初始左右百分比
    private double maxPercent = 100.0;//最大百分比
    boolean isSlideLeft = true;//滑动左边
    boolean isSliding = false;//是否正在滑动，显示百分比进度文本
    PercentCallBackListener mListener;//滑动监听进度

    public void setListener(PercentCallBackListener listener) {
        mListener = listener;
    }

    public void setPercent(double leftPercent, double rightPercent) {
        this.leftPer = leftPercent;
        this.rightPer = rightPercent;
        isSetMode = true;
        //此处使用invalidate()是无效的，因为该方法早于onMeasure()调用，控件宽高还没好，barWidth=0;
    }

    /**
     * 设置最大百分比
     * @param maxPercent 已经乘以100的值
     */
    public void setMaxPercent(double maxPercent) {
        this.maxPercent = maxPercent;
        isSetMode = true;
    }
    public interface PercentCallBackListener {
        void getProgress(String leftPercent, String rightPercent);
    }

    public SlidePercentBar(Context context) {
        this(context,null);
    }

    public SlidePercentBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        bgColor = Color.parseColor("#F7F7F7");//ContextCompat.getColor(context, R.color.bgColor);
        leftColor = Color.parseColor("#F5A623");//ContextCompat.getColor(context, R.color.orange);
        rightColor =Color.parseColor("#fc6156");// ContextCompat.getColor(context, R.color.redfc);

        paddingTop = dpTpPx(paddingTop);
        paddingBottom = dpTpPx(paddingBottom);
        paddingLeft = dpTpPx(paddingLeft);
        paddingRight = dpTpPx(paddingRight);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(bgColor);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);
        bgPaint.setStyle(Paint.Style.STROKE);

        leftPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        leftPaint.setColor(leftColor);
        leftPaint.setStyle(Paint.Style.STROKE);
        leftPaint.setStrokeCap(Paint.Cap.ROUND);


        rightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rightPaint.setColor(rightColor);
        rightPaint.setStrokeCap(Paint.Cap.ROUND);
        rightPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(ContextCompat.getColor(context, R.color.black3));
        textPaint.setTextSize(ToolUtils.sp2px(context, 12));
        textPaint.setTextAlign(Paint.Align.CENTER);

        innerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerCirclePaint.setStyle(Paint.Style.FILL);
        outterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outterCirclePaint.setStyle(Paint.Style.STROKE);
        outterCirclePaint.setStrokeWidth(dpTpPx(1));
        mDecimalFormat = new DecimalFormat("0.0");

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // get calculate mode of width and height
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        // get recommend width and height
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (modeWidth == MeasureSpec.UNSPECIFIED) { // this view used in scrollView or listview or recyclerView
            int wrap_width = getPaddingLeft() + getPaddingRight();
            sizeWidth = wrap_width;
            modeWidth = MeasureSpec.EXACTLY;
        }

        if (modeHeight == MeasureSpec.UNSPECIFIED) { // this view used in scrollView or listview or recyclerView
            int wrap_height = getPaddingTop() + getPaddingBottom();
            sizeHeight = wrap_height;
            modeHeight = MeasureSpec.EXACTLY;
        }

        if (modeWidth == MeasureSpec.AT_MOST) { // wrap_content
            int wrap_width = 100 + getPaddingLeft() + getPaddingRight();
            sizeWidth = Math.min(wrap_width, sizeWidth);
            modeWidth = MeasureSpec.EXACTLY;
        }

        if (modeHeight == MeasureSpec.AT_MOST) { // wrap_content
            int wrap_height = 100 + getPaddingTop() + getPaddingBottom();
            sizeHeight = Math.min(wrap_height, sizeHeight);
            modeHeight = MeasureSpec.EXACTLY;
        }

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(sizeWidth, modeWidth);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(sizeHeight, modeHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Logout.e("onmeasuer", "====" + sizeWidth + ":::" + sizeHeight);
        mWidth = sizeWidth;
        mHeight = sizeHeight;
        initBarPercent(sizeWidth, sizeHeight);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isSetMode) {
            double leftP = leftPer*(100/maxPercent) * barWidth;
            leftBarX = (float) (paddingLeft + leftP);
            rightBarX = (float) (paddingLeft + rightPer*(100/maxPercent) * barWidth);
            isSetMode = false;

            Logout.e("isSetMode", "leftBarX" + leftBarX + ";rightBarX::" + rightBarX + "   leftPercent:" + leftPercent + "barWidth==" + barWidth + "leftp" + leftP);
        }

        int width = mWidth;
        int height = mHeight;
        Logout.e("ss", "onDraw======================" + leftBarX + "::" + rightBarX + "\n" + mWidth + mHeight);
        float topLine = paddingTop;
        float centerLine = height / 2f;
        float startX = paddingLeft;
        float endX = width - paddingRight;
        //画背景

        canvas.drawLine(startX, centerLine, leftBarX, centerLine, bgPaint);
        canvas.drawLine(leftBarX, centerLine, rightBarX, centerLine, leftPaint);
        canvas.drawLine(rightBarX, centerLine, endX, centerLine, rightPaint);

        //画分界圆点，左侧同心圆
        float cx = leftBarX, cy = centerLine;
        innerCirclePaint.setColor(leftColor);
        outterCirclePaint.setColor(leftColor);
        canvas.drawCircle(cx, cy, innerRadius, innerCirclePaint);
        canvas.drawCircle(cx, cy, outterRadius, outterCirclePaint);

        //右侧两个圆
        float cx_rihgt = rightBarX, cy_right = cy;
        innerCirclePaint.setColor(rightColor);
        outterCirclePaint.setColor(rightColor);
        canvas.drawCircle(cx_rihgt, cy_right, innerRadius, innerCirclePaint);
        canvas.drawCircle(cx_rihgt, cy_right, outterRadius, outterCirclePaint);
        if (isSliding) {
            int textHeight = getTextHeight(textPaint);
            if (isSlideLeft) {
                float textWith = textPaint.measureText(leftPercent + "%");
                canvas.drawText(leftPercent + "%", leftBarX + innerRadius + textWith / 2f, centerLine - textHeight / 2f, textPaint);
            } else {
                float textWith = textPaint.measureText(rightPercent + "%");
                canvas.drawText(rightPercent + "%", rightBarX - innerRadius - textWith / 2f, centerLine + textHeight, textPaint);
            }
        }

    }

    private int getTextHeight(Paint paint) {
        Paint.FontMetrics forFontMetrics = paint.getFontMetrics();
        return (int) (forFontMetrics.descent - forFontMetrics.ascent);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                performClick();
                getParent().requestDisallowInterceptTouchEvent(true);
                isSliding = true;
                if (x < rightBarX) {
                    isSlideLeft = true;
                }else{
                //if (x >= rightBarX) {
                    isSlideLeft = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //Logout.e("ss", "move" + x);
                if (isSlideLeft) {
                    if (x <= paddingLeft) {
                        x = paddingLeft;
                    } else if (x >= rightBarX) {
                        x = rightBarX;
                    }
                    leftBarX = x;
                } else {
                    if (x <= leftBarX) {
                        rightBarX = leftBarX;
                    } else if (x >= getWidth() - paddingRight) {
                        rightBarX = getWidth() - paddingRight;
                    } else {
                        rightBarX = x;
                    }
                }
                //百分比
                leftPercent = mDecimalFormat.format((leftBarX - paddingLeft) *100 / barWidth*(maxPercent/100));
                rightPercent = mDecimalFormat.format((rightBarX - paddingLeft) * 100 / barWidth*(maxPercent/100));
                if (mListener != null) {
                    mListener.getProgress(leftPercent, rightPercent);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                //case MotionEvent.ACTION_CANCEL:
                isSliding = false;
                /*if (isSlideLeft) {
                    Logout.e("sss", x + "左边" + leftBarX + "左边" + rightBarX + (leftBarX < rightBarX));
                } else {
                    Logout.e("sss", x + "右边" + leftBarX + "右边" + rightBarX + (leftBarX < rightBarX));
                }*/
                invalidate();
                break;
        }

        return true;


    }

    private void initBarPercent(int width, int height) {
        barWidth = width - paddingLeft - paddingRight;
        barHeight = height - paddingTop - paddingBottom;
        if (barHeight > dpTpPx(8)) {
            barHeight = dpTpPx(8);
        }
        bgPaint.setStrokeWidth(barHeight);
        leftPaint.setStrokeWidth(barHeight);
        rightPaint.setStrokeWidth(barHeight);

        innerRadius = 3f / 4f * barHeight;
        outterRadius = innerRadius + dpTpPx(2);
        //Logout.e("初始化", barWidth + "====" + barHeight + "::::::");
    }


    public int dpTpPx(float value) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, dm) + 0.5);
    }
}
