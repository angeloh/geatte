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
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
    private static final int ACTIVITY_COMMENT = 0;
    private ImageView mGeatteVoteImage;
    private AsyncImageView mVotingThumbnail;
    private String mGeatteId;
    private ProgressDialog mDialog;
    private String mLike = null;
    private String mComment = null;
    private CommentPopupWidget mPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	Log.d(Config.LOGTAG, "GeatteVotingActivity:onCreate START");
	super.onCreate(savedInstanceState);
	setActionBarContentView(R.layout.geatte_voting_view);
	mGeatteVoteImage = (ImageView) findViewById(R.id.voting_image_view);
	mVotingThumbnail = (AsyncImageView) findViewById(R.id.voting_thumbnail);

	Bundle extras = getIntent().getExtras();
	mGeatteId = extras != null ? extras.getString(Config.GEATTE_ID_PARAM) : null;

	Log.d(Config.LOGTAG, " " +  GeatteVotingActivity.CLASSTAG + " GOT geatteId = " + mGeatteId + ", populate the vote view");
	populateFields();
	Log.d(Config.LOGTAG, "GeatteVotingActivity:onCreate END");
    }

    @Override
    protected void onResume() {
	Log.d(Config.LOGTAG, "GeatteVotingActivity.onResume() onResume START");
	super.onResume();
	populateFields();
	Log.d(Config.LOGTAG, "GeatteVotingActivity.onResume() onResume END");
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

    private void saveState(String geatteId, String vote, String comment) {

	final GeatteDBAdapter dbHelper = new GeatteDBAdapter(this);

	try {
	    dbHelper.open();

	    long ret = dbHelper.insertFIFeedback(geatteId, vote, comment);

	    if (ret >= 0) {
		Log.d(Config.LOGTAG, "GeatteVotingActivity:saveState() : saved feedback for friend interest for geatteId = " + geatteId
			+ ", vote = " + vote + ", comment = " + comment + " to DB SUCCESSUL!");
	    } else {
		Log.d(Config.LOGTAG, " GeatteVotingActivity:saveState() : saved contact for friend interest for geatteId = " + geatteId
			+ ", vote = " + vote + ", comment = " + comment + " to DB FAILED!");
	    }

	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "GeatteVotingActivity:saveState: exception", e);
	} finally {
	    dbHelper.close();
	}

    }
    //	DB_TABLE_FI_FEEDBACKS + "." + KEY_FI_FEEDBACK_GEATTE_ID + ", " +
    //	DB_TABLE_FI_FEEDBACKS + "." + KEY_FI_FEEDBACK_VOTE + ", " +
    //	DB_TABLE_FI_FEEDBACKS + "." + KEY_FI_FEEDBACK_COMMENT + ", " +
    //	DB_TABLE_FI_FEEDBACKS + "." + KEY_FI_FEEDBACK_CREATED_DATE + " " +

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

		//save feedback to db
		saveState(mGeatteId, vote, feedback);
		Log.d(Config.LOGTAG, "VoteUploadTask:doInBackground(): save feedback for friend interest to db, " +
			"geatteId = " + mGeatteId + ", vote = " + vote + ", feedback = " + feedback);

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
	/// dialog way
	//	Intent i = new Intent(this, GeatteVotingCommentActivity.class);
	//	startActivityForResult(i, ACTIVITY_COMMENT);

	/// popupwindow way
	//	// get the instance of the LayoutInflater
	//	LayoutInflater inflater = (LayoutInflater) GeatteVotingActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	//	// inflate our view from the corresponding XML file
	//	View layout = inflater.inflate(R.layout.geatte_vote_comment_edit_view, (ViewGroup)findViewById(R.id.voting_comment_root));
	//	// create a 100px width and 200px height popup window
	//	pw = new PopupWindow(layout, 300, 150, true);
	//	// set actions to buttons we have in our popup
	//	Button btnCancel = (Button)layout.findViewById(R.id.voting_cancel_button);
	//	btnCancel.setOnClickListener(new OnClickListener() {
	//	    @Override
	//	    public void onClick(View vv) {
	//		// close the popup
	//		pw.dismiss();
	//	    }
	//	});
	//	final EditText commentEditText = (EditText) findViewById(R.id.voting_comment);
	//
	//	Button btnOk = (Button)layout.findViewById(R.id.voting_ok_button);
	//	btnOk.setOnClickListener(new OnClickListener() {
	//	    @Override
	//	    public void onClick(View vv) {
	//		mComment = commentEditText.getText().toString();
	//		if (mComment != null && !mComment.trim().equals("")) {
	//		    Log.d(Config.LOGTAG, " " + GeatteVotingActivity.CLASSTAG + " return from comment typing, comment = " + mComment);
	//		} else {
	//		    Log.d(Config.LOGTAG, " " + GeatteVotingActivity.CLASSTAG + " return from comment typing, comment is null or empty");
	//		}
	//		finish();
	//	    }
	//	});
	//	// finally show the popup in the center of the window
	//	pw.showAtLocation(layout, Gravity.BOTTOM, 0, 0);

	///override popupwindow
	mPopup = new CommentPopupWidget(this.getApplicationContext());
	mPopup.show(v);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	if (requestCode == ACTIVITY_COMMENT && resultCode == Activity.RESULT_OK){
	    mComment = intent.getExtras().getString(Config.EXTRA_KEY_VOTING_COMMENT);
	    if (mComment != null && !mComment.trim().equals("")) {
		Log.d(Config.LOGTAG, " " + GeatteVotingActivity.CLASSTAG + " return from comment typing, comment = " + mComment);
	    } else {
		Log.d(Config.LOGTAG, " " + GeatteVotingActivity.CLASSTAG + " return from comment typing, comment is null or empty");
	    }
	}
    }

    public void onSend(View v) {
	if (mLike != null) {
	    mDialog = ProgressDialog.show(GeatteVotingActivity.this, "Sending to Your friend", "Please wait...", true);

	    final SharedPreferences prefs = this.getApplicationContext().getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	    mComment = prefs.getString(Config.PREF_VOTING_COMMENT, null);

	    new VoteUploadTask().execute(mLike, mComment);

	    SharedPreferences.Editor editor = prefs.edit();
	    editor.remove(Config.PREF_VOTING_COMMENT);
	    editor.commit();
	} else {
	    Toast.makeText(getApplicationContext(), getString(R.string.voting_choose_answer), Toast.LENGTH_LONG)
	    .show();
	}
    }
}
