package io.ironsourceatom.sdk;

import android.content.Context;
import android.content.Intent;

/**
 * Implementation of Report for using with SimpleReportService
 */

class SimpleReportIntent implements Report {


    private Context context;
    private Intent intent;

    public static final String TABLE = "table";
    public static final String TOKEN = "token";
    public static final String BULK = "bulk";
    public static final String DATA = "data";
    public static final String AUTH = "auth";
    public static final String ENDPOINT = "endpoint";


    SimpleReportIntent(Context context) {
        this.context = context;
        intent = new Intent(context, SimpleReportService.class);

    }

    public void send() {
        context.startService(intent);
    }

    public SimpleReportIntent setToken(String token) {
        intent.putExtra(TOKEN, token);
        return this;
    }

    @Override
    public SimpleReportIntent setEnpoint(String endpoint) {
        intent.putExtra(ENDPOINT, endpoint);
        return this;
    }


    @Override
    public Report setBulk(boolean b) {
        intent.putExtra(BULK, String.valueOf(b));
        return this;
    }

    public SimpleReportIntent setTable(String table) {
        intent.putExtra(TABLE, table);
        return this;
    }

    public SimpleReportIntent setData(String value) {
        intent.putExtra(DATA, value);
        return this;
    }

    public Intent getIntent() { return intent; }

}
