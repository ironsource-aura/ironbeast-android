package com.ironsource.mobilcore;

import android.app.IntentService;
import android.content.Intent;

public class ReportService extends IntentService {
    public ReportService() {
        super("ReportService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.log("ReportService service | onStartCommand --->", Logger.SDK_DEBUG);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.log("ReportService service | onHandleIntent --->", Logger.SDK_DEBUG);
        final Consts.EServiceType serviceType = Consts.EServiceType.getValue(Consts.EXTRA_SERVICE_TYPE, intent);
        try {

            switch (serviceType) {
                case SERVICE_TYPE_REPORT:
                    ReportData report = new ReportData();
                    report.doReport(ReportService.this, intent);
                    break;
                case SERVICE_TYPE_SEND_REPORTS:
                    ReportData.doScheduledSend();
                    break;
                default:
                    break;
            }

        } catch (Throwable th) {
            //TODO: send error report
            Logger.log("ReportService service | onHandleIntent ---> " + th.getMessage(), Logger.SDK_DEBUG);

        }
    }

}
