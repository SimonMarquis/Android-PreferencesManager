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
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
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
import fr.simon.marquis.preferencesmanager.model.PreferenceFile;

public class RestoreDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    private final static String TAG = "RestoreDialogFragment";
    private static final String ARG_FULL_PATH = "FULL_PATH";
    private static final String ARG_BACKUPS = "BACKUPS";

    private OnRestoreFragmentInteractionListener listener;
    private List<String> backups;
    private String mFullPath;

    public static void show(PreferencesFragment target, FragmentManager fm, String fullPath, List<String> backups) {
        dismiss(fm);
        RestoreDialogFragment restoreDialogFragment = RestoreDialogFragment.newInstance(fullPath, backups);
        restoreDialogFragment.setTargetFragment(target, ("Fragment:" + fullPath).hashCode());
        restoreDialogFragment.show(fm, TAG);

    }

    private static RestoreDialogFragment newInstance(String fullPath, List<String> backups) {
        RestoreDialogFragment dialog = new RestoreDialogFragment();
        Bundle args = new Bundle();
        JSONArray array = new JSONArray(backups);
        args.putString(ARG_FULL_PATH, fullPath);
        args.putString(ARG_BACKUPS, array.toString());
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFullPath = getArguments().getString(ARG_FULL_PATH);
            backups = new ArrayList<>();
            try {
                JSONArray array = new JSONArray(getArguments().getString(ARG_BACKUPS));
                for (int i = 0; i < array.length(); i++) {
                    String backup = array.optString(i);
                    if (!TextUtils.isEmpty(backup)) {
                        backups.add(backup);
                    }
                }
            } catch (JSONException ignore) {
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getActivity() == null) {
            return null;
        }
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_restore, null);
        assert view != null;
        ListView listView = view.findViewById(R.id.listView);
        listView.setAdapter(new RestoreAdapter(getActivity(), this, backups, listener, mFullPath));
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

    private static void dismiss(FragmentManager fm) {
        RestoreDialogFragment dialog = find(fm);
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private static RestoreDialogFragment find(FragmentManager fm) {
        return (RestoreDialogFragment) fm.findFragmentByTag(TAG);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (listener != null) {
            String data = listener.onRestoreFile(backups.get(position), mFullPath);
            PreferencesFragment fragment = (PreferencesFragment) getTargetFragment();
            if (fragment != null) {
                fragment.updateListView(PreferenceFile.fromXml(data), true);
            }
            dismiss(getFragmentManager());
        }
    }

    public void noMoreBackup() {
        dismiss(getFragmentManager());
        PreferencesFragment fragment = (PreferencesFragment) getTargetFragment();
        if (fragment != null) {
            fragment.getActivity().invalidateOptionsMenu();
        }
    }


    public interface OnRestoreFragmentInteractionListener {
        String onRestoreFile(String backup, String fullPath);

        List<String> onDeleteBackup(String backup, String fullPath);
    }
}