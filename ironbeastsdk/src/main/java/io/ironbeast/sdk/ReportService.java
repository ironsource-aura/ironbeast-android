package io.ironbeast.sdk;

import android.app.IntentService;
import android.content.Intent;

public class ReportService extends IntentService {

    public ReportService() {
        super("ReportService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.log("---> onCreate", Logger.SDK_DEBUG);
        mConfig = IBConfig.getInstance(this.getApplicationContext());
        mHandler = new ReportHandler(this.getApplicationContext());
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
            Logger.log("ReportService service | onHandleIntent | " + th.getMessage(), Logger.SDK_DEBUG);
        }

    }

    protected void setAlarm() {
        Logger.log("--> setAlarm", Logger.SDK_DEBUG);
        ReportIntent reportIntent = new ReportIntent(this, SdkEvent.FLUSH_QUEUE);
        Utils.scheduleSendReportsAction(this, reportIntent, mConfig.getFlushInterval());
    }
    //
    // Get intent type
    //
    private IBConfig mConfig;
    private ReportHandler mHandler;
}
