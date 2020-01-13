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

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BackupContainer {

    private final static String KEY_FILE = "FILE";
    private final static String KEY_BACKUPS = "BACKUPS";

    private final Map<String, List<String>> backups;

    public BackupContainer() {
        super();
        this.backups = new HashMap<>();
    }

    public static BackupContainer fromJSON(JSONArray filesArray) {
        BackupContainer container = new BackupContainer();
        for (int i = 0; i < filesArray.length(); i++) {
            JSONObject obj = filesArray.optJSONObject(i);
            if (obj != null) {
                String file = obj.optString(KEY_FILE);
                JSONArray backupsArray = obj.optJSONArray(KEY_BACKUPS);
                if (backupsArray != null) {
                    for (int j = 0; j < backupsArray.length(); j++) {
                        String _backup = backupsArray.optString(j);
                        if (!TextUtils.isEmpty(_backup)) {
                            container.put(file, _backup);
                        }
                    }
                }
            }
        }
        return container;
    }

    public JSONArray toJSON() {
        JSONArray array = new JSONArray();
        Set<Map.Entry<String, List<String>>> entries = backups.entrySet();
        for (Map.Entry<String, List<String>> entry : entries) {
            JSONObject obj = new JSONObject();
            JSONArray arrayBackups = new JSONArray(entry.getValue());
            try {
                obj.put(KEY_BACKUPS, arrayBackups);
                obj.put(KEY_FILE, entry.getKey());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (arrayBackups.length() > 0) {
                array.put(obj);
            }
        }
        return array;
    }

    public void put(String key, String value) {
        if (backups.containsKey(key)) {
            backups.get(key).add(value);
        } else {
            List<String> list = new ArrayList<>();
            list.add(value);
            backups.put(key, list);
        }
    }

    public void remove(String key, String value) {
        if (backups.containsKey(key)) {
            List<String> list = backups.get(key);
            list.remove(value);
            if(list.isEmpty()){
                backups.remove(key);
            }
        }
    }

    public boolean contains(String key) {
        return backups.containsKey(key);
    }

    public List<String> get(String key) {
        return backups.get(key);
    }

    public boolean isEmpty() {
        return backups.isEmpty();
    }
}
