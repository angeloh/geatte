package com.geatte.android.view;

import greendroid.widget.item.SubtextItem;
import greendroid.widget.itemview.ItemView;

import com.geatte.android.app.R;

import android.content.Context;
import android.view.ViewGroup;


public class InterestFriendThumbnailItem extends SubtextItem {

    public long id;
    public String geatteId = null;
    public String imagePath = null;
    public byte[] thumbnail = null;
    public String sendByText = null;
    public String sendOnText = null;
    public String voteText = null;
    public String voteFeedbackText = null;

    public InterestFriendThumbnailItem(long id, String title, String desc, String imagePath) {
	super(title, desc);
	this.id = id;
	this.imagePath = imagePath;
    }

    public InterestFriendThumbnailItem(long id, String title, String desc, String imagePath, byte[] thumbnail) {
	this(id, title, desc, imagePath);
	this.thumbnail = thumbnail;
    }

    public InterestFriendThumbnailItem(long id, String title, String desc, String imagePath, byte[] thumbnail,
	    String sendByText, String sendOnText, String voteText, String voteFeedbackText) {
	this(id, title, desc, imagePath, thumbnail);
	this.sendByText = sendByText;
	this.sendOnText = sendOnText;
	this.voteText = voteText;
	this.voteFeedbackText = voteFeedbackText;
    }


    @Override
    public ItemView newView(Context context, ViewGroup parent) {
	return createCellFromXml(context, R.layout.interest_f_thumbnail_item_view, parent);
    }

    public long getId() {
	return id;
    }


}
