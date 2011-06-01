package com.geatte.android.app;

import greendroid.app.GDActivity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;

import com.geatte.android.app.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class GeatteEditActivity extends GDActivity {

    private static final String CLASSTAG = GeatteEditActivity.class.getSimpleName();
    private static final String UPLOAD_PATH = "/geatteupload";

    private EditText mTitleEditText;
    private EditText mDescEditText;
    private ImageView mSnapView;
    private Long mRowId;
    private String mGeatteId;
    private String mImagePath;
    private String mSavedImagePath;
    private GeatteDBAdapter mDbHelper;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	Log.d(Config.LOGTAG, "GeatteEdit:onCreate(): START");
	super.onCreate(savedInstanceState);
	mDbHelper = new GeatteDBAdapter(this);
	mDbHelper.open();

	//setContentView(R.layout.geatte_edit);
	setActionBarContentView(R.layout.geatte_edit_view);
	setTitle(R.string.edit_geatte);

	mTitleEditText = (EditText) findViewById(R.id.title);
	mDescEditText = (EditText) findViewById(R.id.desc);
	mSnapView = (ImageView) findViewById(R.id.edit_img);

	Button sendButton = (Button) findViewById(R.id.send_to_button);

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
		Log.d(Config.LOGTAG, " " + GeatteEditActivity.CLASSTAG + " snap a new picture in " + mImagePath);
	    }
	}

	populateFields();

	mTitleEditText.setOnClickListener(new OnClickListener() {
	    String name = mTitleEditText.getText().toString();
	    String origVal = getResources().getText(R.string.edit_title_text_default).toString();

	    @Override
	    public void onClick(View v) {
		if(name.equals(origVal));
		{
		    mTitleEditText.setText("");
		}

	    }
	});

	mDescEditText.setOnClickListener(new OnClickListener() {
	    String name = mDescEditText.getText().toString();
	    String origVal = getResources().getText(R.string.edit_desc_text_default).toString();

	    @Override
	    public void onClick(View v) {
		if(name.equals(origVal));
		{
		    mDescEditText.setText("");
		}
	    }
	});

	sendButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View view) {
		if (mImagePath == null && mSavedImagePath == null) {
		    Toast.makeText(getApplicationContext(), "Please select image", Toast.LENGTH_SHORT).show();
		} else {
		    mDialog = ProgressDialog.show(GeatteEditActivity.this, "Uploading", "Please wait...", true);
		    new GeatteUploadTask().execute();
		}
		//setResult(RESULT_OK);
		//finish();
	    }

	});
	Log.d(Config.LOGTAG, "GeatteEdit:onCreate(): END");
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	Log.d(Config.LOGTAG, "GeatteEdit:onDestroy(): START");
	if (mDialog != null) {
	    Log.d(Config.LOGTAG, "GeatteEdit:onDestroy(): cancel mDialog");
	    mDialog.cancel();
	}
	if (mDbHelper != null) {
	    mDbHelper.close();
	}
	Log.d(Config.LOGTAG, "GeatteEdit:onDestroy(): END");
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
	    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
	    bitmapOptions.inSampleSize = 6;
	    Bitmap imgBitmap = BitmapFactory.decodeFile(mSavedImagePath, bitmapOptions);
	    mSnapView.setImageBitmap(imgBitmap);
	}

	// in new created mode, save the image to image view
	if (mRowId == null || mRowId == 0L) {
	    if (mImagePath != null) {
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inSampleSize = 6;
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
	updateState();
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
		Log.d(Config.LOGTAG, " " + GeatteEditActivity.CLASSTAG + " create new interest for id = " + mRowId + ", geatteId = " + mGeatteId);
		mDbHelper.insertImage(mRowId, mImagePath);
		Log.d(Config.LOGTAG, " " + GeatteEditActivity.CLASSTAG + " insert geatte image for id " + mGeatteId + " and interest = " + mRowId);
		if (mGeatteId != null) {
		    mDbHelper.updateInterestGeatteId(mRowId, mGeatteId);
		    Log.d(Config.LOGTAG, " " + GeatteEditActivity.CLASSTAG + " add geatte id " + mGeatteId + " to interest = " + mRowId);
		}
	    }
	    else {
		Log.w(Config.LOGTAG, " " + GeatteEditActivity.CLASSTAG + " unable to insert this interest!!");
	    }

	} else {
	    mDbHelper.updateInterest(mRowId, title, desc);
	    if (mGeatteId != null) {
		mDbHelper.updateInterestGeatteId(mRowId, mGeatteId);
		Log.d(Config.LOGTAG, " " + GeatteEditActivity.CLASSTAG + " update geatte id " + mGeatteId + " to interest = " + mRowId);
	    }
	    mDbHelper.updateImage(mRowId, mImagePath);
	    Log.d(Config.LOGTAG, " " + GeatteEditActivity.CLASSTAG + " update interest for id = " + mRowId);
	}
    }

    private void updateState() {
	String title = mTitleEditText.getText().toString();
	String desc = mDescEditText.getText().toString();

	if (mRowId != null && mRowId != 0L) {
	    mDbHelper.updateInterest(mRowId, title, desc);
	    if (mGeatteId != null) {
		mDbHelper.updateInterestGeatteId(mRowId, mGeatteId);
		Log.d(Config.LOGTAG, " " + GeatteEditActivity.CLASSTAG + " update geatte id " + mGeatteId + " to interest = " + mRowId);
	    }
	    mDbHelper.updateImage(mRowId, mImagePath);
	    Log.d(Config.LOGTAG, " " + GeatteEditActivity.CLASSTAG + " update interest for id = " + mRowId);
	}
    }

    class GeatteUploadTask extends AsyncTask<String, String, String> {
	@Override
	protected String doInBackground(String... strings) {
	    try {
		String title = mTitleEditText.getText().toString();
		String desc = mDescEditText.getText().toString();

		Bitmap bitmap = null;
		String imageFileName = null;
		if (mImagePath != null) {
		    //bitmap = BitmapFactory.decodeFile(mImagePath);
		    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		    bitmapOptions.inSampleSize = 6;
		    bitmap = BitmapFactory.decodeFile(mImagePath, bitmapOptions);
		    imageFileName = new File(mImagePath).getName();
		} else {
		    //bitmap = BitmapFactory.decodeFile(mSavedImagePath);
		    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		    bitmapOptions.inSampleSize = 6;
		    bitmap = BitmapFactory.decodeFile(mSavedImagePath, bitmapOptions);
		    imageFileName = new File(mSavedImagePath).getName();
		}

		if (bitmap == null) {
		    Log.e(Config.LOGTAG, "GeatteUploadTask:doInBackground(): bitmap is null, invalid imagePath = "
			    + mImagePath + " or mSavedImagePath = " + mSavedImagePath);
		}

		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 100, bos);
		byte[] data = bos.toByteArray();

		String myNumber = DeviceRegistrar.getPhoneNumber(getApplicationContext());
		entity.addPart(Config.GEATTE_FROM_NUMBER_PARAM, new StringBody(myNumber));
		// TODO to number list
		//entity.addPart(Config.GEATTE_TO_NUMBER_PARAM, new StringBody("15555215554"));
		//entity.addPart(Config.GEATTE_TO_NUMBER_PARAM, new StringBody("15103978860"));
		entity.addPart(Config.GEATTE_TO_NUMBER_PARAM, new StringBody("14085052613"));
		entity.addPart(Config.GEATTE_TITLE_PARAM, new StringBody(title));
		entity.addPart(Config.GEATTE_DESC_PARAM, new StringBody(desc));
		entity.addPart(Config.GEATTE_IMAGE_PARAM, new ByteArrayBody(data, imageFileName));

		final SharedPreferences prefs = getApplicationContext().getSharedPreferences(Config.PREFERENCE_KEY,
			Context.MODE_PRIVATE);
		String accountName = prefs.getString(Config.PREF_USER_EMAIL, null);

		AppEngineClient client = new AppEngineClient(getApplicationContext(), accountName);
		HttpResponse response = client.makeRequestWithEntity(UPLOAD_PATH, entity);

		if (response.getStatusLine().getStatusCode() == 400
			|| response.getStatusLine().getStatusCode() == 500) {
		    Log.e(Config.LOGTAG, "GeatteUploadTask Error: " + response.getStatusLine().getStatusCode() + " " + response.getEntity().getContent());
		    return null;
		}

		if (response.getEntity() != null) {

		    JSONObject JResponse = null;
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
		    try {
			JResponse = new JSONObject(body.toString());
		    } catch (JSONException e) {
			Log.e(Config.LOGTAG, "GeatteUploadTask:doInBackground(): unable to read response after upload geatte to server", e);
		    }

		    if (JResponse != null) {
			mGeatteId = JResponse.getString(Config.GEATTE_ID_PARAM);
			Log.d(Config.LOGTAG, " GeatteUploadTask:doInBackground : GOT geatteId = " + mGeatteId);
		    }

		    Log.d(Config.LOGTAG, "GeatteUploadTask Response: " + JResponse);

		    return mGeatteId;
		}
	    } catch (Exception e) {
		Log.e(Config.LOGTAG, e.getMessage(), e);
		if (mDialog != null && mDialog.isShowing()) {
		    try {
			Log.d(Config.LOGTAG, "GeatteUploadTask:doInBackground(): try to dismiss mDialog");
			mDialog.dismiss();
			mDialog = null;
		    } catch (Exception ex) {
			Log.d(Config.LOGTAG, "GeatteUploadTask:doInBackground(): failed to dismiss mDialog", ex);
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
	    try {
		if (mDialog != null && mDialog.isShowing()) {
		    try {
			Log.d(Config.LOGTAG, "GeatteUploadTask:onPostExecute(): try to dismiss mDialog");
			mDialog.dismiss();
			mDialog = null;
		    } catch (Exception e) {
			Log.w(Config.LOGTAG, "GeatteUploadTask:onPostExecute(): failed to dismiss mDialog", e);
		    }
		}
		if (geatteId != null) {
		    saveState();
		    Log.d(Config.LOGTAG, "GeatteUploadTask:onPostExecute(): save my interest to db, geatteId = " + geatteId + ", interestId = " + mRowId);
		    Toast.makeText(getApplicationContext(), "Geatte uploaded successfully, geatteId = " + geatteId,
			    Toast.LENGTH_LONG).show();
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
