package com.geatte.android.view;

import greendroid.widget.item.SubtitleItem;
import greendroid.widget.itemview.ItemView;

import com.geatte.android.app.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;


public class GeatteContactItem extends SubtitleItem {

    public int contactDrawableId = -1;
    public Bitmap contactBitmap = null;
    public String phone = null;

    public GeatteContactItem(String title, String subtitle, String phone, int drawableId) {
	super(title, subtitle);
	this.contactDrawableId = drawableId;
	this.phone = phone;
    }

    public GeatteContactItem(String title, String subtitle, String phone, Bitmap contactBitmap) {
	super(title, subtitle);
	this.contactBitmap = contactBitmap;
	this.phone = phone;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent) {
	return createCellFromXml(context, R.layout.geatte_contact_item_view, parent);
    }

}
