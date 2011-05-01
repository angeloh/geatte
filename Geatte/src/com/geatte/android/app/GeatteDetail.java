package com.geatte.android.app;

import com.geatte.android.app.R;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Show Geatte detail for review item user selected.
 * 
 */
public class GeatteDetail extends Activity {

    private static final String CLASSTAG = GeatteDetail.class.getSimpleName();
    /*private static final int MENU_CALL_REVIEW = Menu.FIRST + 2;
    private static final int MENU_MAP_REVIEW = Menu.FIRST + 1;
    private static final int MENU_WEB_REVIEW = Menu.FIRST;*/
    private Long mRowId;
    private GeatteDBAdapter mDbHelper;

    private TextView mTitleText;
    private TextView mDescText;
    private ImageView mInterestImage;

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
	mDbHelper = new GeatteDBAdapter(this);
	mDbHelper.open();

	setContentView(R.layout.geatte_detail);
	setTitle(R.string.app_name);

	mTitleText = (TextView) findViewById(R.id.title_detail);
	mDescText = (TextView) findViewById(R.id.desc_detail);
	mInterestImage = (ImageView) findViewById(R.id.image_detail);

	mRowId = (savedInstanceState == null) ? null :
	    (Long) savedInstanceState.getSerializable(GeatteDBAdapter.KEY_INTEREST_ID);
	if (mRowId == null) {
	    Bundle extras = getIntent().getExtras();
	    mRowId = extras != null ? extras.getLong(GeatteDBAdapter.KEY_INTEREST_ID)
		    : null;
	}

	populateFields();

    }

    private void populateFields() {
	if (mRowId != null) {
	    Cursor cursor = mDbHelper.fetchNote(mRowId);
	    startManagingCursor(cursor);
	    mTitleText.setText(cursor.getString(
		    cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_TITLE)));
	    mDescText.setText(cursor.getString(
		    cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_DESC)));

	    String savedImagePath = cursor.getString(
		    cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_PATH));
	    mInterestImage.setImageBitmap(BitmapFactory.decodeFile(savedImagePath));

	}
    }

    @Override
    protected void onResume() {
	super.onResume();
	Log.v(Config.LOGTAG, " " + GeatteDetail.CLASSTAG + " onResume");
	populateFields();
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
