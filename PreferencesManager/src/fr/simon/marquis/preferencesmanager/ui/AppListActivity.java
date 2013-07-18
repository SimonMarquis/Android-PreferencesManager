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
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.spazedog.lib.rootfw.container.FileStat;

import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.AppEntry;
import fr.simon.marquis.preferencesmanager.model.File;
import fr.simon.marquis.preferencesmanager.model.Files;
import fr.simon.marquis.preferencesmanager.util.Utils;

public class AppListActivity extends SherlockActivity {
	private static final int REQUEST_CODE = 123456;
	private StickyListHeadersListView listView;
	private View loadingView;

	String curFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_list);
		loadingView = findViewById(R.id.loadingView);
		listView = (StickyListHeadersListView) findViewById(R.id.listView);

		GetApplicationsTask task = new GetApplicationsTask(this);
		if (Utils.hasHONEYCOMB()) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					(Void[]) null);
		} else {
			task.execute();
		}

	}

	public void updateListView(ArrayList<AppEntry> apps) {
		loadingView.setVisibility(View.GONE);
		loadingView.startAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_out));
		listView.startAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_in));
		listView.setAdapter(new AppAdapter(this, apps));
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (!App.getRoot().connected()) {
					Utils.displayNoRoot(AppListActivity.this).show();
				} else {
					AppEntry item = (AppEntry) ((AppAdapter) listView
							.getWrappedAdapter()).getItem(arg2);

					long start = System.currentTimeMillis();
					Files files = findXmlFiles(item);
					Log.e("", (System.currentTimeMillis() - start)
							+ "ms to findXmlFiles");

					if (files == null || files.size() == 0) {
						Toast.makeText(AppListActivity.this,
								"Pas de fichiers de préférences",
								Toast.LENGTH_SHORT).show();
						// No result, show crouton
					} else {
						Intent i = new Intent(AppListActivity.this,
								PreferencesActivity.class);
						i.putExtra("TITLE", item.getLabel());
						i.putExtra("PACKAGE_NAME",
								item.getApplicationInfo().packageName);
						i.putExtra("FILES", files.toJSON().toString());

						startActivityForResult(i, REQUEST_CODE);
					}
				}
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

	private Files findXmlFiles(AppEntry app) {
		if (app == null)
			return null;

		String path = "data/data/" + app.getApplicationInfo().packageName;
		ArrayList<FileStat> files = App.getRoot().file.statList(path);
		return findFiles(files, path, new Files());
	}

	private Files findFiles(ArrayList<FileStat> files, String path, Files list) {
		if (files == null)
			return list;

		for (FileStat file : files) {
			if (file == null || TextUtils.isEmpty(file.name()))
				continue;
			if (".".equals(file.name()) || "..".equals(file.name()))
				continue;
			if ("d".equals(file.type())) {
				String p = path + "/" + file.name();
				findFiles(App.getRoot().file.statList(p), p, list);
				continue;
			}
			if ("f".equals(file.type()) && file.name().endsWith(".xml")) {
				list.add(new File(file.name(), path));
			}
		}

		return list;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add("Search");
		item.setIcon(android.R.drawable.ic_menu_search);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		View searchView = SearchViewCompat.newSearchView(getSupportActionBar()
				.getThemedContext());
		if (searchView != null) {
			SearchViewCompat.setOnQueryTextListener(searchView,
					new OnQueryTextListenerCompat() {

						@Override
						public boolean onQueryTextChange(String newText) {
							curFilter = !TextUtils.isEmpty(newText) ? newText
									.trim() : null;
									AppAdapter adapter = ((AppAdapter) listView.getWrappedAdapter());
									if(adapter == null){
										return false;
									}
							
									adapter.setFilter(curFilter);
									adapter.getFilter().filter(curFilter);
							return true;
						}
					});
			item.setActionView(searchView);
		}
		return true;
	}

	public class GetApplicationsTask extends
			AsyncTask<Void, Void, ArrayList<AppEntry>> {
		private Context mContext;

		public GetApplicationsTask(Context ctx) {
			this.mContext = ctx;
		}

		@Override
		protected void onPreExecute() {
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
		}

	}

}
