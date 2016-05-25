package io.ironsourceatom.sdk;

/**
 * Created by kirill.bokhanov on 5/24/16.
 */

class Response implements IResponse{
    public int code;
    public String body;

    @Override
    public String getBody() {
        return this.body;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public void setBody(String body) {
        this.body=body;
    }

    @Override
    public void setCode(int code) {
        this.code=code;
    }
}
