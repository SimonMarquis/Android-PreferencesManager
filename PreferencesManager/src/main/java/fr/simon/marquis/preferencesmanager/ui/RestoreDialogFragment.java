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

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.Backup;

public class RestoreDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    private final static String TAG = "RestoreDialogFragment";
    private static final String ARG_BACKUPS = "BACKUPS";

    private OnRestoreFragmentInteractionListener listener;
    private List<Backup> backups;
    private ListView listView;

    public static RestoreDialogFragment newInstance(List<Backup> backups) {
        RestoreDialogFragment dialog = new RestoreDialogFragment();
        Bundle args = new Bundle();
        JSONArray array = new JSONArray();
        for (Backup backup : backups) {
            array.put(backup.toJSON());
        }
        args.putString(ARG_BACKUPS, array.toString());
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            backups = new ArrayList<Backup>();
            try {
                JSONArray array = new JSONArray(getArguments().getString(ARG_BACKUPS));
                for (int i = 0; i < array.length(); i++) {
                    Backup backup = Backup.fromJSON(array.optJSONObject(i));
                    if (backup != null) {
                        backups.add(backup);
                    }
                }
            } catch (JSONException ignore) {
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_restore, null);
        listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(new RestoreAdapter(getActivity(), backups, listener));
        listView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.pick_restore);
        return dialog;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnRestoreFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnRestoreFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public static void show(FragmentManager fm, List<Backup> backups) {
        dismiss(fm);
        RestoreDialogFragment.newInstance(backups).show(fm, TAG);
    }

    public static void dismiss(android.app.FragmentManager fm) {
        RestoreDialogFragment dialog = find(fm);
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private static final RestoreDialogFragment find(android.app.FragmentManager fm) {
        return (RestoreDialogFragment) fm.findFragmentByTag(TAG);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (listener != null) {
            listener.onRestoreFile(backups.get(position));
        }
    }


    public interface OnRestoreFragmentInteractionListener {
        public void onRestoreFile(Backup backup);

        public void onDeleteBackup(Backup backup);
    }
}