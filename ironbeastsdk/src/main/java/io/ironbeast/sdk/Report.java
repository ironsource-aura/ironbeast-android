package io.ironbeast.sdk;

import android.content.Intent;

public interface Report {
    Report setData(String value);
    Report setTable(String table);
    Report setToken(String token);
    Intent getIntent();
    void send();
}
