package com.ironsource.mobilcore;

public class SdkEvent {
    public static final int ENQUEUE = 0;     // save report
    public static final int FLUSH_QUEUE = 1; // send all reports from storage
    public static final int POST_SYNC = 2;   // send report immediately
    public static final int ERROR = 10;      // save report

}
