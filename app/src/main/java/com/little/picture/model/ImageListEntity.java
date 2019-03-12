package com.little.picture.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xxjsb on 2019/3/12.
 */

public class ImageListEntity implements Serializable{
    private static final long serialVersionUID = 510488829187140949L;

    private List<ImageEntity> imageList;

    public ImageListEntity() {
    }

    public List<ImageEntity> getImageList() {
        return imageList;
    }

    public void setImageList(List<ImageEntity> imageList) {
        this.imageList = imageList;
    }
}
