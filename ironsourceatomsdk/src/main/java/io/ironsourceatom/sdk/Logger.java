package io.ironsourceatom.sdk;

import android.util.Log;

class Logger {
    protected static final int PRE_INIT = 1;
    public static final int SDK_ERROR = 2;
    public static final int NORMAL = 3;
    public static final int SDK_DEBUG = 4;
    private static final boolean mIsSuperDevMode = false;
    private static final String LOG_TAG = Logger.class.getSimpleName();

    public static ISAConfig.LOG_TYPE logLevel = ISAConfig.LOG_TYPE.PRODUCTION;

    public static void log(String tag, String msg, int level) {
        log(String.format("[%s]: %s", tag, msg), level);
    }

    public static void log(String logString, int log_level) {
        switch (log_level) {
            case (PRE_INIT):
                Log.w(LOG_TAG, logString);
                break;
            case (NORMAL):
                if (logLevel == ISAConfig.LOG_TYPE.DEBUG || mIsSuperDevMode) {
                    Log.i(LOG_TAG, logString);
                }
                break;
            case (SDK_ERROR):
                IronSourceAtom.getInstance().trackError(logString);
            case (SDK_DEBUG):
                if (!mIsSuperDevMode) {
                    break;
                }
                Log.d(LOG_TAG, logString);
                break;
        }
    }

}
