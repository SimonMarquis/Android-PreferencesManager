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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.spazedog.lib.rootfw.container.Data;

import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.PreferenceFile;
import fr.simon.marquis.preferencesmanager.model.PreferenceType;
import fr.simon.marquis.preferencesmanager.util.Utils;

public class PreferencesFragment extends Fragment {
	public static final int CODE_EDIT_FILE = 666;

	public static final String ARG_NAME = "NAME";
	public static final String ARG_PATH = "PATH";
	public static final String ARG_PACKAGE_NAME = "PACKAGE_NAME";

	private String mName;
	private String mPath;
	private String mPackageName;

	public PreferenceFile preferenceFile;

	private OnFragmentInteractionListener mListener;

	private GridView gridView;
	private View loadingView, emptyView;
	private TextView emptyViewText;

	public static PreferencesFragment newInstance(String paramName,
			String paramPath, String paramPackageName) {
		PreferencesFragment fragment = new PreferencesFragment();
		Bundle args = new Bundle();
		args.putString(ARG_NAME, paramName);
		args.putString(ARG_PATH, paramPath);
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
			mName = getArguments().getString(ARG_NAME);
			mPath = getArguments().getString(ARG_PATH);
			mPackageName = getArguments().getString(ARG_PACKAGE_NAME);
		}

		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater
				.inflate(R.layout.fragment_preferences, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		loadingView = (View) view.findViewById(R.id.loadingView);
		emptyView = (View) view.findViewById(R.id.emptyView);
		emptyViewText = (TextView) view.findViewById(R.id.emptyViewText);
		gridView = (GridView) view.findViewById(R.id.gridView);

		if (preferenceFile == null) {
			launchTask();
		} else {
			updateListView(preferenceFile, false);
		}
	}

	private void launchTask() {
		ParsingTask task = new ParsingTask(mPath + "/" + mName);
		if (Utils.hasHONEYCOMB()) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.preferences_fragment, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_add).setEnabled(
				preferenceFile != null
						&& preferenceFile.isValidPreferenceFile());
		menu.findItem(R.id.action_add)
				.setIcon(
						preferenceFile != null
								&& preferenceFile.isValidPreferenceFile() ? R.drawable.ic_action_add
								: R.drawable.ic_action_add_disabled);
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
				getActivity().finish();
			}
			Intent intent = new Intent(getActivity(), FileEditorActivity.class);
			intent.putExtra(ARG_NAME, mName);
			intent.putExtra(ARG_PATH, mPath);
			intent.putExtra(ARG_PACKAGE_NAME, mPackageName);
			startActivityForResult(intent, CODE_EDIT_FILE);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_EDIT_FILE
				&& resultCode == ActionBarActivity.RESULT_OK) {
			loadingView.setVisibility(View.VISIBLE);
			loadingView.startAnimation(AnimationUtils.loadAnimation(
					getActivity(), android.R.anim.fade_in));
			gridView.setVisibility(View.GONE);
			gridView.startAnimation(AnimationUtils.loadAnimation(getActivity(),
					android.R.anim.fade_out));
			launchTask();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void showPrefDialog(PreferenceType type) {
		showPrefDialog(type, false, null, null);
	}

	private void showPrefDialog(PreferenceType type, boolean editMode,
			String key, Object obj) {
		DialogFragment newFragment = PreferenceDialog.newInstance(type,
				editMode, key, obj);
		newFragment.setTargetFragment(this,
				("Fragment:" + mPath + "/" + mName).hashCode());
		newFragment.show(getFragmentManager(), mPath + "/" + mName + "#" + key);
	}

	public void addPrefKeyValue(String previousKey, String newKey,
			Object value, boolean editMode) {
		if (preferenceFile == null) {
			return;
		}
		preferenceFile.add(previousKey, newKey, value, editMode);
		((PreferenceAdapter) gridView.getAdapter()).notifyDataSetChanged();
		PreferenceFile.saveFast(preferenceFile, mPath + "/" + mName,
				getActivity(), mPackageName);
	}

	public void deletePref(String key) {
		if (preferenceFile == null) {
			return;
		}
		preferenceFile.removeValue(key);
		((PreferenceAdapter) gridView.getAdapter()).notifyDataSetChanged();
		PreferenceFile.saveFast(preferenceFile, mPath + "/" + mName,
				getActivity(), mPackageName);
	}

	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public void updateListView(PreferenceFile p, boolean animate) {
		if (p == null) {
			getActivity().finish();
			return;
		}
		preferenceFile = p;
		emptyViewText
				.setText(preferenceFile.isValidPreferenceFile() ? R.string.empty_preference_file_valid
						: R.string.empty_preference_file_invalid);
		loadingView.setVisibility(View.GONE);
		gridView.setVisibility(View.VISIBLE);
		if (animate) {
			loadingView.startAnimation(AnimationUtils.loadAnimation(
					getActivity(), android.R.anim.fade_out));
			gridView.startAnimation(AnimationUtils.loadAnimation(getActivity(),
					android.R.anim.fade_in));
		}
		gridView.setAdapter(new PreferenceAdapter(getActivity(), this));
		gridView.setEmptyView(emptyView);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				Entry<String, Object> item = (Entry<String, Object>) gridView
						.getAdapter().getItem(arg2);
				PreferenceType type = PreferenceType.fromObject(item.getValue());
				if (type == PreferenceType.UNSUPPORTED) {
					Toast.makeText(getActivity(),
							R.string.preferece_unsupported, Toast.LENGTH_SHORT)
							.show();
				} else {
					showPrefDialog(type, true, item.getKey(), item.getValue());
				}
			}
		});
		getActivity().supportInvalidateOptionsMenu();
	}

	public interface OnFragmentInteractionListener {
		public void onFragmentInteraction(Uri uri);
	}

	public class ParsingTask extends AsyncTask<Void, Void, PreferenceFile> {
		private String mFile;

		public ParsingTask(String file) {
			super();
			this.mFile = file;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected PreferenceFile doInBackground(Void... params) {
			Log.e(Utils.TAG, System.currentTimeMillis() + "\t read start");
			Data data = App.getRoot().file.read(mFile);
			Log.e(Utils.TAG, System.currentTimeMillis() + "\t read end");
			return PreferenceFile
					.fromXml(data == null ? null : data.toString());
		}

		@Override
		protected void onPostExecute(PreferenceFile result) {
			super.onPostExecute(result);
			updateListView(result, true);
		}

	}

}
