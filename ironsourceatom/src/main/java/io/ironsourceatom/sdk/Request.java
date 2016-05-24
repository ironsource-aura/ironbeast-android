package io.ironsourceatom.sdk;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * Created by kirill.bokhanov on 5/24/16.
 */
public class Request implements IRequest {

    private String endpoint;
    private HttpMethod httpMethod;
    private String body;

    private static Request sInstance;
    private static final Object sInstanceLock = new Object();
    private static final String TAG = "HttpService";
    private static final int DEFAULT_READ_TIMEOUT_MILLIS = 15 * 1000; // 15s
    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 10 * 1000; // 10s

    public Request(String endpoint, HttpMethod httpMethod, String body) {
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.body = body;
    }


    @Override
    public void post(IResponse response, IronSourceAtomCall callback) throws IOException {
        HttpURLConnection connection = null;
        DataOutputStream out = null;
        InputStream in = null;
        try {
            connection = createConnection(endpoint);
            connection.setRequestMethod(httpMethod.toString());
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            out = new DataOutputStream(connection.getOutputStream());
            out.write(body.getBytes("UTF-8"));
            out.flush();
            out.close();
            out = null;
            in = connection.getInputStream();
            response.setBody(new String(Utils.getBytes(in), Charset.forName("UTF-8")));
            response.setCode(connection.getResponseCode());
            in.close();
            in = null;
        } catch (IOException e) {
            if (connection != null && response.getCode() >= HTTP_BAD_REQUEST) {
                Logger.log(TAG, "Failed post to IB. StatusCode: " + response.getCode(), Logger.SDK_DEBUG);
            } else {
                throw e;
            }
        } finally {
            if (null != connection) connection.disconnect();
            if (null != out) out.close();
            if (null != in) in.close();
            callback.callback(response);
        }
    }



    @Override
    public void get(IResponse response, IronSourceAtomCall callback) {

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
