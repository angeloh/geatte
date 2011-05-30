package com.geatte.android.view;

import com.geatte.android.app.R;

import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ThumbnailBitmapItemView extends RelativeLayout implements ItemView {

    private TextView mTextView;
    private TextView mSubtitleView;
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
	mTextView = (TextView) findViewById(R.id.gd_text);
	mSubtitleView = (TextView) findViewById(R.id.gd_subtitle);
	mThumbnailView = (ImageView) findViewById(R.id.gd_thumbnail);
    }

    public void setObject(Item object) {
	final ThumbnailBitmapItem item = (ThumbnailBitmapItem) object;
	mTextView.setText(item.text);
	mSubtitleView.setText(item.subtitle);

	BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
	bitmapOptions.inSampleSize = 8;
	Bitmap imgBitmap = BitmapFactory.decodeFile(item.imagePath, bitmapOptions);
	mThumbnailView.setImageBitmap(imgBitmap);
    }

}
