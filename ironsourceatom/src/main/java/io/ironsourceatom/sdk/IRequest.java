package io.ironsourceatom.sdk;

import java.io.IOException;

/**
 * Created by kirill.bokhanov on 5/24/16.
 */
public interface IRequest {
    public void post(IResponse response, IronSourceAtomCall callback) throws IOException;
    public void get(IResponse response, IronSourceAtomCall callback) throws IOException;
}
