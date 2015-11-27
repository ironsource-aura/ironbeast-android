package com.ironsource.mobilcore;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.ironsource.mobilcore.Consts.EServiceType;
import com.ironsource.mobilcore.ReportingConsts.EReportType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

class IronBeastReportDataOld {

    public static final String TAG = IronBeastReportDataOld.class.getSimpleName();
    private static final String ALGORITHM_AES = "AES/ECB/PKCS7Padding";
    private static final String REPORT_ARR = "reportArr";
    private static final String LOG_STACK_FILE = "log_stack.dat";
    private static final String MCLA_STACK_FILE = "mcla_log_stack.dat";
    private static final String LOG_STACK_FILE_ERRORS = "log_stack_errors.dat";
    private static final String MY_DELIMITER = "MY_FUCKING_DELIMITER";
    // less connections reporting
    private static final int LOG_STACK_MAX_LENGTH = 0;
    private static final int LOG_STACK_ERRORS_MAX_LENGTH = 30;
    private static final long MAX_DELAY_PERIOD_IN_MILLIS = (60 * 1000) + (30 * 1000); // 1.5 minutes
    private Context mContext;

    /**
     * ******** public methods ***********
     */

    public IronBeastReportDataOld() {
        Logger.log("in reporter", Logger.SDK_DEBUG);
    }

    public static IronBeastReportIntent openReport(EReportType type) {
        Context context = IronBeast.getAppContext();
        return new IronBeastReportIntent(context, type);
    }

    public static IronBeastReportIntent openReport(Context context, EReportType type) {
        return new IronBeastReportIntent(context, type);
    }

    private static synchronized void addReportToLogStack(JSONObject report, EReportType type) throws Exception {
        if (!NetworkUtils.isNetworkAvail(IronBeast.getAppContext())) {
            Logger.log("MobileCoreReport | addReportToLogStack | We dont have network, adding offline report field", Logger.SDK_DEBUG);
            report.put(ReportingConsts.REPORT_FIELD_OFFLINE, true);
        }

        JSONObject logStack = null;
        JSONObject errorLogStack = null;
        try {
            logStack = loadlogStackFile(LOG_STACK_FILE);
        } catch (Exception e) {
            /* critical exception clear log file */
            deleteLogFile(LOG_STACK_FILE);
            IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError(e, LOG_STACK_FILE).send();
        }
        try {
            errorLogStack = loadlogStackFile(LOG_STACK_FILE_ERRORS);
        } catch (Exception e) {
            /* critical exception clear log file */
            deleteLogFile(LOG_STACK_FILE_ERRORS);
            IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError(e, LOG_STACK_FILE_ERRORS).send();
        }

/*
        if (isErrorReport) {
            errorLogStack = addToLogStack(errorLogStack, report);
        } else {
            logStack = addToLogStack(logStack, report);
        }

        boolean isLogStacksBigEnough = isLogsStacksBigEnough(logStack, errorLogStack);

        if (!NetworkUtils.isNetworkAvail(IronBeast.getAppContext()) || !isLogStacksBigEnough) {
            if (isErrorReport) {
                appendLogStackFile(LOG_STACK_FILE_ERRORS, report);
            } else {
                appendLogStackFile(LOG_STACK_FILE, report);
            }
            Logger.log("MobileCoreReport | addReportToLogStack | report appended to log stack | isLogStacksBigEnough=" + isLogStacksBigEnough, Logger.SDK_DEBUG);
            return;
        }
*/

        JSONObject resultingStack = getResultingLogStack(logStack, errorLogStack);
        if (resultingStack != null) {
            doSendReport(resultingStack);
        }
    }

    private static void doSendReport(JSONObject resultingReportsStack) {
        Logger.log("MobileCoreReport | doSendReport | resultingReportsStack=" + resultingReportsStack.toString(), Logger.SDK_DEBUG);
        try {
            deleteLogFile(LOG_STACK_FILE);
            deleteLogFile(LOG_STACK_FILE_ERRORS);
            boolean isBulk = false;
            sendData(resultingReportsStack, isBulk, true);
        } catch (Exception e) {
            Logger.log("MobileCoreReport | doSendReport | Exception during sendData e=" + e, Logger.SDK_DEBUG);
            appendLogStackFile(LOG_STACK_FILE_ERRORS, resultingReportsStack);
        } finally {
            scheduleSendReportsAction();
        }
        Logger.log("MobileCoreReport | doSendReport | successfully sent", Logger.SDK_DEBUG);
    }

    private static boolean isLogsStacksBigEnough(JSONObject logStack, JSONObject errorLogStack) {
        if (errorLogStack != null && errorLogStack.optJSONArray(REPORT_ARR) != null && errorLogStack.optJSONArray(REPORT_ARR).length() > LOG_STACK_ERRORS_MAX_LENGTH) {
            return true;
        }

        if (logStack != null && logStack.optJSONArray(REPORT_ARR) != null && logStack.optJSONArray(REPORT_ARR).length() > LOG_STACK_MAX_LENGTH) {
            return true;
        }

        // none of the log stacks isn't big enough
        return false;
    }

    private static void scheduleSendReportsAction() {
        Logger.log("stashMobileCoreReport | scheduleSendReportsAction", Logger.SDK_DEBUG);
        try {
            Context context = IronBeast.getAppContext();

            Intent scheduleIntent = new Intent(context, IronBeastReport.class);
            EServiceType.SERVICE_TYPE_SEND_REPORTS.setValue(Consts.EXTRA_SERVICE_TYPE, scheduleIntent);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent intent = PendingIntent.getService(context, 0, scheduleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            am.cancel(intent); // cancel previous one
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + MAX_DELAY_PERIOD_IN_MILLIS, intent);
        } catch (Exception e) {
            IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError(e).send();
        }
    }

    public static synchronized void doScheduledSend() {
        Logger.log("MobileCoreReport | doScheduledSend", Logger.SDK_DEBUG);
        if (NetworkUtils.isNetworkAvail(IronBeast.getAppContext())) {
            JSONObject logStack = null;
            JSONObject errorLogStack = null;
            JSONObject newReportingStack = null;
            try {
                logStack = loadlogStackFile(LOG_STACK_FILE);
            } catch (Exception e) {
                /* clear log file */
                deleteLogFile(LOG_STACK_FILE);
                IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError(e, LOG_STACK_FILE).send();
            }
            try {
                errorLogStack = loadlogStackFile(LOG_STACK_FILE_ERRORS);
            } catch (Exception e) {
                /* clear log file */
                deleteLogFile(LOG_STACK_FILE_ERRORS);
                IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError(e, LOG_STACK_FILE_ERRORS).send();
            }

            JSONObject resultingStack = getResultingLogStack(logStack, errorLogStack);
            if (resultingStack != null) {
                try {
                    doSendReport(resultingStack);
                } catch (Throwable throwable) {
                    deleteLogFile(LOG_STACK_FILE_ERRORS);
                    IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError(throwable.getMessage()).send();
                }
                try {
                    newReportingStack = loadlogStackFile(MCLA_STACK_FILE);

                } catch (Exception e) {
                    deleteLogFile(MCLA_STACK_FILE);
                    IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError(e, LOG_STACK_FILE).send();
                }
                if (newReportingStack != null) {
                    doSendReport(newReportingStack);
                }
            } else {
                scheduleSendReportsAction();
            }
        }
    }

    private static JSONObject getResultingLogStack(JSONObject logStack, JSONObject errorLogStack) {
        JSONObject resultingStack = null;
        JSONArray resultingArray = new JSONArray();
        try {
            if (logStack != null) {
                JSONArray array = logStack.optJSONArray(REPORT_ARR);
                if (array != null) {
                    for (int i = 0; i < array.length(); i++) {
                        resultingArray.put(array.getJSONObject(i));
                    }
                }
            }
            if (errorLogStack != null) {
                JSONArray errorArray = errorLogStack.optJSONArray(REPORT_ARR);
                if (errorArray != null) {
                    for (int i = 0; i < errorArray.length(); i++) {
                        resultingArray.put(errorArray.getJSONObject(i));
                    }
                }
            }
            if (resultingArray.length() >= 1) {
                resultingStack = new JSONObject();
                resultingStack.putOpt(REPORT_ARR, resultingArray);
            }
        } catch (Exception e) {
            Logger.log("MobileCoreReport | getResultingLogStack | exception while building resulting log stack | e=" + e, Logger.SDK_DEBUG);
        }

        return resultingStack;
    }

    private static synchronized void appendLogStackFile(String filename, JSONObject jsonData) {
        FileOutputStream fileOutput = null;
        BufferedWriter writer = null;
        try {
            fileOutput = IronBeast.getAppContext().openFileOutput(filename, Context.MODE_APPEND);
            writer = new BufferedWriter(new OutputStreamWriter(fileOutput));
            writer.write(Guard.encrypt(jsonData.toString() + MY_DELIMITER));
            Logger.log("MobileCoreReport | appendLogStackFile | appending to log | filename=" + filename, Logger.SDK_DEBUG);
        } catch (Exception e) {
            Logger.log("MobileCoreReport | appendLogStackFile | ERROR | filename=" + filename, Logger.SDK_DEBUG);
            IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError(e).send();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (fileOutput != null) {
                    fileOutput.close();
                }
            } catch (Exception ex) {
                Logger.log("MobileCoreReport | loadlogStack | Error couldn't close file | filename=" + filename, Logger.SDK_DEBUG);
            }
        }
    }

    private static String readFileString(File file) throws Exception {
        BufferedReader reader = null;
        String decryptedData = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            decryptedData = Guard.decrypt(builder.toString());
        } catch (OutOfMemoryError err) {
            Logger.log("MobileCoreReport | loadlogStackFile | OutOfMemoryError" + file.getName(), Logger.SDK_DEBUG);
            throw new Exception("OutOfMemoryError ## " + err.getMessage());
        } catch (Exception ex) {
            Logger.log("MobileCoreReport | loadlogStackFile | Exception" + ex.getMessage(), Logger.SDK_DEBUG);
            throw ex;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception ex) {
                Logger.log("MobileCoreReport | loadlogStackFile | Error couldn't close file | filename=" + file.getName(), Logger.SDK_DEBUG);
            }
        }
        return decryptedData;
    }

    private static JSONObject loadlogStackFile(String filename) throws Exception {
        JSONObject logStack = null;

        File file = IronBeast.getAppContext().getFileStreamPath(filename);
        if (!file.exists()) {
            Logger.log("MobileCoreReport | loadlogStackFile | file does not exist | filename=" + filename, Logger.SDK_DEBUG);
            // it can happen if there is no log stack file yet:
            // 1. first launch
            // 2. if we didn't have any errors so the log_stack_errors.dat was not created
            // so we don't report error here
            return logStack;
        }
        try {
            String decryptedData = readFileString(file);
            List<String> jsonObjStrList = Arrays.asList(decryptedData.split(MY_DELIMITER));
            logStack = new JSONObject();
            JSONArray jsonArr = new JSONArray();
            for (String jsonObjStr : jsonObjStrList) {
                try {
                    /* Check if jsonObjStr contains a reportArr key; If it does - obtain only its data */
                    JSONObject currentObj = new JSONObject(jsonObjStr);
                    JSONArray tempArray = currentObj.optJSONArray(REPORT_ARR);

                    if (tempArray == null) { // This is a valid JSON object that can be inserted into the array
                        jsonArr.put(currentObj);
                    } else { // This is a reportArr type of object. Obtain only the data of the array
                        for (int i = 0; i < tempArray.length(); i++) {
                            jsonArr.put(tempArray.getJSONObject(i));
                        }
                    }
                } catch (JSONException ex) {
                    /* we just skip not good report and hope it's not all reports */
                    Logger.log("MobileCoreReport | loadlogStackFile | JSONException" + filename + " msg: " + ex.getMessage(), Logger.SDK_DEBUG);
                }
            }
            logStack.put(REPORT_ARR, jsonArr);
        } catch (OutOfMemoryError err) {
            /* will catch OutOfMemoryError errors lets clear a log and pray */
            /*??? may be loop critical exception ???*/
            Logger.log("MobileCoreReport | loadlogStackFile | OutOfMemoryError" + filename, Logger.SDK_DEBUG);
            throw new Exception("OutOfMemoryError ## " + err.getMessage());
        } catch (Exception e) {
            /*??? may be loop critical exception ???*/
            Logger.log("MobileCoreReport | loadlogStackFile | Exception" + e.getMessage(), Logger.SDK_DEBUG);
            throw e;
        }

        return logStack;
    }

    private static JSONObject addToLogStack(JSONObject logStack, JSONObject jsonData) {
        JSONArray optJSONArray = new JSONArray();
        if (logStack != null) {
            optJSONArray = logStack.optJSONArray(REPORT_ARR);
        }
        Logger.log("MobileCoreReport | addToLogStack | adding to stack " + jsonData.toString(), Logger.SDK_DEBUG);
        optJSONArray.put(jsonData);
        try {
            if (logStack == null) {
                logStack = new JSONObject();
            }
            logStack.putOpt(REPORT_ARR, optJSONArray);
        } catch (Exception e) {
            Logger.log("MobileCoreReport | addToLogStack | Couldn't add json object to array " + e.getLocalizedMessage(), Logger.SDK_DEBUG);
            return null;
        }

        return logStack;
    }

    //TODO: add on send failed
    private static boolean sendReport(String url, byte[] data, String contentType, boolean encrypted) {
        HttpURLConnection con = null;
        boolean sendResult = false;
        try {
            URL host = new URL(url);
            con = (HttpURLConnection) host.openConnection();

            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-type", contentType);
            con.setConnectTimeout(IBConsts.DEFAULT_REQUEST_TIMEOUT_MS);

            //disable default on HttpURLConnection Gzip compression
            con.setRequestProperty("Accept-Encoding", "identity");
            if (encrypted) {
                con.setRequestProperty(Guard.decrypt(ReportingConsts.HEADER_KEY), Guard.decrypt(ReportingConsts.HEADER_VALUE)); // penguin = AES
            }

            //send request
            con.setFixedLengthStreamingMode(data.length);
            OutputStream out = new BufferedOutputStream(con.getOutputStream());
            out.write(data);
            out.close();

            int status = con.getResponseCode();
            Logger.log(String.format("Request status %d msg: %s", status, Network.sResponseCodesToMsgs.get(status)), Logger.SDK_DEBUG);

        } catch (MalformedURLException e) {
            IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError(e, "invalid URL").send();
            Logger.log("MalformedURLException" + e.toString(), Logger.SDK_DEBUG);
        } catch (SocketTimeoutException e) {
            IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError(e, "connection timeout").send();
            Logger.log("connection timeout" + e.toString(), Logger.SDK_DEBUG);
        } catch (IOException e) {
            IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError(e, "I/0 exception").send();
            Logger.log("I/0 exception" + e.toString(), Logger.SDK_DEBUG);
        } catch (Exception e) {
            IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError(e, "general error").send();
            Logger.log("general error http" + e.toString(), Logger.SDK_DEBUG);
        } finally {
            if (con != null) {
                con.disconnect();
                sendResult = true;
            }
            Logger.log("MobileCoreReport | sendData closed session", Logger.SDK_DEBUG);
        }
        return sendResult;
    }

    private static void deleteLogFile(String filename) {
        IronBeast.getAppContext().deleteFile(filename);
    }

    private static byte[] encryptDataUsingAES(String string) throws Exception {
        try {
            // get key
            Key myKey = getAESKey();

            // AES encrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
            cipher.init(Cipher.ENCRYPT_MODE, myKey);
            return cipher.doFinal(string.getBytes());

        } catch (Exception e) {
            IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError(e).send();
        }

        // if we got here, we failed in the encryption.
        // return null so called will know it and send non-encrypted string
        return null;

    }

    private static Key getAESKey() throws Exception {
        byte[] keyb = (Guard.decrypt(ReportingConsts.SECRET_KEY)).getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("MD5");

        byte[] theDigest = md.digest(keyb);

        SecretKeySpec secretKey = new SecretKeySpec(theDigest, ALGORITHM_AES);

        return secretKey;
    }

    private static void sendData(JSONObject jsonData, boolean bulk, boolean encrypt) throws Exception {
        Logger.log("MobileCoreReport | sendData | posting pre encrypt: " + jsonData.toString(), Logger.SDK_DEBUG);
        String jsonStr = jsonData.toString();
        byte[] bytesToSend;
        if (encrypt) {
            byte[] dataToSend = encryptDataUsingAES(jsonStr);

            boolean encryptionSucceeded = (dataToSend != null);

            if (encryptionSucceeded) {
                // encryption succeeded
                byte[] encoded = Base64Obj.encode(dataToSend, Base64Obj.DEFAULT);
                bytesToSend = encoded;

            } else {
                // we failed the encryption.
                // send non-encrypted data
                dataToSend = jsonStr.getBytes();
                bytesToSend = dataToSend;
                encrypt = false;
            }
        } else {
            bytesToSend = jsonStr.getBytes("UTF-8");
        }

        if (bytesToSend.length > 0) {
            sendReport(bulk ? IBConsts.URL_BULK_DATA_IRON_BEAST_HOST : IBConsts.URL_DEFAULT_IRON_BEAST_HOST_NAME,
                    bytesToSend, IBConsts.DEFAULT_CONTENT_TYPE, encrypt);
        } else {
            IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError("Error sending report ContentLength 0, data: " + jsonData.toString()).send();
        }
    }

    public synchronized void doReport(Context context, Intent intent) {
        try {
            mContext = context;
            if (intent.getExtras() != null) {
                EReportType type = fetchReportType(intent);
                // We want to send report
                // immediately: IRON_BEAST_REPORT isBulk = false
                // pending : ERROR, IRON_BEAST_REPORT isBulk = true

                if (type.compareTo(EReportType.REPORT_TYPE_UPDATE_CONFIG) == 0) {
                    String batchSize = getIntentString(intent, Consts.PREFS_MAX_BATCH_SIZE, "");
                    if (!TextUtils.isEmpty(batchSize)) {
                        MCUtils.saveConfig(context, Consts.PREFS_MAX_BATCH_SIZE, batchSize);
                        //TODO: update STORAGE component with new size
                    }
                } else if (type.compareTo(EReportType.REPORT_TYPE_FLUSH) == 0) {
                    //TODO: flush STORAGE component
                }

                JSONObject report = constructReport(type, intent);
                boolean isBulk = report.getBoolean(IronBeastReport.BULK);
                boolean encrypt = false;
                boolean isNetworkAvail = NetworkUtils.isNetworkAvail(context);
                if (isBulk || !isNetworkAvail) {
                    addReportToLogStack(report, type);
                } else {
                    sendData(report, isBulk, encrypt);
                }
            }
        } catch (Throwable throwable) {
            //TODO: need to revise a source
            IronBeastReportIntent errIntent = new IronBeastReportIntent(context, EReportType.REPORT_TYPE_ERROR);
            if (throwable instanceof Exception) {
                errIntent.setError((Exception) throwable);
                String error = MCUtils.formatExceptionMsg((Exception) throwable);
                Logger.log("Error sending error: " + error, Logger.CRITICAL);
            } else {
                errIntent.setError(throwable.getMessage());
            }

            try {
                EReportType type = fetchReportType(intent);
                JSONObject jsonData = constructReport(type, errIntent);
                sendData(jsonData, false, true);
            } catch (Throwable e) {
                IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError(e.getMessage()).send();
                // This is really bad couldn't send report
            }
        }
    }

    /**
     * ******** private methods ***********
     */
    private JSONObject constructIronBeastRequestBody(String tableName, String data, String auth, boolean isBulk) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.putOpt(IBConsts.IRON_BEAST_KEY_TABLE, tableName);
        requestBody.putOpt(IBConsts.IRON_BEAST_KEY_AUTH, auth);
        requestBody.putOpt(IBConsts.IRON_BEAST_KEY_DATA, data);
        requestBody.putOpt(IBConsts.IRON_BEAST_KEY_BULK, isBulk);
        return requestBody;
    }

    private String getIntentString(Intent intent, String field, String defVal) {
        String val = intent.getStringExtra(field);
        if (TextUtils.isEmpty(val)) {
            return defVal;
        } else {
            return val;
        }
    }

    private void setIfNotNull(JSONObject report, String field, Object val) {
        if (val != null) {
            try {
                report.put(field, val);
            } catch (Exception e) {
                IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError(e).send();
            }
        }
    }

    private EReportType fetchReportType(Intent intent) {
        EReportType type = EReportType.REPORT_TYPE_ERROR;
        try {
            type = EReportType.parseString(intent.getIntExtra(ReportingConsts.EXTRA_REPORT_TYPE, -1));
        } catch (Exception e) {
            IronBeastReportDataOld.openReport(EReportType.REPORT_TYPE_ERROR).setError(e).send();
        }
        return type;
    }

    private JSONObject constructReport(EReportType type, Intent intent) throws Exception {
        JSONObject report = new JSONObject();

        switch (type) {
            case REPORT_TYPE_IRON_BEAST:
                Bundle bundle = intent.getExtras();
                //Extract all data from intent extras
                JSONObject dataObject = new JSONObject();
                for (String key : bundle.keySet()) {
                    Object value = bundle.get(key);
                    dataObject.put(key, value);
                }

                //Fields have to be always exist if we here
                String tableName = (String) dataObject.remove(IronBeastReport.TABLE_NAME);
                String auth = (String) dataObject.remove(IronBeastReport.AUTH);
                boolean isBulk = (Boolean) dataObject.remove(IronBeastReport.BULK);
                //TODO: test all possible value of the objects
                report = constructIronBeastRequestBody(tableName, dataObject.toString(), auth, isBulk);

                break;
            case REPORT_TYPE_ERROR:

                report.put(ReportingConsts.REPORT_FIELD_TIMESTAMP, MCUtils.getCurrentTime());
                report.put(ReportingConsts.REPORT_FIELD_IRVER, Consts.VER);
                report.put(ReportingConsts.REPORT_FIELD_PLATFORM, ReportingConsts.REPORT_OS_ANDROID);
                report.put(ReportingConsts.REPORT_FIELD_RV, ReportingConsts.REPORT_VERSION);
                report.put(ReportingConsts.REPORT_FIELD_TOKEN, getIntentString(intent, ReportingConsts.EXTRA_TOKEN, IronBeast.getToken()));
                report.put(ReportingConsts.REPORT_FIELD_CARRIER, mContext.getPackageName());
                report.put(ReportingConsts.REPORT_FIELD_CARRIER_VER, MCUtils.getCarrierVersion(mContext));
                report.put(ReportingConsts.REPORT_FIELD_CUR_CONN, MCUtils.interpretConnection(MCUtils.getCurrentConnection(mContext)));

                setIfNotNull(report, ReportingConsts.REPORT_FIELD_BV, ExternalVars.REPLACABLE_BAMBOO_VER);

                report.put(ReportingConsts.REPORT_FIELD_OS, android.os.Build.VERSION.SDK_INT);
                String shortErr = MCUtils.getShortenedString(intent.getStringExtra(ReportingConsts.EXTRA_EXCEPTION), Consts.REPORT_MAX_ERR_FIELD_LENGTH);
                setIfNotNull(report, ReportingConsts.REPORT_FIELD_ERR, shortErr);
                report.put(IronBeastReport.BULK, true);
                break;
            default:

                break;
        }

        return report;
    }

    static class Network {
        static Map<Integer, String> sResponseCodesToMsgs;

        static {
            Map<Integer, String> map = new HashMap<>();
            map.put(IBConsts.RESPONSE_CODE_OK, IBConsts.RESPONSE_MESSAGE_OK);
            map.put(IBConsts.RESPONSE_CODE_INVALID_JSON, IBConsts.RESPONSE_MESSAGE_INVALID_JSON);
            map.put(IBConsts.RESPONSE_CODE_NO_DATA, IBConsts.RESPONSE_MESSAGE_NO_DATA);
            map.put(IBConsts.RESPONSE_CODE_AUTH_ERROR, IBConsts.RESPONSE_MESSAGE_AUTH_ERROR);
            sResponseCodesToMsgs = Collections.unmodifiableMap(map);
        }
    }
}
