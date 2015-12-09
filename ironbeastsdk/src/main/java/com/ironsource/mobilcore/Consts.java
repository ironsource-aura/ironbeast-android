package com.ironsource.mobilcore;

class Consts {

    protected static final String VER = BuildConfig.VERSION_NAME;
    protected static final String SHARED_PREF_NAME = "ironbeast.prefs";

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
    }
}
