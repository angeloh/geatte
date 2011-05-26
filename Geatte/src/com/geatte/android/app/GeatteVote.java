package com.geatte.android.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.geatte.android.app.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Show Geatte from friends
 * 
 */
public class GeatteVote extends Activity {

    private static final String CLASSTAG = GeatteVote.class.getSimpleName();
    private static final String GEATTE_VOTE_UPLOAD_PATH = "/geattevote";
    /*private static final int MENU_CALL_REVIEW = Menu.FIRST + 2;
    private static final int MENU_MAP_REVIEW = Menu.FIRST + 1;
    private static final int MENU_WEB_REVIEW = Menu.FIRST;*/
    private String mGeatteId;
    private GeatteDBAdapter mDbHelper;
    private ProgressDialog mDialog;
    //
    //    private TextView mTitleText;
    //    private TextView mDescText;
    private ImageView mGeatteVoteImage;

    /*    private Handler handler = new Handler() {
	@Override
	public void handleMessage(Message msg) {
	    if ((imageLink != null) && !imageLink.equals("")) {
		try {
		    URL url = new URL(imageLink);
		    URLConnection conn = url.openConnection();
		    conn.connect();
		    BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
		    Bitmap bm = BitmapFactory.decodeStream(bis);
		    bis.close();
		    reviewImage.setImageBitmap(bm);
		} catch (IOException e) {
		    Log.e(Constants.LOGTAG, " " + GeatteDetail.CLASSTAG, e);
		}
	    } else {
		reviewImage.setImageResource(R.drawable.no_review_image);
	    }
	}
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(Config.LOGTAG, " " + " GeatteVote:onCreate");
	mDbHelper = new GeatteDBAdapter(this);
	mDbHelper.open();

	setContentView(R.layout.geatte_vote);
	setTitle(R.string.app_name);

	//	mTitleText = (TextView) findViewById(R.id.title_detail);
	//	mDescText = (TextView) findViewById(R.id.desc_detail);
	mGeatteVoteImage = (ImageView) findViewById(R.id.geatte_vote_img);

	Bundle extras = getIntent().getExtras();
	mGeatteId = extras != null ? extras.getString(Config.GEATTE_ID_PARAM) : null;
	//	String fromNumber = extras != null ? extras.getString(Config.GEATTE_FROM_NUMBER_PARAM) : null;
	//	String title = extras != null ? extras.getString(Config.GEATTE_TITLE_PARAM) : null;
	//	String desc = extras != null ? extras.getString(Config.GEATTE_DESC_PARAM) : null;
	//	String createdDate = extras != null ? extras.getString(Config.GEATTE_CREATED_DATE_PARAM) : null;
	//	String imagePath = extras != null ? extras.getString(Config.GEATTE_IMAGE_GET_URL) : null;
	Log.d(Config.LOGTAG, " " +  GeatteVote.CLASSTAG + " GOT geatteId = " + mGeatteId + ", populate the vote view");
	populateFields();
	final Button yesButton = (Button) findViewById(R.id.geatte_vote_btn_yes);
	yesButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		mDialog = ProgressDialog.show(GeatteVote.this, "Sending YES to Your friend", "Please wait...", true);
		new VoteUploadTask().execute("YES");
	    }
	});

	final Button noButton = (Button) findViewById(R.id.geatte_vote_btn_no);
	noButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		mDialog = ProgressDialog.show(GeatteVote.this, "Sending NO to Your friend", "Please wait...", true);
		new VoteUploadTask().execute("NO");
	    }
	});

    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	Log.d(Config.LOGTAG, "GeatteVote:onDestroy(): START");
	if (mDialog != null) {
	    Log.d(Config.LOGTAG, "GeatteVote:onDestroy(): cancel mDialog");
	    mDialog.cancel();
	}
	if (mDbHelper != null) {
	    mDbHelper.close();
	}
	Log.d(Config.LOGTAG, "GeatteVote:onDestroy(): END");
    }


    //TABLE friend_interests
    //    public static final String KEY_FRIEND_INTEREST_ID = "_id";
    //    public static final String KEY_FRIEND_INTEREST_TITLE = "f_title";
    //    public static final String KEY_FRIEND_INTEREST_DESC = "f_desc";
    //    public static final String KEY_FRIEND_INTEREST_FROM = "f_from_number";
    //    public static final String KEY_FRIEND_INTEREST_CREATED_DATE = "f_created_date";
    //
    //TABLE fi_images
    //    public static final String KEY_FI_IMAGE_ID = "_id";
    //    public static final String KEY_FI_IMAGE_AS_ID = "fi_image_id";
    //    public static final String KEY_FI_IMAGE_INTEREST_ID = "fi_interest";
    //    public static final String KEY_FI_IMAGE_PATH = "fi_image_path";

    private void populateFields() {
	if (mGeatteId != null) {
	    Cursor cursor = mDbHelper.fetchFriendInterest(mGeatteId);
	    if (cursor.isAfterLast()) {
		Log.w(Config.LOGTAG, " " + GeatteVote.CLASSTAG + " unable to get record from db for geatte id = " + mGeatteId);
	    }
	    startManagingCursor(cursor);
	    //	    mTitleText.setText(cursor.getString(
	    //		    cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_TITLE)));
	    //	    mDescText.setText(cursor.getString(
	    //		    cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_DESC)));

	    try {
		String savedFIImagePath = cursor.getString(
			cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FI_IMAGE_PATH));
		if (mGeatteVoteImage == null) {
		    Log.e(Config.LOGTAG, " " + GeatteVote.CLASSTAG + " mGeatteVoteImage is null ");
		}
		if (savedFIImagePath == null) {
		    Log.e(Config.LOGTAG, " " + GeatteVote.CLASSTAG + " savedFIImagePath is null ");
		}
		if (BitmapFactory.decodeFile(savedFIImagePath) == null) {
		    Log.e(Config.LOGTAG, " " + GeatteVote.CLASSTAG + " BitmapFactory.decodeFile(savedFIImagePath) is null ");
		}
		mGeatteVoteImage.setImageBitmap(BitmapFactory.decodeFile(savedFIImagePath));
	    } catch (Exception ex) {
		Log.e(Config.LOGTAG, " " + GeatteVote.CLASSTAG + " ERROR ", ex);
	    }

	}
    }

    @Override
    protected void onResume() {
	super.onResume();
	Log.v(Config.LOGTAG, " " + GeatteVote.CLASSTAG + " onResume");
	populateFields();
    }

    class VoteUploadTask extends AsyncTask<String, String, String> {
	@Override
	protected String doInBackground(String... strings) {
	    try {
		String vote = strings[0];
		String feedback = null;
		if (strings.length > 1) {
		    feedback = strings[1];
		}
		String myNumber = DeviceRegistrar.getPhoneNumber(getApplicationContext());

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(Config.GEATTE_ID_PARAM, mGeatteId));
		params.add(new BasicNameValuePair(Config.FRIEND_GEATTE_VOTER, myNumber));
		params.add(new BasicNameValuePair(Config.FRIEND_GEATTE_VOTE_RESP, vote));
		params.add(new BasicNameValuePair(Config.FRIEND_GEATTE_FEEDBACK, feedback == null ? "" : feedback));

		final SharedPreferences prefs = getApplicationContext().getSharedPreferences(Config.PREFERENCE_KEY,
			Context.MODE_PRIVATE);
		String accountName = prefs.getString(Config.PREF_USER_EMAIL, null);

		AppEngineClient client = new AppEngineClient(getApplicationContext(), accountName);
		HttpResponse response = client.makeRequestWithParams(GEATTE_VOTE_UPLOAD_PATH, params);

		if (response.getEntity() != null) {
		    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
		    "UTF-8"));
		    String sResponse = reader.readLine();
		    Log.d(Config.LOGTAG, "VoteUploadTask Response: " + sResponse);
		    if (response.getStatusLine().getStatusCode() == 400
			    || response.getStatusLine().getStatusCode() == 500) {
			Log.e(Config.LOGTAG, "VoteUploadTask Error: " + sResponse);
			return null;
		    }
		    return sResponse;
		}
	    } catch (Exception e) {
		Log.e(Config.LOGTAG, e.getMessage(), e);
		if (mDialog != null && mDialog.isShowing()) {
		    try {
			Log.d(Config.LOGTAG, "VoteUploadTask:doInBackground(): try to dismiss mDialog");
			mDialog.dismiss();
			mDialog = null;
		    } catch (Exception ex) {
			Log.d(Config.LOGTAG, "VoteUploadTask:doInBackground(): failed to dismiss mDialog", ex);
		    }
		}
		this.publishProgress(getString(R.string.upload_vote_error));
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
	protected void onPostExecute(String sResponse) {
	    try {
		if (mDialog != null && mDialog.isShowing()) {
		    try {
			Log.d(Config.LOGTAG, "VoteUploadTask:onPostExecute(): try to dismiss mDialog");
			mDialog.dismiss();
			mDialog = null;
		    } catch (Exception e) {
			Log.d(Config.LOGTAG, "VoteUploadTask:onPostExecute(): failed to dismiss mDialog");
		    }
		}
		Log.d(Config.LOGTAG, "VoteUploadTask:onPostExecute(): get response :" + sResponse);
		// server response
		if (sResponse != null) {
		    Toast.makeText(getApplicationContext(), "Geatte vote and feedback sent successfully, result = " + sResponse,
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

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	menu.add(0, ReviewDetail.MENU_WEB_REVIEW, 0, R.string.menu_web_review).setIcon(
		android.R.drawable.ic_menu_info_details);
	menu.add(0, ReviewDetail.MENU_MAP_REVIEW, 1, R.string.menu_map_review).setIcon(
		android.R.drawable.ic_menu_mapmode);
	menu.add(0, ReviewDetail.MENU_CALL_REVIEW, 2, R.string.menu_call_review).setIcon(
		android.R.drawable.ic_menu_call);
	return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	Intent intent = null;
	switch (item.getItemId()) {
	case MENU_WEB_REVIEW:
	    Log.v(Constants.LOGTAG, " " + ReviewDetail.CLASSTAG + " WEB - " + this.link);
	    if ((this.link != null) && !this.link.equals("")) {
		intent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.link));
		startActivity(intent);
	    } else {
		new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.alert_label)).setMessage(
			R.string.no_link_message).setPositiveButton("Continue", new OnClickListener() {

			    public void onClick(final DialogInterface dialog, final int arg1) {
			    }
			}).show();
	    }
	    return true;
	case MENU_MAP_REVIEW:
	    Log.v(Constants.LOGTAG, " " + ReviewDetail.CLASSTAG + " MAP ");
	    if ((this.location.getText() != null) && !this.location.getText().equals("")) {
		intent = new Intent(Intent.ACTION_VIEW, Uri
			.parse("geo:0,0?q=" + this.location.getText().toString()));
		startActivity(intent);
	    } else {
		new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.alert_label)).setMessage(
			R.string.no_location_message).setPositiveButton("Continue", new OnClickListener() {
			    public void onClick(final DialogInterface dialog, final int arg1) {
			    }
			}).show();
	    }
	    return true;
	case MENU_CALL_REVIEW:
	    Log.v(Constants.LOGTAG, " " + ReviewDetail.CLASSTAG + " PHONE ");
	    if ((this.phone.getText() != null) && !this.phone.getText().equals("")
		    && !this.phone.getText().equals("NA")) {
		Log
		.v(Constants.LOGTAG, " " + ReviewDetail.CLASSTAG + " phone - "
			+ this.phone.getText().toString());
		String phoneString = parsePhone(this.phone.getText().toString());
		intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneString));
		startActivity(intent);
	    } else {
		new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.alert_label)).setMessage(
			R.string.no_phone_message).setPositiveButton("Continue", new OnClickListener() {
			    public void onClick(final DialogInterface dialog, final int arg1) {
			    }
			}).show();
	    }
	    return true;
	}
	return super.onMenuItemSelected(featureId, item);
    }*/
}
