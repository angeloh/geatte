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

public class ThumbnailBitmapItemView extends RelativeLayout implements ItemView {

    private TextView mTitleView;
    private TextView mDescView;
    private ImageView mThumbnailView;

    public ThumbnailBitmapItemView(Context context) {
	this(context, null);
    }

    public ThumbnailBitmapItemView(Context context, AttributeSet attrs) {
	this(context, attrs, 0);
    }

    public ThumbnailBitmapItemView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
    }

    public void prepareItemView() {
	mTitleView = (TextView) findViewById(R.id.geatte_bitmap_item_title);
	mDescView = (TextView) findViewById(R.id.geatte_bitmap_item_desc);
	mThumbnailView = (ImageView) findViewById(R.id.geatte_bitmap_item_thumbnail);
    }

    public void setObject(Item object) {
	final ThumbnailBitmapItem item = (ThumbnailBitmapItem) object;
	mTitleView.setText(item.text);
	mDescView.setText(item.subtitle);

	int sampleSize = CommonUtils.getResizeRatio(item.imagePath, 1500, 24);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, " ThumbnailBitmapItemView:setObject() resize image with sampleSize = " + sampleSize);
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
