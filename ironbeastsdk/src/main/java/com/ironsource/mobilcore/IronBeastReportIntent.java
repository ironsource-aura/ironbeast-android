package com.ironsource.mobilcore;

import android.content.Context;
import android.content.Intent;

import com.ironsource.mobilcore.Consts.EServiceType;
import com.ironsource.mobilcore.ReportingConsts.EReportType;

import java.util.Set;

class IronBeastReportIntent extends Intent {

    private Context mCtx;

    public IronBeastReportIntent(Context context, EReportType type) {
        super(context, MobileCoreReport.class);
        mCtx = context;

        EServiceType.SERVICE_TYPE_REPORT.setValue(Consts.EXTRA_SERVICE_TYPE, this);
        putExtra(ReportingConsts.EXTRA_REPORT_TYPE, type.ordinal());
        putExtra(ReportingConsts.EXTRA_TOKEN, IronBeast.getToken(context));
        putExtra(ReportingConsts.EXTRA_UNIQUE_ID, MCUniqueIDHelper.getInstance(context).getUniqueID());
        putExtra(ReportingConsts.EXTRA_UNIQUE_ID_TYPE, MCUniqueIDHelper.getInstance(context).getUniqueIDTypeSimplified());
        putExtra(ReportingConsts.EXTRA_UNIQUE_ID_MC_ID, MCUniqueIDHelper.getInstance(context).getMCId());
        putExtra(ReportingConsts.EXTRA_UNIQUE_ID_GAID, MCUniqueIDHelper.getInstance(context).getGaid());
        putExtra(ReportingConsts.EXTRA_IS_LIMIT_AD_TRACKING_ENABLED, MCUniqueIDHelper.getInstance(context).isLimitAdTrackingEnabled());
    }

    public IronBeastReportIntent setError(Exception e) {
        putExtra(ReportingConsts.EXTRA_EXCEPTION, MCUtils.formatExceptionMsg(e, getCallerClassString()));
        return this;
    }

    public IronBeastReportIntent setError(Exception e, String errorMessage) {
        putExtra(ReportingConsts.EXTRA_EXCEPTION, MCUtils.formatExceptionMsg(e, getCallerClassString()) + " ### " + errorMessage);
        return this;
    }

    public IronBeastReportIntent setError(String errorMsg) {
        putExtra(ReportingConsts.EXTRA_EXCEPTION, getCallerClassString() + " ### " + errorMsg);
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

    public IronBeastReportIntent setReport(IronBeastReport report) {
        Set<String> keys = report.keySet();
        for (String key : keys) {
            this.putExtra(key, report.get(key));
        }
        return this;
    }

    public IronBeastReportIntent setAuth(String auth) {
        putExtra(IronBeastReport.AUTH, auth);
        return this;
    }

    public IronBeastReportIntent setBulk(boolean isBulk) {
        putExtra(IronBeastReport.BULK, isBulk);
        return this;
    }
}