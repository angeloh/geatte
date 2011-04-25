package com.geatte.mobile;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class GeatteEdit extends Activity {

    private static final String CLASSTAG = GeatteEdit.class.getSimpleName();
    public static final String EXTRA_IMAGE = "extra_image";
    private EditText mTitleText;
    private EditText mDescText;
    private ImageView mSnapView;
    private Long mRowId;
    private Bitmap mImageBitmap;
    private GeatteDBAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	mDbHelper = new GeatteDBAdapter(this);
	mDbHelper.open();

	setContentView(R.layout.geatte_edit);
	setTitle(R.string.edit_geatte);

	mTitleText = (EditText) findViewById(R.id.title);
	mDescText = (EditText) findViewById(R.id.desc);
	mSnapView = (ImageView)findViewById(R.id.edit_img);

	Button sendButton = (Button) findViewById(R.id.send_button);

	mRowId = (savedInstanceState == null) ? null :
	    (Long) savedInstanceState.getSerializable(GeatteDBAdapter.KEY_INTEREST_ID);
	if (mRowId == null) {
	    Bundle extras = getIntent().getExtras();
	    long id = extras.getLong(GeatteDBAdapter.KEY_INTEREST_ID);
	    mRowId = id != 0L ? id : null;
	}

	// in new created mode, read the image from extras
	if (mRowId == null || mRowId == 0L) {
	    if (mImageBitmap == null) {
		Bundle extras = getIntent().getExtras();
		mImageBitmap = (Bitmap) (extras != null ? extras.get(GeatteDBAdapter.KEY_IMAGE_IMAGE) : null);
	    }
	}

	populateFields();

	sendButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View view) {
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
	    mTitleText.setText(cursor.getString(
		    cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_TITLE)));
	    mDescText.setText(cursor.getString(
		    cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_DESC)));
	    byte[] byteArr = cursor.getBlob(
		    cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_IMAGE));

	    mSnapView.setImageBitmap(BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length));
	}

	// in new created mode, save the image to image view
	if (mRowId == null || mRowId == 0L) {
	    if (mImageBitmap != null) {
		mSnapView.setImageBitmap(mImageBitmap);
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
	    mDbHelper.insertImage(mRowId, mImageBitmap);
	    Log.d(Constants.LOGTAG, " " + GeatteEdit.CLASSTAG + " create new interest for id = " + mRowId);
	} else {
	    mDbHelper.updateInterest(mRowId, title, desc);
	    mDbHelper.updateImage(mRowId, mImageBitmap);
	    Log.d(Constants.LOGTAG, " " + GeatteEdit.CLASSTAG + " update interest for id = " + mRowId);
	}
    }

}
