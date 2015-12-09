package com.ironsource.mobilcore;

/**
 * Created by mikhaili on 12/9/15.
 */
interface SharePrefService {
    String load(String key, String defaultValue);
    void save(String key, String value);
}
