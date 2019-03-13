package com.little.picture;

/**
 * 图片管理
 */

public class PictureStartManager {
    public static int SCALE_WIDTH = 1080;//缩放至的宽度
    public static int SCALE_HEIGHT = 1920;//缩放至的高度
    public static int quality = 100;//图像质量
    public static String authority = "com.foton.almighty.fileprovider";//7.0以上调取相机相册需要
    public static String imagePathFolder = "";//存储图片的文件夹


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

    public static int getQuality() {
        return quality;
    }

    public static void setQuality(int quality) {
        PictureStartManager.quality = quality;
    }

    public static String getAuthority() {
        return authority;
    }

    public static void setAuthority(String authority) {
        PictureStartManager.authority = authority;
    }

    public static String getImagePathFolder() {
        return imagePathFolder;
    }

    public static void setImagePathFolder(String imagePathFolder) {
        PictureStartManager.imagePathFolder = imagePathFolder;
    }
}
