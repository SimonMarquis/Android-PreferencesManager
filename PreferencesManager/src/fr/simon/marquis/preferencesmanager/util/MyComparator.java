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
package fr.simon.marquis.preferencesmanager.util;

import java.text.Collator;
import java.util.Comparator;

import fr.simon.marquis.preferencesmanager.model.AppEntry;

public class MyComparator implements Comparator<AppEntry> {
	private final Collator sCollator = Collator.getInstance();

	public MyComparator() {
		// Ignore case and accents
		sCollator.setStrength(Collator.SECONDARY);
	}

	@Override
	public int compare(AppEntry obj1, AppEntry obj2) {
		return sCollator
				.compare(obj1.getSortingValue(), obj2.getSortingValue());
	}
}
