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

import android.util.Xml;

public class PreferenceFile {

	ArrayList<PreferenceEntry> prefs;

	public PreferenceFile() {
		super();
		prefs = new ArrayList<PreferenceEntry>();
	}

	public static PreferenceFile fromXML(String string) {

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
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "messages");
			// serializer.attribute("", "number",
			// String.valueOf(messages.size()));
			// for (Message msg: messages){
			// serializer.startTag("", "message");
			// serializer.attribute("", "date", msg.getDate());
			// serializer.startTag("", "title");
			// serializer.text(msg.getTitle());
			// serializer.endTag("", "title");
			// serializer.startTag("", "url");
			// serializer.text(msg.getLink().toExternalForm());
			// serializer.endTag("", "url");
			// serializer.startTag("", "body");
			// serializer.text(msg.getDescription());
			// serializer.endTag("", "body");
			// serializer.endTag("", "message");
			// }
			serializer.endTag("", "messages");
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
