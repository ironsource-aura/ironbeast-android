package io.ironbeast.sdk;

import android.content.Context;
import android.util.Log;
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
    private static final String DEFAULT_URL = "http://sdk.ironbeast.io";
    private static final String DEFAULT_BULK_URL = "http://sdk.ironbeast.io/bulk";
    private static final String RECORDS_FILENAME = "com.ironsource.mobilcore.ib_records";
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
    private static final String KEY_MAX_REQUEST_LIMIT = "max_request_limit";
    private static final String KEY_SDK_TRACKER_ENABLED = "sdk_tracker_enabled";
    // IronBeast sTracker configuration
    protected static String IRONBEAST_TRACKER_TABLE = "ironbeast_sdk";
    protected static String IRONBEAST_TRACKER_TOKEN = "ironbeast_tracker";
    private boolean mSdkTrackerEnabled;
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
        mSdkTrackerEnabled = true;
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

    static IBConfig getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (null == sInstance) {
                Log.d("IBConfig", "null == sInstance");
                sInstance = new IBConfig(context);
            } else if (sInstance.isDefaultConstructorUsed) {
                Log.d("IBConfig", "sInstance.isDefaultConstructorUsed");
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
        mSdkTrackerEnabled = Boolean.getBoolean(mIBPrefService.load(KEY_SDK_TRACKER_ENABLED, "false"));
        mBulkSize = Integer.getInteger(mIBPrefService.load(KEY_BULK_SIZE, ""), DEFAULT_BULK_SIZE);
        mFlushInterval = Integer.getInteger(mIBPrefService.load(KEY_FLUSH_INTERVAL, ""), DEFAULT_FLUSH_INTERVAL);
        mMaximumRequestLimit = Integer.getInteger(mIBPrefService.load(KEY_MAX_REQUEST_LIMIT, ""), DEFAULT_MAX_REQUEST_LIMIT);
        mNumOfRetries = DEFAULT_NUM_OF_RETRIES;
        mIdleSeconds = DEFAULT_IDLE_SECONDS;
    }

    void apply() {
        mIBPrefService.save(KEY_MAX_REQUEST_LIMIT, String.valueOf(mMaximumRequestLimit));
        mIBPrefService.save(KEY_BULK_SIZE, String.valueOf(mBulkSize));
        mIBPrefService.save(KEY_FLUSH_INTERVAL, String.valueOf(mFlushInterval));
        mIBPrefService.save(KEY_IB_END_POINT, mIBEndPoint);
        mIBPrefService.save(KEY_IB_END_POINT_BULK, mIBEndPointBulk);
        mIBPrefService.save(KEY_SDK_TRACKER_ENABLED, String.valueOf(mSdkTrackerEnabled));
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

    IBConfig setFlushInterval(int seconds) {
        mFlushInterval = seconds;
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

    IBConfig setSDKTracker(boolean enable) {
        mSdkTrackerEnabled = enable;
        return this;
    }

    public enum LOG_TYPE {
        PRODUCTION, DEBUG
    }

    public static class Builder {
        IBConfig mConfig = new IBConfig();
        public IBConfig.Builder setLogLevel(LOG_TYPE logLevel) {
            mConfig.setLogLevel(logLevel);
            return this;
        }

        public IBConfig.Builder setIBEndPoint(String url) throws MalformedURLException {
            if (URLUtil.isValidUrl(url)) {
                mConfig.setIBEndPoint(url);
            } else {
                throw new MalformedURLException();
            }
            return this;
        }

        public IBConfig.Builder setIBEndPointBulk(String url) throws MalformedURLException {
            if (URLUtil.isValidUrl(url)) {
                mConfig.setIBEndPointBulk(url);
            } else {
                throw new MalformedURLException();
            }
            return this;
        }

        public IBConfig.Builder setBulkSize(int size) {
            mConfig.setBulkSize(size);
            return this;
        }

        public IBConfig.Builder setFlushInterval(int seconds) {
            mConfig.setFlushInterval(seconds);
            return this;
        }

        public IBConfig.Builder setMaximumRequestLimit(long bytes) {
            mConfig.setMaximumRequestLimit(bytes);
            return this;
        }

        public IBConfig.Builder setSDKTracker(boolean enable) {
            mConfig.setSDKTracker(enable);
            return this;
        }

        public IBConfig build() {
            return mConfig;
        }
    }
}
