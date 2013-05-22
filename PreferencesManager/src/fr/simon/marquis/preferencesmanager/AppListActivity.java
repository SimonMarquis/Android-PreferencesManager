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
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class AppListActivity extends SherlockActivity {
	ListView listView;
	
    // If non-null, this is the current filter the user has provided.
    String mCurFilter;
    AppAdapter mAppAdapter;
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_list);
		listView = (ListView) findViewById(R.id.listView);
		mAppAdapter = new AppAdapter(this);
		mAppAdapter.setData(getAppEntries());
		listView.setAdapter(mAppAdapter);

	}

	
	 @Override 
	 public boolean onCreateOptionsMenu(Menu menu) {
         // Place an action bar item for searching.
         MenuItem item = menu.add("Search");
         item.setIcon(android.R.drawable.ic_menu_search);
         item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
         View searchView = SearchViewCompat.newSearchView(getSupportActionBar().getThemedContext());
         if (searchView != null) {
             SearchViewCompat.setOnQueryTextListener(searchView,
                     new OnQueryTextListenerCompat() {
            	 
                 @Override
                 public boolean onQueryTextChange(String newText) {
                	 Log.e("","TextChanged"+newText);
                     mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
                     mAppAdapter.getFilter().filter(mCurFilter);
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

		Collections.sort(entries, AppEntry.ALPHA_COMPARATOR);
		return entries;
	}

}
