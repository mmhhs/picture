package com.little.picture.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.little.picture.R;
import com.little.picture.glide.GlideUtil;
import com.little.picture.listener.IOnGestureListener;
import com.little.picture.util.ImageUtil;
import com.little.picture.view.ZoomImageView;

import java.util.List;

public class PicturePreviewAdapter extends PagerAdapter {

    private List<String> list;
    private Context context;
    private IOnGestureListener onGestureListener;

    public PicturePreviewAdapter(Context context, List<String> list){
        this.context = context;
        this.list = list;
    }


    @Override
    public void destroyItem(ViewGroup view, int position, Object object) {
        view.removeView((View) object);
    }

    @Override
    public void finishUpdate(ViewGroup container) {
    }

    @Override
    public Object instantiateItem(ViewGroup view, final int position) {
        View convertView = null;
        convertView = LayoutInflater.from(context).inflate(R.layout.picture_adapter_preview, null);
        ZoomImageView imageView = (ZoomImageView)convertView.findViewById(R.id.picture_adapter_preview_imageView);
        GlideUtil.getInstance().display(context, ImageUtil.completeImagePath(list.get(position)),imageView,GlideUtil.FIT_CENTER,R.drawable.picture_placeholder,R.drawable.picture_placeholder, 360, 640);
        imageView.setOnGestureListener(new IOnGestureListener() {
            @Override
            public void onClick() {
                if (onGestureListener != null) {
                    onGestureListener.onClick();
                }
            }

            @Override
            public void onDoubleClick() {
                if (onGestureListener != null) {
                    onGestureListener.onDoubleClick();
                }
            }

            @Override
            public void onLongPress() {
                if (onGestureListener != null) {
                    onGestureListener.onLongPress();
                }
            }
        });
        view.addView(convertView, 0);
        return convertView;
    }

    @Override
    public int getCount() {
        if(list != null){
            return list.size();
        }
        return 0;
    }


    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return (arg0 == arg1);
    }
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public IOnGestureListener getOnGestureListener() {
        return onGestureListener;
    }

    public void setOnGestureListener(IOnGestureListener onGestureListener) {
        this.onGestureListener = onGestureListener;
    }
}
