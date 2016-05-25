package io.ironsourceatom.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.ironsourceatom.sdk.HttpMethod;
import io.ironsourceatom.sdk.IronSourceAtomCall;
import io.ironsourceatom.sdk.IResponse;
import io.ironsourceatom.sdk.IronSourceAtom;
import io.ironsourceatom.sdk.TrackIronSourceAtom;

public class BaseMainActivity extends Activity {
    TrackIronSourceAtom ironsourceatom;
    private static final String STREAM="foremploy_analytics.public.atom_demo_events";
    private static final String ENDPOINT="http://track.atom-data.io/";
    private static final String AUTH="";
    private TextView txtView;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v2);
        txtView = (TextView) findViewById(R.id.textView);
        handler= new Handler();

        // Create and config ironsourceatom instance
        ironsourceatom = ironsourceatom.getInstance(this);
        ironsourceatom.enableErrorReporting();
        ironsourceatom.setBulkSize(2);
        ironsourceatom.setAllowedNetworkTypes(ironsourceatom.NETWORK_MOBILE | ironsourceatom.NETWORK_WIFI);
        ironsourceatom.setAllowedOverRoaming(true);
    }

    public void sendReport(View v) {
        int id = v.getId();
        IronSourceAtom atom = new IronSourceAtom(ENDPOINT);


        final JSONObject params = new JSONObject();
        switch (id) {
            case R.id.btnTrackReport:
                try {
                    params.put("message", "trackPOST");
                    params.put("id", "" + (int) (100 * Math.random()));
                } catch (JSONException e) {
                    Log.d("TAG", "Failed to track your json");
                }
                try {
                    atom.sendEvent(STREAM, params.toString(), new IronSourceAtomCall() {
                        @Override
                        public void call(final IResponse response) {

                            handler.post(new Runnable(){
                                public void run() {
                                    txtView.setText(params.toString()+" "+response.getBody());
                                }
                            });
                            Log.d("Result", "Code"+response.getCode());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btnPostReport:
                try {
                    params.put("message", "trackGET");
                    params.put("id", "" + (int) (100 * Math.random()));
                } catch (JSONException e) {
                    Log.d("TAG", "Failed to track your json");
                }
                try {
                    atom.sendEvent(STREAM, params.toString(), HttpMethod.GET, new IronSourceAtomCall() {
                        @Override
                        public void call(final IResponse response) {

                            handler.post(new Runnable(){
                                public void run() {
                                    txtView.setText(params.toString()+" "+response.getBody());
                                }
                            });
                            Log.d("Result", "Code"+response.getCode());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btnFlushReports:
                try {
                    final Gson gson= new Gson();
                    final List<ExampleData> bulkList= new ArrayList<>();
                    ExampleData data1=new ExampleData(1, "first message");
                    ExampleData data2=new ExampleData(2, "second message");
                    ExampleData data3=new ExampleData(3, "third message");
                    bulkList.add(data1);
                    bulkList.add(data2);
                    bulkList.add(data3);
                    System.out.println(gson.toJson(bulkList).toString());
                    atom.sendEvents(STREAM, gson.toJson(bulkList).toString(), new IronSourceAtomCall() {
                        @Override
                        public void call(final IResponse response) {

                            handler.post(new Runnable(){
                                public void run() {
                                    txtView.setText(gson.toJson(bulkList).toString()+" "+response.getBody());
                                }
                            });
                            Log.d("Result", "Code"+response.getCode());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
