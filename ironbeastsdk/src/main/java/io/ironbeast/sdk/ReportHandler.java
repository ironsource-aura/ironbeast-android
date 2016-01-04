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
import java.util.Iterator;
import java.util.List;

import static java.lang.Math.ceil;

public class ReportHandler {

    public ReportHandler(Context context) {
        mContext = context;
        mConfig = getConfig(context);
        mStorage = getStorage(context);
        mPoster = getPoster();
    }

    /**
     * handleReport responsible to handle the given ReportIntent based on the
     * event-type(that could be one of the 3: FLUSH, ENQUEUE or POST_SYNC).
     * @param intent
     * @return result of the handleReport if success true or failed false
     */
    public synchronized HandleStatus handleReport(Intent intent) {
        HandleStatus status = HandleStatus.HANDLED;
        boolean isOnline = mPoster.isOnline(mContext) &&
                (!mConfig.isRoamingFlushDisabled() ||  Utils.isConnectedWifi(mContext));
        try {
            if (null == intent.getExtras()) return status;
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
                Logger.log(TAG, "Failed extracting the data from Intent", Logger.SDK_ERROR);
            }
            List<Table> tablesToFlush = new ArrayList<>();
            switch (event) {
                case SdkEvent.FLUSH_QUEUE:
                    if (isOnline) {
                        tablesToFlush = mStorage.getTables();
                        break;
                    }
                    return HandleStatus.RETRY;
                case SdkEvent.POST_SYNC:
                    if (isOnline) {
                        String message = createMessage(dataObject, false);
                        String url = mConfig.getIBEndPoint(dataObject.getString(ReportIntent.TOKEN));
                        if (SendStatus.RETRY != send(message, url)) break;
                    }
                case SdkEvent.ENQUEUE:
                    Table table = new Table(dataObject.getString(ReportIntent.TABLE),
                            dataObject.getString(ReportIntent.TOKEN));
                    int nRows = mStorage.addEvent(table, dataObject.getString(ReportIntent.DATA));
                    if (isOnline && mConfig.getBulkSize() <= nRows) {
                        tablesToFlush.add(table);
                    } else {
                        return HandleStatus.RETRY;
                    }
            }
            // If there's something to flush, it'll not be empty.
            for (Table table: tablesToFlush) flush(table);
        } catch (Exception e) {
            status = HandleStatus.RETRY;
            Logger.log(TAG, e.getMessage(), Logger.SDK_DEBUG);
        }
        return status;
    }

    /**
     * First, we peek the batch the fits with the `MaximumRequestLimit`
     * after that we prepare the request and send it.
     * if the send failed, we stop here, and "continue later".
     * if everything goes-well, we do it recursively until wil drain and
     * delete the table.
     * @param table
     * @throws Exception
     */
    public void flush(Table table) throws Exception {
        int bulkSize = mConfig.getBulkSize();
        Batch batch;
        while (true) {
            batch = mStorage.getEvents(table, bulkSize);
            if (batch != null && batch.events.size() > 1) {
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
            SendStatus res = send(createMessage(event, true), mConfig.getIBEndPointBulk(table.token));
            if (res == SendStatus.RETRY) {
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
            Logger.log(TAG, "Failed create message" + e, Logger.SDK_DEBUG);
        }
        return message;
    }

    /**
     * @param data - Stringified JSON. used as a request body.
     * @param url  - IronBeast url endpoint.
     * @return sendStatus ENUM that indicate what to do later on.
     */
    protected SendStatus send(String data, String url) {
        int nRetry = mConfig.getNumOfRetries();
        while (nRetry-- > 0) {
            try {
                RemoteService.Response response = mPoster.post(data, url);
                if (response.code == HttpURLConnection.HTTP_OK) {
                    return SendStatus.SUCCESS;
                }
                if (response.code >= HttpURLConnection.HTTP_BAD_REQUEST &&
                        response.code < HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    return SendStatus.DELETE;
                }
            } catch (SocketTimeoutException | UnknownHostException | SocketException e) {
                Logger.log(TAG, "Connectivity error: " + e, Logger.SDK_DEBUG);
            } catch (IOException e) {
                Logger.log(TAG, "Service IronBeast is unavailable: " + e, Logger.SDK_ERROR);
            }
        }
        return SendStatus.RETRY;
    }

    /**
     * For testing purpose. to allow mocking this behavior.
     */
    protected RemoteService getPoster() {
        return HttpService.getInstance();
    }
    protected StorageService getStorage(Context context) {
        return DbAdapter.getInstance(context);
    }
    protected IBConfig getConfig(Context context) { return IBConfig.getInstance(context); }

    enum SendStatus { SUCCESS, DELETE, RETRY }
    enum HandleStatus { HANDLED, RETRY }

    private static final String TAG = "ReportHandler";
    private StorageService mStorage;
    private RemoteService mPoster;
    private IBConfig mConfig;
    private Context mContext;
}
