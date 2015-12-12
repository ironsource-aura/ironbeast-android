package io.ironbeast.sdk;

public interface Report {
    Report setData(String value);
    Report setTable(String table);
    Report setToken(String token);
    void send();
}
