package io.ironbeast.sdk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * An Network utility class for internal use in this library.
 */
class HttpService implements RemoteService {

    public static HttpService getInstance() {
        synchronized (sInstanceLock) {
            if (null == sInstance) {
                sInstance = new HttpService();
            }
        }
        return sInstance;
    }

    /**
     * Detect whether there's an Internet connection available.
     *
     * @return boolean
     */
    public boolean isOnline(Context context) {
        boolean isOnline;
        try {
            final NetworkInfo netInfo = getNetworkInfo(context);
            isOnline = netInfo != null && netInfo.isConnected();
        } catch (final SecurityException e) {
            isOnline = true;
        }
        return isOnline;
    }

    /**
     * Return a human-readable name describe the type of the network.
     *
     * @param context
     * @return
     */
    public String getConnectedNetworkType(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return info != null && info.isConnected() ? info.getTypeName() : "unknown";
    }

    /**
     * Indicates whether the device is currently roaming on this network.
     * @param context
     * @return
     */
    public boolean isDataRoamingEnabled(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return info != null && info.isRoaming();
    }

    /**
     * Get IronBeast network type based on the returned conectivity
     * network type.
     * @param context
     * @return
     */
    public int getNetworkIBType(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        int networkType = info != null ? info.getType() : 0;
        switch (networkType) {
            case ConnectivityManager.TYPE_MOBILE:
                return IronBeast.NETWORK_MOBILE;
            case ConnectivityManager.TYPE_WIFI:
                return IronBeast.NETWORK_WIFI;
            default:
                return 0;
        }
    }

    private NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Post String-data to the given url.
     *
     * @return RemoteService.Response that has code and body.
     */
    public Response post(final String data, final String url) throws IOException {
        Response response = new Response();
        HttpURLConnection connection = null;
        DataOutputStream out = null;
        InputStream in = null;
        try {
            connection = createConnection(url);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            // Output
            out = new DataOutputStream(connection.getOutputStream());
            out.write(data.getBytes("UTF-8"));
            out.flush();
            out.close();
            out = null;
            // Input
            in = connection.getInputStream();
            response.body = new String(Utils.slurp(in), Charset.forName("UTF-8"));
            response.code = connection.getResponseCode();
            in.close();
            in = null;
        } catch (final IOException e) {
            if (connection != null &&
                    (response.code = connection.getResponseCode()) >= HTTP_BAD_REQUEST) {
                Logger.log(TAG, "Failed post to IB. StatusCode: " + response.code, Logger.SDK_DEBUG);
            } else {
                throw e;
            }
        } finally {
            if (null != connection) connection.disconnect();
            if (null != out) out.close();
            if (null != in) in.close();
        }
        return response;
    }

    /**
     * Returns new connection. referred to by given url.
     */
    protected HttpURLConnection createConnection(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
        connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(DEFAULT_READ_TIMEOUT_MILLIS);
        connection.setDoInput(true);
        return connection;
    }

    private static HttpService sInstance;
    private static final Object sInstanceLock = new Object();
    private static final String TAG = "HttpService";
    private static final int DEFAULT_READ_TIMEOUT_MILLIS = 15 * 1000; // 15s
    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 10 * 1000; // 10s
}
