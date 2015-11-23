package com.ironsource.mobilcore;

import android.util.Log;

class Logger {
	protected static final int PRE_INIT = 1; // Allow non-critical, warning logs before mLogerMode was init and regardless of superDevMode which is internal
	                                         // only
	public static final int CRITICAL = 2;
	public static final int NORMAL = 3;
	public static final int WARNING = 4;
	public static final int SDK_DEBUG = 55;
	private static IronBeast.LOG_TYPE mLogerMode;
	private static final boolean mIsSuperDevMode = Config.mIsSuperDevMode;
	private static final String LOG_TAG = "MobileCore";

	protected static final String LOG_RequestingToInit = "mobileCore SDK: Initializing ";
	protected static final String LOG_InitSucceededAdUnit = "mobileCore SDK: Initialized successfully ";
	protected static final String LOG_InitFailedAdUnit = "mobileCore SDK: Failed to initialize";
	protected static final String LOG_RequestingToLoadAdUnit = "mobileCore SDK: Loading %s";
	protected static final String LOG_FailedToLoadAdUnit = "mobileCore SDK: Failed to load %s";
	protected static final String LOG_AdUnitAlreadyLoading = "mobileCore SDK: %s is already in the process of loading";

	protected static final String LOG_NoInternetConnection = "mobileCore SDK: Failed to show %s trigger %s | No internet connection";

	protected static final String LOG_AdUnitWithTriggerIsDisabled = "mobileCore SDK: %s trigger %s is disabled on the mobileCore dashboard";

	protected static final String LOG_AdUnitWithTriggerIsReady = "mobileCore SDK: %s is ready";
	protected static final String LOG_AdUnitWithIsNotReady = "mobileCore SDK: %s is not ready";

	protected static final String LOG_RequestingToShowAdUnitWithTrigger = "mobileCore SDK: Requesting to show %s trigger %s";

	protected static final String LOG_AdUnitWithTriggerIsShowingOnScreen = "mobileCore SDK: %s trigger %s is showing on screen";

	protected static final String LOG_AdUnitWithTriggerFailedToShow = "mobileCore SDK: Failed to show %s trigger %s";
	protected static final String LOG_AdUnitWithTriggerFailedToShowAdUnitNotReady = "mobileCore SDK: Failed to show %s trigger %s | Ad unit is not ready";
	protected static final String LOG_UserClickedOnAdUnitWithTrigger = "mobileCore SDK: User clicked on %s trigger %s";
	protected static final String LOG_AdUnitDTMWithTriggerRequestingToSendToGooglePlay = "mobileCore SDK: %s trigger %s requesting to send user to Google Play";
	protected static final String LOG_AdUnitDTMWithTriggerFailedToSendToGooglePlay = "mobileCore SDK: %s trigger %s failed to send user to Google Play";

	protected static final String LOG_AdUnitWasDismissed = "mobileCore SDK: User closed %s trigger %s";
	protected static final String LOG_FailedToLoadWhileShowing = "mobileCore SDK: Failed to load %s | " +
			"it is not possible to load %s while it is showing on screen. " +
			"You can load on %s AD_UNIT_DISMISSED event (using the Ad Unit Event Listener)";

	protected static final String LOG_FailedToLoadAdUnitNoInternet = "mobileCore SDK: Failed to loadAdUnit %s | No internet connection";

	public static void setLoggingLevel(IronBeast.LOG_TYPE level) {
		mLogerMode = level;
	}

	public static void log(String log_string, int log_level) {
		switch (log_level) {
		case (PRE_INIT):
			Log.w(LOG_TAG, log_string);
			break;
		case (CRITICAL):
			Log.e(LOG_TAG, log_string);
			break;
		case (WARNING):
			if (mLogerMode == IronBeast.LOG_TYPE.DEBUG || mIsSuperDevMode) {
				Log.w(LOG_TAG, log_string);
			}
		case (NORMAL):
			if (mLogerMode == IronBeast.LOG_TYPE.DEBUG || mIsSuperDevMode) {
				Log.i(LOG_TAG, log_string);
			}
			break;
		case (SDK_DEBUG):
			if (!mIsSuperDevMode) {
				break;
			}
			Log.d(LOG_TAG, log_string);
			break;
		}
	}

}
