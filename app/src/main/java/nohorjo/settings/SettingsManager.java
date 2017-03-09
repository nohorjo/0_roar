package nohorjo.settings;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import nohorjo.application.App;
import nohorjo.common.CommonUtils;
import nohorjo.output.FileOut;

public abstract class SettingsManager {

	public static final String PHONE_NUMBER = "phone_number", INTERVAL_SECONDS = "interval_seconds",
			MAX_DATA = "max_data";

	private static final String settingsFile = "howl.settings";

	private static String[] requiredPermissions = { /*
													 * ACCESS_FINE_LOCATION,
													 * ACCESS_COARSE_LOCATION,
													 * SEND_SMS
													 */ };
	private static String[] storagePermissions = { WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE };
	private static String[] otherPermissions = { /* READ_SMS, RECEIVE_SMS, */ INTERNET };

	static {
		requestPermissions();
	}

	private static void requestPermissions() {
		String[] permissions = CommonUtils.arrayConcat(requiredPermissions, storagePermissions, otherPermissions);

		if (App.getContext() instanceof Activity) {
			((Activity) App.getContext()).requestPermissions(permissions, 0);
		}
	}

	public static String getSetting(String setting) {
		return App.getContext().getSharedPreferences(settingsFile, 0).getString(setting, null);
	}

	public static void setSetting(String setting, String value) {
		SharedPreferences.Editor editor = App.getContext().getSharedPreferences(settingsFile, 0).edit();
		editor.putString(setting, value);
		editor.commit();
		FileOut.println("Saved setting: " + setting + "=" + value);
	}

	public static boolean isStoragePermissionsGranted() {
		return checkPermissions(storagePermissions);
	}

	public static boolean isRequiredPermissionsGranted() {
		return checkPermissions(requiredPermissions);
	}

	private static boolean checkPermissions(String[] permissions) {
		for (String permission : permissions) {
			if (App.getContext().checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}
}
