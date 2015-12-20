package io.ironbeast.sdk;

import android.app.IntentService;
import android.content.Intent;
import static java.lang.String.format;

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
            int event = intent.getIntExtra(ReportIntent.EXTRA_SDK_EVENT, SdkEvent.ERROR);
            boolean success = mHandler.handleReport(intent);
            if (SdkEvent.ENQUEUE == event || !success) setAlarm();
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
