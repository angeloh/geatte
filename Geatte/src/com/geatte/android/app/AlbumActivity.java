package com.geatte.android.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class AlbumActivity extends Activity {

    private static final String CLASSTAG = AlbumActivity.class.getSimpleName();

    private ImageView mImageView;
    private String mImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "AlbumActivity:onCreate START");
	}
	super.onCreate(savedInstanceState);
	setContentView(R.layout.album_view);

	Bundle extras = getIntent().getExtras();
	mImagePath = extras != null ? extras.getString(GeatteDBAdapter.KEY_IMAGE_PATH) : null;
	if (mImagePath == null) {
	    // try fi imagePath
	    mImagePath = extras != null ? extras.getString(GeatteDBAdapter.KEY_FI_IMAGE_PATH) : null;
	}

	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "AlbumActivity:onCreate END");
	}
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
	super.onPostCreate(savedInstanceState);
	//only after parent onPostCreate, view is created
	mImageView = (ImageView) findViewById(R.id.album_image_view);
    }

    @Override
    protected void onResume() {
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "AlbumActivity:onResume() onResume START");
	}
	super.onResume();

	if (Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, " " +  AlbumActivity.CLASSTAG + " GOT mImagePath = " + mImagePath + ", populate the image view");
	}

	int sampleSize = CommonUtils.getResizeRatio(mImagePath, 1500, 8);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, " AlbumActivity:onResume() resize image with sampleSize = " + sampleSize);
	}

	BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
	bitmapOptions.inSampleSize = sampleSize;
	Bitmap imgBitmap = BitmapFactory.decodeFile(mImagePath, bitmapOptions);

	if (imgBitmap == null) {
	    mImageView.setImageResource(R.drawable.empty_list);
	    Toast.makeText(getApplicationContext(), "Image is deleted or missing!", Toast.LENGTH_LONG).show();
	    Log.w(Config.LOGTAG, "file not exist or file is null");
	} else {
	    mImageView.setImageBitmap(imgBitmap);
	}

	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "AlbumActivity:onResume() onResume END");
	}
    }

    @Override
    public void onPause() {
	super.onPause();
	if (mImageView.getDrawable() != null) {
	    BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();
	    drawable.getBitmap().recycle();
	}
	mImageView.setImageBitmap(null);
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
    }

    public void onBack(View v) {
	setResult(RESULT_OK);
	finish();
    }

}
