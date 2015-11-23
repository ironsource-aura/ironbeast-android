package com.ironsource.mobilcore.ironbeauty;

class IBConsts {

    static final String URL_DEFAULT_IRON_BEAST_HOST_NAME = "http://lb.ironbeast.io";
    static final String URL_BULK_DATA_IRON_BEAST_HOST = "http://lb.ironbeast.io/bulk";

    static final String IRON_BEAST_TABLE_PREFIX = "mobile_ssd.mobile.";
    static final String IRON_BEAST_DEFAULT_IMPRESSIONS_TABLE = IRON_BEAST_TABLE_PREFIX + "discover_impressions";
    static final String IRON_BEAST_DEFAULT_CLICKS_TABLE = IRON_BEAST_TABLE_PREFIX + "discover_clicks";
    static final String IRON_BEAST_DEFAULT_SESSION_DATA_TABLE = IRON_BEAST_TABLE_PREFIX + "discover_session_data";

    static final String IRON_BEAST_KEY_TABLE = "table";
    static final String IRON_BEAST_KEY_DATA = "data";
    static final String IRON_BEAST_KEY_AUTH = "auth";
    static final String IRON_BEAST_KEY_BULK = "bulk";

    static final int RESPONSE_CODE_OK = 200;
    static final int RESPONSE_CODE_INVALID_JSON = 421;
    static final int RESPONSE_CODE_NO_DATA = 422;
    static final int RESPONSE_CODE_AUTH_ERROR = 424;

    static final String RESPONSE_MESSAGE_OK = "OK";
    static final String RESPONSE_MESSAGE_INVALID_JSON = "invalid json";
    static final String RESPONSE_MESSAGE_NO_DATA = "no data";
    static final String RESPONSE_MESSAGE_AUTH_ERROR = "auth error";

    static final int DEFAULT_REQUEST_TIMEOUT_MS = 15 * 1000;
    static final String DEFAULT_CONTENT_TYPE = "application/json; charset=UTF-8";
    static final String DEFAULT_REQUEST_METHOD = "POST";

}
