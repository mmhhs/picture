package com.little.picture;

import android.content.Context;

/**
 * 图片管理
 */

public class PictureStartManager {
    private static PictureStartManager instance;
    public static Context context;
    public static int SCALE_WIDTH = 1080;//缩放至的宽度
    public static int SCALE_HEIGHT = 1080;//缩放至的高度
    public static int QUALITY = 100;//图像质量
    public static String AUTHORITY = "com.foton.almighty.fileprovider";//7.0以上调取相机相册需要
    public static String IMAGE_FOLDER = "";//存储图片的文件夹
    public static int picturePlaceholderId = 0;//图片加载占位图

    public static final int FIT_CENTER = 1;
    public static final int CENTER_CROP = 2;
    public static final int CENTER_INSIDE = 3;

    public PictureStartManager(Context context) {
        this.context = context;
    }

    public static PictureStartManager getInstance(Context context) {
        if (instance == null) {
            synchronized (PictureStartManager.class) {
                if (instance == null) {
                    instance = new PictureStartManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * 配置图片选择控件参数
     * @param scaleWidth 缩放至的宽度
     * @param scaleHeight 缩放至的高度
     * @param quality 压缩图像质量
     * @param authority 7.0以上调取相机相册需要权限
     * @param imageFolder 存储图片的文件夹
     */
    public void init(int scaleWidth, int scaleHeight, int quality, String authority, String imageFolder) {
        this.SCALE_WIDTH = scaleWidth;
        this.SCALE_HEIGHT = scaleHeight;
        this.QUALITY = quality;
        this.AUTHORITY = authority;
        this.IMAGE_FOLDER = imageFolder;
    }

    public static int getScaleWidth() {
        return SCALE_WIDTH;
    }

    public static void setScaleWidth(int scaleWidth) {
        SCALE_WIDTH = scaleWidth;
    }

    public static int getScaleHeight() {
        return SCALE_HEIGHT;
    }

    public static void setScaleHeight(int scaleHeight) {
        SCALE_HEIGHT = scaleHeight;
    }

    public static int getQUALITY() {
        return QUALITY;
    }

    public static void setQUALITY(int QUALITY) {
        PictureStartManager.QUALITY = QUALITY;
    }

    public static String getAUTHORITY() {
        return AUTHORITY;
    }

    public static void setAUTHORITY(String AUTHORITY) {
        PictureStartManager.AUTHORITY = AUTHORITY;
    }

    public static String getImageFolder() {
        return IMAGE_FOLDER;
    }

    public static void setImageFolder(String imageFolder) {
        PictureStartManager.IMAGE_FOLDER = imageFolder;
    }

    public static int getPicturePlaceholderId() {
        return picturePlaceholderId;
    }

    public static void setPicturePlaceholderId(int picturePlaceholderId) {
        PictureStartManager.picturePlaceholderId = picturePlaceholderId;
    }
}
