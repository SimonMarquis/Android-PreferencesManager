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

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import eu.chainfire.libsuperuser.Shell;
import fr.simon.marquis.preferencesmanager.model.AppEntry;
import fr.simon.marquis.preferencesmanager.model.BackupContainer;
import fr.simon.marquis.preferencesmanager.model.PreferenceFile;
import fr.simon.marquis.preferencesmanager.ui.RootDialog;

public class Utils {

    public static final String TAG = Utils.class.getSimpleName();
    private static final String FAVORITES_KEY = "FAVORITES_KEY";
    private static final String VERSION_CODE_KEY = "VERSION_CODE";
    public static final String BACKUP_PREFIX = "BACKUP_";
    private static final String TAG_ROOT_DIALOG = "RootDialog";
    private static final String PREF_SHOW_SYSTEM_APPS = "SHOW_SYSTEM_APPS";
    public static final String CMD_FIND_XML_FILES = "find /data/data/%s -type f -name \\*.xml";
    public static final String CMD_CHOWN = "chown %s.%s \"%s\"";
    public static final String CMD_CAT_FILE = "cat \"%s\"";
    public static final String CMD_CP = "cp \"%s\" \"%s\"";
    public static final String TMP_FILE = ".temp";
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String PACKAGE_NAME_PATTERN = "^[a-zA-Z_\\$][\\w\\$]*(?:\\.[a-zA-Z_\\$][\\w\\$]*)*$";

    private static ArrayList<AppEntry> applications;
    private static HashSet<String> favorites;

    public static void displayNoRoot(FragmentManager fm) {
        fm.beginTransaction().add(RootDialog.newInstance(), TAG_ROOT_DIALOG).commitAllowingStateLoss();
    }

    public static ArrayList<AppEntry> getPreviousApps() {
        return applications;
    }

    public static ArrayList<AppEntry> getApplications(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        if (pm == null) {
            applications = new ArrayList<AppEntry>();
        } else {
            boolean showSystemApps = isShowSystemApps(ctx);
            List<ApplicationInfo> appsInfo = pm.getInstalledApplications(0);
            if (appsInfo == null) {
                appsInfo = new ArrayList<ApplicationInfo>();
            }

            List<AppEntry> entries = new ArrayList<AppEntry>(appsInfo.size());
            for (ApplicationInfo a : appsInfo) {
                if (showSystemApps || (a.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    entries.add(new AppEntry(a, ctx));
                }
            }

            Collections.sort(entries, new MyComparator());
            applications = new ArrayList<AppEntry>(entries);
        }
        Log.d(TAG, "Applications: " + Arrays.toString(applications.toArray()));
        return applications;
    }

    public static void setFavorite(String packageName, boolean favorite, Context ctx) {
        Log.d(TAG, String.format("setFavorite(%s, %s)", packageName, favorite));
        initFavorites(ctx);

        if (favorite) {
            favorites.add(packageName);
        } else {
            favorites.remove(packageName);
        }

        Editor ed = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        if (favorites.isEmpty()) {
            ed.remove(FAVORITES_KEY);
        } else {
            ed.putString(FAVORITES_KEY, new JSONArray(favorites).toString());
        }

        ed.apply();
        updateApplicationInfo(packageName, favorite);
    }

    private static void updateApplicationInfo(String packageName, boolean favorite) {
        Log.d(TAG, String.format("updateApplicationInfo(%s, %s)", packageName, favorite));
        for (AppEntry a : applications) {
            if (a.getApplicationInfo().packageName.equals(packageName)) {
                a.setFavorite(favorite);
                return;
            }
        }
    }

    public static boolean isFavorite(String packageName, Context ctx) {
        initFavorites(ctx);
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
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(PREF_SHOW_SYSTEM_APPS, false);
    }

    public static void setShowSystemApps(Context ctx, boolean show) {
        Log.d(TAG, String.format("setShowSystemApps(%s)", show));
        Editor e = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        e.putBoolean(PREF_SHOW_SYSTEM_APPS, show);
        e.commit();
    }

    public static List<String> findXmlFiles(final String packageName) {
        Log.d(TAG, String.format("findXmlFiles(%s)", packageName));
        List<String> files = Shell.SU.run(String.format(CMD_FIND_XML_FILES, packageName));
        Log.d(TAG, "files: " + Arrays.toString(files.toArray()));
        return files;
    }

    public static String readFile(String file) {
        Log.d(TAG, String.format("readFile(%s)", file));
        final StringBuilder sb = new StringBuilder();
        List<String> lines = Shell.SU.run(String.format(CMD_CAT_FILE, file));
        if(lines != null) {
            for (String line : lines) {
                sb.append(line).append(LINE_SEPARATOR);
            }
        }
        return sb.toString();
    }

    public static void checkBackups(Context ctx) {
        Log.d(TAG, "checkBackups");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean needToBackport = needToBackport(sp);
        Log.d(TAG, "needToBackport ? " + needToBackport);
        saveVersionCode(ctx, sp);
        if (!needToBackport) {
            return;
        }
        backportBackups(ctx);
    }

    private static void backportBackups(Context ctx) {
        Log.d(TAG, "backportBackups");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        Editor editor = sp.edit();
        Map<String, ?> keys = sp.getAll();
        if (keys == null) {
            return;
        }

        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            String key = entry.getKey();
            String value = String.valueOf(entry.getValue());

            Log.d(TAG, "key: " + key);

            if (!key.startsWith(BACKUP_PREFIX) && key.matches(PACKAGE_NAME_PATTERN) && value.contains("FILE") && value.contains("BACKUPS")) {
                Log.d(TAG, " need to be updated");
                JSONArray array = null;
                try {
                    array = new JSONArray(value);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject container = array.getJSONObject(i);
                        String file = container.getString("FILE");
                        if (!file.startsWith(FILE_SEPARATOR)) {
                            container.put("FILE", FILE_SEPARATOR + file);
                        }
                        JSONArray backups = container.getJSONArray("BACKUPS");
                        ArrayList<String> values = new ArrayList<String>(backups.length());
                        for (int j = 0; j < backups.length(); j++) {
                            values.add(String.valueOf(backups.getJSONObject(j).getLong("TIME")));
                        }
                        container.put("BACKUPS", new JSONArray(values));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error trying to backport Backups", e);
                }
                if (array != null) {
                    editor.putString(BACKUP_PREFIX + key, array.toString());
                }
                editor.remove(key);
            }
        }

        editor.commit();
    }

    private static boolean needToBackport(SharedPreferences sp) {
        // 18 was the latest version code release with old Backup system
        return sp.getInt(VERSION_CODE_KEY, 0) <= 18;
    }

    private static void saveVersionCode(Context ctx, SharedPreferences sp) {
        try {
            sp.edit().putInt(VERSION_CODE_KEY, ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode).commit();
        } catch (Exception e) {
            Log.e(TAG, "Error trying to save the version code", e);
        }
    }

    public static BackupContainer getBackups(Context ctx, String packageName) {
        Log.d(TAG, String.format("getBackups(%s)", packageName));
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        BackupContainer container = null;
        try {
            container = BackupContainer.fromJSON(new JSONArray(sp.getString(BACKUP_PREFIX + packageName, "[]")));
        } catch (JSONException ignore) {
        }
        if (container == null) {
            container = new BackupContainer();
        }
        Log.d(TAG, "backups: " + container.toJSON().toString());
        return container;
    }

    public static void saveBackups(Context ctx, String packageName, BackupContainer container) {
        Log.d(TAG, String.format("saveBackups(%s, %s)", packageName, container.toJSON().toString()));
        Editor ed = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        if (container.isEmpty()) {
            ed.remove(BACKUP_PREFIX + packageName);
        } else {
            ed.putString(BACKUP_PREFIX + packageName, container.toJSON().toString());
        }
        ed.apply();
    }

    public static boolean backupFile(String backup, String fileName, Context ctx) {
        Log.d(TAG, String.format("backupFile(%s, %s)", backup, fileName));
        File destination = new File(ctx.getFilesDir(), backup);
        Shell.SU.run(String.format(CMD_CP, fileName, destination.getAbsolutePath()));
        Log.d(TAG, String.format("backupFile --> " + destination));
        return true;
    }

    public static boolean restoreFile(Context ctx, String backup, String fileName, String packageName) {
        Log.d(TAG, String.format("restoreFile(%s, %s, %s)", backup, fileName, packageName));
        File backupFile = new File(ctx.getFilesDir(), backup);
        Shell.SU.run(String.format(CMD_CP, backupFile.getAbsolutePath(), fileName));

        if (!fixUserAndGroupId(ctx, fileName, packageName)) {
            Log.e(TAG, "Error fixUserAndGroupId");
            return false;
        }

        ((ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE)).killBackgroundProcesses(packageName);

        Log.d(TAG, String.format("restoreFile --> " + fileName));
        return true;
    }

    public static String extractFileName(String s) {
        if (TextUtils.isEmpty(s)) {
            return null;
        }
        return s.substring(s.lastIndexOf(FILE_SEPARATOR) + 1);
    }

    public static String extractFilePath(String s) {
        if (TextUtils.isEmpty(s)) {
            return null;
        }
        return s.substring(0, Math.max(s.length(), s.lastIndexOf(FILE_SEPARATOR)));
    }

    public static boolean savePreferences(PreferenceFile preferenceFile, String file, String packageName, Context ctx) {
        Log.d(TAG, String.format("savePreferences(%s, %s)", file, packageName));
        if (preferenceFile == null) {
            Log.e(TAG, "Error preferenceFile is null");
            return false;
        }

        if (!preferenceFile.isValid()) {
            Log.e(TAG, "Error preferenceFile is not valid");
            return false;
        }

        String preferences = preferenceFile.toXml();
        if (TextUtils.isEmpty(preferences)) {
            Log.e(TAG, "Error preferences is empty");
            return false;
        }

        File tmpFile = new File(ctx.getFilesDir(), TMP_FILE);
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ctx.openFileOutput(TMP_FILE, Context.MODE_PRIVATE));
            outputStreamWriter.write(preferences);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "Error writing temporary file", e);
            return false;
        }

        Shell.SU.run(String.format(CMD_CP, tmpFile.getAbsolutePath(), file));

        if (!fixUserAndGroupId(ctx, file, packageName)) {
            Log.e(TAG, "Error fixUserAndGroupId");
            return false;
        }

        if (!tmpFile.delete()) {
            Log.e(TAG, "Error deleting temporary file");
        }

        ((ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE)).killBackgroundProcesses(packageName);
        Log.d(TAG, "Preferences correctly updated");
        return true;
    }


    /**
     * Put User id and Group id back to the corresponding app with this cmd: `chown uid.gid filename`
     *
     * @param ctx         Context
     * @param file        The file to fix
     * @param packageName The packageName of the app
     * @return true if success
     */
    private static boolean fixUserAndGroupId(Context ctx, String file, String packageName) {
        Log.d(TAG, String.format("fixUserAndGroupId(%s, %s)", file, packageName));
        String uid;
        PackageManager pm = ctx.getPackageManager();
        if (pm == null) {
            return false;
        }
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            uid = String.valueOf(appInfo.uid);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "error while getting uid", e);
            return false;
        }

        if (TextUtils.isEmpty(uid)) {
            Log.d(TAG, "uid is undefined");
            return false;
        }

        Shell.SU.run(String.format(CMD_CHOWN, uid, uid, file));
        return true;
    }
}
