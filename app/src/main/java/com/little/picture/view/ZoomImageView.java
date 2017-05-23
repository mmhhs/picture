package com.little.picture.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.little.picture.listener.IOnGestureListener;

public class ZoomImageView extends ImageView implements ViewTreeObserver.OnGlobalLayoutListener,
        ScaleGestureDetector.OnScaleGestureListener,View.OnTouchListener,ValueAnimator.AnimatorUpdateListener{

    //---------------init------------------
    private boolean isFirst =true;
    private float initScale;
    private final Matrix mMatrix;
    //---------------scale-----------------
    public static final float SCALE_MAX = 4.0f;
    public static final float SCALE_MIN = 0.5f;//

    private ScaleGestureDetector mScaleGestureDetector;

    private final  float[] matrixValue=new float[9];

    //--------------------------translate-----

    private  int lastPonitCount;

    private boolean isCanTran;

    private float lastX, lastY;

    private int  mSlop;

    private boolean isCheckTopAndBottom,isCheckLeftAndRight;

    //-------------------------双击-------------------------

    private GestureDetector mGestureDetector;

    private float lastScale;


    private boolean isAutoScale;

    private onMinScaleListener listener;

    private IOnGestureListener onGestureListener;

    public interface onMinScaleListener{
        void  minScale();
    }

    public void setListener(onMinScaleListener listener) {
        this.listener = listener;
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //------------init--------------
        setScaleType(ScaleType.MATRIX);
        mMatrix=new Matrix();
        mScaleGestureDetector=new ScaleGestureDetector(getContext(),this);
        mSlop= ViewConfiguration.get(getContext()).getScaledTouchSlop();

        mGestureDetector=new GestureDetector(getContext(),new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (onGestureListener != null) {
                    onGestureListener.onClick();
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                if (onGestureListener != null) {
                    onGestureListener.onLongPress();
                }
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {

                if(isAutoScale==true) return true;//缩放期间不允许在双击缩放
                lastScale=getCurScale();//获取双击前的缩放比例
                if(lastScale>=SCALE_MAX*initScale){
                    initScaleAndOnCenter();
                    return true;
                }
                lastX =e.getX();
                lastY =e.getY();
                ValueAnimator valueAnimator=ValueAnimator.ofFloat(1f,1.5f);
                valueAnimator.setDuration(2000);
                valueAnimator.addUpdateListener(ZoomImageView.this);
                valueAnimator.addListener(new AniListener());
                valueAnimator.start();
                if (onGestureListener != null) {
                    onGestureListener.onDoubleClick();
                }
                return true;
            }
        });

        setOnTouchListener(this);

    }
    public class AniListener extends AnimatorListenerAdapter {
        @Override
        public void onAnimationStart(Animator animation) {
            isAutoScale=true;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            isAutoScale=false;
        }
    }
    public ZoomImageView(Context context) {
        this(context, null);
    }
    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            getViewTreeObserver().addOnGlobalLayoutListener(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onGlobalLayout() {
        if(isFirst &&getDrawable()!=null){
            initScaleAndOnCenter();
        }
    }

    //图片缩放居中
    private void initScaleAndOnCenter() {
        int w=getWidth();
        int h=getHeight();
        Drawable drawable=getDrawable();

        int dw=drawable.getIntrinsicWidth();
        int dh=drawable.getIntrinsicHeight();

        float scale;

        if(dw>w&&dh<h){
            scale=w*1.0f/dw;
        }else if(dh>h&&dw<w){
            scale=h*1.0f/dh;
        }else{
            scale=Math.min(w*1.0f/dw,h*1.0f/dh);
        }
        //将图片缩放
        mMatrix.setScale(scale, scale, dw / 2, dh / 2);
        //将图片居中
        mMatrix.postTranslate(w/2-dw/2,h/2-dh/2);

        initScale=scale;//记录初始化的缩放比例

        setImageMatrix(mMatrix);

        isFirst =false;
    }
    //------------------------ScaleGestureDetector--------------
    @Override
    public boolean onScale(ScaleGestureDetector detector) {//缩放处理

        if(getDrawable()==null) return true;

        if(getCurScale()<=SCALE_MIN*initScale){
            if(listener!=null){
                listener.minScale();
            }
            return true;
        }

        float scale=getCurScale();
        float scaleFactor =detector.getScaleFactor();

        //缩放的范围在(1.0-5.0)
        if((scale<SCALE_MAX*initScale&&scaleFactor>1.0f)||//放大
                (scale>initScale*SCALE_MIN&&scaleFactor<1.0f))//缩小
        {//放大缩小符合要求

            if(scale*scaleFactor>SCALE_MAX*initScale){
                scaleFactor=(SCALE_MAX*initScale)/scale;
            }
            if(scale*scaleFactor<initScale*SCALE_MIN){
                scaleFactor = initScale*SCALE_MIN / scale;
            }

            mMatrix.postScale(scaleFactor,scaleFactor,detector.getFocusX(),detector.getFocusX());
            checkScale();
            setImageMatrix(mMatrix);
        }
        return true;
    }

    //缩放处理
    private void checkScale(){

        RectF rectF=getRectF();
        float left=rectF.left;
        float top=rectF.top;

        float deltaX=0;
        float deltaY=0;

        int width=getWidth();
        int height=getHeight();
        //宽高太大会出现白边 白边处理
        if(rectF.width()>=width){
            if(rectF.left>0){
                deltaX=-left;
            }
            if(rectF.right<width){
                deltaX=width-rectF.right;
            }
        }
        if(rectF.height()>=height){
            if(top>0){
                deltaY=-top;
            }
            if(rectF.bottom<height){
                deltaY=height-rectF.bottom;
            }

        }
        // 如果宽或高小于屏幕，则让其居中
        if (rectF.width() < width)
        {
            deltaX = width * 0.5f - rectF.right + 0.5f * rectF.width();
        }
        if (rectF.height() < height)
        {
            deltaY = height * 0.5f - rectF.bottom + 0.5f * rectF.height();
        }
        mMatrix.postTranslate(deltaX, deltaY);
    }

    private float getCurScale(){
        mMatrix.getValues(matrixValue);
        return matrixValue[Matrix.MSCALE_X];
    }

    private RectF getRectF(){
        Matrix matrix=mMatrix;
        RectF rectF=new RectF();
        Drawable drawable=getDrawable();
        if(drawable!=null){
            rectF.set(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }
        return rectF;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    // 获取现在的缩放比例

    //------------------------onTouch--------------------------------
    @Override
    public boolean onTouch(View v, MotionEvent event) {


        if(mGestureDetector.onTouchEvent(event)){
            return true;//双击缩放的时候，不允许手动缩放和平移
        }

        mScaleGestureDetector.onTouchEvent(event);
        float x=0,y=0;

        int pointCount=event.getPointerCount();

        for(int i=0;i<pointCount;i++){
            x+=event.getX(i);
            y+=event.getY(i);
        }

        //平移的中心
        x=x/pointCount;
        y=y/pointCount;

        if(pointCount!=lastPonitCount){
            lastX =x;
            lastY =y;
            isCanTran=false;
        }

        lastPonitCount=pointCount;

        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                float dx=x- lastX;
                float dy=y- lastY;
                if(!isCanTran){
                    isCanTran=isCanDrag(dx,dy);
                }
                if(isCanTran){//可以平移
                    RectF rectF=getRectF();
                    if(getDrawable()!=null){

                        //大于屏幕的宽高才可以平移
                        isCheckLeftAndRight = isCheckTopAndBottom = true;

                        if(rectF.width()<getWidth()){
                            dx=0;
                            isCheckLeftAndRight=false;
                        }

                        if(rectF.height()<getHeight()){
                            dy=0;
                            isCheckTopAndBottom=false;
                        }
                        mMatrix.postTranslate(dx, dy);
                        checkMatrixBounds();
                        setImageMatrix(mMatrix);
                    }
                }

                lastX =x;
                lastY =y;

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                lastPonitCount=0;
                break;
        }



        return true;
    }

    private void checkMatrixBounds()
    {
        RectF rect = getRectF();

        float deltaX = 0, deltaY = 0;
        final float viewWidth = getWidth();
        final float viewHeight = getHeight();
        // 判断移动或缩放后，图片显示是否超出屏幕边界
        if (rect.top > 0 && isCheckTopAndBottom)
        {
            deltaY = -rect.top;
        }
        if (rect.bottom < viewHeight && isCheckTopAndBottom)
        {
            deltaY = viewHeight - rect.bottom;
        }
        if (rect.left > 0 && isCheckLeftAndRight)
        {
            deltaX = -rect.left;
        }
        if (rect.right < viewWidth && isCheckLeftAndRight)
        {
            deltaX = viewWidth - rect.right;
        }
        mMatrix.postTranslate(deltaX, deltaY);
    }

    private boolean isCanDrag(float dx, float dy)
    {
        return Math.sqrt((dx * dx) + (dy * dy)) >= mSlop;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        //每次双击放大两倍，但是不超过最大缩放比例
        if(getCurScale()<2*lastScale&&getCurScale()<=SCALE_MAX*initScale){
            float x= (Float) animation.getAnimatedValue();
            mMatrix.postScale(x, x, lastX, lastY);
            checkScale();
            setImageMatrix(mMatrix);
        }else {//达到了目标值
            animation.cancel();
        }
    }

    public void setOnGestureListener(IOnGestureListener onGestureListener) {
        this.onGestureListener = onGestureListener;
    }
}
