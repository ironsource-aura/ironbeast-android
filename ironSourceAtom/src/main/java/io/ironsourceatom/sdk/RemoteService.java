package io.ironsourceatom.sdk;

import java.io.IOException;

interface RemoteService {
    Response post(final String data, final String url) throws IOException;


}
