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
import com.actionbarsherlock.view.MenuItem;

import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.Files;
import fr.simon.marquis.preferencesmanager.ui.PreferencesFragment.OnFragmentInteractionListener;
import fr.simon.marquis.preferencesmanager.util.Utils;

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
		boolean fav = Utils.isFavorite(packageName, this);
		menu.findItem(R.id.action_fav)
				.setIcon(
						fav ? R.drawable.ic_action_star_10
								: R.drawable.ic_action_star_0)
				.setTitle(fav ? R.string.action_unfav : R.string.action_fav);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_fav:
			Utils.setFavorite(packageName,
					!Utils.isFavorite(packageName, this), this);
			supportInvalidateOptionsMenu();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			PreferencesFragment fragment = PreferencesFragment.newInstance(
					null, null);
			Bundle args = new Bundle();
			args.putString(PreferencesFragment.ARG_NAME, files.get(position)
					.getName());
			args.putString(PreferencesFragment.ARG_PATH, files.get(position)
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
