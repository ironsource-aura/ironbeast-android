package com.ironsource.mobilcore;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.view.Gravity;
import android.widget.Toast;

import com.ironsource.mobilcore.IronBeast.LOG_TYPE;
import com.ironsource.mobilcore.ReportingConsts.EReportType;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * ImpVerifier: The job of this class is to help warn developers using mobileCore of more immediate wrong or un-recommended implementation. In non critical
 * checks, impVerifier will only work when user inits with LOG_TYPE.DEBUG
 * <p/>
 * Currently, ImpVerifier checks for the following and warns about:
 * <p/>
 * Hard Warnings (Always logs an error message. Will only toast a message to the developer if he's in debug mode) - Mandatory permissions missing from manifest
 * - InstallationTracker receiver isn't registered in manifest - MobileCore's service isn't registered in the manifest - Developer Hash is invalid (according to
 * original check)
 * <p/>
 * Soft Warnings (Only checks when user requests. Logs a warning message to the developer if he's in debug mode) - ShowInterstitial method called directly in
 * onCreate - MobileCore's showInterstitial is called a line after init - Optional premissions missing from manifest
 */

class ImpVerifier {
    private static final String[] REQUIRED_PERMISSIONS = {"android.permission.ACCESS_NETWORK_STATE"}; // "android.permission.INTERNET" checked in a different,
    // already tested method
    private static final String WARNING_DONT_SHOW_IN_ONCREATE = "Calling mobileCore's showInterstitial command in the onCreate event is not optimal, more time will allow better offers to load.";
    private static final String WARNING_RECEIVER_DECLARATION = "mobileCore's receiver is not declared in your manifest, see mobileCore documentation for more details.";
    private static final String WARNING_DONT_SHOW_RIGHT_AFTER_INIT = "It is generally advised not to call mobileCore's showInterstitial directly after calling init";
    private static final String WARNING_OPTIONAL_PERMISSION_MISSING_PREFIX = "mobileCore can potentially work better with: ";
    private static final String WARNING_MANDATORY_PERMISSION_MISSING_PREFIX = "mobileCore requires permission: ";
    private static final String WARNING_DEV_HASH_INVALID = "The developer hash used is invalid";
    private static final String EVENT_NAME_SHOWINTERSTITIAL_LOWER_CASE = "showInterstitial";
    private static final String EVENT_NAME_ONCREATE_LOWER_CASE = "oncreate";
    private static final String WARNING_TRIGGER_EMPTY = "Trigger name must contain at least 1 Alphanumeric character (a-z, A-Z, 0-9) and/or underscore (_)";
    private static final String WARNING_TRIGGER_NOT_ALFANUMERIC_VALUE = "Trigger name can only contain Alphanumeric characters (a-z, A-Z, 0-9) and/or underscore (_)";
    private static ImpVerifier sInstance = null;
    private boolean mDebugMode;
    private Context mContext;
    private int mCodeLine1 = -1;
    private int mCodeLine2 = -1;

    protected static ImpVerifier getInstance() {
        if (sInstance == null) {
            sInstance = new ImpVerifier();
        }
        return sInstance;
    }

    /**
     * Call init first to set global ImpVerifier members.
     *
     * @param context - Context
     * @return true if verification process approved critical issues.
     */
    protected void init(Context context, LOG_TYPE logType) {
        mDebugMode = (logType == LOG_TYPE.DEBUG);
        mContext = context;
    }

    private boolean verifier(Object... triggers) {
        Pattern p = Pattern.compile("^[a-zA-Z0-9_]+$");
        if (triggers.length == 0) {
            warn(WARNING_TRIGGER_EMPTY, WARNING_TYPE.MEDIUM);
            return false;
        }

        return true;

    }

    /**
     * verifyManifest will check the application's manifest for permissions as well as service and receiver registry.
     *
     * @return
     */
    protected boolean verifyManifest() {
        try {
            return checkPermissions();
        } catch (Exception e) {
            Logger.log("ImpVerifier crashed: " + e.getLocalizedMessage(), Logger.SDK_DEBUG);
            return false;
        }
    }

    /**
     * checkService will check if mobileCore's service is in the manifest.
     *
     * @return true If the service is registered in the manifest.
     */
    protected boolean checkService() {
        PackageManager packageManager = mContext.getPackageManager();
        Intent intent = new Intent(mContext, com.ironsource.mobilcore.MobileCoreReport.class);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentServices(intent, PackageManager.MATCH_DEFAULT_ONLY);

        if (resolveInfo.size() > 0) {
            return true;
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        Logger.log("Error: mobileCore's service not detected. Please register according to updated documentation.", Logger.CRITICAL);
        for (StackTraceElement ste : stackTrace) {
            Logger.log(ste.toString(), Logger.CRITICAL);
        }
        return false;
    }

    /**
     * checkReceiver will check if the InstallationTracker receiver is in the manifest.
     *
     * @return true If InstallationTracker is registered in the manifest.
     */

    /**
     * checkPermissions will run through mandatory and optional permissions as dictated above and make sure they're in the manifest
     *
     * @return true If all obligatory permissions are in the manifest. Note: this test will not fail if an optional permission isn't in the manifest.
     */
    private boolean checkPermissions() {
        String curPermission;
        int res;
        PackageManager packageManager = mContext.getPackageManager();
        boolean permissionMissing = false;

        // Check mandatory permissions
        for (int i = 0; i < REQUIRED_PERMISSIONS.length; i++) {
            curPermission = REQUIRED_PERMISSIONS[i];

            res = packageManager.checkPermission(curPermission, mContext.getPackageName());

            if (res != PackageManager.PERMISSION_GRANTED) {
                warn(WARNING_MANDATORY_PERMISSION_MISSING_PREFIX + curPermission, WARNING_TYPE.HARD);
                permissionMissing = true;
            }
        }
        return !permissionMissing;
    }

    /**
     * This method comes to make up for Ginger Bread and below, not warning devs for missing Internet permission. This results in mobileCore failing without any
     * clear indication for developers as to why.
     */
    protected void checkMissingInterenetPermission(Context context) {
        final String INTERNET_PERMISSION = "android.permission.INTERNET";
        final String NO_INTERNET_PERMISSION_ERROR = "Error: mobileCore has detected a critical, missing permission: " + INTERNET_PERMISSION
                + ". Please add it to your manifest for mobileCore to work correctly.";

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            if (!MCUtils.hasPermission(context, INTERNET_PERMISSION)) {
                Logger.log(NO_INTERNET_PERMISSION_ERROR, Logger.CRITICAL);
                logCurrentStackTrace();
            }
        }
    }

    private void logCurrentStackTrace() {
        StackTraceElement[] stea = Thread.currentThread().getStackTrace();
        for (StackTraceElement ste : stea) {
            Logger.log(ste.toString(), Logger.CRITICAL);
        }
    }

    /**
     * This method warns the user not to show our Interstitial on onCreate as there's not enough time to properly load resources. Since users can still define
     * Interstitial listeners, timers, etc... we're only capturing here a call to show immediately from onCreate. This check is independently called from outside
     * ImpVerifier
     *
     * @param ste - StackTraceElement array to be analyzed
     */
    protected void isShowInterstitialShownOnOnCreate(StackTraceElement[] ste) {
        try {
            String methodName;
            for (int i = 0; i < ste.length; i++) {
                methodName = ste[i].getMethodName();
                if (methodName.toLowerCase(Locale.US).contains(EVENT_NAME_ONCREATE_LOWER_CASE)) {
                    if (ste[i - 1] != null && ste[i - 1].getMethodName().toLowerCase(Locale.US).contains(EVENT_NAME_SHOWINTERSTITIAL_LOWER_CASE)) {
                        warn(WARNING_DONT_SHOW_IN_ONCREATE, WARNING_TYPE.SOFT);
                    }
                    return;
                }
            }
        } catch (Exception e) {
            // do nothing
        }
    }

    /**
     * This method will rely on the setting of members codeLine1 & codeLine2 from relevant functions. Notice, the lines calling setCodeLineNumber use
     * getStackTrace()[3] to return the line of the calling stacktrace, meaning the calling activity rather than getStackTrace()[2] that returns the code line
     * of current stacktrace, meaning what line number was the method inside mobilecore.java) Works in conjunction with setCodeLineNumber().
     */
    private boolean areShowInterstitialAndInitTooClose() {
        if (mCodeLine1 == mCodeLine2 - 1) {
            warn(WARNING_DONT_SHOW_RIGHT_AFTER_INIT, WARNING_TYPE.SOFT);
        }
        return true;
    }

    /**
     * @param lineNumber - called method's line number in the context using mobileCore Works in conjunction with areShowInterstitialAndInitTooClose().
     */
    protected void setCodeLineNumber(int lineNumber) {
        if (mCodeLine1 == -1) {
            mCodeLine1 = lineNumber;
        } else if (mCodeLine2 == -1) { // only check the first call to showInterstitial
            mCodeLine2 = lineNumber;
            areShowInterstitialAndInitTooClose();
        }
    }

    /**
     * verifyDevHash uses the existing method of checking the developerHash.
     *
     * @param devHash - The developerHash string
     * @param context - The context running mobileCore
     * @param edit    - MobileCore's SharedPreferences editor
     * @return
     */
    protected boolean verifyDevHash(String devHash, Context context, Editor edit) {
        BigInteger accountId = new BigInteger(devHash, 36);
        String affiliateAccountBase16 = accountId.toString(16);

        if (affiliateAccountBase16.length() > 32) {

            String affiliateAccount = affiliateAccountBase16.substring(32);

            Logger.log("Account name in init. " + affiliateAccount, Logger.SDK_DEBUG);

            // add to prefs
            edit.putString(Consts.PREFS_ACCOUNT_NAME, Guard.encrypt(affiliateAccount));

        } else {
            warn(WARNING_DEV_HASH_INVALID + "(" + devHash + ")", WARNING_TYPE.HARD);
            // setExtra(ReportingConsts.EXTRA_TOKEN, devHash) is here because token is not set yet in prefs
            IronBeastReportData.openReport(context, EReportType.REPORT_TYPE_ERROR).setError("Can't extract affiliateAccount from the token passed")
                    //.setExtra(ReportingConsts.EXTRA_TOKEN, devHash)
                    .send();
            return false;
        }
        return true;
    }

    /**
     * This method will log a warning in the developers console. In more serious offenses a toast message will be displayed.
     *
     * @param warning - warning msg
     * @param wType   - warning severity
     */
    private void warn(final String warning, WARNING_TYPE wType) {
        if (wType == WARNING_TYPE.SOFT) {
            Logger.log("Warning: " + warning, Logger.PRE_INIT);
        } else if (wType == WARNING_TYPE.MEDIUM) {
            Logger.log("Error: " + warning, Logger.CRITICAL);
        } else {
            Logger.log("Error: " + warning, Logger.CRITICAL);
            showToast(warning);
        }
    }

    private void showToast(String s) {
        try {
            Toast toast = Toast.makeText(mContext, s, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            toast = null;
        } catch (Exception e) {
            // do nothing
        }
    }

    /**
     * @return true if developer requested debug mode
     */
    protected boolean hasUserRequestedDebugMode() {
        return mDebugMode;
    }

    private enum WARNING_TYPE {
        SOFT, HARD, MEDIUM
    }
}
