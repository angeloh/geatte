package com.geatte.android.app;

import java.util.ArrayList;
import java.util.List;

import com.cyrilmottier.android.greendroid.R;
import com.geatte.android.view.GeatteThumbnailItem;
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
import greendroid.widget.itemview.ItemView;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class GeatteListAsyncXActivity extends GDListActivity {

    private final Handler mHandler = new Handler();
    private AsyncThumbnailItemAdapter mAsyncImageAdapter = null;
    private int mStartFrom = 1;

    public GeatteListAsyncXActivity() {
	super(ActionBar.Type.Empty);
    }

    @Override
    public int createLayout() {
	return R.layout.geatte_list_content_empty;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "GeatteListAsyncXActivity:onCreate() START");
	}

	mStartFrom = getIntent().getIntExtra(Config.EXTRA_MYGEATTE_STARTFROM, 1);

	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "GeatteListAsyncXActivity:onCreate(): END");
	}
    }

    @Override
    protected void onResume() {
	super.onResume();
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "GeatteListAsyncXActivity:onResume(): START");
	}
	mHandler.postDelayed(new Runnable() {
	    public void run() {
		fillList();
	    }
	},250);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "GeatteListAsyncXActivity:onResume(): END");
	}
    }

    private void fillList() {
	//prepareImageProcessor(this);

	final GeatteThumbnailItem warnItem;
	try {
	    List<Item> items = getMyGeatteItems();
	    if (items.size() == 0) {
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteListAsyncXActivity:fillList() : No geatte available!!");
		}
		warnItem = new GeatteThumbnailItem("No interest created yet!", null, R.drawable.icon);
	    } else {
		warnItem = null;
	    }

	    mAsyncImageAdapter = new AsyncThumbnailItemAdapter(this, items);
	    setListAdapter(mAsyncImageAdapter);

	    mHandler.postDelayed(new Runnable() {
		public void run() {
		    if (warnItem != null) {
			mAsyncImageAdapter.insert(warnItem, 0);
			mAsyncImageAdapter.notifyDataSetChanged();
		    }
		}
	    },500);
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "GeatteListAsyncXActivity:fillList() :  ERROR ", e);
	}
    }

    private List<Item> getMyGeatteItems() {
	List<Item> items = new ArrayList<Item>();

	final GeatteDBAdapter mDbHelper = new GeatteDBAdapter(this);
	Cursor interestCur = null;
	try {
	    mDbHelper.open();
	    interestCur = mDbHelper.fetchAllMyInterestsWithBlob();

	    interestCur.moveToFirst();
	    int counter = 0;
	    while (interestCur.isAfterLast() == false) {
		++counter;
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteListAsyncXActivity:getMyGeatteItems() : Process my interest = " + counter);
		}

		int interestId = interestCur.getInt(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_ID));
		String imagePath = interestCur.getString(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_PATH));
		byte[] imageThumbnail = interestCur.getBlob(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_THUMBNAIL));
		String interestTitle = interestCur.getString(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_TITLE));
		String interestDesc = interestCur.getString(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_DESC));

		Log.i(Config.LOGTAG, "GeatteListAsyncXActivity:getMyGeatteItems() : add one ThumbnailAsyncBitmapItem, " +
			"interestId = " + interestId + ", imagePath = " + imagePath + ", interestTitle = " +
			interestTitle + ", interestDesc = " + interestDesc);

		//items.add(new ThumbnailAsyncBitmapItem(interestId, interestTitle, interestDesc, imagePath));
		items.add(new ThumbnailAsyncBitmapItem(interestId, interestTitle, interestDesc, imagePath, imageThumbnail));

		interestCur.moveToNext();

	    }
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "GeatteListAsyncXActivity:getMyGeatteItems() :  ERROR ", e);
	} finally {
	    if (interestCur != null) {
		interestCur.close();
	    }
	    mDbHelper.close();
	}
	return items;
    }

    private static class AsyncThumbnailItemAdapter extends ItemAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private ImageProcessor mImageProcessor;

	static class ViewHolder {
	    public AsyncImageView imageView;
	    public TextView textViewTitle;
	    public TextView textViewSubTitle;
	}

	public AsyncThumbnailItemAdapter(Context context, Item[] items) {
	    super(context, items);
	    mContext = context;
	    mInflater = LayoutInflater.from(context);
	    prepareImageProcessor(context);
	}

	public AsyncThumbnailItemAdapter(Context context, List<Item> items) {
	    super(context, items);
	    mContext = context;
	    mInflater = LayoutInflater.from(context);
	    prepareImageProcessor(context);
	}

	//	@Override
	//	public View getView(int position, View convertView, ViewGroup parent) {
	//	    final Item item = (Item) getItem(position);
	//	    if (item instanceof ThumbnailAsyncBitmapItem) {
	//		ThumbnailAsyncBitmapItemView asyncBitmapView = (ThumbnailAsyncBitmapItemView) item.newView(mContext, null);
	//		asyncBitmapView.prepareItemView();
	//		asyncBitmapView.setObject(item);
	//		return (View) asyncBitmapView;
	//	    } else {
	//		return super.getView(position, convertView, parent);
	//	    }
	//	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

	    final Item item = (Item) getItem(position);
	    if (item instanceof ThumbnailAsyncBitmapItem) {
		ViewHolder holder;

		if (convertView == null) {
		    convertView = mInflater.inflate(R.layout.geatte_thumbnail_async_bitmap_item_view, parent, false);
		    holder = new ViewHolder();
		    holder.imageView = (AsyncImageView) convertView.findViewById(R.id.geatte_async_image);
		    holder.imageView.setImageProcessor(mImageProcessor);
		    holder.textViewTitle = (TextView) convertView.findViewById(R.id.geatte_async_item_title);
		    holder.textViewSubTitle = (TextView) convertView.findViewById(R.id.geatte_async_subtitle);

		    convertView.setTag(holder);
		} else {
		    holder = (ViewHolder) convertView.getTag();
		}

		ThumbnailAsyncBitmapItem tItem = (ThumbnailAsyncBitmapItem) item;
		holder.textViewTitle.setText(tItem.text);
		holder.textViewSubTitle.setText(tItem.subtitle);
		//setTag(item.id);

		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteListAsyncXActivity:getView() : async image set to bytearray for length= " + tItem.thumbnail.length);
		}
		if (tItem.thumbnail == null || tItem.thumbnail.length <= 0) {
		    holder.imageView.setImageResource(R.drawable.thumb_missing);
		} else {
		    holder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(tItem.thumbnail, 0, tItem.thumbnail.length));
		}
		//String uri = Uri.fromFile(new File(tItem.imagePath)).toString();
		//Log.d(Config.LOGTAG, "GeatteListAsyncActivity:getView() : async image request to = " + uri);
		//holder.imageView.setUrl(Uri.fromFile(new File(tItem.imagePath)).toString());

		//		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		//		bitmapOptions.inSampleSize = 64;
		//		Bitmap imgBitmap = BitmapFactory.decodeFile(tItem.imagePath, bitmapOptions);
		//		holder.imageView.setImageBitmap(imgBitmap);
		return convertView;
	    }
	    else if (item instanceof GeatteThumbnailItem) {
		ItemView cell = item.newView(mContext, null);
		cell.prepareItemView();
		cell.setObject(item);
		return (View) cell;
	    }
	    else {
		return super.getView(position, convertView, parent);
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
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	super.onListItemClick(l, v, position, id);
	if (v instanceof ThumbnailAsyncBitmapItemView) {
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, "GeatteListAsyncXActivity:onListItemClick() START");
	    }
	    //int interestId = (Integer)((GeatteThumbnailItemView)v).getTag();
	    ThumbnailAsyncBitmapItem item = (ThumbnailAsyncBitmapItem) l.getAdapter().getItem(position);
	    //ThumbnailAsyncBitmapItem item = (ThumbnailAsyncBitmapItem) getListView().getItemAtPosition(position);
	    if (item == null) {
		Log.w(Config.LOGTAG, "GeatteListAsyncXActivity:onListItemClick() : item is null");
	    } else {
		Intent intent = new Intent(this, GeatteFeedbackActivity.class);
		intent.putExtra(GeatteDBAdapter.KEY_INTEREST_ID, item.getId());
		intent.putExtra(Config.EXTRA_MYGEATTE_STARTFROM, mStartFrom);
		startActivity(intent);
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteListAsyncXActivity:onListItemClick() END");
		}
	    }
	}
    }

}