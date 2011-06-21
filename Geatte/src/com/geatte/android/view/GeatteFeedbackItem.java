package com.geatte.android.view;

import greendroid.widget.item.SubtitleItem;
import greendroid.widget.itemview.ItemView;

import com.geatte.android.app.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;


public class GeatteFeedbackItem extends SubtitleItem {

    public int contactDrawableId = -1;
    public Bitmap contactBitmap = null;
    public String interestImagePath = null;
    public byte[] interestThumbnail = null;

    public GeatteFeedbackItem(String title, String subtitle, int drawableId,  String interestImagePath, byte[] interestThumbnail) {
	super(title, subtitle);
	this.contactDrawableId = drawableId;
	this.interestImagePath = interestImagePath;
	this.interestThumbnail = interestThumbnail;
    }

    public GeatteFeedbackItem(String title, String subtitle, Bitmap contactBitmap,  String interestImagePath, byte[] interestThumbnail) {
	super(title, subtitle);
	this.contactBitmap = contactBitmap;
	this.interestImagePath = interestImagePath;
	this.interestThumbnail = interestThumbnail;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent) {
	return createCellFromXml(context, R.layout.geatte_feedback_item_view, parent);
    }

}
