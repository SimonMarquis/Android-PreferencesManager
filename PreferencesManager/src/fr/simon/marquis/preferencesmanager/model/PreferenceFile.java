package fr.simon.marquis.preferencesmanager.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

public class PreferenceFile {

	ArrayList<PreferenceEntry> prefs;

	public PreferenceFile() {
		super();
		prefs = new ArrayList<PreferenceEntry>();
	}

	public static PreferenceFile fromXML(String string) {
		Log.e("", "INTÉRIEUR");
		// Check for empty files
		if (TextUtils.isEmpty(string) || string.trim().isEmpty())
			return null;

		try {
			PreferenceFile p = null;
			XmlPullParser parser = XmlPullParserFactory.newInstance()
					.newPullParser();
			InputStream is = new ByteArrayInputStream(string.getBytes());
			parser.setInput(is, null);
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;

				case XmlPullParser.START_TAG:
					String tagName = parser.getName();

					if (tagName.equalsIgnoreCase("map")) {
						p = new PreferenceFile();
					}

					else if (tagName.equalsIgnoreCase("string")
							|| tagName.equalsIgnoreCase("boolean")
							|| tagName.equalsIgnoreCase("long")
							|| tagName.equalsIgnoreCase("int")) {
						if (p == null)
							p = new PreferenceFile();
						p.add(new PreferenceEntry(parser));
					}
					break;
				}
				eventType = parser.next();
			}
			return p;
		} catch (XmlPullParserException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	private void add(PreferenceEntry entry) {
		prefs.add(entry);
	}

	public String toXML() {
		XmlSerializer serializer = Xml.newSerializer();
		serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("utf-8", true);
			serializer.startTag("", "map");

			for (PreferenceEntry p : prefs) {
				p.addTag(serializer);
			}
			serializer.endTag("", "map");
			serializer.endDocument();
			return writer.toString();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void readInt() {

	}

	public void readString() {

	}

}
