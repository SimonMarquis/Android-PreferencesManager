package fr.simon.marquis.preferencesmanager.model;

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
public enum XmlFontSize {
	// EXTRA_SMALL(12), SMALL(14), MEDIUM(16), LARGE(18), EXTRA_LARGE(22);
	EXTRA_SMALL(10), SMALL(13), MEDIUM(16), LARGE(20), EXTRA_LARGE(24);

	private int mSize;

	private XmlFontSize(int size) {
		this.mSize = size;
	}

	public int getSize() {
		return mSize;
	}

	public static XmlFontSize generateSize(int n) {
		XmlFontSize[] sizes = XmlFontSize.values();
		for (int i = 0; i < sizes.length; i++) {
			if (sizes[i].getSize() == n) {
				return sizes[i];
			}
		}
		return MEDIUM;
	}
}
