package com.geatte.android.app;

import greendroid.app.GDListActivity;
import greendroid.widget.ItemAdapter;
import greendroid.widget.item.DescriptionItem;
import greendroid.widget.item.Item;
import greendroid.widget.item.SeparatorItem;
import greendroid.widget.item.ThumbnailItem;
import greendroid.widget.itemview.ItemView;

import java.util.ArrayList;
import java.util.List;

import com.geatte.android.view.ThumbnailBitmapItem;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class GeatteFeedbackActivity extends GDListActivity {

    private final Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(Config.LOGTAG, "START GeatteFeedbackActivity:onCreate");
	setTitle(R.string.app_name);

	Bundle extras = getIntent().getExtras();
	final String geatteId = extras != null ? extras.getString(Config.GEATTE_ID_PARAM) : null;
	Log.d(Config.LOGTAG, "GeatteFeedbackActivity:onCreate get extra geatteId = " + geatteId);
	final Long interestId = extras != null ? extras.getLong(GeatteDBAdapter.KEY_INTEREST_ID) : null;
	Log.d(Config.LOGTAG, "GeatteFeedbackActivity:onCreate get extra interestId = " + interestId);

	List<Item> items = new ArrayList<Item>();
	final ThumbnailItem warnItem;
	final ThumbnailBitmapItem geatteItem;

	if (geatteId != null || interestId != null) {
	    ThumbnailBitmapItem item = createItemsFromFetchResult(geatteId, interestId, items);
	    if (item == null) {
		geatteItem = null;
		warnItem = new ThumbnailItem("No geatte available", null, R.drawable.android_pressed);
	    } else {
		geatteItem = item;
		warnItem = null;
	    }
	}
	else {
	    warnItem = new ThumbnailItem("Invalid geatte", null, R.drawable.android_pressed);
	    geatteItem = null;
	}

	//	final ProgressItem progressItem = new ProgressItem("Retrieving feedbacks", true);
	//	items.add(progressItem);

	final ThumbnailBitmapItemAdapter adapter = new ThumbnailBitmapItemAdapter(this, items);
	setListAdapter(adapter);

	mHandler.postDelayed(new Runnable() {
	    public void run() {
		//		adapter.remove(progressItem);
		if (geatteItem != null) {
		    adapter.insert(geatteItem, 0);
		} else {
		    adapter.insert(warnItem, 0);
		}
		adapter.notifyDataSetChanged();
	    }
	},500);
	Log.d(Config.LOGTAG, "END GeatteFeedbackActivity:onCreate");
    }

    private ThumbnailBitmapItem createItemsFromFetchResult(String geatteId, Long interestId, List<Item> items) {
	ThumbnailBitmapItem geatteItem = null;

	GeatteDBAdapter mDbHelper = new GeatteDBAdapter(this);
	mDbHelper.open();

	if (interestId != null) {
	    geatteId = mDbHelper.getGeatteIdFromInterestId(interestId);
	}
	Cursor myInterestCur = null;
	if (geatteId != null) {
	    myInterestCur = mDbHelper.fetchMyInterest(geatteId);
	} else {
	    myInterestCur = mDbHelper.fetchMyInterest(interestId);
	}
	if (myInterestCur.isAfterLast()) {
	    geatteItem = null;
	} else {
	    //	    String savedImagePath = cursor.getString(
	    //		    cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_PATH));
	    //	    mInterestImage.setImageBitmap(BitmapFactory.decodeFile(savedImagePath));
	    String title = myInterestCur.getString(myInterestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_TITLE));
	    String desc = myInterestCur.getString(myInterestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_DESC));
	    String savedImagePath = myInterestCur.getString(myInterestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_PATH));
	    geatteItem = new ThumbnailBitmapItem(title, desc, savedImagePath);

	}

	if (geatteId != null) {
	    Cursor feedbackCur = mDbHelper.fetchMyInterestFeedback(geatteId);
	    feedbackCur.moveToFirst();
	    while (feedbackCur.isAfterLast() == false) {
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
	}
	mDbHelper.close();
	return geatteItem;
    }

    /**
     * A ThumbnailBitmapItemAdapter is an extension of an ItemAdapter for ThumbnailBitmapItem
     * to return associated view.
     */
    private class ThumbnailBitmapItemAdapter extends ItemAdapter {

	private Context mContext;

	public ThumbnailBitmapItemAdapter(Context context, Item[] items) {
	    super(context, items);
	    mContext = context;
	}

	public ThumbnailBitmapItemAdapter(Context context, List<Item> items) {
	    super(context, items);
	    mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

	    final Item item = (Item) getItem(position);
	    if (item instanceof ThumbnailBitmapItem) {
		ItemView cell = item.newView(mContext, null);
		cell.prepareItemView();
		cell.setObject(item);
		return (View) cell;
	    } else {
		return super.getView(position, convertView, parent);
	    }
	}

    }

}
