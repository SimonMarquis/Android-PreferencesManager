package fr.simon.marquis.preferencesmanager.model;

import java.io.File;

import fr.simon.marquis.preferencesmanager.util.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

public class AppEntry {

	/**
	 * 
	 */
	private final ApplicationInfo mInfo;
	/**
	 * File of the application
	 */
	private final File mApkFile;
	/**
	 * Label of the application
	 */
	private String mLabel;
	/**
	 * Value used to sort the list of applications
	 */
	private String mSortingValue;
	/**
	 * Icon drawable displayed in the list
	 */
	private Drawable mIcon;
	/**
	 * Detect if app is starred by user
	 */
	private boolean isFavorite;
	/**
	 * Char value used by indexed ListView
	 */
	private char headerChar;

	/**
	 * @param info
	 * @param context
	 */
	public AppEntry(ApplicationInfo info, Context context) {
		mInfo = info;
		isFavorite = Utils.isFavorite(mInfo.packageName, context);
		mApkFile = new File(info.sourceDir);
		loadLabels(context);
	}

	public ApplicationInfo getApplicationInfo() {
		return mInfo;
	}

	public String getLabel() {
		return mLabel;
	}

	public String getSortingValue() {
		return mSortingValue;
	}

	public Drawable getIcon(Context ctx) {
		if (mIcon == null) {
			if (mApkFile.exists()) {
				mIcon = mInfo.loadIcon(ctx.getPackageManager());
				return mIcon;
			} else {
				return ctx.getResources().getDrawable(
						android.R.drawable.sym_def_app_icon);
			}
		} else {
			return mIcon;
		}

	}

	@Override
	public String toString() {
		return mLabel;
	}
	
	

	public boolean isFavorite() {
		return isFavorite;
	}

	public void setFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
	}

	/**
	 * Generate the labels
	 * 
	 * @param ctx
	 */
	private void loadLabels(Context ctx) {
		if (mLabel == null) {
			if (!mApkFile.exists()) {
				mLabel = mInfo.packageName;
			} else {
				CharSequence label = mInfo.loadLabel(ctx.getPackageManager());
				mLabel = label != null ? label.toString() : mInfo.packageName;
			}
		}

		if (mSortingValue == null)
			mSortingValue = (isFavorite ? " " : "") + mLabel;

		headerChar = formatChar(mLabel);
	}

	/**
	 * Generate a char from a string to index the entry
	 * 
	 * @param s
	 * @return
	 */
	private char formatChar(String s) {
		if (isFavorite)
			return '☆';
		char c = Character.toUpperCase(s.charAt(0));
		if (c >= '0' && c <= '9')
			return '#';
		switch (c) {
		case 'À':
		case 'Á':
		case 'Â':
		case 'Ã':
		case 'Ä':
			return 'A';
		case 'É':
		case 'È':
		case 'Ê':
		case 'Ë':
			return 'E';
		default:
			return c;
		}
	}

	public char getHeaderChar() {
		return headerChar;
	}
}