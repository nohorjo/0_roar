package nohorjo.application;

import android.app.Service;
import android.content.Context;
import nohorjo.settings.SettingsException;
import nohorjo.settings.SettingsManager;

public abstract class App {

	private static String phoneNumber;
	private static Context context;

	private static int intervalSeconds = 15;
	private static long gpsCheckInterval = 60_000;
	private static int gpsPostMeters = 500;
	private static long serviceHeartBeat = 60_000;
	private static int linesInOut = 50;
	private static int retryLimit = 5;
	private static long maxFileSize = 130 * 1000;
	private static long remoteRetryDelaySeconds = 60_000;

	public static String key() {
		try {
			return getPhoneNumber().substring(1);
		} catch (SettingsException e) {
			throw new RuntimeException(e);
		}
	}

	public static int port() throws NumberFormatException {
		try {
			return Integer.parseInt(getPhoneNumber().substring(1, 5));
		} catch (SettingsException e) {
			throw new RuntimeException(e);
		}
	}

	public static long getGpsCheckInterval() {
		return gpsCheckInterval;
	}

	public static void setGpsCheckInterval(long gpsCheckInterval) {
		App.gpsCheckInterval = gpsCheckInterval;
	}

	public static int getGpsPostMeters() {
		return gpsPostMeters;
	}

	public static void setGpsPostMeters(int gpsPostMeters) {
		App.gpsPostMeters = gpsPostMeters;
	}

	public static String getPhoneNumber() throws SettingsException {
		phoneNumber = SettingsManager.getSetting(SettingsManager.PHONE_NUMBER);
		if (phoneNumber == null || phoneNumber.equals("")) {
			throw new SettingsException(SettingsManager.PHONE_NUMBER);
		}
		return phoneNumber;
	}

	public static void setPhoneNumber(String phoneNumber) {
		App.phoneNumber = phoneNumber;
	}

	public static long getServiceHeartBeat() {
		return serviceHeartBeat;
	}

	public static void setServiceHeartBeat(long serviceHeartBeat) {
		App.serviceHeartBeat = serviceHeartBeat;
	}

	public static int getIntervalSeconds() {
		return intervalSeconds;
	}

	public static void setIntervalSeconds(int intervalSeconds) {
		if (intervalSeconds > 15) {
			App.intervalSeconds = intervalSeconds;
		}
	}

	public static int getLinesInOut() {
		return linesInOut;
	}

	public static void setLinesInOut(int linesInOut) {
		App.linesInOut = linesInOut;
	}

	public static int retryLimit() {
		return retryLimit;
	}

	public static Context getContext() {
		return context;
	}

	public static void setContext(Context context) {
		if (App.context == null || !(App.context instanceof Service)) {
			App.context = context;
		}
	}

	public static long getMaxFileSize() {
		return maxFileSize;
	}

	public static void setMaxFileSize(long maxFileSize) {
		App.maxFileSize = maxFileSize;
	}

	public static long getRemoteRetryDelaySeconds() {
		return remoteRetryDelaySeconds;
	}

	public static void setRemoteRetryDelaySeconds(long remoteRetryDelaySeconds) {
		App.remoteRetryDelaySeconds = remoteRetryDelaySeconds;
	}
}
