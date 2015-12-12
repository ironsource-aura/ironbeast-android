package com.mobilecore.mctester.automation;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import io.ironbeast.sdk.IBConfig;
import io.ironbeast.sdk.IronBeast;

import org.json.JSONException;
import org.json.JSONObject;

public class BaseMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v2);
    }

    public void sendReport(View v) {
        int id = v.getId();
        IronBeast tracker = IronBeast.getInstance(this, "myToken");
        IBConfig.Builder builder = new IBConfig.Builder();
        try {
            IBConfig conf = builder.setFlushInterval(3).setIBEndPoint("http://10.0.2.2:3000/").setLogLevel(IBConfig.LOG_TYPE.DEBUG).setBulkSize(4).build();
            tracker.setConfig(conf);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        JSONObject params = new JSONObject();
        switch (id) {
            case R.id.btnTrackReport:
                try {
                    params.put("action", "track");
                    params.put("id", "" + (int) (100 * Math.random()));
                } catch (JSONException e) {
                    Log.d("TAG", "Failed to track your json");
                }
                tracker.track("ibtest", params);
                tracker.track("mobile", params);
                break;
            case R.id.btnPostReport:
                try {
                    params.put("action", "post");
                    params.put("id", "" + (int) (100 * Math.random()));
                } catch (JSONException e) {
                    Log.d("TAG", "Failed to track your json");
                }
                tracker.post("ibtest", params);
                break;
            case R.id.btnFlushReports:
                tracker.flush();
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
