package io.ironbeast.sdk;

import android.content.Context;
import android.util.Log;
import android.webkit.URLUtil;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Locale;

/**
 * TODO:
 * - finish docs
 * - Write toSharedPreferences
 * Create a global configuration options for the IronBeast library.
 * IBConfig understands the following options:
 * NumOfRetries        - number of retries requests, when "post" request failed or when
 * the device not connect to the internet.
 * BulkSize            - maximum entries in each bulk request(on tracking).
 * FlushInterval       - flushing interval timer
 * MaximumRequestLimit - maximum bytes in request body.
 */
class IBConfig {
    private static final String TAG = IBConfig.class.getSimpleName();

    private static final Object sInstanceLock = new Object();
    protected static final String DEFAULT_URL = "http://sdk.ironbeast.io";
    protected static final String DEFAULT_BULK_URL = "http://sdk.ironbeast.io/bulk";
    protected static final int KILOBYTE = 1024;
    protected static final int DEFAULT_BULK_SIZE = 4;
    protected static final int DEFAULT_NUM_OF_RETRIES = 2;
    protected static final int DEFAULT_FLUSH_INTERVAL = 10 * 1000;
    protected static final int DEFAULT_MAX_REQUEST_LIMIT = KILOBYTE * KILOBYTE;
    protected static final int DEFAUL_MAX_DATABASE_LIMIT = KILOBYTE * KILOBYTE * 10;
    //Shared prefs keys for metadata
    protected static final String KEY_BULK_SIZE = "bulk_size";
    protected static final String KEY_FLUSH_INTERVAL = "flush_interval";
    protected static final String KEY_IB_END_POINT = "ib_end_point";
    protected static final String KEY_IB_END_POINT_BULK = "ib_end_point_bulk";
    protected static final String KEY_MAX_REQUEST_LIMIT = "max_request_limit";
    protected static final String KEY_MAX_DATABASE_LIMIT = "max_database_limit";
    protected static final String KEY_ENABLE_ERROR_REPORTING = "sdk_tracker_enabled";
    // IronBeast sTracker configuration
    protected static String IRONBEAST_TRACKER_TABLE = "ironbeast_sdk";
    protected static String IRONBEAST_TRACKER_TOKEN = "5ALP9S8DUSpnL3hm4N8BewFnzZqzKt";
    private static IBConfig sInstance;
    SharePrefService mIBPrefService;
    private boolean mEnableErrorReporting;
    private int mBulkSize;
    private int mFlushInterval;

    private HashMap<String, String> mIBEndPoint;
    private HashMap<String, String> mIBEndPointBulk;
    private long mMaximumRequestLimit;
    private long mMaximumDatabaseLimit;

    IBConfig(Context context) {
        loadConfig(context);
    }

    static IBConfig getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (null == sInstance) {
                Log.d("IBConfig", "null == sInstance");
                sInstance = new IBConfig(context);
            }
        }
        return sInstance;
    }


    /**
     * function called on instance initialization and load data from SharedPref service
     *
     * @param context
     */
    void loadConfig(Context context) {
        mIBPrefService = getPrefService(context);

        mIBEndPoint = new HashMap<>();
        mIBEndPointBulk = new HashMap<>();

        mEnableErrorReporting = Boolean.parseBoolean(mIBPrefService.load(KEY_ENABLE_ERROR_REPORTING, "false"));

        mFlushInterval = Integer.parseInt(mIBPrefService.load(KEY_FLUSH_INTERVAL, String.valueOf(DEFAULT_FLUSH_INTERVAL)));
        mMaximumRequestLimit = Integer.parseInt(mIBPrefService.load(KEY_MAX_REQUEST_LIMIT, String.valueOf(DEFAULT_MAX_REQUEST_LIMIT)));
        mMaximumDatabaseLimit = Integer.parseInt(mIBPrefService.load(KEY_MAX_DATABASE_LIMIT, String.valueOf(DEFAUL_MAX_DATABASE_LIMIT)));
        mBulkSize = Integer.parseInt(mIBPrefService.load(KEY_BULK_SIZE, String.valueOf(DEFAULT_BULK_SIZE)));
    }

    /**
     * Function provide custom end point url for report if was set or default IronBeast Url
     *
     * @param token uniq publisher token
     * @return url of tracker end point if
     */
    public String getIBEndPoint(String token) {
        if (mIBEndPoint.containsKey(token)) {
            return mIBEndPoint.get(token);
        }
        String url = mIBPrefService.load(String.format("%s_%s", KEY_IB_END_POINT, token), "");
        if (URLUtil.isValidUrl(url)) {
            mIBEndPoint.put(token, url);
            return url;
        }
        return DEFAULT_URL;
    }

    /**
     * Function set custom URL for tracker
     *
     * @param token uniq publisher token
     * @param url   custom tracker URL
     */
    protected void setIBEndPoint(String token, String url) throws MalformedURLException {
        mIBEndPointBulk.put(token, url);
        mIBPrefService.save(String.format("%s_%s", KEY_IB_END_POINT, token), url);
    }

    /**
     * Function provide custom end point url for bulk report if was set or default IronBeast Url
     *
     * @param token uniq publisher token
     * @return url of tracker end point if
     */
    public String getIBEndPointBulk(String token) {
        if (mIBEndPointBulk.containsKey(token)) {
            return mIBEndPointBulk.get(token);
        }
        String url = mIBPrefService.load(String.format("%s_%s", KEY_IB_END_POINT_BULK, token), "");
        if (URLUtil.isValidUrl(url)) {
            mIBEndPointBulk.put(token, url);
            return url;
        }
        return DEFAULT_BULK_URL;
    }

    /**
     * Function set custom URL for tracker
     *
     * @param token uniq publisher token
     * @param url   custom tracker URL
     * @throws MalformedURLException
     */
    protected void setIBEndPointBulk(String token, String url) throws MalformedURLException {
        mIBEndPointBulk.put(token, url);
        mIBPrefService.save(String.format("%s_%s", KEY_IB_END_POINT_BULK, token), url);
    }

    /**
     * Function return the max number of reports in sending bulk
     *
     * @return
     */
    public int getBulkSize() {
        return mBulkSize;
    }

    /**
     * Function set the max number of reports in sending bulk
     *
     * @param size max number of reports in bulk
     */
    void setBulkSize(int size) {
        mBulkSize = size > 0 ? size : mBulkSize;
        mIBPrefService.save(KEY_BULK_SIZE, String.valueOf(mBulkSize));
    }

    /**
     * Function return next flush time of report
     *
     * @return automatic flush time
     */
    public int getFlushInterval() {
        return mFlushInterval;
    }

    void setFlushInterval(int seconds) {
        mFlushInterval = seconds;
        mIBPrefService.save(KEY_FLUSH_INTERVAL, String.valueOf(mFlushInterval));
    }

    public long getMaximumRequestLimit() {
        return mMaximumRequestLimit;
    }

    void setMaximumRequestLimit(long bytes) {
        mMaximumRequestLimit = bytes >= KILOBYTE ? bytes : mMaximumRequestLimit;
        mIBPrefService.save(KEY_MAX_REQUEST_LIMIT, String.valueOf(mMaximumRequestLimit));
    }

    /**
     * @return maximum size of saved reports
     */
    public long getMaximumDatabaseLimit() {
        return mMaximumDatabaseLimit;
    }

    void setMaximumDatabaseLimit(long bytes) {
        mMaximumDatabaseLimit = bytes >= (KILOBYTE * KILOBYTE) ? bytes : mMaximumDatabaseLimit;
        mIBPrefService.save(KEY_MAX_DATABASE_LIMIT, String.valueOf(mMaximumDatabaseLimit));
    }

    /**
     * @return sdk num of retries on sending report failed
     */
    public int getNumOfRetries() {
        return DEFAULT_NUM_OF_RETRIES;
    }

    /**
     * Function enable/disable sending error reports
     *
     * @param enable
     */
    public void enableErrorReporting(boolean enable) {
        mEnableErrorReporting = enable;
        mIBPrefService.save(KEY_ENABLE_ERROR_REPORTING, String.valueOf(mEnableErrorReporting));
    }

    /**
     * Function return if SDK sending error reports
     *
     * @return
     */
    public boolean isErrorReportingEnabled() {
        return mEnableErrorReporting;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "[%s] flushInterval %d " +
                        "req limit %d db limit %s bSize %d error enable ",
                TAG, mFlushInterval, mMaximumRequestLimit,
                mMaximumDatabaseLimit, mBulkSize) +
                mEnableErrorReporting;
    }


    /**
     * Function provide Preference service to save and load IBConfig data
     *
     * @param context application Context
     * @return SharePrefService
     */
    protected SharePrefService getPrefService(Context context) {
        return IBPrefService.getInstance(context);
    }

    public enum LOG_TYPE {
        PRODUCTION, DEBUG
    }
}