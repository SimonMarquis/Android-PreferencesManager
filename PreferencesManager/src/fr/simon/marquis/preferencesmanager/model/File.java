package fr.simon.marquis.preferencesmanager.model;

import org.json.JSONException;
import org.json.JSONObject;

public class File {

	private String name;
	private String path;

	public File(String name, String path) {
		super();
		this.name = name;
		this.path = path;
	}

	public static File fromJSON(JSONObject jsonObject) {
		return new File(jsonObject.optString("NAME"),
				jsonObject.optString("PATH"));
	}

	public JSONObject toJSON() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("NAME", name);
			obj.put("PATH", path);
			return obj;
		} catch (JSONException e) {
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
