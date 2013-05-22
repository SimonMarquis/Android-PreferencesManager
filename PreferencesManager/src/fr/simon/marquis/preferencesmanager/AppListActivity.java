package fr.simon.marquis.preferencesmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

public class AppListActivity extends SherlockActivity {
	StickyListHeadersListView listView;

	// If non-null, this is the current filter the user has provided.
	String curFilter;
	AppAdapter appAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_list);
		listView = (StickyListHeadersListView) findViewById(R.id.listView);
		appAdapter = new AppAdapter(this);
		appAdapter.setData(getAppEntries());
		listView.setAdapter(appAdapter);

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
							Log.e("", "TextChanged" + newText);
							curFilter = !TextUtils.isEmpty(newText) ? newText
									: null;
							appAdapter.setFilter(curFilter);
							appAdapter.getFilter().filter(curFilter);
							return true;
						}
					});
			item.setActionView(searchView);
		}
		return true;
	}

	public List<AppEntry> getAppEntries() {
		List<ApplicationInfo> apps = getPackageManager()
				.getInstalledApplications(
						PackageManager.GET_UNINSTALLED_PACKAGES
								| PackageManager.GET_DISABLED_COMPONENTS);
		if (apps == null)
			apps = new ArrayList<ApplicationInfo>();

		List<AppEntry> entries = new ArrayList<AppEntry>(apps.size());
		for (int i = 0; i < apps.size(); i++) {
			entries.add(new AppEntry(apps.get(i), this));
		}

		Collections.sort(entries, new MyComparator());
		return entries;
	}

}
