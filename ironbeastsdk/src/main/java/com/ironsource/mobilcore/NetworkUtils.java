package com.ironsource.mobilcore;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/*
 * The following connection types correspond to Android's own Connectivity Manager connection types. See the getNetworkTypeName method.
 * A device can be connected simultaneously to any number of these 
 */
class NetworkUtils {
    protected static final int CONNECTION_NONE_INT = -1;
    protected static final int CONNECTION_WIFI_INT = 0;

    protected static final int CONNECTION_CELLULAR_INT = 1;

    protected static final int CONNECTION_CELLULAR_2G = 11;
    protected static final int CONNECTION_CELLULAR_3G = 12;
    protected static final int CONNECTION_CELLULAR_4G_LTE = 13;

    protected static final int CONNECTION_WIMAX_INT = 2;
    protected static final int CONNECTION_ETHERNET_INT = 3;

    private static final String CONNECTION_WIFI = "WIFI";
    private static final String CONNECTION_CELLULAR = "MOBILE";
    private static final String CONNECTION_WIMAX = "WIMAX";
    private static final String CONNECTION_ETHERNET = "ETHERNET";

    // NOTE: The following connections also exist but are irrelevant to the SDK:
    // protected static final int CONNECTION_MOBILE_MMS - Used for MMS-specific Mobile data connection.
    // protected static final int CONNECTION_MOBILE_SUPL - Used for locating user's device
    // protected static final int CONNECTION_MOBILE_DUN - Used when setting an upstream connection for tethering
    // protected static final int CONNECTION_MOBILE_HIPRI - High Priority Connection used for ???
    // protected static final int CONNECTION_MOBILE_FOTA - Firmware over the air
    // protected static final int CONNECTION_IMS - Instant Messaging
    // protected static final int CONNECTION_WIFI_P2P - p2p over wifi (devices connect to eachother over wifi)
    // protected static final int CONNECTION_CBS - "Carrier Branded Services"
    // protected static final int CONNECTION_BLUETOOTH - self explanatory
    // protected static final int CONNECTION_DUMMY

    // This method checks a specific connection's availability (not if connection is currently used)
    // Important: The typeName String values are taken from Android's class ConnectivityManager / getNetworkTypeName method.
    protected static boolean isConnectionPossible(Context context, int connectionType) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        if (netInfo == null) {
            Logger.log("NetworkUtils/isConnectionPossible | NetworkInfo is null.", Logger.NORMAL);
            return false;
        }

        for (NetworkInfo ni : netInfo) {
            Logger.log("network detected: " + ni.getTypeName(), Logger.SDK_DEBUG);
            switch (connectionType) {
                case (CONNECTION_CELLULAR_INT):
                case (CONNECTION_CELLULAR_2G):
                case (CONNECTION_CELLULAR_3G):
                case (CONNECTION_CELLULAR_4G_LTE):
                    if (ni.getTypeName().equalsIgnoreCase(CONNECTION_CELLULAR) && ni.isAvailable()) {
                        return true;
                    }
                    break;
                case (CONNECTION_WIFI_INT):
                    if (ni.getTypeName().equalsIgnoreCase(CONNECTION_WIFI) && ni.isAvailable()) {
                        return true;
                    }
                    break;
                case (CONNECTION_WIMAX_INT):
                    if (ni.getTypeName().equalsIgnoreCase(CONNECTION_WIMAX) && ni.isAvailable()) {
                        return true;
                    }
                    break;
                case (CONNECTION_ETHERNET_INT):
                    if (ni.getTypeName().equalsIgnoreCase(CONNECTION_ETHERNET) && ni.isAvailable()) {
                        return true;
                    }
                    break;
                default:
                    Logger.log("NetworkUtils/isConnectionPossible | error: connection requested is not defined in NetworkUtils.", Logger.NORMAL);
            }
        }
        return false;
    }

    // This method checks which connection is currently active and connected.
    protected static int getConnectedNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        if (info != null && info.isConnected()) {

            if (info.getTypeName().equalsIgnoreCase(CONNECTION_CELLULAR)) {
                int networkType = info.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return CONNECTION_CELLULAR_2G;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        return CONNECTION_CELLULAR_3G;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        return CONNECTION_CELLULAR_4G_LTE;
                    default:
                        // pass
                }
            }
            if (info.getTypeName().equalsIgnoreCase(CONNECTION_WIFI)) {
                return CONNECTION_WIFI_INT;
            }
            if (info.getTypeName().equalsIgnoreCase(CONNECTION_WIMAX)) {
                return CONNECTION_WIMAX_INT;
            }
            if (info.getTypeName().equalsIgnoreCase(CONNECTION_ETHERNET)) {
                return CONNECTION_ETHERNET_INT;
            }
        }

        return CONNECTION_NONE_INT;
    }

    // This method checks if any connection is active and connected.
    protected static boolean isNetworkAvail(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = conMgr.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
    }
}
