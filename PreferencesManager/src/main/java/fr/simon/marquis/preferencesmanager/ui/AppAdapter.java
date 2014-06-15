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

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Pattern;

import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.AppEntry;
import fr.simon.marquis.preferencesmanager.util.MyComparator;
import fr.simon.marquis.preferencesmanager.util.Ui;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class AppAdapter extends BaseAdapter implements StickyListHeadersAdapter, Filterable {
    private final LayoutInflater layoutInflater;
    private final Context context;
    private final ArrayList<AppEntry> applications;
    private final int color;
    private final View emptyView;
    private final Object mLock = new Object();

    private Pattern pattern;
    private ArrayList<AppEntry> applicationsToDisplay;

    public AppAdapter(Context ctx, ArrayList<AppEntry> applications, View emptyView) {
        this.context = ctx;
        this.applications = applications;
        this.applicationsToDisplay = applications;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.color = context.getResources().getColor(R.color.blue);
        this.emptyView = emptyView;
        updateEmptyView();
    }

    @Override
    public void notifyDataSetChanged() {
        synchronized (mLock) {
            Collections.sort(applicationsToDisplay, new MyComparator());
        }
        updateEmptyView();
        super.notifyDataSetChanged();
    }

    private void updateEmptyView() {
        if (isEmpty()) {
            if (emptyView.getVisibility() == View.GONE) {
                Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
                if (animation != null) {
                    emptyView.startAnimation(animation);
                }
            }
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.row_application, parent, false);
            assert convertView != null;
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.item_text);
            holder.imageView = (ImageView) convertView.findViewById(R.id.item_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppEntry item = applicationsToDisplay.get(position);
        holder.textView.setText(Ui.createSpannable(pattern, color, item.getLabel()));

        Picasso.with(context).load(item.getIconUri()).error(R.drawable.ic_action_settings).into(holder.imageView);

        return convertView;
    }

    private static class ViewHolder {
        private ImageView imageView;
        private TextView textView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = layoutInflater.inflate(R.layout.row_header, parent, false);
            assert convertView != null;
            holder.text = (TextView) convertView.findViewById(R.id.text_header);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        String headerText = String.valueOf(applicationsToDisplay.get(position).getHeaderChar());
        holder.text.setText(headerText);
        return convertView;
    }

    private static class HeaderViewHolder {
        TextView text;
    }

    @Override
    public long getHeaderId(int position) {
        return applicationsToDisplay.get(position).getHeaderChar();
    }

    public void setFilter(String filter) {
        if (TextUtils.isEmpty(filter)) {
            pattern = null;
        } else {
            pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
        }
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
                    String prefixString = charSequence.toString().toLowerCase(Locale.getDefault()).trim();
                    ArrayList<AppEntry> filterResultsData = new ArrayList<AppEntry>();
                    synchronized (mLock) {
                        for (AppEntry data : applications) {
                            Pattern p = Pattern.compile(prefixString, Pattern.CASE_INSENSITIVE);
                            if (p.matcher(data.getLabel().toLowerCase(Locale.getDefault()).trim()).find()) {
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

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                applicationsToDisplay = (ArrayList<AppEntry>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public boolean isEmpty() {
        return applicationsToDisplay != null && applicationsToDisplay.size() == 0;
    }
}