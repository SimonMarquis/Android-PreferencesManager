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

import java.util.Map.Entry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.PreferenceFile;

public class PreferenceAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;
	private PreferenceFile mPreferenceFile;

	public PreferenceAdapter(Context ctx, PreferenceFile preferenceFile) {
		this.layoutInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mPreferenceFile = preferenceFile;
	}

	@Override
	public int getCount() {
		return mPreferenceFile.getList().size();
	}

	@Override
	public Object getItem(int position) {
		return mPreferenceFile.getList().get(position);
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
			holder = new ViewHolder();
			holder.thumb = (View) convertView.findViewById(R.id.item_thumb);
			holder.name = (TextView) convertView.findViewById(R.id.item_name);
			holder.value = (TextView) convertView.findViewById(R.id.item_value);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Entry<String, Object> item = mPreferenceFile.getList().get(position);

//		holder.thumb.setBackgroundColor(Color.BLUE);
		holder.name.setText(item.getKey());
		holder.value.setText(item.getValue() == null ? null : item.getValue()
				.toString());

		return convertView;
	}

	class ViewHolder {
		private View thumb;
		private TextView name;
		private TextView value;
	}
}
