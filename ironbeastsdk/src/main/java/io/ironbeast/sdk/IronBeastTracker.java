package io.ironbeast.sdk;

import android.content.Context;
import android.webkit.URLUtil;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.Map;

public class IronBeastTracker {

    protected String mToken;
    protected Context mContext;
    protected IBConfig mConfig;

    IronBeastTracker(Context context, String token) {
        mContext = context;
        mToken = token;
        mConfig = IBConfig.getInstance(context);
    }

    /**
     * Track an event that already stringified send data postponed.
     *
     * @param table - IronBeast destination.
     * @param data - String, containing the data to track.
     */
    public void track(String table, String data) {
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
     * @param table - IronBeast destination table.
     * @param data - String, containing the data to post.
     */
    public void post(String table, String data) {
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

    public void setIBEndPoint(String url) throws MalformedURLException {
        if (URLUtil.isValidUrl(url)) {
            mConfig.setIBEndPoint(mToken, url);
        } else {
            throw new MalformedURLException();
        }
    }

    public void setIBEndPointBulk(String url) throws MalformedURLException {
        if (URLUtil.isValidUrl(url)) {
            mConfig.setIBEndPointBulk(mToken, url);
        } else {
            throw new MalformedURLException();
        }
    }


}
