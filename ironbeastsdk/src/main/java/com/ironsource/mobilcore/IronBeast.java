package com.ironsource.mobilcore;

import android.content.Context;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class IronBeast {

    IronBeast(Context context, String token) {
        appContext = context;
        mToken = token;
        mConfig = IBConfig.getsInstance();
    }

    public static IronBeast getInstance (Context context, String token) {
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

    public IronBeast setConfig (IBConfig config) {
        mConfig = config;
        return this;
    }

    public void track(String table, Map<String, String> data) {
        JSONObject jsonObject = new JSONObject(data);
        track(table, jsonObject);
    }

    public void track(String table, String data) {
        //TODO: escaping on data or encode in order to hide not valid characters
        ReportHandler.openReport(appContext, SdkEvent.ENQUEUE)
                .setTable(table)
                .setToken(mToken)
                .setData(data)
                .send();
    }

    public void track(String table, JSONObject data) {
        track(table, data.toString());
    }

    public void post(String table, Map<String, String> data) {
        JSONObject jsonObject = new JSONObject(data);
        track(table, jsonObject);
    }

    public void post(String table, String data) {
        //TODO: escaping on data or encode in order to hide not valid characters
        ReportHandler.openReport(appContext, SdkEvent.POST_SYNC)
                .setTable(table)
                .setToken(mToken)
                .setData(data)
                .send();
    }

    public void post (String table, JSONObject data) {
        post(table, data.toString());
    }

    public void flush () {
        ReportHandler.openReport(appContext, SdkEvent.FLUSH_QUEUE)
                .send();
    }

    private static final Map<String, IronBeast> sInstances = new HashMap<String, IronBeast>();
    private IBConfig mConfig;
    private Context appContext;
    private String mToken;
}
