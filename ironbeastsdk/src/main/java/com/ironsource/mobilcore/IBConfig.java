package com.ironsource.mobilcore;

import android.content.Context;
import android.webkit.URLUtil;

import java.net.MalformedURLException;

public class IBConfig {

    IBConfig (Context context) {
        mContext = context;
        // TODO(Ariel): Store application info in metadata and
        // use `.getApplicationInfo()` to retrieve these values
        mFlushInterval = FLUSH_INTERVAL;
        mBulkSize = BULK_SIZE;
        mIBEndPoint = DEFAULT_URL;
    }

    public static IBConfig getsInstance (Context context) {
        synchronized (sInstanceLock) {
            if (null == sInstance) {
                final Context appContext = context.getApplicationContext();
                sInstance = new IBConfig(appContext);
            }
        }
        return sInstance;
    }

    public String getIBEndPoint () { return mIBEndPoint; }

    public IBConfig setIBEndPoint (String url) throws MalformedURLException {
        if (URLUtil.isValidUrl(url)) {
            mIBEndPoint = url;
        } else {
            throw new MalformedURLException();
        }
        return this;
    }

    public int getBulkSize () { return mBulkSize; }

    public IBConfig setBulkSize (int size) {
        mBulkSize = size;
        return this;
    }

    public int getFlushInterval () { return mFlushInterval; }

    public IBConfig setFlushInterval (int interval) {
        mFlushInterval = interval;
        return this;
    }

    private final Context mContext;
    private int mFlushInterval;
    private int mBulkSize;
    private String mIBEndPoint;

    private static IBConfig sInstance;
    private static final Object sInstanceLock = new Object();
    // Name for persistent storage of app referral SharedPreferences
    static final String REFERRER_PREFS_NAME = "com.ironsource.mobilcore.ReferralInfo";
    // IBConstants
    private static final String DEFAULT_URL = "http://lb.ironbeast.io";
    private static final String BULK_URL = "http://lb.ironbeast.io/bulk";
    private static final int FLUSH_INTERVAL = 60 * 1000; // 1 second
    private final int BULK_SIZE = 30;                    // 30 records
}
