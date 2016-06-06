package io.ironsourceatom.sdk;

import android.content.Context;
import android.webkit.URLUtil;
import org.json.JSONObject;
import java.util.Map;

public class IronSourceAtomTracker {


    private String auth;
    private Context context;
    private IsaConfig config;

    /**
     * This class is the entry point into this client API to work with tracker.
     * </p>
     * You should use <code>IronSourceAtomFactory.newTracker(String)</code> to create
     * an instance of this class.
     * </p>
     * While tracking events, IronSourceAtomTracker will queue them to disk (using SQLite),
     * and each period of time it upload it as a batch to IronSourceAtom.
     *
     * @param context
     * @param auth
     */
    IronSourceAtomTracker(Context context, String auth) {
        this.context = context;
        this.auth = auth;
        config = IsaConfig.getInstance(context);
    }

    /**
     * Track an event that already stringify send data mechanism is controlled by sendNow parameter.
     *
     * @param streamName   The name on IronSourceAtom stream.
     * @param data    String, containing the data to send.
     * @param sendNow flag if true report will send immediately else will postponed
     */
    public void track(String streamName, String data, boolean sendNow) {
        openReport(context, sendNow ? SdkEvent.POST_SYNC : SdkEvent.ENQUEUE)
                .setTable(streamName)
                .setToken(auth)
                .setData(data)
                .send();
    }

    /**
     * Track an event, send data mechanism is controlled by sendNow parameter.
     *
     * @param streamName   The name on IronSourceAtom stream.
     * @param data    Map, containing the data to send.
     * @param sendNow Send flag if true report will send immediately else will postponed
     */
    public void track(String streamName, Map<String, ?> data, boolean sendNow) {
        track(streamName, new JSONObject(data), sendNow);
    }

    /**
     * Track an event, send data mechanism is controlled by sendNow parameter.
     *
     * @param streamName   The name on IronSourceAtom stream.
     * @param data    JSONObject, containing the data to send.
     * @param sendNow Send flag if true report will send immediately else will postponed
     */
    public void track(String streamName, JSONObject data, boolean sendNow) {
        track(streamName, data.toString(), sendNow);
    }

    /**
     * Track an event that already stringify send data postponed.
     *
     * @param streamName   The name on IronSourceAtom stream.
     * @param data    String, containing the data to send.
     */
    public void track(String streamName, String data) {
        track(streamName, data, false);
    }

    /**
     * Track an event, send data postponed.
     *
     * @param table   IronSourceAtomFactory destination.
     * @param data    Map, containing the data to send.
     */
    public void track(String table, Map<String, ?> data) {
        track(table, new JSONObject(data), false);
    }

    /**
     * Track an event, send data postponed.
     *
     * @param streamName   The name on IronSourceAtom stream.
     * @param data    JSONObject, containing the data to send.
     */
    public void track(String streamName, JSONObject data) {
        track(streamName, data.toString(), false);
    }
    /**
     * Flush immediately all reports
     */
    public void flush() {
        openReport(context, SdkEvent.FLUSH_QUEUE)
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
    public void setISAEndPoint(String url) {
        if (URLUtil.isValidUrl(url)) config.setISAEndPoint(auth, url);
    }

    /**
     * Set custom endpoint to send bulk reports
     *
     * @param url Custom publisher destination url.
     */
    public void setISAEndPointBulk(String url) {
        if (URLUtil.isValidUrl(url)) config.setISAEndPointBulk(auth, url);
    }

}
