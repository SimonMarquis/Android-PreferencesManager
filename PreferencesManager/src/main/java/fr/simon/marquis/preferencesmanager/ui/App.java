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
import android.util.Log;

import fr.simon.marquis.preferencesmanager.model.AppTheme;

public class App extends Application {

    public static AppTheme theme = AppTheme.DEFAULT_THEME;

    @Override
    public void onCreate() {
        initTheme();
        setTheme(theme.theme);
        super.onCreate();
    }

    private void initTheme() {
        try {
            theme = AppTheme.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(AppTheme.class.getSimpleName(), AppTheme.DEFAULT_THEME.name()));
        } catch (IllegalArgumentException iae) {
            Log.d(App.class.getSimpleName(), "No theme specified, using the default one");
            theme = AppTheme.DEFAULT_THEME;
        }
    }

    public void switchTheme() {
        theme = theme == AppTheme.DARK ? AppTheme.LIGHT : AppTheme.DARK;
    }

}
