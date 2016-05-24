package io.ironsourceatom.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import io.ironsourceatom.sdk.TrackIronSourceAtom;
import io.ironsourceatom.sdk.IronSourceAtomTracker;

public class BaseMainActivity extends Activity {
    TrackIronSourceAtom ironsourceatom;
    private static final String STREAM="foremploy_analytics.public.atom_demo_events";
    private static final String URL="http://track.atom-data.io/";
    private static final String AUTH="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v2);

        // Create and config ironsourceatom instance
        ironsourceatom = ironsourceatom.getInstance(this);
        ironsourceatom.enableErrorReporting();
        ironsourceatom.setBulkSize(2);
        ironsourceatom.setAllowedNetworkTypes(ironsourceatom.NETWORK_MOBILE | ironsourceatom.NETWORK_WIFI);
        ironsourceatom.setAllowedOverRoaming(true);
    }

    public void sendReport(View v) {
        int id = v.getId();
        String url = "http://foremploy_analytics.public.atom_demo_events";
        IronSourceAtomTracker tracker = ironsourceatom.newTracker("YOUR_API_TOKEN");
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
