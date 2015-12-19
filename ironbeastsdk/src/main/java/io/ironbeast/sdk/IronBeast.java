package io.ironbeast.sdk;

import android.content.Context;
import android.os.Build;

import org.json.JSONException;
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
    public static IronBeastTracker newTracker(String token) {
        if (null == token) {
            throw new IllegalArgumentException("Please provide valid token");
        }
        synchronized (sAvailableTrackers) {
            IronBeastTracker ret;
            Context context = sInstance.mContext;
            if (sAvailableTrackers.containsKey(token)) {
                ret = sAvailableTrackers.get(token);
            } else {
                ret = new IronBeastTracker(sInstance.mContext, token);
                sAvailableTrackers.put(token, ret);
            }
            if (!sAvailableTrackers.containsKey(IBConfig.IRONBEAST_TRACKER_TOKEN) && IBConfig.getInstance(context).isErrorReportingEnabled()) {
                sAvailableTrackers.put(IBConfig.IRONBEAST_TRACKER_TOKEN, new IronBeastTracker(context, IBConfig.IRONBEAST_TRACKER_TOKEN));
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

    protected static void trackError(String str) {
        IronBeastTracker sdkTracker = sAvailableTrackers.get(IBConfig.IRONBEAST_TRACKER_TOKEN);
        try {
            JSONObject report = new JSONObject();
            report.put("details", str);
            report.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                    .format(Calendar.getInstance().getTime()));
            report.put("sdk_version", Consts.VER);
            report.put("connection", Utils.getConnectedNetworkType(sdkTracker.mContext));
            report.put("platform", "Android");
            report.put("os", String.valueOf(Build.VERSION.SDK_INT));
            sdkTracker.track(IBConfig.IRONBEAST_TRACKER_TABLE, report);
        } catch (JSONException e) {}
    }

    private static final Map<String, IronBeastTracker> sAvailableTrackers = new HashMap<>();
    private static IronBeast sInstance;
    private IBConfig mConfig;
    private Context mContext;

    final static Object sInstanceLockObject = new Object();
}
