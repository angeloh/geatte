package com.geatte.android.view;

import com.geatte.android.app.CommonUtils;
import com.geatte.android.app.Config;
import com.geatte.android.app.R;

import greendroid.widget.AsyncImageView;
import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class InterestThumbnailItemView extends RelativeLayout implements ItemView {

    private TextView mTitleView;
    private TextView mDescView;
    private TextView mCTYesView;
    private TextView mCTMaybeView;
    private TextView mCTNoView;
    private AsyncImageView mThumbnailView;

    public InterestThumbnailItemView(Context context) {
	this(context, null);
    }

    public InterestThumbnailItemView(Context context, AttributeSet attrs) {
	this(context, attrs, 0);
    }

    public InterestThumbnailItemView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
    }

    public void prepareItemView() {
	mTitleView = (TextView) findViewById(R.id.interest_title);
	mDescView = (TextView) findViewById(R.id.interest_subtitle);
	mThumbnailView = (AsyncImageView) findViewById(R.id.interest_thumbnail);
	mCTYesView = (TextView) findViewById(R.id.ct_yes_text);
	mCTMaybeView = (TextView) findViewById(R.id.ct_maybe_text);
	mCTNoView = (TextView) findViewById(R.id.ct_no_text);
    }

    public void setObject(Item object) {
	final InterestThumbnailItem item = (InterestThumbnailItem) object;
	mTitleView.setText(item.text);
	mDescView.setText(item.subtext);
	mCTYesView.setText(item.numOfYes);
	mCTMaybeView.setText(item.numOfMaybe);
	mCTNoView.setText(item.numOfNo);

	int sampleSize = CommonUtils.getResizeRatio(item.imagePath, 1500, 16);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, " InterestThumbnailItemView:setObject() resize image with sampleSize = " + sampleSize);
	}
	BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
	bitmapOptions.inSampleSize = sampleSize;
	Bitmap imgBitmap = BitmapFactory.decodeFile(item.imagePath, bitmapOptions);
	if (imgBitmap == null) {
	    mThumbnailView.setImageResource(R.drawable.thumb_missing);
	} else {
	    mThumbnailView.setImageBitmap(imgBitmap);
	}
    }

}
