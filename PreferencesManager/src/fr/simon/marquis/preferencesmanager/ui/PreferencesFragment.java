package fr.simon.marquis.preferencesmanager.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.PreferenceFile;
import fr.simon.marquis.preferencesmanager.util.Utils;

public class PreferencesFragment extends Fragment {
	public static final String ARG_NAME = "NAME";
	public static final String ARG_PATH = "PATH";

	private String mName;
	private String mPath;

	private PreferenceFile preferenceFile;

	private OnFragmentInteractionListener mListener;

	private ListView listView;

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
		listView = (ListView) view.findViewById(R.id.listView);

		ParsingTask task = new ParsingTask(mPath + "/" + mName);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
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
			Utils.debugFile(mFile);
			return PreferenceFile.fromXml(App.getRoot().file.read(mFile)
					.toString());
		}

		@Override
		protected void onPostExecute(PreferenceFile result) {
			super.onPostExecute(result);
			preferenceFile = result;
			// App.getRoot().file.read(mPath + "/" + mName).toString();
			// preferenceFile.toXml();
			listView.setAdapter(new PreferenceAdapter(getActivity(),
					preferenceFile));
		}

	}

}
