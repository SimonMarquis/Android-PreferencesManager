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
package fr.simon.marquis.preferencesmanager.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import fr.simon.marquis.preferencesmanager.R;

public class AboutDialog extends DialogFragment {

    private static final String TAG = "ABOUT_DIALOG";
    private static final String ARG_EXIT = "EXIT";
    private static final String VERSION_UNAVAILABLE = "N/A";

    private boolean mExit;

    public static void show(FragmentManager fm, boolean exit) {
        AboutDialog newFragment = AboutDialog.newInstance(exit);
        newFragment.show(fm, TAG);
    }

    public static AboutDialog newInstance(boolean exit) {
        AboutDialog frag = new AboutDialog();
        Bundle args = new Bundle();
        args.putBoolean(ARG_EXIT, exit);
        frag.setArguments(args);
        return frag;
    }

    /**
     * Check if this Dialog has already been displayed once.
     *
     * @param ctx .
     * @return .
     */
    public static boolean alreadyDisplayed(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).contains(TAG);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null) {
            mExit = getArguments().getBoolean(ARG_EXIT);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() == null || getActivity().getPackageManager() == null) {
            return null;
        }
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(TAG, true).commit();
        PackageManager pm = getActivity().getPackageManager();
        String packageName = getActivity().getPackageName();
        String versionName;
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = VERSION_UNAVAILABLE;
        }

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View rootView = layoutInflater.inflate(R.layout.dialog_about, null);
        assert rootView != null;
        TextView nameAndVersionView = (TextView) rootView.findViewById(R.id.app_name_and_version);
        nameAndVersionView.setText(Html.fromHtml(getString(R.string.app_name_and_version, versionName)));
        TextView aboutBodyView = (TextView) rootView.findViewById(R.id.about_body);
        aboutBodyView.setText(Html.fromHtml(getString(R.string.about_body)));
        aboutBodyView.setMovementMethod(new LinkMovementMethod());

        return new AlertDialog.Builder(getActivity()).setView(rootView)
                .setPositiveButton(mExit ? R.string.exit : R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        if (mExit) {
                            getActivity().finish();
                        }
                    }
                }).create();
    }

}
