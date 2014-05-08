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
package fr.simon.marquis.preferencesmanager.model;

import fr.simon.marquis.preferencesmanager.R;

public enum AppTheme {
    LIGHT(R.style.AppThemeLight, R.string.dark_theme), DARK(R.style.AppThemeDark, R.string.light_theme);

    public static final AppTheme DEFAULT_THEME = LIGHT;
    public final int theme;
    /**
     * This is the title displayed in menu, so it's normal that's inverted
     */
    public final int title;

    AppTheme(int theme, int title) {
        this.theme = theme;
        this.title = title;
    }
}