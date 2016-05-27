package io.ironsourceatom.sdk;

import android.content.Context;
import android.webkit.URLUtil;


public class IronSourceAtomEventSender {

    private String token;
    private Context context;
    private String endpoint;

    /**
     * This class is the entry point into this client API for work with simple putEvent() and putEvents() methods.
     * </p>
     * You should use <code>IronSourceAtom.newSender(String)</code> to create
     * an instance of this class.
     * </p>
     *
     * @param context
     * @param auth
     */

    IronSourceAtomEventSender(Context context, String auth) {
        this.context = context;
        this.token = auth;

    }

    /**
     *
     * @param streamName
     * @param data
     */
    public void sendEvent(String streamName, String data){
        openReport(context)
                .setEnpoint(endpoint)
                .setTable(streamName)
                .setHttpMethod(HttpMethod.POST)
                .setToken(token)
                .setData(data)
                .send();

    }

    /**
     *
     * @param streamName
     * @param data
     * @param httpMethod
     */
    public void sendEvent(String streamName, String data, HttpMethod httpMethod){
        openReport(context)
                .setEnpoint(endpoint)
                .setTable(streamName)
                .setHttpMethod(httpMethod)
                .setToken(token)
                .setData(data)
                .send();
    }

    /**
     *
     * @param streamName
     * @param data
     */
    public void sendEvents(String streamName, String data){
        openReport(context)
                .setEnpoint(endpoint)
                .setTable(streamName)
                .setHttpMethod(HttpMethod.POST)
                .setToken(token)
                .setData(data)
                .setBulk(true)
                .send();
    }

//    public void sendEvents(String streamName, String data, HttpMethod httpMethod){
//        openReport(context)
//                .setEnpoint(endpoint)
//                .setTable(streamName)
//                .setHttpMethod(httpMethod)
//                .setToken(token)
//                .setData(data)
//                .send();
//
//    }

    /**
     *
     * @param url
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

