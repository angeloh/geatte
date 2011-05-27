package com.geatte.android.app;

import greendroid.app.GDListActivity;
import greendroid.widget.ItemAdapter;
import greendroid.widget.item.DescriptionItem;
import greendroid.widget.item.Item;
import greendroid.widget.item.ProgressItem;
import greendroid.widget.item.SeparatorItem;
import greendroid.widget.item.ThumbnailItem;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class GeatteAllFeedbackActivity extends GDListActivity {

    private final Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(Config.LOGTAG, "START GeatteAllFeedbackActivity:onCreate");
	setTitle(R.string.app_name);

	List<Item> items = new ArrayList<Item>();
	final ThumbnailItem warnItem;

	// create db helper
	GeatteDBAdapter mDbHelper = new GeatteDBAdapter(this);
	mDbHelper.open();
	Cursor feedbackCur = mDbHelper.fetchAllMyInterestFeedback();

	Log.d(Config.LOGTAG, "Got cursor for all feedbacks");

	feedbackCur.moveToFirst();
	if (feedbackCur.isAfterLast()) {
	    Log.d(Config.LOGTAG, "No feedback available!!");
	    warnItem = new ThumbnailItem("No feedback available", null, R.drawable.android_pressed);
	} else {
	    warnItem = null;
	}

	int counter = 0;
	while (feedbackCur.isAfterLast() == false) {
	    ++counter;
	    Log.d(Config.LOGTAG, "Process feedback = " + counter);
	    items.add(new SeparatorItem(feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_VOTER))));

	    String vote = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_VOTE));
	    if (vote.equals("YES")) {
		items.add(new ThumbnailItem(vote, null, R.drawable.android_focused));
	    } else {
		items.add(new ThumbnailItem(vote, null, R.drawable.android_normal));
	    }

	    String comment = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_COMMENT));
	    if (comment != null) {
		items.add(new DescriptionItem(comment));
	    }
	    feedbackCur.moveToNext();

	}
	feedbackCur.close();
	mDbHelper.close();

	final ProgressItem progressItem = new ProgressItem("Retrieving feedbacks", true);
	items.add(progressItem);

	final ItemAdapter adapter = new ItemAdapter(this, items);
	setListAdapter(adapter);

	mHandler.postDelayed(new Runnable() {
	    public void run() {
		if (warnItem != null) {
		    adapter.insert(warnItem, 0);
		}
		adapter.remove(progressItem);
		adapter.notifyDataSetChanged();
	    }
	},500);
	Log.d(Config.LOGTAG, "END GeatteAllFeedbackActivity:onCreate");
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
    }

}
