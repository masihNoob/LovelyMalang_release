package com.lunartech.lovelymalang;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;

public class SSAdapter extends ArrayAdapter<Device> {

	private final Context context;
	private final List<Device> values;

	private static ImageLoader mImageLoader;

	public SSAdapter(Context context, List<Device> devices) {
		super(context, R.layout.ssitem, devices);
		this.context = context;
		this.values = devices;
		mImageLoader = MySingleton.getInstance(context).getImageLoader();
	}

	private static class ViewHolder {
		public ImageView iconView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) { // if it's not recycled, initialize some
									// attributes

			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.ssitem, parent, false);

			ImageView imageView = (ImageView) convertView
					.findViewById(R.id.image);
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			holder = new ViewHolder();
			holder.iconView = imageView;
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Device item = values.get(position);

		mImageLoader.get(item.getLink(),
				com.android.volley.toolbox.ImageLoader.getImageListener(holder.iconView,
						R.mipmap.empty_photo, R.mipmap.empty_photo));

		return convertView;

	}
}