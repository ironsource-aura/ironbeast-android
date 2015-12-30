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
     * Track an event that already stringify send data mechanism is controlled by SEND property.
     *
     * @param table IronBeast destination.
     * @param data String, containing the data to send.
     * @param send Send option {@link SEND#NOW} {@link SEND#POSTPONE}
     * @see SEND#NOW
     * @see SEND#POSTPONE
     *
     */
    public void track(String table, String data, SEND send) {
        if (send == SEND.NOW) {
            post(table, data);
        } else if (send == SEND.POSTPONE) {
            track(table, data);
        }
    }

    public void track(String table, Map<String, ?> data, SEND send) {
        if (send == SEND.NOW) {
            post(table, data);
        } else if (send == SEND.POSTPONE) {
            track(table, data);
        }
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

    /**
     * SEND options: for use with {@link #track}, if function called with {@link SEND#NOW} param,
     * the report will send immediately. if function called with {@link SEND#POSTPONE} param,
     * the report will postponed.
     *
     * @see #track
     */
    public enum SEND {
        NOW,
        POSTPONE
    }

    private String mToken;
    private Context mContext;
    private IBConfig mConfig;

    public String getIBEndPoint() {
        return mConfig.getIBEndPoint(mToken);
    }
}
