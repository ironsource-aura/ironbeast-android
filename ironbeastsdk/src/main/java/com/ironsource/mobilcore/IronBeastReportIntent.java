package com.ironsource.mobilcore;

import android.content.Context;
import android.content.Intent;

import com.ironsource.mobilcore.Consts.EServiceType;

class IronBeastReportIntent extends Intent {
    public static final String TABLE = "table";
    public static final String TOKEN = "token";
    public static final String BULK = "bulk";
    public static final String DATA = "data";
    public static final String AUTH = "auth";
    protected static final String EXTRA_REPORT_TYPE = "report_type";
    protected static final String EXTRA_EXCEPTION = "exception";
    private Context mCtx;

    public IronBeastReportIntent(Context context, int sdkEvent) {
        super(context, IronBeastReportService.class);
        mCtx = context;

        EServiceType.SERVICE_TYPE_REPORT.setValue(Consts.EXTRA_SERVICE_TYPE, this);
        putExtra(EXTRA_REPORT_TYPE, sdkEvent);
    }

    public IronBeastReportIntent setError(String errorMsg) {
        putExtra(EXTRA_EXCEPTION, getCallerClassString() + " ### " + errorMsg);
        return this;
    }

    private String getCallerClassString() {
        StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
        String callerClass = traceElements[1].getClassName(); // default value;
        try {
            for (StackTraceElement e : traceElements) {
                String className = e.getClassName();
                // grab the first class with our package name, but not the MobileCoreReportIntent
                if (className != null && className.contains(this.getClass().getPackage().getName()) && !className.equals(this.getClass().getName())) {
                    callerClass = "class: " + e.getClassName() + " ### method: " + e.getMethodName() + " ### line: " + e.getLineNumber();
                    break;
                }
            }
        } catch (Exception e) {
            // pass
        }
        return callerClass;
    }

    public void send() {
        mCtx.startService(this);
    }

    public IronBeastReportIntent setToken(String token) {
        putExtra(TOKEN, token);
        return this;
    }

    public IronBeastReportIntent setTable(String table) {
        putExtra(TABLE, table);
        return this;
    }

    public IronBeastReportIntent setData(String key, String value) {
        putExtra(key, value);
        return this;
    }

    public IronBeastReportIntent setData(String value) {
        setData(DATA, value);
        return this;
    }

}