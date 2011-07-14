package com.geatte.android.view;

import greendroid.widget.item.SubtextItem;
import greendroid.widget.itemview.ItemView;

import com.geatte.android.app.R;

import android.content.Context;
import android.view.ViewGroup;


public class InterestThumbnailItem extends SubtextItem {

    public long id;
    public String geatteId = null;
    public String imagePath = null;
    public byte[] thumbnail = null;
    public int numOfYes = 0;
    public int numOfNo = 0;
    public int numOfMaybe = 0;

    public InterestThumbnailItem(long id, String title, String desc, String imagePath) {
	super(title, desc);
	this.id = id;
	this.imagePath = imagePath;
    }

    public InterestThumbnailItem(long id, String title, String desc, String imagePath, byte[] thumbnail) {
	this(id, title, desc, imagePath);
	this.thumbnail = thumbnail;
    }

    public InterestThumbnailItem(long id, String title, String desc, String imagePath, byte[] thumbnail, int [] counters) {
	this(id, title, desc, imagePath, thumbnail);
	this.numOfYes = counters[0];
	this.numOfNo = counters[1];
	this.numOfMaybe = counters[2];
    }


    @Override
    public ItemView newView(Context context, ViewGroup parent) {
	return createCellFromXml(context, R.layout.interest_thumbnail_item_view, parent);
    }

    public long getId() {
	return id;
    }


}
