package io.ironsourceatom.sdk;

import android.util.Base64;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;


import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * Created by kirill.bokhanov on 5/24/16.
 */
class Request implements IRequest, Runnable{

    private String endpoint;
    private HttpMethod httpMethod;
    private String body;
    private IronSourceAtomCall callable;
    private static final String TAG = "Request";
    private static final int DEFAULT_READ_TIMEOUT_MILLIS = 15 * 1000; // 15s
    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 10 * 1000; // 10s

    public Request(String endpoint, HttpMethod httpMethod, String body, IronSourceAtomCall callable) {
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.body = body;
        this.callable=callable;
    }


    @Override
        public void post(IResponse response) throws IOException {
        HttpURLConnection connection = null;
        DataOutputStream out = null;
        InputStream in = null;
        try {
            connection = createConnection(endpoint);
            connection.setRequestMethod(httpMethod.POST.toString());
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            out = new DataOutputStream(connection.getOutputStream());
            out.write(body.getBytes("UTF-8"));
            out.flush();
            out.close();
            out = null;
            response.setCode(connection.getResponseCode());
            in = connection.getInputStream();
            response.setBody(new String(Utils.getBytes(in), Charset.forName("UTF-8")));
            in.close();
            in = null;
        } catch (IOException e) {
            if (connection != null && connection.getResponseCode() >= HTTP_BAD_REQUEST) {
                Logger.log(TAG, "Failed post to IronSourceAtom. StatusCode: " + connection.getResponseCode(), Logger.SDK_DEBUG);
            } else {
                throw e;
            }
        } finally {
            if (null != connection) connection.disconnect();
            if (null != out) out.close();
            if (null != in) in.close();
            callable.call(response);

        }

    }



    @Override
    public void get(IResponse response) throws IOException{

        HttpURLConnection connection = null;
        DataOutputStream out = null;
        InputStream in = null;
        try {
            String querry="?data="+ URLEncoder.encode(Base64.encodeToString(body.getBytes(), Base64.URL_SAFE),"utf-8");
            connection = createConnection(endpoint+querry);
            response.setCode(connection.getResponseCode());
            in = connection.getInputStream();
            response.setBody(new String(Utils.getBytes(in), Charset.forName("UTF-8")));
            Thread.sleep(10000);
            in.close();
            in = null;
        } catch (IOException e) {
            if (connection != null && connection.getResponseCode() >= HTTP_BAD_REQUEST) {
                Logger.log(TAG, "Failed post to IronSourceAtom. StatusCode: " + connection.getResponseCode()+ " "+ connection.getResponseMessage(), Logger.SDK_DEBUG);
            } else {
                throw e;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (null != connection) connection.disconnect();
            if (null != out) out.close();
            if (null != in) in.close();
            callable.call(response);

        }

    }

    protected HttpURLConnection createConnection(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
        connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(DEFAULT_READ_TIMEOUT_MILLIS);
        connection.setDoInput(true);
        return connection;
    }



    @Override
    public void run() {
        IResponse response=new Response();
        if(httpMethod==HttpMethod.POST){
            try {
                post(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (httpMethod==HttpMethod.GET){
            try {
                get(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            Logger.log("Unsupported HTTP method", Logger.SDK_DEBUG);
        }

    }
}
