package com.ironsource.mobilcore;

import com.ironsource.mobilcore.ReportingConsts.EReportType;
import android.content.Context;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class IronBeast {

    IronBeast(Context context, String token) {
        appContext = context;
        mToken = token;
        mConfig = IBConfig.getsInstance(context);
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

    public void track (String table, JSONObject data) {
        IronBeastReportData.openReport(appContext, EReportType.ENQUEUE)
                .setTable(table)
                .setToken(mToken)
                .setData(data.toString())
                .send();
    }

    public void post (String table, JSONObject data) {
        IronBeastReportData.openReport(appContext, EReportType.POST_SYNC)
                .setTable(table)
                .setToken(mToken)
                .setData(data.toString())
                .send();
    }

    public void flush () {
        IronBeastReportData.openReport(appContext, EReportType.FLUSH_QUEUE)
                .send();
    }

    private static final Map<String, IronBeast> sInstances = new HashMap<String, IronBeast>();
    private IBConfig mConfig;
    private Context appContext;
    private String mToken;
}
