package io.ironsourceatom.sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Handle send data when putEvent or putEvent method called
 */
public class SimpleReportHandler {


    private static final String TAG = "SimpleReportHandler";
    private RemoteService client;
    private String endpoint;
    private boolean bulk;

    public SimpleReportHandler(Context context) {
    this.client = getClient();


}
    /**
     * handleReport responsible to handle the given SimpleReportIntent
     * @param intent
     */
    public synchronized void handleReport(Intent intent) {

        try {
            if (null == intent.getExtras()) return;
            Bundle bundle = intent.getExtras();
            JSONObject dataObject = new JSONObject();
            try {
                endpoint = (String) bundle.get(ReportIntent.ENDPOINT);
                if(null!=bundle.get(ReportIntent.BULK)&&!"".equals((String)bundle.get(ReportIntent.BULK))) {
                    bulk = Boolean.valueOf((String) bundle.get(ReportIntent.BULK));
                }
                String[] fields = {ReportIntent.TABLE, ReportIntent.TOKEN, ReportIntent.DATA};
                for (String key : fields) {
                    Object value = bundle.get(key);
                    dataObject.put(key, value);
                }
            } catch (Exception e) {
                Logger.log(TAG, "Failed extracting the data from Intent", Logger.SDK_ERROR);
            }
            String message = createMessage(dataObject, bulk);
            send(message, endpoint);

        } catch (Exception e) {

            Logger.log(TAG, e.getMessage(), Logger.SDK_DEBUG);
        }
    }

    /**
     * Prepare the giving object before sending it to IronSourceAtom(Do auth, etc..)
     * @param obj  - the given event to working on.
     * @param bulk - indicate if it need to add a bulk field.
     * @return
     */
    private String createMessage(JSONObject obj, boolean bulk) {
        String message = "";
        try {
            JSONObject clone = new JSONObject(obj.toString());
            String data = clone.getString(ReportIntent.DATA);
            clone.put(SimpleReportIntent.AUTH,
                    Utils.auth(data, (String) clone.remove(SimpleReportIntent.TOKEN)));
            if (bulk) {
                clone.put(SimpleReportIntent.BULK, true);
            }
            message = clone.toString();
        } catch (Exception e) {
            Logger.log(TAG, "Failed create message" + e, Logger.SDK_DEBUG);
        }
        return message;
    }

    /**
     * @param data - Stringified JSON. used as a request body.
     * @param url  - IronSourceAtomFactory url endpoint.
     * @return sendStatus ENUM that indicate what to do later on.
     */
    protected void send(String data, String url) {
            {
            try {
                RemoteService.Response response = new RemoteService.Response();

                response=client.post(data, url);

                if (response.code == HttpURLConnection.HTTP_OK) {
                    Logger.log(TAG, "Status: " + response.code, Logger.SDK_DEBUG);
                    Log.d("Response: ",""+response.code);
                }
                if (response.code >= HttpURLConnection.HTTP_BAD_REQUEST &&
                        response.code < HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    Logger.log(TAG, "Status: " + response.code, Logger.SDK_DEBUG);
                    Log.d("Response: ",""+response.code);
                }
            } catch (SocketTimeoutException | UnknownHostException | SocketException e) {
                Logger.log(TAG, "Connectivity error: " + e, Logger.SDK_DEBUG);

            } catch (IOException e) {
                Logger.log(TAG, "Service IronSourceAtomFactory is unavailable: " + e, Logger.SDK_DEBUG);
            }
        }
    }


    /**
     * For testing purpose. to allow mocking this behavior.
     */
    protected RemoteService getClient() { return HttpClient.getInstance(); }



}