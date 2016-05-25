package io.ironsourceatom.sdk;

/**
 * Created by kirill.bokhanov on 5/24/16.
 */
public interface IResponse {
    String getBody();
    int getCode();
    void setBody(String body);
    void setCode(int code);
}
