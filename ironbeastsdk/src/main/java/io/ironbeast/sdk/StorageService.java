package io.ironbeast.sdk;

interface StorageService {
    int count();
    int push(String ... records);
    String[] drain(); // load data and remove
    String[] peek(); // load data not remove
    void clear();
}
