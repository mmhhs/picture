package com.little.picture.glide;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fos.fosmvp.common.utils.LogUtils;
import com.little.picture.PictureStartManager;
import com.little.picture.R;

/**
 * 图片加载
 */
public class GlideUtil {
    public static final int CENTER_CROP = 1;
    public static final int FIT_CENTER = 2;
    private static GlideUtil glideUtil;
    private static int picturePlaceholderId;

    public static synchronized GlideUtil getInstance(){
        if (glideUtil==null){
            glideUtil = new GlideUtil();
        }
        return glideUtil;
    }

    public GlideUtil() {
        picturePlaceholderId = PictureStartManager.picturePlaceholderId;
        if (picturePlaceholderId<=0){
            picturePlaceholderId = R.drawable.picture_placeholder;
        }
    }

    /**
     * 加载图片
     * @param context
     * @param url
     * @param mImageView
     */
    public void display(Context context,String url,ImageView mImageView){
        Glide.with(context)
                .load(url)
                .listener(mRequestListener)
                .priority(Priority.LOW)
                .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                .into(mImageView);
    }

    /**
     * 加载图片 指定宽高
     * @param context
     * @param url
     * @param mImageView
     * @param width
     * @param height
     */
    public void display(Context context,String url,ImageView mImageView,int width,int height){
        Glide.with(context)
                .load(url)
                .listener(mRequestListener)
                .priority(Priority.LOW)
                .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                .override(width,height)
                .into(mImageView);
    }

    /**
     * 加载图片 指定缩放类型
     * @param context
     * @param url
     * @param mImageView
     * @param scaleType
     */
    public void display(Context context,String url,ImageView mImageView,int scaleType){
        switch (scaleType){
            case CENTER_CROP:
                Glide.with(context)
                        .load(url)
                        .listener(mRequestListener)
                        .priority(Priority.LOW)
                        .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                        .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                        .centerCrop()
                        .into(mImageView);
                break;
            case FIT_CENTER:
                Glide.with(context)
                        .load(url)
                        .listener(mRequestListener)
                        .priority(Priority.LOW)
                        .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                        .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                        .fitCenter()
                        .into(mImageView);
                break;
        }

    }

    /**
     * 加载图片 指定缩放类型 指定宽高
     * @param context
     * @param url
     * @param mImageView
     * @param scaleType
     * @param width
     * @param height
     */
    public void display(Context context,String url,ImageView mImageView,int scaleType,int width,int height){
        switch (scaleType){
            case CENTER_CROP:
                Glide.with(context)
                        .load(url)
                        .listener(mRequestListener)
                        .priority(Priority.LOW)
                        .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                        .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                        .centerCrop()
                        .override(width, height)
                        .into(mImageView);
                break;
            case FIT_CENTER:
                Glide.with(context)
                        .load(url)
                        .listener(mRequestListener)
                        .priority(Priority.LOW)
                        .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                        .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                        .fitCenter()
                        .override(width, height)
                        .into(mImageView);
                break;
        }

    }

    /**
     * 加载圆角图片 指定缩放类型
     * @param context
     * @param url
     * @param mImageView
     * @param scaleType
     * @param radius 圆角大小
     */
    public void displayFillet(Context context,String url,ImageView mImageView,int scaleType,int radius){
        switch (scaleType){
            case CENTER_CROP:
                Glide.with(context)
                        .load(url)
                        .listener(mRequestListener)
                        .priority(Priority.LOW)
                        .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                        .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                        .centerCrop()
                        .transform(new GlideRoundTransform(context,radius))
                        .into(mImageView);
                break;
            case FIT_CENTER:
                Glide.with(context)
                        .load(url)
                        .listener(mRequestListener)
                        .priority(Priority.LOW)
                        .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                        .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                        .fitCenter()
                        .transform(new GlideRoundTransform(context,radius))
                        .into(mImageView);
                break;
        }

    }

    /**
     * 加载圆形图像
     * @param context
     * @param url
     * @param mImageView
     */
    public void displayCircle(Context context,String url,ImageView mImageView){
        Glide.with(context)
                .load(url)
                .listener(mRequestListener)
                .priority(Priority.LOW)
                .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                .centerCrop()
                .transform(new GlideCircleTransform(context))
                .into(mImageView);
    }


    /**
     * 加载资源图片 指定缩放类型
     * @param context
     * @param mImageView
     * @param scaleType
     * @param resId 资源id
     */
    public void displayById(Context context,int resId,ImageView mImageView,int scaleType){
        switch (scaleType){
            case CENTER_CROP:
                Glide.with(context)
                        .load(resId)
                        .priority(Priority.LOW)
                        .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                        .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                        .centerCrop()
                        .into(mImageView);
                break;
            case FIT_CENTER:
                Glide.with(context)
                        .load(resId)
                        .priority(Priority.LOW)
                        .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                        .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                        .fitCenter()
                        .into(mImageView);
                break;
        }

    }

    /**
     * 加载资源图片 圆形
     * @param context
     * @param mImageView
     * @param resId 资源id
     */
    public void displayCircleById(Context context,int resId,ImageView mImageView){
        Glide.with(context)
                .load(resId)
                .priority(Priority.LOW)
                .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                .centerCrop()
                .transform(new GlideCircleTransform(context))
                .into(mImageView);
    }

    /**
     * 加载资源图片 圆形
     * @param context
     * @param mImageView
     * @param resId 资源id
     */
    public void displayFilletById(Context context,int resId,ImageView mImageView,int radius){
        Glide.with(context)
                .load(resId)
                .priority(Priority.LOW)
                .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                .centerCrop()
                .transform(new GlideRoundTransform(context,radius))
                .into(mImageView);

    }


    private RequestListener<String, GlideDrawable> mRequestListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            //显示错误信息
            LogUtils.e("onException: " + e.getMessage());
            //打印请求URL
            LogUtils.e("onException: " + model);
            //打印请求是否还在进行
            LogUtils.e("onException: " + target.getRequest().isRunning());
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            return false;
        }
    };

}
