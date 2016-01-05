package io.ironbeast.sdk;

import android.app.IntentService;
import android.content.Intent;


public class ReportService extends IntentService {

    public ReportService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new ReportHandler(this.getApplicationContext());
        mBackOff = BackOff.getInstance(this.getApplicationContext());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            if (mHandler.handleReport(intent) == ReportHandler.HandleStatus.RETRY &&
                    mBackOff.hasNext()) {
                setAlarm(mBackOff.next());
            } else {
                mBackOff.reset();
            }
        } catch (Throwable th) {
            Logger.log(TAG, "failed to handle intent: " + th, Logger.SDK_ERROR);
        }

    }

    protected void setAlarm(long mills) {
        Logger.log(TAG, "Setting alarm", Logger.SDK_DEBUG);
        ReportIntent reportIntent = new ReportIntent(this, SdkEvent.FLUSH_QUEUE);
        Utils.scheduleSendReportsAction(this, reportIntent, mills);
    }

    final static private String TAG = "ReportService";
    private ReportHandler mHandler;
    private BackOff mBackOff;
}
