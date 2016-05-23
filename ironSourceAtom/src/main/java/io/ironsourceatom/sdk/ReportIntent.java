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
    protected static final String EXTRA_SDK_EVENT = "sdk_event";


    ReportIntent(Context context, int sdkEvent) {
        mContext = context;
        mIntent = new Intent(context, ReportService.class);
        mIntent.putExtra(EXTRA_SDK_EVENT, sdkEvent);
    }

    public void send() { mContext.startService(mIntent); }

    public ReportIntent setToken(String token) {
        mIntent.putExtra(TOKEN, token);
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