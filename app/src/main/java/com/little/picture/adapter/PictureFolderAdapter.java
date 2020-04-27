package com.little.picture.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fos.fosmvp.common.utils.StringUtils;
import com.little.picture.PictureStartManager;
import com.little.picture.R;
import com.little.picture.glide.GlideUtil;
import com.little.picture.model.ImageEntity;
import com.little.picture.model.ImageFolderEntity;
import com.little.picture.util.ImageUtil;

import java.util.List;

import static com.little.picture.PictureStartManager.CENTER_CROP;


public class PictureFolderAdapter extends BaseAdapter{
    public Context context;
    private List<ImageFolderEntity> list;

    public PictureFolderAdapter(Context context, List<ImageFolderEntity> list) {
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
        ImageFolderEntity imageFolderEntity = list.get(position);
        viewHolder.nameText.setText(imageFolderEntity.getFolderName());
        viewHolder.countText.setText(imageFolderEntity.getImageCounts()+context.getString(R.string.picture_unit));

        ImageEntity topImage = imageFolderEntity.getTopImagePath();
        if (StringUtils.isEmpty(topImage.getThumbPath())){
            GlideUtil.getInstance().displayFillet(context, ImageUtil.completeImagePath(topImage.getImagePath()),viewHolder.topImage, PictureStartManager.FIT_CENTER,5);
//            GlideUtil.getInstance().display(context, ImageUtil.completeImagePath(topImage.getImagePath()),viewHolder.topImage, CENTER_CROP,ImageUtil.dip2px(context, 80), ImageUtil.dip2px(context, 80));
        }else {
            GlideUtil.getInstance().displayFillet(context, ImageUtil.completeImagePath(topImage.getThumbPath()),viewHolder.topImage, PictureStartManager.FIT_CENTER,5);
//            GlideUtil.getInstance().display(context, ImageUtil.completeImagePath(topImage.getThumbPath()),viewHolder.topImage, CENTER_CROP,ImageUtil.dip2px(context, 80), ImageUtil.dip2px(context, 80));
        }

        if (imageFolderEntity.getSelected()){
            viewHolder.selectImage.setVisibility(View.VISIBLE);
        }else {
            viewHolder.selectImage.setVisibility(View.GONE);
        }
        return convertView;
    }

    public final static class ViewHolder {
        public ImageView topImage;
        public ImageView selectImage;
        public TextView nameText;
        public TextView countText;
        public LinearLayout containLayout;

        public ViewHolder(View convertView) {
            topImage = (ImageView)convertView.findViewById(R.id.picture_adapter_folder_imageView);
            selectImage = (ImageView)convertView.findViewById(R.id.picture_adapter_folder_select);
            nameText = (TextView)convertView.findViewById(R.id.picture_adapter_folder_name);
            countText = (TextView)convertView.findViewById(R.id.picture_adapter_folder_count);
            containLayout = (LinearLayout)convertView.findViewById(R.id.picture_adapter_folder_layout);
        }
    }


}