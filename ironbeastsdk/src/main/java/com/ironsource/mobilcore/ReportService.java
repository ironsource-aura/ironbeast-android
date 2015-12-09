package com.ironsource.mobilcore;

import android.app.IntentService;
import android.content.Intent;

public class ReportService extends IntentService {
    public ReportService() {
        super("ReportService");
        mHandler = new ReportHandler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.log("ReportService service | onHandleIntent --->", Logger.SDK_DEBUG);
        try {
            int event = intent.getIntExtra(ReportIntent.EXTRA_REPORT_TYPE, SdkEvent.ERROR);
            boolean success = mHandler.handleReport(ReportService.this, intent);
            if (SdkEvent.ENQUEUE == event || !success) setAlarm();
        } catch (Throwable th) {
            //TODO: send error report
            Logger.log("ReportService service | onHandleIntent ---> " + th.getMessage(), Logger.SDK_DEBUG);
        }

    }

    protected void setAlarm() {
        ReportIntent reportIntent = new ReportIntent(this, SdkEvent.FLUSH_QUEUE);
        Utils.scheduleSendReportsAction(this, reportIntent, IBConfig.getsInstance().getFlushInterval());
    }
    //
    // Get intent type
    //
    private ReportHandler mHandler;
}
