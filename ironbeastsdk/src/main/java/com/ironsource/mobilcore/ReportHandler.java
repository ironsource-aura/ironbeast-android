package com.ironsource.mobilcore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static java.lang.Math.*;

public class ReportHandler {

    public ReportHandler() {
        mQueue = null;
        Logger.log("in reporter", Logger.SDK_DEBUG);
    }

    public synchronized boolean handleReport(Context context, Intent intent) {
        boolean success = true;
        Logger.log("doReport --->", Logger.SDK_DEBUG);
        if (null == mQueue) {
            mQueue = getQueue(mConfig.getRecordsFile(), context);
        }
        try {
            if (null == intent.getExtras()) return success;
            int event = intent.getIntExtra(ReportIntent.EXTRA_REPORT_TYPE, SdkEvent.ERROR);
            Bundle bundle = intent.getExtras();
            JSONObject dataObject = new JSONObject();
            try {
                String[] fields = {ReportIntent.TABLE, ReportIntent.TOKEN, ReportIntent.DATA};
                for (String key : fields) {
                    Object value = bundle.get(key);
                    dataObject.put(key, value);
                }
            } catch (Exception ex) {
                //TODO: add sending report
                Logger.log("Failed to fetch data from Intent", Logger.SDK_DEBUG);
            }

            boolean toFlush = event == SdkEvent.FLUSH_QUEUE;
            if (event == SdkEvent.ENQUEUE) {
                toFlush = mConfig.getBulkSize() <= mQueue.push(dataObject.toString());
            } else if (event == SdkEvent.POST_SYNC) {
                String message = createMessage(dataObject, false);
                SEND_RESULT sendResult = sendData(context, message, mConfig.getIBEndPoint());
                // If message failed, push it to queue.
                if (sendResult == SEND_RESULT.FAILED_RESEND_LATER) {
                    mQueue.push(dataObject.toString());
                    success = false;
                }
            } else if (event == SdkEvent.ERROR) {
                //TODO: create ERROR report
            }
            if (toFlush) {
                // map all records according to 'table' field
                // i.e: { table1: [...], table2: [...] }
                Map<String, List<JSONObject>> reqMap = new HashMap<String, List<JSONObject>>();
                String[] records = mQueue.peek();
                // failures list(not-acknowledge)
                List<String> nacks = new ArrayList<>();
                for (String record : records) {
                    try {
                        JSONObject obj = new JSONObject(record);
                        String tName = (String) obj.get(ReportIntent.TABLE);
                        if (!reqMap.containsKey(tName)) {
                            reqMap.put(tName, new ArrayList<JSONObject>());
                        }
                        reqMap.get(tName).add(obj);
                    } catch (JSONException e) {
                        Logger.log("Failed to generate the recordsMap", Logger.SDK_DEBUG);
                    }
                }
                // Loop over map, and call send() for each one of entries
                for (Map.Entry<String, List<JSONObject>> pEntry : reqMap.entrySet()) {
                    // Split each entry-value into list of entries based on byteSizeLimit
                    // or batchSizeLimit
                    // then, send each of the subList entries separately
                    for (List<JSONObject> entry : split(pEntry.getValue())) {
                        JSONObject dataObj = new JSONObject();
                        try {
                            dataObj.put(ReportIntent.TABLE, pEntry.getKey());
                            JSONArray bulk = new JSONArray();
                            for (JSONObject record : entry) {
                                if (!dataObj.has(ReportIntent.TOKEN)) {
                                    dataObj.put(ReportIntent.TOKEN, record.get(ReportIntent.TOKEN));
                                }
                                // Put only the `data` field
                                bulk.put(record.getString(ReportIntent.DATA));
                            }
                            // `bulk` contains all `data` fields of all objects in the current destination
                            dataObj.put(ReportIntent.DATA, bulk.toString());
                        } catch (JSONException e) {
                            Logger.log("Failed to generate the dataObj to send()", Logger.SDK_DEBUG);
                        }
                        // Send each destination/table separately
                        String message = createMessage(dataObj, true);
                        SEND_RESULT sendResult = sendData(context, message, mConfig.getIBEndPoint());
                        // sign-it if this bulk was failed
                        if (sendResult == SEND_RESULT.FAILED_RESEND_LATER) {
                            success = false;
                            for (JSONObject record : entry) nacks.add(record.toString());
                        }
                    }
                }
                // Clear queue and put back all "infected/failed"
                mQueue.clear();
                mQueue.push(nacks.toArray(new String[nacks.size()]));
            } // end flush
        } catch (Exception e) {
            Logger.log("Failed parse the given report:" + e.getMessage(), Logger.SDK_DEBUG);
            //TODO: may be send error
        }
        return success;
    }

    // Split batch of JSONObjects into chunks based on byteSizeLimit || batchSizeLimit
    private List<List<JSONObject>> split(List<JSONObject> batch) {
        List<List<JSONObject>> chunks = new ArrayList<>();
        int byteSize;
        try {
            byteSize = batch.toString().getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            byteSize = batch.toString().length();
        }
        int nChunks = (int) ceil(max(
                (double) batch.size() / mConfig.getBulkSize(),
                (double) byteSize / mConfig.getMaximumRequestLimit()
        ));
        for (int i = 0; i < min(nChunks, batch.size()); i++) {
            int range = (int) ceil((double)batch.size() / nChunks);
            chunks.add(batch.subList(range * i, min(range + range * i, batch.size())));
        }
        return chunks;
    }

    private String createMessage(JSONObject dataObj, boolean bulk) {
        String message = "";
        try {
            JSONObject clone = new JSONObject(dataObj.toString());
            String data = clone.getString(ReportIntent.DATA);
            clone.put(ReportIntent.AUTH,
                    Utils.auth(data, (String) clone.remove(ReportIntent.TOKEN)));
            if (bulk) {
                clone.put(ReportIntent.BULK, true);
            }
            message = clone.toString();
        } catch (Exception e) {
            Logger.log("ReportHandler: failed create message" + e.getMessage(), Logger.SDK_DEBUG);
        }
        return message;
    }

    protected SEND_RESULT sendData(Context context, String data, String ibEndPoint) {
        SEND_RESULT sendResult = SEND_RESULT.FAILED_RESEND_LATER;
        int nRetry = mConfig.getNumOfRetries();
        RemoteService poster = getPoster();
        while (nRetry-- > 0) {
            if (poster.isOnline(context)) {
                try {
                    RemoteService.Response response = poster.post(data, ibEndPoint);
                    if (response.code == HttpURLConnection.HTTP_OK) {
                        sendResult = SEND_RESULT.SUCCESS;
                        break;
                    }
                    if (response.code >= HttpURLConnection.HTTP_BAD_REQUEST &&
                            response.code < HttpURLConnection.HTTP_INTERNAL_ERROR) {
                        sendResult = SEND_RESULT.FAILED_DELETE;
                        break;
                    }
                    //TODO: Check ״internal error״ from what android version
                } catch (IOException e) {
                    Logger.log("Failed to Post to Ironbeast", Logger.SDK_DEBUG);
                }
            }
            try {
                TimeUnit.SECONDS.sleep(mConfig.getIdleSeconds());
            } catch (InterruptedException e) {
                Logger.log("Failed to sleep between retries", Logger.SDK_DEBUG);
            }
        }
        return sendResult;
    }

    ////////////////////////////////////////
    // For testing, to allow for mocking ///
    ////////////////////////////////////////
    protected RemoteService getPoster() {
        return HttpService.getInstance();
    }
    protected StorageService getQueue(String filename, Context context) {
        return FsQueue.getInstance(filename, context);
    }

    enum SEND_RESULT {
        SUCCESS, FAILED_DELETE, FAILED_RESEND_LATER
    }

    private IBConfig mConfig;
    private StorageService mQueue;
}
