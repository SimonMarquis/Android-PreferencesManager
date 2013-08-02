package fr.simon.marquis.preferencesmanager.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.PreferenceFile;
import fr.simon.marquis.preferencesmanager.model.XmlColorTheme;
import fr.simon.marquis.preferencesmanager.model.XmlColorTheme.ColorTagEnum;
import fr.simon.marquis.preferencesmanager.model.XmlColorTheme.ColorThemeEnum;
import fr.simon.marquis.preferencesmanager.util.Utils;

public class FileEditorActivity extends ActionBarActivity implements
		TextWatcher {

	private ColorThemeEnum mColorTheme;
	private XmlColorTheme mXmlColorTheme;

	private String mName;
	private String mPath;
	private String mPackageName;
	private EditText mEditText;

	private static final String KEY_HAS_CONTENT_CHANGED = "HAS_CONTENT_CHANGED";
	private static final String KEY_COLOR_THEME = "KEY_COLOR_THEME";
	private boolean mHasContentChanged;

	Pattern TAG_START = Pattern.compile("</?[\\w-\\?]+",
			Pattern.CASE_INSENSITIVE);
	Pattern TAG_END = Pattern.compile("\\??/?>");
	Pattern TAG_ATTRIBUTE_NAME = Pattern.compile("\\s(\\w*)\\=");
	Pattern TAG_ATTRIBUTE_VALUE = Pattern.compile("[a-z\\-]*\\=(\"[^\"]*\")");
	Pattern TAG_ATTRIBUTE_VALUE_2 = Pattern.compile("[a-z\\-]*\\=(\'[^\']*\')");
	Pattern COMMENT_START = Pattern.compile("<!--");
	Pattern COMMENT_END = Pattern.compile("-->");
	Pattern CDATA_START = Pattern.compile("<\\[CDATA\\[");
	Pattern CDATA_END = Pattern.compile("\\]\\]>");

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_file_editor);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle b = getIntent().getExtras();
		if (b == null) {
			finish();
			return;
		}

		mName = b.getString(PreferencesFragment.ARG_NAME);
		mPath = b.getString(PreferencesFragment.ARG_PATH);
		mPackageName = b.getString(PreferencesFragment.ARG_PACKAGE_NAME);

		mEditText = (EditText) findViewById(R.id.editText);

		if (arg0 == null) {
			mEditText.setText(b.getString("CONTENT"));
			mColorTheme = ColorThemeEnum.valueOf(PreferenceManager
					.getDefaultSharedPreferences(this).getString(
							KEY_COLOR_THEME, ColorThemeEnum.ECLIPSE.name()));
		} else {
			mHasContentChanged = arg0
					.getBoolean(KEY_HAS_CONTENT_CHANGED, false);
			mColorTheme = ColorThemeEnum.valueOf(arg0
					.getString(KEY_COLOR_THEME));
		}
		mXmlColorTheme = XmlColorTheme.createTheme(getResources(), mColorTheme);

		updateTitle();
		supportInvalidateOptionsMenu();

		highlightXMLText(mEditText.getText());

		mEditText.clearFocus();
		mEditText.setTypeface(Typeface.createFromAsset(getAssets(),
				"fonts/RobotoSlab-Regular.ttf"));
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

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_save)
				.setEnabled(mHasContentChanged)
				.setIcon(
						mHasContentChanged ? R.drawable.ic_action_tick
								: R.drawable.ic_action_tick_disabled);

		menu.findItem(R.id.action_theme_eclipse).setChecked(
				mColorTheme == ColorThemeEnum.ECLIPSE);
		menu.findItem(R.id.action_theme_google).setChecked(
				mColorTheme == ColorThemeEnum.GOOGLE);
		menu.findItem(R.id.action_theme_roboticket).setChecked(
				mColorTheme == ColorThemeEnum.ROBOTICKET);
		menu.findItem(R.id.action_theme_notepad).setChecked(
				mColorTheme == ColorThemeEnum.NOTEPAD);
		menu.findItem(R.id.action_theme_netbeans).setChecked(
				mColorTheme == ColorThemeEnum.NETBEANS);
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
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setXmlColorTheme(ColorThemeEnum theme) {
		if (mColorTheme != theme) {
			mColorTheme = theme;
			mXmlColorTheme = XmlColorTheme.createTheme(getResources(),
					mColorTheme);
			supportInvalidateOptionsMenu();
			highlightXMLText(mEditText.getText());

			PreferenceManager.getDefaultSharedPreferences(this).edit()
					.putString(KEY_COLOR_THEME, mColorTheme.name()).commit();
		}
	}

	private boolean save() {
		String preferences = mEditText.getText().toString();
		if (PreferenceFile.save(preferences, mPath + "/" + mName, this,
				mPackageName)) {
			setResult(RESULT_OK);
			Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT)
					.show();
			Utils.hideSoftKeyboard(this, mEditText);
			mHasContentChanged = false;
			updateTitle();
			supportInvalidateOptionsMenu();
			return true;
		} else {
			Toast.makeText(this, R.string.save_fail, Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	private void updateTitle() {
		getActionBar()
				.setTitle(
						Html.fromHtml(mName
								+ (mHasContentChanged ? " <font color='#33b5e5'><b>&#9679;</b></font>"
										: "")));
	}

	private void clearSpans(Spannable source) {
		Object[] toRemoveSpans = source.getSpans(0, source.length(),
				ForegroundColorSpan.class);
		for (int i = 0; i < toRemoveSpans.length; i++)
			source.removeSpan(toRemoveSpans[i]);
	}

	private Spannable highlightXMLText(Spannable source) {
		Log.e(Utils.TAG, "Start to highlightXMLText");
		long start = System.currentTimeMillis();
		clearSpans(source);
		generateSpan(source, TAG_START,
				mXmlColorTheme.getColor(ColorTagEnum.TAG));
		generateSpan(source, TAG_END, mXmlColorTheme.getColor(ColorTagEnum.TAG));
		generateSpan(source, TAG_ATTRIBUTE_VALUE,
				mXmlColorTheme.getColor(ColorTagEnum.ATTR_VALUE));
		generateSpan(source, TAG_ATTRIBUTE_VALUE_2,
				mXmlColorTheme.getColor(ColorTagEnum.ATTR_VALUE));
		generateSpan(source, TAG_ATTRIBUTE_NAME,
				mXmlColorTheme.getColor(ColorTagEnum.ATTR_NAME));
		generateSpan(source, COMMENT_START,
				mXmlColorTheme.getColor(ColorTagEnum.COMMENT));
		generateSpan(source, COMMENT_END,
				mXmlColorTheme.getColor(ColorTagEnum.COMMENT));
		Log.e(Utils.TAG, (System.currentTimeMillis() - start)
				+ "ms to highlightXMLText");
		return source;
	}

	private static void generateSpan(Spannable source, Pattern p, int color) {
		final Matcher matcher = p.matcher(source);
		int start;
		int end;
		while (matcher.find()) {
			start = matcher.start();
			end = matcher.end();
			if (start != end) {
				source.setSpan(new ForegroundColorSpan(color), matcher.start(),
						matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (!mHasContentChanged) {
			mHasContentChanged = true;
			updateTitle();
			supportInvalidateOptionsMenu();
		}
		highlightXMLText(mEditText.getText());
	}

	private void showSavePopup() {
		new AlertDialog.Builder(this).setTitle(mName)
				.setMessage(R.string.popup_edit_message)
				.setIcon(R.drawable.ic_action_edit)
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
