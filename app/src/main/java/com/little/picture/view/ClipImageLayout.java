package com.little.picture.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RelativeLayout;

import com.little.picture.PictureStartManager;
import com.little.picture.R;
import com.little.picture.util.ImageUtil;

import java.io.File;


public class ClipImageLayout extends RelativeLayout
{

	private Context mContext;
	private ClipZoomImageView mZoomImageView;
	private ClipImageBorderView mClipImageView;

	/**
	 * 这里测试，直接写死了大小，真正使用过程中，可以提取为自定义属性
	 */
	private int mHorizontalPadding = 20;

	public ClipImageLayout(Context context) {
		super(context);
		mContext = context;
		init(null, 0);
	}

	public ClipImageLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init(attrs, 0);
	}

	public ClipImageLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init(attrs, defStyle);
	}



	public void init(AttributeSet attrs, int defStyle)
	{
		final TypedArray a = getContext().obtainStyledAttributes(
				attrs, R.styleable.PageIndicatorView, defStyle, 0);
		mHorizontalPadding = a.getDimensionPixelSize(R.styleable.ClipImageLayout_clipImageHorizontalPadding, 20);

		mZoomImageView = new ClipZoomImageView(mContext);
		mClipImageView = new ClipImageBorderView(mContext);

		android.view.ViewGroup.LayoutParams lp = new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		
		/**
		 * 这里测试，直接写死了图片，真正使用过程中，可以提取为自定义属性
		 */
//		mZoomImageView.setImageDrawable(getResources().getDrawable(
//				R.drawable.picture_avatar));
		
		this.addView(mZoomImageView, lp);
		this.addView(mClipImageView, lp);

		
		// 计算padding的px
		mHorizontalPadding = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, mHorizontalPadding, getResources()
						.getDisplayMetrics());
		mZoomImageView.setHorizontalPadding(mHorizontalPadding);
		mClipImageView.setHorizontalPadding(mHorizontalPadding);
	}

	public void setImageUri(String imagePath){
		String path = ImageUtil.saveScaleImage(imagePath, PictureStartManager.getImageFolder(), PictureStartManager.SCALE_WIDTH, PictureStartManager.SCALE_HEIGHT, 100);

		mZoomImageView.setImageUri(Uri.fromFile(new File(path)));
	}

	/**
	 * 对外公布设置边距的方法,单位为dp
	 *
	 */
	public void setHorizontalPadding(int mHorizontalPadding)
	{
		this.mHorizontalPadding = mHorizontalPadding;
	}

	/**
	 * 裁切图片
	 *
	 */
	public Bitmap clip()
	{
		return mZoomImageView.clip();
	}

	public void setRate(float rate){
		mZoomImageView.setRate(rate);
		mClipImageView.setWhRate(rate);
	}

}
