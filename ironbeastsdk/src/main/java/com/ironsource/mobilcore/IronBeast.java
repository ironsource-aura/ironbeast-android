package com.ironsource.mobilcore;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;


public abstract class IronBeast {
    private static final String TAG = IronBeast.class.getSimpleName();

    protected static boolean sWasInit = false;
    private static Context sAppContext;
    private static String sToken;
    private static String mAuthKey;
    private static int sBatchSize;
    private static int sCurrentBatchSize;

    /**
     * This method performs initialization work associated with setting everything MobileCore needs for its proper work.<br> <b>Calling "init" is mandatory and must
     * be carried out before any other MobileCore function call.</b>
     *
     * @param context  The context that is used to init the ads. This is usually an Activity context
     * @param token    This is your personal Developer Hash. You can get from our site. Notice: This key is assigned unique to each developer and does not change
     * @param logLevel This is used to control exposure of different logs during the development or production stages of your work
     */
    public static void init(Context context, String token, LOG_TYPE logLevel) {
        setAppContext(context);
        doInit(token, logLevel);
    }

    private static void doInit(final String token, final LOG_TYPE logLevel) {
        if (TextUtils.isEmpty(token)) {
            throw new IllegalArgumentException("MobileCore init method got an empty developer hash string.");
        }
        // make sure init is called only once
        if (sWasInit) {
            Logger.log("MobileCore was already initialized", Logger.NORMAL);
            return;
        }
        sWasInit = true;
        try {
            if (logLevel != null && logLevel instanceof LOG_TYPE) {
                Logger.setLoggingLevel(logLevel);
            } else {
                Logger.setLoggingLevel(LOG_TYPE.PRODUCTION);
            }
            // save token and affiliateAccount in prefs
            sToken = token;

            String size = MCUtils.getValueFromConfig(getAppContext(), Consts.PREFS_MAX_BATCH_SIZE);
            if (!TextUtils.isEmpty(size)) {
                sBatchSize = Integer.valueOf(size);
            }
        } catch (Exception e) {
            IronBeastReportData.openReport(sAppContext, SdkEvent.ERROR).setError(e).send();
        }
    }

    protected static String getToken() {
        return getToken(sAppContext);
    }

    protected static String getToken(Context context) {
        if (TextUtils.isEmpty(sToken) && context != null) {
            SharedPreferences prefs = MCUtils.getSharedPrefs(context, Consts.SHARED_PREFS_NAME_HASH);
            sToken = prefs.getString(Consts.PREFS_TOKEN, "");
        }
        return sToken;
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
    /*
    *  Will batch reports
    * */
    public static void track(IronBeastReport report) {
        IronBeastReportData.openReport(sAppContext, SdkEvent.ENQUEUE)
                .setReport(report)
                .setAuth(mAuthKey)
                .setBulk(true)
                .send();
    }

    /*
    *  Will send report immediately
    * */
    public static void post(IronBeastReport report) {
        IronBeastReportData.openReport(sAppContext, SdkEvent.POST_SYNC)
                .setReport(report)
                .setAuth(mAuthKey)
                .setBulk(false)
                .send();
    }

    /*
    *  Will send all batched report till now
    * */
    public static void flush() {
        IronBeastReportData.openReport(sAppContext, SdkEvent.FLUSH_QUEUE)
                .setAuth(mAuthKey)
                .setBulk(true)
                .send();
    }

    /*
    *  Get current batch size
    * */
    public static int getCurrentBatchSize() {
        return sCurrentBatchSize;
    }

    /*
    *  Will set max amount of saved report
    * */
    public void setBatchSize(int batchSize) {
        sBatchSize = batchSize;
        //update config
    }

    public enum LOG_TYPE {
        DEBUG, PRODUCTION
    }
}

