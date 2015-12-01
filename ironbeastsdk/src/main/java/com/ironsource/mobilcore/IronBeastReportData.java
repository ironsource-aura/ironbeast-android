package com.ironsource.mobilcore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

class IronBeastReportData {


    public static final String TAG = IronBeastReportData.class.getSimpleName();

    /**
     * ******** public methods ***********
     */

    public IronBeastReportData() {
        Logger.log("in reporter", Logger.SDK_DEBUG);
    }

    public static IronBeastReportIntent openReport(Context context, int sdkEvent) {
        return new IronBeastReportIntent(context, sdkEvent);
    }

    public static void doScheduledSend() {

    }

    public synchronized void doReport(Context context, Intent intent) {
        Logger.log("doReport --->", Logger.SDK_DEBUG);
        try {
            if (intent.getExtras() != null) {
                int event = intent.getIntExtra(IronBeastReportIntent.EXTRA_REPORT_TYPE, SdkEvent.ERROR);
                ;
                // We want to send report
                // immediately: IRON_BEAST_REPORT isBulk = false
                // pending : ERROR, IRON_BEAST_REPORT isBulk = true

                if (event == SdkEvent.FLUSH_QUEUE) {
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

                    if (event == SdkEvent.ENQUEUE) {
                        //TODO: add something

                    } else if (event == SdkEvent.ERROR) {
                    }

                    String tableName = (String) dataObject.remove(IronBeastReportIntent.TABLE);
                    String auth = (String) dataObject.remove(IronBeastReportIntent.TOKEN);
                    boolean isBulk = (Boolean) dataObject.remove(IronBeastReportIntent.BULK);
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
}
