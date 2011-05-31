package com.geatte.android.view;

import greendroid.widget.item.SubtitleItem;
import greendroid.widget.itemview.ItemView;

import com.geatte.android.app.R;

import android.content.Context;
import android.view.ViewGroup;


public class ThumbnailBitmapItem extends SubtitleItem {

    String imagePath = null;

    public ThumbnailBitmapItem(String title, String desc, String imagePath) {
	super(title, desc);
	this.imagePath = imagePath;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent) {
	return createCellFromXml(context, R.layout.geatte_thumbnail_bitmap_item_view, parent);
    }

}
