package com.ironsource.mobilcore;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;

import com.ironsource.mobilcore.AdvertisingIdClient.Info;

import java.io.IOException;

class AdIdFetcher {

    private static final String GPS_AVAILABLE = "GPS_AVAIL";
    private static final String GPS_VERSION = "GPS_VER";

    /**********
     * public static methods
     **********/

    public static void fetchAdInfo(Context context, IOnAdInfoRetrieveListener listener) {

        if (listener == null) {
            // nothing to do
            return;
        }

        // try catch to never crash on getting the ad id
        try {

            boolean isMainThread = (Looper.myLooper() == Looper.getMainLooper());

            if (isMainThread) {
                // is main thread. do async
                doFetchAdInfoAsync(context, listener);
            } else {
                // is background thread. do sync
                Info adInfo = doGetAdvertisingIdInfoSync(context);
                notifyListenerAccordingToInfo(adInfo, listener);
            }

        } catch (Exception e) {
            listener.onAdInfoFetchError();
        }

    }

    private static void doFetchAdInfoAsync(final Context context, final IOnAdInfoRetrieveListener listener) {

        AsyncTask<Void, Void, Info> task = new AsyncTask<Void, Void, AdvertisingIdClient.Info>() {

            @Override
            protected Info doInBackground(Void... params) {
                return doGetAdvertisingIdInfoSync(context);
            }

            @Override
            protected void onPostExecute(Info adInfo) {
                notifyListenerAccordingToInfo(adInfo, listener);
            }
        };

        MCUtils.executeAsyncTask(task);
    }

    /***********
     * private methods
     **********/

    private static Info doGetAdvertisingIdInfoSync(Context context) {

        Info adInfo = null;

        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);

        } catch (IOException e) {
            // Unrecoverable error connecting to Google Play services (e.g.,
            // the old version of the service doesn't support getting AdvertisingId).
            reportGAIDFetchError(context, e);
        } catch (AdIdGooglePlayServicesNotAvailableException e) {
            // Google Play services is not available entirely
            reportGAIDFetchError(context, e);
        } catch (IllegalStateException e) {
            // e.printStackTrace();
            reportGAIDFetchError(context, e);
        } catch (AdIdGooglePlayServicesRepairableException e) {
            // e.printStackTrace();
            reportGAIDFetchError(context, e);
        } catch (Exception e) {
            // prevent ever crashing over getting ad id
            reportGAIDFetchError(context, e);
        }

        return adInfo;

    }

    private static void reportGAIDFetchError(Context context, Exception e) {
        boolean fetchErrorReported = MCUtils.getSharedPrefs(context).getBoolean(Consts.PREFS_AD_ID_FETCH_ERROR_REPORTED + "_" + e.getClass().toString(), false);
        MCUtils.setSharedBooleanPrefs(Consts.PREFS_AD_ID_FETCH_ERROR_REPORTED + "_" + e.getClass().toString(), true);

    }

    private static void notifyListenerAccordingToInfo(Info adInfo, IOnAdInfoRetrieveListener listener) {

        if (adInfo != null) {

            String id = adInfo.getId();
            boolean isLimitAdTrackingEnabled = adInfo.isLimitAdTrackingEnabled();

            listener.onAdInfoFetched(id, isLimitAdTrackingEnabled);

        } else {

            listener.onAdInfoFetchError();

        }
    }

    /*********
     * public interfaces
     ***********/

    public interface IOnAdInfoRetrieveListener {
        void onAdInfoFetched(String id, boolean isLimitAdTrackingEnabled);

        void onAdInfoFetchError();
    }
}
