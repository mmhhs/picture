package com.little.picture;

import android.content.Context;

import com.fos.fosmvp.common.utils.LogUtils;
import com.little.picture.util.AppApplication;
import com.little.picture.view.dialog.IOnItemListener;
import com.little.picture.view.dialog.PAPopupManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片管理
 */

public class PictureStartManager {
    private static PictureStartManager instance;
    public static Context context;
    public static int SCALE_WIDTH = 1080;//缩放至的宽度
    public static int SCALE_HEIGHT = 1080;//缩放至的高度
    public static int QUALITY = 100;//图像质量
    public static String AUTHORITY = "com.little.picture.fileprovider";//7.0以上调取相机相册需要
    public static String IMAGE_FOLDER = "";//存储图片的文件夹
    public static int picturePlaceholderId = 0;//图片加载占位图

    public static final int FIT_CENTER = 1;
    public static final int CENTER_CROP = 2;
    public static final int CENTER_INSIDE = 3;

    public PictureStartManager(Context context) {
        this.context = context;
        IMAGE_FOLDER = AppApplication.getAppContext().getExternalFilesDir("") + "/cache/image/";
        File folder = new File(IMAGE_FOLDER);
        if (!folder.exists()){
            folder.mkdirs();
        }
        LogUtils.setShowLogEnabled(true);
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
    public static void init(int scaleWidth, int scaleHeight, int quality, String authority, String imageFolder) {
        SCALE_WIDTH = scaleWidth;
        SCALE_HEIGHT = scaleHeight;
        QUALITY = quality;
        AUTHORITY = authority;
        IMAGE_FOLDER = imageFolder;
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

    /**
     * 照片选择框
     * @param cxt 上下文
     * @param fromTag 区分来源
     * @param maxSize 最多选择的数量
     */
    public static void showChooseDialog(final Context cxt,final String fromTag,final int maxSize){
        showChooseDialog(cxt,fromTag,maxSize,0);
    }

    /**
     * 照片选择框
     * @param cxt 上下文
     * @param fromTag 区分来源
     * @param maxSize 最多选择的数量
     */
    public static void showChooseDialog(final Context cxt,final String fromTag,final int maxSize,final int canRecord){
        PAPopupManager popupManager = new PAPopupManager(cxt);
        List<String> list = new ArrayList<>();
        list.add("拍摄");
        list.add("从相册选择");
        popupManager.showListDialog(list);
        popupManager.setOnItemListener(new IOnItemListener() {
            @Override
            public void onItem(int position) {
                if (position==0){
                    PictureTakeActivity.startAction(cxt,0,fromTag,null,canRecord);
                }else if (position==1){
                    PicturePickActivity.startAction(cxt,1,maxSize,fromTag,canRecord);
                }
            }
        });
    }
}
