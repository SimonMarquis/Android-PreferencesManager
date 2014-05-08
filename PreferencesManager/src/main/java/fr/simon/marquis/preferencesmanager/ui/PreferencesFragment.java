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
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map.Entry;

import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.PreferenceFile;
import fr.simon.marquis.preferencesmanager.model.PreferenceSortType;
import fr.simon.marquis.preferencesmanager.model.PreferenceType;
import fr.simon.marquis.preferencesmanager.util.Ui;
import fr.simon.marquis.preferencesmanager.util.Utils;

public class PreferencesFragment extends Fragment {
    private static final int CODE_EDIT_FILE = 666;

    public static final String ARG_FILE = "FILE";
    public static final String ARG_PACKAGE_NAME = "PACKAGE_NAME";

    private String mFile;
    private String mPackageName;

    private SearchView mSearchView;

    public PreferenceFile preferenceFile;

    private OnPreferenceFragmentInteractionListener mListener;

    private GridView gridView;
    private View loadingView, emptyView;
    private TextView emptyViewText;

    public static PreferencesFragment newInstance(String paramFile, String paramPackageName) {
        PreferencesFragment fragment = new PreferencesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILE, paramFile);
        args.putString(ARG_PACKAGE_NAME, paramPackageName);
        fragment.setArguments(args);
        return fragment;
    }

    public PreferencesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFile = getArguments().getString(ARG_FILE);
            mPackageName = getArguments().getString(ARG_PACKAGE_NAME);
        }

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preferences, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadingView = view.findViewById(R.id.loadingView);
        emptyView = view.findViewById(R.id.emptyView);
        emptyViewText = (TextView) view.findViewById(R.id.emptyViewText);
        gridView = (GridView) view.findViewById(R.id.gridView);
        gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);

        updateFilter(null);

        if (preferenceFile == null) {
            launchTask();
        } else {
            updateListView(preferenceFile, false);
        }
    }

    private void launchTask() {
        ParsingTask task = new ParsingTask(mFile);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.preferences_fragment, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setQueryHint(getString(R.string.action_search_preference));
        mSearchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                Ui.hideSoftKeyboard(getActivity(), mSearchView);
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (mSearchView.hasFocus()) {
                    return updateFilter(s);
                } else {
                    return false;
                }
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem arg0) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem arg0) {
                return updateFilter(null);
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_add).setEnabled(preferenceFile != null && preferenceFile.isValidPreferenceFile());
        menu.findItem(R.id.action_add).setIcon(preferenceFile != null && preferenceFile.isValidPreferenceFile() ? R.drawable.ic_action_add : R.drawable.ic_action_add_disabled);
        MenuItem sortAlpha = menu.findItem(R.id.action_sort_alpha);
        MenuItem sortType = menu.findItem(R.id.action_sort_type);
        sortAlpha.setChecked(false);
        sortType.setChecked(false);
        if (PreferencesActivity.preferenceSortType == PreferenceSortType.ALPHANUMERIC) {
            sortAlpha.setChecked(true);
        } else if (PreferencesActivity.preferenceSortType == PreferenceSortType.TYPE_AND_ALPHANUMERIC) {
            sortType.setChecked(true);
        }
        menu.findItem(R.id.action_restore_file).setVisible(mListener != null && mListener.canRestoreFile(mFile));
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_int:
                showPrefDialog(PreferenceType.INT);
                return true;
            case R.id.action_add_boolean:
                showPrefDialog(PreferenceType.BOOLEAN);
                return true;
            case R.id.action_add_string:
                showPrefDialog(PreferenceType.STRING);
                return true;
            case R.id.action_add_float:
                showPrefDialog(PreferenceType.FLOAT);
                return true;
            case R.id.action_add_long:
                showPrefDialog(PreferenceType.LONG);
                return true;
            case R.id.action_add_stringset:
                showPrefDialog(PreferenceType.STRINGSET);
                return true;
            case R.id.action_edit_file:
                if (preferenceFile == null) {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                }
                Intent intent = new Intent(getActivity(), FileEditorActivity.class);
                intent.putExtra(ARG_FILE, mFile);
                intent.putExtra(ARG_PACKAGE_NAME, mPackageName);
                startActivityForResult(intent, CODE_EDIT_FILE);
                return true;
            case R.id.action_sort_alpha:
                setSortType(PreferenceSortType.ALPHANUMERIC);
                return true;
            case R.id.action_sort_type:
                setSortType(PreferenceSortType.TYPE_AND_ALPHANUMERIC);
                return true;
            case R.id.action_backup_file:
                if (mListener != null) {
                    mListener.onBackupFile(mFile);
                }
                return true;
            case R.id.action_restore_file:
                restoreBackup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void restoreBackup() {
        if (mListener != null) {
            RestoreDialogFragment.show(this, getFragmentManager(), mFile, mListener.getBackups(mFile));
        }
    }

    private boolean updateFilter(String s) {
        String filter = !TextUtils.isEmpty(s) ? s.trim() : null;
        PreferenceAdapter adapter = ((PreferenceAdapter) gridView.getAdapter());
        if (adapter == null) {
            return false;
        }
        adapter.setFilter(filter);
        adapter.getFilter().filter(filter);
        return true;
    }

    private void setSortType(PreferenceSortType type) {
        if (PreferencesActivity.preferenceSortType != type) {
            PreferencesActivity.preferenceSortType = type;
            if (getActivity() != null) {
                getActivity().invalidateOptionsMenu();
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(PreferencesActivity.KEY_SORT_TYPE, type.ordinal()).commit();
            }

            if (gridView.getAdapter() != null && preferenceFile != null) {
                preferenceFile.updateSort();
                ((PreferenceAdapter) gridView.getAdapter()).notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_EDIT_FILE && resultCode == ActionBarActivity.RESULT_OK) {
            loadingView.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);

            if (getActivity() != null) {
                Animation fadeInAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
                if (fadeInAnim != null) {
                    loadingView.startAnimation(fadeInAnim);
                }
                Animation fadeOutAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
                if (fadeOutAnim != null) {
                    gridView.startAnimation(fadeOutAnim);
                }
            }
            launchTask();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showPrefDialog(PreferenceType type) {
        showPrefDialog(type, false, null, null);
    }

    private void showPrefDialog(PreferenceType type, boolean editMode, String key, Object obj) {
        PreferenceDialog newFragment = PreferenceDialog.newInstance(type, editMode, key, obj);
        newFragment.setTargetFragment(this, ("Fragment:" + mFile).hashCode());
        FragmentManager fm = getFragmentManager();
        if (fm != null) {
            newFragment.show(fm, mFile + "#" + key);
        }
    }

    public void addPrefKeyValue(String previousKey, String newKey, Object value, boolean editMode) {
        if (preferenceFile == null) {
            return;
        }
        preferenceFile.add(previousKey, newKey, value, editMode);
        Utils.savePreferences(preferenceFile, mFile, mPackageName, getActivity());
        ((PreferenceAdapter) gridView.getAdapter()).notifyDataSetChanged();
    }

    public void deletePref(String key) {
        if (preferenceFile == null) {
            return;
        }
        preferenceFile.removeValue(key);
        Utils.savePreferences(preferenceFile, mFile, mPackageName, getActivity());
        ((PreferenceAdapter) gridView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnPreferenceFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnPreferenceFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    void updateListView(PreferenceFile p, boolean animate) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (p == null) {
            getActivity().finish();
            return;
        }
        preferenceFile = p;
        emptyViewText.setText(preferenceFile.isValidPreferenceFile() ? R.string.empty_preference_file_valid : R.string.empty_preference_file_invalid);
        loadingView.setVisibility(View.GONE);
        gridView.setVisibility(View.VISIBLE);

        if (animate) {
            if (getActivity() != null) {
                Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
                if (fadeOut != null) {
                    loadingView.startAnimation(fadeOut);
                }
                Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
                if (fadeIn != null) {
                    gridView.startAnimation(fadeIn);
                }
            }
        }

        gridView.setAdapter(new PreferenceAdapter(getActivity(), this));
        gridView.setEmptyView(emptyView);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                Entry<String, Object> item = (Entry<String, Object>) gridView.getAdapter().getItem(arg2);
                PreferenceType type = PreferenceType.fromObject(item.getValue());
                if (type == PreferenceType.UNSUPPORTED) {
                    Toast.makeText(getActivity(), R.string.preference_unsupported, Toast.LENGTH_SHORT).show();
                } else {
                    showPrefDialog(type, true, item.getKey(), item.getValue());
                }
            }
        });
        gridView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                ((PreferenceAdapter) gridView.getAdapter()).itemCheckedStateChanged(position, checked);
                mode.setTitle(Html.fromHtml("<b>" + gridView.getCheckedItemCount() + "</b>"));
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        ((PreferenceAdapter) gridView.getAdapter()).deleteSelection();
                        Utils.savePreferences(preferenceFile, mFile, mPackageName, getActivity());
                        ((PreferenceAdapter) gridView.getAdapter()).notifyDataSetChanged();
                        mode.finish();
                        return true;
                    case R.id.action_select_all:
                        boolean check = gridView.getCheckedItemCount() != gridView.getCount();
                        for (int i = 0; i < gridView.getCount(); i++) {
                            gridView.setItemChecked(i, check);
                        }
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                if (inflater != null) {
                    inflater.inflate(R.menu.cab, menu);
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                ((PreferenceAdapter) gridView.getAdapter()).resetSelection();
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

        });
        getActivity().invalidateOptionsMenu();
    }

    public interface OnPreferenceFragmentInteractionListener {

        public void onBackupFile(String fullPath);

        public boolean canRestoreFile(String fullPath);

        public List<String> getBackups(String fullPath);
    }

    public class ParsingTask extends AsyncTask<Void, Void, PreferenceFile> {
        private final String mFile;

        public ParsingTask(String file) {
            super();
            this.mFile = file;
        }

        @Override
        protected PreferenceFile doInBackground(Void... params) {
            long start = System.currentTimeMillis();
            Log.d(Utils.TAG, "Start reading " + mFile);
            String content = Utils.readFile(mFile);
            Log.d(Utils.TAG, "End reading " + mFile + " --> " + (System.currentTimeMillis() - start) + " ms");
            return PreferenceFile.fromXml(content);
        }

        @Override
        protected void onPostExecute(PreferenceFile result) {
            super.onPostExecute(result);
            updateListView(result, true);
        }

    }

}
