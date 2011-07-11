package com.geatte.android.view;

import com.geatte.android.app.R;

import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;

import android.content.Context;
import android.view.ViewGroup;


public class GridBitmapItem extends Item {

    public long id;
    public String geatteId = null;
    public String imagePath = null;
    public byte[] thumbnail = null;

    public GridBitmapItem(long id, String imagePath) {
	super();
	this.id = id;
	this.imagePath = imagePath;
    }

    public GridBitmapItem(long id, String imagePath, byte[] thumbnail) {
	super();
	this.id = id;
	this.imagePath = imagePath;
	this.thumbnail = thumbnail;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent) {
	return createCellFromXml(context, R.layout.grid_bitmap_item_view, parent);
    }

    public long getId() {
	return id;
    }


}
