package com.little.picture.glide;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

/**
 * Created by xxjsb on 2019/4/16.
 */

public class GlidePlaceholderDrawables extends Drawable{
    private final Paint mPaint;
    private final float[] mMatrixValues;
    private final int mWidth;
    private final int mHeight;
    private final Bitmap mResource;

    public GlidePlaceholderDrawables(Resources res, @DrawableRes int resource) {
        this(BitmapFactory.decodeResource(res, resource));
    }

    public GlidePlaceholderDrawables(Bitmap resource) {
        this.mPaint = new Paint(1);
        this.mMatrixValues = new float[9];
        this.mHeight = resource.getHeight();
        this.mWidth = resource.getWidth();
        this.mResource = resource;
    }

    public int getMinimumHeight() {
        return this.mHeight;
    }

    public int getMinimumWidth() {
        return this.mWidth;
    }

    public void draw(@NonNull Canvas canvas) {
        Matrix matrix = canvas.getMatrix();
        matrix.getValues(this.mMatrixValues);
        this.mMatrixValues[2] = ((float)((canvas.getWidth() - this.mWidth) / 2) - this.mMatrixValues[2]) / this.mMatrixValues[0];
        this.mMatrixValues[5] = ((float)((canvas.getHeight() - this.mHeight) / 2) - this.mMatrixValues[5]) / this.mMatrixValues[4];
        this.mMatrixValues[0] = 1.0F / this.mMatrixValues[0];
        this.mMatrixValues[4] = 1.0F / this.mMatrixValues[4];
        matrix.setValues(this.mMatrixValues);
        canvas.drawBitmap(this.mResource, matrix, this.mPaint);
    }

    public void setAlpha(int i) {
    }

    public void setColorFilter(ColorFilter colorFilter) {
    }

    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }


}
