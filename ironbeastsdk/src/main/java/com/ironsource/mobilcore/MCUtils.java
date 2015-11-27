package com.ironsource.mobilcore;

import android.Manifest;
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

import com.ironsource.mobilcore.ReportingConsts.EReportType;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

class MCUtils {

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

    public static ArrayList<String> getInstalledAppPackages() {

        ArrayList<String> packagesArr = new ArrayList<String>();

        PackageManager pm = IronBeast.getAppContext().getPackageManager();

        // get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            packagesArr.add(packageInfo.packageName);
        }

        return packagesArr;
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
            IronBeastReportData.openReport(IronBeast.getAppContext(), EReportType.REPORT_TYPE_ERROR).setError(e).send();
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
                IronBeastReportData.openReport(IronBeast.getAppContext(), EReportType.REPORT_TYPE_ERROR).setError(e).send();
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

    public static JSONObject getMobileParamsJSON(Context context) {

        JSONObject obj = new JSONObject();
        try {
            String devId = IronBeast.getToken(context);

            obj.putOpt("os", escape(Build.VERSION.RELEASE));
            obj.putOpt("deviceCode", escape(Build.DEVICE));
            obj.putOpt("devId", devId);
            obj.putOpt("carVer", getCarrierVersion(context));
            obj.putOpt("bv", ExternalVars.REPLACABLE_BAMBOO_VER);
            obj.putOpt("appId", context.getApplicationContext().getPackageName());
            obj.putOpt("deviceName", escape(Build.MODEL));
            obj.putOpt("deviceBrand", escape(Build.MANUFACTURER));
            obj.putOpt("uns", getUnkownSources(context));
            obj.putOpt("externalStorage", hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE));
            obj.putOpt("sdkVer", Consts.VER);
            obj.putOpt("conn", NetworkUtils.getConnectedNetworkType(context)); // connection on wifi \ 3G
            obj.putOpt("ipn", getInstallerPackageName()); // installer package name
            /* get google play version or -1 if not installed package name */
            obj.putOpt("gpv", getGooglePlayStoreVersion());
            /* get string value of device orientation */
        } catch (Exception e) {
            IronBeastReportData.openReport(IronBeast.getAppContext(), EReportType.REPORT_TYPE_ERROR).setError(e).send();
        }
        Logger.log("getMobileParams " + obj.toString(), Logger.SDK_DEBUG);
        return obj;
    }

    public static String getMobileParams(Context context) {
        return "'" + getMobileParamsJSON(context) + "'";
    }

    /**
     * @param context
     * @param permission
     * @return true if permission exists in androidManifest.xml
     */
    public static boolean hasPermission(Context context, String permission) {
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private static String getInstallerPackageName() {
        String installerPackage;
        try {
            PackageManager pm = IronBeast.getAppContext().getPackageManager();
            String packageName = IronBeast.getAppContext().getPackageName();
            installerPackage = pm.getInstallerPackageName(packageName);
            if (installerPackage == null) {
                installerPackage = "";
            }
        } catch (Exception e) {
            IronBeastReportData.openReport(IronBeast.getAppContext(), EReportType.REPORT_TYPE_ERROR).setError(e).send();
            installerPackage = "error";
        }
        return installerPackage;
    }

    /**
     * @return version if google play installed otherwise null
     */
    private static String getGooglePlayStoreVersion() {

        try {
            PackageInfo pInfo = IronBeast.getAppContext().getPackageManager().getPackageInfo(GOOGLE_PLAY_PACKAGE_NAME, 0);
            if (pInfo != null) {
                return pInfo.versionName;
            }
        } catch (Exception e) {
        }

		/* Google Play not installed */
        return String.valueOf(-1);
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
            IronBeastReportData.openReport(IronBeast.getAppContext(), EReportType.REPORT_TYPE_ERROR).setError(e).send();
        }

        return false;
    }

    /*******************
     * reporting util methods
     *****************/

    public static String formatExceptionMsg(Exception ex) {
        StackTraceElement stackTraceElement = ex.getStackTrace()[0];
        return ex.getMessage() + " ### " + stackTraceElement.getFileName() + "##" + stackTraceElement.getMethodName() + ":" + stackTraceElement.getLineNumber();
    }

    public static String formatExceptionMsg(Exception ex, String caller) {
        return caller + " ###" + formatExceptionMsg(ex);
    }

    public static String extentionFromUrl(String imgUrl) {
        String fileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1, imgUrl.length());
        if (fileName.lastIndexOf(".") < 0) {
            return ".png";
        }
        return fileName.substring(fileName.lastIndexOf("."), fileName.length());

    }

    public static String hashedFilename(String filename) {
        String extention = extentionFromUrl(filename);
        String hashedName = MCUtils.md5(filename) + extention;
        return hashedName;
    }

    public static String getExtentionFromUrl(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
        if (fileName.lastIndexOf(".") < 0) {
            return ".png";
        }
        return fileName.substring(fileName.lastIndexOf("."), fileName.length());

    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    public static File writeFile(InputStream inputStream, String path, String fileName) throws IOException {

        File file = new File(path, fileName);
        File dir = new File(path);
        dir.mkdirs();

        try {
            FileOutputStream outputStream = new FileOutputStream(file);

            int len, bufferSize = 512;
            byte[] buffer = new byte[bufferSize];

            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

        } catch (Exception e) {
            Logger.log("OutputStream write error" + e.toString(), Logger.SDK_DEBUG);
        }
        return file;
    }

    public static File getFileFromPath(String path, String fileName) {
        File file = new File(path, fileName);
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }

    public static String inputStreamToString(InputStream inputStream) throws IOException {

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }

    @SuppressLint("NewApi")
    public static InputStream stringToInputStream(String string) {
        return new ByteArrayInputStream(string.getBytes(Charset.defaultCharset()));

    }

    public static boolean copyFile(File srcFile, File targetFile) {
        try {
            InputStream in = new FileInputStream(srcFile);
            // For Overwrite the file.
            OutputStream out = new FileOutputStream(targetFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            Logger.log("File copied.", Logger.SDK_DEBUG);

            return true;
        } catch (Exception e) {
            IronBeastReportData.openReport(IronBeast.getAppContext(), EReportType.REPORT_TYPE_ERROR).setError(e).send();
        }
        return false;
    }

    public static void clearFolder(String folder) {
        File tempDir = new File(folder);
        MCUtils.deleteFolder(tempDir);
        tempDir.mkdir();
    }

    @SuppressLint("InlinedApi")
    public static synchronized SharedPreferences getSharedPrefs(final Context context, String name) {
        return context.getSharedPreferences(name, Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ? Context.MODE_MULTI_PROCESS : Context.MODE_PRIVATE);
    }

    public static String getShortenedString(String source, int maxLength) {
        if (source == null) {
            return null;
        }

        return source.substring(0, Math.min(source.length(), maxLength));
    }

    private static int getAppVersionCode() {
        int versionCode = -1;
        try {
            versionCode = IronBeast.getAppContext().getPackageManager().getPackageInfo(IronBeast.getAppContext().getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static String getSignature(Method method, boolean longTypeNames) {
        return method.getName() + "(" + parametersAsString(method, longTypeNames) + ")";
    }

    public static String parametersAsString(Method method, boolean longTypeNames) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0)
            return "";
        StringBuilder paramString = new StringBuilder();
        paramString.append(longTypeNames ? parameterTypes[0].getName() : parameterTypes[0].getSimpleName());
        for (int i = 1; i < parameterTypes.length; i++) {
            paramString.append("|").append(longTypeNames ? parameterTypes[i].getName() : parameterTypes[i].getSimpleName());
        }
        return paramString.toString();
    }

    public static String escape(String string) {
        if (string == null) {
            return null;
        }
        StringWriter sw = new StringWriter();
        synchronized (sw.getBuffer()) {
            try {
                /* we don't do escaping on \' so we will just replace it by ' ' */
                string = string.replace('\'', ' ');
                return escape(string, sw).toString();
            } catch (IOException ignored) {
                // will never happen - we are writing to a string writer
                return "";
            }
        }
    }

    private static Writer escape(String string, Writer w) throws IOException {
        if (string == null || string.length() == 0) {
            return w;
        }

        char b;
        char c = 0;
        String hhhh;
        int i;
        int len = string.length();

        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    w.write('\\');
                    w.write(c);
                    break;
                case '/':
                    if (b == '<') {
                        w.write('\\');
                    }
                    w.write(c);
                    break;
                case '\b':
                    w.write("\\b");
                    break;
                case '\t':
                    w.write("\\t");
                    break;
                case '\n':
                    w.write("\\n");
                    break;
                case '\f':
                    w.write("\\f");
                    break;
                case '\r':
                    w.write("\\r");
                    break;
                default:
                    if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
                        w.write("\\u");
                        hhhh = Integer.toHexString(c);
                        w.write("0000", 0, 4 - hhhh.length());
                        w.write(hhhh);
                    } else {
                        w.write(c);
                    }
            }
        }
        return w;
    }

    public static boolean checkIfSdkWasInitAndNotifyIfNot(Method method) {
        if (IronBeast.getAppContext() != null) {
            return true;
        } else {
            Logger.log("Trying to use " + method.getName() + " before MobileCore SDK is initialized, make sure to call MobileCore.init() first", Logger.CRITICAL);
            return false;
        }
    }

    public static String getCurrentTime() {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat(BACKEND_TIME_FORMAT, Locale.ENGLISH);
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar aUTCCalendar = Calendar.getInstance();
        return dateFormatGmt.format(aUTCCalendar.getTime());
    }

    public static void saveConfig(Context ctx, String key, String value) {
        SharedPreferences sp = getSharedPrefs(ctx, Consts.SHARED_PREFS_NAME);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getValueFromConfig(Context ctx, String key) {
        SharedPreferences sp = getSharedPrefs(ctx, Consts.SHARED_PREFS_NAME);
        return sp.getString(key, "");
    }

}
