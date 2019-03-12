package com.little.picture.model;

import java.io.Serializable;

/**
 * Created by xxjsb on 2019/3/12.
 */

public class ImageEntity implements Serializable{
    private static final long serialVersionUID = 510488828187140949L;

    private String originalPath = "";
    private String scalePath = "";
    private int width = 0;
    private int height = 0;

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
}
