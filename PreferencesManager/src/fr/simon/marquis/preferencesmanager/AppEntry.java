package fr.simon.marquis.preferencesmanager;

import java.io.File;
import java.text.Collator;
import java.util.Comparator;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

public class AppEntry {

	private final ApplicationInfo mInfo;
	private final File mApkFile;
	private String mLabel;
	private Drawable mIcon;
	private boolean mMounted;

	public AppEntry(ApplicationInfo info, Context context) {
		mInfo = info;
		mApkFile = new File(info.sourceDir);
		loadLabel(context);
	}

	public ApplicationInfo getApplicationInfo() {
		return mInfo;
	}

	public String getLabel() {
		return mLabel;
	}

	public Drawable getIcon(Context ctx) {
		if (mIcon == null) {
			if (mApkFile.exists()) {
				mIcon = mInfo.loadIcon(ctx.getPackageManager());
				return mIcon;
			} else {
				mMounted = false;
			}
		} else if (!mMounted) {
			// If the app wasn't mounted but is now mounted, reload
			// its icon.
			if (mApkFile.exists()) {
				mMounted = true;
				mIcon = mInfo.loadIcon(ctx.getPackageManager());
				return mIcon;
			}
		} else {
			return mIcon;
		}

		return ctx.getResources().getDrawable(
				android.R.drawable.sym_def_app_icon);
	}

	@Override
	public String toString() {
		return mLabel;
	}

	private void loadLabel(Context ctx) {
		if (mLabel == null || !mMounted) {
			if (!mApkFile.exists()) {
				mMounted = false;
				mLabel = mInfo.packageName;
			} else {
				mMounted = true;
				CharSequence label = mInfo.loadLabel(ctx.getPackageManager());
				mLabel = label != null ? label.toString() : mInfo.packageName;
			}
		}
	}

	public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
		private final Collator sCollator = Collator.getInstance();

		@Override
		public int compare(AppEntry object1, AppEntry object2) {
			return sCollator.compare(object1.getLabel(), object2.getLabel());
		}
	};
}