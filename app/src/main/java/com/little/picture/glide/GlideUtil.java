package com.little.picture.glide;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
            picturePlaceholderId = R.mipmap.picture_placeholder;
        }
    }

    /**
     * 加载图片
     * @param context
     * @param url
     * @param mImageView
     */
    public void display(Context context,String url,ImageView mImageView){
        try {
            //先加载原图的十分之一作为缩略图，再加载原图
//            Glide.with( context ).load( url ).thumbnail(0.1f).into( mImageView ) ;
            Glide.with(context)
                    .load(""+url).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(mRequestListener)
                    .priority(Priority.NORMAL)
                    .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                    .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                    .into(mImageView);
        }catch (Exception e){
            e.printStackTrace();
        }

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
        try {
            Glide.with(context)
                    .load(""+url).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(mRequestListener)
                    .priority(Priority.NORMAL)
                    .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                    .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                    .override(width,height)
                    .into(mImageView);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 加载图片 指定缩放类型
     * @param context
     * @param url
     * @param mImageView
     * @param scaleType
     */
    public void display(Context context,String url,ImageView mImageView,int scaleType){
        try {
            switch (scaleType){
                case CENTER_CROP:
                    Glide.with(context)
                            .load(""+url).diskCacheStrategy(DiskCacheStrategy.ALL)
                            .listener(mRequestListener)
                            .priority(Priority.NORMAL)
                            .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                            .centerCrop()
                            .into(mImageView);
                    break;
                case FIT_CENTER:
                    Glide.with(context)
                            .load(""+url).diskCacheStrategy(DiskCacheStrategy.ALL)
                            .listener(mRequestListener)
                            .priority(Priority.NORMAL)
                            .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                            .fitCenter()
                            .into(mImageView);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
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
        try {
            switch (scaleType){
                case CENTER_CROP:
                    Glide.with(context)
                            .load(""+url).diskCacheStrategy(DiskCacheStrategy.ALL)
                            .listener(mRequestListener)
                            .priority(Priority.NORMAL)
                            .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                            .centerCrop()
                            .override(width, height)
                            .into(mImageView);
                    break;
                case FIT_CENTER:
                    Glide.with(context)
                            .load(""+url).diskCacheStrategy(DiskCacheStrategy.ALL)
                            .listener(mRequestListener)
                            .priority(Priority.NORMAL)
                            .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                            .fitCenter()
                            .override(width, height)
                            .into(mImageView);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
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
        try {
            switch (scaleType){
                case CENTER_CROP:
                    Glide.with(context)
                            .load(""+url).diskCacheStrategy(DiskCacheStrategy.ALL)
                            .listener(mRequestListener)
                            .priority(Priority.NORMAL)
                            .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                            .centerCrop()
                            .transform(new GlideRoundTransform(context,radius))
                            .into(mImageView);
                    break;
                case FIT_CENTER:
                    Glide.with(context)
                            .load(""+url).diskCacheStrategy(DiskCacheStrategy.ALL)
                            .listener(mRequestListener)
                            .priority(Priority.NORMAL)
                            .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                            .fitCenter()
                            .transform(new GlideRoundTransform(context,radius))
                            .into(mImageView);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    /**
     * 加载圆形图像
     * @param context
     * @param url
     * @param mImageView
     */
    public void displayCircle(Context context,String url,ImageView mImageView){
        try {
            Glide.with(context)
                    .load(""+url).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(mRequestListener)
                    .priority(Priority.NORMAL)
//                .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
//                .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                    .centerCrop()
                    .transform(new GlideCircleTransform(context))
                    .into(mImageView);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    /**
     * 加载资源图片 指定缩放类型
     * @param context
     * @param mImageView
     * @param scaleType
     * @param resId 资源id
     */
    public void displayById(Context context,int resId,ImageView mImageView,int scaleType){
        try {
            switch (scaleType){
                case CENTER_CROP:
                    Glide.with(context)
                            .load(resId).diskCacheStrategy(DiskCacheStrategy.ALL)
                            .priority(Priority.NORMAL)
                            .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                            .centerCrop()
                            .into(mImageView);
                    break;
                case FIT_CENTER:
                    Glide.with(context)
                            .load(resId).diskCacheStrategy(DiskCacheStrategy.ALL)
                            .priority(Priority.NORMAL)
                            .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                            .fitCenter()
                            .into(mImageView);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    /**
     * 加载资源图片 圆形
     * @param context
     * @param mImageView
     * @param resId 资源id
     */
    public void displayCircleById(Context context,int resId,ImageView mImageView){
        try {
            Glide.with(context)
                    .load(resId).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.NORMAL)
//                .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
//                .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                    .centerCrop()
                    .transform(new GlideCircleTransform(context))
                    .into(mImageView);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 加载资源图片 圆形
     * @param context
     * @param mImageView
     * @param resId 资源id
     */
    public void displayFilletById(Context context,int resId,ImageView mImageView,int radius){
        try {
            Glide.with(context)
                    .load(resId).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.NORMAL)
                    .placeholder(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                    .error(new GlidePlaceholderDrawables(context.getResources(), picturePlaceholderId))
                    .centerCrop()
                    .transform(new GlideRoundTransform(context,radius))
                    .into(mImageView);
        }catch (Exception e){
            e.printStackTrace();
        }


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

    /**
     * 清除内存缓存.
     */
    public static void clearMemoryCache(Context context){
        // This method must be called on the main thread.
        Glide.get(context).clearMemory();
    }

    /**
     * 清除磁盘缓存.
     */
    public static void clearDiskCache(final Context context){

        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                // This method must be called on a background thread.
                Glide.get(context).clearDiskCache();
                return null;
            }
        };
    }

}
