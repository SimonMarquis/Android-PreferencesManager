package fr.simon.marquis.preferencesmanager.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.PreferenceType;

public class AddPreferenceDialog extends DialogFragment {
	private EditText mKey;
	private View mValue;
	private PreferenceType mPreferenceType;

	private Button mButton;

	public static AddPreferenceDialog newInstance(PreferenceType type) {
		AddPreferenceDialog frag = new AddPreferenceDialog();
		Bundle args = new Bundle();
		args.putString("type", type.name());
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String type = getArguments().getString("type");
		mPreferenceType = PreferenceType.valueOf(type);
		View view = buildView();

		if (view == null) {
			return null;
		}
		
		initValues();

		createValidator();

		AlertDialog dialog = new AlertDialog.Builder(getActivity())
				// .setIcon(R.drawable.alert_dialog_icon)
				.setTitle(generateTitle())
				.setView(view)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).create();

		return dialog;
	}
	
	private void initValues() {
		switch (mPreferenceType) {
		case BOOLEAN:
			((CompoundButton) mValue).setChecked(true);
			break;
		
		default:
			break;
		}
	}

	private void createValidator() {
		TextWatcher textWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
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
			//always valid
			break;
		case STRINGSET:
			// TODO validation
			break;
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
	}

	@Override
	public void onStart() {
		super.onStart();
		mButton = ((AlertDialog) getDialog())
				.getButton(DialogInterface.BUTTON_POSITIVE);
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				performOK();
				dismiss();
			}
		});
		
		//first validate to correctly disable the OK button
		validate();
	}

	private Spanned generateTitle() {
		String title = null;
		switch (mPreferenceType) {
		case BOOLEAN:
			title = getString(R.string.title_add_boolean);
			break;
		case INT:
			title = getString(R.string.title_add_int);
			break;
		case STRING:
			title = getString(R.string.title_add_string);
			break;
		case FLOAT:
			title = getString(R.string.title_add_float);
			break;
		case LONG:
			title = getString(R.string.title_add_long);
			break;
		case STRINGSET:
			title = getString(R.string.title_add_stringset);
			break;
		}
		return Html.fromHtml(title);
	}

	private View buildView() {
		int layout = 0;
		switch (mPreferenceType) {
		case BOOLEAN:
			layout = R.layout.dialog_add_boolean;
			break;
		case INT:/*same layout as Long are integer*/
		case LONG:
			layout = R.layout.dialog_add_integer;
			break;
		case STRING:
			layout = R.layout.dialog_add_string;
			break;
		case FLOAT:
			layout = R.layout.dialog_add_float;
			break;
		case STRINGSET:
			layout = R.layout.dialog_add_stringset;
			break;
		}
		View view = getActivity().getLayoutInflater().inflate(layout, null);
		mKey = (EditText) view.findViewById(R.id.key);
		mValue = view.findViewById(R.id.value);
		return view;
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
				value = Integer.valueOf(((EditText) mValue).getText().toString());
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
				// FIXME
				value = ((EditText) mValue).getText().toString();
				break;
			}
			fragment.addPrefKeyValue(key, value);
		}
	}

	private boolean validate() {
		if (mButton == null) {
			return false;
		}
		String key = mKey.getText().toString().trim();
		boolean keyValid = !TextUtils.isEmpty(key) /* TODO: && key isUnique*/;
		boolean valueValid = false;
		try {
			switch (mPreferenceType) {
			case BOOLEAN:
				valueValid = true;
				break;
			case FLOAT:
				Float f = Float.parseFloat(((EditText) mValue).getText().toString().trim());
				valueValid = !Float.isInfinite(f) && !Float.isNaN(f);
				break;
			case LONG:
				Long.parseLong(((EditText) mValue).getText().toString().trim());
				valueValid = true;
				break;
			case INT:
				Integer.parseInt(((EditText) mValue).getText().toString().trim());
				valueValid = true;
				break;
			case STRING:
				//Maybe there are some things to validate in a string?
				valueValid = true;
				break;
			case STRINGSET:
				//TODO
				break;
			}
		} catch (NumberFormatException e) {
			valueValid = false;
		}
		mButton.setEnabled(keyValid && valueValid);
		return keyValid && valueValid;
	}
}