package io.ironsourceatom.sdk;

import java.io.IOException;

/**
 * Created by kirill.bokhanov on 5/24/16.
 */
public interface IRequest {
    void post(IResponse response) throws IOException;
    void get(IResponse response) throws IOException;
}
