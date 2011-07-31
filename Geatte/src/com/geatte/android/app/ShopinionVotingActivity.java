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
import greendroid.widget.ActionBar.OnActionBarListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class ShopinionVotingActivity extends GDActivity {

    private static final String CLASSTAG = ShopinionVotingActivity.class.getSimpleName();
    private static final String GEATTE_VOTE_UPLOAD_PATH = "/geattevote";
    private static final int ACTIVITY_COMMENT = 0;
    private ImageView mGeatteVoteImage;
    private AsyncImageView mVotingThumbnail;
    private String mGeatteId;
    private ProgressDialog mDialog;
    private String mLike = null;
    private String mComment = null;
    private CommentPopupWidget mPopup;
    private Config.BACK_STYLE mIsHomeBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionVotingActivity:onCreate START");
	}
	super.onCreate(savedInstanceState);

	Bundle extras = getIntent().getExtras();
	mGeatteId = extras != null ? extras.getString(Config.GEATTE_ID_PARAM) : null;

	String isHomeBar = extras != null ? extras.getString(Config.ACTION_VOTING_BAR_HOME) : Config.BACK_STYLE.HOME.toString();
	if (isHomeBar == null) {
	    mIsHomeBar = Config.BACK_STYLE.HOME;
	} else {
	    mIsHomeBar = Config.BACK_STYLE.valueOf(isHomeBar);
	}
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionVotingActivity:onCreate get extra mIsHomeBar = " + mIsHomeBar);
	}

	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionVotingActivity:onCreate END");
	}
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
	super.onPostCreate(savedInstanceState);
	mGeatteVoteImage = (ImageView) findViewById(R.id.voting_image_view);
	mVotingThumbnail = (AsyncImageView) findViewById(R.id.voting_thumbnail);
    }

    @Override
    protected void onResume() {
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionVotingActivity:onResume() onResume START");
	}
	super.onResume();
	Log.i(Config.LOGTAG, " " +  ShopinionVotingActivity.CLASSTAG + " GOT geatteId = " + mGeatteId + ", populate the vote view");
	populateFields();
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionVotingActivity:onResume() onResume END");
	}
    }

    @Override
    public void onPause() {
	super.onPause();
	if (mGeatteVoteImage.getDrawable() != null) {
	    BitmapDrawable drawable = (BitmapDrawable) mGeatteVoteImage.getDrawable();
	    drawable.getBitmap().recycle();
	}
	mGeatteVoteImage.setImageBitmap(null);
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionVotingActivity:onDestroy(): START");
	}
	if (mDialog != null) {
	    mDialog.cancel();
	}
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionVotingActivity:onDestroy(): END");
	}
    }

    private void populateFields() {
	if (mGeatteId != null) {
	    final GeatteDBAdapter dbHelper = new GeatteDBAdapter(this);
	    Cursor fiCursor = null;
	    try {
		dbHelper.open();
		fiCursor = dbHelper.fetchFriendInterest(mGeatteId);

		if (fiCursor.isAfterLast()) {
		    Log.w(Config.LOGTAG, " " + ShopinionVotingActivity.CLASSTAG + " unable to get record from db for geatte id = " + mGeatteId);
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
		    Log.e(Config.LOGTAG, " " + ShopinionVotingActivity.CLASSTAG + " mGeatteVoteImage is null ");
		}
		if (savedFIImagePath == null) {
		    Log.e(Config.LOGTAG, " " + ShopinionVotingActivity.CLASSTAG + " savedFIImagePath is null ");
		}
		if (BitmapFactory.decodeFile(savedFIImagePath) == null) {
		    Log.e(Config.LOGTAG, " " + ShopinionVotingActivity.CLASSTAG + " BitmapFactory.decodeFile(savedFIImagePath) is null ");
		}
		mGeatteVoteImage.setImageBitmap(BitmapFactory.decodeFile(savedFIImagePath));
		if (savedFITitle != null && savedFITitle.length() > 0) {
		    setTitle(savedFITitle);
		} else {
		    setTitle(R.string.voting_a_geatte);
		}

	    } catch (Exception ex) {
		Log.e(Config.LOGTAG, " " + ShopinionVotingActivity.CLASSTAG + " ERROR ", ex);
	    } finally {
		fiCursor.close();
		dbHelper.close();
	    }

	} else {
	    Log.e(Config.LOGTAG, " " + ShopinionVotingActivity.CLASSTAG + " can not populate the fields when mGeatteId is null ");
	}
    }

    private void saveState(String geatteId, String vote, String comment) {

	final GeatteDBAdapter dbHelper = new GeatteDBAdapter(this);

	try {
	    dbHelper.open();

	    long ret = dbHelper.insertFIFeedback(geatteId, vote, comment);

	    if (ret >= 0) {
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "ShopinionVotingActivity:saveState() : saved feedback for friend interest for geatteId = " + geatteId
			    + ", vote = " + vote + ", comment = " + comment + " to DB SUCCESSUL!");
		}
	    } else {
		Log.w(Config.LOGTAG, " ShopinionVotingActivity:saveState() : saved contact for friend interest for geatteId = " + geatteId
			+ ", vote = " + vote + ", comment = " + comment + " to DB FAILED!");
	    }

	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "ShopinionVotingActivity:saveState: exception", e);
	} finally {
	    dbHelper.close();
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
		String myCountryCode = DeviceRegistrar.getPhoneConuntryCode(getApplicationContext());
		params.add(new BasicNameValuePair(Config.FRIEND_GEATTE_COUNTRY_ISO, myCountryCode));
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
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "VoteUploadTask:doInBackground(): save feedback for friend interest to db, " +
			    "geatteId = " + mGeatteId + ", vote = " + vote + ", feedback = " + feedback);
		}

		if (response.getEntity() != null) {
		    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
		    "UTF-8"));
		    String sResponse = reader.readLine();
		    if(Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, "VoteUploadTask Response: " + sResponse);
		    }
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
			if(Config.LOG_DEBUG_ENABLED) {
			    Log.d(Config.LOGTAG, "VoteUploadTask:doInBackground(): try to dismiss mDialog");
			}
			mDialog.dismiss();
			mDialog = null;
		    } catch (Exception ex) {
			Log.w(Config.LOGTAG, "VoteUploadTask:doInBackground(): failed to dismiss mDialog", ex);
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
			if(Config.LOG_DEBUG_ENABLED) {
			    Log.d(Config.LOGTAG, "VoteUploadTask:onPostExecute(): try to dismiss mDialog");
			}
			mDialog.dismiss();
			mDialog = null;
		    } catch (Exception e) {
			Log.w(Config.LOGTAG, "VoteUploadTask:onPostExecute(): failed to dismiss mDialog");
		    }
		}
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "VoteUploadTask:onPostExecute(): get response :" + sResponse);
		}

		// server response
		if (sResponse != null) {
		    Toast.makeText(getApplicationContext(), "You feedback sent successfully", Toast.LENGTH_LONG).show();
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
	mVotingThumbnail.setDefaultImageResource(R.drawable.v_yes);
    }

    public void onMaybe(View v) {
	mLike = Config.LIKE.MAYBE.toString();
	mVotingThumbnail.setDefaultImageResource(R.drawable.v_maybe);
    }

    public void onNo(View v) {
	mLike = Config.LIKE.NO.toString();
	mVotingThumbnail.setDefaultImageResource(R.drawable.v_no);
    }

    public void onComment(View v) {
	/// dialog way
	//	Intent i = new Intent(this, GeatteVotingCommentActivity.class);
	//	startActivityForResult(i, ACTIVITY_COMMENT);

	/// popupwindow way
	//	// get the instance of the LayoutInflater
	//	LayoutInflater inflater = (LayoutInflater) ShopinionVotingActivity:this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
	//		    Log.d(Config.LOGTAG, " " + ShopinionVotingActivity:CLASSTAG + " return from comment typing, comment = " + mComment);
	//		} else {
	//		    Log.d(Config.LOGTAG, " " + ShopinionVotingActivity:CLASSTAG + " return from comment typing, comment is null or empty");
	//		}
	//		finish();
	//	    }
	//	});
	//	// finally show the popup in the center of the window
	//	pw.showAtLocation(layout, Gravity.BOTTOM, 0, 0);

	///override popupwindow
	mPopup = new CommentPopupWidget(this);
	mPopup.show(v);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	if (requestCode == ACTIVITY_COMMENT && resultCode == Activity.RESULT_OK){
	    mComment = intent.getExtras().getString(Config.EXTRA_KEY_VOTING_COMMENT);
	    if(Config.LOG_DEBUG_ENABLED) {
		if (mComment != null && !mComment.trim().equals("")) {
		    Log.d(Config.LOGTAG, " " + ShopinionVotingActivity.CLASSTAG + " return from comment typing, comment = " + mComment);
		} else {
		    Log.d(Config.LOGTAG, " " + ShopinionVotingActivity.CLASSTAG + " return from comment typing, comment is null or empty");
		}
	    }
	}
    }

    public void onSend(View v) {
	if (mLike != null) {
	    mDialog = ProgressDialog.show(ShopinionVotingActivity.this, "Sending to Your friend", "Please wait...", true);

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

    @Override
    public int createLayout() {
	if (mIsHomeBar == Config.BACK_STYLE.LIST) {
	    return R.layout.shopinion_voting_view_actionbar_back_to_list;
	} else if (mIsHomeBar == Config.BACK_STYLE.GRID) {
	    return R.layout.shopinion_voting_view_actionbar_back_to_grid;
	} else {
	    return R.layout.shopinion_voting_view;
	}
    }

    @Override
    public void onPreContentChanged() {
	super.onPreContentChanged();
	getActionBar().setOnActionBarListener(mActionBarOnVotingListener);
    }

    private OnActionBarListener mActionBarOnVotingListener = new OnActionBarListener() {
	public void onActionBarItemClicked(int position) {
	    if (position == OnActionBarListener.HOME_ITEM) {
		switch (mIsHomeBar) {
		case GRID:
		    if (Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, "Going back to the grid fi activity");
		    }
		    Intent intentG = new Intent(ShopinionVotingActivity.this, ShopinionFIGridActivity.class);
		    startActivity(intentG);
		    break;
		case LIST:
		    if (Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, "Going back to the list fi activity");
		    }
		    Intent intentM = new Intent(ShopinionVotingActivity.this, ShopinionFIListActivity.class);
		    startActivity(intentM);
		    break;
		case HOME:
		default:
		    Intent intentD = new Intent(ShopinionVotingActivity.this, ShopinionMainActivity.class);
		    startActivity(intentD);
		    break;
		}

	    } else {
		if (!onHandleActionBarItemClick(getActionBar().getItem(position), position)) {
		    if (Config.LOG_DEBUG_ENABLED) {
			Log.w(Config.LOGTAG, "Click on item at position " + position + " dropped down to the floor");
		    }
		}
	    }
	}
    };
}
