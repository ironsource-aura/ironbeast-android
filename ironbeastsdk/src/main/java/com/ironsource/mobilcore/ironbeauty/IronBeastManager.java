package com.ironsource.mobilcore.ironbeauty;

import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

class IronBeastManager {

    private static IronBeastManager sInstance;
    private String mCustomHostname;
    private Map<IB_REPORT_TYPE, String> mTableMap = new HashMap<>();

    private IronBeastManager(Map<IB_REPORT_TYPE, String> tableMap) {
        mTableMap = tableMap;
    }

    public static synchronized IronBeastManager getInstance() {
        if (sInstance == null) {
            // default report type -to- table name map
            Map<IB_REPORT_TYPE, String> tableMap = new HashMap<>();
//            tableMap.put(IB_REPORT_TYPE.ORGANIC_IMPRESSIONS, IBConsts.IRON_BEAST_DEFAULT_IMPRESSIONS_TABLE);
//            tableMap.put(IB_REPORT_TYPE.ORGANIC_CLICKS, IBConsts.IRON_BEAST_DEFAULT_CLICKS_TABLE);
//            tableMap.put(IB_REPORT_TYPE.SESSION_DATA, IBConsts.IRON_BEAST_DEFAULT_SESSION_DATA_TABLE);
//
            sInstance = new IronBeastManager(tableMap);
        }
        return sInstance;
    }

    /**
     * This method sets the custom hostname for all reporting
     *
     * @param customHostname - New custom hostname.
     */
    public void setCustomHostname(String customHostname) {
        this.mCustomHostname = customHostname;
    }

    private String getHostname() {
        return mCustomHostname;
    }

    /**
     * This method sets the custom impressions table name
     *
     * @param reportType      - IronBeast report type.
     * @param customTableName - IronBeast custom table name.
     */
    public void setCustomTableName(IB_REPORT_TYPE reportType, String customTableName) {
        mTableMap.put(reportType, customTableName);
    }

    // get an IronBeast tablename for the corresponding report type
    private String getTableNameByReportType(IB_REPORT_TYPE reportType) {
        return mTableMap.get(reportType);
    }

    /**
     * This method performs data sending to IronBeast API
     *
     * @param reportType - Report type.
     * @param dataObject - JSON string, containing the data.
     * @param isBulk     - Set this to true if sending a batch of data-objects.
     */
    public void sendData(IB_REPORT_TYPE reportType, String dataObject, boolean isBulk) {
        sendData(reportType, dataObject, null, isBulk);
    }


    /**
     * This method performs data sending to IronBeast API
     *
     * @param reportType - Report type.
     * @param dataObject - JSON string, containing the data.
     * @param auth       - optional auth parameter.
     * @param isBulk     - Set this to true if sending a batch of data-objects.
     */
    public void sendData(IB_REPORT_TYPE reportType, String dataObject, String auth, boolean isBulk) {
        Log.d("AAAA", "IronBeastManager | sendData | reportType=" + reportType + " | dataObject=" + dataObject +
                " | auth=" + auth + " | isBulk=" + isBulk);
        if (TextUtils.isEmpty(dataObject)) {
            return;
        }

        try {
            String ironBeastBody = "";//createIronBeastRequestBody(getTableNameByReportType(reportType), dataObject, auth, isBulk);

            Log.d("AAAA", "IronBeastManager | sendData | ironBeastBody=" + ironBeastBody);

            // result observer
            IBHttpObserver observer = new IBHttpObserver() {
                @Override
                public void onFinish(String responseString) {
                    Log.d("AAAA", "IronBeastManager | sendData | observer onFinish responseString=" + responseString);
                }

                @Override
                public void onError() {
                    Log.d("AAAA", "IronBeastManager | sendData | observer onError");
                }
            };

            // url depends on the hostname and isBulk param
            String url = getHostname();

            Log.d("AAAA", "IronBeastManager | sendData | url=" + url);

            // create and execute the task
            IBHttpTask task = new IBHttpTask.HttpTaskBuilder(url, observer)
                    .jsonBody(ironBeastBody)
                    .build();
            task.executeTask();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum IB_REPORT_TYPE {
        ORGANIC_IMPRESSIONS,
        ORGANIC_CLICKS,
        SESSION_DATA
    }
}
