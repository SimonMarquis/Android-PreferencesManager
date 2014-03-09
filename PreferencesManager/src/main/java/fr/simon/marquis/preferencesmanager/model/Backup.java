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

import android.content.Context;
import android.text.format.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Backup implements Comparable<Backup> {

    private static final String KEY_TIME = "TIME";

    private final long time;
    private String displayLabel;

    public Backup(long time) {
        super();
        this.time = time;
    }

    public static Backup fromJSON(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        return new Backup(jsonObject.optLong(KEY_TIME));
    }

    public JSONObject toJSON() {
        try {
            JSONObject obj = new JSONObject();
            obj.put(KEY_TIME, time);
            return obj;
        } catch (JSONException ignore) {
        }
        return null;
    }

    public long getTime() {
        return time;
    }

    public String getDisplayLabel(Context ctx) {
        if (displayLabel == null) {
            displayLabel = new StringBuilder(upperFirstLetter(DateUtils.formatDateTime(ctx, getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY))).append(" (").append(lowerFirstLetter(DateUtils.getRelativeTimeSpanString(getTime(), new Date().getTime(), DateUtils.SECOND_IN_MILLIS).toString())).append(")").toString();
        }
        return displayLabel;
    }

    private String upperFirstLetter(String original) {
        if (original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    private String lowerFirstLetter(String original) {
        if (original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toLowerCase() + original.substring(1);
    }

    @Override
    public int compareTo(Backup another) {
        return (int) (another.getTime() - getTime());
    }
}
