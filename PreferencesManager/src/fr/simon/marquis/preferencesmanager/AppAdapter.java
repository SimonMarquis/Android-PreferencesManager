package fr.simon.marquis.preferencesmanager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class AppAdapter extends ArrayAdapter<AppEntry> {
	LayoutInflater layoutInflater;
	Context ctx;

	public AppAdapter(Context ctx) {
		super(ctx, android.R.layout.simple_list_item_2);
		this.layoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.ctx = ctx;
	}

	public void setData(List<AppEntry> data) {
        clear();
        if (data != null) {
            for (AppEntry appEntry : data) {
                add(appEntry);
            }
        }
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
//
//	@Override
//	public Filter getFilter() {
//		return new Filter() {
//
//			@Override
//			protected void publishResults(CharSequence constraint,
//					FilterResults results) {
//				Log.e("","publishResults "+results.count);
//				applications = (List<AppEntry>) results.values;
//				notifyDataSetChanged();
//			}
//
//			@Override
//			protected FilterResults performFiltering(CharSequence constraint) {
//				
//				Log.e("","performFiltering");
//				FilterResults results = new FilterResults();
//				if (TextUtils.isEmpty(constraint)) {
//					results.values = _applications;
//					results.count = _applications.size();
//				} else {
//					List<AppEntry> resApps = new ArrayList<AppEntry>();
//					for (AppEntry p : applications) {
//						if (p.getLabel().toUpperCase()
//								.contains(constraint.toString().toUpperCase()))
//							resApps.add(p);
//					}
//
//					results.values = resApps;
//					results.count = resApps.size();
//
//				}
//				return results;
//			}
//		};
//	}

}