package fr.simon.marquis.preferencesmanager.ui;

import android.app.Application;

import com.spazedog.rootfw.RootFW;

public class App extends Application {
	private static RootFW root;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public static RootFW getRoot() {
		if (root == null)
			root = new RootFW();
		return root;
	}
}
