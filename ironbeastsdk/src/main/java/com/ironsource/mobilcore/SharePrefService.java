package com.ironsource.mobilcore;

interface SharePrefService {
    String load(String key, String defaultValue);
    void save(String key, String value);
}
