package fr.simon.marquis.preferencesmanager.ui;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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
	StickyListHeadersListView listView;

	String curFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_list);
		AppAdapter appAdapter = new AppAdapter(this, Utils.getApplications(this));
		listView = (StickyListHeadersListView) findViewById(R.id.listView);
		listView.setAdapter(appAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (!App.getRoot().connected()) {
					Utils.displayNoRoot(AppListActivity.this).show();
				} else {
					AppEntry item = Utils.getApplications(AppListActivity.this).get(arg2);
					Files files = findXmlFiles(item);
					if (files == null || files.size() == 0) {
						Toast.makeText(AppListActivity.this,
								"Pas de fichiers de préférences",
								Toast.LENGTH_SHORT).show();
						// No result, show crouton
					} else {
						Intent i = new Intent(AppListActivity.this,
								PreferencesActivity.class);
						Log.d("", item.getLabel());
						Log.d("", item.getApplicationInfo().packageName);
						Log.d("", files.toJSON().toString());
						i.putExtra("TITLE", item.getLabel());
						i.putExtra("PACKAGE_NAME",
								item.getApplicationInfo().packageName);
						i.putExtra("FILES", files.toJSON().toString());

						startActivity(i);
					}
				}
			}

		});

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
							((AppAdapter) listView.getWrappedAdapter())
									.setFilter(curFilter);
//	TODO						((AppAdapter) listView.getWrappedAdapter())
//									.getFilter().filter(curFilter);
							return true;
						}
					});
			item.setActionView(searchView);
		}
		return true;
	}

}
