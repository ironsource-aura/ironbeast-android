package io.ironbeast.sdk;

import android.content.Context;
import java.io.IOException;

interface RemoteService {
    boolean isOnline(Context context);
    boolean isDataRoamingEnabled(Context context);
    String getConnectedNetworkType(Context context);
    int getNetworkIBType(Context context);
    Response post(final String data, final String url) throws IOException;

    /**
     * Response-like class
     */
    class Response {
        public int code;
        public String body;
    }
}
