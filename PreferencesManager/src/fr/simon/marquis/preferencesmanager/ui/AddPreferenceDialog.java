package fr.simon.marquis.preferencesmanager.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.PreferenceType;

public class AddPreferenceDialog extends DialogFragment {
	private EditText mKey;
	private View mValue;
	private PreferenceType mPreferenceType;

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

		// TODO add a validation step

		return new AlertDialog.Builder(getActivity())
				// .setIcon(R.drawable.alert_dialog_icon)
				.setTitle(generateTitle())
				.setView(view)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								performOK();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).create();
	}

	private String generateTitle() {
		switch (mPreferenceType) {
		case BOOLEAN:
			return getString(R.string.title_add_boolean);
		default:
			return null;
		}
	}

	private View buildView() {
		int layout = 0;
		switch (mPreferenceType) {
		case BOOLEAN:
			layout = R.layout.dialog_add_boolean;
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

		if (!validate()) {

		} else {
			String key = mKey.getText().toString();
			Object value = null;

			switch (mPreferenceType) {
			case BOOLEAN:
				value = ((CompoundButton) mValue).isChecked();
				break;
			}
			fragment.addPrefKeyValue(key, value);
		}
	}

	private boolean validate() {
		return true;
	}
}