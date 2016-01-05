package io.ironbeast.sdk;

import android.content.Context;
import android.webkit.URLUtil;

import org.json.JSONObject;

import java.util.Map;

public class IronBeastTracker {

    IronBeastTracker(Context context, String token) {
        mContext = context;
        mToken = token;
        mConfig = IBConfig.getInstance(context);
    }

    /**
     * Track an event that already stringify send data mechanism is controlled by sendNow parameter.
     *
     * @param table   IronBeast destination.
     * @param data    String, containing the data to send.
     * @param sendNow flag if true report will send immediately else will postponed
     */
    public void track(String table, String data, boolean sendNow) {
        openReport(mContext, sendNow ? SdkEvent.POST_SYNC : SdkEvent.ENQUEUE)
                .setTable(table)
                .setToken(mToken)
                .setData(data)
                .send();
    }

    /**
     * Track an event, send data mechanism is controlled by sendNow parameter.
     *
     * @param table   IronBeast destination.
     * @param data    Map, containing the data to send.
     * @param sendNow Send flag if true report will send immediately else will postponed
     */
    public void track(String table, Map<String, ?> data, boolean sendNow) {
        track(table, new JSONObject(data), sendNow);
    }

    /**
     * Track an event, send data mechanism is controlled by sendNow parameter.
     *
     * @param table   IronBeast destination.
     * @param data    JSONObject, containing the data to send.
     * @param sendNow Send flag if true report will send immediately else will postponed
     */
    public void track(String table, JSONObject data, boolean sendNow) {
        track(table, data.toString(), sendNow);
    }

    /**
     * Track an event that already stringify send data postponed.
     *
     * @param table   IronBeast destination.
     * @param data    String, containing the data to send.
     */
    public void track(String table, String data) {
        track(table, data, false);
    }

    /**
     * Track an event, send data postponed.
     *
     * @param table   IronBeast destination.
     * @param data    Map, containing the data to send.
     */
    public void track(String table, Map<String, ?> data) {
        track(table, new JSONObject(data), false);
    }

    /**
     * Track an event, send data postponed.
     *
     * @param table   IronBeast destination.
     * @param data    JSONObject, containing the data to send.
     */
    public void track(String table, JSONObject data) {
        track(table, data.toString(), false);
    }
    /**
     * Flush immediately all reports
     */
    public void flush() {
        openReport(mContext, SdkEvent.FLUSH_QUEUE)
                .send();
    }

    protected Report openReport(Context context, int event) {
        return new ReportIntent(context, event);
    }

    /**
     * Set custom endpoint to send reports
     *
     * @param url Custom publisher destination url.
     */
    public void setIBEndPoint(String url) {
        if (URLUtil.isValidUrl(url)) mConfig.setIBEndPoint(mToken, url);
    }

    /**
     * Set custom endpoint to send bulk reports
     *
     * @param url Custom publisher destination url.
     */
    public void setIBEndPointBulk(String url) {
        if (URLUtil.isValidUrl(url)) mConfig.setIBEndPointBulk(mToken, url);
    }

    private String mToken;
    private Context mContext;
    private IBConfig mConfig;
}
