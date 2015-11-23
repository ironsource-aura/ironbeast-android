package com.ironsource.mobilcore;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.ironsource.mobilcore.ReportingConsts.EReportType;

public abstract class IronBeast {
    private static final String TAG = IronBeast.class.getSimpleName();

    protected static boolean sWasInit = false;
    private static Context sAppContext;
    private static String sToken;
    private static ImpVerifier sImpVerifierInstance = null; // imp
    private static String mAuthKey;
    private static int sBullkSize;
    private static int sCurrentBulkSize;

    /**
     * This method performs initialization work associated with setting everything MobileCore needs for its proper work.<br> <b>Calling "init" is mandatory and must
     * be carried out before any other MobileCore function call.</b>
     *
     * @param context  The context that is used to init the ads. This is usually an Activity context
     * @param token    This is your personal Developer Hash. You can get from our site. Notice: This key is assigned unique to each developer and does not change
     * @param authKey  This is your personal Developer Hash. You can get from our site. Notice: This key is assigned unique to each developer and does not change*                 per application
     * @param logLevel This is used to control exposure of different logs during the development or production stages of your work
     */
    public static void init(final Context context, final String token, String authKey, final LOG_TYPE logLevel) {
        setAppContext(context);
        doInit(context, token, authKey, logLevel);
    }

    private static void doInit(final Context context, final String token, final String authKey, final LOG_TYPE logLevel) {
        if (TextUtils.isEmpty(token)) {
            throw new IllegalArgumentException("MobileCore init method got an empty developer hash string.");
        }
        try {
            sImpVerifierInstance = ImpVerifier.getInstance();
            sImpVerifierInstance.init(sAppContext, logLevel);
            if (sImpVerifierInstance.hasUserRequestedDebugMode()) {
                sImpVerifierInstance.setCodeLineNumber(Thread.currentThread().getStackTrace()[3].getLineNumber()); // the same code is called in
                sImpVerifierInstance.verifyManifest();
            }
        } catch (Exception e) {
            // do nothing
        }

        // make sure init is called only once
        if (sWasInit) {
            Logger.log("MobileCore was already initialized", Logger.NORMAL);
            return;
        }

        sWasInit = true;

        try {
            if (sImpVerifierInstance != null && sImpVerifierInstance.hasUserRequestedDebugMode()) {
                sImpVerifierInstance.checkService();
                sImpVerifierInstance.checkMissingInterenetPermission(context);
            }
        } catch (Exception e) {
            // do nothing
        }

        try {
            if (logLevel != null && logLevel instanceof LOG_TYPE) {
                Logger.setLoggingLevel(logLevel);
            } else {
                Logger.setLoggingLevel(LOG_TYPE.PRODUCTION);
            }
            // save token and affiliateAccount in prefs
            sToken = token;
            saveToken(context, token);

        } catch (Exception e) {
            IronBeastReportData.openReport(sAppContext, EReportType.REPORT_TYPE_ERROR).setError(e).send();
        }
    }

    protected static String getToken() {
        return getToken(sAppContext);
    }

    protected static String getToken(Context context) {
        if (TextUtils.isEmpty(sToken) && context != null) {
            SharedPreferences prefs = MCUtils.getSharedPrefs(context, Consts.SHARED_PREFS_NAME_HASH);
            sToken = Guard.decrypt(prefs.getString(Consts.PREFS_TOKEN, ""));
        }
        return sToken;
    }

    protected static void saveToken(Context context, String token) {
        if (TextUtils.isEmpty(token) || null == context) {
            return;
        }
        SharedPreferences prefs = MCUtils.getSharedPrefs(context, Consts.SHARED_PREFS_NAME_HASH);
        Editor editor = prefs.edit();

        // The function that was here was moved to implementation verifier.
        if (sImpVerifierInstance.hasUserRequestedDebugMode()) {
            sImpVerifierInstance.verifyDevHash(token, context, editor);
        }

        editor.putString(Consts.PREFS_TOKEN, Guard.encrypt(token));
        editor.apply();
    }


    protected static Context getAppContext() {
        return sAppContext;
    }

    protected static void setAppContext(Context context) {
        if (null == sAppContext) {
            Logger.log("Setting app context", Logger.SDK_DEBUG);
            sAppContext = context.getApplicationContext();
        }
    }

    /***************
     * general api
     ******************/

    public static void trackEvent(IronBeastReport report, SEND_PRIORITY priority) {
        IronBeastReportData.openReport(sAppContext, EReportType.REPORT_TYPE_IRON_BEAST)
                .setReport(report)
                .setAuth(mAuthKey)
                .setBulk(priority.compareTo(SEND_PRIORITY.BULK) == 0)
                .send();
    }

    /**
     * This methode set max bulk size of the report
     *
     * @param bulkSize the size of the bulk
     */
    public static void setMaxBulkSize(int bulkSize) {
        sBullkSize = bulkSize;
    }

    public static int getCurrentBullkSize() {
        return sCurrentBulkSize;
    }
    /**
     * ************ ad unit event listener *****************
     */
    /**
     * This enum represents 2 log level types that are supported in the SDK.<br> The 2 log level types are DEBUG, PRODUCTION.<br>
     * When implementing the SDK during development it's recommended that you will use the DEBUG value in order to get all the debug messages in your logcat window.<br>
     * On production you can hide all the debug messages by using the PRODUCTION value.
     */
    public enum LOG_TYPE {
        DEBUG, PRODUCTION
    }

    public enum SEND_PRIORITY {
        NOW, BULK
    }
}
