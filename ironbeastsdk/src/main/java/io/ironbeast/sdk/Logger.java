package io.ironbeast.sdk;

import android.util.Log;

class Logger {
    public static final int NORMAL = 3;
    public static final int SDK_DEBUG = 55;
    public static final int SDK_ERROR = 2;
    protected static final int PRE_INIT = 1;
    private static final boolean mIsSuperDevMode = BuildConfig.IS_SUPER_DEV_MODE;
    private static final String LOG_TAG = Logger.class.getSimpleName();


    public static void log(String logString, int log_level) {
        switch (log_level) {
            case (PRE_INIT):
                Log.w(LOG_TAG, logString);
                break;
            case (NORMAL):
                if (IBConfig.getsInstance().getLogLevel() == IBConfig.LOG_TYPE.DEBUG || mIsSuperDevMode) {
                    Log.i(LOG_TAG, logString);
                }
                break;
            case (SDK_ERROR):
                IronBeast.trackError(logString);
            case (SDK_DEBUG):
                if (!mIsSuperDevMode) {
                    break;
                }
                Log.d(LOG_TAG, logString);
                break;
        }
    }

}
