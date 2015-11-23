package com.ironsource.mobilcore.ironbeauty;

import java.util.Map;

/**
 * Created by mikhaili on 11/12/15.
 */
class Batch {
    private int size;
    private int mCurrentSize;

    public Batch() {

    }
    public void setMaxSize(int size) {
        this.size = size;
    }

    synchronized public int getSize() {
        return mCurrentSize;
    }

    public void saveReport(Map<String, String> params) {
        if (size < mCurrentSize) {
            mCurrentSize = saveToBatchFile(params);
        } else {
            sendBatchFile();
            removeBatchFile();
        }
    }

    private int saveToBatchFile(Map<String, String> params) {
        return (++mCurrentSize);
    }

    private void removeBatchFile() {
    }

    private void sendBatchFile() {


    }
}
