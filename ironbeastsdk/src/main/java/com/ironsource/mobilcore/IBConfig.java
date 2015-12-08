package com.ironsource.mobilcore;

import android.webkit.URLUtil;

import java.net.MalformedURLException;

/**
 * TODO: finish docs
 * Create a global configuration options for the IronBeast library.
 * IBConfig understands the following options:
 * ...
 */
public class IBConfig {


    IBConfig() {
        // TODO(Ariel): Store application info in metadata and
        // use `.getApplicationInfo()` to retrieve these values
        mIBEndPoint = DEFAULT_URL;
        mLoggerMode = LOG_TYPE.DEBUG;
        // Default configuration
        mBulkSize = 4;
        mNumOfRetries = 3;
        mFlushInterval = 60 * 1000;
        mMaximumRequestLimit = 1024 * 1024;
    }

    public static IBConfig getsInstance() {
        synchronized (sInstanceLock) {
            if (null == sInstance) {
                sInstance = new IBConfig();
            }
        }
        return sInstance;
    }

    public LOG_TYPE getLogLevel() {
        return mLoggerMode;
    }

    public IBConfig setLogLevel(LOG_TYPE logLevel) {
        mLoggerMode = logLevel;
        return this;
    }

    public String getIBEndPoint() {
        return mIBEndPoint;
    }

    public IBConfig setIBEndPoint(String url) throws MalformedURLException {
        if (URLUtil.isValidUrl(url)) {
            mIBEndPoint = url;
        } else {
            throw new MalformedURLException();
        }
        return this;
    }

    public int getBulkSize() {
        return mBulkSize;
    }

    public IBConfig setBulkSize(int size) {
        mBulkSize = size;
        return this;
    }

    public int getFlushInterval() {
        return mFlushInterval;
    }

    public IBConfig setFlushInterval(int interval) {
        mFlushInterval = interval;
        return this;
    }

    public long getMaximumRequestLimit()  { return mMaximumRequestLimit; }

    public IBConfig setMaximumRequestLimit(long bytes) {
        mMaximumRequestLimit = bytes >= 1024 ? bytes : mMaximumRequestLimit;
        return this;
    }

    public int getNumOfRetries() {
        return mNumOfRetries;
    }

    protected int getIdleSeconds() {
        return IDLE_SECONDS;
    }

    protected String getRecordsFile() { return RECORDS_FILENAME; }

    protected String getErrorsFile() {
        return ERRORS_FILENAME;
    }

    private int mBulkSize;
    private int mNumOfRetries;
    private int mFlushInterval;
    private String mIBEndPoint;
    private LOG_TYPE mLoggerMode;
    private long mMaximumRequestLimit;

    private static IBConfig sInstance;
    private static final Object sInstanceLock = new Object();
    // Name for persistent storage of app referral SharedPreferences
    static final String REFERRER_PREFS_NAME = "com.ironsource.mobilcore.ReferralInfo";
    // IBConstants
    private static final String DEFAULT_URL = "http://10.0.2.2:3000/";  // (temporary, just for debugging right now)
    private static final String BULK_URL = "http://lb.ironbeast.io/bulk";
    private static final String RECORDS_FILENAME = "com.ironsource.mobilcore.ib_records";
    private static final String ERRORS_FILENAME = "com.ironsource.mobilcore.ib_errors";
    private static final int IDLE_SECONDS = 3;

    enum LOG_TYPE {
        PRODUCTION, DEBUG
    }
}
