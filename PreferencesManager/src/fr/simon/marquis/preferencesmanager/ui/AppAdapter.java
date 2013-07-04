package fr.simon.marquis.preferencesmanager.ui;

import java.util.ArrayList;
import java.util.Collections;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;

import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.AppEntry;
import fr.simon.marquis.preferencesmanager.util.MyComparator;

public class AppAdapter extends BaseAdapter implements StickyListHeadersAdapter {
	private LayoutInflater layoutInflater;
	private Context ctx;
	private Pattern pattern;
	private int color;
	private ArrayList<AppEntry> applications;

	public AppAdapter(Context ctx, ArrayList<AppEntry> applications) {
		this.applications = applications;
		this.layoutInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.ctx = ctx;
		this.color = ctx.getResources().getColor(R.color.blue);
	}

	@Override
	public void notifyDataSetChanged() {
		Collections.sort(applications, new MyComparator());
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

		AppEntry item = applications.get(position);

		holder.textView.setText(createSpannable(item.getLabel()));
		holder.imageView.setImageDrawable(item.getIcon(ctx));

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
		String headerText = String.valueOf(applications.get(position)
				.getHeaderChar());
		holder.text.setText(headerText);
		return convertView;
	}

	class HeaderViewHolder {
		TextView text;
	}

	@Override
	public long getHeaderId(int position) {
		return applications.get(position).getHeaderChar();
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
		return applications == null ? 0 : applications.size();
	}

	@Override
	public Object getItem(int position) {
		return applications.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

}