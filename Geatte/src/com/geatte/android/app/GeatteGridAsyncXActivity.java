package com.geatte.android.app;

import java.util.ArrayList;
import java.util.List;

import com.cyrilmottier.android.greendroid.R;
import com.geatte.android.view.GridActionBarActivity;
import com.geatte.android.view.GridBitmapItem;
import greendroid.widget.ActionBar;
import greendroid.widget.ItemAdapter;
import greendroid.widget.item.Item;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

public class GeatteGridAsyncXActivity extends GridActionBarActivity {

    private final Handler mHandler = new Handler();
    private ImageAdapter mImageAdapter = null;

    public GeatteGridAsyncXActivity() {
	super(ActionBar.Type.Empty);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "GeatteGridAsyncXActivity:onCreate() START");
	}

	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "GeatteGridAsyncXActivity:onCreate(): END");
	}
    }

    @Override
    protected void onResume() {
	super.onResume();
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "GeatteGridAsyncXActivity:onResume(): START");
	}
	mHandler.postDelayed(new Runnable() {
	    public void run() {
		fillList();
	    }
	},250);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "GeatteGridAsyncXActivity:onResume(): END");
	}
    }

    private void fillList() {
	try {
	    List<Item> items = getMyGeatteItems();
	    if (items.size() == 0) {
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteListAsyncXActivity:fillList() : No geatte available!!");
		}
	    }

	    mImageAdapter = new ImageAdapter(this, items);
	    setListAdapter(mImageAdapter);

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
		    Log.d(Config.LOGTAG, "GeatteGridAsyncXActivity:getMyGeatteItems() : Process my interest = " + counter);
		}

		int interestId = interestCur.getInt(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_AS_ID));
		String imagePath = interestCur.getString(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_PATH));
		byte[] imageThumbnail = interestCur.getBlob(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_THUMBNAIL));

		Log.i(Config.LOGTAG, "GeatteGridAsyncXActivity:getMyGeatteItems() : add one ThumbnailAsyncBitmapItem, " +
			"interestId = " + interestId + ", imageThumbnail.length = " + imageThumbnail.length);

		items.add(new GridBitmapItem(interestId, imagePath, imageThumbnail));

		interestCur.moveToNext();

	    }
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "GeatteGridAsyncXActivity:getMyGeatteItems() :  ERROR ", e);
	} finally {
	    if (interestCur != null) {
		interestCur.close();
	    }
	    mDbHelper.close();
	}
	return items;
    }

    private static class ImageAdapter extends ItemAdapter {
	private Context mContext;
	private LayoutInflater mInflater;

	static class ViewHolder {
	    public ImageView imageView;
	}

	public ImageAdapter(Context context, Item[] items) {
	    super(context, items);
	    mContext = context;
	    mInflater = LayoutInflater.from(context);
	}

	public ImageAdapter(Context context, List<Item> items) {
	    super(context, items);
	    mContext = context;
	    mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

	    final Item item = (Item) getItem(position);
	    if (item instanceof GridBitmapItem) {
		ViewHolder holder;

		if (convertView == null) {
		    convertView = mInflater.inflate(R.layout.grid_bitmap_item_view, parent, false);
		    holder = new ViewHolder();
		    holder.imageView = (ImageView) convertView.findViewById(R.id.grid_image);

		    convertView.setTag(holder);
		} else {
		    holder = (ViewHolder) convertView.getTag();
		}

		GridBitmapItem tItem = (GridBitmapItem) item;

		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteGridAsyncXActivity:ImageAdapter:getView() : image set to bytearray for length = " + tItem.thumbnail.length);
		}

		if (tItem.thumbnail == null || tItem.thumbnail.length <= 0) {
		    holder.imageView.setImageResource(R.drawable.thumb_missing);
		} else {
		    holder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(tItem.thumbnail, 0, tItem.thumbnail.length));
		}

		return convertView;
	    }
	    else {
		return super.getView(position, convertView, parent);
	    }

	}

    }

    @Override
    protected void onGridItemClick(GridView g, View v, int position, long id) {
	super.onGridItemClick(g, v, position, id);
	if (v instanceof ImageView) {
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, "GeatteGridAsyncXActivity:onGridItemClick() START");
	    }
	    GridBitmapItem item = (GridBitmapItem) g.getAdapter().getItem(position);
	    if (item == null) {
		Log.w(Config.LOGTAG, "GeatteGridAsyncXActivity:onGridItemClick() : item is null");
	    } else {
		Intent intent = new Intent(this, GeatteFeedbackActivity.class);
		intent.putExtra(GeatteDBAdapter.KEY_INTEREST_ID, item.getId());
		startActivity(intent);
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteGridAsyncXActivity:onGridItemClick() END");
		}
	    }
	}
    }

}