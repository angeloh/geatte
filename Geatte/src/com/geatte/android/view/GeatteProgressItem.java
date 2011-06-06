package com.geatte.android.view;

import greendroid.widget.item.TextItem;
import greendroid.widget.itemview.ItemView;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.geatte.android.app.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class GeatteProgressItem extends TextItem {

    private static final boolean DEFAULT_IS_IN_PROGRESS = false;

    /**
     * The state of this item. When set to true, a circular progress bar
     * indicates something is going on/being computed.
     */
    public boolean isInProgress;

    /**
     * @hide
     */
    public GeatteProgressItem() {
	this(null);
    }

    /**
     * Constructs a ProgressItem with the given text. By default, the circular
     * progress bar is not visible ... which indicates nothing is currently in
     * progress.
     * 
     * @param text The text for this item
     */
    public GeatteProgressItem(String text) {
	this(text, DEFAULT_IS_IN_PROGRESS);
    }

    /**
     * Constructs a ProgressItem with the given text and state.
     * 
     * @param text The text for this item
     * @param isInProgress The state for this item
     */
    public GeatteProgressItem(String text, boolean isInProgress) {
	super(text);
	this.isInProgress = isInProgress;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent) {
	return createCellFromXml(context, R.layout.geatte_progress_item_view, parent);
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException,
    IOException {
	super.inflate(r, parser, attrs);

	TypedArray a = r.obtainAttributes(attrs, R.styleable.GeatteProgressItem);
	isInProgress = a.getBoolean(R.styleable.GeatteProgressItem_isInProgressAttr, DEFAULT_IS_IN_PROGRESS);
	a.recycle();
    }

}
