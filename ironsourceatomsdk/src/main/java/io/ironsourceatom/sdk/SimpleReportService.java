package io.ironsourceatom.sdk;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by kirill.bokhanov on 5/26/16.
 */
public class SimpleReportService extends IntentService {

    final static private String TAG = "SimpleReportService";
    private SimpleReportHandler handler;


    public SimpleReportService () {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = this.getApplicationContext();
        handler = new SimpleReportHandler(context);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            handler.handleReport(intent);
        } catch (Throwable th) {
            Logger.log(TAG, "failed to handle intent: " + th, Logger.SDK_ERROR);
        }

    }


}
