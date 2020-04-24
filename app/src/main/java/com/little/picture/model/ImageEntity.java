package com.little.picture.model;

import java.io.Serializable;

/**
 * Created by xxjsb on 2019/3/12.
 */

public class ImageEntity implements Serializable{
    private static final long serialVersionUID = 510488828187140949L;

    private String imagePath = "";//图片路径
    private String originalPath = "";//原始图片路径
    private String scalePath = "";//压缩后图片路径
    private int width = 0;
    private int height = 0;

    private int type=0;//类型：0：照片，1：视频
    private String videoPath;//视频路径
    private String videoThumbPath;//视频缩略图路径
    private int duration = 0;//时长，单位秒

    public ImageEntity() {
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
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

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getVideoThumbPath() {
        return videoThumbPath;
    }

    public void setVideoThumbPath(String videoThumbPath) {
        this.videoThumbPath = videoThumbPath;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
