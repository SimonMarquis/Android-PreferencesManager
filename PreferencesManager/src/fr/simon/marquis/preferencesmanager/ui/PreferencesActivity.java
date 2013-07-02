package fr.simon.marquis.preferencesmanager.ui;

import org.json.JSONArray;
import org.json.JSONException;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.Files;
import fr.simon.marquis.preferencesmanager.ui.PreferenceFragment.OnFragmentInteractionListener;

public class PreferencesActivity extends SherlockFragmentActivity implements
		OnFragmentInteractionListener {

	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;

	Files files;
	String packageName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);

		Bundle b = getIntent().getExtras();
		if (b == null) {
			finish();
			return;
		}
		
		try {
			files = Files.fromJSON(new JSONArray(b.getString("FILES")));
			getSupportActionBar().setTitle(b.getString("TITLE"));
			packageName = b.getString("PACKAGE_NAME");
		} catch (JSONException e) {
			finish();
			return;
		}

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.preferences, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		return super.onPrepareOptionsMenu(menu);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			PreferenceFragment fragment = PreferenceFragment.newInstance(null,
					null);
			Bundle args = new Bundle();
			args.putString(PreferenceFragment.ARG_NAME, files.get(position)
					.getName());
			args.putString(PreferenceFragment.ARG_PATH, files.get(position)
					.getPath());
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return files.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return files.get(position).getName();
		}
	}

	@Override
	public void onFragmentInteraction(Uri uri) {

	}

}
