package com.ironsource.mobilcore.ironbeauty;

/**
 * Created by mikhaili on 11/12/15.
 */
abstract class Network<V> {
    public interface INetworkListener {
        void onSendFailedWithResult(String message);
        void onSendSuccessWithResult(String message);
    }
    abstract public void append();

    abstract protected void send(V params, boolean bulk);

    abstract public String getRequestMethod();

    abstract public void setRequestMethod(String requestMethod);
    abstract public String getHostName();
    abstract public void setHostName(String hostName);
}
