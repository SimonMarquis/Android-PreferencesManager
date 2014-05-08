package fr.simon.marquis.preferencesmanager.ui;

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

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.PreferenceFile;
import fr.simon.marquis.preferencesmanager.model.XmlColorTheme;
import fr.simon.marquis.preferencesmanager.model.XmlColorTheme.ColorTagEnum;
import fr.simon.marquis.preferencesmanager.model.XmlColorTheme.ColorThemeEnum;
import fr.simon.marquis.preferencesmanager.model.XmlFontSize;
import fr.simon.marquis.preferencesmanager.util.Ui;
import fr.simon.marquis.preferencesmanager.util.Utils;

public class FileEditorActivity extends ActionBarActivity implements TextWatcher {

    private XmlFontSize mXmlFontSize;
    private ColorThemeEnum mColorTheme;
    private XmlColorTheme mXmlColorTheme;

    private String mFile;
    private String mTitle;
    private String mPackageName;
    private EditText mEditText;

    private static final String KEY_HAS_CONTENT_CHANGED = "HAS_CONTENT_CHANGED";
    private static final String KEY_COLOR_THEME = "KEY_COLOR_THEME";
    private static final String KEY_FONT_SIZE = "KEY_FONT_SIZE";
    private static final String KEY_NEED_UPDATE_ON_ACTIVITY_FINISH = "NEED_UPDATE_ON_ACTIVITY_FINISH";
    private boolean mHasContentChanged;
    private boolean mNeedUpdateOnActivityFinish = false;

    private final Pattern TAG_START = Pattern.compile("</?[-\\w\\?]+", Pattern.CASE_INSENSITIVE);
    private final Pattern TAG_END = Pattern.compile("\\??/?>");
    private final Pattern TAG_ATTRIBUTE_NAME = Pattern.compile("\\s(\\w*)\\=");
    private final Pattern TAG_ATTRIBUTE_VALUE = Pattern.compile("[a-z\\-]*\\=(\"[^\"]*\")");
    private final Pattern TAG_ATTRIBUTE_VALUE_2 = Pattern.compile("[a-z\\-]*\\=(\'[^\']*\')");
    private final Pattern COMMENT_START = Pattern.compile("<!--");
    private final Pattern COMMENT_END = Pattern.compile("-->");
    // private final Pattern CDATA_START = Pattern.compile("<\\[CDATA\\[");
    // private final Pattern CDATA_END = Pattern.compile("\\]\\]>");

    @Override
    protected void onCreate(Bundle arg0) {
        setTheme(App.theme.theme);
        super.onCreate(arg0);
        setContentView(R.layout.activity_file_editor);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle b = getIntent().getExtras();
        if (b == null) {
            finish();
            return;
        }

        mFile = b.getString(PreferencesFragment.ARG_FILE);
        mTitle = Utils.extractFileName(mFile);
        mPackageName = b.getString(PreferencesFragment.ARG_PACKAGE_NAME);

        Drawable drawable = Utils.findDrawable(mPackageName, this);
        if (drawable != null) {
            getSupportActionBar().setIcon(drawable);
        }

        mEditText = (EditText) findViewById(R.id.editText);
        //Hack to prevent EditText to request focus when the Activity is created
        mEditText.post(new Runnable() {
            @Override
            public void run() {
                mEditText.setFocusable(true);
                mEditText.setFocusableInTouchMode(true);
            }
        });


        if (arg0 == null) {
            mEditText.setText(Utils.readFile(mFile));
            mColorTheme = ColorThemeEnum.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_COLOR_THEME, ColorThemeEnum.ECLIPSE.name()));
            setXmlFontSize(XmlFontSize.generateSize(PreferenceManager.getDefaultSharedPreferences(this).getInt(KEY_FONT_SIZE, XmlFontSize.MEDIUM.getSize())));
        } else {
            mHasContentChanged = arg0.getBoolean(KEY_HAS_CONTENT_CHANGED, false);
            mNeedUpdateOnActivityFinish = arg0.getBoolean(KEY_NEED_UPDATE_ON_ACTIVITY_FINISH, false);
            if (mNeedUpdateOnActivityFinish) {
                setResult(RESULT_OK);
            }
            mColorTheme = ColorThemeEnum.valueOf(arg0.getString(KEY_COLOR_THEME));
            setXmlFontSize(XmlFontSize.generateSize(arg0.getInt(KEY_FONT_SIZE)));
        }
        mXmlColorTheme = XmlColorTheme.createTheme(getResources(), mColorTheme);

        updateTitle();
        invalidateOptionsMenu();

        highlightXMLText(mEditText.getText());

        mEditText.clearFocus();
    }

    @Override
    protected void onResume() {
        mEditText.addTextChangedListener(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mEditText.removeTextChangedListener(this);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_HAS_CONTENT_CHANGED, mHasContentChanged);
        outState.putString(KEY_COLOR_THEME, mColorTheme.name());
        outState.putInt(KEY_FONT_SIZE, mXmlFontSize.getSize());
        outState.putBoolean(KEY_NEED_UPDATE_ON_ACTIVITY_FINISH, mNeedUpdateOnActivityFinish);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (mHasContentChanged) {
            showSavePopup();
            return;
        }
        super.onBackPressed();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_save).setEnabled(mHasContentChanged).setIcon(mHasContentChanged ? R.drawable.ic_action_save : R.drawable.ic_action_save_disabled);

        menu.findItem(R.id.action_theme_eclipse).setChecked(false);
        menu.findItem(R.id.action_theme_google).setChecked(false);
        menu.findItem(R.id.action_theme_roboticket).setChecked(false);
        menu.findItem(R.id.action_theme_notepad).setChecked(false);
        menu.findItem(R.id.action_theme_netbeans).setChecked(false);

        if(mColorTheme == ColorThemeEnum.ECLIPSE) {
            menu.findItem(R.id.action_theme_eclipse).setChecked(true);
        }
        if(mColorTheme == ColorThemeEnum.GOOGLE) {
            menu.findItem(R.id.action_theme_google).setChecked(true);
        }
        if(mColorTheme == ColorThemeEnum.ROBOTICKET) {
            menu.findItem(R.id.action_theme_roboticket).setChecked(true);
        }
        if(mColorTheme == ColorThemeEnum.NOTEPAD) {
            menu.findItem(R.id.action_theme_notepad).setChecked(true);
        }
        if(mColorTheme == ColorThemeEnum.NETBEANS) {
            menu.findItem(R.id.action_theme_netbeans).setChecked(true);
        }

        menu.findItem(R.id.action_size_extra_small).setChecked(false);
        menu.findItem(R.id.action_size_small).setChecked(false);
        menu.findItem(R.id.action_size_medium).setChecked(false);
        menu.findItem(R.id.action_size_large).setChecked(false);
        menu.findItem(R.id.action_size_extra_large).setChecked(false);

        if (mXmlFontSize == XmlFontSize.EXTRA_SMALL) {
            menu.findItem(R.id.action_size_extra_small).setChecked(true);
        }
        if (mXmlFontSize == XmlFontSize.SMALL) {
            menu.findItem(R.id.action_size_small).setChecked(true);
        }
        if (mXmlFontSize == XmlFontSize.MEDIUM) {
            menu.findItem(R.id.action_size_medium).setChecked(true);
        }
        if (mXmlFontSize == XmlFontSize.LARGE) {
            menu.findItem(R.id.action_size_large).setChecked(true);
        }
        if (mXmlFontSize == XmlFontSize.EXTRA_LARGE) {
            menu.findItem(R.id.action_size_extra_large).setChecked(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mHasContentChanged) {
                    showSavePopup();
                } else {
                    finish();
                }
                break;
            case R.id.action_save:
                save();
                break;
            case R.id.action_theme_eclipse:
                setXmlColorTheme(ColorThemeEnum.ECLIPSE);
                break;
            case R.id.action_theme_google:
                setXmlColorTheme(ColorThemeEnum.GOOGLE);
                break;
            case R.id.action_theme_roboticket:
                setXmlColorTheme(ColorThemeEnum.ROBOTICKET);
                break;
            case R.id.action_theme_notepad:
                setXmlColorTheme(ColorThemeEnum.NOTEPAD);
                break;
            case R.id.action_theme_netbeans:
                setXmlColorTheme(ColorThemeEnum.NETBEANS);
                break;
            case R.id.action_size_extra_small:
                setXmlFontSize(XmlFontSize.EXTRA_SMALL);
                break;
            case R.id.action_size_small:
                setXmlFontSize(XmlFontSize.SMALL);
                break;
            case R.id.action_size_medium:
                setXmlFontSize(XmlFontSize.MEDIUM);
                break;
            case R.id.action_size_large:
                setXmlFontSize(XmlFontSize.LARGE);
                break;
            case R.id.action_size_extra_large:
                setXmlFontSize(XmlFontSize.EXTRA_LARGE);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setXmlFontSize(XmlFontSize size) {
        if (mXmlFontSize != size) {
            mXmlFontSize = size;
            invalidateOptionsMenu();
            mEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, mXmlFontSize.getSize());

            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(KEY_FONT_SIZE, mXmlFontSize.getSize()).commit();
        }
    }

    private void setXmlColorTheme(ColorThemeEnum theme) {
        if (mColorTheme != theme) {
            mColorTheme = theme;
            mXmlColorTheme = XmlColorTheme.createTheme(getResources(), mColorTheme);
            invalidateOptionsMenu();
            highlightXMLText(mEditText.getText());

            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(KEY_COLOR_THEME, mColorTheme.name()).commit();
        }
    }

    private boolean save() {
        Editable editable = mEditText.getText();
        String preferences = editable == null ? "" : editable.toString();
        PreferenceFile pref = PreferenceFile.fromXml(preferences);
        if (Utils.savePreferences(pref, mFile, mPackageName, this)) {
            mNeedUpdateOnActivityFinish = true;
            setResult(RESULT_OK);
            Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
            Ui.hideSoftKeyboard(this, mEditText);
            mHasContentChanged = false;
            updateTitle();
            invalidateOptionsMenu();
            return true;
        } else {
            Toast.makeText(this, R.string.save_fail, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void updateTitle() {
        Spanned str = Html.fromHtml((mHasContentChanged ? "<font color='#33b5e5'><b>&#9679;</b></font> " : "") + mTitle);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(Ui.applyCustomTypeFace(str, this));
        }
    }

    private void clearSpans(Spannable source) {
        Object[] toRemoveSpans = source.getSpans(0, source.length(), ForegroundColorSpan.class);
        for (Object toRemoveSpan : toRemoveSpans) {
            source.removeSpan(toRemoveSpan);
        }
    }

    private void highlightXMLText(Spannable source) {
        clearSpans(source);
        generateSpan(source, TAG_START, mXmlColorTheme.getColor(ColorTagEnum.TAG));
        generateSpan(source, TAG_END, mXmlColorTheme.getColor(ColorTagEnum.TAG));
        generateSpan(source, TAG_ATTRIBUTE_VALUE, mXmlColorTheme.getColor(ColorTagEnum.ATTR_VALUE));
        generateSpan(source, TAG_ATTRIBUTE_VALUE_2, mXmlColorTheme.getColor(ColorTagEnum.ATTR_VALUE));
        generateSpan(source, TAG_ATTRIBUTE_NAME, mXmlColorTheme.getColor(ColorTagEnum.ATTR_NAME));
        generateSpan(source, COMMENT_START, mXmlColorTheme.getColor(ColorTagEnum.COMMENT));
        generateSpan(source, COMMENT_END, mXmlColorTheme.getColor(ColorTagEnum.COMMENT));
    }

    private static void generateSpan(Spannable source, Pattern p, int color) {
        final Matcher matcher = p.matcher(source);
        int start;
        int end;
        while (matcher.find()) {
            start = matcher.start();
            end = matcher.end();
            if (start != end) {
                source.setSpan(new ForegroundColorSpan(color), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!mHasContentChanged) {
            mHasContentChanged = true;
            updateTitle();
            invalidateOptionsMenu();
        }
        highlightXMLText(mEditText.getText());
    }

    private void showSavePopup() {
        new AlertDialog.Builder(this).setTitle(mTitle).setMessage(R.string.popup_edit_message).setIcon(R.drawable.ic_action_edit)
                .setNegativeButton(R.string.no, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setPositiveButton(R.string.yes, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (save()) {
                    finish();
                }
            }
        }).create().show();
    }
}
