package com.ironsource.mobilcore;

import android.content.Context;

/**
 * Created by mikhaili on 12/9/15.
 */
interface SharePrefService {
    String load(Context context, String key, String defaultValue);
    void save(Context context, String key, String value);
}
