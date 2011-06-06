package com.geatte.android.view;

import com.geatte.android.app.R;

import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GeatteThumbnailCheckboxView extends RelativeLayout implements ItemView {

    private CheckBox mCheckbox;
    private TextView mTitleView;
    private TextView mSubtitleView;
    private ImageView mThumbnailView;

    public GeatteThumbnailCheckboxView(Context context) {
	this(context, null);
    }

    public GeatteThumbnailCheckboxView(Context context, AttributeSet attrs) {
	this(context, attrs, 0);
    }

    public GeatteThumbnailCheckboxView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
    }

    public void prepareItemView() {
	mCheckbox = (CheckBox) findViewById(R.id.contact_check);
	mTitleView = (TextView) findViewById(R.id.contact_title);
	mSubtitleView = (TextView) findViewById(R.id.contact_subtitle);
	mThumbnailView = (ImageView) findViewById(R.id.contact_thumbnail);
    }

    public void setCheckboxClickListener(OnClickListener listener) {
	mCheckbox.setOnClickListener(listener);
    }

    public void setCheckboxTag(String phone) {
	mCheckbox.setTag(phone);
    }

    public void setCheckboxChecked(boolean checked) {
	mCheckbox.setChecked(checked);
    }

    public void setObject(Item object) {
	final GeatteThumbnailCheckbox item = (GeatteThumbnailCheckbox) object;
	mTitleView.setText(item.text);
	mSubtitleView.setText(item.subtitle);
	if (item.drawableId != -1) {
	    mThumbnailView.setImageResource(item.drawableId);
	} else if (item.bitmap != null){
	    mThumbnailView.setImageBitmap(item.bitmap);
	}
    }

}
