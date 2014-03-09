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

public class Files {

    private final List<File> files;

    public Files() {
        super();
		files = new ArrayList<File>();
	}

	public static Files fromJSON(JSONArray jsonArray) {
		Files _f = new Files();
		for (int i = 0; i < jsonArray.length(); i++) {
			File _t = File.fromJSON(jsonArray.optJSONObject(i));
			if (_t != null)
				_f.add(_t);
		}
		return _f;
	}

	public void add(File file) {
		files.add(file);
	}

	public int size() {
		return files.size();
	}

	public File get(int position) {
		return files.get(position);
	}

	public JSONArray toJSON() {
		JSONArray array = new JSONArray();
		for (File file : files) {
			JSONObject obj = file.toJSON();
			if (obj != null) {
				array.put(obj);
			}
		}
		return array;
	}

}
