package com.ironsource.mobilcore.ironbeauty;

import java.util.Map;

/**
 * Created by mikhaili on 11/12/15.
 */
class Logic {
    Batch mBatch;
    Network mNetwork;
    private long maxDelaySend;
    private String token;

    public Logic(Network networkModule) {
        mNetwork = networkModule;
        mBatch = new Batch();
    }

    public void setBatchSize(int size) {
        mBatch.setMaxSize(size);

    }

    public void proceedReport(Map<String, String> params, int sendPriority) {
        switch (sendPriority) {
            case IronBeauty.SendPriority.BATCH:
                mBatch.saveReport(params);
                break;
            case IronBeauty.SendPriority.NOW:
                mNetwork.send(params, false);
                break;
        }
    }

    public void setMaxDelaySend(long maxDelaySend) {
        this.maxDelaySend = maxDelaySend;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
