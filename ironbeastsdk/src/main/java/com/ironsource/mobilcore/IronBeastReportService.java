package com.ironsource.mobilcore;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by mikhaili on 11/30/15.
 */
public class IronBeastReportService extends IntentService {
    public IronBeastReportService() {
        super("NewIronBeastReportService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.log("MobileCoreReport service | onHandleIntent --->", Logger.SDK_DEBUG);
        final Consts.EServiceType serviceType = Consts.EServiceType.getValue(Consts.EXTRA_SERVICE_TYPE, intent);
        try {

            switch (serviceType) {
                case SERVICE_TYPE_REPORT:
                    IronBeastReportData report = new IronBeastReportData();
                    report.doReport(IronBeastReportService.this, intent);
                    break;
                case SERVICE_TYPE_SEND_REPORTS:
                    IronBeastReportData.doScheduledSend();
                    break;
                default:
                    break;
            }

        } catch (Throwable th) {
            //TODO: send error report
        }
    }

}
