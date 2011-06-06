package com.geatte.android.view;

import greendroid.widget.item.SubtitleItem;
import greendroid.widget.itemview.ItemView;

import com.geatte.android.app.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;


public class GeatteThumbnailCheckbox extends SubtitleItem {

    public int drawableId = -1;

    public Bitmap bitmap = null;

    public String phone = null;

    public GeatteThumbnailCheckbox(String title, String subtitle, String phone, Bitmap bitmap) {
	super(title, subtitle);
	this.bitmap = bitmap;
	this.phone = phone;
    }

    public GeatteThumbnailCheckbox(String title, String subtitle, String phone, int drawableId) {
	super(title, subtitle);
	this.drawableId = drawableId;
	this.phone = phone;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent) {
	return createCellFromXml(context, R.layout.geatte_thumbnail_checkbox_view, parent);
    }

}
