package com.little.picture.glide;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.little.picture.R;
import com.little.picture.util.LogUtil;

public class GlideUtil {
    public static final int CENTER_CROP = 1;
    public static final int FIT_CENTER = 2;
    private static GlideUtil glideUtil;

    public static synchronized GlideUtil getInstance(){
        if (glideUtil==null){
            glideUtil = new GlideUtil();
        }
        return glideUtil;
    }

    public GlideUtil() {
    }

    public void display(Context context,String url,ImageView mImageView){
        Glide.with(context)
                .load(url)
                .listener(mRequestListener)
                .priority(Priority.LOW)
                .placeholder(R.drawable.picture_placeholder)
                .error(R.drawable.picture_placeholder)
                .centerCrop()
                .override(100, 100)
                .into(mImageView);
    }

    public void display(Context context,String url,ImageView mImageView,int width,int height){
        Glide.with(context)
                .load(url)
                .listener(mRequestListener)
                .priority(Priority.LOW)
                .placeholder(R.drawable.picture_placeholder)
                .error(R.drawable.picture_placeholder)
                .centerCrop()
                .override(width,height)
                .into(mImageView);
    }

    public void display(Context context,String url,ImageView mImageView,int scaleType,int placeholderResourceId,int errorResourceId){
        switch (scaleType){
            case CENTER_CROP:
                Glide.with(context)
                        .load(url)
                        .listener(mRequestListener)
                        .priority(Priority.LOW)
                        .placeholder(placeholderResourceId)
                        .error(errorResourceId)
                        .centerCrop()
                        .into(mImageView);
                break;
            case FIT_CENTER:
                Glide.with(context)
                        .load(url)
                        .listener(mRequestListener)
                        .priority(Priority.LOW)
                        .placeholder(placeholderResourceId)
                        .error(errorResourceId)
                        .fitCenter()
                        .into(mImageView);
                break;
        }

    }

    public void display(Context context,String url,ImageView mImageView,int scaleType,int placeholderResourceId,int errorResourceId,int width,int height){
        switch (scaleType){
            case CENTER_CROP:
                Glide.with(context)
                        .load(url)
                        .listener(mRequestListener)
                        .priority(Priority.LOW)
                        .placeholder(placeholderResourceId)
                        .error(errorResourceId)
                        .centerCrop()
                        .override(width, height)
                        .into(mImageView);
                break;
            case FIT_CENTER:
                Glide.with(context)
                        .load(url)
                        .listener(mRequestListener)
                        .priority(Priority.LOW)
                        .placeholder(placeholderResourceId)
                        .error(errorResourceId)
                        .fitCenter()
                        .override(width, height)
                        .into(mImageView);
                break;
        }

    }

    private RequestListener<String, GlideDrawable> mRequestListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            //显示错误信息
            LogUtil.e("onException: " + e.getMessage());
            //打印请求URL
            LogUtil.e("onException: " + model);
            //打印请求是否还在进行
            LogUtil.e("onException: " + target.getRequest().isRunning());
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            return false;
        }
    };

}
