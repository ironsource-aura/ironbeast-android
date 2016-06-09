package io.ironsourceatom.sdk;

import android.content.Context;
import android.webkit.URLUtil;

/**
 * This class is the entry point into this client API for work with low level putEvent() and putEvents() methods.
 *
 */
public class IronSourceAtom {

    private String token;
    private Context context;
    private String endpoint;

    /**
     * This class is the entry point into this client API for work with simple putEvent() and putEvents() methods.
     * </p>
     * You should use <code>IronSourceAtomFactory.newAtom(String)</code> to create
     * an instance of this class.
     * </p>
     *
     * @param context current context object
     * @param auth pre shared auth key for Atom cluster
     */

    protected IronSourceAtom(Context context, String auth) {
        this.context = context;
        this.token = auth;

    }

    /**
     * Sends a single event to IronSourceAtom stream
     * @param streamName the name on IronSourceAtom stream
     * @param data JSON string of your event data
     */
    public void putEvent(String streamName, String data){
        openReport(context)
                .setEnpoint(endpoint)
                .setTable(streamName)
                .setToken(token)
                .setData(data)
                .send();

    }



    /**
     * Sends an array of events to IronSourceAtom stream
     * @param streamName the name on IronSourceAtom stream
     * @param data JSON string of your event data
     */
    public void putEvents(String streamName, String data){
        openReport(context)
                .setEnpoint(endpoint)
                .setTable(streamName)
                .setToken(token)
                .setData(data)
                .setBulk(true)
                .send();
    }

    /**
     * Set custom endpoint to send reports
     * @param url custom publisher destination url.
     */
    public void setEndPoint(String url) {
        if (URLUtil.isValidUrl(url)){
            this.endpoint=url;
        } else  {
            throw new IllegalArgumentException("Enpoint must be valid url");
        }
    }

    protected Report openReport(Context context) {
        return new SimpleReportIntent(context);
    }

    }

