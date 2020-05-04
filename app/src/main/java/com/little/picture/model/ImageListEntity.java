package com.little.picture.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xxjsb on 2019/3/12.
 */

public class ImageListEntity implements Serializable{
    private static final long serialVersionUID = 510488829187140949L;

    private List<ImageEntity> imageList;

    private int mode = 0;//模式：0：拍照，1：录像
    private String fromTag = "";//来源标志
    private List<ImageEntity> chooseImageList;//选中的图片
    private String videoPath;//视频路径
    private String videoThumbPath;//视频缩略图路径

    public ImageListEntity() {
    }

    public List<ImageEntity> getImageList() {
        return imageList;
    }

    public void setImageList(List<ImageEntity> imageList) {
        this.imageList = imageList;
    }

    public List<ImageEntity> getChooseImageList() {
        return chooseImageList;
    }

    public void setChooseImageList(List<ImageEntity> chooseImageList) {
        this.chooseImageList = chooseImageList;
    }

    public String getFromTag() {
        return fromTag;
    }

    public void setFromTag(String fromTag) {
        this.fromTag = fromTag;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
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

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
