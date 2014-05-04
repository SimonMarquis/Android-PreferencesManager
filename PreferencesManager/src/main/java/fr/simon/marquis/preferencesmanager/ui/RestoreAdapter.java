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
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import fr.simon.marquis.preferencesmanager.R;

public class RestoreAdapter extends BaseAdapter {

    private final Context ctx;
    private final String fullPath;
    private final RestoreDialogFragment.OnRestoreFragmentInteractionListener listener;
    private final RestoreDialogFragment dialog;
    private List<String> backups;

    public RestoreAdapter(Context ctx, RestoreDialogFragment dialog, List<String> backups, RestoreDialogFragment.OnRestoreFragmentInteractionListener listener, String fullPath) {
        Collections.sort(backups);
        this.ctx = ctx;
        this.backups = backups;
        this.listener = listener;
        this.dialog = dialog;
        this.fullPath = fullPath;
    }

    @Override
    public int getCount() {
        return backups.size();
    }

    @Override
    public Object getItem(int position) {
        return backups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.row_restore, parent, false);
            assert convertView != null;
            holder = new ViewHolder();
            holder.label = (TextView) convertView.findViewById(R.id.item_label);
            holder.delete = (ImageButton) convertView.findViewById(R.id.item_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final String backup = backups.get(position);
        holder.label.setText(getDisplayLabel(ctx, Long.valueOf(backup)));
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backups = listener.onDeleteBackup(backup, fullPath);
                if(backups == null || backups.isEmpty()){
                    dialog.noMoreBackup();
                } else {
                    notifyDataSetChanged();
                }
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        private ImageButton delete;
        private TextView label;
    }

    public String getDisplayLabel(Context ctx, long time) {
        return upperFirstLetter(DateUtils.formatDateTime(ctx, time, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY)) + " (" + lowerFirstLetter(DateUtils.getRelativeTimeSpanString(time, new Date().getTime(), DateUtils.SECOND_IN_MILLIS).toString()) + ")";
    }

    private String upperFirstLetter(String original) {
        if (original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    private String lowerFirstLetter(String original) {
        if (original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toLowerCase() + original.substring(1);
    }
}
