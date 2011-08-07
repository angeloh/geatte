package com.geatte.android.app;

import greendroid.app.GDActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;

import com.geatte.android.app.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.media.MediaScannerConnection;
import android.net.Uri;

public class ShopinionSnapEditActivity extends GDActivity {

    private static final String CLASSTAG = ShopinionSnapEditActivity.class.getSimpleName();
    private static final int ACTIVITY_SNAP = 0;
    private static final int ACTIVITY_CONTACT = 1;

    private ImageView mSnapView;
    private Long mInterestId;
    private String mGeatteId;
    private String mImagePath;
    private String mImageRandomId;
    private String mSavedImagePath;
    private ProgressDialog mDialog;
    private Button mSendToButton;
    private Button mSendGeatteButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
	if (Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionSnapEditActivity:onCreate(): START");
	}
	super.onCreate(savedInstanceState);

	mInterestId = (savedInstanceState == null) ? null : (Long) savedInstanceState
		.getSerializable(GeatteDBAdapter.KEY_INTEREST_ID);
	if (mInterestId == null) {
	    Bundle extras = getIntent().getExtras();
	    if (extras != null) {
		long id = extras.getLong(GeatteDBAdapter.KEY_INTEREST_ID);
		mInterestId = id != 0L ? id : null;
	    }
	}

	// in new created mode, read the image from extras
	if (mInterestId == null || mInterestId == 0L) {
	    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	    mImagePath = createImagePath();
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mImagePath)));
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, " " + ShopinionSnapEditActivity.CLASSTAG + " Will put a EXTRA_OUTPUT for image capture to " + mImagePath);
	    }

	    startActivityForResult(intent, ACTIVITY_SNAP);
	}

	if (Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionSnapEditActivity:onCreate(): END");
	}
    }

    @Override
    public int createLayout() {
	return R.layout.shopinion_edit_view;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
	super.onPostCreate(savedInstanceState);

	setTitle(R.string.edit_interest);

	//only after parent onPostCreate, view is created
	mSnapView = (ImageView) findViewById(R.id.edit_img);

	mSendToButton = (Button) findViewById(R.id.send_to_button);
	mSendToButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View view) {
		Intent intent = new Intent(getApplicationContext(), ShopinionContactSelectActivity.class);
		intent.putExtra(Config.EXTRA_IMAGE_RANDOM_ID, mImageRandomId);
		startActivityForResult(intent, ACTIVITY_CONTACT);
	    }

	});

	mSendGeatteButton = (Button) findViewById(R.id.send_geatte_button);
	mSendGeatteButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View view) {
		final SharedPreferences prefs = getApplicationContext().getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
		String selectedContacts = prefs.getString(Config.PREF_SELECTED_CONTACTS, null);
		if (selectedContacts != null && selectedContacts.length() > 0) {
		    if (mImagePath == null && mSavedImagePath == null) {
			Toast.makeText(getApplicationContext(), "No image to send", Toast.LENGTH_SHORT).show();
			setResult(RESULT_CANCELED);
			finish();
		    } else {
			mDialog = ProgressDialog.show(ShopinionSnapEditActivity.this, "Sending to friends", "Please wait...", true);
			new UploadInterestTask().execute();
		    }
		} else {
		    Toast.makeText(getApplicationContext(), "Please Select Any Friend To Send", Toast.LENGTH_SHORT).show();
		}
		//setResult(RESULT_OK);
		//finish();
	    }

	});

	checkContactsSelected();
    }

    @Override
    protected void onDestroy() {
	if (Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionSnapEditActivity:onDestroy(): START");
	}
	super.onDestroy();
	if (mDialog != null) {
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, "ShopinionSnapEditActivity:onDestroy(): cancel mDialog");
	    }
	    mDialog.cancel();
	}
	if (Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionSnapEditActivity:onDestroy(): END");
	}
    }

    public String createImagePath() {
	String filename;
	Date date = new Date();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	filename = sdf.format(date);

	try {
	    if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
		String path = Environment.getExternalStorageDirectory().toString();
		File dir = new File(path, Config.MEDIA_FOLDER);
		if (!dir.isDirectory()) {
		    dir.mkdirs();
		}

		File file = new File(dir, filename + ".jpg");
		return file.getAbsolutePath();
	    } else {
		//no external storage available
		File file = File.createTempFile("geatte_", ".jpg");
		return file.getAbsolutePath();
	    }

	} catch (Exception e) {
	    Log.w(Config.LOGTAG, " " + ShopinionSnapEditActivity.CLASSTAG + " Exception :", e);
	}
	return null;
    }

    private void checkContactsSelected() {
	Context context = getApplicationContext();
	final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	String selectedContacts = prefs.getString(Config.PREF_SELECTED_CONTACTS, null);
	if (selectedContacts != null && selectedContacts.length() > 0) {
	    mSendToButton.setText(R.string.send_to_reset_text);
	} else {
	    mSendToButton.setText(R.string.send_to_text);
	}
    }

    private void populateFields() {
	// in edit mode
	if (mInterestId != null && mInterestId != 0L) {
	    final GeatteDBAdapter dbHelper = new GeatteDBAdapter(this);
	    Cursor cursor = null;
	    String title = null;
	    String desc = null;
	    try {
		dbHelper.open();
		cursor = dbHelper.fetchMyInterest(mInterestId);

		title = cursor.getString(cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_TITLE));
		desc = cursor.getString(cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_DESC));

		mSavedImagePath = cursor.getString(cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_PATH));
	    } catch (Exception e) {
		Log.e(Config.LOGTAG, " ShopinionSnapEditActivity:populateFields() :  ERROR ", e);
	    } finally {
		if (cursor != null) {
		    cursor.close();
		}
		dbHelper.close();
	    }

	    //save to pref, so popup can pick up
	    setCaptionDescToPref(title, desc);

	    int sampleSize = CommonUtils.getResizeRatio(mSavedImagePath, 1500, 6);
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, " ShopinionSnapEditActivity:populateFields() resize image with sampleSize = " + sampleSize);
	    }
	    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
	    bitmapOptions.inSampleSize = sampleSize;
	    Bitmap imgBitmap = BitmapFactory.decodeFile(mSavedImagePath, bitmapOptions);
	    mSnapView.setImageBitmap(imgBitmap);
	}

	// in new created mode, save the image to image view
	if (mInterestId == null || mInterestId == 0L) {
	    if (mImagePath != null) {
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		int sampleSize = CommonUtils.getResizeRatio(mImagePath, 1500, 6);
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, " ShopinionSnapEditActivity:populateFields() resize image with sampleSize = " + sampleSize);
		}
		bitmapOptions.inSampleSize = sampleSize;
		Bitmap imgBitmap = BitmapFactory.decodeFile(mImagePath, bitmapOptions);
		mSnapView.setImageBitmap(imgBitmap);
		//mSnapView.setImageBitmap(BitmapFactory.decodeFile(mImagePath));
	    }
	}
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, " " + ShopinionSnapEditActivity.CLASSTAG +
	    " onSaveInstanceState() called");
	}
	updateState();
	outState.putSerializable(GeatteDBAdapter.KEY_INTEREST_ID, mInterestId);
    }

    @Override
    protected void onPause() {
	super.onPause();
	if (mSnapView.getDrawable() != null) {
	    BitmapDrawable drawable = (BitmapDrawable) mSnapView.getDrawable();
	    if (drawable.getBitmap() != null) {
		drawable.getBitmap().recycle();
	    }
	}
	mSnapView.setImageBitmap(null);
    }

    @Override
    protected void onResume() {
	super.onResume();
	if (mImageRandomId != null && !mImageRandomId.equals("NOT_AN_ID")) {
	    populateFields();
	    return;
	}

	if (mImageRandomId != null && mImageRandomId.equals("NOT_AN_ID")) {
	    // user click back in camera app, onResume() is called.
	    setResult(RESULT_CANCELED);
	    finish();
	    return;
	}
	if (mImageRandomId == null) {
	    // hack to ignore first onResume call, otherwise finish() will be called
	    mImageRandomId = "NOT_AN_ID";
	}
    }

    /**
     * Button write comment calls this function to pop up widget.
     * 
     * @param v view
     */
    public void onWrite(View v) {
	SendWritePopupWidget popup = new SendWritePopupWidget(this);
	popup.show(v);
    }

    private void saveState() {
	String title = getCaptionFromPref(true);
	String desc = getDescFromPref(true);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, " " + ShopinionSnapEditActivity.CLASSTAG +
		    " get caption and desc for interestId = " + mInterestId + ", geatteId = " + mGeatteId +
		    ", title = " + title+ ", desc = " + desc);
	}
	final GeatteDBAdapter dbHelper = new GeatteDBAdapter(this);
	try {
	    dbHelper.open();
	    if (mInterestId == null || mInterestId == 0L) {
		long id = -1;
		if (mGeatteId != null) {
		    id = dbHelper.insertInterest(title, desc, mGeatteId);
		} else {
		    id = dbHelper.insertInterest(title, desc);
		}

		if (id >= 0) {
		    mInterestId = id;
		    if(Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, " " + ShopinionSnapEditActivity.CLASSTAG +
				" create new interest for id = " + mInterestId + ", geatteId = " + mGeatteId);
		    }
		    dbHelper.insertImage(mInterestId, mImagePath);
		    Log.i(Config.LOGTAG, " " + ShopinionSnapEditActivity.CLASSTAG +
			    " insert geatte image for id " + mGeatteId + " and interest = " + mInterestId);

		}
		else {
		    Log.w(Config.LOGTAG, " " + ShopinionSnapEditActivity.CLASSTAG +
		    " unable to insert this interest!!");
		}

	    } else {
		if (mGeatteId != null) {
		    dbHelper.updateInterestWithGeatteId(mInterestId, title, desc, mGeatteId);
		} else {
		    dbHelper.updateInterest(mInterestId, title, desc);
		}
		dbHelper.updateImage(mInterestId, mImagePath);
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, " " + ShopinionSnapEditActivity.CLASSTAG +
			    " update interest for id = " + mInterestId);
		}
	    }
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, " " + ShopinionSnapEditActivity.CLASSTAG + " :  ERROR ", e);
	} finally {
	    dbHelper.close();
	}
    }

    private void updateState() {
	String title = getCaptionFromPref(false);
	String desc = getDescFromPref(false);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, " " + ShopinionSnapEditActivity.CLASSTAG +
		    " update caption and desc for interestId = " + mInterestId + ", geatteId = " + mGeatteId +
		    ", title = " + title+ ", desc = " + desc);
	}
	if (mInterestId != null && mInterestId != 0L) {
	    final GeatteDBAdapter dbHelper = new GeatteDBAdapter(this);
	    try {
		dbHelper.open();

		if (mGeatteId != null) {
		    dbHelper.updateInterestWithGeatteId(mInterestId, title, desc, mGeatteId);
		} else {
		    dbHelper.updateInterest(mInterestId, title, desc);
		}
		dbHelper.updateImage(mInterestId, mImagePath);
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, " " + ShopinionSnapEditActivity.CLASSTAG +
			    " updated interest for id = " + mInterestId);
		}
	    } catch (Exception e) {
		Log.e(Config.LOGTAG, " " + ShopinionSnapEditActivity.CLASSTAG + " :  ERROR ", e);
	    } finally {
		dbHelper.close();
	    }
	}
    }

    private void cleanSelectedContactsPref() {
	Context context = getApplicationContext();
	final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	SharedPreferences.Editor editor = prefs.edit();
	editor.putString(Config.PREF_SELECTED_CONTACTS, null);
	editor.commit();
    }

    class ImageUploadAsynTask extends AsyncTask<String, String, String> {
	@Override
	protected String doInBackground(String... strings) {
	    try {
		String imageRandomId = strings[0];
		Context context = getApplicationContext();
		Intent imageUploadIntent = new Intent(GeatteImageUploadIntentService.IMAGE_UPLOAD_ACTION);
		imageUploadIntent.putExtra(Config.EXTRA_IMAGE_PATH, mImagePath);
		imageUploadIntent.putExtra(Config.EXTRA_IMAGE_RANDOM_ID, imageRandomId);
		GeatteImageUploadIntentService.runIntentInService(context, imageUploadIntent);

	    } catch (Exception e) {
		Log.e(Config.LOGTAG, e.getMessage(), e);
	    }
	    return null;
	}
    }

    class UploadInterestTask extends AsyncTask<String, String, String> {
	@Override
	protected String doInBackground(String... strings) {
	    try {
		String title = getCaptionFromPref(false);
		String desc = getDescFromPref(false);

		Context context = getApplicationContext();
		final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
		String selectedContacts = prefs.getString(Config.PREF_SELECTED_CONTACTS, null);

		if (selectedContacts == null || selectedContacts.length() == 0) {
		    Log.e(Config.LOGTAG, " GeatteUploadTask:doInBackground(): selectedContacts is null, invalid selectedContacts = "
			    + selectedContacts);
		}

		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		String myNumber = DeviceRegistrar.getPhoneNumber(getApplicationContext());
		entity.addPart(Config.GEATTE_FROM_NUMBER_PARAM, new StringBody(myNumber));
		String myCountryCode = DeviceRegistrar.getPhoneConuntryCode(getApplicationContext());
		entity.addPart(Config.GEATTE_COUNTRY_ISO_PARAM, new StringBody(myCountryCode));
		entity.addPart(Config.GEATTE_TO_NUMBER_PARAM, new StringBody(selectedContacts));
		entity.addPart(Config.GEATTE_TITLE_PARAM, new StringBody(title));
		entity.addPart(Config.GEATTE_DESC_PARAM, new StringBody(desc));
		entity.addPart(Config.GEATTE_IMAGE_RANDOM_ID_PARAM, new StringBody(mImageRandomId));

		String accountName = prefs.getString(Config.PREF_USER_EMAIL, null);

		AppEngineClient client = new AppEngineClient(getApplicationContext(), accountName);
		HttpResponse response = client.makeRequestWithEntity(Config.ITEM_UPLOAD_PATH, entity);

		int respStatusCode = response.getStatusLine().getStatusCode();

		if (response.getEntity() != null) {

		    JSONObject jResponse = null;
		    BufferedReader reader = new BufferedReader(
			    new InputStreamReader(
				    response.getEntity().getContent(), "UTF-8"));

		    char[] tmp = new char[2048];
		    StringBuffer body = new StringBuffer();
		    while (true) {
			int cnt = reader.read(tmp);
			if (cnt <= 0) {
			    break;
			}
			body.append(tmp, 0, cnt);
		    }

		    if (respStatusCode == 400 || respStatusCode == 500) {

			//when resp is RETRY, redirect to geatte canvas
			if (body.toString().contains(Config.RETRY_STATUS)) {
			    if (mDialog != null && mDialog.isShowing()) {
				try {
				    if(Config.LOG_DEBUG_ENABLED) {
					Log.d(Config.LOGTAG, " GeatteUploadTask:doInBackground(): try to dismiss mDialog");
				    }
				    mDialog.dismiss();
				    mDialog = null;
				} catch (Exception ex) {
				    Log.w(Config.LOGTAG, " GeatteUploadTask:doInBackground(): failed to dismiss mDialog", ex);
				}
			    }
			    this.publishProgress(getString(R.string.upload_text_retry));
			    Log.w(Config.LOGTAG, " GeatteUploadTask Got RETRY, body = " + body.toString());
			    return Config.RETRY_STATUS;
			} else {
			    Log.w(Config.LOGTAG, " GeatteUploadTask Error: " + respStatusCode + " " + body.toString());
			    throw new Exception(" GeatteUploadTask Error: " + respStatusCode + " " + body.toString());
			}
		    }

		    try {
			jResponse = new JSONObject(URLDecoder.decode((body.toString()==null ? "" : body.toString()), Config.ENCODE_UTF8));
		    } catch (JSONException e) {
			Log.e(Config.LOGTAG, " GeatteUploadTask:doInBackground(): unable to read response after upload geatte to server", e);
		    }

		    if (jResponse != null) {
			mGeatteId = jResponse.getString(Config.GEATTE_ID_PARAM);
			Log.d(Config.LOGTAG, " GeatteUploadTask:doInBackground : GOT geatteId = " + mGeatteId);
		    }

		    if(Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, " GeatteUploadTask Response: " + jResponse);
		    }

		    return mGeatteId;
		}
	    } catch (Exception e) {
		Log.e(Config.LOGTAG, e.getMessage(), e);
		if (mDialog != null && mDialog.isShowing()) {
		    try {
			if(Config.LOG_DEBUG_ENABLED) {
			    Log.d(Config.LOGTAG, " GeatteUploadTask:doInBackground(): try to dismiss mDialog");
			}
			mDialog.dismiss();
			mDialog = null;
		    } catch (Exception ex) {
			Log.w(Config.LOGTAG, " GeatteUploadTask:doInBackground(): failed to dismiss mDialog", ex);
		    }
		}
		this.publishProgress(getString(R.string.upload_text_error));
	    }
	    return null;
	}

	@Override
	protected void onProgressUpdate(String... values) {
	    super.onProgressUpdate(values);
	    if (values.length > 0) {
		Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_LONG).show();
	    }
	}

	@Override
	protected void onPostExecute(String geatteId) {
	    if (geatteId != null && geatteId.equals(Config.RETRY_STATUS)) {
		Log.i(Config.LOGTAG, " GeatteUploadTask:onPostExecute(): got retry");
		return;
	    }
	    try {
		if (mDialog != null && mDialog.isShowing()) {
		    try {
			if(Config.LOG_DEBUG_ENABLED) {
			    Log.d(Config.LOGTAG, " GeatteUploadTask:onPostExecute(): try to dismiss mDialog");
			}
			mDialog.dismiss();
			mDialog = null;
		    } catch (Exception e) {
			Log.w(Config.LOGTAG, " GeatteUploadTask:onPostExecute(): failed to dismiss mDialog", e);
		    }
		}
		if (geatteId != null) {
		    saveState();
		    cleanSelectedContactsPref();
		    if(Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, " GeatteUploadTask:onPostExecute(): save my interest to db, " +
				"geatteId = " + geatteId + ", interestId = " + mInterestId);
		    }
		    Toast.makeText(getApplicationContext(), " Geatte sent successfully", Toast.LENGTH_LONG).show();
		}
		setResult(RESULT_OK);
	    } catch (Exception e) {
		Log.e(Config.LOGTAG, e.getMessage(), e);
		setResult(RESULT_CANCELED);
		Toast.makeText(getApplicationContext(), getString(R.string.upload_text_error), Toast.LENGTH_LONG)
		.show();
	    } finally {
		finish();
	    }
	}
    }

    private String getCaptionFromPref(boolean reset) {
	final SharedPreferences prefs = this.getApplicationContext().getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	String caption = prefs.getString(Config.PREF_SEND_CAPTION, "");

	if (reset) {
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.remove(Config.PREF_SEND_CAPTION);
	    editor.commit();
	}
	return caption;
    }

    private String getDescFromPref(boolean reset) {
	final SharedPreferences prefs = this.getApplicationContext().getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	String caption = prefs.getString(Config.PREF_SEND_DESC, "");

	if (reset) {
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.remove(Config.PREF_SEND_DESC);
	    editor.commit();
	}
	return caption;
    }

    private void setCaptionDescToPref(String caption, String desc) {
	final SharedPreferences prefs = this.getApplicationContext().getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	SharedPreferences.Editor editor = prefs.edit();
	editor.putString(Config.PREF_SEND_CAPTION, caption);
	editor.putString(Config.PREF_SEND_DESC, desc);
	editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	if (requestCode == ACTIVITY_SNAP && resultCode == Activity.RESULT_OK) {
	    File fi = null;
	    try {
		fi = new File(mImagePath);
	    } catch (Exception ex) {
		Log.w(Config.LOGTAG, "mImagePath not exist " + mImagePath);
	    }

	    if (fi != null && fi.exists()) {
		if (Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, " " + ShopinionSnapEditActivity.CLASSTAG
			    + " save image capture output to path : " + mImagePath);
		}

		mImageRandomId = UUID.randomUUID().toString();
		if (Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, " " + ShopinionSnapEditActivity.CLASSTAG
			    + " try upload image capture output to server as intent service, randomId : " + mImageRandomId);
		}

		//scan the image so show up in album
		MediaScannerConnection.scanFile(this,
			new String[] { mImagePath }, null,
			new MediaScannerConnection.OnScanCompletedListener() {
		    public void onScanCompleted(String path, Uri uri) {
			if(Config.LOG_DEBUG_ENABLED) {
			    Log.d(Config.LOGTAG, " ShopinionSnapEditActivity:onActivityResult() scanned : " + path);
			}
		    }
		});
		new ImageUploadAsynTask().execute(mImageRandomId);
	    } else {
		Toast.makeText(getApplicationContext(), "file not exist or file is null", Toast.LENGTH_LONG).show();
		setResult(RESULT_CANCELED);
		finish();
		Log.w(Config.LOGTAG, "file not exist or file is null");
	    }

	} else if (requestCode == ACTIVITY_CONTACT && resultCode == Activity.RESULT_OK) {
	    if (intent != null) {
		Bundle extras = intent.getExtras();
		mGeatteId = extras.getString(Config.GEATTE_ID_PARAM);
		if (mGeatteId != null) {
		    saveState();
		    cleanSelectedContactsPref();
		    if(Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, "ShopinionSnapEditActivity:onActivityResult(): item already uploaded at" +
				" contact view, save my interest to db, " +
				"geatteId = " + mGeatteId + ", interestId = " + mInterestId);
		    }
		    setResult(RESULT_OK);
		    finish();
		}
	    } else {
		checkContactsSelected();
	    }
	}
    }

}
