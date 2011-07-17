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

public class InterestFriendThumbnailItemView extends RelativeLayout implements ItemView {

    private TextView mTitleView;
    private TextView mDescView;
    private TextView mSentByView;
    private TextView mSentOnView;
    private TextView mVoteTextView;
    private TextView mVoteFeedbackView;
    private AsyncImageView mThumbnailView;

    public InterestFriendThumbnailItemView(Context context) {
	this(context, null);
    }

    public InterestFriendThumbnailItemView(Context context, AttributeSet attrs) {
	this(context, attrs, 0);
    }

    public InterestFriendThumbnailItemView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
    }

    public void prepareItemView() {
	mTitleView = (TextView) findViewById(R.id.f_interest_title);
	mDescView = (TextView) findViewById(R.id.f_interest_subtitle);
	mThumbnailView = (AsyncImageView) findViewById(R.id.f_interest_thumbnail);
	mSentByView = (TextView) findViewById(R.id.fi_sent_by_text);
	mSentOnView = (TextView) findViewById(R.id.fi_sent_on_text);
	mVoteTextView = (TextView) findViewById(R.id.fi_vote_text);
	mVoteFeedbackView = (TextView) findViewById(R.id.fi_vote_feedback);
    }

    public void setObject(Item object) {
	final InterestFriendThumbnailItem item = (InterestFriendThumbnailItem) object;
	mTitleView.setText(item.text);
	mDescView.setText(item.subtext);
	mSentByView.setText(item.sendByText);
	mSentOnView.setText(item.sendOnText);
	mVoteTextView.setText(item.voteText);
	mVoteFeedbackView.setText(item.voteFeedbackText);

	int sampleSize = CommonUtils.getResizeRatio(item.imagePath, 250, 12);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, " InterestFriendThumbnailItemView:setObject() resize image with sampleSize = " + sampleSize);
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
