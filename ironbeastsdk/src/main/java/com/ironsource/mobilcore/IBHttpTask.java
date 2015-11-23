package com.ironsource.mobilcore;

import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class IBHttpTask {

    // static response messages map
    private static final Map<Integer, String> sResponseCodesToMsgs;

    static {
        Map<Integer, String> map = new HashMap<>();
        map.put(IBConsts.RESPONSE_CODE_OK, IBConsts.RESPONSE_MESSAGE_OK);
        map.put(IBConsts.RESPONSE_CODE_INVALID_JSON, IBConsts.RESPONSE_MESSAGE_INVALID_JSON);
        map.put(IBConsts.RESPONSE_CODE_NO_DATA, IBConsts.RESPONSE_MESSAGE_NO_DATA);
        map.put(IBConsts.RESPONSE_CODE_AUTH_ERROR, IBConsts.RESPONSE_MESSAGE_AUTH_ERROR);
        sResponseCodesToMsgs = Collections.unmodifiableMap(map);
    }

    // mandatory params
    private IBHttpObserver mRequestObserver;
    private String mUrlString;
    private String mRequestMethod = IBConsts.DEFAULT_REQUEST_METHOD;
    private int mConnectTimeout = IBConsts.DEFAULT_REQUEST_TIMEOUT_MS;
    private String mContentType = IBConsts.DEFAULT_CONTENT_TYPE;

    // optional params
    private String mBody;
    private HashMap<String, Object> mAdditionalParamsMap;

    // async task
    private IronBeastAsyncTask mCurrentTask;

    private IBHttpTask(HttpTaskBuilder builder) {
        this.mUrlString = builder.url;
        this.mRequestObserver = builder.requestObserver;

        // add optional members
        this.mBody = builder.body;
        this.mAdditionalParamsMap = builder.additionalParamsMap;

        // replace default mandatory params if set
        if (!TextUtils.isEmpty(builder.requestMethod)) {
            this.mRequestMethod = builder.requestMethod;
        }
        if (builder.connectTimeout > 0) {
            this.mConnectTimeout = builder.connectTimeout;
        }
        if (!TextUtils.isEmpty(builder.contentType)) {
            this.mContentType = builder.contentType;
        }
    }

    //==========================================================================
    // 							Public methods
    //==========================================================================

    public void executeTask() {
        mCurrentTask = new IronBeastAsyncTask();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mCurrentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        } else {
            mCurrentTask.execute((Void[]) null);
        }
    }

    public void cancelIfPossible() {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
    }

    //==========================================================================
    // 							Private methods
    //==========================================================================

    private void processResult(String responseString) {
        if (TextUtils.isEmpty(responseString)) {
            mRequestObserver.onError();
        } else {
            mRequestObserver.onFinish(responseString);
        }
    }

    private String executeHttpRequest() {
        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;

        try {
            String finalUrl = new StringBuilder().append(mUrlString)
                    .append(IBUrlUtils.generateParamsString(mAdditionalParamsMap))
                    .toString();

            Log.d("AAAA", "IBHttpTask | executeHttpRequest | finalUrl=" + finalUrl);

            url = new URL(finalUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", mContentType);
            urlConnection.setConnectTimeout(mConnectTimeout);
            urlConnection.setRequestMethod(mRequestMethod);
            if (mRequestMethod.equals(IBConsts.DEFAULT_REQUEST_METHOD)) {
                urlConnection.setDoOutput(true); // marking the request as POST
            }
            urlConnection.connect();

            Log.d("AAAA", "IBHttpTask | executeHttpRequest | mBody=" + mBody);
            if (!TextUtils.isEmpty(mBody)) {
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.writeBytes(mBody);
                wr.close();
            }

            int responseCode = urlConnection.getResponseCode();
            result = sResponseCodesToMsgs.get(responseCode);
            Log.d("AAAA", "IBHttpTask | executeHttpRequest | responseCode=" + responseCode + " | result=" + result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return result;
    }

    //==========================================================================
    // 							internal AsyncTask
    //==========================================================================

    public static class HttpTaskBuilder {
        private IBHttpObserver requestObserver;
        private String url;
        private String body;
        private HashMap<String, Object> additionalParamsMap;
        private String requestMethod;
        private int connectTimeout;
        private String contentType;

        public HttpTaskBuilder(String url, IBHttpObserver requestObserver) {
            this.url = url;
            this.requestObserver = requestObserver;
        }

        public HttpTaskBuilder jsonBody(String jsonBody) {
            this.body = jsonBody;
            return this;
        }

        public HttpTaskBuilder additionalParams(HashMap<String, Object> additionalParamsMap) {
            this.additionalParamsMap = additionalParamsMap;
            return this;
        }

        public HttpTaskBuilder requestMethod(String requestMethod) {
            this.requestMethod = requestMethod;
            return this;
        }

        public HttpTaskBuilder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public HttpTaskBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public IBHttpTask build() {
            return new IBHttpTask(this);
        }
    }

    private class IronBeastAsyncTask
            extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return executeHttpRequest();
        }

        @Override
        protected void onPostExecute(String result) {
            processResult(result);
        }
    }
}
