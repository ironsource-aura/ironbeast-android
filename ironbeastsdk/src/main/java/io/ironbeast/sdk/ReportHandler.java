package io.ironbeast.sdk;

import io.ironbeast.sdk.StorageService.*;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static java.lang.Math.*;

public class ReportHandler {

    public ReportHandler(Context context) {
        mContext = context;
        mConfig = IBConfig.getInstance(context);
        mStorage = getStorage(context);
    }

    public synchronized boolean handleReport(Intent intent) {
        boolean success = true;
        try {
            if (null == intent.getExtras()) return success;
            int event = intent.getIntExtra(ReportIntent.EXTRA_SDK_EVENT, SdkEvent.ERROR);
            Bundle bundle = intent.getExtras();
            JSONObject dataObject = new JSONObject();
            try {
                String[] fields = {ReportIntent.TABLE, ReportIntent.TOKEN, ReportIntent.DATA};
                for (String key : fields) {
                    Object value = bundle.get(key);
                    dataObject.put(key, value);
                }
            } catch (Exception e) {
                Logger.log(TAG, "Failed extracting the data from Intent", Logger.SDK_DEBUG);
            }
            List<Table> tablesToFlush = new ArrayList<>();
            switch (event) {
                case SdkEvent.FLUSH_QUEUE:
                    tablesToFlush = mStorage.getTables();
                    break;
                case SdkEvent.POST_SYNC:
                    String message = createMessage(dataObject, false);
                    SEND_RESULT res = sendData(message, mConfig.getIBEndPoint());
                    if (success = (res != SEND_RESULT.FAILED_RESEND_LATER)) break;
                case SdkEvent.ENQUEUE:
                    Table table = new Table(dataObject.getString(ReportIntent.TABLE),
                            dataObject.getString(ReportIntent.TOKEN));
                    if (mConfig.getBulkSize() <= mStorage.addEvent(table, dataObject.getString(ReportIntent.DATA))) {
                        tablesToFlush.add(table);
                    }
            }
            // If there's something to flush, it'll no be empty.
            for (Table table: tablesToFlush) {
                try {
                    flush(table);
                } catch (Exception e) {
                    Logger.log(e.getMessage(), Logger.SDK_DEBUG);
                    success = false;
                    break;
                }
            }
        } catch (Exception e) {
            Logger.log("Failed parse the given report:" + e, Logger.SDK_DEBUG);
        }
        return success;
    }

    /**
     * First, we peek the batch the fits with the `MaximumRequestLimit`
     * after that we prepare the request and send it.
     * if the send failed, we stop here, and "continue later".
     * if everything goes-well, we do it recursively until wil drain and
     * delete the table.
     * @param table
     * @throws Exception
     * TODO: LIMIT RECURSION AND TEST BULK_SIZE > 1
     */
    public void flush(Table table) throws Exception {
        int bulkSize = mConfig.getBulkSize();
        Batch batch = null;
        while (bulkSize >= 1) {
            batch = mStorage.getEvents(table, bulkSize);
            if (batch != null) {
                int byteSize = batch.events.toString().getBytes("UTF-8").length;
                if (byteSize <= mConfig.getMaximumRequestLimit()) break;
                bulkSize = (int) (bulkSize / ceil(byteSize / mConfig.getMaximumRequestLimit()));
            } else break;
        }
        if (batch != null) {
            JSONObject event = new JSONObject();
            event.put(ReportIntent.TABLE, table.name);
            event.put(ReportIntent.TOKEN, table.token);
            event.put(ReportIntent.DATA, batch.events.toString());
            SEND_RESULT res = sendData(createMessage(event, true), mConfig.getIBEndPointBulk());
            if (res == SEND_RESULT.FAILED_RESEND_LATER) {
                throw new Exception("Failed flush entries for table: " + table.name);
            }
            if (mStorage.deleteEvents(table, batch.lastId) < bulkSize || mStorage.count(table) == 0) {
                mStorage.deleteTable(table);
            } else {
                flush(table);
            }
        }
    }

    /**
     * Prepare the giving object before sending it to IronBeast(Do auth, etc..)
     * @param obj  - the given event to working on.
     * @param bulk - indicate if it need to add a bulk field.
     * @return
     */
    private String createMessage(JSONObject obj, boolean bulk) {
        String message = "";
        try {
            JSONObject clone = new JSONObject(obj.toString());
            String data = clone.getString(ReportIntent.DATA);
            clone.put(ReportIntent.AUTH,
                    Utils.auth(data, (String) clone.remove(ReportIntent.TOKEN)));
            if (bulk) {
                clone.put(ReportIntent.BULK, true);
            }
            message = clone.toString();
        } catch (Exception e) {
            Logger.log("ReportHandler: failed create message" + e, Logger.SDK_DEBUG);
        }
        return message;
    }

    /**
     * @param data - Stringified JSON. used as a request body.
     * @param url  - IronBeast url endpoint.
     * @return SEND_RESULT ENUM that indicate what to do later on.
     */
    protected SEND_RESULT sendData(String data, String url) {
        SEND_RESULT sendResult = SEND_RESULT.FAILED_RESEND_LATER;
        int nRetry = mConfig.getNumOfRetries();
        RemoteService poster = getPoster();
        while (nRetry-- > 0) {
            if (poster.isOnline(mContext)) {
                try {
                    RemoteService.Response response = poster.post(data, url);
                    if (response.code == HttpURLConnection.HTTP_OK) {
                        sendResult = SEND_RESULT.SUCCESS;
                        break;
                    }
                    if (response.code >= HttpURLConnection.HTTP_BAD_REQUEST &&
                            response.code < HttpURLConnection.HTTP_INTERNAL_ERROR) {
                        sendResult = SEND_RESULT.FAILED_DELETE;
                        break;
                    }
                } catch (SocketTimeoutException | UnknownHostException | SocketException e) {
                    Logger.log(TAG, "Connectivity error, try again or later", Logger.SDK_DEBUG);
                } catch (IOException e) {
                    Logger.log(TAG, "Service IronBeast is unavailable right now", Logger.SDK_ERROR);
                }
            }
            try {
                TimeUnit.SECONDS.sleep(mConfig.getIdleSeconds());
            } catch (InterruptedException e) {
                Logger.log(TAG, "Failed to sleep between retries", Logger.SDK_DEBUG);
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
    protected StorageService getStorage(Context context) {
        return DbAdapter.getInstance(context);
    }

    enum SEND_RESULT {
        SUCCESS, FAILED_DELETE, FAILED_RESEND_LATER
    }

    private static final String TAG = "ReportHandler";
    private StorageService mStorage;
    private IBConfig mConfig;
    private Context mContext;
}
