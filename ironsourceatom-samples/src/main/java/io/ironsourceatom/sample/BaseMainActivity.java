package io.ironsourceatom.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import io.ironsourceatom.sdk.IronSourceAtom;
import io.ironsourceatom.sdk.IronSourceAtomEventSender;
import io.ironsourceatom.sdk.IronSourceAtomTracker;

public class BaseMainActivity extends Activity {
    IronSourceAtom ironSourceAtom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v2);

        // Create and config IronSourceAtom instance
        ironSourceAtom = IronSourceAtom.getInstance(this);
        ironSourceAtom.enableErrorReporting();
        ironSourceAtom.setBulkSize(2);
        ironSourceAtom.setAllowedNetworkTypes(IronSourceAtom.NETWORK_MOBILE | IronSourceAtom.NETWORK_WIFI);
        ironSourceAtom.setAllowedOverRoaming(true);
    }

    public void sendReport(View v) {
        int id = v.getId();
        String url = "https://track.atom-data.io/";
        IronSourceAtomTracker tracker = ironSourceAtom.newTracker("3tCP2pIzNW9EYxMdkbyR8TNI75kcpe");
        tracker.setIBEndPoint(url);

        IronSourceAtomEventSender sender = ironSourceAtom.newSender("3tCP2pIzNW9EYxMdkbyR8TNI75kcpe");
        sender.setEndPoint(url);

        JSONObject params = new JSONObject();
        switch (id) {
            case R.id.btnTrackReport:
                try {
                    params.put("action", "track");
                    params.put("id", "" + (int) (100 * Math.random()));
                } catch (JSONException e) {
                    Log.d("TAG", "Failed to track your json");
                }
                sender.sendEvent("foremploy_analytics.public.atomdata", params.toString());
                break;
            case R.id.btnPostReport:
                try {
                    params.put("action", "post");
                    params.put("id", "" + (int) (100 * Math.random()));
                } catch (JSONException e) {
                    Log.d("TAG", "Failed to track your json");
                }
                // Will send this event immediately
                tracker.track("foremploy_analytics.public.atomdata", params, true);
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
