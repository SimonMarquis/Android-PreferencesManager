package fr.simon.marquis.preferencesmanager;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class PreferenceEntry {

	public enum EntryType {
		INT("int"), STRING("string"), LONG("long"), BOOLEAN("boolean");
		private String val;

		private EntryType(String val) {
			this.val = val;
		}

		public String getVal() {
			return val;
		}
	};

	private EntryType entryType;
	private String name;
	private String value;

	public PreferenceEntry(XmlPullParser parser) {
		EntryType t = EntryType.valueOf(parser.getName());
		name = parser.getAttributeValue(null, "name");
		switch (t) {
		case INT:
		case BOOLEAN:
		case LONG:
			value = parser.getAttributeValue(null, "value");
			break;
		case STRING:
			try {
				value = parser.nextText();
			} catch (XmlPullParserException e) {
			} catch (IOException e) {
			}
			break;
		default:
			break;
		}
	}
}
