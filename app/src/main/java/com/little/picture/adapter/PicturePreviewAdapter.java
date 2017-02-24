package com.little.picture.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.little.picture.R;
import com.little.picture.listener.IOnItemClickListener;
import com.little.picture.util.ImageUtil;
import com.little.picture.util.fresco.FrescoUtils;
import com.little.picture.util.fresco.InstrumentedDraweeView;

import java.util.List;

public class PicturePreviewAdapter extends PagerAdapter {

    private List<String> list;
    private Context context;
    private IOnItemClickListener onItemClickListener;

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
        InstrumentedDraweeView draweeView = (InstrumentedDraweeView)convertView.findViewById(R.id.picture_fresco_fit_center_draweeView);
        FrescoUtils.displayImage(draweeView, ImageUtil.completeImagePath(list.get(position)), 720, 1280);
        draweeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position);
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

    public IOnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(IOnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
