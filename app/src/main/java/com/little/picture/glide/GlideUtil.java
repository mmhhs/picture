package com.little.picture.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.little.picture.PictureStartManager;
import com.little.picture.R;

import static com.little.picture.PictureStartManager.CENTER_CROP;
import static com.little.picture.PictureStartManager.CENTER_INSIDE;
import static com.little.picture.PictureStartManager.FIT_CENTER;

/**
 * 图片加载
 */
public class GlideUtil {
    private static GlideUtil glideUtil;
    private static int picturePlaceholderId;
    private RequestOptions options1,options2,options3,options4,options5;

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
        options1 = new RequestOptions()
                .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                .error(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                .priority(Priority.NORMAL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true);
        options2 = new RequestOptions()
                .priority(Priority.NORMAL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true);
        options3 = new RequestOptions()
                .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                .error(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                .priority(Priority.NORMAL)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true);
        options4 = new RequestOptions()
                .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                .error(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                .priority(Priority.NORMAL)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true);
        options5 = new RequestOptions()
                .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                .error( new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                .priority(Priority.NORMAL)
                .centerInside()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true);
    }

    /**
     * 加载图片
     * @param context 上下文
     * @param url 路径
     * @param mImageView 控件
     */
    public void display(Context context,String url,ImageView mImageView){
        try {
//            RequestBuilder<Drawable> requestBuilder = Glide.with(context)
//                    .load(url);
//            requestBuilder
//                    .listener(mRequestListener)
//                    .apply(options1)
//                    .load(url)
//                    .into(mImageView);
            Glide.with(context)
                    .load(""+url)
                    .listener(mRequestListener)
                    .apply(options1)
                    .into(mImageView);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 加载图片
     * @param context 上下文
     * @param url 路径
     * @param mImageView 控件
     */
    public void displayNoPlaceholder(Context context,String url,ImageView mImageView){
        try {
            Glide.with(context)
                    .load(""+url)
                    .listener(mRequestListener)
                    .apply(options2)
                    .into(mImageView);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 加载图片 指定宽高
     * @param context 上下文
     * @param url 路径
     * @param mImageView 控件
     * @param width 宽度
     * @param height 长度
     */
    public void display(Context context,String url,ImageView mImageView,int width,int height){
        try {
            RequestOptions options = new RequestOptions()
                    .priority(Priority.NORMAL)
                    .override(width,height)
                    .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                    .error(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(true);
            Glide.with(context)
                    .load(""+url)
                    .listener(mRequestListener)
                    .apply(options)
                    .into(mImageView);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 加载图片 指定缩放类型
     * @param context 上下文
     * @param url 路径
     * @param mImageView 控件
     * @param scaleType 缩放类型
     */
    public void display(Context context,String url,ImageView mImageView,int scaleType){
        try {
            switch (scaleType){
                case CENTER_CROP:
                    Glide.with(context)
                            .load(""+url)
                            .listener(mRequestListener)
                            .apply(options3)
                            .into(mImageView);
                    break;
                case FIT_CENTER:
                    Glide.with(context)
                            .load(""+url)
                            .listener(mRequestListener)
                            .apply(options4)
                            .into(mImageView);
                    break;
                case CENTER_INSIDE:
                    Glide.with(context)
                            .load(""+url)
                            .listener(mRequestListener)
                            .apply(options5)
                            .into(mImageView);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 加载图片 指定缩放类型
     * @param context 上下文
     * @param url 路径
     * @param mImageView 控件
     * @param scaleType 缩放类型
     */
    public void displayNoPlaceholder(Context context,String url,ImageView mImageView,int scaleType){
        try {
            switch (scaleType){
                case CENTER_CROP:
                    Glide.with(context)
                            .load(""+url)
                            .listener(mRequestListener)
                            .apply(options3)
                            .into(mImageView);
                    break;
                case FIT_CENTER:
                    Glide.with(context)
                            .load(""+url)
                            .listener(mRequestListener)
                            .apply(options4)
                            .into(mImageView);
                    break;
                case CENTER_INSIDE:
                    Glide.with(context)
                            .load(""+url)
                            .listener(mRequestListener)
                            .apply(options5)
                            .into(mImageView);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 加载图片 指定缩放类型 指定宽高
     * @param context 上下文
     * @param url 路径
     * @param mImageView 控件
     * @param scaleType 缩放类型
     * @param width 宽度
     * @param height 长度
     */
    public void display(Context context,String url,ImageView mImageView,int scaleType,int width,int height){
        try {
            RequestOptions options = null;
            switch (scaleType){
                case CENTER_CROP:
                    options = new RequestOptions()
                            .priority(Priority.NORMAL)
                            .override(width,height)
                            .centerCrop()
                            .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(true);
                    break;
                case FIT_CENTER:
                    options = new RequestOptions()
                            .priority(Priority.NORMAL)
                            .override(width,height)
                            .fitCenter()
                            .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(true);
                    break;
                case CENTER_INSIDE:
                    options = new RequestOptions()
                            .priority(Priority.NORMAL)
                            .override(width,height)
                            .centerInside()
                            .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(true);
                    break;
            }
            Glide.with(context)
                    .load(""+url)
                    .listener(mRequestListener)
                    .apply(options)
                    .into(mImageView);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void display(Context context,String url,ImageView mImageView,int scaleType,int width,int height,int picturePlaceholderId){
        try {
            RequestOptions options = null;
            switch (scaleType){
                case CENTER_CROP:
                    options = new RequestOptions()
                            .priority(Priority.NORMAL)
                            .override(width,height)
                            .centerCrop()
                            .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(true);
                    break;
                case FIT_CENTER:
                    options = new RequestOptions()
                            .priority(Priority.NORMAL)
                            .override(width,height)
                            .fitCenter()
                            .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(true);
                    break;
                case CENTER_INSIDE:
                    options = new RequestOptions()
                            .priority(Priority.NORMAL)
                            .override(width,height)
                            .centerInside()
                            .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(true);
                    break;
            }
            Glide.with(context)
                    .load(""+url)
                    .listener(mRequestListener)
                    .apply(options)
                    .into(mImageView);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 加载圆角图片 指定缩放类型
     * @param context 上下文
     * @param url 路径
     * @param mImageView 控件
     * @param scaleType 缩放类型
     * @param radius 圆角大小
     */
    public void displayFillet(Context context,String url,ImageView mImageView,int scaleType,int radius){
        displayFillet(context,url,mImageView,scaleType,radius,picturePlaceholderId);
    }

    /**
     * 加载圆角图片 指定缩放类型
     * @param context 上下文
     * @param url 路径
     * @param mImageView 控件
     * @param scaleType 缩放类型
     * @param radius 圆角大小
     */
    public void displayFillet(Context context,String url,ImageView mImageView,int scaleType,int radius,int picturePlaceholderId){
        try {
            RequestOptions options = null;
            switch (scaleType){
                case CENTER_CROP:
                    options = new RequestOptions()
                            .priority(Priority.NORMAL)
                            .transform(new GlideRoundTransform(radius,0))
                            .centerCrop()
                            .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(true);
                    break;
                case FIT_CENTER:
                    options = new RequestOptions()
                            .priority(Priority.NORMAL)
                            .transform(new GlideRoundTransform(radius,0))
                            .fitCenter()
                            .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(true);
                    break;
                case CENTER_INSIDE:
                    options = new RequestOptions()
                            .priority(Priority.NORMAL)
                            .transform(new GlideRoundTransform(radius,0))
                            .centerInside()
                            .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .error(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(true);
                    break;
            }
            Glide.with(context)
                    .load(""+url)
                    .listener(mRequestListener)
                    .apply(options)
                    .into(mImageView);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 加载圆形图像
     * @param context 上下文
     * @param url 路径
     * @param mImageView 控件
     */
    public void displayCircle(Context context,String url,ImageView mImageView){
        try {
            displayCircle(context,url,mImageView,picturePlaceholderId);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 加载圆形图像
     * @param context 上下文
     * @param url 路径
     * @param mImageView 控件
     */
    public void displayCircle(Context context,String url,ImageView mImageView,int picturePlaceholderId){
        try {
            RequestOptions options = new RequestOptions()
                    .priority(Priority.NORMAL)
                    .transform(new GlideCircleTransform())
                    .centerCrop()
                    .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                    .error(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(true);
            Glide.with(context)
                    .load(""+url)
                    .listener(mRequestListener)
                    .apply(options)
                    .into(mImageView);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    /**
     * 加载资源图片 指定缩放类型
     * @param context 上下文
     * @param mImageView 控件
     * @param scaleType 缩放类型
     * @param resId 资源id
     */
    public void displayById(Context context,int resId,ImageView mImageView,int scaleType){
        try {

            displayById(context,resId,mImageView,scaleType,picturePlaceholderId);
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public void displayById(Context context,int resId,ImageView mImageView,int scaleType,int picturePlaceholderId){
        try {
            switch (scaleType){
                case CENTER_CROP:
                    Glide.with(context)
                            .load(resId)
                            .listener(mRequestListener)
                            .apply(options3)
                            .into(mImageView);
                    break;
                case FIT_CENTER:
                    Glide.with(context)
                            .load(resId)
                            .listener(mRequestListener)
                            .apply(options4)
                            .into(mImageView);
                    break;
                case CENTER_INSIDE:
                    Glide.with(context)
                            .load(resId)
                            .listener(mRequestListener)
                            .apply(options5)
                            .into(mImageView);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    /**
     * 加载资源图片 圆形
     * @param context 上下文
     * @param mImageView 控件
     * @param resId 资源id
     */
    public void displayCircleById(Context context,int resId,ImageView mImageView){
        try {
            displayCircleById(context,resId,mImageView,picturePlaceholderId);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void displayCircleById(Context context,int resId,ImageView mImageView,int picturePlaceholderId){
        try {
            RequestOptions options = new RequestOptions()
                    .priority(Priority.NORMAL)
                    .transform(new GlideCircleTransform())
                    .centerCrop()
                    .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                    .error(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(true);
            Glide.with(context)
                    .load(resId)
                    .listener(mRequestListener)
                    .apply(options)
                    .into(mImageView);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 加载资源图片 圆形
     * @param context 上下文
     * @param mImageView 控件
     * @param resId 资源id
     */
    public void displayFilletById(Context context,int resId,ImageView mImageView,int radius){
        try {
            displayFilletById(context,resId,mImageView,radius,picturePlaceholderId);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void displayFilletById(Context context,int resId,ImageView mImageView,int radius,int picturePlaceholderId){
        try {
            RequestOptions options = new RequestOptions()
                    .priority(Priority.NORMAL)
                    .transform(new GlideRoundTransform(radius,0))
                    .centerCrop()
                    .placeholder(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                    .error(new GlidePlaceholderDrawables(PictureStartManager.context.getResources(), picturePlaceholderId))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(true);
            Glide.with(context)
                    .load(resId)
                    .listener(mRequestListener)
                    .apply(options)
                    .into(mImageView);
        }catch (Exception e){
            e.printStackTrace();
        }


    }


    RequestListener mRequestListener = new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
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
