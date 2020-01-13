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

import android.app.Application;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.topjohnwu.superuser.Shell;

import fr.simon.marquis.preferencesmanager.BuildConfig;
import fr.simon.marquis.preferencesmanager.model.AppTheme;

public class App extends Application {

    public static AppTheme theme = AppTheme.DEFAULT_THEME;

    static {
        /* Shell.Config methods shall be called before any shell is created
         * This is the why in this example we call it in a static block
         * The followings are some examples, check Javadoc for more details */
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(10);
    }

    @Override
    public void onCreate() {
        initTheme();
        setTheme(theme.theme);
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private void initTheme() {
        try {
            theme = AppTheme.valueOf(PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .getString(AppTheme.APP_THEME_KEY, AppTheme.DEFAULT_THEME.name()));
        } catch (IllegalArgumentException iae) {
            Log.d(App.class.getSimpleName(), "No theme specified, using the default one");
            theme = AppTheme.DEFAULT_THEME;
        }
    }

    public void switchTheme() {
        theme = theme == AppTheme.DARK ? AppTheme.LIGHT : AppTheme.DARK;
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(AppTheme.APP_THEME_KEY, theme.name()).commit();
    }

}
