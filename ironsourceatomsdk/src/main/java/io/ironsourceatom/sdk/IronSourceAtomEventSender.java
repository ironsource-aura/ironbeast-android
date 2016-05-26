package io.ironsourceatom.sdk;

import android.content.Context;
import android.webkit.URLUtil;

/**
 * Created by kirill.bokhanov on 5/26/16.
 */
public class IronSourceAtomEventSender {

    private String token;
    private Context context;
    private String endpoint;

    IronSourceAtomEventSender(Context context, String token) {
        this.context = context;
        this.token = token;

    }


    public void sendEvent(String streamName, String data){
        openReport(context)
                .setEnpoint(endpoint)
                .setTable(streamName)
                .setHttpMethod(HttpMethod.POST)
                .setToken(token)
                .setData(data)
                .send();

    }

    public void sendEvent(String streamName, String data, HttpMethod httpMethod){
        openReport(context)
                .setEnpoint(endpoint)
                .setTable(streamName)
                .setHttpMethod(httpMethod)
                .setToken(token)
                .setData(data)
                .send();
    }

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

