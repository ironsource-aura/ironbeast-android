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
    private static final Object sInstanceLock = new Object();
    // Name for persistent storage of app referral SharedPreferences
    // IBConstants
    private static final String DEFAULT_URL = "http://10.0.2.2:3000/";  // (temporary, just for debugging right now)
    private static final String DEFAULT_BULK_URL = "http://lb.ironbeast.io/bulk";
    private static final String RECORDS_FILENAME = "com.ironsource.mobilcore.ib_records";
    private static final String ERRORS_FILENAME = "com.ironsource.mobilcore.ib_errors";
    private static final int DEFAULT_IDLE_SECONDS = 1;
    private static final int MINIMUM_REQUEST_LIMIT = 1024;
    private static final int DEFAULT_BULK_SIZE = 4;
    private static final int DEFAULT_NUM_OF_RETRIES = 3;
    private static final int DEFAULT_FLUSH_INTERVAL = 10 * 1000;
    private static final int DEFAULT_MAX_REQUEST_LIMIT = MINIMUM_REQUEST_LIMIT * MINIMUM_REQUEST_LIMIT;
    //Shared prefs keys for metadata
    private static final String KEY_BULK_SIZE = "bulk_size";
    private static final String KEY_FLUSH_INTERVAL = "flush_interval";
    private static final String KEY_IB_END_POINT = "ib_end_point";
    private static final String KEY_IB_END_POINT_BULK = "ib_end_point_bulk";
    //TODO: save logger_mode???
    private static final String KEY_LOGGER_MODE = "logger_mode";
    private static final String KEY_MAX_REQUEST_LIMIT = "max_request_limit";
    private static IBConfig sInstance;
    protected boolean isDefaultConstructorUsed = false;
    IBPrefService mIBPrefService;
    private LOG_TYPE mLoggerMode = LOG_TYPE.DEBUG;
    private int mBulkSize;
    private int mNumOfRetries;
    private int mFlushInterval;
    private String mIBEndPoint;
    private String mIBEndPointBulk;
    private long mMaximumRequestLimit;
    private int mIdleSeconds;

    IBConfig() {
        isDefaultConstructorUsed = true;
        mIBEndPoint = DEFAULT_URL;
        mIBEndPointBulk = DEFAULT_BULK_URL;

        mBulkSize = DEFAULT_BULK_SIZE;
        mFlushInterval = DEFAULT_FLUSH_INTERVAL;
        mMaximumRequestLimit = DEFAULT_MAX_REQUEST_LIMIT;

        mNumOfRetries = DEFAULT_NUM_OF_RETRIES;
        mIdleSeconds = DEFAULT_IDLE_SECONDS;
    }

    IBConfig(Context context) {
        loadConfig(context);
    }

    static IBConfig getsInstance(Context context) {
        synchronized (sInstanceLock) {
            if (null == sInstance) {
                sInstance = new IBConfig(context);
            } else if (sInstance.isDefaultConstructorUsed) {
                sInstance.loadConfig(context);
            }
        }
        return sInstance;
    }

    static IBConfig getsInstance() {
        synchronized (sInstanceLock) {
            if (null == sInstance) {
                sInstance = new IBConfig();
            }
        }
        return sInstance;
    }

    void loadConfig(Context context) {
        mIBPrefService = IBPrefService.getInstance(context);

        mIBEndPoint = mIBPrefService.load(KEY_IB_END_POINT, DEFAULT_URL);
        mIBEndPointBulk = mIBPrefService.load(KEY_IB_END_POINT, DEFAULT_BULK_URL);

        mBulkSize = Integer.getInteger(mIBPrefService.load(KEY_BULK_SIZE, ""), DEFAULT_BULK_SIZE);
        mFlushInterval = Integer.getInteger(mIBPrefService.load(KEY_FLUSH_INTERVAL, ""), DEFAULT_FLUSH_INTERVAL);
        mMaximumRequestLimit = Integer.getInteger(mIBPrefService.load(KEY_MAX_REQUEST_LIMIT, ""), DEFAULT_MAX_REQUEST_LIMIT);

        mNumOfRetries = DEFAULT_NUM_OF_RETRIES;
    }

    void apply() {
        mIBPrefService.save(KEY_MAX_REQUEST_LIMIT, String.valueOf(mMaximumRequestLimit));
        mIBPrefService.save(KEY_BULK_SIZE, String.valueOf(mBulkSize));
        mIBPrefService.save(KEY_FLUSH_INTERVAL, String.valueOf(mFlushInterval));
        mIBPrefService.save(KEY_IB_END_POINT, mIBEndPoint);
        mIBPrefService.save(KEY_IB_END_POINT_BULK, mIBEndPointBulk);
    }

    public LOG_TYPE getLogLevel() {
        return mLoggerMode;
    }

    IBConfig setLogLevel(LOG_TYPE logLevel) {
        mLoggerMode = logLevel;
        return this;
    }

    public String getIBEndPoint() {
        return mIBEndPoint;
    }

    IBConfig setIBEndPoint(String url) throws MalformedURLException {
        if (URLUtil.isValidUrl(url)) {
            mIBEndPoint = url;
        } else {
            throw new MalformedURLException();
        }
        return this;
    }

    public String getIBEndPointBulk() {
        return mIBEndPointBulk;
    }

    IBConfig setIBEndPointBulk(String url) throws MalformedURLException {
        if (URLUtil.isValidUrl(url)) {
            mIBEndPointBulk = url;
        } else {
            throw new MalformedURLException();
        }
        return this;
    }

    public int getBulkSize() {
        return mBulkSize;
    }

    IBConfig setBulkSize(int size) {
        mBulkSize = size;
        return this;
    }

    public int getFlushInterval() {
        return mFlushInterval;
    }

    IBConfig setFlushInterval(int interval) {
        mFlushInterval = interval;
        return this;
    }

    public long getMaximumRequestLimit() {
        return mMaximumRequestLimit;
    }

    IBConfig setMaximumRequestLimit(long bytes) {
        mMaximumRequestLimit = (bytes >= MINIMUM_REQUEST_LIMIT) ? bytes : mMaximumRequestLimit;
        return this;
    }

    public int getNumOfRetries() {
        return mNumOfRetries;
    }

    IBConfig setNumOfRetries(int n) {
        mNumOfRetries = n > 0 ? n : mNumOfRetries;
        return this;
    }

    protected int getIdleSeconds() {
        return mIdleSeconds;
    }

    IBConfig setIdleSeconds(int secs) {
        mIdleSeconds = secs >= 0 ? secs : mIdleSeconds;
        return this;
    }

    String getRecordsFile() { return RECORDS_FILENAME; }

    String getErrorsFile() {
        return ERRORS_FILENAME;
    }

    void update(IBConfig config) {
        this.setBulkSize(config.getBulkSize());
        this.setFlushInterval(config.getFlushInterval());
        this.setLogLevel(config.getLogLevel());
        this.setMaximumRequestLimit(config.getMaximumRequestLimit());

        try {
            this.setIBEndPoint(config.getIBEndPoint());
        } catch (MalformedURLException e) {
            Logger.log("Failed to set custom IronBeast end point" + config.getIBEndPoint(), Logger.NORMAL);
        }
        try {
            this.setIBEndPointBulk(config.getIBEndPointBulk());
        } catch (MalformedURLException e) {
            Logger.log("Failed to set custom IronBeast end point for bulk" + config.getIBEndPoint(), Logger.NORMAL);
        }
    }

    public enum LOG_TYPE {
        PRODUCTION, DEBUG
    }
    
    public static class Builder {
        LOG_TYPE mLoggerMode;
        private int mBulkSize;
        private int mFlushInterval;
        private String mIBEndPoint;
        private String mIBEndPointBulk;
        private long mMaximumRequestLimit;

        public IBConfig.Builder setLogLevel(LOG_TYPE logLevel) {
            mLoggerMode = logLevel;
            return this;
        }

        public IBConfig.Builder setIBEndPoint(String url) throws MalformedURLException {
            if (URLUtil.isValidUrl(url)) {
                mIBEndPoint = url;
            } else {
                throw new MalformedURLException();
            }
            return this;
        }

        public IBConfig.Builder setIBEndPointBulk(String url) throws MalformedURLException {
            if (URLUtil.isValidUrl(url)) {
                mIBEndPointBulk = url;
            } else {
                throw new MalformedURLException();
            }
            return this;
        }

        public IBConfig.Builder setBulkSize(int size) {
            mBulkSize = size;
            return this;
        }

        public IBConfig.Builder setFlushInterval(int interval) {
            mFlushInterval = interval;
            return this;
        }

        public IBConfig.Builder setMaximumRequestLimit(long bytes) {
            mMaximumRequestLimit = (bytes >= MINIMUM_REQUEST_LIMIT) ? bytes : mMaximumRequestLimit;
            return this;
        }

        public IBConfig build() {
            IBConfig config = new IBConfig();
            config.setBulkSize(mBulkSize);
            config.setFlushInterval(mFlushInterval);
            config.setLogLevel(mLoggerMode);
            config.setMaximumRequestLimit(mMaximumRequestLimit);
            try {
                config.setIBEndPoint(mIBEndPoint);
            } catch (MalformedURLException ex) {
                //TODO: do something
                Logger.log("Failed to set new IronBeast URL for reports", Logger.SDK_DEBUG);
            }
            try {
                config.setIBEndPointBulk(mIBEndPointBulk);
            } catch (MalformedURLException ex) {
                //TODO: do something
                Logger.log("Failed to set new IronBeast URL for bulk reports", Logger.SDK_DEBUG);
            }
            return config;
        }
    }
}
