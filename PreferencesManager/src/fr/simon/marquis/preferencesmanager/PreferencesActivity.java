package fr.simon.marquis.preferencesmanager;

import org.json.JSONArray;
import org.json.JSONException;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import fr.simon.marquis.preferencesmanager.PreferenceFragment.OnFragmentInteractionListener;

public class PreferencesActivity extends SherlockFragmentActivity implements
		OnFragmentInteractionListener {

	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;
	
	Files files;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);
		
		String str = getIntent().getExtras().getString("FILES");
		try {
			files = Files.fromJSON(new JSONArray(str));
		} catch (JSONException e) {
			finish();
			//Error
		}

//		Log.e("",Arrays.toString(files));

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

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			PreferenceFragment fragment = PreferenceFragment.newInstance(null,
					null);
			Bundle args = new Bundle();
			args.putString(PreferenceFragment.ARG_NAME, files.get(position).name);
			args.putString(PreferenceFragment.ARG_PATH, files.get(position).path);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return files.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return files.get(position).name;
		}
	}

	@Override
	public void onFragmentInteraction(Uri uri) {

	}

}
