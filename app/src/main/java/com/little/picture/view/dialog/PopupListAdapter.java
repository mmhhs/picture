package com.little.picture.view.dialog;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.little.picture.R;

import java.util.List;


public class PopupListAdapter extends BaseAdapter {
	public Context context;
	public List<String> list;

	public PopupListAdapter(Context context, List<String> list) {
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
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder ;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.picture_adapter_list, null);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		try {
			viewHolder.text.setText(list.get(position));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return convertView;
	}

	public final static class ViewHolder {
		TextView text;

		public ViewHolder(View convertView) {
			text = (TextView) convertView.findViewById(R.id.popup_adapter_list_text);
		}
	}
}
