package com.geatte.android.c2dm;

import java.io.IOException;

import com.geatte.android.app.Config;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

/**
 * Base class for C2D message receiver. Includes constants for the
 * strings used in the protocol.
 */
public abstract class C2DMBaseReceiver extends IntentService {
    public static final String C2DM_RETRY_ACTION = "com.google.android.c2dm.intent.RETRY";
    public static final String C2DM_REGISTRATION_ACTION = "com.google.android.c2dm.intent.REGISTRATION";
    public static final String C2DM_RECEIVE_ACTION = "com.google.android.c2dm.intent.RECEIVE";

    // Extras in the registration callback intents.
    public static final String EXTRA_UNREGISTERED = "unregistered";

    public static final String EXTRA_ERROR = "error";

    public static final String EXTRA_REGISTRATION_ID = "registration_id";

    public static final String ERR_SERVICE_NOT_AVAILABLE = "SERVICE_NOT_AVAILABLE";
    public static final String ERR_ACCOUNT_MISSING = "ACCOUNT_MISSING";
    public static final String ERR_AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
    public static final String ERR_TOO_MANY_REGISTRATIONS = "TOO_MANY_REGISTRATIONS";
    public static final String ERR_INVALID_PARAMETERS = "INVALID_PARAMETERS";
    public static final String ERR_INVALID_SENDER = "INVALID_SENDER";
    public static final String ERR_PHONE_REGISTRATION_ERROR = "PHONE_REGISTRATION_ERROR";

    // wakelock
    private static final String WAKELOCK_KEY = "C2DM_LIB";

    private static PowerManager.WakeLock mWakeLock;
    private final String senderId;

    /**
     * The C2DMReceiver class must create a no-arg constructor and pass the
     * sender id to be used for registration.
     */
    public C2DMBaseReceiver(String senderId) {
	// senderId is used as base name for threads, etc.
	super(senderId);
	this.senderId = senderId;
    }

    /**
     * Called when a cloud message has been received.
     */
    protected abstract void onMessage(Context context, Intent intent);

    /**
     * Called on registration error. Override to provide better
     * error messages.
     * 
     * This is called in the context of a Service - no dialog or UI.
     */
    public abstract void onError(Context context, String errorId);

    /**
     * Called when a registration token has been received.
     */
    public abstract void onRegistered(Context context, String registrationId) throws IOException;

    /**
     * Called when the device has been unregistered.
     */
    public void onUnregistered(Context context) {
    }

    @Override
    public final void onHandleIntent(Intent intent) {
	try {
	    Context context = getApplicationContext();
	    if (intent.getAction().equals(C2DM_REGISTRATION_ACTION)) {
		Log.d(Config.LOGTAG_C2DM, "Registration CALLBACK STARTED!!");
		handleRegistration(context, intent);
	    } else if (intent.getAction().equals(C2DM_RECEIVE_ACTION)) {
		Log.d(Config.LOGTAG_C2DM, "Got MESSAGE!!");
		onMessage(context, intent);
	    } else if (intent.getAction().equals(C2DM_RETRY_ACTION)) {
		Log.d(Config.LOGTAG_C2DM, "Registration RETRY STARTED!!");
		C2DMessaging.register(context, senderId);
	    }
	} finally {
	    //  Release the power lock, so phone can get back to sleep.
	    // The lock is reference counted by default, so multiple
	    // messages are ok.

	    // If the onMessage() needs to spawn a thread or do something else,
	    // it should use it's own lock.
	    mWakeLock.release();
	}
    }

    /**
     * Called from the broadcast receiver.
     * Will process the received intent, call handleMessage(), registered(), etc.
     * in background threads, with a wake lock, while keeping the service
     * alive.
     */
    public static void runIntentInService(Context context, Intent intent) {
	if (mWakeLock == null) {
	    // This is called from BroadcastReceiver, there is no init.
	    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
	    mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_KEY);
	}
	mWakeLock.acquire();

	// Use a naming convention, similar with how permissions and intents are
	// used. Alternatives are introspection or an ugly use of statics.
	String receiver = "com.geatte.android.c2dm.C2DMReceiver";
	intent.setClassName(context, receiver);

	context.startService(intent);

    }

    private void handleRegistration(final Context context, Intent intent) {
	final String registrationId = intent.getStringExtra(EXTRA_REGISTRATION_ID);
	String error = intent.getStringExtra(EXTRA_ERROR);
	String removed = intent.getStringExtra(EXTRA_UNREGISTERED);

	if (Log.isLoggable(Config.LOGTAG_C2DM, Log.DEBUG)) {
	    Log.d(Config.LOGTAG_C2DM, "dmControl: registrationId = " + registrationId + ", error = " + error
		    + ", removed = " + removed);
	}

	if (removed != null) {
	    // Remember we are unregistered
	    //C2DMessaging.clearRegistrationId(context);
	    onUnregistered(context);
	    return;
	} else if (error != null) {
	    // we are not registered, can try again
	    //C2DMessaging.clearRegistrationId(context);
	    // Registration failed
	    Log.e(Config.LOGTAG_C2DM, "Registration error " + error);
	    onError(context, error);
	    if ("SERVICE_NOT_AVAILABLE".equals(error)) {
		//		long backoffTimeMs = C2DMessaging.getBackoff(context);
		long backoffTimeMs = C2DMessaging.DEFAULT_BACKOFF;

		Log.d(Config.LOGTAG_C2DM, "Scheduling registration retry, backoff = " + backoffTimeMs);
		Intent retryIntent = new Intent(C2DM_RETRY_ACTION);
		PendingIntent retryPIntent = PendingIntent.getBroadcast(context, 0 /* requestCode */, retryIntent, 0 /* flags */);

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.ELAPSED_REALTIME, backoffTimeMs, retryPIntent);


		//		new Thread(new Runnable() {
		//		    public void run() {
		//			Intent retryIntent = new Intent(C2DM_RETRY_ACTION);
		//			Log.w(Config.LOGTAG_C2DM, "Start a RETRY intent");
		//			context.sendBroadcast(retryIntent);
		//			//			context.startService(retryIntent);
		//		    }
		//		}).start();

		//Log.w(Config.LOGTAG_C2DM, "Start a RETRY intent");
		//C2DMessaging.register(this, Config.C2DM_SENDER);

		// Next retry should wait longer.
		//backoffTimeMs *= 2;
		//C2DMessaging.setBackoff(context, backoffTimeMs);
	    }else if(error == "ACCOUNT_MISSING"){
		Log.d(Config.LOGTAG_C2DM, "ACCOUNT_MISSING");
	    }else if(error == "AUTHENTICATION_FAILED"){
		Log.d(Config.LOGTAG_C2DM, "AUTHENTICATION_FAILED");
	    }else if(error == "TOO_MANY_REGISTRATIONS"){
		Log.d(Config.LOGTAG_C2DM, "TOO_MANY_REGISTRATIONS");
	    }else if(error == "INVALID_SENDER"){
		Log.d(Config.LOGTAG_C2DM, "INVALID_SENDER");
	    }else if(error == "PHONE_REGISTRATION_ERROR"){
		Log.d(Config.LOGTAG_C2DM, "PHONE_REGISTRATION_ERROR");
	    }
	} else {
	    try {
		onRegistered(context, registrationId);
		//C2DMessaging.setRegistrationId(context, registrationId);
	    } catch (IOException ex) {
		Log.e(Config.LOGTAG_C2DM, "Registration error " + ex.getMessage());
	    }
	}
    }
}
