package com.geatte.android.app;


import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Register/unregister with the Chrome to Phone App Engine server.
 */
public class DeviceRegistrar {
    public static final String STATUS_EXTRA = "Status";
    public static final int REGISTERED_STATUS = 1;
    public static final int AUTH_ERROR_STATUS = 2;
    public static final int UNREGISTERED_STATUS = 3;
    public static final int ERROR_STATUS = 4;

    private static final String REGISTER_PATH = "/register";
    private static final String UNREGISTER_PATH = "/unregister";

    /*public static void registerWithServer(final Context context,
	    final String deviceRegistrationID) {
	new Thread(new Runnable() {
	    public void run() {
		Intent updateUIIntent = new Intent("com.google.ctp.UPDATE_UI");
		try {
		    HttpResponse res = makeRequest(context, deviceRegistrationID, REGISTER_PATH);
		    if (res.getStatusLine().getStatusCode() == 200) {
			final SharedPreferences prefs = context.getSharedPreferences(
				Config.PREFERENCE_KEY,
				Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("deviceRegistrationID", deviceRegistrationID);
			editor.commit();
			updateUIIntent.putExtra(STATUS_EXTRA, REGISTERED_STATUS);
		    } else if (res.getStatusLine().getStatusCode() == 400) {
			updateUIIntent.putExtra(STATUS_EXTRA, AUTH_ERROR_STATUS);
		    } else {
			Log.w(TAG, "Registration error " +
				String.valueOf(res.getStatusLine().getStatusCode()));
			updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
		    }
		    context.sendBroadcast(updateUIIntent);
		} catch (AppEngineClient.PendingAuthException pae) {
		    // Ignore - we'll reregister later
		} catch (Exception e) {
		    Log.w(TAG, "Registration error " + e.getMessage());
		    updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
		    context.sendBroadcast(updateUIIntent);
		}
	    }
	}).start();
    }*/

    public static void registerWithServer(final Context context,
	    final String registrationId) {
	new Thread(new Runnable() {
	    public void run() {
		Intent updateUIIntent = new Intent(Config.INTENT_ACTION_UPDATE_UI);
		try {
		    if (registrationId != null && registrationId.length() > 0) {
			HttpResponse res = makeRequest(context, registrationId, REGISTER_PATH);
			if (res.getStatusLine().getStatusCode() == 200) {
			    final SharedPreferences prefs = context.getSharedPreferences(
				    Config.PREFERENCE_KEY,
				    Context.MODE_PRIVATE);
			    SharedPreferences.Editor editor = prefs.edit();
			    editor.putString(Config.PREF_REGISTRATION_ID, registrationId);
			    editor.commit();
			    updateUIIntent.putExtra(STATUS_EXTRA, REGISTERED_STATUS);
			    Log.d(Config.LOGTAG_C2DM, "Registration OK " +
				    String.valueOf(res.getStatusLine().getStatusCode()));
			} else if (res.getStatusLine().getStatusCode() == 400) {
			    updateUIIntent.putExtra(STATUS_EXTRA, AUTH_ERROR_STATUS);
			    Log.w(Config.LOGTAG_C2DM, "Registration error " +
				    String.valueOf(res.getStatusLine().getStatusCode()));
			} else {
			    Log.w(Config.LOGTAG_C2DM, "Registration error " +
				    String.valueOf(res.getStatusLine().getStatusCode()));
			    updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
			}

		    } else {
			Log.w(Config.LOGTAG_C2DM, "Registration error, null registrationID");
			updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
		    }
		} catch (Exception e) {
		    Log.w(Config.LOGTAG_C2DM, "Registration error " + e.getMessage());
		    updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
		} finally{
		    context.sendBroadcast(updateUIIntent);
		}
	    }
	}).start();
    }

    public static void unregisterWithServer(final Context context,
	    final String registrationId) {
	new Thread(new Runnable() {
	    public void run() {
		Intent updateUIIntent = new Intent(Config.INTENT_ACTION_UPDATE_UI);
		try {
		    if (registrationId != null && registrationId.length() > 0) {
			HttpResponse res = makeRequest(context, registrationId, UNREGISTER_PATH);
			if (res.getStatusLine().getStatusCode() == 200) {
			    final SharedPreferences prefs = context.getSharedPreferences(
				    Config.PREFERENCE_KEY,
				    Context.MODE_PRIVATE);
			    SharedPreferences.Editor editor = prefs.edit();
			    editor.remove(Config.PREF_REGISTRATION_ID);
			    editor.commit();
			    updateUIIntent.putExtra(STATUS_EXTRA, UNREGISTERED_STATUS);
			    Log.d(Config.LOGTAG_C2DM, "Unregistration OK " +
				    String.valueOf(res.getStatusLine().getStatusCode()));
			} else {
			    Log.w(Config.LOGTAG_C2DM, "Unregistration error " +
				    String.valueOf(res.getStatusLine().getStatusCode()));
			    updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
			}
		    } else {
			Log.w(Config.LOGTAG_C2DM, "Registration error, null registrationID");
			updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
		    }
		} catch (Exception e) {
		    updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
		    Log.w(Config.LOGTAG_C2DM, "Unegistration error " + e.getMessage());
		} finally{
		    context.sendBroadcast(updateUIIntent);
		}
	    }
	}).start();
    }

    /*public static void unregisterWithServer(final Context context,
	    final String deviceRegistrationID) {
	new Thread(new Runnable() {
	    public void run() {
		Intent updateUIIntent = new Intent("com.google.ctp.UPDATE_UI");
		try {
		    HttpResponse res = makeRequest(context, deviceRegistrationID, UNREGISTER_PATH);
		    if (res.getStatusLine().getStatusCode() == 200) {
			final SharedPreferences prefs = context.getSharedPreferences(
				Config.PREFERENCE_KEY,
				Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.remove("deviceRegistrationID");
			editor.commit();
			updateUIIntent.putExtra(STATUS_EXTRA, UNREGISTERED_STATUS);
		    } else {
			Log.w(Config.LOGTAG_C2DM, "Unregistration error " +
				String.valueOf(res.getStatusLine().getStatusCode()));
			updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
		    }
		} catch (Exception e) {
		    updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
		    Log.w(Config.LOGTAG_C2DM, "Unegistration error " + e.getMessage());
		}

		// Update dialog activity
		context.sendBroadcast(updateUIIntent);
	    }
	}).start();
    }*/

    private static HttpResponse makeRequest(Context context, String deviceRegistrationID,
	    String urlPath) throws Exception {
	final SharedPreferences prefs = context.getSharedPreferences(
		Config.PREFERENCE_KEY,
		Context.MODE_PRIVATE);
	String accountName = prefs.getString(Config.PREF_USER_EMAIL, null);

	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair(Config.DEV_REG_ID_PARAM, deviceRegistrationID));

	Log.d(Config.LOGTAG_C2DM, "DeviceRegistrar.makeRequest() : set request parameter " + Config.DEV_REG_ID_PARAM + "=" + deviceRegistrationID);

	//String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	String installationId = Installation.id(context);
	if (installationId != null) {
	    params.add(new BasicNameValuePair(Config.DEVICE_ID_PARAM, installationId));

	    Log.d(Config.LOGTAG_C2DM, "DeviceRegistrar.makeRequest() : set request parameter " + Config.DEVICE_ID_PARAM + "=" + installationId);
	}

	String phoneNumber = getPhoneNumber(context);
	if (phoneNumber != null) {
	    params.add(new BasicNameValuePair(Config.DEV_PHONE_NUMBER_PARAM, phoneNumber));

	    Log.d(Config.LOGTAG_C2DM, "DeviceRegistrar.makeRequest() : set request parameter " + Config.DEV_PHONE_NUMBER_PARAM + "=" + phoneNumber);
	}

	String countryCode = getPhoneConuntryCode(context);
	if (countryCode != null) {
	    params.add(new BasicNameValuePair(Config.DEV_PHONE_COUNTRY_ISO_PARAM, countryCode));

	    Log.d(Config.LOGTAG_C2DM, "DeviceRegistrar.makeRequest() : set request parameter " + Config.DEV_PHONE_COUNTRY_ISO_PARAM + "=" + countryCode);
	}


	// TODO: Allow device name to be configured
	params.add(new BasicNameValuePair(Config.DEVICE_NAME_PARAM, isTablet(context) ? "Tablet" : "Phone"));

	Log.d(Config.LOGTAG_C2DM, "DeviceRegistrar.makeRequest() : set request parameter " + Config.DEVICE_NAME_PARAM + "=" + (isTablet(context) ? "Tablet" : "Phone"));

	Log.d(Config.LOGTAG_C2DM, "DeviceRegistrar.makeRequest() : set request urlPath =" + urlPath);

	AppEngineClient client = new AppEngineClient(context, accountName);
	//return client.makeRequestNoAuth(urlPath, params);
	return client.makeRequestWithParams(urlPath, params);

    }

    public static boolean isTablet (Context context) {
	// TODO: This hacky stuff goes away when we allow users to target devices
	int xlargeBit = 4; // Configuration.SCREENLAYOUT_SIZE_XLARGE;  // upgrade to HC SDK to get this
	Configuration config = context.getResources().getConfiguration();
	return (config.screenLayout & xlargeBit) == xlargeBit;
    }

    public static String getPhoneNumberFromTeleService(Context context) {
	TelephonyManager mTelephonyMgr =
	    (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	String phoneNum =  mTelephonyMgr.getLine1Number();
	Log.d(Config.LOGTAG_C2DM, "DeviceRegistrar.getPhoneNumber() : phone number is " + phoneNum);
	if (phoneNum == null || phoneNum.trim().equals("")) {
	    phoneNum = null;
	    Log.d(Config.LOGTAG_C2DM, "DeviceRegistrar.getPhoneNumber() : line 1 is null return null");
	    //phoneNum = "15103978860";
	}
	return phoneNum;
    }

    public static String getPhoneNumber(Context context) {
	TelephonyManager mTelephonyMgr =
	    (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	String phoneNum =  mTelephonyMgr.getLine1Number();
	Log.d(Config.LOGTAG_C2DM, "DeviceRegistrar.getPhoneNumber() : phone number is " + phoneNum);
	if (phoneNum == null || phoneNum.trim().equals("")) {
	    final SharedPreferences prefs = context.getSharedPreferences(
		    Config.PREFERENCE_KEY,
		    Context.MODE_PRIVATE);
	    phoneNum = prefs.getString(Config.PREF_PHONE_NUMBER, null);
	    Log.d(Config.LOGTAG_C2DM, "DeviceRegistrar.getPhoneNumber() : line 1 is null try preference, got phoneNum = " + phoneNum);
	}
	return phoneNum;
    }

    public static String getPhoneConuntryCode(Context context) {
	TelephonyManager mTelephonyMgr =
	    (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	String countryCode =  mTelephonyMgr.getSimCountryIso();
	if (countryCode == null || countryCode.trim().equals("")) {
	    countryCode = "us";
	}
	return countryCode;
    }
}
