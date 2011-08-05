package com.geatte.android.view;

import java.io.File;
import java.util.UUID;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import com.geatte.android.app.Config;
import com.geatte.android.app.GeatteDBAdapter;
import com.geatte.android.app.GeatteImageUploadIntentService;
import com.geatte.android.app.R;
import com.geatte.android.app.ShopinionAllFeedbackActivity;
import com.geatte.android.app.ShopinionEditTextActivity;
import com.geatte.android.app.ShopinionFIListActivity;
import com.geatte.android.app.ShopinionMainActivity;
import com.geatte.android.app.ShopinionSnapEditActivity;

/**
 * An equivalent to {@link ListActivity} that manages a ListView.
 * 
 * @see {@link ListActivity}
 */
public abstract class AppFooterActionbarActivity extends GDActivity {

    private static final String LOG_TAG = AppFooterActionbarActivity.class.getSimpleName();
    private static final int ACTIVITY_SNAP = 0;
    private static final int ACTIVITY_PICK = 1;
    protected String mImagePath = null;
    protected View m_shopinion_footer = null;
    protected Button m_shopinion_mi_btn = null;
    protected Button m_shopinion_fi_btn = null;
    protected Button m_shopinion_snap_btn = null;
    protected Button m_shopinion_pick_btn = null;
    protected Button m_shopinion_all_btn = null;

    public AppFooterActionbarActivity() {
	super();
    }

    public AppFooterActionbarActivity(ActionBar.Type actionBarType) {
	super(actionBarType);
    }

    @Override
    public int createLayout() {
	if (Config.LOG_INFO_ENABLED) {
	    Log.i(Config.LOGTAG, "No layout specified : creating the default layout");
	}

	switch (getActionBarType()) {
	case Dashboard:
	case Empty:
	case Normal:
	default:
	    return R.layout.default_app_footer_actionbar;
	}
    }

    @Override
    protected boolean verifyLayout() {
	return super.verifyLayout() && m_shopinion_footer != null;
    }

    @Override
    public void onPreContentChanged() {
	super.onPreContentChanged();
	m_shopinion_footer = findViewById(R.id.shopinion_footer);

	View tmpView = findViewById(R.id.shopinion_mi_btn);
	if (tmpView != null) {
	    m_shopinion_mi_btn = (Button) tmpView;
	}
	tmpView = findViewById(R.id.shopinion_fi_btn);
	if (tmpView != null) {
	    m_shopinion_fi_btn = (Button) tmpView;
	}
	tmpView = findViewById(R.id.shopinion_snap_btn);
	if (tmpView != null) {
	    m_shopinion_snap_btn = (Button) tmpView;
	}
	tmpView = findViewById(R.id.shopinion_pick_btn);
	if (tmpView != null) {
	    m_shopinion_pick_btn = (Button) tmpView;
	}
	tmpView = findViewById(R.id.shopinion_all_btn);
	if (tmpView != null) {
	    m_shopinion_all_btn = (Button) tmpView;
	}
	if (m_shopinion_footer == null) {
	    throw new RuntimeException("Your content must have a LinearLayout whose id attribute is " + "'shopinion_footer'");
	}
    }

    @Override
    public void onPostContentChanged() {
	super.onPostContentChanged();
	if (m_shopinion_mi_btn != null) {
	    m_shopinion_mi_btn.setOnClickListener( new OnClickListener(){
		public void onClick(View v ) {
		    onMIBtnClick(v);
		}
	    });
	}
	if (m_shopinion_fi_btn != null) {
	    m_shopinion_fi_btn.setOnClickListener( new OnClickListener(){
		public void onClick(View v ) {
		    onFIBtnClick(v);
		}
	    });
	}
	if (m_shopinion_snap_btn != null) {
	    m_shopinion_snap_btn.setOnClickListener( new OnClickListener(){
		public void onClick(View v ) {
		    onSnapBtnClick(v);
		}
	    });
	}
	if (m_shopinion_pick_btn != null) {
	    m_shopinion_pick_btn.setOnClickListener( new OnClickListener(){
		public void onClick(View v ) {
		    onPickBtnClick(v);
		}
	    });
	}
	if (m_shopinion_all_btn != null) {
	    m_shopinion_all_btn.setOnClickListener( new OnClickListener(){
		public void onClick(View v ) {
		    onAllBtnClick(v);
		}
	    });
	}
    }

    public void onMIBtnClick(View v ) {
	Intent intent = new Intent(this, ShopinionMainActivity.class);
	startActivity(intent);
    }

    public void onFIBtnClick(View v ) {
	Intent intent = new Intent(this, ShopinionFIListActivity.class);
	startActivity(intent);
    }

    public void onSnapBtnClick(View v ) {
	Intent intent = new Intent(this, ShopinionSnapEditActivity.class);
	startActivity(intent);
	//	Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	//	mImagePath = createImagePath();
	//	intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mImagePath)));
	//	if(Config.LOG_DEBUG_ENABLED) {
	//	    Log.d(Config.LOGTAG, " " + AppFooterActionbarActivity.LOG_TAG + " Will put a EXTRA_OUTPUT for image capture to " + mImagePath);
	//	}
	//
	//	startActivityForResult(intent, ACTIVITY_SNAP);
    }

    public void onPickBtnClick(View v ) {
	Intent intent = new Intent();
	intent.setType("image/*");
	intent.setAction(Intent.ACTION_GET_CONTENT);
	startActivityForResult(Intent.createChooser(intent, "Select Picture"), ACTIVITY_PICK);
    }

    public void onAllBtnClick(View v ) {
	Intent intent = new Intent(getApplicationContext(), ShopinionAllFeedbackActivity.class);
	startActivity(intent);
    }

    @Override
    public void setActionBarContentView(int resID) {
	throwSetActionBarContentViewException();
    }

    @Override
    public void setActionBarContentView(View view, LayoutParams params) {
	throwSetActionBarContentViewException();
    }

    @Override
    public void setActionBarContentView(View view) {
	throwSetActionBarContentViewException();
    }

    private void throwSetActionBarContentViewException() {
	throw new UnsupportedOperationException(
		"The setActionBarContentView method is not supported for GDListActivity. " +
	"In order to get a custom layout you must return a layout identifier in createLayout");

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
		    Log.d(Config.LOGTAG, " " + AppFooterActionbarActivity.LOG_TAG
			    + " save image capture output to path : " + mImagePath);
		}

		String randomId = UUID.randomUUID().toString();
		if (Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, " " + AppFooterActionbarActivity.LOG_TAG
			    + " try upload image capture output to server as intent service, randomId : " + randomId);
		}

		new ImageUploadAsynTask().execute(randomId);

		Intent editIntent = new Intent(this.getApplicationContext(), ShopinionEditTextActivity.class);
		editIntent.putExtra(GeatteDBAdapter.KEY_IMAGE_PATH, mImagePath);
		editIntent.putExtra(Config.EXTRA_IMAGE_RANDOM_ID, randomId);
		startActivity(editIntent);

	    } else {
		Log.w(Config.LOGTAG, "file not exist or file is null");
	    }

	} else if (requestCode == ACTIVITY_PICK && resultCode == Activity.RESULT_OK) {
	    Uri selectedImageUri = intent.getData();

	    // OI FILE Manager
	    String fileManagerString = selectedImageUri.getPath();

	    // MEDIA GALLERY
	    String selectedImagePath = getPathFromMediaStore(selectedImageUri);

	    if (selectedImagePath != null) {
		if (Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, " " + AppFooterActionbarActivity.LOG_TAG
			    + " user chose a image from MEDIA GALLERY " + selectedImagePath);
		}
		mImagePath = selectedImagePath;
	    } else if (fileManagerString != null) {
		if (Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, " " + AppFooterActionbarActivity.LOG_TAG
			    + " user chose a image from OI FILE Manager " + fileManagerString);
		}
		mImagePath = fileManagerString;
	    }

	    File fi = null;
	    try {
		fi = new File(mImagePath);
	    } catch (Exception ex) {
		Log.w(Config.LOGTAG, "mImagePath not exist " + mImagePath);
	    }

	    if (fi != null && fi.exists()) {
		String randomId = UUID.randomUUID().toString();
		if (Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, " " + AppFooterActionbarActivity.LOG_TAG
			    + " try upload user-selected image to server as intent service, randomId : " + randomId);
		}

		new ImageUploadAsynTask().execute(randomId);
		Intent editIntent = new Intent(this.getApplicationContext(), ShopinionEditTextActivity.class);
		editIntent.putExtra(GeatteDBAdapter.KEY_IMAGE_PATH, mImagePath);
		editIntent.putExtra(Config.EXTRA_IMAGE_RANDOM_ID, randomId);
		startActivity(editIntent);
	    } else {
		Log.w(Config.LOGTAG, "file not exist or file is null");
	    }
	}
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

    public String getPathFromMediaStore(Uri uri) {
	String[] projection = { MediaStore.Images.Media.DATA };
	Cursor cursor = managedQuery(uri, projection, null, null, null);
	if (cursor != null) {
	    // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
	    // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	} else
	    return null;
    }

    //    public String createImagePath() {
    //	String filename;
    //	Date date = new Date();
    //	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    //	filename = sdf.format(date);
    //
    //	try {
    //	    if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
    //		String path = Environment.getExternalStorageDirectory().toString();
    //		File dir = new File(path, "/geatte/media/geatte images/");
    //		if (!dir.isDirectory()) {
    //		    dir.mkdirs();
    //		}
    //
    //		File file = new File(dir, filename + ".jpg");
    //		return file.getAbsolutePath();
    //	    } else {
    //		//no external storage available
    //		File file = File.createTempFile("geatte_", ".jpg");
    //		return file.getAbsolutePath();
    //	    }
    //
    //	} catch (Exception e) {
    //	    Log.w(Config.LOGTAG, " " + AppFooterActionbarActivity.LOG_TAG + " Exception :", e);
    //	}
    //	return null;
    //    }

}
