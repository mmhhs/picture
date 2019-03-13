package com.little.picture.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xxjsb on 2019/3/12.
 */

public class ImageListEntity implements Serializable{
    private static final long serialVersionUID = 510488829187140949L;

    private List<ImageEntity> imageList;

    private List<String> chooseImageList;//选中的图片

    private String fromTag = "";//来源标志

    public ImageListEntity() {
    }

    public List<ImageEntity> getImageList() {
        return imageList;
    }

    public void setImageList(List<ImageEntity> imageList) {
        this.imageList = imageList;
    }

    public List<String> getChooseImageList() {
        return chooseImageList;
    }

    public void setChooseImageList(List<String> chooseImageList) {
        this.chooseImageList = chooseImageList;
    }

    public String getFromTag() {
        return fromTag;
    }

    public void setFromTag(String fromTag) {
        this.fromTag = fromTag;
    }
}
