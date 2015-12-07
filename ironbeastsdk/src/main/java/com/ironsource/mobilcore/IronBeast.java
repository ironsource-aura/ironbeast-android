package com.ironsource.mobilcore;

import android.content.Context;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class IronBeast {

    /**
     * Do not call directly.
     * You should use IronBeast.getInstance()
     */
    public IronBeast(Context context, String token) {
        appContext = context;
        mToken = token;
        mConfig = IBConfig.getsInstance();
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
        mConfig = config;
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
        openReport(appContext, SdkEvent.ENQUEUE)
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
        openReport(appContext, SdkEvent.POST_SYNC)
                .setTable(table)
                .setToken(mToken)
                .setData(data)
                .send();
    }

    public void post(String table, JSONObject data) {
        post(table, data.toString());
    }

    public void post(String table, Map<String, ?> data) { post(table, new JSONObject(data)); }

    public void flush() {
        openReport(appContext, SdkEvent.FLUSH_QUEUE)
                .send();
    }

    protected Report openReport(Context context, int event) {
        return new ReportIntent(context, event);
    }

    private static final Map<String, IronBeast> sInstances = new HashMap<String, IronBeast>();
    private IBConfig mConfig;
    private Context appContext;
    private String mToken;
}
