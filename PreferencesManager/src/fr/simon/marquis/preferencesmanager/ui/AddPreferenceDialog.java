package fr.simon.marquis.preferencesmanager.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import fr.simon.marquis.preferencesmanager.R;

public class AddPreferenceDialog extends DialogFragment {
	private EditText mKey;
	private CompoundButton mValue;

	public static AddPreferenceDialog newInstance(String title) {
		AddPreferenceDialog frag = new AddPreferenceDialog();
		Bundle args = new Bundle();
		args.putString("title", title);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String title = getArguments().getString("title");
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.dialog_add_boolean, null);
		mKey = (EditText) view.findViewById(R.id.key);
		mValue = (Switch) view.findViewById(R.id.value);

		mValue.setChecked(true);

		// TODO add a validation step

		return new AlertDialog.Builder(getActivity())
				// .setIcon(R.drawable.alert_dialog_icon)
				.setTitle(title)
				.setView(view)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								PreferencesFragment fragment = (PreferencesFragment) getTargetFragment();
								if (fragment != null) {
									fragment.addBooleanPref(mKey.getText()
											.toString(), mValue.isChecked());
								}
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).create();
	}
}