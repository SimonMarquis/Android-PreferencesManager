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

import org.json.JSONException;
import org.json.JSONObject;

public class Backup {

    private static final String KEY_TIME = "TIME";

    private final long time;

    public Backup(long time) {
        super();
        this.time = time;
    }

    public static Backup fromJSON(JSONObject jsonObject) {
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
}
