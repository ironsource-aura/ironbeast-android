package com.ironsource.mobilcore;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class Utils {

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
}
