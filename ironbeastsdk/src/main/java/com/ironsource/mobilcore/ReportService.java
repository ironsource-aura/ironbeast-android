package com.ironsource.mobilcore;

import android.app.IntentService;
import android.content.Intent;

public class ReportService extends IntentService {

    public ReportService() {
        super("ReportService");
        mHandler = new ReportHandler(this);
        mConfig = IBConfig.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.log("ReportService service | onHandleIntent --->", Logger.SDK_DEBUG);
        try {
            int event = intent.getIntExtra(ReportIntent.EXTRA_SDK_EVENT, SdkEvent.ERROR);
            boolean success = mHandler.handleReport(intent);
            if (SdkEvent.ENQUEUE == event || !success) setAlarm();
        } catch (Throwable th) {
            //TODO: send error report
            Logger.log("ReportService service | onHandleIntent ---> " + th.getMessage(), Logger.SDK_DEBUG);
        }

    }

    protected void setAlarm() {
        ReportIntent reportIntent = new ReportIntent(this, SdkEvent.FLUSH_QUEUE);
        Utils.scheduleSendReportsAction(this, reportIntent, mConfig.getFlushInterval());
    }
    //
    // Get intent type
    //
    private IBConfig mConfig;
    private ReportHandler mHandler;
}
