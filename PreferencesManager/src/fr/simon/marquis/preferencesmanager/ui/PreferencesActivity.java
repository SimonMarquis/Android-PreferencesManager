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

import org.json.JSONArray;
import org.json.JSONException;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.Files;
import fr.simon.marquis.preferencesmanager.ui.PreferencesFragment.OnFragmentInteractionListener;
import fr.simon.marquis.preferencesmanager.util.Utils;

public class PreferencesActivity extends FragmentActivity implements
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
//			long start = System.currentTimeMillis();
//			HashMap<String, PreferenceFile> _prefs = new HashMap<String, PreferenceFile>();
//			for(int i = 0; i < files.size(); i++) {
//				String _title = files.get(i).getPath()  + "/" + files.get(i).getName();
//				_prefs.put(_title, PreferenceFile.fromXml(App.getRoot().file.read(_title).toString()));
//			}
//			Log.e("",(System.currentTimeMillis() - start) + "ms to read all files");
			
			getActionBar().setTitle(b.getString("TITLE"));
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.preferences_activity, menu);
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
