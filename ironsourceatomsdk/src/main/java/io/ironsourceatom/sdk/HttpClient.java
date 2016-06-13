package io.ironsourceatom.sdk;

import android.util.Base64;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * HttpClient is the default implementations to RemoteService.
 * Used for processing requests to enpoint
 */
public class HttpClient implements RemoteService {


    private static HttpClient sInstance;
    private static final Object sInstanceLock = new Object();
    private static final String TAG = "HttpService";
    private static final int DEFAULT_READ_TIMEOUT_MILLIS = 15 * 1000; // 15s
    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 10 * 1000; // 10s

    public static HttpClient getInstance() {
        synchronized (sInstanceLock) {
            if (null == sInstance) {
                sInstance = new HttpClient();
            }
        }
        return sInstance;
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
            out = new DataOutputStream(connection.getOutputStream());
            out.write(data.getBytes("UTF-8"));
            out.flush();
            out.close();
            out = null;
            in = connection.getInputStream();
            response.body = new String(Utils.getBytes(in), Charset.forName("UTF-8"));
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

}
