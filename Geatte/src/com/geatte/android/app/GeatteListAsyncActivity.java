package com.geatte.android.app;

import java.util.ArrayList;
import java.util.List;

import com.cyrilmottier.android.greendroid.R;
import com.geatte.android.view.GeatteProgressItem;
import com.geatte.android.view.GeatteThumbnailItem;
import com.geatte.android.view.GeatteThumbnailItemView;
import com.geatte.android.view.ThumbnailAsyncBitmapItem;
import com.geatte.android.view.ThumbnailAsyncBitmapItemView;

import greendroid.app.GDListActivity;
import greendroid.image.ChainImageProcessor;
import greendroid.image.ImageProcessor;
import greendroid.image.MaskImageProcessor;
import greendroid.image.ScaleImageProcessor;
import greendroid.widget.ActionBar;
import greendroid.widget.AsyncImageView;
import greendroid.widget.ItemAdapter;
import greendroid.widget.item.Item;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView.ScaleType;

public class GeatteListAsyncActivity extends GDListActivity implements OnScrollListener {

    private final Handler mHandler = new Handler();
    private ImageProcessor mImageProcessor;
    private AsyncThumbnailItemAdapter mAsyncImageAdapter = null;
    private int mStartFrom = 1;

    public GeatteListAsyncActivity() {
	super(ActionBar.Type.Empty);
    }

    @Override
    public int createLayout() {
	return R.layout.geatte_list_content_empty;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(Config.LOGTAG, "GeatteListAsyncActivity:onCreate() START");

	prepareImageProcessor(this);

	final GeatteThumbnailItem warnItem;
	try {
	    List<Item> items = getMyGeatteItems();
	    if (items.size() == 0) {
		Log.d(Config.LOGTAG, "GeatteListAsyncActivity:onCreate() : No geatte available!!");
		warnItem = new GeatteThumbnailItem("Click Home to create a Geatte", null, R.drawable.icon);
	    } else {
		warnItem = null;
	    }
	    final GeatteProgressItem progressItem = new GeatteProgressItem("Retrieving Geattes", true);
	    items.add(progressItem);

	    mAsyncImageAdapter = new AsyncThumbnailItemAdapter(this, items);
	    setListAdapter(mAsyncImageAdapter);

	    mHandler.postDelayed(new Runnable() {
		public void run() {
		    if (warnItem != null) {
			mAsyncImageAdapter.insert(warnItem, 0);
		    }
		    mAsyncImageAdapter.remove(progressItem);
		    mAsyncImageAdapter.notifyDataSetChanged();
		}
	    },500);
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "GeatteListAsyncActivity:onCreate() :  ERROR ", e);
	}

	getListView().setOnScrollListener(this);

	// get start from, an int, from extras
	mStartFrom = getIntent().getIntExtra(Config.EXTRA_MYGEATTE_STARTFROM, 1);

	Log.d(Config.LOGTAG, "GeatteListAsyncActivity:onCreate(): END");
    }

    @Override
    protected void onResume() {
	super.onResume();
	Log.d(Config.LOGTAG, "GeatteListAsyncActivity:onResume(): START");
	mAsyncImageAdapter.notifyDataSetChanged();
	Log.d(Config.LOGTAG, "GeatteListAsyncActivity:onResume(): END");
    }

    private List<Item> getMyGeatteItems() {
	List<Item> items = new ArrayList<Item>();

	final GeatteDBAdapter mDbHelper = new GeatteDBAdapter(this);
	Cursor interestCur = null;
	try {
	    mDbHelper.open();
	    interestCur = mDbHelper.fetchAllMyInterests();

	    Log.d(Config.LOGTAG, "GeatteListAsyncActivity:getMyGeatteItems() : Got cursor for all my interests");

	    interestCur.moveToFirst();
	    int counter = 0;
	    while (interestCur.isAfterLast() == false) {
		++counter;
		Log.d(Config.LOGTAG, "GeatteListAsyncActivity:getMyGeatteItems() : Process my interest = " + counter);

		String imagePath = interestCur.getString(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_PATH));
		String interestTitle = interestCur.getString(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_TITLE));
		String interestDesc = interestCur.getString(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_DESC));

		Log.d(Config.LOGTAG, "GeatteListAsyncActivity:getMyGeatteItems() : add one GeatteThumbnailCheckbox, imagePath = " + imagePath
			+ ", imagePath = " + imagePath + ", imagePath = " + imagePath);

		if (imagePath != null) {
		    items.add(new ThumbnailAsyncBitmapItem(interestTitle, interestDesc, imagePath, mImageProcessor));
		}

		interestCur.moveToNext();

	    }
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "GeatteListAsyncActivity:getMyGeatteItems() :  ERROR ", e);
	} finally {
	    if (interestCur != null) {
		interestCur.close();
	    }
	    mDbHelper.close();
	}
	return items;
    }

    private class AsyncThumbnailItemAdapter extends ItemAdapter {

	private Context mContext;


	public AsyncThumbnailItemAdapter(Context context, Item[] items) {
	    super(context, items);
	    mContext = context;
	}

	public AsyncThumbnailItemAdapter(Context context, List<Item> items) {
	    super(context, items);
	    mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    final Item item = (Item) getItem(position);
	    if (item instanceof ThumbnailAsyncBitmapItem) {
		ThumbnailAsyncBitmapItemView asyncBitmapView = (ThumbnailAsyncBitmapItemView) item.newView(mContext, null);
		asyncBitmapView.prepareItemView();
		asyncBitmapView.setObject(item);
		return (View) asyncBitmapView;
	    } else {
		return super.getView(position, convertView, parent);
	    }
	}

    }

    private void prepareImageProcessor(Context context) {

	final int thumbnailSize = context.getResources().getDimensionPixelSize(R.dimen.geatte_thumbnail_size);
	final int thumbnailRadius = context.getResources().getDimensionPixelSize(R.dimen.geatte_thumbnail_radius);

	if (Math.random() >= 0.5f) {
	    //@formatter:off
	    mImageProcessor = new ChainImageProcessor(
		    new ScaleImageProcessor(thumbnailSize, thumbnailSize, ScaleType.FIT_XY),
		    new MaskImageProcessor(thumbnailRadius));
	    //@formatter:on
	} else {

	    Path path = new Path();
	    path.moveTo(thumbnailRadius, 0);

	    path.lineTo(thumbnailSize - thumbnailRadius, 0);
	    path.lineTo(thumbnailSize, thumbnailRadius);
	    path.lineTo(thumbnailSize, thumbnailSize - thumbnailRadius);
	    path.lineTo(thumbnailSize - thumbnailRadius, thumbnailSize);
	    path.lineTo(thumbnailRadius, thumbnailSize);
	    path.lineTo(0, thumbnailSize - thumbnailRadius);
	    path.lineTo(0, thumbnailRadius);

	    path.close();

	    Bitmap mask = Bitmap.createBitmap(thumbnailSize, thumbnailSize, android.graphics.Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(mask);

	    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    paint.setStyle(Style.FILL_AND_STROKE);
	    paint.setColor(Color.RED);

	    canvas.drawPath(path, paint);

	    //@formatter:off
	    mImageProcessor = new ChainImageProcessor(
		    new ScaleImageProcessor(thumbnailSize, thumbnailSize, ScaleType.FIT_XY),
		    new MaskImageProcessor(mask));
	    //@formatter:on
	}
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	super.onListItemClick(l, v, position, id);
	if (!(v instanceof GeatteThumbnailItemView)) {
	    Intent intent = new Intent(this, GeatteFeedbackActivity.class);
	    intent.putExtra(GeatteDBAdapter.KEY_INTEREST_ID, id);
	    intent.putExtra(Config.EXTRA_MYGEATTE_STARTFROM, mStartFrom);
	    startActivity(intent);
	}
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    public void onScrollStateChanged(AbsListView listView, int scrollState) {
	if (getListView() == listView) {
	    searchAsyncImageViews(listView, scrollState == OnScrollListener.SCROLL_STATE_FLING);
	}
    }

    private void searchAsyncImageViews(ViewGroup viewGroup, boolean pause) {
	final int childCount = viewGroup.getChildCount();
	for (int i = 0; i < childCount; i++) {
	    AsyncImageView image = (AsyncImageView) viewGroup.getChildAt(i).findViewById(R.id.geatte_async_image);
	    if (image != null) {
		image.setPaused(pause);
	    }
	}
    }

}