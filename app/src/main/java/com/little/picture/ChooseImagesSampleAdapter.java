package com.little.picture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.little.picture.listener.IOnItemClickListener;
import com.little.picture.util.ImageUtil;
import com.little.picture.util.fresco.FrescoUtils;
import com.little.picture.util.fresco.InstrumentedDraweeView;

import java.util.List;



public class ChooseImagesSampleAdapter extends BaseAdapter{
    public Context context;
    private List<String> list;
    private int itemWidth = 0;
    private IOnItemClickListener iOnItemClickListener;

    public ChooseImagesSampleAdapter(Context context, List<String> list,int screenWidth) {
        this.context = context;
        this.list = list;
        itemWidth = (screenWidth- 2* ImageUtil.dip2px(context, 1))/3;
    }

    @Override
    public int getCount() {
        return list.size()+1;
    }

    @Override
    public Object getItem(int position) {
        return position;
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
//        final String path = list.get(position);
        viewHolder.containLayout.getLayoutParams().width = itemWidth;
        viewHolder.containLayout.getLayoutParams().height = itemWidth;
        viewHolder.checkBox.setVisibility(View.GONE);
        if (position==(list.size())){
            viewHolder.contentImage.setImageResource(R.drawable.picture_button_green);
        }else {
//            imageLoader.displayImage("file://"+list.get(position),viewHolder.contentImage, OptionTools.getNoDiscOptions(context));

            FrescoUtils.displayImage(viewHolder.contentImage, "file://" + list.get(position));
        }
        final int p = position;
        viewHolder.containLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iOnItemClickListener!=null){
                    iOnItemClickListener.onItemClick(p);
                }
            }
        });
        return convertView;
    }

    public final static class ViewHolder {
        public InstrumentedDraweeView contentImage;
        public ImageView selectorImage;
        public CheckBox checkBox;
        public RelativeLayout containLayout;

        public ViewHolder(View convertView) {
            contentImage = (InstrumentedDraweeView)convertView.findViewById(R.id.picture_fresco_fit_center_draweeView);
            selectorImage = (ImageView)convertView.findViewById(R.id.picture_adapter_grid_selector);
            checkBox = (CheckBox)convertView.findViewById(R.id.picture_adapter_grid_checkBox);
            containLayout = (RelativeLayout)convertView.findViewById(R.id.picture_adapter_grid_layout);
        }
    }

    public IOnItemClickListener getiOnItemClickListener() {
        return iOnItemClickListener;
    }

    public void setiOnItemClickListener(IOnItemClickListener iOnItemClickListener) {
        this.iOnItemClickListener = iOnItemClickListener;
    }
}