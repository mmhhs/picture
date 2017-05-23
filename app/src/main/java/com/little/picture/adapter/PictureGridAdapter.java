package com.little.picture.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.little.picture.PicturePickActivity;
import com.little.picture.R;
import com.little.picture.glide.GlideUtil;
import com.little.picture.listener.IOnCheckListener;
import com.little.picture.listener.IOnItemClickListener;
import com.little.picture.util.ImageUtil;
import com.little.picture.util.ToastUtil;

import java.util.List;


public class PictureGridAdapter extends BaseAdapter{
    public Context context;
    private List<String> list;//当前显示图片列表
    private List<String> chooseList;//已选择的图片列表
    private int itemWidth = 0;
    private IOnCheckListener onCheckListener;
    private IOnItemClickListener onItemClickListener;
    private int maxSize;//最大选择图片数
    private int folderShowIndex;//文件夹索引
    private int funcType;//功能类型

    public PictureGridAdapter(Context context, List<String> list, List<String> chooseList, int screenWidth, int maxSize, int folderShowIndex, int funcType) {
        this.context = context;
        this.list = list;
        this.chooseList = chooseList;
        this.itemWidth = (screenWidth- 2* ImageUtil.dip2px(context, 1))/3;
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
        final String path = list.get(position);
        viewHolder.containerLayout.getLayoutParams().width = itemWidth;
        viewHolder.containerLayout.getLayoutParams().height = itemWidth;
        if (position==0&&folderShowIndex==0){
            //全部图片文件夹时显示拍照
            viewHolder.contentImage.setImageResource(R.drawable.picture_shoot);
            viewHolder.checkBox.setVisibility(View.GONE);
        }else {
            GlideUtil.getInstance().display(context, ImageUtil.completeImagePath(path),viewHolder.contentImage, itemWidth, itemWidth);
            viewHolder.checkBox.setVisibility(View.VISIBLE);
        }
        if (funcType == PicturePickActivity.PICK_AVATAR){
            viewHolder.checkBox.setVisibility(View.GONE);
        }
        if (isSelected(path)){
            viewHolder.selectorImage.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setChecked(true);
        }else {
            viewHolder.selectorImage.setVisibility(View.GONE);
            viewHolder.checkBox.setChecked(false);
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
        public CheckBox checkBox;
        public RelativeLayout containerLayout;

        public ViewHolder(View convertView) {
            contentImage = (ImageView)convertView.findViewById(R.id.picture_adapter_grid_imageView);
            selectorImage = (ImageView)convertView.findViewById(R.id.picture_adapter_grid_selector);
            checkBox = (CheckBox)convertView.findViewById(R.id.picture_adapter_grid_checkBox);
            containerLayout = (RelativeLayout)convertView.findViewById(R.id.picture_adapter_grid_layout);
        }
    }

    private boolean isSelected(String path){
        boolean result = false;
        for(String imagePath:chooseList){
            if (path.equals(imagePath)){
                result = true;
            }
        }
        return result;
    }

    private void setSelected(String path,boolean isChecked){
        if (isChecked){
            if (!isSelected(path)){
                if (chooseList.size()<maxSize){
                    chooseList.add(path);
                }else {
                    ToastUtil.addToast(context, "" + context.getString(R.string.picture_max) + maxSize);
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