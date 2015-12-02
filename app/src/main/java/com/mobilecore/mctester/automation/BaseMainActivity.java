package com.mobilecore.mctester.automation;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.ironsource.mobilcore.IBConfig;
import com.ironsource.mobilcore.IronBeast;

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
        JSONObject params = new JSONObject();
        try {
            params.put("hello", "world");
        } catch (JSONException e) {
            Log.d("TAG", "Failed to track your json");
        }
        switch (id) {
            case R.id.btnTrackReport:
                tracker.track("ibtest", params);
                tracker.track("mobile", params);
                break;
            case R.id.btnPostReport:
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
