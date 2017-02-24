package com.little.picture.model;

import java.io.Serializable;
import java.util.List;


public class ImageEntity implements Serializable{
	/**
	 * 文件夹的第一张图片路径
	 */
	private String topImagePath;
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
	private List<String> imagePathList;

	public String getTopImagePath() {
		return topImagePath;
	}

	public void setTopImagePath(String topImagePath) {
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

	public List<String> getImagePathList() {
		return imagePathList;
	}

	public void setImagePathList(List<String> imagePathList) {
		this.imagePathList = imagePathList;
	}
}
