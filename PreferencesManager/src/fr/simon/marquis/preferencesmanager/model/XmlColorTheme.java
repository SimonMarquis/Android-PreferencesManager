package fr.simon.marquis.preferencesmanager.model;

import android.content.res.Resources;
import fr.simon.marquis.preferencesmanager.R;

public class XmlColorTheme {
	public enum ColorThemeEnum { ECLIPSE, GOOGLE, ROBOTICKET, NOTEPAD, NETBEANS };
	public enum ColorTagEnum { TAG, ATTR_NAME, ATTR_VALUE, COMMENT, VALUE, DEFAULT };
	
	private int tag, attributeName, attributeValue, comment, value,
			defaultColor;

	public static XmlColorTheme createTheme(Resources r, ColorThemeEnum theme) {
		switch (theme) {
		case ECLIPSE:
			return new XmlColorTheme(r,
					R.color.xml_eclipse_tag,
					R.color.xml_eclipse_attribute_name,
					R.color.xml_eclipse_attribute_value,
					R.color.xml_eclipse_comment,
					R.color.xml_eclipse_value,
					R.color.xml_eclipse_default);
		case GOOGLE:
			return new XmlColorTheme(r,
					R.color.xml_google_tag,
					R.color.xml_google_attribute_name,
					R.color.xml_google_attribute_value,
					R.color.xml_google_comment,
					R.color.xml_google_value,
					R.color.xml_google_default);
		case NETBEANS:
			return new XmlColorTheme(r,
					R.color.xml_netbeans_tag,
					R.color.xml_netbeans_attribute_name,
					R.color.xml_netbeans_attribute_value,
					R.color.xml_netbeans_comment,
					R.color.xml_netbeans_value,
					R.color.xml_netbeans_default);
		case NOTEPAD:
			return new XmlColorTheme(r,
					R.color.xml_notepad_tag,
					R.color.xml_notepad_attribute_name,
					R.color.xml_notepad_attribute_value,
					R.color.xml_notepad_comment,
					R.color.xml_notepad_value,
					R.color.xml_notepad_default);
		case ROBOTICKET:
			return new XmlColorTheme(r,
					R.color.xml_roboticket_tag,
					R.color.xml_roboticket_attribute_name,
					R.color.xml_roboticket_attribute_value,
					R.color.xml_roboticket_comment,
					R.color.xml_roboticket_value,
					R.color.xml_roboticket_default);
		default:
			return null;
		}
	}

	private XmlColorTheme(Resources r, int xmlTag, int xmlAttributeName,
			int xmlAttributeValue, int xmlComment, int xmlValue, int xmlDefault) {
		this.tag = r.getColor(xmlTag);
		this.attributeName = r.getColor(xmlAttributeName);
		this.attributeValue = r.getColor(xmlAttributeValue);
		this.comment = r.getColor(xmlComment);
		this.value = r.getColor(xmlValue);
		this.defaultColor = r.getColor(xmlDefault);
	}

	public int getColor(ColorTagEnum type) {
		switch (type) {
		case TAG:
			return tag;
		case ATTR_NAME:
			return attributeName;
		case ATTR_VALUE:
			return attributeValue;
		case COMMENT:
			return comment;
		case VALUE:
			return value;
		default:
			return defaultColor;
		}
	}
}
