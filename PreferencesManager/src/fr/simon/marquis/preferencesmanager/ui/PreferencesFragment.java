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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import android.widget.ListView;
import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.PreferenceFile;
import fr.simon.marquis.preferencesmanager.model.PreferenceType;
import fr.simon.marquis.preferencesmanager.util.Utils;

public class PreferencesFragment extends Fragment {
	public static final String ARG_NAME = "NAME";
	public static final String ARG_PATH = "PATH";

	private String mName;
	private String mPath;

	public PreferenceFile preferenceFile;

	private OnFragmentInteractionListener mListener;

	private ListView listView;
	private View loadingView, emptyView;

	public static PreferencesFragment newInstance(String paramName,
			String paramPath) {
		PreferencesFragment fragment = new PreferencesFragment();
		Bundle args = new Bundle();
		args.putString(ARG_NAME, paramName);
		args.putString(ARG_PATH, paramPath);
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
		listView = (ListView) view.findViewById(R.id.listView);

		if (preferenceFile == null) {
			ParsingTask task = new ParsingTask(mPath + "/" + mName);
			if (Utils.hasHONEYCOMB()) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				task.execute();
			}
		} else {
			updateListView(preferenceFile, false);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.preferences_fragment, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showPrefDialog(PreferenceType type) {
		DialogFragment newFragment = AddPreferenceDialog.newInstance(type);
		//efficace ?
		newFragment.setTargetFragment(this, ("Fragment:"+mPath+"/"+mName).hashCode());
		newFragment.show(getFragmentManager(), "dialog");
	}

	public void addPrefKeyValue(String key, Object value) {
		preferenceFile.add(key, value);
		((PreferenceAdapter) listView.getAdapter()).notifyDataSetChanged();
		preferenceFile.save(mPath+"/"+mName, getActivity());
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
		preferenceFile = p;
		loadingView.setVisibility(View.GONE);
		if (animate) {
			loadingView.startAnimation(AnimationUtils.loadAnimation(
					getActivity(), android.R.anim.fade_out));
			listView.startAnimation(AnimationUtils.loadAnimation(getActivity(),
					android.R.anim.fade_in));
		}
		listView.setAdapter(new PreferenceAdapter(getActivity(), this));
		listView.setEmptyView(emptyView);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO
				Log.e("", ((Entry<String, Object>) listView.getAdapter()
						.getItem(arg2)).getValue().toString());
			}
		});
	}

	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
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
			// Utils.debugFile(mFile);
			return PreferenceFile.fromXml(App.getRoot().file.read(mFile)
					.toString());
		}

		@Override
		protected void onPostExecute(PreferenceFile result) {
			super.onPostExecute(result);
			// App.getRoot().file.read(mPath + "/" + mName).toString();
			// preferenceFile.toXml();
			updateListView(result, true);
		}

	}

}
