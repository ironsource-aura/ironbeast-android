package io.ironbeast.sdk;

import android.content.Context;
import android.os.Build;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class IronBeast {

    /**
     * Do not call directly.
     * You should use IronBeast.getInstance()
     */
    public IronBeast(Context context) {
        mContext = context;
        mConfig = IBConfig.getInstance(context);
    }

    public static IronBeast getInstance() {return  sInstance;}

    public static IronBeast getInstance(Context context) {
        if (null == context) {
            throw new IllegalArgumentException("`context` should be valid Context object");
        }
        synchronized (sInstanceLockObject) {
            if (sInstance == null) {
                sInstance = new IronBeast(context.getApplicationContext());
            }
        }
        return sInstance;
    }

    /**
     * Create IronBeastTracker with your API_TOKEN.
     * Example:
     * <code>
     *      IronBeastTracker tracker = ironBeast.newTracker("YOUR_API_TOKEN");
     * </code>
     * @param token
     * @return IronBeastTracker
     */
    public IronBeastTracker newTracker(String token) {
        if (null == token) {
            throw new IllegalArgumentException("`token` should be valid String");
        }
        synchronized (sAvailableTrackers) {
            IronBeastTracker ret;
            if (sAvailableTrackers.containsKey(token)) {
                ret = sAvailableTrackers.get(token);
            } else {
                ret = new IronBeastTracker(sInstance.mContext, token);
                sAvailableTrackers.put(token, ret);
            }
            return ret;
        }
    }

    /**
     * Enable the SDK error-tracker.
     */
    public void enableErrorReporting() { mConfig.enableErrorReporting(); }

    /**
     * Set whether the SDK can keep sending over a roaming connection.
     * @param allowed
     */
    public void setAllowedOverRoaming(boolean allowed) {
        mConfig.setAllowedOverRoaming(allowed);
    }

    /**
     * Restrict the types of networks over which this SDK can keep making HTTP requests.
     * By default, all network types are allowed
     * @param flags
     */
    public void setAllowedNetworkTypes(int flags) {
        mConfig.setAllowedNetworkTypes(flags);
    }

    /**
     * Set the SDK log level.
     * @param logType
     */
    public void setLogType(IBConfig.LOG_TYPE logType) {
        Logger.logLevel  = logType;
    }

    /**
     * function set report bulk max size
     *
     * @param size - max size of report bulk (rows)
     */
    public void setBulkSize(int size) {
        mConfig.setBulkSize(size);
    }

    /**
     * function set report max size in bytes
     *
     * @param bytes - max size of report (file size)
     */
    public void setMaximumRequestLimit(long bytes) {
        mConfig.setMaximumRequestLimit(bytes);
    }

    /**
     * function set report flush intervals
     *
     * @param seconds - time for flush
     */
    public void setFlushInterval(int seconds) {
        mConfig.setFlushInterval(seconds);
    }

    /**
     * Track all SDK-errors/crashes when error-tracker enabled.
     * @param str
     */
    protected void trackError(String str) {
        String token = IBConfig.IRONBEAST_TRACKER_TOKEN;
        if (!sAvailableTrackers.containsKey(token) && mConfig.isErrorReportingEnabled()) {
            sAvailableTrackers.put(token, new IronBeastTracker(mContext, token));
        }
        IronBeastTracker sdkTracker = sAvailableTrackers.get(IBConfig.IRONBEAST_TRACKER_TOKEN);
        try {
            JSONObject report = new JSONObject();
            report.put("details", str);
            report.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                    .format(Calendar.getInstance().getTime()));
            report.put("sdk_version", Consts.VER);
            report.put("connection", NetworkManager.getInstance(mContext).getConnectedNetworkType());
            report.put("platform", "Android");
            report.put("os", String.valueOf(Build.VERSION.SDK_INT));
            sdkTracker.track(IBConfig.IRONBEAST_TRACKER_TABLE, report, false);
        } catch (Exception e) {
            Logger.log(TAG, "Failed to track error: " + e, Logger.SDK_DEBUG);
        }
    }


    private IBConfig mConfig;
    private Context mContext;
    private final Map<String, IronBeastTracker> sAvailableTrackers = new HashMap<>();
    private static IronBeast sInstance;
    final static Object sInstanceLockObject = new Object();
    private static final String TAG = "IronBeast";
    public static final int NETWORK_MOBILE = 1 << 0;
    public static final int NETWORK_WIFI = 1 << 1;
}
