package io.ironbeast.sdk;

interface SharePrefService {
    String load(String key, String defaultValue);
    void save(String key, String value);
}
