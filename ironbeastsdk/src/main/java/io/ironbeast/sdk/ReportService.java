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
        mConfig = IBConfig.getInstance(this.getApplicationContext());
        mHandler = new ReportHandler(this.getApplicationContext());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            if (mHandler.handleReport(intent) == ReportHandler.HandleStatus.RETRY) {
                // Write the setAlarm mechanism
//                setAlarm();
            }
        } catch (Throwable th) {
            Logger.log(TAG, "onHandleIntent error: " + th, Logger.SDK_ERROR);
        }

    }

    protected void setAlarm() {
        Logger.log(TAG, "Setting alarm", Logger.SDK_DEBUG);
        ReportIntent reportIntent = new ReportIntent(this, SdkEvent.FLUSH_QUEUE);
        Utils.scheduleSendReportsAction(this, reportIntent, mConfig.getFlushInterval());
    }

    final private String TAG = "ReportService";
    private IBConfig mConfig;
    private ReportHandler mHandler;
}
