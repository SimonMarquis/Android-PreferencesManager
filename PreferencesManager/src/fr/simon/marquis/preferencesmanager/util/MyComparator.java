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
