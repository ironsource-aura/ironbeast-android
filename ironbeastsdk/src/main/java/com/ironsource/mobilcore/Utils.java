package com.ironsource.mobilcore;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class Utils {

    private static final String MARKET_BASE_URL = "https://play.google.com/store/apps/details?id={0}";
    private static final String GOOGLE_PLAY_PACKAGE_NAME = "com.android.vending";
    private static final String BACKEND_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static SharedPreferences sSharedPrefs;

    public static int getCurrentConnection(Context context) {
        return NetworkUtils.getConnectedNetworkType(context);
    }

    public static String interpretConnection(int conn) {
        if (conn == NetworkUtils.CONNECTION_CELLULAR_INT) {
            return "cell";
        }
        if (conn == NetworkUtils.CONNECTION_CELLULAR_2G) {
            return "2G";
        }
        if (conn == NetworkUtils.CONNECTION_CELLULAR_3G) {
            return "3G";
        }
        if (conn == NetworkUtils.CONNECTION_CELLULAR_4G_LTE) {
            return "4G";
        }
        if (conn == NetworkUtils.CONNECTION_WIFI_INT) {
            return "wifi";
        }
        if (conn == NetworkUtils.CONNECTION_WIMAX_INT) {
            return "wimax";
        }
        if (conn == NetworkUtils.CONNECTION_ETHERNET_INT) {
            return "ethernet";
        }
        return "none";
    }

    // This method returns the time in UK time
    public static String getDateLastAppInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        long installed;
        long lastInstalled = Long.MIN_VALUE;
        String appFile;

        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            // Log.d("tlog", "Installed package :" + packageInfo.packageName);
            appFile = packageInfo.sourceDir;
            // Log.d("tlog", "Installed package source dir: " + appFile);
            installed = new File(appFile).lastModified();
            // Log.d("tlog", "Installed package last modified: " + installed);
            if (installed > lastInstalled) {
                lastInstalled = installed;
            }
        }

        Date date = new Date(lastInstalled);
        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy-HH:mm:ss", Locale.UK);
        String formatted = format.format(date);

        return formatted;
    }

    protected static String getCarrierVersion(Context context) {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (null == pInfo) {
            return "null";
        }
        return pInfo.versionName;
    }

    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
//            ReportHandler.openReport(IronBeast.getAppContext(), SdkEvent.ERROR).setError(e).send();
        }
        return "";
    }

    public static String fileNameFromPath(String path) {
        String fileName;
        try {
            int i = path.lastIndexOf('/') + 1;
            fileName = path.substring(i);
            return fileName;
        } catch (IndexOutOfBoundsException e) {
            Logger.log("fileFromPath error: " + e.getMessage(), Logger.SDK_DEBUG);
        }
        return null;
    }

    public static String getPkgNameFromApkFile(Context context, String filePath, String apkFileName) {
        boolean sdCardMounted = true;
        try {
            String APKFilePath = filePath + "/" + apkFileName;
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageArchiveInfo(APKFilePath, 0);
            // In this context PackageManager can't find the file if the download failed (since it doesn't have to be installed to read a package name
            if (TextUtils.isEmpty(APKFilePath)) {
                sdCardMounted = false;
            }
            pi.applicationInfo.sourceDir = APKFilePath;
            pi.applicationInfo.publicSourceDir = APKFilePath;

            String appName = pi.applicationInfo.packageName;
            Logger.log("^^^appName: " + appName, Logger.SDK_DEBUG);

            return appName;
        } catch (Exception e) {
            if (!sdCardMounted) {
//                ReportHandler.openReport(IronBeast.getAppContext(), SdkEvent.ERROR).setError(e).send();
            }
            return null;
        }
    }

    /**************
     * AsyncTask utility methods
     ***************/

    public static <G, R> void executeAsyncTask(AsyncTask<Void, G, R> task) {
        executeAsyncTask(task, (Void[]) null);
    }

    @SuppressLint({"InlinedApi", "NewApi"})
    public static <P, G, R> void executeAsyncTask(AsyncTask<P, G, R> task, P... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            task.execute(params);
        }
    }

    /**************
     * params methods
     ***************/

    /**
     * @param context
     * @param permission
     * @return true if permission exists in androidManifest.xml
     */
    public static boolean hasPermission(Context context, String permission) {
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * @param context
     * @return true if unknown sources are allowed for the device
     */
    @SuppressWarnings("deprecation")
    public static boolean getUnkownSources(Context context) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        try {
            if (currentapiVersion <= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                if (Settings.Secure.getInt(context.getContentResolver(), Settings.Global.INSTALL_NON_MARKET_APPS) == 1) {
                    return true;
                }

            } else if (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 1) {
                return true;
            }
        } catch (Exception e) {
//            ReportHandler.openReport(IronBeast.getAppContext(), SdkEvent.ERROR).setError(e).send();
        }

        return false;
    }



    // Extract input stream
    public static byte[] slurp(final InputStream inputStream)
            throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[8192];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }

    // auth helper
    // Exception could be: NoSuchAlgorithmException, UnsupportedEncodingException
    // and InvalidKeyException
    public static String auth(String data, String key) throws Exception {
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        sha256_HMAC.init(secret_key);
        StringBuilder sb = new StringBuilder();
        for (byte b : sha256_HMAC.doFinal(data.getBytes("UTF-8"))) {
            sb.append(String.format("%1$02x", b));
        }
        return sb.toString();
    }

    public static void scheduleSendReportsAction(Context context, Intent scheduleIntent, long delay) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent intent = PendingIntent.getService(context, 0, scheduleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(intent); // cancel previous one
        //will fire log than device not sleep
        am.set(AlarmManager.RTC, System.currentTimeMillis() + delay, intent);
    }

    /*
    * The following connection types correspond to Android's own Connectivity Manager connection types. See the getNetworkTypeName method.
    * A device can be connected simultaneously to any number of these
    */
    static class NetworkUtils {
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
}
