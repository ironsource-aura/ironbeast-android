package io.ironsourceatom.sdk;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


public class ReportService extends IntentService {


    final static private String TAG = "ReportService";
    private AlarmManager mAlarmManager;
    private ReportHandler mHandler;
    private BackOff mBackOff;

    public ReportService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = this.getApplicationContext();
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mHandler = new ReportHandler(context);
        mBackOff = BackOff.getInstance(context);
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

    protected void setAlarm(long triggerMills) {
        Logger.log(TAG, "Setting alarm", Logger.SDK_DEBUG);
        ReportIntent report = new ReportIntent(this, SdkEvent.FLUSH_QUEUE);
        PendingIntent intent = PendingIntent.getService(this, 0, report.getIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.cancel(intent);
        mAlarmManager.set(AlarmManager.RTC, triggerMills, intent);
    }

}
