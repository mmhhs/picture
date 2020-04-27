package com.little.picture.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fos.fosmvp.common.utils.StringUtils;
import com.little.picture.PicturePickActivity;
import com.little.picture.R;
import com.little.picture.glide.GlideUtil;
import com.little.picture.listener.IOnCheckListener;
import com.little.picture.listener.IOnItemClickListener;
import com.little.picture.model.ImageEntity;
import com.little.picture.util.ImageUtil;
import com.little.picture.util.PaToastUtils;
import com.little.picture.util.VideoUtil;

import java.util.List;

import static com.little.picture.PictureStartManager.CENTER_CROP;


public class PictureGridAdapter extends BaseAdapter{
    public Context context;
    private List<ImageEntity> list;//当前显示图片列表
    private List<ImageEntity> chooseList;//已选择的图片列表
    private int itemWidth = 0;
    private IOnCheckListener onCheckListener;
    private IOnItemClickListener onItemClickListener;
    private int maxSize;//最大选择图片数
    private int folderShowIndex;//文件夹索引
    private int funcType;//功能类型

    public PictureGridAdapter(Context context, List<ImageEntity> list, List<ImageEntity> chooseList, int screenWidth, int maxSize, int folderShowIndex, int funcType) {
        this.context = context;
        this.list = list;
        this.chooseList = chooseList;
        this.itemWidth = (screenWidth- 3* ImageUtil.dip2px(context, 1))/4;
        this.maxSize = maxSize;
        this.folderShowIndex = folderShowIndex;
        this.funcType = funcType;
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
                    R.layout.picture_adapter_grid, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final ImageEntity path = list.get(position);
        viewHolder.containerLayout.getLayoutParams().width = itemWidth;
        viewHolder.containerLayout.getLayoutParams().height = itemWidth;

        if (path.getType()==0){
            GlideUtil.getInstance().display(context, ImageUtil.completeImagePath(path.getImagePath()),viewHolder.contentImage,CENTER_CROP, itemWidth, itemWidth);

            viewHolder.tvSc.setVisibility(View.GONE);
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            if (funcType == PicturePickActivity.PICK_AVATAR||funcType==PicturePickActivity.PICK_CROP_IMAGE){
                viewHolder.checkBox.setVisibility(View.GONE);
            }
            if (isSelected(path)){
                viewHolder.selectorImage.setVisibility(View.VISIBLE);
                viewHolder.checkBox.setSelected(true);
            }else {
                viewHolder.selectorImage.setVisibility(View.GONE);
                viewHolder.checkBox.setSelected(false);
            }
        }else {
            String imagePath = path.getImagePath();
            if (!StringUtils.isEmpty(path.getThumbPath())){
                imagePath = path.getThumbPath();
            }
            GlideUtil.getInstance().display(context, ImageUtil.completeImagePath(imagePath),viewHolder.contentImage,CENTER_CROP, itemWidth, itemWidth);
            viewHolder.tvSc.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setVisibility(View.GONE);
            viewHolder.tvSc.setText(VideoUtil.formatVideoTime(path.getDuration()));
        }

        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelected(path)){
                    setSelected(path,false);
                }else {
                    setSelected(path,true);
                }
            }
        });
        final int p = position;
        viewHolder.containerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(p);
                }
            }
        });
        return convertView;
    }

    public final static class ViewHolder {
        public ImageView contentImage;
        public ImageView selectorImage;
        public ImageView checkBox;
        public RelativeLayout containerLayout;
        public TextView tvSc;

        public ViewHolder(View convertView) {
            contentImage = convertView.findViewById(R.id.picture_adapter_grid_imageView);
            selectorImage = convertView.findViewById(R.id.picture_adapter_grid_selector);
            checkBox = convertView.findViewById(R.id.picture_adapter_grid_checkBox);
            containerLayout = convertView.findViewById(R.id.picture_adapter_grid_layout);
            tvSc = convertView.findViewById(R.id.picture_adapter_grid_sc);
        }
    }

    private boolean isSelected(ImageEntity path){
        boolean result = false;
        for(ImageEntity imagePath:chooseList){
            if (path.getImagePath().equals(imagePath.getImagePath())){
                result = true;
            }
        }
        return result;
    }

    private void setSelected(ImageEntity path,boolean isChecked){
        if (isChecked){
            if (!isSelected(path)){
                if (chooseList.size()<maxSize){
                    chooseList.add(path);
                }else {
                    PaToastUtils.addToast(context, "" + context.getString(R.string.picture_max) + maxSize);
                }
            }
        }else {
            if (isSelected(path)){
                chooseList.remove(path);
            }
        }
        notifyDataSetChanged();
        if (onCheckListener !=null){
            onCheckListener.onCheck(chooseList);
        }
    }

    public IOnCheckListener getOnCheckListener() {
        return onCheckListener;
    }

    public void setOnCheckListener(IOnCheckListener onCheckListener) {
        this.onCheckListener = onCheckListener;
    }

    public IOnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(IOnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}