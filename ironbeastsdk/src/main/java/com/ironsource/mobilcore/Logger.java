package com.ironsource.mobilcore;

import android.util.Log;

class Logger {
    // only
    public static final int CRITICAL = 2;
    public static final int NORMAL = 3;
    public static final int SDK_DEBUG = 55;
    protected static final int PRE_INIT = 1; // Allow non-critical, warning logs before mLogerMode was init and regardless of superDevMode which is internal
    private static final boolean mIsSuperDevMode = BuildConfig.IS_SUPER_DEV_MODE;
    private static final String LOG_TAG = Logger.class.getSimpleName();


    public static void log(String logString, int log_level) {
        switch (log_level) {
            case (PRE_INIT):
                Log.w(LOG_TAG, logString);
                break;
            case (CRITICAL):
                IronBeast.trackError(logString);
                Log.e(LOG_TAG, logString);
                break;
            case (NORMAL):
                if (IBConfig.getsInstance().getLogLevel() == IBConfig.LOG_TYPE.DEBUG || mIsSuperDevMode) {
                    Log.i(LOG_TAG, logString);
                }
                break;
            case (SDK_DEBUG):
                if (!mIsSuperDevMode) {
                    break;
                }
                Log.d(LOG_TAG, logString);
                break;
        }
    }

}
