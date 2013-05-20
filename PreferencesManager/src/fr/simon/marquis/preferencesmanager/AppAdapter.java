package fr.simon.marquis.preferencesmanager;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AppAdapter extends BaseAdapter {
	List<AppEntry> applications;
	LayoutInflater layoutInflater;
	Context ctx;

	public AppAdapter(List<AppEntry> applications, LayoutInflater layoutInflater, Context ctx) {
		this.applications = applications;
		this.layoutInflater = layoutInflater;
		this.ctx = ctx;
	}

	@Override
	public int getCount() {
		return applications.size();
	}

	@Override
	public AppEntry getItem(int arg0) {
		return applications.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = layoutInflater
					.inflate(R.layout.row_application, null);
			holder = new ViewHolder();
			holder.textView = (TextView) convertView
					.findViewById(R.id.item_text);
			holder.imageView = (ImageView) convertView
					.findViewById(R.id.item_image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		AppEntry item = getItem(position);
		holder.textView.setText(item.getLabel());
		holder.imageView.setImageDrawable(item.getIcon(ctx));

		return convertView;
	}

	class ViewHolder {
		private ImageView imageView;
		private TextView textView;
	}
}