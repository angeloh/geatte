package com.geatte.android.app;

import greendroid.app.GDListActivity;
import greendroid.widget.ItemAdapter;
import greendroid.widget.item.Item;
import greendroid.widget.item.ProgressItem;
import greendroid.widget.item.ThumbnailItem;
import greendroid.widget.itemview.ItemView;

import java.util.ArrayList;
import java.util.List;

import com.cyrilmottier.android.greendroid.R;
import com.geatte.android.view.GeatteThumbnailItem;
import com.geatte.android.view.SeparatorThumbnailItem;
import com.geatte.android.view.ThumbnailBitmapItem;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class GeatteAllFeedbackActivity extends GDListActivity {

    private final Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(Config.LOGTAG, "START GeatteAllFeedbackActivity:onCreate");
	setTitle(R.string.app_name);

	List<Item> items = new ArrayList<Item>();
	final ThumbnailItem warnItem;

	final GeatteDBAdapter mDbHelper = new GeatteDBAdapter(this);
	Cursor feedbackCur = null;
	try {
	    mDbHelper.open();
	    feedbackCur = mDbHelper.fetchAllMyInterestFeedback();

	    Log.d(Config.LOGTAG, "Got cursor for all feedbacks");

	    feedbackCur.moveToFirst();
	    if (feedbackCur.isAfterLast()) {
		Log.d(Config.LOGTAG, "No feedback available!!");
		warnItem = new ThumbnailItem("No feedback available", null, R.drawable.empty);
	    } else {
		warnItem = null;
	    }

	    int counter = 0;
	    while (feedbackCur.isAfterLast() == false) {
		++counter;
		Log.d(Config.LOGTAG, "Process feedback = " + counter);

		String geatteId = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_GEATTE_ID));
		String vote = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_VOTE));
		String voter = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_VOTER));
		String comment = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_COMMENT));
		// get voter contact thumbnail

		String interestTitle = null;
		Cursor myInterestCur = mDbHelper.fetchMyInterest(geatteId);
		try {
		    interestTitle = myInterestCur.getString(myInterestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_TITLE));
		} catch (Exception ex) {
		    Log.e(Config.LOGTAG, "GeatteAllFeedbackActivity:onCreate error to fetch interest geatteId = " + geatteId, ex);
		} finally{
		    myInterestCur.close();
		}

		StringBuilder sb = new StringBuilder("said ").append(vote).append(" for ").append(interestTitle);

		items.add(new GeatteThumbnailItem(sb.toString(), comment, R.drawable.profile));

		feedbackCur.moveToNext();

	    }

	    final ProgressItem progressItem = new ProgressItem("Retrieving feedbacks", true);
	    items.add(progressItem);

	    final ThumbnailBitmapItemAdapter adapter = new ThumbnailBitmapItemAdapter(this, items);
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
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "GeatteAllFeedbackActivity:onCreate ERROR ", e);
	} finally {
	    if (feedbackCur != null) {
		feedbackCur.close();
	    }
	    mDbHelper.close();
	}
	Log.d(Config.LOGTAG, "END GeatteAllFeedbackActivity:onCreate");
    }

    /**
     * A ThumbnailBitmapItemAdapter is an extension of an ItemAdapter for
     * ThumbnailBitmapItem, SeparatorThumbnailItem, GeatteThumbnailItem
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
	    } else if (item instanceof SeparatorThumbnailItem) {
		ItemView cell = item.newView(mContext, null);
		cell.prepareItemView();
		cell.setObject(item);
		return (View) cell;
	    } else if (item instanceof GeatteThumbnailItem) {
		ItemView cell = item.newView(mContext, null);
		cell.prepareItemView();
		cell.setObject(item);
		return (View) cell;
	    } else {
		return super.getView(position, convertView, parent);
	    }
	}

    }

    @Override
    public void onDestroy() {
	super.onDestroy();
    }

    @Override
    public int createLayout() {
	Log.d(Config.LOGTAG, "creating the geatte feedback layout");
	return R.layout.geatte_feedback_list_content;
    }

}
