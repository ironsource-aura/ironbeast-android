package com.ironsource.mobilcore;

public interface StorageService {
    int count();
    int push(String record);
    String[] drain();
}
