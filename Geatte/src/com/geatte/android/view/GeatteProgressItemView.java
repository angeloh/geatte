package com.geatte.android.view;

import com.geatte.android.app.R;

import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GeatteProgressItemView extends FrameLayout implements ItemView {

    private ProgressBar mProgressBar;
    private TextView mTextView;

    public GeatteProgressItemView(Context context) {
	this(context, null);
    }

    public GeatteProgressItemView(Context context, AttributeSet attrs) {
	this(context, attrs, 0);
    }

    public GeatteProgressItemView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
    }

    public void prepareItemView() {
	mProgressBar = (ProgressBar) findViewById(R.id.geatte_progress_bar);
	mTextView = (TextView) findViewById(R.id.geatte_progress_text);
    }

    public void setObject(Item object) {
	final GeatteProgressItem item = (GeatteProgressItem) object;
	mProgressBar.setVisibility(item.isInProgress ? View.VISIBLE : View.GONE);
	mTextView.setText(item.text);
    }

}
