package com.geatte.android.c2dm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.util.Log;


public class SimpleC2DMReceiver extends BroadcastReceiver {
    private static String KEY = "c2dmPref";
    private static String REGISTRATION_KEY = "registrationKey";

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
	this.context = context;
	if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
	    handleRegistration(context, intent);
	} else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
	    handleMessage(context, intent);
	}
    }

    private void handleRegistration(Context context, Intent intent) {
	String registration = intent.getStringExtra("registration_id");
	if (intent.getStringExtra("error") != null) {
	    // Registration failed, should try again later.
	    Log.d("c2dm", "registration failed");
	    String error = intent.getStringExtra("error");
	    if (error == "SERVICE_NOT_AVAILABLE") {
		Log.d("c2dm", "SERVICE_NOT_AVAILABLE");
	    } else if (error == "ACCOUNT_MISSING") {
		Log.d("c2dm", "ACCOUNT_MISSING");
	    } else if (error == "AUTHENTICATION_FAILED") {
		Log.d("c2dm", "AUTHENTICATION_FAILED");
	    } else if (error == "TOO_MANY_REGISTRATIONS") {
		Log.d("c2dm", "TOO_MANY_REGISTRATIONS");
	    } else if (error == "INVALID_SENDER") {
		Log.d("c2dm", "INVALID_SENDER");
	    } else if (error == "PHONE_REGISTRATION_ERROR") {
		Log.d("c2dm", "PHONE_REGISTRATION_ERROR");
	    }
	} else if (intent.getStringExtra("unregistered") != null) {
	    // unregistration done, new messages from the authorized sender will
	    // be rejected
	    Log.d("c2dm", "unregistered");

	} else if (registration != null) {
	    Log.d("c2dm", registration);
	    Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
	    editor.putString(REGISTRATION_KEY, registration);
	    editor.commit();
	    // Send the registration ID to the 3rd party site that is sending
	    // the messages.
	    // This should be done in a separate thread.
	    // When done, remember that all registration is done.
	}
    }

    private void handleMessage(Context context, Intent intent) {
	// Do whatever you want with the message
    }
}