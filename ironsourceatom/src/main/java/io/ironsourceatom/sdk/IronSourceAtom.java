package io.ironsourceatom.sdk;

import android.provider.ContactsContract;

/**
 * Created by kirill.bokhanov on 5/24/16.
 */
public class IronSourceAtom {

    private String endpoint;
    private String authKey;

    public IronSourceAtom(String endpoint){
        this.endpoint=endpoint;
    }

    public IronSourceAtom(String endpoint, String authKey){
        this.endpoint=endpoint;
        this.authKey=authKey;
    }

    public void sendEvent(String streamName, String data, IronSourceAtomCall call){

    }

    public void sendEvent(String streamName, String data, HttpMethod httpMethod, IronSourceAtomCall call){

    }

    public void sendEvents(String streamName, String data, IronSourceAtomCall call){

    }
    public void sendEvents(String streamName, String data, HttpMethod httpMethod, IronSourceAtomCall call){

    }
}
