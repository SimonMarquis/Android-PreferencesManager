package fr.simon.marquis.preferencesmanager.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class Files {

	ArrayList<File> files;

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
