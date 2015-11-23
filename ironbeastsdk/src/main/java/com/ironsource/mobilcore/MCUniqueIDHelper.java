package com.ironsource.mobilcore;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.ironsource.mobilcore.AdIdFetcher.IOnAdInfoRetrieveListener;
import com.ironsource.mobilcore.ReportingConsts.EReportType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

class MCUniqueIDHelper {




	private enum EFetchAdIdState {
		FETCHING, SUCCESS, FAILURE
	}

	private Context mAppContext;

	private ArrayList<IOnUniqueIDReadyListener> mReadyListenerArr;
	private EFetchAdIdState mFetchAdIdState;

	private String mUniqueID;
	private String mUniqueIDType;
	private String mMCId;
	private String mGaid;

	private boolean mIsLimitAdTrackingEnabled = false;

	private String mDeviceIdFromTelephonyManager;
	private String mMACAddress;

	private final String UNIQUE_ID_AD_ID = "UNIQUE_ID_AD_ID";
	private final String UNIQUE_ID_UUID = "UNIQUE_ID_UUID";

	private final String UNIQUE_ID_AD_ID_SIMPLIFIED = "aid";
	private final String UNIQUE_ID_UUID_SIMPLIFIED = "uuid";
	/*************** singleton pattern ***************/

	private boolean mWasInit = false;
	private static MCUniqueIDHelper sInstance;

	/**
	 * private constructor
	 */
	private MCUniqueIDHelper(Context context) {
		initIfNeeded(context);
	}

	public static synchronized MCUniqueIDHelper getInstance(Context context) {

		if (sInstance == null) {
			sInstance = new MCUniqueIDHelper(context);

			// fetch Ad ID can fail - it must not be called from constructor.
			// this was causing a FUN stack overflow.
			sInstance.fetchAdId();
		}

		return sInstance;

	}

	private void initIfNeeded(Context context) {

		if (!mWasInit) {
			mWasInit = true;

			mAppContext = context.getApplicationContext();

			// members
			mReadyListenerArr = new ArrayList<MCUniqueIDHelper.IOnUniqueIDReadyListener>();
			mFetchAdIdState = EFetchAdIdState.FETCHING;

			mDeviceIdFromTelephonyManager = generateDeviceIdFromTelephonyManager();
			mMACAddress = generateMACAddress();

			// first set fallback unique id (for the case where we will fail getting the ad id)
			setUniqueIDAndStore();

			// fetch ad id
			Logger.log("MCUniqueIDHelper , init() | fetching ad id", Logger.SDK_DEBUG);

		}
	}

	private void fetchAdId() {

        Logger.log("MCUniqueIDHelper | fetchAdId", Logger.SDK_DEBUG);

		final long fetchAdIdStartTime = System.currentTimeMillis();

		AdIdFetcher.fetchAdInfo(mAppContext, new IOnAdInfoRetrieveListener() {

			@Override
			public void onAdInfoFetched(String id, boolean isLimitAdTrackingEnabled) {

				Logger.log("MCUniqueIDHelper | fetchAdId | id:" + id + " , isLimitAdTrackingEnabled:" + isLimitAdTrackingEnabled, Logger.SDK_DEBUG);

				mFetchAdIdState = EFetchAdIdState.SUCCESS;
				mIsLimitAdTrackingEnabled = isLimitAdTrackingEnabled;

				boolean isIdChanged = !(id.equals(mUniqueID));

				if (isIdChanged) {
					Logger.log("MCUniqueIDHelper | fetchAdId | UniqueID changed, saving the new one and reporting", Logger.SDK_DEBUG);


					// save uid and uit
					storeUniqueId(id, UNIQUE_ID_AD_ID);

					long fetchTime = System.currentTimeMillis() - fetchAdIdStartTime;

					IronBeastReportData.openReport(mAppContext, EReportType.REPORT_TYPE_EVENT)
		//					.setEventData(EventReportingConsts.COMPONENT_UNIQUE_ID, EventReportingConsts.EVENT_UNIQUE_ID_FETCH_AD_ID, EventReportingConsts.ACTION_COMMON_SUCCESS)
		//					.setAdditionalParam(EventReportingConsts.ADDITIONAL_PARAM_DURATION, String.valueOf(fetchTime))
							.send();
				}

				notifyListenersAndClear();
			}

			@Override
			public void onAdInfoFetchError() {

				Logger.log("MCUniqueIDHelper , init() , onAdInfoFetchError() | called", Logger.SDK_DEBUG);

				mFetchAdIdState = EFetchAdIdState.FAILURE;

				notifyListenersAndClear();

				// report
				long fetchTime = System.currentTimeMillis() - fetchAdIdStartTime;

				boolean fetchErrorReported = MCUtils.getSharedPrefs(mAppContext).getBoolean(Consts.PREFS_AD_ID_FETCH_ERROR_REPORTED, false);

				if (!fetchErrorReported) {

					IronBeastReportData.openReport(mAppContext, EReportType.REPORT_TYPE_EVENT)
//							.setEventData(EventReportingConsts.COMPONENT_UNIQUE_ID, EventReportingConsts.EVENT_UNIQUE_ID_FETCH_AD_ID, EventReportingConsts.ACTION_COMMON_FAILURE)
//							.setAdditionalParam(EventReportingConsts.ADDITIONAL_PARAM_DURATION, String.valueOf(fetchTime))
							.send();
					MCUtils.setSharedBooleanPrefs(Consts.PREFS_AD_ID_FETCH_ERROR_REPORTED, true);
				}
			}
		});
	}

	/************ listener related interfaces and methods ************/

	public interface IOnUniqueIDReadyListener {
		public void onUniqueIDReady();
	}

	public synchronized void addUniqueIdReadyOneTimeListener(IOnUniqueIDReadyListener listener) {

		Logger.log("MCUniqueIDHelper , addUniqueIdReadyOneTimeListener() | called", Logger.SDK_DEBUG);

		switch (mFetchAdIdState) {
		case SUCCESS:
		case FAILURE:

			listener.onUniqueIDReady();

			break;
		case FETCHING:

			mReadyListenerArr.add(listener);

			break;
		}

	}

	private synchronized void notifyListenersAndClear() {

		int numListeners = mReadyListenerArr.size();

		Logger.log("MCUniqueIDHelper , notifyListeners() | numListeners:" + numListeners, Logger.SDK_DEBUG);

		if (numListeners <= 0) {
			return;
		}

		for (IOnUniqueIDReadyListener listener : mReadyListenerArr) {
			listener.onUniqueIDReady();
		}

		mReadyListenerArr.clear();
	}

	/************ getters ************/

	public String getUniqueID() {
		return mUniqueID;
	}
	public String getMCId() {
		return mMCId;
	}
	public String getGaid() {
		return mGaid;
	}
	
	public boolean isLimitAdTrackingEnabled() {
		return mIsLimitAdTrackingEnabled;
	}

	public String getDeviceIdFromTelephonyManager() {
		return mDeviceIdFromTelephonyManager;
	}

	public String getMACAddress() {
		return mMACAddress;
	}
	
	public String getUniqueIDType() {
		return mUniqueIDType;
	}

	public String getUniqueIDTypeSimplified() {
		if (mUniqueIDType.equals(UNIQUE_ID_AD_ID)) return UNIQUE_ID_AD_ID_SIMPLIFIED;
		else if (mUniqueIDType.equals(UNIQUE_ID_UUID)) return UNIQUE_ID_UUID_SIMPLIFIED;
		else return mUniqueIDType;
	}

	/************ public methods ************/

	@SuppressLint("InlinedApi")
	private synchronized void setUniqueIDAndStore() {

		mMCId = MCUtils.getSharedPrefs(mAppContext).getString(Consts.PREFS_USER_UNIQUE_ID_MC_ID, "");

		// if the uid is already saved, return it
		String encryptedId = MCUtils.getSharedPrefs(mAppContext).getString(Consts.PREFS_USER_UNIQUE_ID, "");

		if (!TextUtils.isEmpty(encryptedId) && MCUtils.getSharedPrefs(mAppContext).getString(Consts.PREFS_AD_ID_TYPE, "").equals(UNIQUE_ID_AD_ID)) {

			mUniqueID = Guard.decrypt(encryptedId);
			mGaid = mUniqueID;

            mUniqueIDType = MCUtils.getSharedPrefs(mAppContext).getString(Consts.PREFS_AD_ID_TYPE, "");

			Logger.log("MCUniqueIDHelper , setUniqueIDAndStore() | found id in prefs. uniqueId: " + mUniqueID, Logger.SDK_DEBUG);

			// we already have the unique id. return
			return;
		}

		// no unique id in prefs. find it ourselves
		generateUniqueIdAsUUID();
	}

	private void generateUniqueIdAsUUID() {
		if (TextUtils.isEmpty(mMCId)) {
			String uniqueId = "mc_"+UUID.randomUUID().toString();
			Logger.log("MCUniqueIDHelper | generateUniqueIdAsUUID() | got UUID : " + uniqueId, Logger.SDK_DEBUG);

			// save uid and uit
			storeUniqueId(uniqueId, UNIQUE_ID_UUID);

			Logger.log("MCUniqueIDHelper | generateUniqueIdAsUUID() | Final UserID saved = " + uniqueId, Logger.SDK_DEBUG);
			return;
		}
		mUniqueID = mMCId;
		mUniqueIDType = UNIQUE_ID_AD_ID;
	}

	/************ private methods ************/

	/**
	 * returns the deviceId found in telephony manager if possible. telManager.getDeviceId(): Returns the unique device ID, for example, the IMEI for GSM and
	 * the MEID or ESN for CDMA phones. Return null if device ID is not available. returns null if it is not available / it was not found
	 */
	private String generateDeviceIdFromTelephonyManager() {

		if (!MCUtils.hasPermission(mAppContext, Manifest.permission.READ_PHONE_STATE)) {
			return null;
		}

		String uniqueId = null;

		TelephonyManager telManager = (TelephonyManager) mAppContext.getSystemService(Context.TELEPHONY_SERVICE);

		if (telManager != null) {

			uniqueId = telManager.getDeviceId();

			Logger.log("MCUniqueIDHelper , getDeviceIdFromTelephonyManager() | telephony Manager uniqueId: " + uniqueId, Logger.SDK_DEBUG);
		}

		return uniqueId;
	}

	private String generateMACAddress() {

		if (!MCUtils.hasPermission(mAppContext, Manifest.permission.ACCESS_WIFI_STATE)) {
			try {
				return getMACFromFile();
			} catch (Exception ex) {
				return "";
			}
		}

		String uniqueId = "";

		WifiManager wifiManager = (WifiManager) mAppContext.getSystemService(Context.WIFI_SERVICE);

		if (wifiManager != null) {

			uniqueId = wifiManager.getConnectionInfo().getMacAddress();

			Logger.log("MCUniqueIDHelper , getMACAddress() | got MAC address: " + uniqueId, Logger.SDK_DEBUG);
		}

		return uniqueId;
	}

	private String getMACFromFile() {

		try {
			File file = new File("/sys/class/net/");
			if (file != null && file.exists()) {

				List<String> fileList = Arrays.asList(file.list());
				if (fileList.contains("wifi0")) {
					File addressFile = new File(file.getPath() + "/wifi0/address");
					if (addressFile != null && addressFile.exists()) {
						return readFileAddress(addressFile);
					}
				} else if (fileList.contains("eth0")) {
					File addressFile = new File(file.getPath() + "/eth0/address");

					if (addressFile != null && addressFile.exists()) {
						return readFileAddress(addressFile);
					}
				} else if (fileList.contains("wlan0")) {
					File addressFile = new File(file.getPath() + "/wlan0/address");

					if (addressFile != null && addressFile.exists()) {
						return readFileAddress(addressFile);
					}
				}

			}
		} catch (Exception ex) {

		}

		return null;
	}

	private String readFileAddress(File addressFile) {

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(addressFile));
			return reader.readLine();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
			}
		}

		return null;
	}

	private synchronized void storeUniqueId(String uniqueId, String uniqueIdType) {
		Logger.log("MCUniqueIDHelper | storeUniqueId | uniqueId=" + uniqueId + " | uniqueIdType=" + uniqueIdType, Logger.SDK_DEBUG);
		Editor editor = MCUtils.getSharedPrefs(mAppContext).edit();
		editor.putString(Consts.PREFS_USER_UNIQUE_ID, Guard.encrypt(uniqueId));
		editor.putString(Consts.PREFS_AD_ID_TYPE, uniqueIdType);
		if(uniqueIdType == UNIQUE_ID_AD_ID){
			editor.putString(Consts.PREFS_USER_UNIQUE_ID_GAID,uniqueId);
			mGaid = uniqueId;
		}else{
			editor.putString(Consts.PREFS_USER_UNIQUE_ID_MC_ID,uniqueId);
			mMCId = uniqueId;
		}


        //set the uid
        mUniqueID = uniqueId;

		//set the uit
		mUniqueIDType = uniqueIdType;
		editor.apply();
	}

}
