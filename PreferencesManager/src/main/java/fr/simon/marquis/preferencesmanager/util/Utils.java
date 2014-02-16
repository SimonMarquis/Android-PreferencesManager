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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.spazedog.lib.rootfw.container.FileStat;

import fr.simon.marquis.preferencesmanager.model.AppEntry;
import fr.simon.marquis.preferencesmanager.model.File;
import fr.simon.marquis.preferencesmanager.model.Files;
import fr.simon.marquis.preferencesmanager.roboto.RobotoTypefaceManager;
import fr.simon.marquis.preferencesmanager.ui.App;
import fr.simon.marquis.preferencesmanager.ui.RootDialog;

public class Utils {

	public static final String TAG = "PreferencesManager";
	private static final String FAVORITES_KEY = "FAVORITES_KEY";
	private static ArrayList<AppEntry> applications;
	private static HashSet<String> favorites;

	public static ArrayList<AppEntry> getPreviousApps() {
		return applications;
	}

	public static void displayNoRoot(Context ctx, FragmentManager fragmentManager) {
		FragmentTransaction tr = fragmentManager.beginTransaction();
		DialogFragment newFragment = RootDialog.newInstance();
		tr.add(newFragment, "RootDialog");
		tr.commitAllowingStateLoss();
	}

	public static ArrayList<AppEntry> getApplications(Context ctx) {
		boolean showSystemApps = isShowSystemApps(ctx);
		List<ApplicationInfo> appsInfo = ctx.getPackageManager().getInstalledApplications(
				PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_DISABLED_COMPONENTS);
		if (appsInfo == null)
			appsInfo = new ArrayList<ApplicationInfo>();

		List<AppEntry> entries = new ArrayList<AppEntry>(appsInfo.size());
		for (ApplicationInfo a : appsInfo) {
			if (showSystemApps || (a.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				entries.add(new AppEntry(a, ctx));
			}
		}

		Collections.sort(entries, new MyComparator());
		applications = new ArrayList<AppEntry>(entries);
		return applications;
	}

	public static void verifyFavorites(Context ctx) {
		for (AppEntry a : applications) {
			a.setFavorite(isFavorite(a.getApplicationInfo().packageName, ctx));
		}
	}

	public static void setFavorite(String packageName, boolean favorite, Context ctx) {

		if (favorites == null) {
			initFavorites(ctx);
		}

		if (favorite) {
			favorites.add(packageName);
		} else {
			favorites.remove(packageName);
		}

		Editor ed = PreferenceManager.getDefaultSharedPreferences(ctx).edit();

		if (favorites.size() == 0) {
			ed.remove(FAVORITES_KEY);
		} else {
			JSONArray array = new JSONArray(favorites);
			ed.putString(FAVORITES_KEY, array.toString());
		}

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
			ed.apply();
		} else {
			ed.commit();
		}

		updateApplicationInfo(packageName, favorite);
	}

	private static void updateApplicationInfo(String packageName, boolean favorite) {
		for (AppEntry a : applications) {
			if (a.getApplicationInfo().packageName.equals(packageName)) {
				a.setFavorite(favorite);
				return;
			}
		}
	}

	public static boolean isFavorite(String packageName, Context ctx) {
		if (favorites == null) {
			initFavorites(ctx);
		}
		return favorites.contains(packageName);
	}

	private static void initFavorites(Context ctx) {
		if (favorites == null) {
			favorites = new HashSet<String>();

			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

			if (sp.contains(FAVORITES_KEY)) {
				try {
					JSONArray array = new JSONArray(sp.getString(FAVORITES_KEY, "[]"));
					for (int i = 0; i < array.length(); i++) {
						favorites.add(array.optString(i));
					}
				} catch (JSONException e) {
					Log.e(TAG, "error parsing JSON", e);
				}
			}
		}
	}

	public static boolean isShowSystemApps(Context ctx) {
		return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("SHOW_SYSTEM_APPS", false);
	}

	public static void setShowSystemApps(Context ctx, boolean show) {
		Editor e = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
		e.putBoolean("SHOW_SYSTEM_APPS", show);
		e.commit();
	}

	public static void debugFile(String file) {
		FileStat fileStat = App.getRoot().file.stat(file);
		Log.d(Utils.TAG, file + " [ `" + fileStat.access() + "` , `" + fileStat.link() + "` , `" + fileStat.mm() + "` , `" + fileStat.name()
				+ "` , `" + fileStat.permission() + "` , `" + fileStat.type() + "` , `" + fileStat.group() + "` , `" + fileStat.size() + "` , `"
				+ fileStat.user() + "` ]");
	}

	public static boolean hasHONEYCOMB() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public static Files findXmlFiles(String packageName) {
		String path = "data/data/" + packageName;
		ArrayList<FileStat> files = App.getRoot().file.statList(path);
		return findFiles(files, path, new Files());
	}

	public static Files findFiles(ArrayList<FileStat> files, String path, Files list) {
		if (files == null)
			return list;

		for (FileStat file : files) {
			if (file == null || TextUtils.isEmpty(file.name()))
				continue;
			if (".".equals(file.name()) || "..".equals(file.name()))
				continue;
			if ("d".equals(file.type())) {
				String p = path + "/" + file.name();
				findFiles(App.getRoot().file.statList(p), p, list);
				continue;
			}
			if ("f".equals(file.type()) && file.name().endsWith(".xml")) {
				list.add(new File(file.name(), path));
			}
		}

		return list;
	}

	public static void hideSoftKeyboard(Context context, View view) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public static SpannableString applyCustomTypeFace(CharSequence src, Context ctx) {
		SpannableString span = new SpannableString(src);

		span.setSpan(new CustomTypefaceSpan("", RobotoTypefaceManager.obtainTypeface(ctx, RobotoTypefaceManager.ROBOTOSLAB_REGULAR)), 0,
				span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return span;
	}

	public static Drawable findDrawable(String packageName, Context ctx) {
		if (TextUtils.isEmpty(packageName)) {
			return null;
		}
		for (AppEntry app : applications) {
			if (packageName.equals(app.getApplicationInfo().packageName)) {
				return app.getIcon(ctx);
			}
		}
		return null;
	}

	/**
	 * Highlight text
	 * 
	 * @param color
	 * 
	 * @param s
	 * @return
	 */
	public static SpannableStringBuilder createSpannable(Pattern pattern, int color, String s) {
		final SpannableStringBuilder spannable = new SpannableStringBuilder(s);
		if (pattern == null)
			return spannable;
		final Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			final ForegroundColorSpan span = new ForegroundColorSpan(color);
			final StyleSpan span2 = new StyleSpan(android.graphics.Typeface.BOLD);
			spannable.setSpan(span2, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			spannable.setSpan(span, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return spannable;
	}
}
