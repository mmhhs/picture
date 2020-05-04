package com.little.picture.model;

import java.io.Serializable;
import java.util.List;


public class ImageFolderEntity implements Serializable{
	private static final long serialVersionUID = 510488827187140949L;

	/**
	 * 文件夹的第一张图片路径
	 */
	private ImageEntity topImagePath;
	/**
	 * 文件夹名
	 */
	private String folderName;
	/**
	 * 文件夹中的图片数
	 */
	private int imageCounts;
	/**
	 * 是否选中
	 */
	private Boolean selected = false;
	/**
	 * 文件夹中图片路径集合
	 */
	private List<ImageEntity> imagePathList;

	/**
	 * 文件夹中图片路径集合
	 */
	private List<ImageEntity> imageList;


	public ImageFolderEntity() {
	}

	public ImageEntity getTopImagePath() {
		return topImagePath;
	}

	public void setTopImagePath(ImageEntity topImagePath) {
		this.topImagePath = topImagePath;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public int getImageCounts() {
		return imageCounts;
	}

	public void setImageCounts(int imageCounts) {
		this.imageCounts = imageCounts;
	}

	public Boolean getSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public List<ImageEntity> getImagePathList() {
		return imagePathList;
	}

	public void setImagePathList(List<ImageEntity> imagePathList) {
		this.imagePathList = imagePathList;
	}

	public List<ImageEntity> getImageList() {
		return imageList;
	}

	public void setImageList(List<ImageEntity> imageList) {
		this.imageList = imageList;
	}
}
