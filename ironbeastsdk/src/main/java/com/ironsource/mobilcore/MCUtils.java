package com.ironsource.mobilcore;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ironsource.mobilcore.ReportingConsts.EReportType;

import org.json.JSONException;
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
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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

    public static String getFormattedInstalledAppPackages() {
        ArrayList<String> appPackagesArr = MCUtils.getInstalledAppPackages();
        return TextUtils.join(",", appPackagesArr);

    }

    public static int getNumberOfAppsInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
        int total = list.size();

        Logger.log("found an app total of " + total, Logger.SDK_DEBUG);
        return total;
    }

    public static int getDeviceDpi(Context context) {
        return context.getResources().getDisplayMetrics().densityDpi;

    }

    public static String getDeviceDpiName() {
        int deviceDpi = getDeviceDpi(IronBeast.getAppContext());
        switch (deviceDpi) {
            case DisplayMetrics.DENSITY_LOW:
                return "ldpi";
            case DisplayMetrics.DENSITY_MEDIUM:
                return "mdpi";
            case DisplayMetrics.DENSITY_TV:
                return "tvdpi";
            case DisplayMetrics.DENSITY_HIGH:
                return "hdpi";
            case DisplayMetrics.DENSITY_XHIGH:
                return "xhdpi";
            case DisplayMetrics.DENSITY_XXHIGH:
                return "xxhdpi";
            case DisplayMetrics.DENSITY_XXXHIGH:
                return "xxxhdpi";
            default:
                return "xhdpi";
        }
    }

    public static float asFloatPixels(float dips, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, displayMetrics);
    }

    public static int asIntPixels(float dips, Context context) {
        return (int) (asFloatPixels(dips, context) + 0.5f);
    }

    @SuppressLint("NewApi")
    public static double getDeviceScreenSize(Context context) {
        try {
            DisplayMetrics metrics = new DisplayMetrics();
            double xDpiValue, yDpiValue;

            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                windowManager.getDefaultDisplay().getRealMetrics(metrics);
            } else {
                windowManager.getDefaultDisplay().getMetrics(metrics);
            }

            // Avoid dividing by zero (it's claimed some cheap manufacturers have 0'z in their metrics values)
            xDpiValue = metrics.xdpi;
            if (xDpiValue == 0)
                xDpiValue = 1.0;

            yDpiValue = metrics.ydpi;
            if (yDpiValue == 0)
                yDpiValue = 1.0;

            double x = Math.pow(metrics.widthPixels / xDpiValue, 2);
            double y = Math.pow(metrics.heightPixels / yDpiValue, 2);
            double screenInches = Math.sqrt(x + y);

            return screenInches;
        } catch (Exception e) {
            IronBeastReportData.openReport(EReportType.REPORT_TYPE_ERROR).setError(e).send();
            return -1;
        }
    }

    @SuppressLint("NewApi")
    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            windowManager.getDefaultDisplay().getRealMetrics(metrics);
        } else {
            windowManager.getDefaultDisplay().getMetrics(metrics);
        }

        return metrics.widthPixels;
    }

    public static String doubleFormatter(Double d, Context context) {
        try {
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            DecimalFormat formatter = (DecimalFormat) nf;
            formatter.setMaximumFractionDigits(2);
            DecimalFormatSymbols dfs = formatter.getDecimalFormatSymbols();
            dfs.setDecimalSeparator('.');
            formatter.setDecimalFormatSymbols(dfs);

            return formatter.format(d);
        } catch (Exception e) {
            Logger.log("Error formatting double value", Logger.SDK_DEBUG);
            IronBeastReportData.openReport(EReportType.REPORT_TYPE_ERROR).setError(e).send();
        }
        return null;
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

    private static String getOrientation(Context context) {
        /* if it's portrait, 1 if it's landscape. */
        return String.valueOf(getDeviceOrientation(context) == Configuration.ORIENTATION_PORTRAIT ? 0 : 1);
    }

    public static int getDeviceOrientation(Context context) {
        return context.getResources().getConfiguration().orientation;
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
            IronBeastReportData.openReport(EReportType.REPORT_TYPE_ERROR).setError(e).send();
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

    public static String urlToFileName(String url) {
        String realName = MCUtils.md5(url);

        if (TextUtils.isEmpty(realName)) { // "" is the fallback value of MCUtils.md5()
            realName = String.valueOf(System.currentTimeMillis());
            realName = "file_".concat(realName);
        }

        realName = realName.concat(".apk");

        return realName;
    }

    public static String removeFilePrefix(String s) {
        if (s.startsWith("file://")) {
            return s.substring(8);
        } else {
            return s;
        }
    }

    public static boolean isAppVisible(final Context context) {
        boolean isCarrierVisible = false;
        String testedPackage = context.getPackageName();

        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(testedPackage)) {
                isCarrierVisible = true;
            }
        }

        return isCarrierVisible;
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
                IronBeastReportData.openReport(EReportType.REPORT_TYPE_ERROR).setError(e).send();
            }
            return null;
        }
    }

    public static String getAppMarketUrl(Context context) {
        return MessageFormat.format(MARKET_BASE_URL, context.getPackageName());
    }

    public static String encodeStringUTF8(String origStr) throws UnsupportedEncodingException {
        return URLEncoder.encode(origStr, "UTF-8");
    }

    /**
     * utility for opening a url either in a web view or with an intent
     *
     * @param activity
     * @param url
     * @param internal
     */
    public static void openUrl(Activity activity, String url, boolean internal) {

        if (TextUtils.isEmpty(url)) {
            return;
        }

        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            url = "http://" + url;
        }

        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        activity.startActivity(i);

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
            obj.putOpt("orientation", getOrientation(context));

			/* get string value of mediation param */
            String mediationParam = getMediationParam();
            if (!TextUtils.isEmpty(mediationParam)) {
                obj.putOpt(Consts.MEDIATION_KEY, mediationParam);
            }
            /* ad audience params */
			/* uit is the user id type, it can be a UNIQUE_ID_AD_ID or UNIQUE_ID_UUID */

        } catch (Exception e) {
            IronBeastReportData.openReport(EReportType.REPORT_TYPE_ERROR).setError(e).send();
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
            IronBeastReportData.openReport(EReportType.REPORT_TYPE_ERROR).setError(e).send();
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
            IronBeastReportData.openReport(EReportType.REPORT_TYPE_ERROR).setError(e).send();
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
            IronBeastReportData.openReport(EReportType.REPORT_TYPE_ERROR).setError(e).send();
        }
        return false;
    }

    public static void clearFolder(String folder) {
        File tempDir = new File(folder);
        MCUtils.deleteFolder(tempDir);
        tempDir.mkdir();
    }

    @SuppressLint("InlinedApi")
    public static synchronized SharedPreferences getSharedPrefs(final Context context) {
        if (sSharedPrefs == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                sSharedPrefs = context.getSharedPreferences(Consts.SHARED_PREFS_NAME, Context.MODE_MULTI_PROCESS);
            } else {
                sSharedPrefs = context.getSharedPreferences(Consts.SHARED_PREFS_NAME, 0);
            }
        }
        return sSharedPrefs;
    }

    @SuppressLint("InlinedApi")
    public static SharedPreferences getSharedPrefs(final Context context, String name) {
        return context.getSharedPreferences(name, Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ? Context.MODE_MULTI_PROCESS : Context.MODE_PRIVATE);
    }

    public static SharedPreferences sharedPrefs() {
        return getSharedPrefs(IronBeast.getAppContext());
    }

    public static void setSharedIntPrefs(String key, int value) {
        Editor edit = MCUtils.sharedPrefs().edit();
        edit.putInt(key, value);
        edit.commit();
    }

    public static void setSharedFloatPrefs(String key, float value) {
        Editor edit = MCUtils.sharedPrefs().edit();
        edit.putFloat(key, value);
        edit.commit();
    }

    public static void setSharedLongPrefs(String key, long value) {
        Editor edit = MCUtils.sharedPrefs().edit();
        edit.putLong(key, value);
        edit.commit();
    }

    public static void setSharedBooleanPrefs(String key, boolean value) {
        Editor edit = MCUtils.sharedPrefs().edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public static void setSharedStringPrefs(String key, String value) {
        Editor edit = MCUtils.sharedPrefs().edit();
        edit.putString(key, value);
        edit.commit();
    }

    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    public static void configureWebView(WebView webview, WebChromeClient client) {
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setSupportMultipleWindows(false);
        webview.getSettings().setSupportZoom(false);
        webview.setInitialScale(100);
        webview.setHorizontalScrollBarEnabled(false);
        webview.getSettings().setNeedInitialFocus(false);
        webview.getSettings().setDomStorageEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // From Kitkat this is false by default
            try {
                webview.getSettings().setAllowFileAccessFromFileURLs(true);
                webview.getSettings().setAllowUniversalAccessFromFileURLs(true);
            } catch (Exception ex) {
                // just in case
                IronBeastReportData.openReport(EReportType.REPORT_TYPE_ERROR).setError(ex).send();

            }
        }

        if (client != null) {
            webview.setWebChromeClient(client);
        }
    }

    /**
     * The WebViews in the sdk do not use an Activity as a Context, so we must have a WebViewClient to avoid internal WebView crash (when getting certain non
     * mobilecore urls, for instance in captive mode):
     * "Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?"
     *
     * @param webView
     */
    public static void setDefaultWebViewClient(WebView webView) {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                String errorString = "WebView: " + view.getClass().getName() + ", onReceivedError errorCode:" + errorCode + " , description:" + description + " , failingUrl:" + failingUrl;
                Logger.log(errorString, Logger.SDK_DEBUG);
                IronBeastReportData.openReport(EReportType.REPORT_TYPE_ERROR).setError(errorString).send();
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });
    }

    public static String getShortenedString(String source, int maxLength) {
        if (source == null) {
            return null;
        }

        return source.substring(0, Math.min(source.length(), maxLength));
    }

    /***
     * api reporting utils
     ***/

    public static String getReportingString(Object object) {
        if (object == null) {
            return null;
        } else {
            return object.toString();
        }
    }

    public static String getReportingString(Object[] array) {
        if (array == null || (array.length == 0)) {
            return null;
        }

        StringBuilder sb = new StringBuilder(array.length * 7);
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(",");
            sb.append(array[i]);
        }
        return sb.toString();
    }

    public static boolean isApiReported(Method method) {
        return getSharedPrefs(IronBeast.getAppContext()).getBoolean(getApiPrefsString(method), false);
    }

    public static void markApiAsReported(Method method) {
        setSharedBooleanPrefs(getApiPrefsString(method), true);
    }

    public static boolean isApiReported(Method method, Object[] params) {
        return getSharedPrefs(IronBeast.getAppContext()).getBoolean(getApiPrefsString(method, params), false);
    }

    public static void markApiAsReported(Method method, Object[] params) {
        setSharedBooleanPrefs(getApiPrefsString(method, params), true);
    }

    private static String getApiPrefsString(Method method) {
        return Consts.PREFS_API_CALLED_KEY + "_" + getAppVersionCode() + "_" + method;
    }

    private static String getApiPrefsString(Method method, Object[] params) {
        return Consts.PREFS_API_CALLED_KEY + "_" + getAppVersionCode() + "_" + method + "_" + Arrays.toString(params);
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

    /***************
     * mediation and plugin parameters
     ******************/

    public static void savePluginParam(String param) {
        if (TextUtils.isEmpty(param)) {
            return;
        }
        setSharedStringPrefs(Consts.PREFS_PLUGIN_PARAM, param);
    }

    public static void saveMediationParam(String param) {
        if (TextUtils.isEmpty(param)) {
            return;
        }
        setSharedStringPrefs(Consts.PREFS_MEDIATION_PARAM, param);
    }

    public static String getPluginParam() {
        return sharedPrefs().getString(Consts.PREFS_PLUGIN_PARAM, null);
    }

    public static String getMediationParam() {
        return sharedPrefs().getString(Consts.PREFS_MEDIATION_PARAM, null);
    }

    /***************
     * end of mediation and plugin parameters
     ******************/

    public static boolean checkIfSdkWasInitAndNotifyIfNot(Method method) {
        if (IronBeast.getAppContext() != null) {
            return true;
        } else {
            Logger.log("Trying to use " + method.getName() + " before MobileCore SDK is initialized, make sure to call MobileCore.init() first", Logger.CRITICAL);
            return false;
        }
    }

    public static String getFlowTypeVersion(String mFileName) {
        try {
            SharedPreferences sharedPrefs = getSharedPrefs(IronBeast.getAppContext());
            return sharedPrefs.getString(Consts.PREFS_FLOW_FILE_VERSION + mFileName, "-1");
        } catch (Exception ex) {

        }
        return "-1";

    }

    public static String getCurrentTime() {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat(BACKEND_TIME_FORMAT, Locale.ENGLISH);
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar aUTCCalendar = Calendar.getInstance();
        String gmtTime = dateFormatGmt.format(aUTCCalendar.getTime());
        return gmtTime;
    }

    public interface IFileProcessingProgress {
        void storeStarted(String fileName);

        void storeComplete(String fileName, boolean success);

        void processingComplete(String fileName, boolean success);

        void processingHttpError(String fileName, int statusCode);

        void processingException(String fileName, Exception e);

        void downloadAdditionalResource(JSONObject resource) throws JSONException;

        void processingComplete(JSONObject feed, String mFileName, boolean success);
    }

    public interface IFileHandler {
        boolean processFile(HttpURLConnection httpURLConnection);

        void processHttpError(int statusCode);

        void processException(Exception e);
    }

    public interface IResourcesLoadedListener {
        void feedReady(String fileName, JSONObject feed);

        void resourceComplete(String fileName, boolean success);

        void allComplete(boolean success);
    }

    public static abstract class FileProcessingProgressAdapter implements IFileProcessingProgress {
        public void storeStarted(String fileName) {
        }

        public void storeComplete(String fileName, boolean success) {
        }

        public void processingComplete(String fileName, boolean success) {
        }

        public void processingHttpError(String fileName, int statusCode) {
        }

        public void processingException(String fileName, Exception e) {
        }

        public void downloadAdditionalResource(JSONObject resource) throws JSONException {
        }

        public void processingComplete(JSONObject feed, String mFileName, boolean success) {
        }
    }

    public static abstract class ResourcesLoadedListenerAdapter implements IResourcesLoadedListener {
        public void feedReady(String fileName, JSONObject feed) {
        }

        public void resourceComplete(String fileName, boolean success) {
        }

        public void allComplete(boolean success) {
        }
    }

}
