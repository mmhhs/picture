package com.little.picture.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.little.picture.R;


/**
 * 页面指示器
 */
public class PageIndicatorView extends View {
    private final int LEFT = 1;//居左
    private final int CENTER = 2;//居中
    private final int RIGHT = 3;//居右
    private final int DefaultRadius = 10;//默认半径
    private final int DefaultSelectLength = 10;//默认选中项中间长度
    private final int DefaultSpace = 30;//默认间隔
    private int indicatorRadius; // 指示点半径
    private int indicatorSelectLength; // 指示点选中项中间长度
    private int indicatorDefaultColor = Color.WHITE; // 指示点默认颜色
    private int indicatorSelectColor = Color.RED; // 指示点选中颜色
    private int indicatorSpace; // 指示点间隔
    private int indicatorGravity; // 内容位置 ,LEFT:1,CENTER:2,RIGHT:3

    private int pageTotal = 0; // 页面总数
    private int pageSelect = 0; // 当前页面索引

    private Paint mDefalutPaint,mSelectPaint;
    private Context mContext;

    public PageIndicatorView(Context context) {
        super(context);
        mContext = context;
        init(null, 0);
    }

    public PageIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs, 0);
    }

    public PageIndicatorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.PageIndicatorView, defStyle, 0);

        indicatorRadius = a.getDimensionPixelSize(R.styleable.PageIndicatorView_indicatorRadius, DefaultRadius);
        indicatorSelectLength = a.getDimensionPixelSize(R.styleable.PageIndicatorView_indicatorSelectLength, DefaultSelectLength);
        indicatorDefaultColor = a.getColor(R.styleable.PageIndicatorView_indicatorDefaultColor, Color.WHITE);
        indicatorSelectColor = a.getColor(R.styleable.PageIndicatorView_indicatorDefaultColor, Color.RED);
        indicatorSpace = a.getDimensionPixelSize(R.styleable.PageIndicatorView_indicatorSelectLength, DefaultSpace);
        indicatorGravity = a.getInteger(R.styleable.PageIndicatorView_indicatorGravity, CENTER);

        a.recycle();

        // Set up Paint object
        mDefalutPaint = new Paint();
        // 设置画笔为抗锯齿
        mDefalutPaint.setAntiAlias(true);
        // 设置颜色为红色
        mDefalutPaint.setColor(indicatorDefaultColor);
        mDefalutPaint.setStyle(Paint.Style.FILL);

        mSelectPaint = new Paint();
        // 设置画笔为抗锯齿
        mSelectPaint.setAntiAlias(true);
        // 设置颜色为红色
        mSelectPaint.setColor(indicatorSelectColor);
        mSelectPaint.setStyle(Paint.Style.FILL);

    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();

        // Draw the dot.
        int startPositionX = 0;
        int startPositionY = 0;
        int indicatorTotalLength = pageTotal*indicatorRadius*2+indicatorSelectLength+(pageTotal-1)*indicatorSpace;
        if (indicatorGravity == LEFT){
            startPositionX = paddingLeft;
        }else if (indicatorGravity == CENTER){
            startPositionX = (getWidth() - indicatorTotalLength)/2;
        }else if (indicatorGravity == RIGHT){
            startPositionX = paddingRight + indicatorTotalLength;
        }
        startPositionY = (getHeight() - indicatorRadius*2)/2;
        if (pageTotal>1){
            for (int i=0;i<pageTotal;i++){
                if (i==pageSelect){
                    //选中的
                    float x = startPositionX;
                    float y = startPositionY;

                    RectF ovalLeft = new RectF( x, y,x + 2*indicatorRadius, y + 2*indicatorRadius);
                    canvas.drawArc(ovalLeft,90,180,true,mSelectPaint);
                    if (indicatorSelectLength>0)
                        canvas.drawRect(x + indicatorRadius, y, x + indicatorRadius + indicatorSelectLength, y + 2*indicatorRadius, mSelectPaint);
//
                    RectF ovalRight = new RectF( x + indicatorSelectLength, y,x + 2*indicatorRadius + indicatorSelectLength, y + 2*indicatorRadius);
                    canvas.drawArc(ovalRight,270,180,true,mSelectPaint);
                    startPositionX = startPositionX + 2*indicatorRadius + indicatorSelectLength + indicatorSpace;
                }else {
                    canvas.drawCircle(startPositionX + indicatorRadius, startPositionY + indicatorRadius, indicatorRadius, mDefalutPaint);
                    startPositionX = startPositionX + 2*indicatorRadius + indicatorSpace;
                }
            }
        }


    }

    public void setPageTotal(int pageTotal) {
        this.pageTotal = pageTotal;
    }

    public void setPageSelect(int pageSelect) {
        if (pageSelect < 0 || pageSelect >= pageTotal)
            return;

        if (pageSelect != this.pageSelect) {
            this.pageSelect = pageSelect;
            this.invalidate();
        }
    }

    //    /**
//     * @see android.view.View#measure(int, int)
//     */
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        setMeasuredDimension(measureWidth(widthMeasureSpec),
//                measureHeight(heightMeasureSpec));
//    }
//
//    /**
//     * Determines the width of this view
//     * @param measureSpec A measureSpec packed into an int
//     * @return The width of the view, honoring constraints from measureSpec
//     */
//    private int measureWidth(int measureSpec) {
//        int result = 0;
//        int specMode = MeasureSpec.getMode(measureSpec);
//        int specSize = MeasureSpec.getSize(measureSpec);
//
//        if (specMode == MeasureSpec.EXACTLY) {
//            // We were told how big to be
//            result = specSize;
//        } else {
//            // Measure the view
//
//        }
//
//        return result;
//    }
//
//    /**
//     * Determines the height of this view
//     * @param measureSpec A measureSpec packed into an int
//     * @return The height of the view, honoring constraints from measureSpec
//     */
//    private int measureHeight(int measureSpec) {
//        int result = 0;
//        int specMode = MeasureSpec.getMode(measureSpec);
//        int specSize = MeasureSpec.getSize(measureSpec);
//
//
//        if (specMode == MeasureSpec.EXACTLY) {
//            // We were told how big to be
//            result = specSize;
//        } else {
//            // Measure the view
//
//        }
//        return result;
//    }
}
