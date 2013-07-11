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
import java.util.HashSet;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fr.simon.marquis.preferencesmanager.R;

public class PreferenceAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;
	private PreferencesFragment mPreferencesFragment;

	// private PreferenceFile mPreferenceFile;

	public PreferenceAdapter(Context ctx, PreferencesFragment f) {
		this.layoutInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mPreferencesFragment = f;
	}

	@Override
	public int getCount() {
		return mPreferencesFragment.preferenceFile.getList().size();
	}

	@Override
	public Object getItem(int position) {
		return mPreferencesFragment.preferenceFile.getList().get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.row_preference,
					parent, false);
			holder = new ViewHolder();
			holder.thumb = (View) convertView.findViewById(R.id.item_thumb);
			holder.name = (TextView) convertView.findViewById(R.id.item_name);
			holder.value = (TextView) convertView.findViewById(R.id.item_value);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Entry<String, Object> item = mPreferencesFragment.preferenceFile
				.getList().get(position);
		Object obj = item.getValue();
		int color = Color.BLACK;
		if (obj instanceof String) {
			color = 0xff33B5E5;
		} else if (obj instanceof Integer) {
			color = 0xff99CC00;
		} else if (obj instanceof Long) {
			color = 0xffFFBB33;
		} else if (obj instanceof Float) {
			color = 0xffFF8800;
		} else if (obj instanceof Double) {
			color = 0xff669900;
		} else if (obj instanceof Boolean) {
			color = 0xffAA66CC;
		} else if (obj instanceof int[]) {
			color = 0xffFF4444;
		} else if (obj instanceof ArrayList) {
			color = 0xffCC0000;
		} else if (obj instanceof HashSet) {
			color = 0xffcccccc;
		}
		holder.thumb.setBackgroundColor(color);
		holder.name.setText(item.getKey());
		holder.value.setText((item.getValue() == null ? null : item.getValue()
				.toString()));

		return convertView;
	}

	class ViewHolder {
		private View thumb;
		private TextView name;
		private TextView value;
	}
}
