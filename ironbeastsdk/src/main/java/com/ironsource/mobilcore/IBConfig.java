package com.ironsource.mobilcore;

import android.content.Context;
import android.webkit.URLUtil;

import java.net.MalformedURLException;

/**
 * TODO:
 * - finish docs
 * - Write toSharedPreferences
 * Create a global configuration options for the IronBeast library.
 * IBConfig understands the following options:
 * IdleSeconds         - idle seconds between each retries requests in ReportHandler.
 * NumOfRetries        - number of retries requests, when "post" request failed or when
 *                       the device not connect to the internet.
 * BulkSize            - maximum entries in each bulk request(on tracking).
 * FlushInterval       - flushing interval timer
 * MaximumRequestLimit - maximum bytes in request body.
 */
public class IBConfig {
    IBPrefService mIBPrefService;

    IBConfig(Context context) {
        mIBPrefService = IBPrefService.getInstance(context);
        // TODO(Ariel): Store application info in metadata and
        // use `.getApplicationInfo()` to retrieve these values
        mLoggerMode = LOG_TYPE.DEBUG;

        mIBEndPoint = mIBPrefService.getKeyIbEndPoint(DEFAULT_URL);
        mBulkSize = mIBPrefService.getKeyBulkSize(DEFAULT_BULK_SIZE);
        mFlushInterval = mIBPrefService.getKeyFlushInterval(DEFAULT_FLUSH_INTERVAL);
        mMaximumRequestLimit = mIBPrefService.getKeyMaxRequestLimit(DEFAULT_MAX_REQUEST_LIMIT);
        mToken = mIBPrefService.getToken();
        mNumOfRetries = DEFAULT_NUM_OF_RETRIES;

    }


    public static IBConfig getsInstance(Context context) {
        synchronized (sInstanceLock) {
            if (null == sInstance) {
                sInstance = new IBConfig(context);
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
            mIBPrefService.saveIbEndPoint(mIBEndPoint);
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
        mIBPrefService.saveBulkSize(mBulkSize);
        return this;
    }

    public int getFlushInterval() {
        return mFlushInterval;
    }

    public IBConfig setFlushInterval(int interval) {
        mFlushInterval = interval;
        mIBPrefService.saveFlushInterval(mFlushInterval);
        return this;
    }

    public long getMaximumRequestLimit()  { return mMaximumRequestLimit; }

    public IBConfig setMaximumRequestLimit(long bytes) {
        mMaximumRequestLimit = (bytes >= MINIMUM_REQUEST_LIMIT) ? bytes : mMaximumRequestLimit;
        mIBPrefService.saveMaximumRequestLimit(mMaximumRequestLimit);
        return this;
    }

    public int getNumOfRetries() {
        return mNumOfRetries;
    }

    public IBConfig setNumOfRetries(int n) {
        mNumOfRetries = n > 0 ? n : mNumOfRetries;
        return this;
    }

    protected int getIdleSeconds() {
        return mIdleSeconds;
    }

    protected IBConfig setIdleSeconds(int secs) {
        mIdleSeconds = secs >= 0 ? secs : mIdleSeconds;
        return this;
    }

    protected String getRecordsFile() { return RECORDS_FILENAME; }

    protected String getErrorsFile() {
        return ERRORS_FILENAME;
    }

    private int mBulkSize;
    private int mIdleSeconds;
    private int mNumOfRetries;
    private int mFlushInterval;
    private String mIBEndPoint;
    private LOG_TYPE mLoggerMode;
    private long mMaximumRequestLimit;
    private String mToken;

    private static IBConfig sInstance;
    private static final Object sInstanceLock = new Object();
    // Name for persistent storage of app referral SharedPreferences
    // IBConstants
    private static final String DEFAULT_URL = "http://10.0.2.2:3000/";  // (temporary, just for debugging right now)
    private static final String BULK_URL = "http://lb.ironbeast.io/bulk";
    private static final String RECORDS_FILENAME = "com.ironsource.mobilcore.ib_records";
    private static final String ERRORS_FILENAME = "com.ironsource.mobilcore.ib_errors";


    private static final int IDLE_SECONDS = 3;
    private static final int MINIMUM_REQUEST_LIMIT = 1024;
    private static final int DEFAULT_BULK_SIZE = 4;
    private static final int DEFAULT_NUM_OF_RETRIES = 3;
    private static final int DEFAULT_FLUSH_INTERVAL = 10 * 1000;
    private static final int DEFAULT_MAX_REQUEST_LIMIT = MINIMUM_REQUEST_LIMIT * MINIMUM_REQUEST_LIMIT;

    public static IBConfig getsInstance() {
        return null;
    }


    enum LOG_TYPE {
        PRODUCTION, DEBUG
    }
}
