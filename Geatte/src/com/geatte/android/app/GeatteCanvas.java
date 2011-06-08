package com.geatte.android.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.geatte.android.c2dm.C2DMessaging;
import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;

public class GeatteCanvas extends GDActivity {
    private static final String CLASSTAG = GeatteCanvas.class.getSimpleName();
    private static final int ACTIVITY_SNAP = 0;
    private static final int ACTIVITY_CREATE = 1;
    private boolean mPendingAuth = false;
    private String mImagePath = null;
    private PendingIntent mAlarmSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	// TODO check if server has this device's reg id

	// Run the setup first if necessary
	if (isSetupRequired()) {
	    startActivity(new Intent(this, GeatteSetupActivity.class));
	}

	setActionBarContentView(R.layout.geatte_canvas);

	addActionBarItem(Type.Info);

	Button cameraButton = (Button) findViewById(R.id.app_snap_button);
	cameraButton.setOnClickListener( new OnClickListener(){
	    public void onClick(View v ){
		//Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
		mImagePath = createImagePath();
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mImagePath)));
		Log.d(Config.LOGTAG, " " + GeatteCanvas.CLASSTAG + " put a EXTRA_OUTPUT for image capture to " + mImagePath);

		//		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(getTmpImagePath())));
		//		Log.d(Config.LOGTAG, " " + GeatteCanvas.CLASSTAG + " put a EXTRA_OUTPUT for image capture to " + getTmpImagePath());
		startActivityForResult(intent, ACTIVITY_SNAP);
	    }
	});

	final Button showAllFeedbackButton = (Button) findViewById(R.id.app_show_all_feedback_btn);
	showAllFeedbackButton.setOnClickListener( new OnClickListener(){
	    public void onClick(View v ){
		Intent intent = new Intent(getApplicationContext(), GeatteAllFeedbackActivity.class);
		startActivity(intent);
	    }
	});

	final Button showGeatteTab = (Button) findViewById(R.id.app_show_geattetab_btn);
	showGeatteTab.setOnClickListener( new OnClickListener(){
	    public void onClick(View v ) {
		Intent intent = new Intent(getApplicationContext(), GeatteTabActivity.class);
		startActivity(intent);
	    }
	});
	showGeatteTab.setOnFocusChangeListener( new OnFocusChangeListener(){
	    @Override
	    public void onFocusChange(View view, boolean isFocused) {
		if (isFocused == true)	{
		    showGeatteTab.setText(R.string.show_geattetab_loading);
		} else{
		    showGeatteTab.setText(R.string.show_geattetab);
		}
	    }
	});

	// Create an IntentSender that will launch our service, to be scheduled
	// with the alarm manager.
	mAlarmSender = PendingIntent.getService(GeatteCanvas.this,
		0, new Intent(GeatteCanvas.this, GeatteContactsService.class), 0);

	scheduleContactService();

    }

    private void scheduleContactService() {
	// We want the alarm to go off 30 seconds from now.
	long firstTime = SystemClock.elapsedRealtime();

	// Schedule the alarm!
	AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
	am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
		firstTime, 5*60*60*1000, mAlarmSender);
    }

    private void unScheduleContactService() {
	// And cancel the alarm.
	AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
	am.cancel(mAlarmSender);
    }

    //TODO show information
    public void onShowInfo(View v) {

    }

    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

	switch (position) {
	case 0:
	    onShowInfo(item.getItemView());
	    break;

	default:
	    return super.onHandleActionBarItemClick(item, position);
	}

	return true;
    }

    @Override
    protected void onResume() {
	Log.d(Config.LOGTAG_C2DM, "GeatteCanvas:onResume() START");
	super.onResume();
	if (mPendingAuth) {
	    mPendingAuth = false;
	    String regId = C2DMessaging.getRegistrationId(this);
	    if (regId != null && !regId.equals("")) {
		DeviceRegistrar.registerWithServer(this, regId);
	    } else {
		C2DMessaging.register(this, Config.C2DM_SENDER);
	    }
	}
	Log.d(Config.LOGTAG_C2DM, "GeatteCanvas:onResume() END");
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
	unScheduleContactService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	if (requestCode == ACTIVITY_SNAP && resultCode == Activity.RESULT_OK){
	    //Bitmap x = (Bitmap) intent.getExtras().get("data");
	    //String path = saveToFile(imgBitmap);

	    ////File fi = new File(getTmpImagePath());
	    File fi = new File(mImagePath);

	    //Bitmap captureBmp = Media.getBitmap(getContentResolver(), Uri.fromFile(fi) );
	    //uri = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getContentResolver(), fi.getAbsolutePath(), null, null));
	    if (fi.exists()) {
		////		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		////		bitmapOptions.inSampleSize = 6;
		////		Bitmap imgBitmap = BitmapFactory.decodeFile(fi.getAbsolutePath(), bitmapOptions);
		////		String pathToPicture = saveToFile(imgBitmap);
		////		Log.d(Config.LOGTAG, " " + GeatteCanvas.CLASSTAG + " save image capture output to path : " + pathToPicture);
		////
		////		Intent i = new Intent(this, GeatteEditActivity.class);
		////		i.putExtra(GeatteDBAdapter.KEY_IMAGE_PATH, pathToPicture);
		////		startActivityForResult(i, ACTIVITY_CREATE);
		////		if (!fi.delete()) {
		////		    Log.d(Config.LOGTAG, "Failed to delete " + fi.getAbsolutePath());
		////		}

		Log.d(Config.LOGTAG, " " + GeatteCanvas.CLASSTAG + " save image capture output to path : " + mImagePath);

		Intent i = new Intent(this, GeatteEditActivity.class);
		i.putExtra(GeatteDBAdapter.KEY_IMAGE_PATH, mImagePath);
		startActivityForResult(i, ACTIVITY_CREATE);

	    } else {
		Log.d(Config.LOGTAG, "file not exist " + fi.getAbsolutePath());
	    }

	}
    }

    public String saveToFile(Bitmap bitmap) {
	String filename;
	Date date = new Date();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	filename = sdf.format(date);

	try {
	    String path = Environment.getExternalStorageDirectory().toString();
	    OutputStream fOut = null;
	    File dir = new File(path, "/geatte/images/");
	    if (!dir.isDirectory()) {
		dir.mkdirs();
	    }

	    File file = new File(dir, filename + ".jpg");

	    fOut = new FileOutputStream(file);

	    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
	    fOut.flush();
	    fOut.close();

	    MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file
		    .getName());
	    return file.getAbsolutePath();
	} catch (Exception e) {
	    Log.w(Config.LOGTAG, " " + GeatteCanvas.CLASSTAG + " Exception :" , e);
	}
	return null;
    }

    public String createImagePath() {
	String filename;
	Date date = new Date();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	filename = sdf.format(date);

	try {
	    String path = Environment.getExternalStorageDirectory().toString();
	    File dir = new File(path, "/geatte/images/");
	    if (!dir.isDirectory()) {
		dir.mkdirs();
	    }

	    File file = new File(dir, filename + ".jpg");
	    return file.getAbsolutePath();
	} catch (Exception e) {
	    Log.w(Config.LOGTAG, " " + GeatteCanvas.CLASSTAG + " Exception :" , e);
	}
	return null;
    }

    public String getTmpImagePath() {
	try {
	    String externalDir = Environment.getExternalStorageDirectory().toString();
	    Log.d(Config.LOGTAG, " " + GeatteCanvas.CLASSTAG + " getTmpImagePath() : get externalDir " + externalDir);
	    File tmpfile = new File(externalDir, "/tmpgeatte");
	    if (!tmpfile.exists()) {
		tmpfile.getParentFile().mkdirs();
		tmpfile.createNewFile();
	    }
	    Log.d(Config.LOGTAG, " " + GeatteCanvas.CLASSTAG + " getTmpImagePath() : get tmp image path " + tmpfile.getAbsolutePath());
	    return tmpfile.getAbsolutePath();
	} catch (Exception e) {
	    Log.w(Config.LOGTAG, " " + GeatteCanvas.CLASSTAG + " Exception :" , e);
	}
	return null;
    }

    public boolean hasImageCaptureBug() {

	// list of known devices that have the bug
	ArrayList<String> devices = new ArrayList<String>();
	devices.add("android-devphone1/dream_devphone/dream");
	devices.add("generic/sdk/generic");
	devices.add("vodafone/vfpioneer/sapphire");
	devices.add("tmobile/kila/dream");
	devices.add("verizon/voles/sholes");
	devices.add("google_ion/google_ion/sapphire");

	return devices.contains(android.os.Build.BRAND + "/" + android.os.Build.PRODUCT + "/"
		+ android.os.Build.DEVICE);

    }

    private boolean isSetupRequired() {
	Context context = getApplicationContext();
	final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	if (prefs.getString(Config.PREF_REGISTRATION_ID, null) == null) {
	    return true;
	}
	String userEmail = prefs.getString(Config.PREF_USER_EMAIL, null);
	if (userEmail == null) {
	    return true;
	}
	return false;
	// async check server
	//	String phoneNumber = DeviceRegistrar.getPhoneNumber(context);
	//	new RegIdCheckTask().execute(phoneNumber, userEmail);
	//
	//	String hasRegIdStr = prefs.getString(Config.PREF_SERVER_HAS_REG_ID, null);
	//	boolean hasRegId = Boolean.parseBoolean(hasRegIdStr);
	//	if (!hasRegId) {
	//	    return true;
	//	} else {
	//	    return false;
	//	}
    }

    class RegIdCheckTask extends AsyncTask<String, String, String> {
	@Override
	protected String doInBackground(String... strings) {
	    try {
		String myNumber = strings[0];
		String userEmail = strings[1];

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(Config.DEV_PHONE_NUMBER_PARAM, myNumber));

		AppEngineClient client = new AppEngineClient(getApplicationContext(), userEmail);
		HttpResponse response = client.makeRequestWithParams(Config.GEATTE_REG_CHECK_URL, params);

		if (response.getEntity() != null) {
		    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
		    "UTF-8"));
		    String sResponse = reader.readLine();
		    Log.d(Config.LOGTAG, "RegIdCheckTask Response: " + sResponse);
		    if (response.getStatusLine().getStatusCode() == 400
			    || response.getStatusLine().getStatusCode() == 500) {
			Log.e(Config.LOGTAG, "RegIdCheckTask Error: " + sResponse);
			return null;
		    }
		    return sResponse;
		}
	    } catch (Exception e) {
		Log.e(Config.LOGTAG, e.getMessage(), e);
	    }
	    return null;
	}

	@Override
	protected void onPostExecute(String sResponse) {
	    try {
		Log.d(Config.LOGTAG, "RegIdCheckTask:onPostExecute(): get response :" + sResponse);

		final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
			Config.PREFERENCE_KEY,
			Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Config.PREF_SERVER_HAS_REG_ID, sResponse);
		editor.commit();

	    } catch (Exception e) {
		Log.e(Config.LOGTAG, e.getMessage(), e);
	    }
	}
    }
}