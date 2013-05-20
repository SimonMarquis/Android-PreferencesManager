package fr.simon.marquis.preferencesmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;

public class AppListActivity extends SherlockActivity {
	ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_list);
		listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(new AppAdapter(getAppEntries(),
				getLayoutInflater(), this));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.app_list, menu);
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
