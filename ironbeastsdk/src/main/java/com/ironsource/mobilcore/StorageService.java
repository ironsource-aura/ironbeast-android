package com.ironsource.mobilcore;

public interface StorageService {
    int count();
    int push(String record);
    String[] drain(); // load data and remove
    String[] peek(); // load data not remove
    void clear();
}
