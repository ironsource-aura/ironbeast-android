package io.ironsourceatom.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.ironsourceatom.sdk.IronSourceAtomFactory;
import io.ironsourceatom.sdk.IronSourceAtom;
import io.ironsourceatom.sdk.IronSourceAtomTracker;

public class BaseMainActivity extends Activity {
    private IronSourceAtomFactory ironSourceAtomFactory;
    private final String STREAM="sdkdev_sdkdev.public.atomtestkeyone";
    static int i;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v2);

        // Create and config IronSourceAtomFactory instance
        ironSourceAtomFactory = IronSourceAtomFactory.getInstance(this);
        ironSourceAtomFactory.enableErrorReporting();
        ironSourceAtomFactory.setBulkSize(2);
        ironSourceAtomFactory.setFlushInterval(1000);
        ironSourceAtomFactory.setAllowedNetworkTypes(IronSourceAtomFactory.NETWORK_MOBILE | IronSourceAtomFactory.NETWORK_WIFI);
        ironSourceAtomFactory.setAllowedOverRoaming(true);
    }

    public void sendReport(View v) {
        int id = v.getId();
        String url = "http://track.atom-data.io/";

        //Configure sender to use methods putEvent() or putEvents()
        IronSourceAtom atom = ironSourceAtomFactory.newAtom("I40iwPPOsG3dfWX30labriCg9HqMfL");
        atom.setEndPoint(url);

        //Configure tracker
        IronSourceAtomTracker tracker = ironSourceAtomFactory.newTracker("I40iwPPOsG3dfWX30labriCg9HqMfL");
        tracker.setISAEndPoint(url);

        JSONObject params = new JSONObject();
        switch (id) {
            case R.id.putEventPost:
                try {
                    params.put("message", "track");
                    params.put("id", "" + (int) (100 * Math.random()));
                } catch (JSONException e) {
                    Log.d("TAG", "Failed to track your json");
                }
                atom.putEvent(STREAM, params.toString());
                break;

            case R.id.putEventsBulk:
                Gson gson= new Gson();
                List<ExampleData> bulkList= new ArrayList<>();
                ExampleData data1=new ExampleData(1, "first message");
                ExampleData data2=new ExampleData(2, "second message");
                ExampleData data3=new ExampleData(3, "third message");
                bulkList.add(data1);
                bulkList.add(data2);
                bulkList.add(data3);
                atom.putEvents(STREAM, gson.toJson(bulkList).toString());
                break;
            case R.id.btnTrackReport:
                try {
                    params.put("message", "track");
                    params.put("id", "" + i++);
                } catch (JSONException e) {
                    Log.d("TAG", "Failed to track your json");
                }
                tracker.track(STREAM, params);
                break;
            case R.id.btnPostReport:
                try {
                    params.put("message", "post");
                    params.put("id", "" + (int) (100 * Math.random()));
                } catch (JSONException e) {
                    Log.d("TAG", "Failed to track your json");
                }
                // Will send this event immediately
                tracker.track(STREAM, params, true);
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
