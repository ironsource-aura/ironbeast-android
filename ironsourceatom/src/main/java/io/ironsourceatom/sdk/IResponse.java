package io.ironsourceatom.sdk;

/**
 * Created by kirill.bokhanov on 5/24/16.
 */
public interface IResponse {
    public String getBody();
    public int getCode();
    public void setBody(String body);
    public void setCode(int code);
}
