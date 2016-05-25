package io.ironsourceatom.sdk;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kirill.bokhanov on 5/24/16.
 */
public class IronSourceAtom {

    private String endpoint;
    private String authKey;
    private ExecutorService executor;

    public IronSourceAtom(String endpoint){
        this.endpoint=endpoint;
        this.executor = Executors.newCachedThreadPool();
    }

    public IronSourceAtom(String endpoint, String authKey){
        this.endpoint=endpoint;
        this.authKey=authKey;
        this.executor = Executors.newCachedThreadPool();
    }

    public void sendEvent(String streamName, String data, IronSourceAtomCall call) throws Exception {
        JSONObject jsonData = new JSONObject();
        jsonData.put("data", data);
        jsonData.put("table", streamName);
        executor.execute(new Request(endpoint, HttpMethod.POST, jsonData.toString(), call));
    }

    public void sendEvent(String streamName, String data, HttpMethod httpMethod, IronSourceAtomCall call) throws Exception {
        JSONObject jsonData = new JSONObject();
        jsonData.put("data", data);
        jsonData.put("table", streamName);
        executor.execute(new Request(endpoint, httpMethod, jsonData.toString(), call));
    }

    public void sendEvents(String streamName, String data, IronSourceAtomCall call) throws Exception{
        JSONObject jsonData = new JSONObject();
        jsonData.put("data", data);
        jsonData.put("table", streamName);
        jsonData.put("bulk", "true");
        executor.execute(new Request(endpoint, HttpMethod.POST, jsonData.toString(), call));
    }

    public void sendEvents(String streamName, String data, HttpMethod httpMethod, IronSourceAtomCall call) throws Exception{
        JSONObject jsonData = new JSONObject();
        jsonData.put("data", data);
        jsonData.put("table", streamName);
        jsonData.put("bulk", "true");
        executor.execute(new Request(endpoint, httpMethod, jsonData.toString(), call));
    }
}
