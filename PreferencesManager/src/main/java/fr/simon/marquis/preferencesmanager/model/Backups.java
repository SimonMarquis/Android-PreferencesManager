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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Backups {

    private final List<Backup> backups;

    public Backups() {
        super();
        this.backups = new ArrayList<Backup>();
    }

    public static Backups fromJSON(JSONArray jsonArray) {
        Backups _b = new Backups();
        for (int i = 0; i < jsonArray.length(); i++) {
            Backup _t = Backup.fromJSON(jsonArray.optJSONObject(i));
            if (_t != null)
                _b.add(_t);
        }
        return _b;
    }

    public JSONArray toJSON() {
        JSONArray array = new JSONArray();
        for (Backup backup : backups) {
            JSONObject obj = backup.toJSON();
            if (obj != null) {
                array.put(obj);
            }
        }
        return array;
    }

    public void add(Backup backup) {
        backups.add(backup);
    }

    public int size() {
        return backups.size();
    }

    public Backup get(int position) {
        return backups.get(position);
    }

}
