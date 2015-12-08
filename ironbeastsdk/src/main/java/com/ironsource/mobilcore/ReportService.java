package com.ironsource.mobilcore;

import android.app.IntentService;
import android.content.Intent;

public class ReportService extends IntentService {
    public ReportService() {
        super("ReportService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.log("ReportService service | onHandleIntent --->", Logger.SDK_DEBUG);
        final Consts.EServiceType serviceType = Consts.EServiceType.getValue(Consts.EXTRA_SERVICE_TYPE, intent);
        try {
            switch (serviceType) {
                case SERVICE_TYPE_REPORT:
                    // TODO: Use singleton instead.
                    ReportHandler report = new ReportHandler();
                    report.doReport(ReportService.this, intent);
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
