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

public class File {

    private static final String KEY_NAME = "NAME";
    private static final String KEY_PATH = "PATH";

    private final String name;
    private final String path;

    public File(String name, String path) {
        super();
		this.name = name;
		this.path = path;
	}

	public static File fromJSON(JSONObject jsonObject) {
        return new File(jsonObject.optString(KEY_NAME), jsonObject.optString(KEY_PATH));
    }

    public JSONObject toJSON() {
		try {
			JSONObject obj = new JSONObject();
            obj.put(KEY_NAME, name);
            obj.put(KEY_PATH, path);
            return obj;
        } catch (JSONException ignored) {
        }
        return null;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

}
