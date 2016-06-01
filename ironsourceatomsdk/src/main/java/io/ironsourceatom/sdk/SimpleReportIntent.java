package io.ironsourceatom.sdk;

import android.content.Context;
import android.content.Intent;

/**
 * Implementation of Report for using with SimpleReportService
 */

class SimpleReportIntent implements Report {


    private Context mContext;
    private Intent mIntent;

    public static final String TABLE = "table";
    public static final String TOKEN = "token";
    public static final String BULK = "bulk";
    public static final String DATA = "data";
    public static final String AUTH = "auth";
    public static final String ENDPOINT = "endpoint";


    SimpleReportIntent(Context context) {
        mContext = context;
        mIntent = new Intent(context, SimpleReportService.class);

    }

    public void send() {
        mContext.startService(mIntent);
    }

    public SimpleReportIntent setToken(String token) {
        mIntent.putExtra(TOKEN, token);
        return this;
    }

    @Override
    public SimpleReportIntent setEnpoint(String endpoint) {
        mIntent.putExtra(ENDPOINT, endpoint);
        return this;
    }


    @Override
    public Report setBulk(boolean b) {
        mIntent.putExtra(BULK, String.valueOf(b));
        return this;
    }

    public SimpleReportIntent setTable(String table) {
        mIntent.putExtra(TABLE, table);
        return this;
    }

    public SimpleReportIntent setData(String value) {
        mIntent.putExtra(DATA, value);
        return this;
    }

    public Intent getIntent() { return mIntent; }

}
