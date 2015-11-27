package com.ironsource.mobilcore;

import android.content.Intent;

class Consts {

    protected static final int DEFAULT_BATCH_SIZE = 30;
    protected static final int REPORT_MAX_ERR_FIELD_LENGTH = 256;

    protected static final String VER = Config.VER;
    protected static final String SHARED_PREFS_NAME_HASH = "s#ges#gd1%ds#gos#gcs#ghss#gas#gh";
    protected static final String SHARED_PREFS_NAME = "1%dss#gfs#ge1%dr1%dps#g_s#gds#ge1%drs#gas#ghs#gSs#g_s#ge1%dr1%dos#gCs#ge1%dls#gis#gb1%do1%dm";

    // port
    protected static final String EXTRA_SERVICE_TYPE = "extra_service_type";
    protected static final String OS_ANDROID = "android";

    public static final String PREFS_CURRENT_BATCH_SIZE = "prefs_curr_batch_size";
    public static final String PREFS_MAX_BATCH_SIZE = "prefs_batch_size";
    public static final String PREFS_TOKEN = "prefs_batch_token";

    protected enum EServiceType {

        SERVICE_TYPE_REPORT,
        SERVICE_TYPE_APK_DOWNLOAD,
        SERVICE_TYPE_SEND_REPORTS;

        public static EServiceType parse(int value) {
            for (EServiceType v : values())
                if (value == v.ordinal())
                    return v;
            throw new IllegalArgumentException();
        }

        public static EServiceType getValue(String name, Intent intent) {
            if (!intent.hasExtra(name))
                throw new IllegalStateException();
            return values()[intent.getIntExtra(name, -1)];
        }

        public void setValue(String name, Intent intent) {
            intent.putExtra(name, ordinal());
        }
    }
}
