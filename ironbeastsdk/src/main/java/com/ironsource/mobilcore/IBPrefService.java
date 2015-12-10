package com.ironsource.mobilcore;

import android.content.Context;
import android.content.SharedPreferences;

class IBPrefService implements SharePrefService {

    private static final Object sInstanceLock = new Object();
    static IBPrefService sInstance;
    Context mContext;

    public IBPrefService(Context context) {
        mContext = context;
    }

    public static IBPrefService getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (null == sInstance) {
                sInstance = new IBPrefService(context);
            }
        }
        return sInstance;
    }

    @Override
    public String load(String key, String defaultValue) {
        SharedPreferences pr = mContext.getSharedPreferences(Consts.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        if (null != pr) {
            return pr.getString(key, defaultValue);
        }
        return defaultValue;
    }

    @Override
    public void save(String key, String value) {
        SharedPreferences pr = mContext.getSharedPreferences(Consts.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        if (null != pr) {
            SharedPreferences.Editor editor = pr.edit();
            editor.putString(key, value);
            editor.apply();
        }
    }
}
