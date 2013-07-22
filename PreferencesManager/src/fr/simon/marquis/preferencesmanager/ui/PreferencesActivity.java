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

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.Files;
import fr.simon.marquis.preferencesmanager.ui.PreferencesFragment.OnFragmentInteractionListener;
import fr.simon.marquis.preferencesmanager.util.Utils;

public class PreferencesActivity extends FragmentActivity implements
		OnFragmentInteractionListener {

	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;
	View mLoadingView;
	View mEmptyView;

	Files files;
	String packageName;

	FindFilesTask findFilesTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle b = getIntent().getExtras();
		if (b == null) {
			finish();
			return;
		}

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mLoadingView = (View) findViewById(R.id.loadingView);
		mEmptyView = (View) findViewById(R.id.emptyView);

		getActionBar().setTitle(b.getString("TITLE"));
		packageName = b.getString("PACKAGE_NAME");

		if (!App.getRoot().connected()) {
			Utils.displayNoRoot(this).show();
		} else {
			findFilesTask = new FindFilesTask(packageName);
			findFilesTask.execute();
		}
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
		case android.R.id.home:
			finish();
			break;
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

	public class FindFilesTask extends AsyncTask<Void, Void, Files> {
		private String mPackageName;

		public FindFilesTask(String packageName) {
			this.mPackageName = packageName;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Files doInBackground(Void... params) {
			return Utils.findXmlFiles(mPackageName);
		}

		@Override
		protected void onPostExecute(Files result) {
			updateFindFiles(result);
			super.onPostExecute(result);
		}
	}
	
	private void updateFindFiles(Files f){
		files = f;
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		if(files == null || files.size() == 0){
			mEmptyView.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
			mEmptyView.setVisibility(View.VISIBLE);
			mLoadingView.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
			mLoadingView.setVisibility(View.GONE);
		} else {
			mEmptyView.setVisibility(View.GONE);
//			mLoadingView.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
			mLoadingView.setVisibility(View.GONE);
			mViewPager.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
			mViewPager.setVisibility(View.VISIBLE);
		}
	}
}
