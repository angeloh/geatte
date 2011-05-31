package com.geatte.android.view;

import greendroid.widget.item.SubtitleItem;
import greendroid.widget.itemview.ItemView;

import com.geatte.android.app.R;

import android.content.Context;
import android.view.ViewGroup;


public class GeatteThumbnailItem extends SubtitleItem {

    /**
     * The resource ID for the drawable.
     */
    public int drawableId;


    public GeatteThumbnailItem(String title, String subtitle, int drawableId) {
	super(title, subtitle);
	this.drawableId = drawableId;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent) {
	return createCellFromXml(context, R.layout.geattethumbnail_item_view, parent);
    }

}
