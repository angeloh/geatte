package com.geatte.android.app;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLDecoder;

import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;

import com.geatte.android.app.Config;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

/**
 * Base class for image upload service.
 */
public class GeatteImageUploadIntentService extends IntentService {
    public static final String IMAGE_UPLOAD_ACTION = "com.geatte.android.app.IMAGE_UPLOAD";

    // wakelock
    private static final String WAKELOCK_KEY = "GEATTE_IMAGE";

    private static PowerManager.WakeLock mWakeLock;

    public GeatteImageUploadIntentService() {
	super("image_upload");
    }

    @Override
    public final void onHandleIntent(Intent intent) {
	try {
	    Context context = getApplicationContext();
	    if (intent.getAction().equals(IMAGE_UPLOAD_ACTION)) {
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "Upload Image Intent Service STARTED!!");
		}
		handleImageUpload(context, intent);
	    }
	} finally {
	    //  Release the power lock, so phone can get back to sleep.
	    // The lock is reference counted by default, so multiple
	    // messages are ok.
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

	String receiver = "com.geatte.android.app.GeatteImageUploadIntentService";
	intent.setClassName(context, receiver);

	context.startService(intent);
    }

    protected void handleImageUpload(Context context, Intent intent) {
	try {
	    Bundle extras = intent.getExtras();
	    String imagePath = (String) (extras != null ? extras.get(Config.EXTRA_IMAGE_PATH) : null);
	    String imageRandomId = (String) (extras != null ? extras.get(Config.EXTRA_IMAGE_RANDOM_ID) : null);
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, " GeatteImageUploadIntentService:handleImageUpload() got a new picture in "
			+ imagePath + ", imageRandomId = " + imageRandomId);
	    }

	    Bitmap bitmap = null;
	    String imageFileName = null;
	    if (imagePath != null && imageRandomId != null) {
		// bitmap = BitmapFactory.decodeFile(mImagePath);
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		int sampleSize = CommonUtils.getResizeRatio(imagePath, 1500, 6);
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, " GeatteImageUploadIntentService:handleImageUpload() resize image with sampleSize = " + sampleSize);
		}
		bitmapOptions.inSampleSize = sampleSize;
		bitmap = BitmapFactory.decodeFile(imagePath, bitmapOptions);
		imageFileName = new File(imagePath).getName();
	    } else {
		Log.w(Config.LOGTAG,
		" GeatteImageUploadIntentService:handleImageUpload() imagePath or imageRandomId is null ");
		return;
	    }

	    if (bitmap == null) {
		Log.e(Config.LOGTAG,
			"GeatteImageUploadIntentService:handleImageUpload(): bitmap is null, invalid imagePath = "
			+ imagePath);
		return;
	    }

	    MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    bitmap.compress(CompressFormat.JPEG, 100, bos);
	    byte[] data = bos.toByteArray();

	    entity.addPart(Config.GEATTE_IMAGE_RANDOM_ID_PARAM, new StringBody(imageRandomId));
	    entity.addPart(Config.GEATTE_IMAGE_BLOB_PARAM, new ByteArrayBody(data, imageFileName));

	    final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	    String accountName = prefs.getString(Config.PREF_USER_EMAIL, null);

	    AppEngineClient client = new AppEngineClient(getApplicationContext(), accountName);
	    HttpResponse response = client.makeRequestWithEntity(Config.GEATTE_IMAGE_BLOB_UPLOAD_URL, entity);
	    int respStatusCode = response.getStatusLine().getStatusCode();
	    if (respStatusCode == 400 || respStatusCode == 500) {
		// RETRY
		Log.i(Config.LOGTAG, "Got Error status code = " + respStatusCode + ", scheduling image blob upload " +
			"retry, backoff = " + Config.IMAGE_BLOB_UPLOAD_BACKOFF);
		Intent retryIntent = new Intent(IMAGE_UPLOAD_ACTION);
		PendingIntent retryPIntent = PendingIntent.getBroadcast(context, 0 /* requestCode */, retryIntent, 0 /* flags */);

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.ELAPSED_REALTIME, Config.IMAGE_BLOB_UPLOAD_BACKOFF, retryPIntent);
		return;
	    }

	    if (response.getEntity() != null) {

		JSONObject jResponse = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
		"UTF-8"));

		char[] tmp = new char[2048];
		StringBuffer body = new StringBuffer();
		while (true) {
		    int cnt = reader.read(tmp);
		    if (cnt <= 0) {
			break;
		    }
		    body.append(tmp, 0, cnt);
		}
		try {
		    jResponse = new JSONObject(URLDecoder.decode((body.toString()==null ? "" : body.toString()), Config.ENCODE_UTF8));
		    if(Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, "GeatteImageUploadIntentService:handleImageUpload Response: " + jResponse);
		    }
		} catch (JSONException e) {
		    Log.e(Config.LOGTAG,
			    "GeatteImageUploadIntentService:handleImageUpload(): unable to read " +
			    "response after upload image blob to server", e);
		}

		String resp = null;
		if (jResponse != null) {
		    resp = jResponse.getString(Config.GEATTE_IMAGE_BLOB_RESP);
		    if(Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, " GeatteImageUploadIntentService:handleImageUpload : GOT image uplaod resp = "
				+ resp);
		    }
		}

		if (jResponse == null || resp == null || !resp.equals("OK")) {
		    // RETRY
		    if(Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, "Scheduling image blob upload retry, backoff = " + Config.IMAGE_BLOB_UPLOAD_BACKOFF);
		    }
		    Intent retryIntent = new Intent(IMAGE_UPLOAD_ACTION);
		    PendingIntent retryPIntent = PendingIntent.getBroadcast(context, 0 /* requestCode */, retryIntent, 0 /* flags */);

		    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		    am.set(AlarmManager.ELAPSED_REALTIME, Config.IMAGE_BLOB_UPLOAD_BACKOFF, retryPIntent);
		}

	    }
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "GeatteImageUploadIntentService:handleImageUpload(): ERROR", e);
	}
    }

}
