package com.mobilecore.mctester.automation;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.ironsource.mobilcore.IronBeast;
import com.ironsource.mobilcore.IronBeastReport;

public class BaseMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v2);
        IronBeast.init(this, "token", IronBeast.LOG_TYPE.DEBUG);
    }

    public void sendReport(View v) {
        int id = v.getId();
        IronBeastReport.Builder ibReportBuilder = new IronBeastReport.Builder();
        ibReportBuilder.setTableName("LALA").setData("AAAA", "BBB").setData("BBBB", "CCCC");

        switch (id) {
            case R.id.btnTrackReport:
                IronBeast.track(ibReportBuilder.build());
                break;
            case R.id.btnPostReport:
                IronBeast.post(ibReportBuilder.build());
                break;
            case R.id.btnFlushReports:
                IronBeast.flush();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
