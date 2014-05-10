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
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.PreferenceType;
import fr.simon.marquis.preferencesmanager.util.Ui;

public class PreferenceAdapter extends BaseAdapter implements Filterable {

    private final LayoutInflater layoutInflater;
    private final PreferencesFragment mPreferencesFragment;
    private final int color;
    private final Object mLock = new Object();
    private final Map<Entry<String, Object>, Boolean> mCheckedPositions;
    private Pattern pattern;
    private List<Entry<String, Object>> mListToDisplay;

    public PreferenceAdapter(Context ctx, PreferencesFragment f) {
        this.layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mPreferencesFragment = f;
        this.color = ctx.getResources().getColor(R.color.blue);
        this.mCheckedPositions = new HashMap<Entry<String, Object>, Boolean>();
        this.mListToDisplay = mPreferencesFragment.preferenceFile.getList();
    }

    @Override
    public int getCount() {
        return mListToDisplay.size();
    }

    @Override
    public Object getItem(int position) {
        return mListToDisplay.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.row_preference, parent, false);
            assert convertView != null;
            holder = new ViewHolder();
            holder.background = convertView;
            holder.name = (TextView) convertView.findViewById(R.id.item_name);
            holder.value = (TextView) convertView.findViewById(R.id.item_value);
            holder.selector = (LinearLayout) convertView.findViewById(R.id.item_selector);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Entry<String, Object> item = mListToDisplay.get(position);
        Boolean checked = mCheckedPositions.get(item);
        holder.background.setBackgroundResource(PreferenceType.getDialogLayout(item.getValue()));
        holder.name.setText(Ui.createSpannable(pattern, color, item.getKey()));
        holder.value.setText((item.getValue() == null ? null : Ui.createSpannable(pattern, color, truncate(item.getValue().toString(), 100))));
        holder.selector.setBackgroundResource((checked != null && checked) ? R.drawable.list_focused : R.drawable.selectable_background);

        return convertView;
    }

    private static class ViewHolder {
        private View background;
        private TextView name;
        private TextView value;
        private LinearLayout selector;
    }

    public static String truncate(String str, int length) {
        return str != null && str.length() > length ? str.substring(0, length) : str;
    }

    public void setFilter(String filter) {
        pattern = TextUtils.isEmpty(filter) ? null : Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();
                if (charSequence == null || charSequence.length() == 0) {
                    synchronized (mLock) {
                        results.values = mPreferencesFragment.preferenceFile.getList();
                        results.count = mPreferencesFragment.preferenceFile.getList().size();
                    }
                } else {
                    String prefixString = charSequence.toString().toLowerCase(Locale.getDefault()).trim();
                    ArrayList<Entry<String, Object>> filterResultsData = new ArrayList<Entry<String, Object>>();
                    synchronized (mLock) {
                        for (Entry<String, Object> data : mPreferencesFragment.preferenceFile.getList()) {
                            Pattern p = Pattern.compile(prefixString, Pattern.CASE_INSENSITIVE);
                            if (p.matcher(data.getKey().toLowerCase(Locale.getDefault()).trim()).find() || (data.getValue() != null && p.matcher(data.getValue().toString().toLowerCase(Locale.getDefault()).trim()).find())) {
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
                mListToDisplay = (List<Entry<String, Object>>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void resetSelection() {
        mCheckedPositions.clear();
        notifyDataSetChanged();
    }

    public void itemCheckedStateChanged(int position, boolean checked) {
        mCheckedPositions.put(mListToDisplay.get(position), checked);
        super.notifyDataSetChanged();
    }

    public void deleteSelection() {
        ArrayList<Entry<String, Object>> temp = new ArrayList<Entry<String, Object>>();
        for (Entry<String, Object> item : mListToDisplay) {
            if (!mCheckedPositions.containsKey(item) || !mCheckedPositions.get(item)) {
                mCheckedPositions.remove(item);
                temp.add(item);
            }
        }
        mPreferencesFragment.preferenceFile.setList(temp);
        mListToDisplay = temp;
    }

    public void setSelection(SparseBooleanArray checkedItemPositions) {
        mCheckedPositions.clear();
        for (int i = 0; i < checkedItemPositions.size(); i++) {
            mCheckedPositions.put(mListToDisplay.get(checkedItemPositions.keyAt(i)), checkedItemPositions.valueAt(i));
        }
        super.notifyDataSetChanged();
    }
}
