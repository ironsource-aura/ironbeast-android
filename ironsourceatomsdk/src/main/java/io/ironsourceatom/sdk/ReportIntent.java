package io.ironsourceatom.sdk;

import android.content.Context;
import android.content.Intent;

class ReportIntent implements Report {


    private Context mContext;
    private Intent mIntent;

    public static final String TABLE = "table";
    public static final String TOKEN = "token";
    public static final String BULK = "bulk";
    public static final String DATA = "data";
    public static final String AUTH = "auth";
    public static final String ENDPOINT = "endpoint";
    protected static final String EXTRA_SDK_EVENT = "sdk_event";
    protected static final String HTTPMETHOD = "httpMethod";

    ReportIntent(Context context, int sdkEvent) {
        mContext = context;
        mIntent = new Intent(context, ReportService.class);
        mIntent.putExtra(EXTRA_SDK_EVENT, sdkEvent);
    }

    ReportIntent(Context context) {
        mContext = context;
        mIntent = new Intent(context, ReportService.class);

    }

    public void send() {
        mContext.startService(mIntent);
    }

    public ReportIntent setToken(String token) {
        mIntent.putExtra(TOKEN, token);
        return this;
    }

    @Override
    public Report setEnpoint(String endpoint) {
        mIntent.putExtra(ENDPOINT, endpoint);
        return this;
    }

    @Override
    public Report setBulk(boolean b) {
        mIntent.putExtra(BULK, String.valueOf(b));
        return this;
    }

    public ReportIntent setTable(String table) {
        mIntent.putExtra(TABLE, table);
        return this;
    }

    public ReportIntent setData(String value) {
        mIntent.putExtra(DATA, value);
        return this;
    }

    public Intent getIntent() { return mIntent; }

}