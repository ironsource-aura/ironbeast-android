package io.ironsourceatom.sdk;

import android.content.Context;
import android.content.Intent;

class ReportIntent implements Report {


    private Context context;
    private Intent intent;

    public static final String TABLE = "table";
    public static final String TOKEN = "token";
    public static final String BULK = "bulk";
    public static final String DATA = "data";
    public static final String AUTH = "auth";
    public static final String ENDPOINT = "endpoint";
    protected static final String EXTRA_SDK_EVENT = "sdk_event";

    ReportIntent(Context context, int sdkEvent) {
        this.context = context;
        intent = new Intent(context, ReportService.class);
        intent.putExtra(EXTRA_SDK_EVENT, sdkEvent);
    }

    ReportIntent(Context context) {
        this.context = context;
        intent = new Intent(context, ReportService.class);

    }

    public void send() {
        context.startService(intent);
    }

    public ReportIntent setToken(String token) {
        intent.putExtra(TOKEN, token);
        return this;
    }

    @Override
    public Report setEnpoint(String endpoint) {
        intent.putExtra(ENDPOINT, endpoint);
        return this;
    }

    @Override
    public Report setBulk(boolean b) {
        intent.putExtra(BULK, String.valueOf(b));
        return this;
    }

    public ReportIntent setTable(String table) {
        intent.putExtra(TABLE, table);
        return this;
    }

    public ReportIntent setData(String value) {
        intent.putExtra(DATA, value);
        return this;
    }

    public Intent getIntent() { return intent; }

}