package com.geatte.android.view;

import greendroid.widget.item.SubtitleItem;
import greendroid.widget.itemview.ItemView;

import com.geatte.android.app.R;

import android.content.Context;
import android.view.ViewGroup;


public class SeparatorThumbnailItem extends SubtitleItem {

    /**
     * The resource ID for the drawable.
     */
    public int drawableId;


    public SeparatorThumbnailItem(String text, int drawableId) {
	super(text, null);
	this.drawableId = drawableId;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent) {
	return createCellFromXml(context, R.layout.geatte_separator_thumbnail_item_view, parent);
    }

}
