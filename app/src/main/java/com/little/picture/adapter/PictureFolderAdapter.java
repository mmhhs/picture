package com.little.picture.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.little.picture.R;
import com.little.picture.model.ImageEntity;
import com.little.picture.util.ImageUtil;
import com.little.picture.util.fresco.FrescoUtils;
import com.little.picture.util.fresco.InstrumentedDraweeView;

import java.util.List;


public class PictureFolderAdapter extends BaseAdapter{
    public Context context;
    private List<ImageEntity> list;

    public PictureFolderAdapter(Context context, List<ImageEntity> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView,final ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.picture_adapter_folder, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ImageEntity imageEntity = list.get(position);
        viewHolder.nameText.setText(imageEntity.getFolderName());
        viewHolder.countText.setText(imageEntity.getImageCounts()+context.getString(R.string.picture_unit));
        FrescoUtils.displayImage(viewHolder.topImage, ImageUtil.completeImagePath(imageEntity.getTopImagePath()), ImageUtil.dip2px(context, 80), ImageUtil.dip2px(context, 80));
        if (imageEntity.getSelected()){
            viewHolder.selectImage.setVisibility(View.VISIBLE);
        }else {
            viewHolder.selectImage.setVisibility(View.GONE);
        }
        return convertView;
    }

    public final static class ViewHolder {
        public InstrumentedDraweeView topImage;
        public ImageView selectImage;
        public TextView nameText;
        public TextView countText;
        public LinearLayout containLayout;

        public ViewHolder(View convertView) {
            topImage = (InstrumentedDraweeView)convertView.findViewById(R.id.picture_fresco_center_crop_draweeView);
            selectImage = (ImageView)convertView.findViewById(R.id.picture_adapter_folder_select);
            nameText = (TextView)convertView.findViewById(R.id.picture_adapter_folder_name);
            countText = (TextView)convertView.findViewById(R.id.picture_adapter_folder_count);
            containLayout = (LinearLayout)convertView.findViewById(R.id.picture_adapter_folder_layout);
        }
    }


}