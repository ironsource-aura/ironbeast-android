package com.ironsource.mobilcore;

import android.content.Context;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class IronBeast {

    private String mToken;

    /**
     * Do not call directly.
     * You should use IronBeast.getInstance()
     */
    public IronBeast(Context context, String token) {
        mContext = context;
        mConfig = IBConfig.getInstance(context);
        mToken = token;
        if (!sInstances.containsKey(IBConfig.IRONBEAST_TRACKER_TOKEN)) {
            getInstance(context, IBConfig.IRONBEAST_TRACKER_TOKEN);
        }
    }

    /**
     * Use this to get a singleton instance of IronBeast instead of creating one directly
     * for yourself.
     */
    public static IronBeast getInstance(Context context, String token) {
        if (null == token || null == context) {
            return null;
        }
        synchronized (sInstances) {
            IronBeast ret;
            if (sInstances.containsKey(token)) {
                ret = sInstances.get(token);
            } else {
                ret = new IronBeast(context.getApplicationContext(), token);
                sInstances.put(token, ret);
            }
            return ret;
        }
    }

    public IronBeast setConfig(IBConfig config) {
        IBConfig lConfig = IBConfig.getsInstance();
        lConfig.update(config);
        lConfig.apply();
        return this;
    }

    /**
     * Track an event that already stringified.
     *
     * @param table - IronBeast destination.
     * @param data
     */
    public void track(String table, String data) {
        //TODO: escaping on data or encode in order to hide not valid characters
        openReport(mContext, SdkEvent.ENQUEUE)
                .setTable(table)
                .setToken(mToken)
                .setData(data)
                .send();
    }

    public void track(String table, Map<String, ?> data) {
        track(table, new JSONObject(data));
    }

    public void track(String table, JSONObject data) {
        track(table, data.toString());
    }

    /**
     * Post (send immediately) and event that already stringified.
     *
     * @param table
     * @param data
     */
    public void post(String table, String data) {
        //TODO: escaping on data or encode in order to hide not valid characters
        openReport(mContext, SdkEvent.POST_SYNC)
                .setTable(table)
                .setToken(mToken)
                .setData(data)
                .send();
    }

    public void post(String table, JSONObject data) {
        post(table, data.toString());
    }

    public void post(String table, Map<String, ?> data) {
        post(table, new JSONObject(data));
    }

    public void flush() {
        openReport(mContext, SdkEvent.FLUSH_QUEUE)
                .send();
    }

    protected Report openReport(Context context, int event) {
        return new ReportIntent(context, event);
    }

    protected static void trackError(String str) {
        IronBeast sdkTracker = sInstances.get(IBConfig.IRONBEAST_TRACKER_TOKEN);
        try {
            JSONObject report = new JSONObject();
            report.put("details", str);
            report.put("timestamp", new SimpleDateFormat("yyyy-MM-ddHHmmss")
                    .format(Calendar.getInstance().getTime()));
            report.put("sdk_version", Consts.VER);
            report.put("connection", Utils.getConnectedNetworkType(sdkTracker.mContext));
            report.put("platform", "Android");
            report.put("os", String.valueOf(Build.VERSION.SDK_INT));
            sdkTracker.track(IBConfig.IRONBEAST_TRACKER_TABLE, report);
        } catch (JSONException e) {}
    }

    private static final Map<String, IronBeast> sInstances = new HashMap<String, IronBeast>();
    private IBConfig mConfig;
    private Context mContext;
}
