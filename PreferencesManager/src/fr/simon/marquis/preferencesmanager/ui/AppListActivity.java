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

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.AppEntry;
import fr.simon.marquis.preferencesmanager.util.Utils;

public class AppListActivity extends SherlockActivity {
	private static final int REQUEST_CODE = 123456;
	private StickyListHeadersListView listView;
	private View loadingView;
	private GetApplicationsTask task;

	String curFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_list);
		loadingView = findViewById(R.id.loadingView);
		listView = (StickyListHeadersListView) findViewById(R.id.listView);

		startTask();
	}

	/**
	 * @return true if a new task is started
	 */
	private boolean startTask() {
		if (task == null || task.isCancelled()) {
			task = new GetApplicationsTask(this);
			if (Utils.hasHONEYCOMB()) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
						(Void[]) null);
			} else {
				task.execute();
			}
			return true;
		}
		return false;
	}

	private void toggleDisplay(boolean showList) {
		loadingView.startAnimation(AnimationUtils.loadAnimation(this,
				showList ? android.R.anim.fade_out : android.R.anim.fade_in));
		loadingView.setVisibility(showList ? View.GONE : View.VISIBLE);
		listView.startAnimation(AnimationUtils.loadAnimation(this,
				showList ? android.R.anim.fade_in : android.R.anim.fade_out));
		listView.setVisibility(showList ? View.VISIBLE : View.GONE);
	}

	public void updateListView(ArrayList<AppEntry> apps) {
		toggleDisplay(true);
		listView.setAdapter(new AppAdapter(this, apps));
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				AppEntry item = (AppEntry) ((AppAdapter) listView
						.getWrappedAdapter()).getItem(arg2);

				Intent i = new Intent(AppListActivity.this,
						PreferencesActivity.class);
				i.putExtra("TITLE", item.getLabel());
				i.putExtra("PACKAGE_NAME",
						item.getApplicationInfo().packageName);

				startActivityForResult(i, REQUEST_CODE);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE) {
			if (listView != null && listView.getWrappedAdapter() != null) {
				((AppAdapter) listView.getWrappedAdapter())
						.notifyDataSetChanged();
			}
		}
	}

	@Override
	protected void onStart() {
		if (!App.getRoot().connected()) {
			Utils.displayNoRoot(AppListActivity.this).show();
		}
		super.onStart();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.app_list_activity, menu);
		MenuItem itemSearch = menu.add("Search");
		itemSearch.setIcon(android.R.drawable.ic_menu_search);
		itemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		View searchView = SearchViewCompat.newSearchView(getSupportActionBar()
				.getThemedContext());
		if (searchView != null) {
			SearchViewCompat.setOnQueryTextListener(searchView,
					new OnQueryTextListenerCompat() {

						@Override
						public boolean onQueryTextChange(String newText) {
							curFilter = !TextUtils.isEmpty(newText) ? newText
									.trim() : null;
							AppAdapter adapter = ((AppAdapter) listView
									.getWrappedAdapter());
							if (adapter == null) {
								return false;
							}

							adapter.setFilter(curFilter);
							adapter.getFilter().filter(curFilter);
							return true;
						}
					});
			itemSearch.setActionView(searchView);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.show_system_apps).setTitle(
				Utils.isShowSystemApps(this) ? R.string.hide_system_apps
						: R.string.show_system_apps);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.show_system_apps:
			Utils.setShowSystemApps(this, !Utils.isShowSystemApps(this));
			boolean launched = startTask();
			if (!launched) {
				Utils.setShowSystemApps(this, !Utils.isShowSystemApps(this));
			}
			supportInvalidateOptionsMenu();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public class GetApplicationsTask extends
			AsyncTask<Void, Void, ArrayList<AppEntry>> {
		private Context mContext;

		public GetApplicationsTask(Context ctx) {
			this.mContext = ctx;
		}

		@Override
		protected void onPreExecute() {
			toggleDisplay(false);
			super.onPreExecute();
		}

		@Override
		protected ArrayList<AppEntry> doInBackground(Void... params) {
			return Utils.getApplications(mContext);
		}

		@Override
		protected void onPostExecute(ArrayList<AppEntry> result) {
			super.onPostExecute(result);
			updateListView(result);
			finishTask();
		}

		private void finishTask() {
			task = null;
		}

		@Override
		protected void onCancelled() {
			finishTask();
			super.onCancelled();
		}

	}

}
