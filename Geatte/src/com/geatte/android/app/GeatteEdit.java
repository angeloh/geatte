package com.geatte.android.app;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import com.geatte.android.app.R;
import android.app.Activity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class GeatteEdit extends Activity {

    private static final String CLASSTAG = GeatteEdit.class.getSimpleName();
    private static final String UPLOAD_PATH = "/geatteupload";
    // public static final String EXTRA_IMAGE = "extra_image";

    private EditText mTitleText;
    private EditText mDescText;
    private ImageView mSnapView;
    private Long mRowId;
    private String mImagePath;
    private String mSavedImagePath;
    private GeatteDBAdapter mDbHelper;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	mDbHelper = new GeatteDBAdapter(this);
	mDbHelper.open();

	setContentView(R.layout.geatte_edit);
	setTitle(R.string.edit_geatte);

	mTitleText = (EditText) findViewById(R.id.title);
	mDescText = (EditText) findViewById(R.id.desc);
	mSnapView = (ImageView) findViewById(R.id.edit_img);

	Button sendButton = (Button) findViewById(R.id.send_button);

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
	    }
	}

	populateFields();

	sendButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View view) {
		if (mImagePath == null || mSavedImagePath == null) {
		    Toast.makeText(getApplicationContext(), "Please select image", Toast.LENGTH_SHORT).show();
		} else {
		    mDialog = ProgressDialog.show(GeatteEdit.this, "Uploading", "Please wait...", true);
		    new ImageUploadTask().execute();
		}
		setResult(RESULT_OK);
		finish();
	    }

	});
    }

    private void populateFields() {
	// in edit mode
	if (mRowId != null && mRowId != 0L) {
	    Cursor cursor = mDbHelper.fetchNote(mRowId);
	    startManagingCursor(cursor);
	    mTitleText.setText(cursor.getString(cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_TITLE)));
	    mDescText.setText(cursor.getString(cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_DESC)));

	    mSavedImagePath = cursor.getString(cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_PATH));
	    mSnapView.setImageBitmap(BitmapFactory.decodeFile(mSavedImagePath));
	}

	// in new created mode, save the image to image view
	if (mRowId == null || mRowId == 0L) {
	    if (mImagePath != null) {
		mSnapView.setImageBitmap(BitmapFactory.decodeFile(mImagePath));
	    }
	}
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	saveState();
	outState.putSerializable(GeatteDBAdapter.KEY_INTEREST_ID, mRowId);
    }

    @Override
    protected void onPause() {
	super.onPause();
	saveState();
    }

    @Override
    protected void onResume() {
	super.onResume();
	populateFields();
    }

    private void saveState() {
	String title = mTitleText.getText().toString();
	String desc = mDescText.getText().toString();

	if (mRowId == null || mRowId == 0L) {
	    long id = mDbHelper.insertInterest(title, desc);
	    if (id > 0) {
		mRowId = id;
	    }
	    mDbHelper.insertImage(mRowId, mImagePath);
	    Log.d(Config.LOGTAG, " " + GeatteEdit.CLASSTAG + " create new interest for id = " + mRowId);
	} else {
	    mDbHelper.updateInterest(mRowId, title, desc);
	    mDbHelper.updateImage(mRowId, mImagePath);
	    Log.d(Config.LOGTAG, " " + GeatteEdit.CLASSTAG + " update interest for id = " + mRowId);
	}
    }

    class ImageUploadTask extends AsyncTask<Void, Void, String> {
	@Override
	protected String doInBackground(Void... unsued) {
	    try {
		String title = mTitleText.getText().toString();
		String desc = mDescText.getText().toString();

		// HttpClient httpClient = new DefaultHttpClient();
		// HttpContext localContext = new BasicHttpContext();
		// HttpPost httpPost = new HttpPost(Config.BASE_URL +
		// UPLOAD_PATH);

		Bitmap bitmap = null;
		String imageFileName = null;
		if (mImagePath != null) {
		    bitmap = BitmapFactory.decodeFile(mImagePath);
		    imageFileName = new File(mImagePath).getName();
		} else {
		    bitmap = BitmapFactory.decodeFile(mSavedImagePath);
		    imageFileName = new File(mSavedImagePath).getName();
		}

		if (bitmap == null) {
		    Log.e(Config.LOGTAG, " " + GeatteEdit.CLASSTAG + " bitmap is null, invalid imagePath = "
			    + mImagePath + " or mSavedImagePath = " + mSavedImagePath);
		}

		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 100, bos);
		byte[] data = bos.toByteArray();

		String myNumber = DeviceRegistrar.getPhoneNumber(getApplicationContext());
		entity.addPart(Config.FROM_NUMBER_PARAM, new StringBody(myNumber));
		// TODO to number list
		entity.addPart(Config.TO_NUMBER_PARAM, new StringBody("TONUMBER"));
		entity.addPart(Config.GEATTE_TITLE_PARAM, new StringBody(title));
		entity.addPart(Config.GEATTE_DESC_PARAM, new StringBody(desc));
		entity.addPart(Config.GEATTE_IMAGE_PARAM, new ByteArrayBody(data, imageFileName));

		final SharedPreferences prefs = getApplicationContext().getSharedPreferences(Config.PREFERENCE_KEY,
			Context.MODE_PRIVATE);
		String accountName = prefs.getString(Config.PREF_USER_EMAIL, null);

		AppEngineClient client = new AppEngineClient(getApplicationContext(), accountName);
		HttpResponse response = client.makeRequest(UPLOAD_PATH, entity);

		// httpPost.setEntity(entity);
		// HttpResponse response = httpClient.execute(httpPost,
		// localContext);
		if (response.getEntity() != null) {
		    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
		    "UTF-8"));
		    String sResponse = reader.readLine();
		    Log.d(Config.LOGTAG, "ImageUploadTask Response: " + sResponse);
		    if (response.getStatusLine().getStatusCode() == 400
			    || response.getStatusLine().getStatusCode() == 500) {
			Log.e(Config.LOGTAG, "ImageUploadTask Error: " + sResponse);
			return null;
		    }
		    return sResponse;
		}
	    } catch (Exception e) {
		if (mDialog.isShowing()) {
		    mDialog.dismiss();
		}
		Toast.makeText(getApplicationContext(), getString(R.string.upload_text_error), Toast.LENGTH_LONG)
		.show();
		Log.e(Config.LOGTAG, e.getMessage(), e);
	    }
	    return null;
	}

	@Override
	protected void onProgressUpdate(Void... unsued) {

	}

	@Override
	protected void onPostExecute(String sResponse) {
	    try {
		if (mDialog.isShowing()) {
		    mDialog.dismiss();
		}

		// TODO server response
		if (sResponse != null) {
		    // JSONObject JResponse = new JSONObject(sResponse);
		    // int success = JResponse.getInt("SUCCESS");
		    // String message = JResponse.getString("MESSAGE");
		    // if (success == 0) {
		    // Toast.makeText(getApplicationContext(), message,
		    // Toast.LENGTH_LONG).show();
		    // } else {
		    Toast.makeText(getApplicationContext(), "Geatte uploaded successfully, GeatteId = " + sResponse,
			    Toast.LENGTH_SHORT).show();
		    // }
		}
	    } catch (Exception e) {
		Toast.makeText(getApplicationContext(), getString(R.string.upload_text_error), Toast.LENGTH_LONG)
		.show();
		Log.e(Config.LOGTAG, e.getMessage(), e);
	    }
	}
    }

}
