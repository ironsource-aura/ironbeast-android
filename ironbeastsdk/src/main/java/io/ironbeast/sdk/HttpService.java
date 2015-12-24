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
 * An HTTP utility class for internal use in this library.
 */
public class HttpService implements RemoteService {

    public static HttpService getInstance() {
        synchronized (sInstanceLock) {
            if (null == sInstance) {
                sInstance = new HttpService();
            }
        }
        return sInstance;
    }

    /**
     * Test if there's a network
     */
    public boolean isOnline(Context context) {
        boolean isOnline;
        try {
            final ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo netInfo = cm.getActiveNetworkInfo();
            isOnline = netInfo != null && netInfo.isConnectedOrConnecting();
        } catch (final SecurityException e) {
            // We don't have permission to check connectivity, will assume we are online
            isOnline = true;
        }
        return isOnline;
    }

    /**
     * Post String-data to the given url
     * If you want to use machine 'localhost'(for personal testing), you should use: `10.0.2.2`
     * to get host loopback interface.
     * That's because Android emulator runs inside a Virtual Machine(QEMU)
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
        } catch(final IOException e) {
            if ((response.code = connection.getResponseCode()) >= HTTP_BAD_REQUEST) {
                Logger.log(TAG, "Service IB unavailable:" + e, Logger.SDK_DEBUG);
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
