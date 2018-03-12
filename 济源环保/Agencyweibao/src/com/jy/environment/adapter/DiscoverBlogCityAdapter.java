package com.jy.environment.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jy.environment.R;


public class DiscoverBlogCityAdapter extends BaseAdapter {

	private Context context;

	private List<String> list;

	public DiscoverBlogCityAdapter(Context context, List<String> list) {

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
	public View getView(int position, View convertView, ViewGroup viewGroup) {

		
		ViewHolder holder;
		if (convertView==null) {
			convertView=LayoutInflater.from(context).inflate(R.layout.discove_blog_city_item, null);
			holder=new ViewHolder();
			convertView.setTag(holder);
			holder.groupItem=(TextView) convertView.findViewById(R.id.groupItem);
		}
		else{
			holder=(ViewHolder) convertView.getTag();
		}
		holder.groupItem.setText(list.get(position));
		
		return convertView;
	}

	static class ViewHolder {
		TextView groupItem;
	}

}
