package fr.simon.marquis.preferencesmanager.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import fr.simon.marquis.preferencesmanager.R;

public class Utils {

	public static AlertDialog displayNoRoot(Context ctx) {
		return new Builder(ctx)
				.setIcon(R.drawable.ic_launcher)
				.setTitle(R.string.no_root_title)
				.setMessage(R.string.no_root_message)
				.setPositiveButton(R.string.no_root_button,
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).create();
	}

}
