package com.geatte.android.view;

import greendroid.widget.item.SubtitleItem;
import greendroid.widget.itemview.ItemView;

import com.geatte.android.app.R;

import android.content.Context;
import android.view.ViewGroup;


public class ThumbnailAsyncBitmapItem extends SubtitleItem {

    public long id;
    public String geatteId = null;
    public String imagePath = null;
    public byte[] thumbnail = null;

    public ThumbnailAsyncBitmapItem(long id, String title, String desc, String imagePath) {
	super(title, desc);
	this.id = id;
	this.imagePath = imagePath;
    }

    public ThumbnailAsyncBitmapItem(long id, String title, String desc, String imagePath, byte[] thumbnail) {
	super(title, desc);
	this.id = id;
	this.imagePath = imagePath;
	this.thumbnail = thumbnail;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent) {
	return createCellFromXml(context, R.layout.geatte_thumbnail_async_bitmap_item_view, parent);
    }

    public long getId() {
	return id;
    }


}
