package com.ironsource.mobilcore;

import android.content.Context;
import android.content.SharedPreferences;

class IBPrefService implements SharePrefService {

    //Shared prefs keys for metadata
    private static final String KEY_BULK_SIZE = "bulk_size";
    private static final String KEY_NUM_OF_RETRIES = "num_of_retries";
    private static final String KEY_FLUSH_INTERVAL = "flush_interval";
    private static final String KEY_IB_END_POINT = "ib_end_point";
    private static final String KEY_IB_TOKEN = "ib_token";
    //TODO: save logger_mode???
    private static final String KEY_LOGGER_MODE = "logger_mode";
    private static final String KEY_MAX_REQUEST_LIMIT = "max_request_limit";

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

    public int getKeyBulkSize(int defValue) {
        String value = load(mContext, KEY_BULK_SIZE, "");
        return Integer.getInteger(value, defValue);
    }

    public int getKeyNumOfRetries(int defValue) {
        String value = load(mContext, KEY_NUM_OF_RETRIES, "");
        return Integer.getInteger(value, defValue);
    }

    public int getKeyFlushInterval(int defValue) {
        String value = load(mContext, KEY_FLUSH_INTERVAL, "");
        return Integer.getInteger(value, defValue);
    }

    public String getKeyIbEndPoint(String defValue) {
        return load(mContext, KEY_IB_END_POINT, defValue);
    }

    public int getKeyMaxRequestLimit(int defValue) {
        String value = load(mContext, KEY_MAX_REQUEST_LIMIT, "");
        return Integer.getInteger(value, defValue);
    }

    @Override
    public String load(String key, String defaultValue) {
        SharedPreferences pr = context.getSharedPreferences(Consts.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return pr.getString(key, defaultValue);
    }

    @Override
    public void save(String key, String value) {
        SharedPreferences pr = context.getSharedPreferences(Consts.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pr.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void saveMaximumRequestLimit(long mMaximumRequestLimit) {
        save(mContext, KEY_MAX_REQUEST_LIMIT, String.valueOf(mMaximumRequestLimit));
    }

    public void saveFlushInterval(int interval) {
        save(mContext, KEY_FLUSH_INTERVAL, String.valueOf(interval));
    }

    public void saveBulkSize(int size) {
        save(mContext, KEY_IB_END_POINT, String.valueOf(size));
    }

    public void saveIbEndPoint(String url) {
        save(mContext, KEY_IB_END_POINT, url);
    }

    public void saveToken(String token) {
        save(mContext, KEY_IB_TOKEN, token);
    }

    public String getToken() {
        return load(mContext, KEY_IB_TOKEN, "");
    }
}
