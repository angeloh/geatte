package com.geatte.android.c2dm;

import com.geatte.android.app.Config;
import com.geatte.android.app.DeviceRegistrar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Broadcast receiver that handles Android Cloud to Data Messaging (AC2DM) messages, initiated
 * by the JumpNote App Engine server and routed/delivered by Google AC2DM servers. The
 * only currently defined message is 'sync'.
 */
public class C2DMReceiver extends C2DMBaseReceiver {
    static final String TAG = C2DMReceiver.class.getSimpleName();

    public C2DMReceiver() {
	super(Config.C2DM_SENDER);
    }

    @Override
    public void onRegistered(Context context, String registrationId)
    throws java.io.IOException {
	// The registrationId should be send to your applicatioin server.
	// We just log it to the LogCat view
	// We will copy it from there

	if (registrationId != null) {
	    Log.d(Config.LOGTAG_C2DM, "Registration ID arrived: Fantastic!!!");
	    Log.d(Config.LOGTAG_C2DM, registrationId);
	} else {
	    Log.e(Config.LOGTAG_C2DM, "Registration ID is null!!!");
	}

	//TODO if null, donot call REST at all
	DeviceRegistrar.registerWithServer(context, registrationId);

    };

    @Override
    public void onUnregistered(Context context) {
	Log.d(Config.LOGTAG_C2DM, "GeatteApp:onUnregistered() START");
	final SharedPreferences prefs = context.getSharedPreferences(
		Config.PREFERENCE_KEY,
		Context.MODE_PRIVATE);
	String registrationId = prefs.getString(Config.PREF_REGISTRATION_ID, null);
	DeviceRegistrar.unregisterWithServer(context, registrationId);
	Log.d(Config.LOGTAG_C2DM, "GeatteApp:onUnregistered() END");
    }

    @Override
    public void onError(Context context, String errorId) {
	Log.d(Config.LOGTAG_C2DM, "GeatteApp:onError() START");
	final SharedPreferences prefs = context.getSharedPreferences(
		Config.PREFERENCE_KEY,
		Context.MODE_PRIVATE);
	String registrationId = prefs.getString(Config.PREF_REGISTRATION_ID, null);
	DeviceRegistrar.unregisterWithServer(context, registrationId);
	Toast.makeText(context, "Messaging registration error: " + errorId,
		Toast.LENGTH_LONG).show();
	Log.d(Config.LOGTAG_C2DM, "GeatteApp:onError() END");
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
	Log.d(Config.LOGTAG_C2DM, "onMessage: Fantastic!!!");
	Bundle extras = intent.getExtras();
	if (extras != null) {
	    String accountName = extras.getString(Config.C2DM_ACCOUNT_EXTRA);
	    if (accountName != null) {
		Log.d(Config.LOGTAG_C2DM, "Messaging request received for account " + accountName);
	    }
	    String message = extras.getString(Config.C2DM_MESSAGE_EXTRA);
	    if (message != null) {
		Log.d(Config.LOGTAG_C2DM, "Messaging request received for message " + message);
	    }
	    String payload = (String) extras.get(Config.C2DM_MESSAGE_PAYLOAD);
	    if (payload != null) {
		Log.d(Config.LOGTAG_C2DM, "Messaging request received for payload " + payload);
	    }
	    String geatteid = (String) extras.get(Config.C2DM_MESSAGE_GEATTE_ID);
	    if (geatteid != null) {
		Log.d(Config.LOGTAG_C2DM, "Messaging request received for geatteid " + geatteid);
	    }
	    geatteid = extras.getString(Config.C2DM_MESSAGE_GEATTE_ID);
	    if (geatteid != null) {
		Log.d(Config.LOGTAG_C2DM, "Messaging request received for geatteid " + geatteid);
	    }
	    // Now do something smart based on the information
	}

    }

    /**
     * Register or unregister based on phone sync settings.
     * Called on each performSync by the SyncAdapter.
     */
    /*    public static void refreshAppC2DMRegistrationState(Context context) {
	// Determine if there are any auto-syncable accounts. If there are, make sure we are
	// registered with the C2DM servers. If not, unregister the application.
	boolean autoSyncDesired = false;
	if (ContentResolver.getMasterSyncAutomatically()) {
	    AccountManager am = AccountManager.get(context);
	    Account[] accounts = am.getAccountsByType(Config.GOOGLE_ACCOUNT_TYPE);
	    for (Account account : accounts) {
		if (ContentResolver.getIsSyncable(account, JumpNoteContract.AUTHORITY) > 0 &&
			ContentResolver.getSyncAutomatically(account, JumpNoteContract.AUTHORITY)) {
		    autoSyncDesired = true;
		    break;
		}
	    }
	}

	boolean autoSyncEnabled = !C2DMessaging.getRegistrationId(context).equals("");

	if (autoSyncEnabled != autoSyncDesired) {
	    Log.i(TAG, "System-wide desirability for JumpNote auto sync has changed; " +
		    (autoSyncDesired ? "registering" : "unregistering") +
	    " application with C2DM servers.");

	    if (autoSyncDesired == true) {
		C2DMessaging.register(context, Config.C2DM_SENDER);
	    } else {
		C2DMessaging.unregister(context);
	    }
	}
    }*/
}
