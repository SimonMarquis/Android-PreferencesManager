package fr.simon.marquis.preferencesmanager.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import android.text.TextUtils;
import fr.simon.marquis.preferencesmanager.util.XmlUtils;

public class PreferenceFile {

	private Map<String, Object> mPreferences;

	public PreferenceFile() {
		super();
		mPreferences = new HashMap<String, Object>();
	}

	public static PreferenceFile fromXml(String xml) {
		PreferenceFile preferenceFile = new PreferenceFile();

		// Check for empty files
		if (TextUtils.isEmpty(xml) || xml.trim().isEmpty())
			return preferenceFile;

		try {
			InputStream in = new ByteArrayInputStream(xml.getBytes());
			Map map = XmlUtils.readMapXml(in);
			in.close();

			if (map != null) {
				preferenceFile.setPreferences(map);
			}
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}
		return preferenceFile;
	}

	public void setPreferences(Map map) {
		mPreferences = map;
	}

	public Map<String, Object> getPreferences() {
		return mPreferences;
	}

	public String toXml() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			XmlUtils.writeMapXml(mPreferences, out);
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}
		return out.toString();
	}
}
