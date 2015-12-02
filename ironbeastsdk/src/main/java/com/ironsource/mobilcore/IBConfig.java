package com.ironsource.mobilcore;

import android.webkit.URLUtil;

import java.net.MalformedURLException;

public class IBConfig {


    IBConfig () {
        // TODO(Ariel): Store application info in metadata and
        // use `.getApplicationInfo()` to retrieve these values
        mFlushInterval = FLUSH_INTERVAL;
        mBulkSize = BULK_SIZE;
        mIBEndPoint = DEFAULT_URL;
        mLoggerMode = LOG_TYPE.DEBUG;
        mNumOfRetries = NUM_OF_RETRIES;
    }

    public static IBConfig getsInstance () {
        synchronized (sInstanceLock) {
            if (null == sInstance) {
                sInstance = new IBConfig();
            }
        }
        return sInstance;
    }

    public LOG_TYPE getLogLevel () {
        return mLoggerMode;
    }

    public IBConfig setLogLevel (LOG_TYPE logLevel) {
        mLoggerMode = logLevel;
        return this;
    }

    public String getIBEndPoint () {
        return mIBEndPoint;
    }

    public IBConfig setIBEndPoint (String url) throws MalformedURLException {
        if (URLUtil.isValidUrl(url)) {
            mIBEndPoint = url;
        } else {
            throw new MalformedURLException();
        }
        return this;
    }

    public int getBulkSize () {
        return mBulkSize;
    }

    public IBConfig setBulkSize (int size) {
        mBulkSize = size;
        return this;
    }

    public int getFlushInterval () {
        return mFlushInterval;
    }

    public IBConfig setFlushInterval (int interval) {
        mFlushInterval = interval;
        return this;
    }

    protected int getNumOfRetries () {
        return mNumOfRetries;
    }

    protected int getIdleSeconds () {
        return IDLE_SECONDS;
    }

    protected String getRecordsFile () {
        return RECORDS_FILENAME;
    }

    protected String getErrorsFile () {
        return ERRORS_FILENAME;
    }

    private int mFlushInterval;
    private int mBulkSize;
    private int mNumOfRetries;
    private String mIBEndPoint;
    private LOG_TYPE mLoggerMode;

    private static IBConfig sInstance;
    private static final Object sInstanceLock = new Object();
    // Name for persistent storage of app referral SharedPreferences
    static final String REFERRER_PREFS_NAME = "com.ironsource.mobilcore.ReferralInfo";
    // IBConstants
    private static final String DEFAULT_URL = "http://10.0.2.2:3000/";  // (temporary, just for debugging)
    private static final String BULK_URL = "http://lb.ironbeast.io/bulk";
    private static final String RECORDS_FILENAME = "com.ironsource.mobilcore.ib_records";
    private static final String ERRORS_FILENAME = "com.ironsource.mobilcore.ib_errors";
    private static final int FLUSH_INTERVAL = 60 * 1000; // 1 second
    private final int BULK_SIZE = 4;                     // 30 records
    private final int NUM_OF_RETRIES = 3;
    private static final int IDLE_SECONDS = 3;

    enum LOG_TYPE {
        PRODUCTION, DEBUG
    }
}
