package com.geatte.android.view;

import com.geatte.android.app.R;

import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SeparatorThumbnailItemView extends RelativeLayout implements ItemView {

    private TextView mTextView;
    private ImageView mThumbnailView;

    public SeparatorThumbnailItemView(Context context) {
	this(context, null);
    }

    public SeparatorThumbnailItemView(Context context, AttributeSet attrs) {
	this(context, attrs, 0);
    }

    public SeparatorThumbnailItemView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
    }

    public void prepareItemView() {
	mTextView = (TextView) findViewById(R.id.geatte_separator_text);
	mThumbnailView = (ImageView) findViewById(R.id.geatte_separator_thumbnail);
    }

    public void setObject(Item object) {
	final SeparatorThumbnailItem item = (SeparatorThumbnailItem) object;
	mTextView.setText(item.text);
	mThumbnailView.setImageResource(item.drawableId);
    }

}
