package com.little.picture.model;

import java.io.Serializable;

/**
 * Created by xxjsb on 2019/3/12.
 */

public class ImageEntity implements Serializable , Comparable<ImageEntity>{
    private static final long serialVersionUID = 510488828187140949L;

    private int type=0;//类型：0：照片，1：视频，2：音频

    private String imagePath = "";//原始图片路径或视频路径
    private String scalePath = "";//压缩后图片路径
    private String thumbPath = "";//视频缩略图路径
    private int width = 0;//宽度
    private int height = 0;//高度
    private String displayName = "";//名称
    private String mimeType = "";//文件类型
    private String duration = "0";//时长，单位秒
    private String addTime = "";//添加时间
    private boolean showDelete = false;//是否显示删除按钮

    public ImageEntity() {
    }

    public String getScalePath() {
        return scalePath;
    }

    public void setScalePath(String scalePath) {
        this.scalePath = scalePath;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }

    public boolean isShowDelete() {
        return showDelete;
    }

    public void setShowDelete(boolean showDelete) {
        this.showDelete = showDelete;
    }

    @Override
    public int compareTo(ImageEntity o) {
        int i = o.getAddTime().compareTo(this.getAddTime());
        return i;
    }
}
