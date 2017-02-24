package com.little.picture.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * 图片工具类
 * -获取屏幕宽度、获取屏幕高度
 * -dp转换为px、px转换为dp
 * -获取sd卡路径、判断SD卡是否存在
 * -缩放图片、将Bitmap转换成指定大小
 * -view转bitmap、Drawable 转 Bitmap、Bitmap 转 Drawable、byte[] 转 bitmap、bitmap 转 byte[]
 * -保存图片为JPEG、读取路径中的图片，然后将其转化为缩放后的bitmap
 * -计算缩放倍数
 */
public class ImageUtil {

    /**
     * 获取屏幕宽度px
     * @param activity
     * @return
     */
    public static int getScreenWidth(Activity activity){
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        return screenWidth;
    }

    /**
     * 获取屏幕高度px
     * @param activity
     * @return
     */
    public static int getScreenHeight(Activity activity){
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        return screenHeight;
    }

    /**
     * 获取状态栏高度
     * @param activity
     * @return
     */
    public static int getStatusBarHeight(Activity activity){
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 38;//默认为38，貌似大部分是这样的

        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = activity.getResources().getDimensionPixelSize(x);

        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }

    /**
     * dp转换为px
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px转换为dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 获取sd卡路径
     * @return
     */
    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
        }else {
            return "";
        }
        return sdDir.toString();
    }

    /**
     * 判断SD卡是否存在
     *
     * @return
     */
    public static Boolean sdCardExist() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 缩放图片
     * @param bigImage 源图片资源
     * @param newWidth 缩放后宽度
     * @param newHeight 缩放后高度
     * @return
     */
    public static Bitmap zoomImage(Bitmap bigImage, double newWidth,
                                   double newHeight) {
        // 获取这个图片的宽和高
        float width = bigImage.getWidth();
        float height = bigImage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bigImage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }

    /**
     * view转bitmap
     * @param view
     * @return
     */
    public static Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = null;
        try {
            int width = view.getWidth();
            int height = view.getHeight();
            if (width != 0 && height != 0) {
                bitmap = Bitmap.createBitmap(width, height,
                        Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                view.layout(0, 0, width, height);
                view.draw(canvas);
            }
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }
        return bitmap;
    }

    /**
     * 将Bitmap转换成指定大小
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    public static Bitmap createBitmapBySize(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    /**
     * Drawable 转 Bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmapByBD(Drawable drawable) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        return bitmapDrawable.getBitmap();
    }

    /**
     * Bitmap 转 Drawable
     *
     * @param bitmap
     * @return
     */
    public static Drawable bitmapToDrawableByBD(Bitmap bitmap) {
        Drawable drawable = new BitmapDrawable(bitmap);
        return drawable;
    }

    /**
     * byte[] 转 bitmap
     *
     * @param b
     * @return
     */
    public static Bitmap bytesToBimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }


    /**
     * bitmap 转 byte[]
     *
     * @param bm
     * @return
     */
    public static byte[] bitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 保存图片为JPEG
     *
     * @param bitmap
     * @param path
     */
    public static Boolean saveJPGE_After(Bitmap bitmap, int arg,String path) {
        boolean result = false;
        try {
            File photoFile = new File(path);
            if(!photoFile.exists()){
                try {
                    photoFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                FileOutputStream out = new FileOutputStream(photoFile);
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, arg, out)) {
                    out.flush();
                    out.close();
                }
            } catch (FileNotFoundException e) {
                photoFile.delete();
                e.printStackTrace();
                result =  false;
            } catch (IOException e) {
                photoFile.delete();
                e.printStackTrace();
                result =  false;
            }catch (Exception e){
                e.printStackTrace();
                result =   false;
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            return result;
        }
    }

    /**
     * 读取路径中的图片，然后将其转化为缩放后的bitmap
     *
     * @param imagePath
     */
    /**
     * 读取路径中的图片，然后将其缩放
     * @param imagePath 源图片路径
     * @param imagePathFolder 存储文件夹
     * @param scaleWidth 缩放至的宽度
     * @param scaleHeight 缩放至的高度
     * @param quality 图片质量 1-100
     * @return
     */
    public static String saveScaleImage(String imagePath,String imagePathFolder,int scaleWidth,int scaleHeight,int quality) {
        String result = "";
        try {
            String fileSavePath ="";
            long time = System.currentTimeMillis();
            if(!sdCardExist()||imagePath.equals("")){
                return "";
            }
            fileSavePath = imagePathFolder +time+".jpg";
            File folder = new File(imagePathFolder);
            if(!folder.exists()){
                folder.mkdirs();
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options); // 此时返回bm为空
            // 获取这个图片的宽和高
            int width = options.outWidth;
            int height = options.outHeight;
            options.inJustDecodeBounds = false;
            // 计算缩放比
            int be = caculateInSampleSize(width,height,scaleWidth,scaleHeight);
            options.inSampleSize =be;
            try{
                bitmap = BitmapFactory.decodeFile(imagePath, options);
            }catch(Exception e){
                e.printStackTrace();
                return "";
            }catch(OutOfMemoryError error){
                error.printStackTrace();
                return "";
            }
            if(bitmap==null){
                return "";
            }
            if (quality<1){
                quality = 1;
            }
            if (quality>100){
                quality = 100;
            }
            boolean saveResult = saveJPGE_After(bitmap,quality, fileSavePath);
            if(!bitmap.isRecycled()){
                bitmap.recycle();
            }
            bitmap=null;
            if(saveResult){

                result = fileSavePath;
            }else{

                result = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            return result;
        }


    }

    /**
     * 计算缩放倍数
     * @param width 当前图像宽度
     * @param height 当前图像高度
     * @param scaleWidth 缩放至的宽度
     * @param scaleHeight 缩放至的高度
     * @return
     */
    public static int caculateInSampleSize(int width,int height,int scaleWidth,int scaleHeight){
        int be = 1;//be=1表示不缩放
        try {
            if (width > height && height > scaleWidth) {//如果宽度大的话根据宽度固定大小缩放
                be = (int) (width / scaleWidth);
            } else if (width < height && height > scaleHeight) {//如果高度高的话根据宽度固定大小缩放
                be = (int) (height / scaleHeight);
            }
            if(be<=1){
                be = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            return be;
        }
    }

    /**
     * 预处理图片路径 补全图片路径
     * @param imagePath 图片路径
     * @return
     */
    public static String completeImagePath(String imagePath){
        if (StringUtil.isEmpty(imagePath)){
            return "";
        }else {
            if (imagePath.startsWith("http")){
                return imagePath;
            }else{
                return "file://"+imagePath;
            }
        }
    }

    /**
     * 预处理图片路径 补全图片路径
     * @param imagePath 图片路径
     * @param prefix 域名前缀
     * @return
     */
    public static String completeImagePath(String imagePath,String prefix){
        if (StringUtil.isEmpty(imagePath)){
            return "";
        }else {
            if (imagePath.startsWith("http")){
                return imagePath;
            }else{
                return prefix+imagePath;
            }
        }
    }
}
