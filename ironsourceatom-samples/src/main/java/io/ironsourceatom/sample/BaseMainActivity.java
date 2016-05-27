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

import io.ironsourceatom.sdk.HttpMethod;
import io.ironsourceatom.sdk.IronSourceAtom;
import io.ironsourceatom.sdk.IronSourceAtomEventSender;

public class BaseMainActivity extends Activity {
    private IronSourceAtom ironSourceAtom;
    private final String STREAM="foremploy_analytics.public.atomdata";
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
                sender.sendEvent(STREAM, params.toString());
                break;
            case R.id.btnPostReport:
                try {
                    params.put("action", "post");
                    params.put("id", "" + (int) (100 * Math.random()));
                } catch (JSONException e) {
                    Log.d("TAG", "Failed to track your json");
                }
                // Will send this event immediately
                sender.sendEvent(STREAM, params.toString(), HttpMethod.GET);
                break;
            case R.id.btnFlushReports:
                Gson gson= new Gson();
                List<ExampleData> bulkList= new ArrayList<>();
                ExampleData data1=new ExampleData(1, "first message");
                ExampleData data2=new ExampleData(2, "second message");
                ExampleData data3=new ExampleData(3, "third message");
                bulkList.add(data1);
                bulkList.add(data2);
                bulkList.add(data3);
                System.out.println(gson.toJson(bulkList).toString());
                sender.sendEvents(STREAM, gson.toJson(bulkList).toString());
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
