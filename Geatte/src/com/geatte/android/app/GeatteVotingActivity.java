package com.geatte.android.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import greendroid.app.GDActivity;
import greendroid.widget.AsyncImageView;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class GeatteVotingActivity extends GDActivity {

    private static final String CLASSTAG = GeatteVotingActivity.class.getSimpleName();
    private static final String GEATTE_VOTE_UPLOAD_PATH = "/geattevote";
    private ImageView mGeatteVoteImage;
    private AsyncImageView mVotingThumbnail;
    private String mGeatteId;
    private ProgressDialog mDialog;
    private String mLike = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setActionBarContentView(R.layout.geatte_voting_view);
	mGeatteVoteImage = (ImageView) findViewById(R.id.voting_image_view);
	mVotingThumbnail = (AsyncImageView) findViewById(R.id.voting_thumbnail);

	Log.d(Config.LOGTAG, " " + " GeatteVotingActivity:onCreate");

	Bundle extras = getIntent().getExtras();
	mGeatteId = extras != null ? extras.getString(Config.GEATTE_ID_PARAM) : null;

	Log.d(Config.LOGTAG, " " +  GeatteVotingActivity.CLASSTAG + " GOT geatteId = " + mGeatteId + ", populate the vote view");
	populateFields();
    }

    @Override
    protected void onResume() {
	super.onResume();
	Log.v(Config.LOGTAG, " " + GeatteVotingActivity.CLASSTAG + " onResume");
	populateFields();
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	Log.d(Config.LOGTAG, "GeatteVotingActivity:onDestroy(): START");
	if (mDialog != null) {
	    Log.d(Config.LOGTAG, "GeatteVotingActivity:onDestroy(): cancel mDialog");
	    mDialog.cancel();
	}
	Log.d(Config.LOGTAG, "GeatteVotingActivity:onDestroy(): END");
    }

    private void populateFields() {
	if (mGeatteId != null) {
	    final GeatteDBAdapter dbHelper = new GeatteDBAdapter(this);
	    Cursor fiCursor = null;
	    try {
		dbHelper.open();
		fiCursor = dbHelper.fetchFriendInterest(mGeatteId);

		if (fiCursor.isAfterLast()) {
		    Log.w(Config.LOGTAG, " " + GeatteVotingActivity.CLASSTAG + " unable to get record from db for geatte id = " + mGeatteId);
		}
		//	    mTitleText.setText(cursor.getString(
		//		    cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_TITLE)));
		//	    mDescText.setText(cursor.getString(
		//		    cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_DESC)));

		String savedFIImagePath = fiCursor.getString(
			fiCursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FI_IMAGE_PATH));
		String savedFITitle = fiCursor.getString(
			fiCursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FRIEND_INTEREST_TITLE));


		if (mGeatteVoteImage == null) {
		    Log.e(Config.LOGTAG, " " + GeatteVotingActivity.CLASSTAG + " mGeatteVoteImage is null ");
		}
		if (savedFIImagePath == null) {
		    Log.e(Config.LOGTAG, " " + GeatteVotingActivity.CLASSTAG + " savedFIImagePath is null ");
		}
		if (BitmapFactory.decodeFile(savedFIImagePath) == null) {
		    Log.e(Config.LOGTAG, " " + GeatteVotingActivity.CLASSTAG + " BitmapFactory.decodeFile(savedFIImagePath) is null ");
		}
		mGeatteVoteImage.setImageBitmap(BitmapFactory.decodeFile(savedFIImagePath));
		if (savedFITitle != null) {
		    setTitle(savedFITitle);
		} else {
		    setTitle(R.string.voting_a_geatte);
		}

	    } catch (Exception ex) {
		Log.e(Config.LOGTAG, " " + GeatteVotingActivity.CLASSTAG + " ERROR ", ex);
	    } finally {
		fiCursor.close();
		dbHelper.close();
	    }

	} else {
	    Log.e(Config.LOGTAG, " " + GeatteVotingActivity.CLASSTAG + " can not populate the fields when mGeatteId is null ");
	}
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

    public void onYes(View v) {
	mLike = Config.LIKE.YES.toString();
	mVotingThumbnail.setDefaultImageResource(R.drawable.green_light);
    }

    public void onMaybe(View v) {
	mLike = Config.LIKE.MAYBE.toString();
	mVotingThumbnail.setDefaultImageResource(R.drawable.warning);
    }

    public void onNo(View v) {
	mLike = Config.LIKE.NO.toString();
	mVotingThumbnail.setDefaultImageResource(R.drawable.red_light);
    }

    public void onComment(View v) {
    }

    public void onSend(View v) {
	if (mLike != null) {
	    mDialog = ProgressDialog.show(GeatteVotingActivity.this, "Sending to Your friend", "Please wait...", true);
	    new VoteUploadTask().execute("YES");
	} else {
	    Toast.makeText(getApplicationContext(), getString(R.string.voting_choose_answer), Toast.LENGTH_LONG)
	    .show();
	}
    }
}