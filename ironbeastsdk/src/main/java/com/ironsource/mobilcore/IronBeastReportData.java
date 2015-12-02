package com.ironsource.mobilcore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

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
        Debug.waitForDebugger();
        Logger.log("doReport --->", Logger.SDK_DEBUG);
        try {
            if (intent.getExtras() != null) {
                int event = intent.getIntExtra(IronBeastReportIntent.EXTRA_REPORT_TYPE, SdkEvent.ERROR);
                Logger.log("doReport event --> " + event, Logger.SDK_DEBUG);

                // We want to send report
                // immediately: IRON_BEAST_REPORT isBulk = false
                // pending : ERROR, IRON_BEAST_REPORT isBulk = true

                Bundle bundle = intent.getExtras();
                JSONObject dataObject = new JSONObject();
                try {
                    for (String key : bundle.keySet()) {
                        Object value = bundle.get(key);
                        dataObject.put(key, value);
                    }
                } catch (Exception ex) {
                    //TODO: add sending report
                    Logger.log("Failed to fetch data from Intent", Logger.SDK_DEBUG);
                }

                String table = (String) dataObject.remove(IronBeastReportIntent.TABLE);
                String token = (String) dataObject.remove(IronBeastReportIntent.TOKEN);
                boolean isBulk = (Boolean) dataObject.remove(IronBeastReportIntent.BULK);

                if (event == SdkEvent.FLUSH_QUEUE) {
                    //TODO: flush STORAGE component
                    isBulk = true;
                } else if (event == SdkEvent.ENQUEUE) {
                    //TODO: add something
                    isBulk = false;
                } else if (event == SdkEvent.POST_SYNC) {

                } else if (event == SdkEvent.ERROR) {
                    //TODO: create ERROR report
                    isBulk = true;
                }

                if (isBulk) {
                    //QUEUE
                    //TODO: create message for ironBeast
                    //TODO: encrypt data
                    //TODO: send data to storage or network
                    if (IBConfig.getsInstance().getBulkSize() > FsQueue.getInstance(table, context).count()) {
                        String[] records = FsQueue.getInstance(table, context).peek();
                        //Prepare to send fill json array with data
                        JSONArray array = new JSONArray();
                        for (String record : records) {
                            array.put(record);
                        }

                        String data = array.toString();
                        String message = createMessage(table, token, data);
                        String encryptedData = Utils.auth(message, token);

                        SEND_RESULT sendResult = sendData(context, encryptedData, IBConfig.getsInstance().getIBEndPoint());
                        Logger.log("Send result --> " + sendResult.toString(), Logger.SDK_DEBUG);
                        if (sendResult == SEND_RESULT.FAILED_DELETE) {
                            Logger.log("Remove report file --> " + sendResult.toString(), Logger.SDK_DEBUG);
                            FsQueue.getInstance(table, context).clear();
                            //TODO: send error report delete reason
                        } else if (sendResult == SEND_RESULT.FAILED_RESEND_LATER) {
                            int currNumOfRetry = intent.getExtras().getInt("nRetries");
                            int idleSeconds = IBConfig.getsInstance().getIdleSeconds();
                            Logger.log("Resubmit report --> " + currNumOfRetry, Logger.SDK_DEBUG);
                            //data was save so we need just flush the queue
                            intent.putExtra(IronBeastReportIntent.EXTRA_REPORT_TYPE, SdkEvent.FLUSH_QUEUE);
                            //resubmit last intent
                            resubmitReport(context, intent, currNumOfRetry, idleSeconds);
                        } else if (sendResult == SEND_RESULT.SUCCESS) {
                            Logger.log("Remove report file --> " + sendResult.toString(), Logger.SDK_DEBUG);
                            FsQueue.getInstance(table, context).clear();
                        }
                    }

                } else {
                    //TODO: create message for ironBeast
                    //TODO: encrypt data
                    //TODO: send data to storage or network
                    String data = dataObject.getString(IronBeastReportIntent.DATA);
                    String message = createMessage(table, token, data);
                    String encryptedData = Utils.auth(message, token);

                    SEND_RESULT sendResult = sendData(context, encryptedData, IBConfig.getsInstance().getIBEndPoint());
                    Logger.log("Send result --> " + sendResult.toString(), Logger.SDK_DEBUG);
                    if (sendResult == SEND_RESULT.FAILED_RESEND_LATER) {
                        int currNumOfRetry = intent.getExtras().getInt("nRetries");
                        int idleSeconds = IBConfig.getsInstance().getIdleSeconds();
                        Logger.log("Resubmit report --> " + currNumOfRetry, Logger.SDK_DEBUG);
                        boolean resubmitResult = resubmitReport(context, intent, currNumOfRetry, idleSeconds);
                        if (!resubmitResult) {
                            Logger.log("On send failed save report --> ", Logger.SDK_DEBUG);
                            FsQueue.getInstance(table, context).push(data);
                        }
                    }
                }

            }
        } catch (Exception ex) {
            //TODO: may be send error
        }
    }

    String createMessage(String table, String token, String data) {
        String message = "";
        try {
            JSONObject event = new JSONObject();
            event.put("table", table);
            event.put("token", token);
            event.put("data", data);
        } catch (JSONException e) {
            // Log "failed to track your event ${e}"
            e.printStackTrace();
        }
        return message;
    }

    protected SEND_RESULT sendData(Context context, String encryptedData, String ibEndPoint) {
        SEND_RESULT sendResult = SEND_RESULT.FAILED_RESEND_LATER;

        int nRetry = IBConfig.getsInstance().getNumOfRetries();
        RemoteService poster = HttpService.getInstance();
        if (poster.isOnline(context)) {
            try {
                RemoteService.Response response = poster.post(encryptedData, ibEndPoint);
                if (response.code == HttpURLConnection.HTTP_OK) {
                    sendResult = SEND_RESULT.SUCCESS;
                }
                //TODO: Check ״internal error״ from what android version
                if (response.code >= HttpURLConnection.HTTP_BAD_REQUEST && response.code < HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    sendResult = SEND_RESULT.FAILED_DELETE;
                } else if (response.code >= HttpURLConnection.HTTP_INTERNAL_ERROR && response.code < HttpURLConnection.HTTP_VERSION) {
                    sendResult = SEND_RESULT.FAILED_RESEND_LATER;
                }
                // TODO: Handle 40X, 50x status situations
            } catch (IOException e) {
                Logger.log("Failed to Post to Ironbeast", Logger.SDK_DEBUG);
            }
        }

        return sendResult;
    }

    boolean resubmitReport(Context context, Intent intent, int currNumOfRetry, long delay) {
        boolean result = false;
        if (0 <= currNumOfRetry && currNumOfRetry < IBConfig.getsInstance().getNumOfRetries()) {
            intent.getExtras().putInt("nRetries", currNumOfRetry++);
            Utils.scheduleSendReportsAction(context, intent, delay);
            result = true;
        }
        return result;
    }

    enum SEND_RESULT {
        SUCCESS, FAILED_DELETE, FAILED_RESEND_LATER
    }
    private static final int SEND_RESU_EVENTS = 1; // Put message in the QStorage
    private static final int FLUSH_QUEUE = 2;    // Flush QStorage
    private static final int POST_SYNC = 3;      // Post message sync
}
