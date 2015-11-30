package com.ironsource.mobilcore;

class ReportingConsts {


    protected enum EReportType {
        REPORT_TYPE_IRON_BEAST,
        REPORT_TYPE_ERROR,
        REPORT_TYPE_FLUSH,
        REPORT_TYPE_UPDATE_CONFIG;

        public static EReportType parseString(int value) {
            for (EReportType v : values())
                if (value == v.ordinal())
                    return v;
            throw new IllegalArgumentException();
        }
    }

    public enum EReportFails {
        SHOW, INIT, LOAD;
        public static EReportFails parseString(int value) {
            for(EReportFails v : values())
                if(value == v.ordinal())
                    return v;
            throw new IllegalArgumentException();
        }
    }

    private static final String REPORT_ACTION_SHOWN = "W";
    private static final String REPORT_ACTION_IMPRESSION = "D";
    private static final String REPORT_ACTION_QUIT = "Q";
    private static final String REPORT_ACTION_NO_THANKS = "-";
    private static final String REPORT_ACTION_CLICK = "C";
    private static final String REPORT_ACTION_START = "S";
    private static final String REPORT_ACTION_COMPLETE = "+";
    private static final String REPORT_ACTION_REGRET = "S-";
    private static final String REPORT_ACTION_ALREADY_INSTALLED = "AI";
    private static final String REPORT_ACTION_FILTERED = "X";

    protected enum EReportResult {

	REPORT_ACTION_SHOWN(ReportingConsts.REPORT_ACTION_SHOWN),
	REPORT_ACTION_IMPRESSION(ReportingConsts.REPORT_ACTION_IMPRESSION),
	REPORT_ACTION_QUIT(ReportingConsts.REPORT_ACTION_QUIT),
	REPORT_ACTION_NO_THANKS(ReportingConsts.REPORT_ACTION_NO_THANKS),
	REPORT_ACTION_CLICK(ReportingConsts.REPORT_ACTION_CLICK),
	REPORT_ACTION_START(ReportingConsts.REPORT_ACTION_START),
	REPORT_ACTION_COMPLETE(ReportingConsts.REPORT_ACTION_COMPLETE),
	REPORT_ACTION_REGRET(ReportingConsts.REPORT_ACTION_REGRET),
	REPORT_ACTION_ALREADY_INSTALLED(ReportingConsts.REPORT_ACTION_ALREADY_INSTALLED),
	REPORT_ACTION_FILTERED_APPS(ReportingConsts.REPORT_ACTION_FILTERED);

	private String mReportType;

	EReportResult(String reportType) {
	    mReportType = reportType;
	}

	public String getReportTypeStr() {
	    return mReportType;
	}

	public static EReportResult parseString(String value) {
	    if (value == null)
		throw new IllegalArgumentException();
	    for (EReportResult v : values())
		if (value.equalsIgnoreCase(v.getReportTypeStr()))
		    return v;
	    throw new IllegalArgumentException();
	}
    }

    protected static final String SECRET_KEY = "s#ge1%ds1%du1%do1%drs#gG1%ds1%du1%do1%dms#gas#gFs#ges#ghs#gT";
    
    protected static final String HEADER_KEY = "s#ge1%dp1%dy1%dts#g-1%dys#gg1%drs#ge1%dns#ges#g-1%dx";    
	protected static final String HEADER_VALUE = "1%dns#gi1%dus#gg1%dns#ge1%dp";

    protected static final String EXTRA_REPORT_TYPE = "s#ge1%dp1%dys#gT1%dt1%dr1%do1%dps#ge1%dr";
    protected static final String EXTRA_TOKEN = "1%dns#ge1%dk1%do1%dt";
    protected static final String EXTRA_UNIQUE_ID = "s#gds#gis#g_s#ge1%du1%dqs#gi1%dn1%dus#g_s#ga1%dr1%dt1%dxs#ge";
    protected static final String EXTRA_UNIQUE_ID_TYPE = "com.ironsource.mobilecore.MobileCoreReport_unique_id_type";
    protected static final String EXTRA_UNIQUE_ID_MC_ID = "com.ironsource.mobilecore.MobileCoreReport_unique_id_mcid";
    protected static final String EXTRA_UNIQUE_ID_GAID = "com.ironsource.mobilecore.MobileCoreReport_unique_id_gaid";
    protected static final String EXTRA_IS_LIMIT_AD_TRACKING_ENABLED = "com.ironsource.mobilecore.MobileCoreReport_is_limit_ad_tracking_enabled";
    
    // public static native String encrypt(String data);
    protected static final String EXTRA_FLOW = "com.ironsource.mobilecore.MobileCoreReport_extra_flow";
    protected static final String EXTRA_RES = "com.ironsource.mobilecore.MobileCoreReport_extra_result";
    protected static final String EXTRA_EXCEPTION = "com.ironsource.mobilcore.MobileCoreReport_extra_ex";

    protected static final String EXTRA_OFFERS = "com.ironsource.mobilcore.MobileCoreReport_extra_offers";
    protected static final String EXTRA_FORMATTED_OFFER = "com.ironsource.mobilcore.MobileCoreReport_extra_offer";
    protected static final String EXTRA_FLOW_NAME = "com.ironsource.mobilcore.MobileCoreReport_extra_flow_type";
    protected static final String EXTRA_COMPONENT = "com.ironsource.mobilcore.MobileCoreReport_extra_component";
    protected static final String EXTRA_EVENT = "com.ironsource.mobilcore.MobileCoreReport_extra_event";
    protected static final String EXTRA_ACTION = "com.ironsource.mobilcore.MobileCoreReport_extra_action";
    protected static final String EXTRA_ADDITIONAL_PARAMS = "com.ironsource.mobilcore.MobileCoreReport_extra_additional_params";
    protected static final String EXTRA_FLAT_ADDITIONAL_PARAMS = "com.ironsource.mobilcore.MobileCoreReport_extra_flat_additional_params";

    
    public static final String EXTRA_BASE_DATA = "com.ironsource.mobilcore.MobileCoreReport_extra_base_data";
    public static final String EXTRA_DATA = "com.ironsource.mobilcore.MobileCoreReport_extra_data";
   

    /****************************************/
    protected static final String REPORT_VERSION = "1.0";

    protected static final String REPORT_FIELD_CARRIER = "Carrier";
    protected static final String REPORT_FIELD_BV = "BV";
    protected static final String REPORT_FIELD_FLOW_TYPE = "Flow";
    protected static final String REPORT_FIELD_FLOW_NAME = "FlowName";
    protected static final String REPORT_FIELD_UID = "UID";
    protected static final String REPORT_FIELD_UIT = "UIT";
    protected static final String REPORT_FIELD_MCID = "MCID";
    protected static final String REPORT_FIELD_GAID = "GAID";
    protected static final String REPORT_FIELD_IS_LIMIT_AD_TRACKING_ENABLED = "late";
    protected static final String REPORT_FIELD_RS = "RS";
    protected static final String REPORT_FIELD_ERR = "Err";
    protected static final String REPORT_FIELD_IRVER = "IRVER";
    protected static final String REPORT_FIELD_TOKEN = "TK";
    protected static final String REPORT_FIELD_FIRST_RUN = "FirstRun";
    protected static final String REPORT_FIELD_TIME_SINCE_SHOWN = "time_since_shown";
    protected static final String REPORT_FIELD_RV = "RV";
    protected static final String REPORT_FIELD_PLATFORM = "Platform";
    protected static final String REPORT_FIELD_ORIENTATION = "Orientation";
    protected static final String REPORT_FIELD_CUR_CONN = "curConnection";
    protected static final String REPORT_FIELD_OFFLINE = "Offline";
    protected static final String REPORT_FIELD_CARRIER_VER = "CarrierVer";
    protected static final String REPORT_FIELD_OFFERS = "Offers";
    protected static final String REPORT_FIELD_STICKEE_ID = "ow_id";
    protected static final String REPORT_FIELD_STICKEEZ_POSITION = "stickeez_position";
    protected static final String REPORT_FIELD_SHOW_AD_UNIT_TRIGGER = "trigger";
    protected static final String REPORT_FIELD_VIDEO_AD_CLICK_TIME = "videoClickTime";
    protected static final String REPORT_FIELD_TIMESTAMP = "timestamp";
    protected static final String REPORT_FIELD_CAMPAIGN_ID = "campaign_id";

    protected static final String REPORT_FIELD_INIT_MODEL = "Model";
    protected static final String REPORT_FIELD_INIT_DEVICE = "Device";
    protected static final String REPORT_FIELD_INIT_BRAND = "Brand";
    protected static final String REPORT_FIELD_INIT_LANGUAGE = "Language";
    protected static final String REPORT_FIELD_OS = "OS";
    protected static final String REPORT_FIELD_INIT_CELLULAR = "Cellular";
    protected static final String REPORT_FIELD_INIT_WIFI = "Wifi";
    protected static final String REPORT_FIELD_INIT_DPI = "Dpi";
    protected static final String REPORT_FIELD_INIT_SCREEN_SIZE = "ScreenSize";
    protected static final String REPORT_FIELD_INIT_LAST_APP_INST = "LastAppInst";
    protected static final String REPORT_FIELD_INIT_INSTALLED_APPS = "InstalledApps";
    protected static final String REPORT_FIELD_INIT_UNS = "UNS";
    
    protected static final String REPORT_FIELD_EVENT_COMPONENT = "Component";
    protected static final String REPORT_FIELD_EVENT_EVENT = "Event";
    protected static final String REPORT_FIELD_EVENT_ACTION = "Action";
    protected static final String REPORT_FIELD_EVENT_ADDITIONAL = "AdditionalParams";

    protected static final Object REPORT_FLOW_EVENTS = "events";
    protected static final Object REPORT_FLOW_ERROR = "errors";
    
    protected static final String REPORT_OS_ANDROID = "Android";
    
    protected static final String REPORT_ORIENTATION_LANDSCAPE = "landscape";
    protected static final String REPORT_ORIENTATION_PORTRAIT = "portrait";
    
    protected static final String REPORT_ORIENTATION_UNDEFINED = "undefined";    
    protected static final String REPORT_FIELD_PLUGIN_PARAM = "plugin";
    protected static final String REPORT_FIELD_MEDIATION_PARAM = "mediation";

}
