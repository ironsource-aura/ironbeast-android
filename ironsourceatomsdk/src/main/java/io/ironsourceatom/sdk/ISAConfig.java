package io.ironsourceatom.sdk;

import android.content.Context;
import android.webkit.URLUtil;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Locale;

class IsaConfig {

    private static final String TAG = IsaConfig.class.getSimpleName();
    private static final Object sInstanceLock = new Object();
    protected static final String DEFAULT_URL = "http://track.atom-data.io/bulk";
    protected static final String DEFAULT_BULK_URL = "http://track.atom-data.io/bulk";
    protected static final int KILOBYTE = 1024;
    protected static final int DEFAULT_BULK_SIZE = 4;
    protected static final int DEFAULT_NUM_OF_RETRIES = 2;
    protected static final int DEFAULT_FLUSH_INTERVAL = 10 * 1000;
    protected static final int DEFAULT_MAX_REQUEST_LIMIT = KILOBYTE * KILOBYTE;
    protected static final int DEFAUL_MAX_DATABASE_LIMIT = KILOBYTE * KILOBYTE * 10;
    protected static final int DEFAULT_ALLOWED_NETWORK_TYPES = ~0;
    //SharedPreferences keys for metadata
    protected static final String KEY_BULK_SIZE = "bulk_size";
    protected static final String KEY_IB_END_POINT = "ib_end_point";
    protected static final String KEY_FLUSH_INTERVAL = "flush_interval";
    protected static final String KEY_IB_END_POINT_BULK = "ib_end_point_bulk";
    protected static final String KEY_MAX_REQUEST_LIMIT = "max_request_limit";
    protected static final String KEY_MAX_DATABASE_LIMIT = "max_database_limit";
    protected static final String KEY_ENABLE_ERROR_REPORTING = "sdk_tracker_enabled";
    protected static final String KEY_ALLOWED_OVER_ROAMING = "allow_roaming_flush";
    protected static final String KEY_ALLOWED_NETWORK_TYPES = "allowed_network_types";
    // IronSourceAtom sTracker configuration
    protected static String IRONBEAST_TRACKER_TABLE = "ironsourceatom_sdk";
    protected static String IRONBEAST_TRACKER_TOKEN = "5ALP9S8DUSpnL3hm4N8BewFnzZqzKt";
    private static IsaConfig sInstance;

    IsaPrefService isaPrefService;
    private boolean isEnableErrorReporting;
    private boolean isAllowedOverRoaming;
    private int allowedNetworkTypes;
    private int bulkSize;
    private int flushInterval;
    private HashMap<String, String> isaEndPoint;
    private HashMap<String, String> isaEndPointBulk;
    private long maximumRequestLimit;
    private long maximumDatabaseLimit;

    public enum LOG_TYPE {
        PRODUCTION, DEBUG
    }

    IsaConfig(Context context) {
        loadConfig(context);
    }

    static IsaConfig getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (null == sInstance) {
                sInstance = new IsaConfig(context);
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
        isaPrefService = getPrefService(context);
        isaEndPoint = new HashMap<>();
        isaEndPointBulk = new HashMap<>();
        isEnableErrorReporting = isaPrefService.load(KEY_ENABLE_ERROR_REPORTING, false);
        isAllowedOverRoaming = isaPrefService.load(KEY_ALLOWED_OVER_ROAMING, true);
        allowedNetworkTypes = isaPrefService.load(KEY_ALLOWED_NETWORK_TYPES, DEFAULT_ALLOWED_NETWORK_TYPES);
        flushInterval = isaPrefService.load(KEY_FLUSH_INTERVAL, DEFAULT_FLUSH_INTERVAL);
        maximumRequestLimit = isaPrefService.load(KEY_MAX_REQUEST_LIMIT, DEFAULT_MAX_REQUEST_LIMIT);
        maximumDatabaseLimit = isaPrefService.load(KEY_MAX_DATABASE_LIMIT, DEFAUL_MAX_DATABASE_LIMIT);
        bulkSize = isaPrefService.load(KEY_BULK_SIZE, DEFAULT_BULK_SIZE);
    }

    /**
     * Function provide custom end point url for report if was set or default IronSourceAtom Url
     *
     * @param token unique publisher token
     * @return url of tracker end point
     */
    public String getIBEndPoint(String token) {
        if (isaEndPoint.containsKey(token)) {
            return isaEndPoint.get(token);
        }
        String url = isaPrefService.load(String.format("%s_%s", KEY_IB_END_POINT, token));
        if (URLUtil.isValidUrl(url)) {
            isaEndPoint.put(token, url);
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
    protected void setISAEndPoint(String token, String url) {
        isaEndPoint.put(token, url);
        isaPrefService.save(String.format("%s_%s", KEY_IB_END_POINT, token), url);
    }

    /**
     * Function provide custom end point url for bulk report if was set or default IronSourceAtom Url
     *
     * @param token unique publisher token
     * @return url of tracker end point if
     */
    public String getIBEndPointBulk(String token) {
        if (isaEndPointBulk.containsKey(token)) {
            return isaEndPointBulk.get(token);
        }
        String url = isaPrefService.load(String.format("%s_%s", KEY_IB_END_POINT_BULK, token));
        if (URLUtil.isValidUrl(url)) {
            isaEndPointBulk.put(token, url);
            return url;
        }
        return DEFAULT_BULK_URL;
    }

    /**
     * Function set custom URL for tracker
     *
     * @param token unique publisher token
     * @param url   custom tracker URL
     * @throws MalformedURLException
     */
    protected void setISAEndPointBulk(String token, String url) {
        isaEndPointBulk.put(token, url);
        isaPrefService.save(String.format("%s_%s", KEY_IB_END_POINT_BULK, token), url);
    }

    /**
     * Function return the max number of reports in sending bulk
     *
     * @return
     */
    public int getBulkSize() {
        return bulkSize;
    }

    /**
     * Function set the max number of reports in sending bulk
     *
     * @param size max number of reports in bulk
     */
    void setBulkSize(int size) {
        bulkSize = size > 0 ? size : bulkSize;
        isaPrefService.save(KEY_BULK_SIZE, bulkSize);
    }

    /**
     * Function return next flush time of report
     *
     * @return automatic flush time
     */
    public int getFlushInterval() {
        return flushInterval;
    }

    void setFlushInterval(int ms) {
        flushInterval = ms;
        isaPrefService.save(KEY_FLUSH_INTERVAL, flushInterval);
    }

    public long getMaximumRequestLimit() {
        return maximumRequestLimit;
    }

    void setMaximumRequestLimit(long bytes) {
        maximumRequestLimit = bytes >= KILOBYTE ? bytes : maximumRequestLimit;
        isaPrefService.save(KEY_MAX_REQUEST_LIMIT, maximumRequestLimit);
    }

    /**
     * @return maximum size of saved reports
     */
    public long getMaximumDatabaseLimit() {
        return maximumDatabaseLimit;
    }

    void setMaximumDatabaseLimit(long bytes) {
        maximumDatabaseLimit = bytes >= (KILOBYTE * KILOBYTE) ? bytes : maximumDatabaseLimit;
        isaPrefService.save(KEY_MAX_DATABASE_LIMIT, maximumDatabaseLimit);
    }

    /**
     * @return sdk num of retries on sending report failed
     */
    public int getNumOfRetries() {
        return DEFAULT_NUM_OF_RETRIES;
    }

    /**
     * Enable the SDK error-tracker.
     */
    public void enableErrorReporting() {
        isEnableErrorReporting = true;
        isaPrefService.save(KEY_ENABLE_ERROR_REPORTING, isEnableErrorReporting);
    }

    /**
     * return if SDK sending error reports
     *
     * @return boolean
     */
    public boolean isErrorReportingEnabled() {
        return isEnableErrorReporting;
    }

    /**
     * return if the SDK should send reports only when the device is connected via WiFi.
     *
     * @return boolean
     */
    public boolean isAllowedOverRoaming() {
        return isAllowedOverRoaming;
    }

    /**
     * Set whether the SDK can keep sending over a roaming connection.
     */
    public void setAllowedOverRoaming(boolean allowed) {
        isAllowedOverRoaming = allowed;
        isaPrefService.save(KEY_ALLOWED_OVER_ROAMING, isAllowedOverRoaming);
    }

    /**
     * Restrict the types of networks over which this SDK can keep making HTTP requests.
     * By default, all network types are allowed
     */
    public void setAllowedNetworkTypes(int flags) {
        allowedNetworkTypes = flags;
        isaPrefService.save(KEY_ALLOWED_NETWORK_TYPES, allowedNetworkTypes);
    }

    protected int getAllowedNetworkTypes() {
        return allowedNetworkTypes;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "[%s] flushInterval %d " +
                        "req limit %d db limit %s bSize %d error enable ",
                TAG, flushInterval, maximumRequestLimit,
                maximumDatabaseLimit, bulkSize) +
                isEnableErrorReporting;
    }


    /**
     * Function provide Preference service to save and load IsaConfig data
     *
     * @param context application Context
     * @return SharePrefService
     */
    protected IsaPrefService getPrefService(Context context) {
        return IsaPrefService.getInstance(context);
    }




}