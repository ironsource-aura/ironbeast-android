package com.mobilecore.mctester.automation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.ironsource.mobilcore.IronBeast;
import com.ironsource.mobilcore.IronBeastReport;

import java.io.File;

public class BaseMainActivity extends Activity {

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v2);
        IronBeast.init(this, "token", IronBeast.LOG_TYPE.DEBUG);

        IronBeastReport.Builder ibReportBuilder = new IronBeastReport.Builder();
        ibReportBuilder.setTableName("LALA").setData("AAAA", "BBB").setData("BBBB", "CCCC");

        IronBeast.track(ibReportBuilder.build());
        IronBeast.post(ibReportBuilder.build());
        IronBeast.flush();
    }

    public void initAll(View v) {
    }

    public void openActivity(View v) {
        int id = v.getId();
        Intent i = null;
        switch (id) {
            case R.id.btnInterstitial:
//                i = new Intent(this, InterstitialWithTriggers.class);
                break;
            case R.id.btnStickeez:
//                i = new Intent(this, StickeezWithTriggers.class);
                break;
            case R.id.btnNativeAds:
//                i = new Intent(this, NativeAdsWithTriggers.class);
                break;
            case R.id.btnDtm:
//                i = new Intent(this, DTMWithTriggers.class);
                break;
            case R.id.btnBackwardsCompat:
//                i = new Intent(this, BackwardsCompatInterstitial.class);
                break;
        }
        if (i != null) {
            startActivity(i);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(BaseMainActivity.this, "Refresing feeds (only if soft expired)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onlyStop(View button) {
        System.exit(0);
    }

    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }

    public void directToMarket(View button) {
//        MobileCore.directToMarket(this, AD_UNIT_TRIGGER.APP_EXIT);
    }

    public void directToMarketIsReady(View button) {
//        if (MobileCore.isDirectToMarketReady()) {
//            Toast.makeText(BaseMainActivity.this, "DTM ready", Toast.LENGTH_SHORT).show();
//        } else {
//
//            Toast.makeText(BaseMainActivity.this, "DTM not ready", Toast.LENGTH_SHORT).show();
//        }
    }

    public void showNativeAdsWithBanners(View button) {
    }

    public void showNativeAdsWithoutBanners(View button) {
    }

    public void newActivity(View button) {
//        Intent intent = new Intent(this, SecondActivity.class);
//        startActivity(intent);
    }

    public void newInterstTriggers(View button) {
//        Intent intent = new Intent(this, InterstitialWithTriggers.class);
//        startActivity(intent);
    }

    public void clearAll(View button) {
        this.getSharedPreferences("1%dss#gfs#ge1%dr1%dps#g_s#gds#ge1%drs#gas#ghs#gSs#g_s#ge1%dr1%dos#gCs#ge1%dls#gis#gb1%do1%dm", 0).edit().clear().commit();
        WebView w = new WebView(this);
        w.clearCache(true);
        clearApplicationData();
        System.exit(0);
    }

}
