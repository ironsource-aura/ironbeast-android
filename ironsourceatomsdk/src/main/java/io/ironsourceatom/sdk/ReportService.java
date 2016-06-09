package io.ironsourceatom.sdk;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Intent service to handle tracker functionality
 */
public class ReportService extends IntentService {


    final static private String TAG = "ReportService";
    private AlarmManager alarmManager;
    private ReportHandler handler;
    private BackOff backOff;

    public ReportService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = this.getApplicationContext();
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        handler = new ReportHandler(context);
        backOff = BackOff.getInstance(context);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            if (handler.handleReport(intent) == ReportHandler.HandleStatus.RETRY &&
                    backOff.hasNext()) {
                setAlarm(backOff.next());
            } else {
                backOff.reset();
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
        alarmManager.cancel(intent);
        alarmManager.set(AlarmManager.RTC, triggerMills, intent);
    }

}
