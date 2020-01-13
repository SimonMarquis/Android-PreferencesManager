package fr.simon.marquis.preferencesmanager.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import fr.simon.marquis.preferencesmanager.R;

public class RootDialog extends DialogFragment {

    public static RootDialog newInstance() {
        return new RootDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() == null) {
            return null;
        }
        return new AlertDialog.Builder(getActivity()).setIcon(R.drawable.ic_action_emo_evil).setTitle(R.string.no_root_title)
                .setMessage(R.string.no_root_message).setPositiveButton(R.string.no_root_button, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                }).create();
    }
}
