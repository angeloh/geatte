package com.geatte.android.app;

import greendroid.app.GDActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLDecoder;

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
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;

@Deprecated
public class GeatteEditUploadTextOnlyActivity extends GDActivity {
    private static final String CLASSTAG = GeatteEditUploadTextOnlyActivity.class.getSimpleName();
    private static final String UPLOAD_PATH = "/geatteuploadtextonly";
    private static final int ACTIVITY_CONTACT = 0;

    private EditText mTitleEditText;
    private EditText mDescEditText;
    private ImageView mSnapView;
    private Long mRowId;
    private String mGeatteId;
    private String mImagePath;
    private String mImageRandomId;
    private String mSavedImagePath;
    private GeatteDBAdapter mDbHelper;
    private ProgressDialog mDialog;
    private Button mSendToButton;
    private Button mSendGeatteButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
	Log.d(Config.LOGTAG, "GeatteEdit:onCreate(): START");
	super.onCreate(savedInstanceState);
	mDbHelper = new GeatteDBAdapter(this);
	mDbHelper.open();

	//setContentView(R.layout.geatte_edit);
	setActionBarContentView(R.layout.geatte_edit_view);
	setTitle(R.string.edit_interest);

	mTitleEditText = (EditText) findViewById(R.id.title);
	mDescEditText = (EditText) findViewById(R.id.desc);
	mSnapView = (ImageView) findViewById(R.id.edit_img);

	mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState
		.getSerializable(GeatteDBAdapter.KEY_INTEREST_ID);
	if (mRowId == null) {
	    Bundle extras = getIntent().getExtras();
	    long id = extras.getLong(GeatteDBAdapter.KEY_INTEREST_ID);
	    mRowId = id != 0L ? id : null;
	}

	// in new created mode, read the image from extras
	if (mRowId == null || mRowId == 0L) {
	    if (mImagePath == null) {
		Bundle extras = getIntent().getExtras();
		mImagePath = (String) (extras != null ? extras.get(GeatteDBAdapter.KEY_IMAGE_PATH) : null);
		mImageRandomId = (String) (extras != null ? extras.get(Config.EXTRA_IMAGE_RANDOM_ID) : null);
		Log.d(Config.LOGTAG, " GeatteEditUploadTextOnlyActivity:onCreate() scanned : snap a new picture in " + mImagePath + ", and mImageRandomId = " + mImageRandomId);
		if (mImagePath == null || mImageRandomId == null) {
		    setResult(RESULT_CANCELED);
		    finish();
		    return;
		}
		//scan the image so show up in album
		MediaScannerConnection.scanFile(this,
			new String[] { mImagePath }, null,
			new MediaScannerConnection.OnScanCompletedListener() {
		    public void onScanCompleted(String path, Uri uri) {
			if(Config.LOG_DEBUG_ENABLED) {
			    Log.d(Config.LOGTAG, " GeatteEditUploadTextOnlyActivity:onCreate() scanned : " + path);
			}
		    }
		});
	    }
	}

	//populateFields();

	mSendToButton = (Button) findViewById(R.id.send_to_button);
	mSendToButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View view) {
		Intent intent = new Intent(getApplicationContext(), GeatteContactSelectActivity.class);
		startActivityForResult(intent, ACTIVITY_CONTACT);
	    }

	});

	mSendGeatteButton = (Button) findViewById(R.id.send_geatte_button);
	mSendGeatteButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View view) {
		if (mImagePath == null && mSavedImagePath == null) {
		    Toast.makeText(getApplicationContext(), "No image to send", Toast.LENGTH_SHORT).show();
		    setResult(RESULT_CANCELED);
		    finish();
		} else {
		    mDialog = ProgressDialog.show(GeatteEditUploadTextOnlyActivity.this, "Sending to friends", "Please wait...", true);
		    new GeatteUploadTask().execute();
		}
		//setResult(RESULT_OK);
		//finish();
	    }

	});

	checkContactsSelected();

	Log.d(Config.LOGTAG, "GeatteEdit:onCreate(): END");
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	Log.d(Config.LOGTAG, "GeatteEdit:onDestroy(): START");
	if (mDialog != null) {
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, "GeatteEdit:onDestroy(): cancel mDialog");
	    }
	    mDialog.cancel();
	}
	if (mDbHelper != null) {
	    mDbHelper.close();
	}
	Log.d(Config.LOGTAG, "GeatteEdit:onDestroy(): END");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	if (requestCode == ACTIVITY_CONTACT && resultCode == Activity.RESULT_OK){
	    checkContactsSelected();
	}
    }

    private void checkContactsSelected() {
	Context context = getApplicationContext();
	final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	String selectedContacts = prefs.getString(Config.PREF_SELECTED_CONTACTS, null);
	if (selectedContacts != null && selectedContacts.length() > 0) {
	    mSendGeatteButton.setEnabled(true);
	    mSendToButton.setText(R.string.send_to_reset_text);
	} else {
	    mSendGeatteButton.setEnabled(false);
	    mSendToButton.setText(R.string.send_to_text);
	}
    }

    private void populateFields() {
	// in edit mode
	if (mRowId != null && mRowId != 0L) {
	    Cursor cursor = mDbHelper.fetchMyInterest(mRowId);
	    startManagingCursor(cursor);
	    mTitleEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_TITLE)));
	    mDescEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_DESC)));

	    mSavedImagePath = cursor.getString(cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_PATH));
	    //mSnapView.setImageBitmap(BitmapFactory.decodeFile(mSavedImagePath));
	    int sampleSize = CommonUtils.getResizeRatio(mSavedImagePath, 1500, 6);
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, " GeatteEditUploadTextOnlyActivity:populateFields() resize image with sampleSize = " + sampleSize);
	    }
	    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
	    bitmapOptions.inSampleSize = sampleSize;
	    Bitmap imgBitmap = BitmapFactory.decodeFile(mSavedImagePath, bitmapOptions);
	    mSnapView.setImageBitmap(imgBitmap);
	}

	// in new created mode, save the image to image view
	if (mRowId == null || mRowId == 0L) {
	    if (mImagePath != null) {
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		int sampleSize = CommonUtils.getResizeRatio(mImagePath, 1500, 6);
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, " GeatteEditUploadTextOnlyActivity:populateFields() resize image with sampleSize = " + sampleSize);
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
	updateState();
	outState.putSerializable(GeatteDBAdapter.KEY_INTEREST_ID, mRowId);
    }

    @Override
    protected void onPause() {
	super.onPause();
    }

    @Override
    protected void onResume() {
	super.onResume();
	populateFields();
    }

    private void saveState() {
	String title = mTitleEditText.getText().toString();
	String desc = mDescEditText.getText().toString();

	if (mRowId == null || mRowId == 0L) {
	    long id = mDbHelper.insertInterest(title, desc);
	    if (id >= 0) {
		mRowId = id;
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, " " + GeatteEditUploadTextOnlyActivity.CLASSTAG + " create new interest for id = " + mRowId + ", geatteId = " + mGeatteId);
		}
		mDbHelper.insertImage(mRowId, mImagePath);
		Log.i(Config.LOGTAG, " " + GeatteEditUploadTextOnlyActivity.CLASSTAG + " insert geatte image for id " + mGeatteId + " and interest = " + mRowId);
		if (mGeatteId != null) {
		    mDbHelper.updateInterestGeatteId(mRowId, mGeatteId);
		    Log.i(Config.LOGTAG, " " + GeatteEditUploadTextOnlyActivity.CLASSTAG + " add geatte id " + mGeatteId + " to interest = " + mRowId);
		}
	    }
	    else {
		Log.w(Config.LOGTAG, " " + GeatteEditUploadTextOnlyActivity.CLASSTAG + " unable to insert this interest!!");
	    }

	} else {
	    mDbHelper.updateInterest(mRowId, title, desc);
	    if (mGeatteId != null) {
		mDbHelper.updateInterestGeatteId(mRowId, mGeatteId);
		Log.i(Config.LOGTAG, " " + GeatteEditUploadTextOnlyActivity.CLASSTAG + " update geatte id " + mGeatteId + " to interest = " + mRowId);
	    }
	    mDbHelper.updateImage(mRowId, mImagePath);
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, " " + GeatteEditUploadTextOnlyActivity.CLASSTAG + " update interest for id = " + mRowId);
	    }
	}
    }

    private void updateState() {
	String title = mTitleEditText.getText().toString();
	String desc = mDescEditText.getText().toString();

	if (mRowId != null && mRowId != 0L) {
	    mDbHelper.updateInterest(mRowId, title, desc);
	    if (mGeatteId != null) {
		mDbHelper.updateInterestGeatteId(mRowId, mGeatteId);
		Log.i(Config.LOGTAG, " " + GeatteEditUploadTextOnlyActivity.CLASSTAG + " update geatte id " + mGeatteId + " to interest = " + mRowId);
	    }
	    mDbHelper.updateImage(mRowId, mImagePath);
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, " " + GeatteEditUploadTextOnlyActivity.CLASSTAG + " update interest for id = " + mRowId);
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

    class GeatteUploadTask extends AsyncTask<String, String, String> {
	@Override
	protected String doInBackground(String... strings) {
	    try {
		String title = mTitleEditText.getText().toString();
		String desc = mDescEditText.getText().toString();

		Context context = getApplicationContext();
		final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
		String selectedContacts = prefs.getString(Config.PREF_SELECTED_CONTACTS, null);

		if (selectedContacts == null || selectedContacts.length() == 0) {
		    Log.e(Config.LOGTAG, "GeatteUploadTask:doInBackground(): selectedContacts is null, invalid selectedContacts = "
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
		HttpResponse response = client.makeRequestWithEntity(UPLOAD_PATH, entity);

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
					Log.d(Config.LOGTAG, "GeatteUploadTask:doInBackground(): try to dismiss mDialog");
				    }
				    mDialog.dismiss();
				    mDialog = null;
				} catch (Exception ex) {
				    Log.w(Config.LOGTAG, "GeatteUploadTask:doInBackground(): failed to dismiss mDialog", ex);
				}
			    }
			    this.publishProgress(getString(R.string.upload_text_retry));
			    Log.w(Config.LOGTAG, "GeatteUploadTask Got RETRY, body = " + body.toString());
			    return Config.RETRY_STATUS;
			} else {
			    Log.w(Config.LOGTAG, "GeatteUploadTask Error: " + respStatusCode + " " + body.toString());
			    throw new Exception("GeatteUploadTask Error: " + respStatusCode + " " + body.toString());
			}
		    }

		    try {
			jResponse = new JSONObject(URLDecoder.decode((body.toString()==null ? "" : body.toString()), Config.ENCODE_UTF8));
		    } catch (JSONException e) {
			Log.e(Config.LOGTAG, "GeatteUploadTask:doInBackground(): unable to read response after upload geatte to server", e);
		    }

		    if (jResponse != null) {
			mGeatteId = jResponse.getString(Config.GEATTE_ID_PARAM);
			Log.d(Config.LOGTAG, " GeatteUploadTask:doInBackground : GOT geatteId = " + mGeatteId);
		    }

		    if(Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, "GeatteUploadTask Response: " + jResponse);
		    }

		    return mGeatteId;
		}
	    } catch (Exception e) {
		Log.e(Config.LOGTAG, e.getMessage(), e);
		if (mDialog != null && mDialog.isShowing()) {
		    try {
			if(Config.LOG_DEBUG_ENABLED) {
			    Log.d(Config.LOGTAG, "GeatteUploadTask:doInBackground(): try to dismiss mDialog");
			}
			mDialog.dismiss();
			mDialog = null;
		    } catch (Exception ex) {
			Log.w(Config.LOGTAG, "GeatteUploadTask:doInBackground(): failed to dismiss mDialog", ex);
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
		Log.i(Config.LOGTAG, "GeatteUploadTask:onPostExecute(): got retry");
		return;
	    }
	    try {
		if (mDialog != null && mDialog.isShowing()) {
		    try {
			if(Config.LOG_DEBUG_ENABLED) {
			    Log.d(Config.LOGTAG, "GeatteUploadTask:onPostExecute(): try to dismiss mDialog");
			}
			mDialog.dismiss();
			mDialog = null;
		    } catch (Exception e) {
			Log.w(Config.LOGTAG, "GeatteUploadTask:onPostExecute(): failed to dismiss mDialog", e);
		    }
		}
		if (geatteId != null) {
		    saveState();
		    cleanSelectedContactsPref();
		    if(Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, "GeatteUploadTask:onPostExecute(): save my interest to db, geatteId = " + geatteId + ", interestId = " + mRowId);
		    }
		    Toast.makeText(getApplicationContext(), "Geatte sent successfully", Toast.LENGTH_LONG).show();
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

}
