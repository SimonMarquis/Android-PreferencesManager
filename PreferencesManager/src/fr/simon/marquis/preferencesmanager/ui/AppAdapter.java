/*
 * Copyright (C) 2013 Simon Marquis (http://www.simon-marquis.fr)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package fr.simon.marquis.preferencesmanager.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;

import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.AppEntry;
import fr.simon.marquis.preferencesmanager.util.MyComparator;

public class AppAdapter extends BaseAdapter implements
		StickyListHeadersAdapter, Filterable {
	private LayoutInflater layoutInflater;
	private Context context;
	private Pattern pattern;
	private ArrayList<AppEntry> applications;
	private ArrayList<AppEntry> applicationsToDisplay;
	private int color;
	private final Object mLock = new Object();

	public AppAdapter(Context ctx, ArrayList<AppEntry> applications) {
		this.context = ctx;
		this.applications = applications;
		this.applicationsToDisplay = applications;
		this.layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.color = context.getResources().getColor(R.color.blue);
	}

	@Override
	public void notifyDataSetChanged() {
		synchronized (mLock) {
			Collections.sort(applicationsToDisplay, new MyComparator());
		}
		super.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.row_application,
					parent, false);
			holder = new ViewHolder();
			holder.textView = (TextView) convertView
					.findViewById(R.id.item_text);
			holder.imageView = (ImageView) convertView
					.findViewById(R.id.item_image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		AppEntry item = applicationsToDisplay.get(position);

		holder.textView.setText(createSpannable(item.getLabel()));
		holder.imageView.setImageDrawable(item.getIcon(context));

		return convertView;
	}

	class ViewHolder {
		private ImageView imageView;
		private TextView textView;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderViewHolder holder;
		if (convertView == null) {
			holder = new HeaderViewHolder();
			convertView = layoutInflater.inflate(R.layout.row_header, parent,
					false);
			holder.text = (TextView) convertView.findViewById(R.id.text_header);
			convertView.setTag(holder);
		} else {
			holder = (HeaderViewHolder) convertView.getTag();
		}
		String headerText = String.valueOf(applicationsToDisplay.get(position)
				.getHeaderChar());
		holder.text.setText(headerText);
		return convertView;
	}

	class HeaderViewHolder {
		TextView text;
	}

	@Override
	public long getHeaderId(int position) {
		return applicationsToDisplay.get(position).getHeaderChar();
	}

	public void setFilter(String filter) {
		if (TextUtils.isEmpty(filter))
			pattern = null;
		else
			pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
	}

	/**
	 * Highlight text
	 * 
	 * @param s
	 * @return
	 */
	public SpannableStringBuilder createSpannable(String s) {
		final SpannableStringBuilder spannable = new SpannableStringBuilder(s);
		if (pattern == null)
			return spannable;
		final Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			final ForegroundColorSpan span = new ForegroundColorSpan(color);
			final StyleSpan span2 = new StyleSpan(
					android.graphics.Typeface.BOLD);
			spannable.setSpan(span2, matcher.start(), matcher.end(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			spannable.setSpan(span, matcher.start(), matcher.end(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return spannable;
	}

	@Override
	public int getCount() {
		return applicationsToDisplay.size();
	}

	@Override
	public Object getItem(int position) {
		return applicationsToDisplay.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence charSequence) {
				FilterResults results = new FilterResults();
				if (charSequence == null || charSequence.length() == 0) {
					synchronized (mLock) {
						results.values = applications;
						results.count = applications.size();
					}
				} else {
					String prefixString = charSequence.toString()
							.toLowerCase(Locale.getDefault()).trim();
					ArrayList<AppEntry> filterResultsData = new ArrayList<AppEntry>();
					synchronized (mLock) {
						for (AppEntry data : applications) {
							Pattern p = Pattern.compile(prefixString,
									Pattern.CASE_INSENSITIVE);
							if (p.matcher(
									data.getLabel()
											.toLowerCase(Locale.getDefault())
											.trim()).find()) {
								filterResultsData.add(data);
							}
						}
					}
					synchronized (mLock) {
						results.values = filterResultsData;
						results.count = filterResultsData.size();
					}
				}

				return results;
			}

			@Override
			protected void publishResults(CharSequence charSequence,
					FilterResults filterResults) {
				applicationsToDisplay = (ArrayList<AppEntry>) filterResults.values;
				notifyDataSetChanged();
			}
		};
	}

	@Override
	public boolean isEmpty() {
		return applicationsToDisplay != null
				&& applicationsToDisplay.size() == 0;
	}
}