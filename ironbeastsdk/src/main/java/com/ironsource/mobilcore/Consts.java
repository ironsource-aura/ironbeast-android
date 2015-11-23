package com.ironsource.mobilcore;

import android.content.Intent;

class Consts {

	protected static final String VER = Config.VER;

	protected static final String SHARED_PREFS_NAME_HASH = "s#ges#gd1%ds#gos#gcs#ghss#gas#gh";
	protected static final String SHARED_PREFS_NAME = "1%dss#gfs#ge1%dr1%dps#g_s#gds#ge1%drs#gas#ghs#gSs#g_s#ge1%dr1%dos#gCs#ge1%dls#gis#gb1%do1%dm";
	protected static final String INST_TRACKER_SHARED_PREFS_NAME = "inst_tracker_spref_file_name";
	protected static final String PREFS_USER_UNIQUE_ID = "s#gDs#gI1%drs#ge1%ds1%dus#gLs#gSs#gI";
	protected static final String PREFS_ACCOUNT_NAME = "s#ge1%dms#ga1%dns#g_1%dt1%dn1%du1%dos#gcs#gcs#gas#g_1%dss#gfs#ge1%dr1%dp";
	protected static final String PREFS_TOKEN = "1%dns#ge1%dk1%do1%dts#g_1%dss#gfs#ge1%dr1%dp";
	
	protected static final String PREFS_AD_ID_FETCH_ERROR_REPORTED = "ad_id_error_reported";
	protected static final String PREFS_AD_ID_TYPE = "ad_id_type";
	protected static final String PREFS_USER_UNIQUE_ID_GAID = "uid_gaid";
	protected static final String PREFS_USER_UNIQUE_ID_MC_ID = "uid_mcid";

	protected static final class AdFields {
		public static final String FIELD_ID = "id";
		public static final String FIELD_IMG = "img";
		public static final String FIELD_EXTRA = "extra";
		public static final String FIELD_ADS = "ads";
		public static final String FIELD_EXPIRATIONS = "expirations";
		public static final String FIELD_EXPIRATION_SOFT = "soft_expiration";
		public static final String FIELD_EXPIRATION_HARD = "hard_expiration";
		public static final String FIELD_EXPIRATION_MEDIA = "media_expiration";
		public static final String FIELD_EXTRA_TYPE = "type";
		public static final String FIELD_EXTRA_IMG_TYPE = "imgtype";
		public static final String FIELD_EXTRA_NAME = "name";
		public static final String FIELD_EXTRA_VALUE = "value";
		public static final String FIELD_COVER_IMG = "cover_img";
		public static final String FIELD_IMPRESSION = "impression";
		public static final String FIELD_CPI = "cpi";
		public static final String FIELD_CLICK = "click";
		public static final String FIELD_TYPE = "type";
		public static final String FIELD_AFF = "aff";
		public static final String FIELD_TITLE = "title";
		public static final String FIELD_DESC = "desc";
		public static final String FIELD_APP_ID = "appId";
		public static final String FIELD_ITEM_INDEX = "index";
		public static final String FIELD_PREPARE_CLICK = "bc";
	}

	// port
	protected static final String REPORT_SERVER = "1%dm1%dos#gcs#g.s#gi1%dn1%do1%dss#gi1%dn1%do1%dms#ge1%dys#g.1%dn1%dos#gds#gis#ge1%ds1%do1%dps#g/s#g/s#g:1%dp1%dt1%dts#gh"; 
protected static final String TRACKED_INSTALLATION_PREFIX = "tracked_package_";
	protected static final String EXTRA_PACKAGE_NAME = "s#ge1%dms#ga1%dns#g_s#ges#ggs#ga1%dks#gcs#ga1%dps#g_s#ga1%dr1%dt1%dxs#ge";

	protected static final String EXTRA_SERVICE_TYPE = "extra_service_type";
    protected static final String HEADER_IF_NON_MATCHED = "If-None-Match";
    protected static final String HEADER_ETAG = "Etag";
	protected static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
	protected static final String HEADER_LAST_MODIFIED = "Last_Modified";
	protected static final String PREFS_READY_EVENT_NEEDED = "PREFS_READY_EVENT_NEEDED_";

	protected enum EServiceType {

		SERVICE_TYPE_REPORT,
		SERVICE_TYPE_APK_DOWNLOAD,
		SERVICE_TYPE_SEND_REPORTS;

		public static EServiceType parse(int value) {
			for (EServiceType v : values())
				if (value == v.ordinal())
					return v;
			throw new IllegalArgumentException();
		}

		public void setValue(String name, Intent intent) {
			intent.putExtra(name, ordinal());
		}

		public static EServiceType getValue(String name, Intent intent) {
			if (!intent.hasExtra(name))
				throw new IllegalStateException();
			return values()[intent.getIntExtra(name, -1)];
		}
	}

	protected static final String ACTION_TRACK_INSTALL = "track_install";

	protected static final int CONST_INST_CHECK_TIME = 0;

	protected static final String PREFS_FIRST_RUN = "prefs_first_run";
	protected static final String EXTRA_DOWNLOAD_URL = "com.ironsource.mobilcore.extra_download_url";
	protected static final String EXTRA_DOWNLOAD_FILENAME = "com.ironsource.mobilcore.extra_download_filename";
	protected static final String EXTRA_DOWNLOAD_APPNAME = "com.ironsource.mobilcore.extra_download_appname";
	protected static final String EXTRA_DOWNLOAD_APP_IMG_NAME = "com.ironsource.mobilcore.extra_download_app_img_name";
	protected static final String EXTRA_DOWNLOAD_APP_PACKAGE = "com.ironsource.mobilcore.extra_download_pkgname";

	protected static final String PREFS_ALARM_ID = "com.ironsource.mobilecore.prefs_alarm_id";
	protected static final String PREFS_TRACKER_ID = "com.ironsource.mobilecore.prefs_tracker_id";

	// strings to display
	protected static final String FAILED_TO_OPEN_URL = "Failed to open url";

	protected class ExtraType {
		public static final String EXTRA_TYPE_IMG = "img";
		public static final String EXTRA_TYPE_AUDIO = "audio";
		public static final String EXTRA_TYPE_VIDEO = "video";
		public static final String EXTRA_TYPE_FILE = "file";
		public static final String EXTRA_TYPE_REPORT = "report";
	}

	// app installs
	protected static final String APP_ALREADY_INSTALLED = "Application already installed";
    protected static final String APK_ALREADY_DOWNLOADING_TOAST_TEXT = "Already downloading this offer";
    protected static final String APK_FAILED_TO_DOWNLOAD_TOAST_TEXT = "Failed to download apk";
    protected static final String APK_DOWNLOAD_TOAST_TEXT = "The app will be downloaded to your device shortly";
    protected static final String DOWNLOAD_IN_PROGRESS = "Download In Progress...";
	
    // base 64 assets

	protected static final String CLOSE_ICON_IMG = "iVBORw0KGgoAAAANSUhEUgAAAEYAAABGCAMAAABG8BK2AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA2RpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYwIDYxLjEzNDc3NywgMjAxMC8wMi8xMi0xNzozMjowMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDo1RDVCOTE2MjkyNURFMzExOEUxRUI5ODgyM0IyQjAxQyIgeG1wTU06RG9jdW1lbnRJRD0ieG1wLmRpZDoyNjFENDM2NzVEOUYxMUUzQkY2N0I5QTU1MzNBQUZDRSIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDoyNjFENDM2NjVEOUYxMUUzQkY2N0I5QTU1MzNBQUZDRSIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ1M1IFdpbmRvd3MiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo1RTVCOTE2MjkyNURFMzExOEUxRUI5ODgyM0IyQjAxQyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo1RDVCOTE2MjkyNURFMzExOEUxRUI5ODgyM0IyQjAxQyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PusLT/cAAAE+UExURf///2ZmZmVlZVxcXGRkZGFhYWNjY11dXYKCgmdnZ3BwcPv7+2BgYNjY2F5eXv7+/vz8/Gtra6SkpGpqaqGhoWJiYubm5pycnK2trZWVlf39/V9fX/j4+GhoaH9/f46Ojs/Pz8XFxW9vb+np6ff394yMjIGBgbi4uKWlpZ2dnVtbW9zc3Ovr6/Ly8tDQ0PX19ZiYmJ+fn+rq6vb29tnZ2XZ2duDg4OLi4u7u7nR0dOTk5KamppaWlufn55mZmfPz87S0tImJiaenp6ysrM7OzmlpadHR0ZeXl3V1ddvb27Ozs6CgoKKiotLS0m1tbW5ublpaWtXV1fn5+VlZWWxsbPHx8cvLy9TU1H5+foeHh9fX1/T09JqampCQkPDw8HFxcXd3d3Nzc/r6+pubm9PT08rKyqurq4uLi5GRkf///2B65lQAAABqdFJOU////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////wC85NzjAAADMUlEQVR42uyXZ3/aMBCHdR7yBFNDGA57EzIge7XZu83u3rv9/l+gsg3ENl751S+5N2AkPZxO+t+d0d8wJqVb+zl+RRRX+Nx+Ky2NTUAhIJvZZhIslmxmNx+NiW0bDIrBrCyzmKEM0nbsUZinHYGsYmQaH4l8o8GLR5iWGfKT0HkaHlPZICswJ/C7h7UlSVGkpdrhLi9wmPy8UQmJyad0T7iquppAFkusqlVO9yiVD4OZK5Gp06DOojGbVckAQGkuGLOzRwJLF5eRqy0XaRLsvZ0gTJ74wsQzC8jDFjJxsrFSPgBD4kLFs8jHsnHiT8ofUwmkDDgVP8yiCEBnUIBlaABx0QeTIWdUrOtT5/qlrnP5ze92Xv+sF8l5ZbwxXQEYONNnfnp7R9/H7JRjgb77Y8T+jEwTup6YHADXNq7ax2sA9omNcwxTANffNf17mwPIeWHSBcDVdX3a0qV+722csk4B9uut/rBexVBIe2AOiDOqsUZLzYCdMy/oFGrml/moEncO3DFKEyhhzZz2rceZnMFtfmH4QsUvNPN5TaCgqbhi0kmQeW0oxBPa4s88ZVDo/tA5jZchmXbFtMidOR/FQnr2sK+yuSM6pYyGz8m/tFwxW0DhMrJwzH3dd28EVqdwKc0ScUzBFnI/bqZQsyaYE4MzdZU0fOH61uOvFRjIuWJ4wGLdlqh6RnwYxtjRhe0u1kUMvCvmFFjelu+Q9IEelAWKfq7YhhI8C6euGBHkhkNFUgebFNxWHEMNGcSwGFSeosw6E0NhMeObQuWC6Q3gSwfHe1NjIR4oAADGdOoTYueBEwWwBoF14XgfuOP6oXlTR5za4cY53tfPLgaiANY8aaQNdfEOhRCDTZokSw0UQE5a6jn98ZGmNVGYuW6kALve/ROFJW0N4zLSdKJnz2M+acuSRG+/TJtx0R70bvgTL0qBSfQhpedXZD3X9S0KMOMz/UYKTOmWAvOKYhmHphMnM4wsvA5RYCzl7ucVONUodZjPP8KUO2vxfenS3yy+D1d8I2oFompMomqTImraomoho2poo2qvI2v2o3r1iOxFKLLXshA2wUwwE8wEM8H8h/0TYACZQeeCp+VpcAAAAABJRU5ErkJggg==";
	protected static final String CLOSE_ICON_BLACK_IMG = "iVBORw0KGgoAAAANSUhEUgAAADYAAAA2CAMAAAC7m5rvAAAAmVBMVEUAAAD///////////////////////////////////////////////////////////9CPj1IREQnHhs0LSxLSEhFQkEqIR9AOzoiFxQ3MS86MzIlGxguJSMyKik9NzYwKCbY19ZPTEwtJCE8NjR/enlwbWzz8/OIhYVmYV+Qi4qem5u0srFaV1bOzc3IxsW9u7vm5eWqp6fd3d3TE7E0AAAAD3RSTlMAJIpkWNJtAZ73MT4Hps2ZA6zKAAACVklEQVRIx6WW65aaMBCAdRUBdTfSxbZaV2PlJlCUff+Hq8OczMSYCOf4/TKZfIfJxWRGFvz5LPBEhxfM5v5oAP4iEAbBos9cTkJhIZwsn31pIpxMnF8ce0IhqzZPkqRsm6vq8sZ2a6oGXPL6yBTlt0CmtgTfMJa15BDpBWNv/oP1jpGqjm2kUgDvvmHhql+T2EVp8zDD7zp2k2Zdno+rcYmegt6UrTFb/d6YJvbRZXiM+ijA+/D1FLN63c9ZS3MpgGQ9hAaG4vmcwc9/q0HUkOasm1kIWqrF0tLdgu0LYXYL82P5rZ0bLSKG0YubFnQz+0XkAsiNFlHB//aWY3eouLsQSE4WUFM8gaY/mncHmLVUkMeWSHkALMoc1zHZE5Ekb0+W3DMXWEuc2nFv8yqyIi1edpPzoP+ks1YeWWs9DLPwRiEs//YO5ZF1F61h50aCNGYldWtlRKETtY0BeGSZwQeNOZN23ri1T4NEMIkZ7LQQEnm03F4BS4Ib4LbY0zUPtzv+0iCLPT2M242Hy2bJSNo8PFx4lA9ExOfwsFdeFvEAPMp+1829hbJOt8ZJeQXFU2j66m+6U2wlWtu71o6AHAN1KTQ7zWOLW4p1dynQFVT8JTZSNBujRVTqCsK1lD8HcYSxM+16PQ/SpLpe6b05/uiHLnN+Oq6fvVYh6Ongh0r2eTE/VHqa8uvPM9CaPj7CMn5ipVgs2AqFLPnt4NDSk2/xRHOyWumVLHs5I9rIInE54y6eZKKbRZkJZNpbqmVNewYqKbhUe6EwfKEMfa3odZfYoavE/g+lYcB2DuoRYAAAAABJRU5ErkJggg==";
	protected static final String MUTE_BTN_IMG = "iVBORw0KGgoAAAANSUhEUgAAADYAAAA2CAMAAAC7m5rvAAAB2lBMVEUDAwMAAAAAAAAAAAD///8YGBj///8SEhIhISFvb28ODg4aGhoJCQkfHx8jIyP///////8UFBT+/v7///8LCwv////j4+MdHR3///9PT0/v7+89PT3x8fH7+/v4+PgmJib9/f38/PxbW1vQ0NB0dHTLy8v+/v7FxcXR0dEoKCj///+wsLCIiIj////////////////////////////////////9/f3MzMz9/f3Ly8v9/f3Y2NiRkZFlZWVxcXFRUVH////////////////////////////8/Pz////7+/v+/v74+Pj29vbo6Ojw8PDl5eXg4ODo6Ojt7e3k5OTW1tbJycnb29tGRka9vb3CwsKoqKgxMTFubm5bW1tUVFQ4ODj////////////////9/f3+/v75+fns7OyqqqrKysrh4eHr6+teXl7y8vJ3d3dycnLU1NTBwcHMzMy6urq1tbWdnZ2YmJiDg4N2dnZfX1+EhIR7e3tKSkr////////////////9/f339/f19fW6urr4+Pju7u7g4ODX19fR0dHNzc2Kiory8vLv7+9kZGTg4OBPT0+ampqgoKBLS0tLS0tBQUH////+/v7////8/Pz4+Pjy8vL////+/v5PWfOSAAAAnHRSTlPMAMzKA8wBzMzTzMzMzczZ1szY08y9D8ymyRYD+9nYzUnc1NPT0tHPzs3Ly8vKyMGrmYt8eGM8Cf7u1dTMysnJxbi1gHVva2BbTkYuIPjZ2NbU09HQ0M/OzczLy8rKycmblIVlQjEoEQPp2NbV1NTT0M/OzcvKysrKysnJyLqunlY1HRkF/vr08O7u2tnT0tDPysnJyMiRdGdVJyffhUC/AAAEKklEQVRIx53WBXfaUBQH8Jv7CMkCCQ6lHYyuW2113Wqrrzb3us/d3d3d4LvuPuhWXmQ9Z/ccILzkx//m5pwQkMzlduU+i1pbW4tWVtzmg8zMxVHhyOD8vbuRSKRutq+5JZpdNjELejRUXxsq3lNysKysbHPJ7kRppCFFkhKdGKHlxlrfniPX44wxAKD3eFXZKX/vYCHttWdut3SsqTpZNkkAQVcNw1BjiAhK1flgpPlxfiDkR6XuBI9uB/QasuKFXIUV2SDZdd5XMJoXCKvqeFPoXRegKhMRKhyLIXScqd6x6uCvOjbru8RQDYBdyR6cKfc3StLxfEaqsK7tKqAOTqUiXPY10JGrjH4jWtd2E1UNHRnKGmzhzv2X0Va9/yZuAFLOTgmQ619lFNzvu0Lq34WaDEdDw9k2IasWQ0dB98JaTgF2qHaMO+AtRnv3M1TQ5kCvJnyN4VRyjgAxwgO+W6jbKUUDYUpo4Lh/hATQ62H3RdDBRgUAK0QXBra3oEhyc/Yt0YmyVfG1Ezvfg5K/qOMHf0pyAZ1ZTzno9mpbOn2OCb/opbh6SnNJw/5r4HFSmXUiAxW2VP+UKK3hlAc1C8uqTDptZgreCDbTSFp7DoJhGrthVDAkZWW8yz1zxJYqO8AjKmDA8CVXVkYRFyJRkO4Hp1AYlgaHTzx/8YzOy65JiuioXAKpf9cMasI6O5vOlS1TYKptGKSGdqZ4hWGwTenMVhJUdgzjvmaQ6ktYTOyCGOU4pWk4sasJigo2M9XKnJv0IjvZ58Sc08LIdjf+L5O+HmCyaSTnMlTO5/YlOQBS30mGYZsLkHFgCk4Hh0D6XhwXL3eAbdz5dOcTMrYsAJOliyCNhCZpM68wjPrhiXXr01ttmQrjNWMgjXVvoU3R0Rs7zZ0N06GkrggkqaBdnAmVpmgVWWdlGlbs6pPAJQ0mp1EBc+EGtnF9OmNhHqwKpTgbreRdOjgL06G8J5q9vd7byzBs4yq4eyuyAE4n+iUXv3Ol/FdQRfu8Vwqa5nike4wzNw1ln4Ya2DidMUGhBzuDjZSUvSu3hC6DgXZOFk/aK8OB2oc5RnnzwVvWNu26xo/+HaSIZf+Ae/dNYAzXUip2JeophrOse1B5gFHja6n4mzuFPIxYzv3wlzNTnlV59tcscbXCeO4O7lRn5DUwvr+mhVQ+Ixc6dBjVgAOLBfD6vm6u8hl3wzVnqqgVxQbJOsJ4W+8oKZHxx8jlOt/F7YC6RxOulabLCF2H/HN8GmbG59I6UJM8QhB1VVayFfAYZFjnQV/kPj/CyrKrv+ark5uvzTA+BF4AwD5dbQ9GBsQnQzA/7y439YRel1yquhG//fn29s6OC+2J0rsLjwREzPrMG11sKqitLA0WFxcnSqsjs4Mtj3M7nNmf/YUPUkMLzQtDI6PRlUWxfgNirtuW28oXKQAAAABJRU5ErkJggg==";
	protected static final String SOUND_BTN_IMG = "iVBORw0KGgoAAAANSUhEUgAAADYAAAA2CAMAAAC7m5rvAAACLlBMVEUCAgIAAAAAAAD+/v4hISEHBwfPz88eHh4YGBgTExMREREKCgokJCQODg77+/v///8AAAD8/Pz////l5eX////8/PxWVlYcHBxRUVHu7u7c3Nz////6+vrm5uaHh4f////+/v4mJib////////////////9/f3w8PDl5eX9/f1RUVHOzs5CQkLFxcXCwsI5OTn////////////09PTGxsb5+fnY2NjT09OMjIyHh4fx8fHKyspTU1MtLS2wsLCRkZGHh4dzc3NlZWVKSkr////////////////////////////7+/v+/v78/Pz29vb39/f09PTx8fHX19fKysqnp6f4+Ph5eXnr6+vg4OBxcXFsbGzQ0NDu7u7X19dcXFxAQEDk5OTb29vLy8u8vLzS0tK2trZvb2+enp6YmJh6enpbW1tVVVUxMTGEhIROTk7////////////////////////////////8/Pz9/f3////5+fnq6urz8/Pq6urt7e3i4uLg4ODi4uLPz8/Dw8Ovr6+0tLSSkpLx8fH////29vbh4eFjY2NmZmZVVVXU1NTX19czMzOrq6sqKipfX1+mpqY4ODj////////////////////////+/v79/f319fW6urqZmZmZmZl/f3/Y2Nj7+/vU1NTLy8tLS0vg4ODGxsa+vr6Dg4NtbW1FRUWLi4tBQUH////////////6+vr5+fny8vL29vbR0dH////9/f1DWwIYAAAAuHRSTlPMAMoCzMwBzczMzMzNzP7VyNnYD8v+0MzK+fO92dbW09HMxrimYkkW9tzT0dDPzs6ZfW8gCP7z7tnY1tTPzszMy8rKyMK1k4t1ZltORj0u/f387uzk2NjW1tbV1NPT0tHQz87NzMzLysrKysrKycnIv6udgHl3a2BCMicT+fj19fXw6unk493a2djX1dTT0c7Ozc3LysmurKSbh4NVNxnl397X1tXV1NLQ0M3KysrIyMSNWTEqJxwLwEKMqgAABMdJREFUSMeN1odf00AUB/BfrkmTNKmd2kpBNogKIuDeygZx77333nvvvffee+v53/kuAhcbQB8faHjJl3d5lzsCJT38vtbPqqoqf2vGn35ROvMJVNy8486WG6lUavOWrYUTYk46jXnQg8by6pzMWX2Hl5aWDp+bFclJ1RWRlBU9jNCeAZuis5au3sAYRDCWHFM6M1Szo5jOdsz8fmXvtvjp0hVCaLpq27aqCxsYcyGSKvS5C8Jdqig/WpYkYhsBSwOEtgKGDWDFhWhBi6sgpPJtC70aC6iGhb/DSujA6GHxndKhXe29HV3KoJroIDTDwPVloQHtDm2q+GZkFaCjk9BUYPmpz20OrSq2ObIeagCdhxHEqGgdda6d0VF5aD1yNXQVAZNcvWRUrD66ErkAunYGyrKbnGHCUbuyy6Br3r+eFpRYXF0pHMQQYzWLgPSLDLBguktgzfRaAsQIV0RXe3qos0sv4XE2roWaSYC+Kz8t8SibLTzYs7/jgq7RWxrLKvArfsHqM8fB8KjenPcQIzctw1VUx6pQkeID3Vl+3/R+qKxfb/6r2whiBAHpLLCscqrmU5pCb93FNA22ozixkVfY0P1T3U7FqPgeharVzTRc6aCtjnQUsR6MIXyc86NMjsbE1UghteRn/mvYUoGxsFD01b3/0IyhrN9Bzocy3dWUWbXEdsdHyzEa7NyUKYP3kxFs5CA+aU747CSeAbmabJSlYlC+RNfAlL0YwrkgDrv0qBufgvBD3m2GLGdgdHw3lPqT1xGUk9yL7koo0ZIF4am82+zw7Il8GlPlza151gSl7ikzLVmNmEAO6xGeM5AEDvDBsikBbCgphFI+V6bSGVP38SMMGfywvCaIdcO2w18wHGpnDG4mJ/zM1q4ZOztRDlKyrAHw33qB9pSnJUf5rxnhc5P4MdESN1NqnzND63gC+g8ZyDPEc9L9iZyAAD5Or4By9wyDJad7TkbGoMlkBMsdxAfOCPeZzA/BtCRbW9IIpSGyAQHXw0WxoGfrw3Xi0GOmD+L8hCwGEytydkFpzn5Hh9IlzFx2XjjREiB8jPOpLKG5lsDlvEoolXmj6FCGZWm2cM7CyR3JhkweDNdwNB19b/qhKAXzqSfpi7sPuYFimQax0K3o9yvDtiq0THdkrhV5r+tOm4IgmvusgTGhIsFa4pehogM3sT8ItDVRjjE/5myvW7IYrA7cbG+Wyq/N3K74xM5VFFrpLQcdLAhPqHiTV0lM7LG3ZgbgvcRMeJWBcZEBzvZKPyZkL4eN/wjLwMXqH38Y1bsbWg31P1guVkZ3khKMPmI189Yh8U+lYmxmOZURzHH34xcZjH+q5Lz84vb/b8LdCy1j0P+hEovyvgrVxqhuIbmu7s+ykVy0cQIpN/MrO7MXv4dqdqISJsbOy3OUZI5ryhv2AQQ1j9EMHbgWqWmRrzOu18hvm08tSQK6EdTcJqAbwNjFodq9pNKZ6EtVxcbpSwlCVw0zIMI0bDIYtySauieu8DIn+/3OxtN9169jkMHGr5of2VRRLJVk8n13z/b87JNzl4+5mhw/fnxy3Oiy+Zk5NxoeuJFg3nfe2K5tBdXxnEhJSUkkJ77pdsX9KudEF6ztfPHuosaGwobG5paYTLriN/xrH/Hv5th8AAAAAElFTkSuQmCC";

	protected static final String FALLBACK_ICON = "iVBORw0KGgoAAAANSUhEUgAAAGQAAABkCAYAAABw4pVUAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyBpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYwIDYxLjEzNDc3NywgMjAxMC8wMi8xMi0xNzozMjowMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNSBXaW5kb3dzIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjFGRDQ5OTMzNUQ5RDExRTNCQ0I0Q0U0QUU0NUM0OTJGIiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOjFGRDQ5OTM0NUQ5RDExRTNCQ0I0Q0U0QUU0NUM0OTJGIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6MUZENDk5MzE1RDlEMTFFM0JDQjRDRTRBRTQ1QzQ5MkYiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6MUZENDk5MzI1RDlEMTFFM0JDQjRDRTRBRTQ1QzQ5MkYiLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz5gcz1cAAAQUUlEQVR42uyde3BU133H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";

	protected static final int NO_EXPIRATION = -1;

    //content descriptions values
	protected static final String CONT_DESC_STICKEEZ_CONTAINER = "stickeez-container";
	protected static final String CONT_DESC_STICKEEZ_X = "stickeez-x";
	protected static final String CONT_DESC_STICKEEZ_HANDLE = "stickeez-handle";
	protected static final String CONT_DESC_STICKEEZ_HANDLE_CLICKABLE_AREA = "stickeez-handle-clickable-area";
	protected static final String CONT_DESC_STICKEEZ_BANNER_APP_TITLE = "stickeez-banner-app-title";
	protected static final String CONT_DESC_STICKEEZ_BANNER_APP_DESCRIPTION = "stickeez-banner-app-description";
	protected static final String CONT_DESC_STICKEEZ_BANNER_X = "stickeez-banner-x";
    
	protected static final String CONT_DESC_INTERSTITIAL_HOLDER = "offerwall-holder";
    protected static final String CONT_DESC_INTERSTITIAL_WEBVIEW = "offerwall-webview-";
    protected static final String CONT_DESC_INTERSTITIAL_MAIN_WEBVIEW = "offerwall-webview-main";

	protected static final String PREFS_API_CALLED_KEY = "com.mobilecore.PREFS_API_CALLED_KEY"; 

	protected static final int REPORT_MAX_ERR_FIELD_LENGTH = 256;
	
	protected static final long MAX_APK_AGE = 30 * 1000; // 30 sec
	
	protected static final String VIDEO_DATA = "data";
	protected static final String VIDEO_AD_CONFIG_JSON_STR = "ad_config";
	protected static final String VIDEO_AD_JSON_STR = "ad_offer_data";
	protected static final String VIDEO_AD_CONFIG_FORCE_HORIZONTAL = "force_horizontal";
	protected static final String VIDEO_AD_CONFIG_CLICK_SENDS_TO_PLAY = "click_sends_to_play";
	protected static final String VIDEO_AD_CONFIG_BLOCK_BACK_BUTTON = "block_back_button";
	protected static final String VIDEO_AD_CONFIG_IS_REPLAY = "isReplay";
	protected static final String VIDEO_AD_CONFIG_ALLOW_SKIP_AFTER = "allow_skip_after";
	protected static final String VIDEO_AD_CONFIG_AUTO_PLAY = "auto_play";
	protected static final String VIDEO_AD_CONFIG_FILE_NAME = "ad_video_file_name";
	protected static final String VIDEO_AD_CONFIG_CLICK_CALLBACK = "clickCallback";
    protected static final String VIDEO_AD_CONFIG_CLOSE_AFTER_CLICK_CALLBACK = "closeAfterClickCallback";
	protected static final String VIDEO_AD_CONFIG_REPORT_IMPRESSIONS_CALLBACK = "reportImpressionsCallback";
	protected static final String VIDEO_AD_CONFIG_SHOW_PROGRESS = "show_progress";
	protected static final String VIDEO_CACHE_NUM = "cache_num";

    protected static final String VIDEO_EXTRA_TRIGGER = "trigger";
	protected static final String PREFS_PLUGIN_PARAM = "com.ironsource.mobilcore.Consts.PREFS_PLUGIN_PARAM";
	protected static final String PLUGIN_KEY = "plugin";
	
	protected static final String PREFS_MEDIATION_PARAM = "com.ironsource.mobilcore.Consts.PREFS_MEDIATION_PARAM";
	protected static final String MEDIATION_KEY = "mediation";

	protected static final String PREFS_FLOW_FILE_VERSION = "com.ironsource.mobilcore.Consts.PREFS_FLOW_FILE_VERSION_";
	protected static final String OS_ANDROID = "android";

	protected enum EOfferType {

		OFFER_TYPE_MARKET("Market"), OFFER_TYPE_APK_DOWNLOAD("ApkDownload"), OFFER_TYPE_CPC("CPC"), OFFER_TYPE_DEEPLINK("DeepLink");

		private String mOfferTypeStr;

		EOfferType(String offerTypeStr) {
			mOfferTypeStr = offerTypeStr;
		}

		public String getActionStr() {
			return mOfferTypeStr;
		}

		public static EOfferType parseString(String value) {
			if (value == null)
				throw new IllegalArgumentException();
			for (EOfferType v : values())
				if (value.equalsIgnoreCase(v.getActionStr()))
					return v;
			throw new IllegalArgumentException();
		}

	}

}
