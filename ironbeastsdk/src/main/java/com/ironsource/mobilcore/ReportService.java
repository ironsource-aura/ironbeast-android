package com.ironsource.mobilcore;

import android.app.IntentService;
import android.content.Intent;

public class ReportService extends IntentService {

    public ReportService() {
        super("ReportService");
        mHandler = new ReportHandler();
        mConfig = IBConfig.getsInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.log("ReportService service | onHandleIntent --->", Logger.SDK_DEBUG);
        try {
            int event = intent.getIntExtra(ReportIntent.EXTRA_SDK_EVENT, SdkEvent.ERROR);
            boolean success = mHandler.doReport(ReportService.this, intent);
            if (SdkEvent.ENQUEUE == event || !success) setAlarm();
        } catch (Throwable th) {
            //TODO: send error report
            Logger.log("ReportService service | onHandleIntent ---> " + th.getMessage(), Logger.SDK_DEBUG);
        }

    }

    protected void setAlarm() {
        ReportIntent reportIntent = new ReportIntent(this, SdkEvent.FLUSH_QUEUE).setToken(mConfig.getToken());
        Utils.scheduleSendReportsAction(this, reportIntent, IBConfig.getsInstance(this).getFlushInterval());
    }
    //
    // Get intent type
    //
    private ReportHandler mHandler;
    private final IBConfig mConfig;
}
