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

import java.util.Set;

import fr.simon.marquis.preferencesmanager.R;

public enum PreferenceType {
    BOOLEAN(R.layout.dialog_pref_boolean_add, R.layout.dialog_pref_boolean_edit, R.string.title_add_boolean, R.string.title_edit_boolean, R.drawable.card_purpleborder), //
    STRING(R.layout.dialog_pref_string_add, R.layout.dialog_pref_string_edit, R.string.title_add_string, R.string.title_edit_string, R.drawable.card_greenborder), //
    INT(R.layout.dialog_pref_integer_add, R.layout.dialog_pref_integer_edit, R.string.title_add_int, R.string.title_edit_int, R.drawable.card_redborder), //
    FLOAT(R.layout.dialog_pref_float_add, R.layout.dialog_pref_float_edit, R.string.title_add_float, R.string.title_edit_float, R.drawable.card_navyborder), //
    LONG(R.layout.dialog_pref_long_add, R.layout.dialog_pref_long_edit, R.string.title_add_long, R.string.title_edit_long, R.drawable.card_tealborder), //
    STRINGSET(R.layout.dialog_pref_stringset_add, R.layout.dialog_pref_stringset_edit, R.string.title_add_stringset, R.string.title_edit_stringset, R.drawable.card_goldborder),//
    UNSUPPORTED(0, 0, 0, 0, R.drawable.card_unknown);

    private final int mDialogLayoutAdd;
    private final int mDialogLayoutEdit;
    private final int mDialogTitleAdd;
    private final int mDialogTitleEdit;
    private final int mCardBackground;

    private PreferenceType(int dialogLayoutAdd, int dialogLayoutEdit, int dialogTitleAdd, int dialogTitleEdit, int cardBackground) {
        this.mDialogLayoutAdd = dialogLayoutAdd;
        this.mDialogLayoutEdit = dialogLayoutEdit;
        this.mDialogTitleAdd = dialogTitleAdd;
        this.mDialogTitleEdit = dialogTitleEdit;
        this.mCardBackground = cardBackground;
    }

    public static PreferenceType fromObject(Object obj) {
        if (obj instanceof String) {
            return STRING;
        } else if (obj instanceof Integer) {
            return INT;
        } else if (obj instanceof Long) {
            return LONG;
        } else if (obj instanceof Float) {
            return FLOAT;
        } else if (obj instanceof Boolean) {
            return BOOLEAN;
        } else if (obj instanceof Set<?>) {
            return STRINGSET;
        }
        return UNSUPPORTED;
    }

    public static int getDialogLayout(Object obj) {
        return fromObject(obj).getCardBackground();
    }

    public int getDialogLayout(boolean editMode) {
        return editMode ? mDialogLayoutEdit : mDialogLayoutAdd;
    }

    public int getCardBackground() {
        return mCardBackground;
    }

    public int getDialogTitleAdd() {
        return mDialogTitleAdd;
    }

    public int getDialogTitleEdit() {
        return mDialogTitleEdit;
    }
}
