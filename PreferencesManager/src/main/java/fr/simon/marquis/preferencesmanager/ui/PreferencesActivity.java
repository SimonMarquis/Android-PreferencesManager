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

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.Files;
import fr.simon.marquis.preferencesmanager.model.PreferenceSortType;
import fr.simon.marquis.preferencesmanager.ui.PreferencesFragment.OnFragmentInteractionListener;
import fr.simon.marquis.preferencesmanager.util.Utils;

public class PreferencesActivity extends ActionBarActivity implements OnFragmentInteractionListener {

    public final static String KEY_SORT_TYPE = "KEY_SORT_TYPE";
    public final static String EXTRA_PACKAGE_NAME = "EXTRA_PACKAGE_NAME";
    public final static String EXTRA_TITLE = "EXTRA_TITLE";
    private final static String KEY_FILES = "KEY_FILES";
    private final static String INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    private final static String EXTRA_SHORTCUT = "EXTRA_SHORTCUT";
    public static PreferenceSortType preferenceSortType = PreferenceSortType.TYPE_AND_ALPHANUMERIC;

    private ViewPager mViewPager;
    private View mLoadingView;
    private View mEmptyView;

    private Files files;
    private String packageName;
    private String title;

    private boolean launchedFromShortcut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle b = getIntent().getExtras();
        if (b == null) {
            finish();
            return;
        }

        int index = PreferenceManager.getDefaultSharedPreferences(this).getInt(KEY_SORT_TYPE, 0);
        preferenceSortType = PreferenceSortType.values()[index];

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mLoadingView = findViewById(R.id.loadingView);
        mEmptyView = findViewById(R.id.emptyView);

        packageName = b.getString(EXTRA_PACKAGE_NAME);
        title = b.getString(EXTRA_TITLE);
        launchedFromShortcut = b.getBoolean(EXTRA_SHORTCUT, false);

        getActionBar().setTitle(Utils.applyCustomTypeFace(title, this));
        getActionBar().setSubtitle(Utils.applyCustomTypeFace(packageName, this));
        Drawable drawable = Utils.findDrawable(packageName, this);
        if (drawable != null) {
            getSupportActionBar().setIcon(drawable);
        }

        if (savedInstanceState == null) {
            new FindFilesTask(packageName).execute();
        } else {
            try {
                updateFindFiles(Files.fromJSON(new JSONArray(savedInstanceState.getString(KEY_FILES))));
            } catch (JSONException e) {
                new FindFilesTask(packageName).execute();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(files != null){
            outState.putString(KEY_FILES, files.toJSON().toString());
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
                supportInvalidateOptionsMenu();
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
    public void onFragmentInteraction(Uri uri) {

    }

    private void updateFindFiles(Files f) {
        files = f;
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PreferencesFragment.newInstance(files.get(position).getName(), files.get(position).getPath(), packageName);
        }

        @Override
        public int getCount() {
            return files.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Utils.applyCustomTypeFace(files.get(position).getName(), PreferencesActivity.this);
        }
    }

    public class FindFilesTask extends AsyncTask<Void, Void, Files> {
        private final String mPackageName;

        public FindFilesTask(String packageName) {
            this.mPackageName = packageName;
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
}
