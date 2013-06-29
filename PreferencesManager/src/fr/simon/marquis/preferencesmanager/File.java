package fr.simon.marquis.preferencesmanager;

import org.json.JSONException;
import org.json.JSONObject;

public class File {

	String name;
	String path;

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

}
