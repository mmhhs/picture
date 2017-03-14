package com.little.picture.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.little.picture.R;


public class PageIndicatorView extends View {
	private int mCurrentPage = 0;
	/**
	 * 选中颜色
	 */
	private int selectedColor;
	/**
	 * 未选中颜色
	 */
	private int unselectedColor;
	/**
	 * 圆点半径
	 */
	private float radius;
	/**
	 * 间距
	 */
	private float space;
	/**
	 * 透明度
	 */
	private int alpha;

	/**
	 * 选中颜色值
	 */
	private String selectedColorString = "#f03b66";
	/**
	 * 未选中颜色值
	 */
	private String unselectedColorString = "#a0a0a0";
	/**
	 * 总页数
	 */
	private int mTotalPage = 0;
	/**
	 * 半径
	 */
	private int radiusSize = 10;
	/**
	 * 间距
	 */
	private int spaceSize = 25;


	public PageIndicatorView(Context context) {
		super(context);
	}

	public PageIndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray customAttrs = context.obtainStyledAttributes(attrs,
				R.styleable.PageIndicatorView);
		radius = customAttrs.getDimension(R.styleable.PageIndicatorView_radius, radiusSize);
		space = customAttrs.getDimension(R.styleable.PageIndicatorView_space, spaceSize);
		alpha = customAttrs.getInt(R.styleable.PageIndicatorView_alphas, 0x00);
		selectedColor = customAttrs.getColor(
				R.styleable.PageIndicatorView_selectedColor,
				Color.parseColor(selectedColorString));
		unselectedColor = customAttrs.getColor(
				R.styleable.PageIndicatorView_unselectedColor,
				Color.parseColor(unselectedColorString));
		customAttrs.recycle();
	}

	public void setTotalPage(int nPageNum) {
		mTotalPage = nPageNum;
		if (mCurrentPage >= mTotalPage)
			mCurrentPage = mTotalPage - 1;
	}

	public int getCurrentPage() {
		return mCurrentPage;
	}

	public void setCurrentPage(int nPageIndex) {
		if (nPageIndex < 0 || nPageIndex >= mTotalPage)
			return;

		if (mCurrentPage != nPageIndex||nPageIndex==0) {
			mCurrentPage = nPageIndex;
			this.invalidate();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setAlpha(alpha);
		paint.setAntiAlias(true);

		Rect r = new Rect();
		getDrawingRect(r);

		canvas.drawRect(r, paint);

		if (mTotalPage == 1)
			return;

		float x = (r.width() - (radius * 2 * mTotalPage + space
				* (mTotalPage - 1))) / 2;
		float y = r.height() / 2;

		for (int i = 0; i < mTotalPage; i++) {
			if (i == mCurrentPage) {
				paint.setColor(selectedColor);
			} else {
				paint.setColor(unselectedColor);
			}

			canvas.drawCircle(x, y, radius, paint);

			x += radius * 2 + space;
		}
	}

	public void setSpace(int space) {
		this.space = space;
	}

	public void setmCurrentPage(int mCurrentPage) {
		this.mCurrentPage = mCurrentPage;
	}

	public void setSelectedColor(int selectedColor) {
		this.selectedColor = selectedColor;
	}

	public void setUnselectedColor(int unselectedColor) {
		this.unselectedColor = unselectedColor;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}
}
