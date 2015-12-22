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

    private static final String TAG = IronBeast.class.getSimpleName();

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
            throw new IllegalArgumentException("Please provide valid context");
        }
        synchronized (sInstanceLockObject) {
            if (sInstance == null) {
                sInstance = new IronBeast(context.getApplicationContext());
            }
        }
        return sInstance;
    }

    /**
     * Use this to get a singleton instance of IronBeastTracker instead of creating one directly
     * for yourself.
     */
    public IronBeastTracker newTracker(String token) {
        if (null == token) {
            throw new IllegalArgumentException("Please provide valid token");
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
     * function enable to report errors from SDK
     *
     * @param enable - enable/disable error reports
     */
    public void enableErrorReporting(boolean enable) {
        mConfig.enableErrorReporting(enable);
    }

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

    protected void trackError(String str) {
        Logger.log(TAG, "trackError " + str, Logger.SDK_DEBUG);
        if (!sAvailableTrackers.containsKey(IBConfig.IRONBEAST_TRACKER_TOKEN) && mConfig.isErrorReportingEnabled()) {
            sAvailableTrackers.put(IBConfig.IRONBEAST_TRACKER_TOKEN, new IronBeastTracker(mContext, IBConfig.IRONBEAST_TRACKER_TOKEN));
            Logger.log(TAG, "error tracker created " + str, Logger.SDK_DEBUG);
        }

        IronBeastTracker sdkTracker = sAvailableTrackers.get(IBConfig.IRONBEAST_TRACKER_TOKEN);
        //TODO: sdkTracker maybe NULL
        try {
            JSONObject report = new JSONObject();
            report.put("details", str);
            report.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                    .format(Calendar.getInstance().getTime()));
            report.put("sdk_version", Consts.VER);
            report.put("connection", Utils.getConnectedNetworkType(mContext));
            report.put("platform", "Android");
            report.put("os", String.valueOf(Build.VERSION.SDK_INT));
            sdkTracker.track(IBConfig.IRONBEAST_TRACKER_TABLE, report);
        } catch (Exception e) {
            //TODO: not good maybe stack here :))))
            Logger.log("Failed to track Error " + e, Logger.SDK_DEBUG);
        }
    }


    private IBConfig mConfig;
    private Context mContext;
    private final Map<String, IronBeastTracker> sAvailableTrackers = new HashMap<>();

    private static IronBeast sInstance;
    final static Object sInstanceLockObject = new Object();
}
