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

import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import fr.simon.marquis.preferencesmanager.model.AppEntry;
import fr.simon.marquis.preferencesmanager.model.Backup;
import fr.simon.marquis.preferencesmanager.model.BackupContainer;
import fr.simon.marquis.preferencesmanager.ui.RootDialog;

public class Utils {

    public static final String TAG = Utils.class.getSimpleName();
    private static final String FAVORITES_KEY = "FAVORITES_KEY";
    private static final String TAG_ROOT_DIALOG = "RootDialog";
    private static final String PREF_SHOW_SYSTEM_APPS = "SHOW_SYSTEM_APPS";
    public static final String CMD_FIND_XML_FILES = "find /data/data/%s -type f -name \\*.xml";
    public static final String CMD_CAT_FILE = "cat %s";
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static ArrayList<AppEntry> applications;
    private static HashSet<String> favorites;

    public static ArrayList<AppEntry> getPreviousApps() {
        return applications;
    }

    public static void displayNoRoot(FragmentManager fm) {
        RootDialog.newInstance().show(fm, TAG_ROOT_DIALOG);
    }

    public static ArrayList<AppEntry> getApplications(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        if (pm == null) {
            applications = new ArrayList<AppEntry>();
        } else {
            boolean showSystemApps = isShowSystemApps(ctx);
            List<ApplicationInfo> appsInfo = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_DISABLED_COMPONENTS);
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
        Log.d(TAG, String.format("setFavorite(%s, %b)", packageName, favorite));
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
        Log.d(TAG, String.format("updateApplicationInfo(%s, %d)", packageName, favorite));
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
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(PREF_SHOW_SYSTEM_APPS, false);
    }

    public static void setShowSystemApps(Context ctx, boolean show) {
        Log.d(TAG, String.format("setShowSystemApps(%b)", show));
        Editor e = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        e.putBoolean(PREF_SHOW_SYSTEM_APPS, show);
        e.commit();
    }

    public static boolean hasHONEYCOMB() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static ArrayList<String> findXmlFiles(final String packageName) {
        Log.d(TAG, String.format("findXmlFiles(%s)", packageName));
        final ArrayList<String> files = new ArrayList<String>();
        CommandCapture cmd = new CommandCapture(CMD_FIND_XML_FILES.hashCode(), false, String.format(CMD_FIND_XML_FILES, packageName)) {
            @Override
            public void commandOutput(int i, String s) {
                if (!files.contains(s)) {
                    files.add(s);
                }
            }
        };

        synchronized (cmd) {
            try {
                RootTools.getShell(true).add(cmd).wait();
            } catch (Exception e) {
                Log.e(TAG, "Error in findXmlFiles", e);
            }
        }
        Log.d(TAG, "files: " + Arrays.toString(files.toArray()));
        return files;
    }

    public static String readFile(String file) {
        Log.d(TAG, String.format("readFile(%s)", file));
        final StringBuilder sb = new StringBuilder();
        CommandCapture cmd = new CommandCapture(0, false, String.format(CMD_CAT_FILE, file)) {
            @Override
            public void commandOutput(int i, String s) {
                sb.append(s).append(LINE_SEPARATOR);
            }
        };

        synchronized (cmd) {
            try {
                RootTools.getShell(true).add(cmd).wait();
            } catch (Exception e) {
                Log.e(TAG, "Error in readFile", e);
            }
        }

        return sb.toString();
    }

    public static BackupContainer getBackups(Context ctx, String packageName) {
        Log.d(TAG, String.format("getBackups(%s)", packageName));
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        BackupContainer container = null;
        try {
            container = BackupContainer.fromJSON(new JSONArray(sp.getString(packageName, "[]")));
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
        String str = container.toJSON().toString();
        ed.putString(packageName, str);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
            ed.apply();
        } else {
            ed.commit();
        }
    }

    public static boolean backupFile(Backup backup, String fileName, Context ctx) {
        Log.d(TAG, String.format("backupFile(%s, %s)", String.valueOf(backup.getTime()), fileName));
        java.io.File filesDir = ctx.getFilesDir();
        if (filesDir == null) {
            return false;
        }
        java.io.File destination = new java.io.File(ctx.getFilesDir(), String.valueOf(backup.getTime()));
        RootTools.copyFile(fileName, destination.getAbsolutePath(), true, true);
        Log.d(TAG, String.format("backupFile --> " + destination));
        return true;
    }


    public static String getBackupContent(Backup backup, Context ctx) {
        Log.d(TAG, String.format("getBackupContent(%s)", String.valueOf(backup.getTime())));
        BufferedReader input = null;
        StringBuilder buffer = new StringBuilder();
        try {
            input = new BufferedReader(new InputStreamReader(ctx.openFileInput(String.valueOf(backup.getTime()))));
            String line;
            while ((line = input.readLine()) != null) {
                buffer.append(line).append(LINE_SEPARATOR);
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return buffer.toString();
    }

    public static Drawable findDrawable(String packageName, Context ctx) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }

        if (applications != null) {
            for (AppEntry app : applications) {
                if (packageName.equals(app.getApplicationInfo().packageName)) {
                    return app.getIcon(ctx);
                }
            }
        } else {
            try {
                PackageManager pm = ctx.getPackageManager();
                if (pm == null) {
                    return null;
                }
                ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
                if (applicationInfo != null) {
                    AppEntry appEntry = new AppEntry(applicationInfo, ctx);
                    return appEntry.getIcon(ctx);
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return null;
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

    public static boolean updatePreferences(Context ctx, String preferences, String mFile) {
        Log.d(TAG, String.format("updatePreferences(%s", mFile));
        File filesDir = ctx.getFilesDir();
        if (filesDir == null) {
            return false;
        }
        File destination = new File(ctx.getFilesDir(), "tmp");
        try {

            if (!destination.exists()) {
                if (!destination.createNewFile()) {
                    return false;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error while creating tmp file", e);
            return false;
        }

        RootTools.copyFile(mFile, destination.getAbsolutePath(), true, true);
        FileWriter fw;
        try {
            fw = new FileWriter(destination, true);
            fw.write(preferences);
            fw.close();
        } catch (IOException e) {
            Log.e(TAG, "Error while writing tmp file", e);
            return false;
        }
        RootTools.copyFile(destination.getAbsolutePath(), mFile, true, true);
        Log.d(TAG, "Preferences correctly updated");
        return true;
    }
}
