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
import fr.simon.marquis.preferencesmanager.model.PreferenceType;

public class PreferenceAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;
	private PreferencesFragment mPreferencesFragment;

	public PreferenceAdapter(Context ctx, PreferencesFragment f) {
		this.layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			convertView = layoutInflater.inflate(R.layout.row_preference, parent, false);
			holder = new ViewHolder();
			holder.background = convertView;
			holder.name = (TextView) convertView.findViewById(R.id.item_name);
			holder.value = (TextView) convertView.findViewById(R.id.item_value);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Entry<String, Object> item = mPreferencesFragment.preferenceFile.getList().get(position);
		holder.background.setBackgroundResource(PreferenceType.getDialogLayout(item.getValue()));
		holder.name.setText(item.getKey());
		holder.value.setText((item.getValue() == null ? null : item.getValue().toString()));

		return convertView;
	}

	class ViewHolder {
		private View background;
		private TextView name;
		private TextView value;
	}
}
