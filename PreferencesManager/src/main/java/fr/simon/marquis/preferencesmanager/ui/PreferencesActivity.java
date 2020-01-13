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

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.BackupContainer;
import fr.simon.marquis.preferencesmanager.model.PreferenceSortType;
import fr.simon.marquis.preferencesmanager.ui.PreferencesFragment.OnPreferenceFragmentInteractionListener;
import fr.simon.marquis.preferencesmanager.util.Ui;
import fr.simon.marquis.preferencesmanager.util.Utils;

public class PreferencesActivity extends AppCompatActivity implements
        OnPreferenceFragmentInteractionListener,
        RestoreDialogFragment.OnRestoreFragmentInteractionListener {

    private static final String TAG = PreferencesActivity.class.getSimpleName();
    public final static String KEY_SORT_TYPE = "KEY_SORT_TYPE";
    public final static String EXTRA_PACKAGE_NAME = "EXTRA_PACKAGE_NAME";
    public final static String KEY_ICON_URI = "KEY_ICON_URI";
    public final static String EXTRA_TITLE = "EXTRA_TITLE";
    private final static String KEY_FILES = "KEY_FILES";
    private final static String INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    private final static String EXTRA_SHORTCUT = "EXTRA_SHORTCUT";
    public static PreferenceSortType preferenceSortType = PreferenceSortType.TYPE_AND_ALPHANUMERIC;

    private ViewPager mViewPager;
    private View mLoadingView;
    private View mEmptyView;

    private Uri iconUri;
    private List<String> files;
    private String packageName;
    private String title;

    private BackupContainer backupContainer;

    private FindFilesAndBackupsTask findFilesAndBackupsTask;

    private boolean launchedFromShortcut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(App.theme.theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        if (b == null) {
            finish();
            return;
        }

        int index = PreferenceManager.getDefaultSharedPreferences(this).getInt(KEY_SORT_TYPE, 0);
        preferenceSortType = PreferenceSortType.values()[index];

        mViewPager = findViewById(R.id.pager);
        mLoadingView = findViewById(R.id.loadingView);
        mEmptyView = findViewById(R.id.emptyView);

        iconUri = b.getParcelable(KEY_ICON_URI);
        packageName = b.getString(EXTRA_PACKAGE_NAME);
        title = b.getString(EXTRA_TITLE);
        launchedFromShortcut = b.getBoolean(EXTRA_SHORTCUT, false);

        getSupportActionBar().setTitle(Ui.applyCustomTypeFace(title, this));
        getSupportActionBar().setSubtitle(Ui.applyCustomTypeFace(packageName, this));
        Picasso.get().load(iconUri).error(R.drawable.ic_launcher).into((android.widget.ImageView) findViewById(android.R.id.home));

        if (savedInstanceState == null) {
            findFilesAndBackupsTask = new FindFilesAndBackupsTask(packageName);
            findFilesAndBackupsTask.execute();
        } else {
            try {
                ArrayList<String> tmp = new ArrayList<>();
                JSONArray array = new JSONArray(savedInstanceState.getString(KEY_FILES));
                for (int i = 0; i < array.length(); i++) {
                    tmp.add(array.getString(i));
                }
                updateFindFiles(tmp);
                updateFindBackups(Utils.getBackups(getApplicationContext(), packageName));
            } catch (Exception e) {
                findFilesAndBackupsTask = new FindFilesAndBackupsTask(packageName);
                findFilesAndBackupsTask.execute();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (files != null) {
            JSONArray json = new JSONArray();
            for (String file : files) {
                json.put(file);
            }
            outState.putString(KEY_FILES, json.toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preferences_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean fav = Utils.isFavorite(packageName, this);
        MenuItem itemFav = menu.findItem(R.id.action_fav);
        if (itemFav != null) {
            itemFav.setIcon(fav ? R.drawable.ic_action_star_10 : R.drawable.ic_action_star_0)
                    .setTitle(fav ? R.string.action_unfav : R.string.action_fav);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (launchedFromShortcut) {
                    Intent i = new Intent(this, AppListActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                finish();
                return true;
            case R.id.action_fav:
                Utils.setFavorite(packageName, !Utils.isFavorite(packageName, this), this);
                invalidateOptionsMenu();
                break;
            case R.id.action_shortcut:
                createShortcut();
                Toast.makeText(this, R.string.toast_shortcut, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createShortcut() {
        Intent shortcutIntent = new Intent(this, PreferencesActivity.class);
        shortcutIntent.putExtra(EXTRA_PACKAGE_NAME, packageName);
        shortcutIntent.putExtra(EXTRA_TITLE, title);
        shortcutIntent.putExtra(EXTRA_SHORTCUT, true);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher));
        addIntent.setAction(INSTALL_SHORTCUT);
        sendBroadcast(addIntent);
    }

    @Override
    public void onBackupFile(String fullPath) {
        String backup = String.valueOf(new Date().getTime());
        backupContainer.put(fullPath, backup);

        //TODO: asynctask
        if (Utils.backupFile(backup, fullPath, this)) {
            Utils.saveBackups(this, packageName, backupContainer);
            Toast.makeText(this, R.string.toast_backup_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.toast_backup_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean canRestoreFile(String fullPath) {
        return backupContainer != null && backupContainer.contains(fullPath);
    }

    @Override
    public List<String> getBackups(String fullPath) {
        return backupContainer == null ? null : backupContainer.get(fullPath);
    }

    @Override
    public String onRestoreFile(String backup, String fullPath) {
        Log.d(TAG, String.format("onRestoreFile(%s, %s)", backup, fullPath));
        if (Utils.restoreFile(this, backup, fullPath, packageName)) {
            Toast.makeText(this, R.string.file_restored, Toast.LENGTH_SHORT).show();
        }
        return Utils.readFile(fullPath);
    }

    @Override
    public List<String> onDeleteBackup(String backup, String fullPath) {
        backupContainer.remove(fullPath, backup);
        deleteFile(backup);
        Utils.saveBackups(this, packageName, backupContainer);
        invalidateOptionsMenu();
        return backupContainer.get(fullPath);
    }

    private void updateFindFiles(List<String> tmp) {
        files = tmp;
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), files);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);

        if (files == null || files.size() == 0) {
            if (fadeIn != null) {
                mEmptyView.startAnimation(fadeIn);
            }
            mEmptyView.setVisibility(View.VISIBLE);
            if (fadeOut != null) {
                mLoadingView.startAnimation(fadeOut);
            }
            mLoadingView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.GONE);
            mLoadingView.setVisibility(View.GONE);
            if (fadeIn != null) {
                mViewPager.startAnimation(fadeIn);
            }
            mViewPager.setVisibility(View.VISIBLE);
        }
    }

    private void updateFindBackups(BackupContainer b) {
        backupContainer = b;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private List<String> mFiles;

        public SectionsPagerAdapter(FragmentManager fm, List<String> files) {
            super(fm);
            this.mFiles = files;
        }

        @Override
        public Fragment getItem(int position) {
            return PreferencesFragment.newInstance(mFiles.get(position), packageName, iconUri);
        }

        @Override
        public int getCount() {
            return mFiles.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Ui.applyCustomTypeFace(Utils.extractFileName(mFiles.get(position)), PreferencesActivity.this);
        }
    }

    public class FindFilesAndBackupsTask extends AsyncTask<Void, Void, Pair<List<String>, BackupContainer>> {
        private final String mPackageName;

        public FindFilesAndBackupsTask(String packageName) {
            this.mPackageName = packageName;
        }

        @Override
        protected Pair<List<String>, BackupContainer> doInBackground(Void... params) {
            return Pair.create(Utils.findXmlFiles(mPackageName), Utils.getBackups(getApplicationContext(), mPackageName));
        }

        @Override
        protected void onPostExecute(Pair<List<String>, BackupContainer> result) {
            updateFindFiles(result.first);
            updateFindBackups(result.second);
            super.onPostExecute(result);
        }
    }

    @Override
    protected void onDestroy() {
        if (findFilesAndBackupsTask != null) {
            findFilesAndBackupsTask.cancel(true);
        }
        super.onDestroy();
    }
}
