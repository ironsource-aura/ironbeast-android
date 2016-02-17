package io.ironbeast.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import io.ironbeast.sdk.IronBeast;
import io.ironbeast.sdk.IronBeastTracker;

public class BaseMainActivity extends Activity {
    IronBeast ironBeast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v2);

        // Create and config IronBeast instance
        ironBeast = IronBeast.getInstance(this);
        ironBeast.enableErrorReporting();
        ironBeast.setBulkSize(2);
        ironBeast.setAllowedNetworkTypes(IronBeast.NETWORK_MOBILE | IronBeast.NETWORK_WIFI);
        ironBeast.setAllowedOverRoaming(true);
    }

    public void sendReport(View v) {
        int id = v.getId();
        String url = "http://10.2.2:3000";
        IronBeastTracker tracker = ironBeast.newTracker("YOUR_API_TOKEN");
        tracker.setIBEndPoint(url);

        JSONObject params = new JSONObject();
        switch (id) {
            case R.id.btnTrackReport:
                try {
                    params.put("action", "track");
                    params.put("id", "" + (int) (100 * Math.random()));
                } catch (JSONException e) {
                    Log.d("TAG", "Failed to track your json");
                }
                tracker.track("a8m.table", params);
                break;
            case R.id.btnPostReport:
                try {
                    params.put("action", "post");
                    params.put("id", "" + (int) (100 * Math.random()));
                } catch (JSONException e) {
                    Log.d("TAG", "Failed to track your json");
                }
                // Will send this event immediately
                tracker.track("a8m.table", params, true);
                break;
            case R.id.btnFlushReports:
                tracker.flush();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
