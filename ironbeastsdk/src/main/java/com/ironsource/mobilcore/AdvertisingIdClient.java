package com.ironsource.mobilcore;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.util.Log;

final class AdvertisingIdClient {
	private static AdIdServiceConnection g(Context paramContext) throws IOException, AdIdGooglePlayServicesNotAvailableException, AdIdGooglePlayServicesRepairableException {
		try {
			PackageManager localPackageManager = paramContext.getPackageManager();
			localPackageManager.getPackageInfo("com.android.vending", 0);
		} catch (PackageManager.NameNotFoundException localNameNotFoundException) {
			throw new AdIdGooglePlayServicesNotAvailableException(9);
		}
		/*
		 * try
		 * {
		 * GooglePlayServicesUtil.m(paramContext);
		 * }
		 * catch (GooglePlayServicesNotAvailableException localGooglePlayServicesNotAvailableException)
		 * {
		 * throw new IOException(localGooglePlayServicesNotAvailableException);
		 * }
		 */

		AdIdServiceConnection localA = new AdIdServiceConnection();
		Intent localIntent = new Intent("com.google.android.gms.ads.identifier.service.START");
		localIntent.setPackage("com.google.android.gms");
		if (paramContext.bindService(localIntent, localA, 1)) {
			return localA;
		}
		throw new IOException("Connection failure");
	}

	public static Info getAdvertisingIdInfo(Context context) throws IOException, IllegalStateException, AdIdGooglePlayServicesNotAvailableException, AdIdGooglePlayServicesRepairableException {

		AdIdServiceConnection localA = g(context);
		try {
			AdIdBinderHolder localP = AdIdBinderHolder.a.b(localA.aG());
			Info localInfo = new Info(localP.getId(), localP.a(true));
			return localInfo;
		} catch (RemoteException localRemoteException1) {
			Log.i("AdvertisingIdClient", "GMS remote exception ", localRemoteException1);
			throw new IOException("Remote exception");
		} catch (InterruptedException localInterruptedException1) {
			throw new IOException("Interrupted exception");
		} finally {
			context.unbindService(localA);
		}
	}

	public static final class Info {
		private final String dX;
		private final boolean dY;

		Info(String advertisingId, boolean limitAdTrackingEnabled) {
			this.dX = advertisingId;
			this.dY = limitAdTrackingEnabled;
		}

		public String getId() {
			return this.dX;
		}

		public boolean isLimitAdTrackingEnabled() {
			return this.dY;
		}
	}
}