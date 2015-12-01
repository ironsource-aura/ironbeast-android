package com.ironsource.mobilcore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
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
//            IronBeastReportData.openReport(IronBeast.getAppContext(), SdkEvent.ERROR).setError(e).send();
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
//                IronBeastReportData.openReport(IronBeast.getAppContext(), SdkEvent.ERROR).setError(e).send();
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
//            IronBeastReportData.openReport(IronBeast.getAppContext(), SdkEvent.ERROR).setError(e).send();
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
}
