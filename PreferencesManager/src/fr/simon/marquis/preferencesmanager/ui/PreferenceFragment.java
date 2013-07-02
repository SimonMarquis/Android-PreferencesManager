package fr.simon.marquis.preferencesmanager.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.PreferenceFile;

public class PreferenceFragment extends Fragment {
	public static final String ARG_NAME = "NAME";
	public static final String ARG_PATH = "PATH";

	private String mName;
	private String mPath;

	private PreferenceFile preferenceFile;

	private OnFragmentInteractionListener mListener;

	public static PreferenceFragment newInstance(String paramName,
			String paramPath) {
		PreferenceFragment fragment = new PreferenceFragment();
		Bundle args = new Bundle();
		args.putString(ARG_NAME, paramName);
		args.putString(ARG_PATH, paramPath);
		fragment.setArguments(args);
		return fragment;
	}

	public PreferenceFragment() {
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
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_preferences_dummy,
				container, false);
		TextView dummyTextView = (TextView) rootView
				.findViewById(R.id.section_label);

		//FIXME: STACKOVERFLOW if the file is empty U_u
		preferenceFile = PreferenceFile.fromXML(App.getRoot().file.read(
				mPath + "/" + mName).toString());
		dummyTextView.setText(App.getRoot().file.read(mPath + "/" + mName)
				.toString() + "\n\n\n__________________\n\n\n"+preferenceFile.toXML());
		return rootView;
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

}
