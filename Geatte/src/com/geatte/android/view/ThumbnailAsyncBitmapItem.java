package com.geatte.android.view;

import greendroid.image.ImageProcessor;
import greendroid.widget.item.SubtitleItem;
import greendroid.widget.itemview.ItemView;

import com.geatte.android.app.R;

import android.content.Context;
import android.view.ViewGroup;


public class ThumbnailAsyncBitmapItem extends SubtitleItem {

    String imagePath = null;
    ImageProcessor mImageProcessor;

    public ThumbnailAsyncBitmapItem(String title, String desc, String imagePath, ImageProcessor imageProcessor) {
	super(title, desc);
	this.imagePath = imagePath;
	this.mImageProcessor = imageProcessor;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent) {
	return createCellFromXml(context, R.layout.geatte_thumbnail_async_bitmap_item_view, parent);
    }

}
