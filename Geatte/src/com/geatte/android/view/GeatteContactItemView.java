package com.geatte.android.view;

import com.geatte.android.app.R;

import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GeatteContactItemView extends RelativeLayout implements ItemView {

    private TextView mTitleView;
    private TextView mSubtitleView;
    private ImageView mContactThumbnailView;

    public GeatteContactItemView(Context context) {
	this(context, null);
    }

    public GeatteContactItemView(Context context, AttributeSet attrs) {
	this(context, attrs, 0);
    }

    public GeatteContactItemView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
    }

    public void prepareItemView() {
	mTitleView = (TextView) findViewById(R.id.geatte_contact_item_title);
	mSubtitleView = (TextView) findViewById(R.id.geatte_contact_item_subtitle);
	mContactThumbnailView = (ImageView) findViewById(R.id.geatte_contact_item_thumbnail);
    }

    public void setObject(Item object) {
	final GeatteContactItem item = (GeatteContactItem) object;
	mTitleView.setText(item.text);
	mSubtitleView.setText(item.subtitle);
	if (item.contactBitmap != null) {
	    mContactThumbnailView.setImageBitmap(item.contactBitmap);
	} else {
	    mContactThumbnailView.setImageResource(item.contactDrawableId);
	}

    }

}
