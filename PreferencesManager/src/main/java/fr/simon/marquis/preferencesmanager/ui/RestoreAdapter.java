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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Collections;
import java.util.List;

import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.Backup;

public class RestoreAdapter extends BaseAdapter {

    private Context ctx;
    private List<Backup> backups;
    private String fullPath;
    private DateFormat format;
    private RestoreDialogFragment.OnRestoreFragmentInteractionListener listener;

    public RestoreAdapter(Context ctx, List<Backup> backups, RestoreDialogFragment.OnRestoreFragmentInteractionListener listener, String fullPath) {
        Collections.sort(backups);
        this.ctx = ctx;
        this.backups = backups;
        this.listener = listener;
        this.fullPath = fullPath;
        this.format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
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
            holder = new ViewHolder();
            holder.label = (TextView) convertView.findViewById(R.id.item_label);
            holder.delete = (ImageButton) convertView.findViewById(R.id.item_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Backup backup = backups.get(position);
        holder.label.setText(backup.getDisplayLabel(ctx));
        //holder.label.setText(format.format(new Date(backup.getTime())));
       /* holder.label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRestoreFile(backup);
            }
        });*/
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backups = listener.onDeleteBackup(backup, fullPath);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        private ImageButton delete;
        private TextView label;
    }
}
