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

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.PreferenceType;

public class PreferenceDialog extends DialogFragment {
	private final static String KEY_TYPE = "KEY_TYPE";
	private final static String KEY_EDIT_MODE = "KEY_EDIT_MODE";
	private final static String KEY_EDIT_KEY = "KEY_EDIT_KEY";
	private final static String KEY_EDIT_VALUE = "KEY_EDIT_VALUE";

	private EditText mKey;
	private View mValue;

	private PreferenceType mPreferenceType;
	private boolean mEditMode;
	private String mEditKey;
	private Object mEditValue;

	private Button mBtnOK, mBtnKO, mBtnSUPPR, mBtnAddEntrySet;

	@SuppressWarnings("unchecked")
	public static PreferenceDialog newInstance(PreferenceType type,
			boolean editMode, String editKey, Object editValue) {
		PreferenceDialog frag = new PreferenceDialog();
		Bundle args = new Bundle();
		args.putString(KEY_TYPE, type.name());
		args.putBoolean(KEY_EDIT_MODE, editMode);
		args.putString(KEY_EDIT_KEY, editKey);
		if (editMode) {
			switch (type) {
			case BOOLEAN:
				args.putBoolean(KEY_EDIT_VALUE, (Boolean) editValue);
				break;
			case FLOAT:
				args.putFloat(KEY_EDIT_VALUE, (Float) editValue);
				break;
			case INT:
				args.putInt(KEY_EDIT_VALUE, (Integer) editValue);
				break;
			case LONG:
				args.putLong(KEY_EDIT_VALUE, (Long) editValue);
				break;
			case STRING:
				args.putString(KEY_EDIT_VALUE, (String) editValue);
				break;
			case STRINGSET:
				Object[] objArray = ((Set<String>) editValue).toArray();
				String[] stringArray = new String[objArray.length];
				for (int i = 0; i < stringArray.length; i++) {
					stringArray[i] = objArray[i].toString();
				}
				args.putStringArray(KEY_EDIT_VALUE, stringArray);
				break;
			case UNSUPPORTED:
				break;
			}
		}
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getArguments();
		mPreferenceType = PreferenceType.valueOf(b.getString(KEY_TYPE));
		mEditMode = b.getBoolean(KEY_EDIT_MODE);
		mEditKey = b.getString(KEY_EDIT_KEY);

		switch (mPreferenceType) {
		case BOOLEAN:
			mEditValue = b.getBoolean(KEY_EDIT_VALUE);
			break;
		case FLOAT:
			mEditValue = b.getFloat(KEY_EDIT_VALUE);
			break;
		case INT:
			mEditValue = b.getInt(KEY_EDIT_VALUE);
			break;
		case LONG:
			mEditValue = b.getLong(KEY_EDIT_VALUE);
			break;
		case STRING:
			mEditValue = b.getString(KEY_EDIT_VALUE);
			break;
		case STRINGSET:
			// Convert to Set<String>
			mEditValue = b.getStringArray(KEY_EDIT_VALUE);
			break;
		case UNSUPPORTED:
			break;
		}

		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// remove the background of the regular Dialog
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));

		View view = buildView();

		initValues();

		createValidator();

		// first validate to correctly disable the OK button
		validate();

		return view;
	}

	private void initValues() {
		if (mEditMode) {
			mKey.setText(mEditKey);
			switch (mPreferenceType) {
			case BOOLEAN:
				((CompoundButton) mValue).setChecked((Boolean) mEditValue);
				break;
			case FLOAT:
			case INT:
			case LONG:
			case STRING:
				((EditText) mValue).setText(mEditValue.toString());
				break;
			case STRINGSET:
				String[] array = (String[]) mEditValue;
				for (int i = 0; i < array.length; i++) {
					addStringSetEntry(false, array[i]);
				}
				break;
			case UNSUPPORTED:
				break;
			}
		} else {
			switch (mPreferenceType) {
			case BOOLEAN:
				((CompoundButton) mValue).setChecked(true);
				break;
			case STRINGSET:
				if (((LinearLayout) mValue).getChildCount() == 0) {
					addStringSetEntry(false, null);
				}
				break;
			default:
				break;
			}
		}
	}

	private void addStringSetEntry(boolean changeFocus, String value) {
		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = ((LinearLayout) mValue);
		final LinearLayout item = (LinearLayout) inflater.inflate(
				R.layout.row_stringset, null);
		layout.addView(item);
		EditText editText = (EditText) item.getChildAt(0);
		item.getChildAt(1).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((LinearLayout) mValue).getChildCount() > 1) {
					((LinearLayout) mValue).removeView(item);
				} else {
					((EditText) ((ViewGroup) ((LinearLayout) mValue)
							.getChildAt(0)).getChildAt(0)).setText(null);
				}
				validate();
			}
		});
		if (changeFocus) {
			editText.requestFocus();
		}
		editText.setText(value);
	}

	private void createValidator() {
		TextWatcher textWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				validate();
			}
		};

		mKey.addTextChangedListener(textWatcher);

		switch (mPreferenceType) {
		case STRING:
		case FLOAT:
		case LONG:
		case INT:
			((EditText) mValue).addTextChangedListener(textWatcher);
			break;
		case BOOLEAN:
		case STRINGSET:
			// always valid
			break;
		case UNSUPPORTED:
			break;
		}
	}

	private String generateTitle() {
		return getString(mEditMode ? mPreferenceType.getDialogTitleEdit()
				: mPreferenceType.getDialogTitleAdd());
	}

	private View buildView() {
		int layout = mPreferenceType.getDialogLayout(mEditMode);
		View view = getActivity().getLayoutInflater().inflate(layout, null);
		view.setBackgroundResource(mPreferenceType.getCardBackground());

		mKey = (EditText) view.findViewById(R.id.key);
		mValue = view.findViewById(R.id.value);
		((TextView) view.findViewById(R.id.title)).setText(generateTitle());

		mBtnKO = (Button) view.findViewById(R.id.btnKO);
		mBtnKO.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		mBtnOK = (Button) view.findViewById(R.id.btnOK);
		mBtnOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				performOK();
				dismiss();
			}
		});
		if (mEditMode) {
			mBtnSUPPR = (Button) view.findViewById(R.id.btnSUPPR);
			mBtnSUPPR.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					performSUPPR();
					dismiss();
				}
			});
		}
		if (mPreferenceType == PreferenceType.STRINGSET) {
			mBtnAddEntrySet = (Button) view
					.findViewById(R.id.action_add_stringset_entry);
			mBtnAddEntrySet.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					addStringSetEntry(true, null);
				}
			});
		}
		return view;
	}

	private void performSUPPR() {
		PreferencesFragment fragment = (PreferencesFragment) getTargetFragment();
		if (fragment == null) {
			return;
		}
		fragment.deletePref(mEditKey);
	}

	private void performOK() {
		PreferencesFragment fragment = (PreferencesFragment) getTargetFragment();
		if (fragment == null) {
			return;
		}

		if (validate()) {
			String key = mKey.getText().toString();
			Object value = null;

			switch (mPreferenceType) {
			case BOOLEAN:
				value = ((CompoundButton) mValue).isChecked();
				break;
			case INT:
				value = Integer.valueOf(((EditText) mValue).getText()
						.toString());
				break;
			case STRING:
				value = ((EditText) mValue).getText().toString();
				break;
			case FLOAT:
				value = Float.valueOf(((EditText) mValue).getText().toString());
				break;
			case LONG:
				value = Long.valueOf(((EditText) mValue).getText().toString());
				break;
			case STRINGSET:
				Set<String> set = new HashSet<String>();
				LinearLayout container = (LinearLayout) mValue;
				for (int i = 0; i < container.getChildCount(); i++) {
					set.add(((EditText) ((ViewGroup) container.getChildAt(i))
							.getChildAt(0)).getText().toString());
				}
				value = set;
				break;
			case UNSUPPORTED:
				break;
			}
			fragment.addPrefKeyValue(mEditKey, key, value, mEditMode);
		}
	}

	private boolean validate() {
		String key = mKey.getText().toString().trim();
		boolean keyValid = !TextUtils.isEmpty(key);
		boolean valueValid = false;
		try {
			switch (mPreferenceType) {
			case BOOLEAN:
				valueValid = true;
				break;
			case FLOAT:
				Float f = Float.parseFloat(((EditText) mValue).getText()
						.toString().trim());
				valueValid = !Float.isInfinite(f) && !Float.isNaN(f);
				break;
			case LONG:
				Long.parseLong(((EditText) mValue).getText().toString().trim());
				valueValid = true;
				break;
			case INT:
				Integer.parseInt(((EditText) mValue).getText().toString()
						.trim());
				valueValid = true;
				break;
			case STRING:
				valueValid = true;
				break;
			case STRINGSET:
				valueValid = true;
				break;
			case UNSUPPORTED:
				break;
			}
		} catch (NumberFormatException e) {
			valueValid = false;
		}
		mBtnOK.setEnabled(keyValid && valueValid);
		return keyValid && valueValid;
	}
}