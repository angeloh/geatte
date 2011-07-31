package com.geatte.android.view;

import com.geatte.android.app.CommonUtils;
import com.geatte.android.app.Config;
import com.geatte.android.app.R;

import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GeatteFeedbackItemView extends RelativeLayout implements ItemView {

    private TextView mTitleView;
    private TextView mSubtitleView;
    private ImageView mContactThumbnailView;
    private ImageView mInterestThumbnailView;

    public GeatteFeedbackItemView(Context context) {
	this(context, null);
    }

    public GeatteFeedbackItemView(Context context, AttributeSet attrs) {
	this(context, attrs, 0);
    }

    public GeatteFeedbackItemView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
    }

    public void prepareItemView() {
	mTitleView = (TextView) findViewById(R.id.geatte_feedback_item_title);
	mSubtitleView = (TextView) findViewById(R.id.geatte_feedback_item_subtitle);
	mContactThumbnailView = (ImageView) findViewById(R.id.geatte_feedback_item_contact_thumbnail);
	mInterestThumbnailView = (ImageView) findViewById(R.id.geatte_feedback_item_interest_thumbnail);
    }

    public void setObject(Item object) {
	final GeatteFeedbackItem item = (GeatteFeedbackItem) object;
	mTitleView.setText(item.text);
	mSubtitleView.setText(item.subtitle);
	if (item.contactBitmap != null && !item.contactBitmap.isRecycled()) {
	    mContactThumbnailView.setImageBitmap(item.contactBitmap);
	} else {
	    mContactThumbnailView.setImageResource(item.contactDrawableId);
	}

	if (item.interestImagePath != null) {
	    int sampleSize = CommonUtils.getResizeRatio(item.interestImagePath, 1500, 16);
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, " GeatteFeedbackItemView:setObject() resize image with sampleSize = " + sampleSize);
	    }
	    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
	    bitmapOptions.inSampleSize = sampleSize;
	    Bitmap imgBitmap = BitmapFactory.decodeFile(item.interestImagePath, bitmapOptions);
	    mInterestThumbnailView.setImageBitmap(imgBitmap);
	} else {
	    // interest bitmap is gone
	    mInterestThumbnailView.setImageResource(R.drawable.invalid);
	}
    }

}
