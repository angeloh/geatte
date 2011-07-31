package com.geatte.android.view;

import com.geatte.android.app.R;

import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GeatteThumbnailItemView extends RelativeLayout implements ItemView {

    private TextView mTitleView;
    private TextView mSubtitleView;
    private ImageView mThumbnailView;

    public GeatteThumbnailItemView(Context context) {
	this(context, null);
    }

    public GeatteThumbnailItemView(Context context, AttributeSet attrs) {
	this(context, attrs, 0);
    }

    public GeatteThumbnailItemView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
    }

    public void prepareItemView() {
	mTitleView = (TextView) findViewById(R.id.geatte_thumbnail_item_title);
	mSubtitleView = (TextView) findViewById(R.id.geatte_thumbnail_item_subtitle);
	mThumbnailView = (ImageView) findViewById(R.id.geatte_thumbnail_item_thumbnail);
    }

    public void setObject(Item object) {
	final GeatteThumbnailItem item = (GeatteThumbnailItem) object;
	mTitleView.setText(item.text);
	mSubtitleView.setText(item.subtitle);
	if (item.bitmap != null && !item.bitmap.isRecycled()) {
	    mThumbnailView.setImageBitmap(item.bitmap);
	} else {
	    mThumbnailView.setImageResource(item.drawableId);
	}
    }

}
