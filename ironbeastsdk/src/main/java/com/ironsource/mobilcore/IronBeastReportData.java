package com.ironsource.mobilcore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.ironsource.mobilcore.ReportingConsts.EReportType;

import org.json.JSONObject;

class IronBeastReportData {

    public static final String TAG = IronBeastReportData.class.getSimpleName();

    /**
     * ******** public methods ***********
     */

    public IronBeastReportData() {
        Logger.log("in reporter", Logger.SDK_DEBUG);
    }

    public static IronBeastReportIntent openReport(Context context, EReportType type) {
        return new IronBeastReportIntent(context, type);
    }

    public static void doScheduledSend() {

    }

    public synchronized void doReport(Context context, Intent intent) {
        Logger.log("doReport --->", Logger.SDK_DEBUG);
        try {
            if (intent.getExtras() != null) {
                EReportType type = fetchReportType(intent);
                // We want to send report
                // immediately: IRON_BEAST_REPORT isBulk = false
                // pending : ERROR, IRON_BEAST_REPORT isBulk = true

                if (type.compareTo(EReportType.REPORT_TYPE_UPDATE_CONFIG) == 0) {
                    String batchSize = getIntentString(intent, Consts.PREFS_MAX_BATCH_SIZE, "");
                    if (!TextUtils.isEmpty(batchSize)) {
                        MCUtils.saveConfig(context, Consts.PREFS_MAX_BATCH_SIZE, batchSize);
                        //TODO: update STORAGE component with new size
                    }
                } else if (type.compareTo(EReportType.REPORT_TYPE_FLUSH) == 0) {
                    //TODO: flush STORAGE component

                } else {
                    //Extract all data from intent extras
                    Bundle bundle = intent.getExtras();
                    JSONObject dataObject = new JSONObject();
                    try {
                        for (String key : bundle.keySet()) {
                            Object value = bundle.get(key);
                            dataObject.put(key, value);
                        }
                    } catch (Exception ex) {
                        //TODO: add sending report
                    }

                    if (type.compareTo(EReportType.REPORT_TYPE_IRON_BEAST) == 0) {
                        //TODO: add something

                    } else if (type.compareTo(EReportType.REPORT_TYPE_ERROR) == 0) {
                        appendMoreDataToErrorReport(context, intent, dataObject);
                    }

                    String tableName = (String) dataObject.remove(IronBeastReport.TABLE_NAME);
                    String auth = (String) dataObject.remove(IronBeastReport.AUTH);
                    boolean isBulk = (Boolean) dataObject.remove(IronBeastReport.BULK);
                    boolean isNetworkAvail = NetworkUtils.isNetworkAvail(context);
                    if (isBulk) {
                        //QUEUE
                        //TODO: create message for ironBeast
                        //TODO: encrypt data
                        //TODO: send data to storage or network
                    } else {
                        //TODO: create message for ironBeast
                        //TODO: encrypt data
                        //TODO: send data to storage or network
                    }

                }
            }
        } catch (Exception ex) {
            //TODO: may be send error
        }
    }

    private JSONObject appendMoreDataToErrorReport(Context context, Intent intent, JSONObject report) {
        try {
            report.put(ReportingConsts.REPORT_FIELD_TIMESTAMP, MCUtils.getCurrentTime());
            report.put(ReportingConsts.REPORT_FIELD_IRVER, Consts.VER);
            report.put(ReportingConsts.REPORT_FIELD_PLATFORM, ReportingConsts.REPORT_OS_ANDROID);
            report.put(ReportingConsts.REPORT_FIELD_RV, ReportingConsts.REPORT_VERSION);
            report.put(ReportingConsts.REPORT_FIELD_TOKEN, getIntentString(intent, ReportingConsts.EXTRA_TOKEN, IronBeast.getToken()));
            report.put(ReportingConsts.REPORT_FIELD_CARRIER, context.getPackageName());
            report.put(ReportingConsts.REPORT_FIELD_CARRIER_VER, MCUtils.getCarrierVersion(context));
            report.put(ReportingConsts.REPORT_FIELD_CUR_CONN, MCUtils.interpretConnection(MCUtils.getCurrentConnection(context)));

            setIfNotNull(report, ReportingConsts.REPORT_FIELD_BV, ExternalVars.REPLACABLE_BAMBOO_VER);

            report.put(ReportingConsts.REPORT_FIELD_OS, android.os.Build.VERSION.SDK_INT);
            String shortErr = MCUtils.getShortenedString(intent.getStringExtra(ReportingConsts.EXTRA_EXCEPTION), Consts.REPORT_MAX_ERR_FIELD_LENGTH);
            setIfNotNull(report, ReportingConsts.REPORT_FIELD_ERR, shortErr);
            report.put(IronBeastReport.BULK, true);
        } catch (Exception ex) {
            //TODO: send or print
        }
        return report;
    }

    private String getIntentString(Intent intent, String field, String defVal) {
        String val = intent.getStringExtra(field);
        if (TextUtils.isEmpty(val)) {
            return defVal;
        } else {
            return val;
        }
    }

    private void setIfNotNull(JSONObject report, String field, Object val) {
        if (val != null) {
            try {
                report.put(field, val);
            } catch (Exception e) {
                IronBeastReportData.openReport(IronBeast.getAppContext(), EReportType.REPORT_TYPE_ERROR).setError(e).send();
            }
        }
    }

    private EReportType fetchReportType(Intent intent) {
        EReportType type = EReportType.REPORT_TYPE_ERROR;
        try {
            type = EReportType.parseString(intent.getIntExtra(ReportingConsts.EXTRA_REPORT_TYPE, -1));
        } catch (Exception e) {
            IronBeastReportData.openReport(IronBeast.getAppContext(), EReportType.REPORT_TYPE_ERROR).setError(e).send();
        }
        return type;
    }
}
