package com.ironsource.mobilcore.ironbeauty;

import android.content.Context;

import com.ironsource.mobilcore.IronBeastReport;

import java.lang.ref.WeakReference;

/**
 * Created by mikhaili on 11/12/15.
 */
public class IronBeauty {
    static IronBeauty mInstance;
    Logic mLogic;
    Network mNetwork;
    Logger mLogger = Logger.getInstance("IronBeauty");
    WeakReference<Context> mCtx;

    IronBeauty(Context ctx) {
        mCtx = new WeakReference<>(ctx);
        mNetwork = new NetworkModule();
        mLogic = new Logic(mNetwork);
    }

    public static IronBeauty getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new IronBeauty(ctx);
        }
        return mInstance;
    }

    public void sendReport(IronBeastReport report, int sendPriority) {
        //mLogic.proceedReport(report, sendPriority);
        mLogger.verbose("sendReport: " + report.toString() + sendPriority);
    }

    public void setBatchFileSize(int size) {
        mLogic.setBatchSize(size);
        mLogger.verbose("setBatchFileSize: " + size);
    }

    public void setMaxDelaySend(long delay) {
        mLogic.setMaxDelaySend(delay);
        mLogger.verbose("setMaxDelaySend: " + delay);
    }

    public void setToken(String token) {
        mLogic.setToken(token);
        mLogger.verbose("setToken: " + token);
    }

    public void setLogLevel(int logLevel) {
        mLogger.setLogLevel(logLevel);
    }

    public boolean validateConfig() {
        return true;
    }

    public void setRequestMethod(String method) {
        mNetwork.setRequestMethod(method);
    }

    public static class SendPriority {
        final public static int NOW = 0;
        final public static int BATCH = 1;
    }
}
