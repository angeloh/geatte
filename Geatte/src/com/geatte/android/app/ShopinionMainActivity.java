package com.geatte.android.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cyrilmottier.android.greendroid.R;
import com.geatte.android.view.InterestThumbnailItem;
import com.geatte.android.view.InterestThumbnailItemView;
import com.geatte.android.view.ListActionBarActivity;
import greendroid.image.ChainImageProcessor;
import greendroid.image.ImageProcessor;
import greendroid.image.MaskImageProcessor;
import greendroid.image.ScaleImageProcessor;
import greendroid.widget.ActionBar;
import greendroid.widget.ActionBarItem;
import greendroid.widget.AsyncImageView;
import greendroid.widget.ItemAdapter;
import greendroid.widget.NormalActionBarItem;
import greendroid.widget.item.Item;
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
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class ShopinionMainActivity extends ListActionBarActivity {

    private final Handler mHandler = new Handler();
    private InterestThumbnailItemAdapter mImageAdapter = null;

    public ShopinionMainActivity() {
	super(ActionBar.Type.Dashboard);
    }

    @Override
    public int createLayout() {
	return R.layout.shopinin_my_interests;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionMainActivity:onCreate() START");
	}

	ActionBarItem actionBarItem = getActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(
		R.drawable.grid).setContentDescription(R.string.tab_grid);
	addActionBarItem(actionBarItem);

	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionMainActivity:onCreate(): END");
	}
    }

    @Override
    protected void onResume() {
	super.onResume();
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionMainActivity:onResume(): START");
	}
	mHandler.postDelayed(new Runnable() {
	    public void run() {
		fillList();
	    }
	},250);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionMainActivity:onResume(): END");
	}
    }

    private void fillList() {
	try {
	    List<Item> items = getMyGeatteItems();
	    if (items.size() == 0) {
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "ShopinionMainActivity:fillList() : No geatte available!!");
		}
	    }

	    mImageAdapter = new InterestThumbnailItemAdapter(this, items);
	    setListAdapter(mImageAdapter);

	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "ShopinionMainActivity:fillList() :  ERROR ", e);
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
		    Log.d(Config.LOGTAG, "ShopinionMainActivity:getMyGeatteItems() : Process my interest = " + counter);
		}

		int interestId = interestCur.getInt(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_ID));
		String geatteId = interestCur.getString(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_GEATTE_ID));
		String imagePath = interestCur.getString(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_PATH));
		byte[] imageThumbnail = interestCur.getBlob(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_THUMBNAIL));
		String interestTitle = interestCur.getString(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_TITLE));
		String interestDesc = interestCur.getString(interestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_DESC));

		int [] counters = mDbHelper.fetchMyInterestFeedbackCounters(geatteId);

		if (Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "ShopinionMainActivity:getMyGeatteItems() : add one ThumbnailAsyncBitmapItem, " +
			    "interestId = " + interestId + ", imagePath = " + imagePath + ", interestTitle = " +
			    interestTitle + ", interestDesc = " + interestDesc + ", counters = " + Arrays.toString(counters));
		}

		items.add(new InterestThumbnailItem(interestId, interestTitle, interestDesc, imagePath, imageThumbnail, counters));

		interestCur.moveToNext();

	    }
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "ShopinionMainActivity:getMyGeatteItems() :  ERROR ", e);
	} finally {
	    if (interestCur != null) {
		interestCur.close();
	    }
	    mDbHelper.close();
	}
	return items;
    }

    private static class InterestThumbnailItemAdapter extends ItemAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private ImageProcessor mImageProcessor;

	static class ViewHolder {
	    public AsyncImageView imageView;
	    public TextView textViewTitle;
	    public TextView textViewSubTitle;
	    public TextView textCounterYes;
	    public TextView textCounterMaybe;
	    public TextView textCounterNo;
	    public ImageButton btnYes;
	    public ImageButton btnMaybe;
	    public ImageButton btnNo;
	}

	public InterestThumbnailItemAdapter(Context context, Item[] items) {
	    super(context, items);
	    mContext = context;
	    mInflater = LayoutInflater.from(context);
	    //prepareImageProcessor(context);
	}

	public InterestThumbnailItemAdapter(Context context, List<Item> items) {
	    super(context, items);
	    mContext = context;
	    mInflater = LayoutInflater.from(context);
	    //prepareImageProcessor(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

	    final Item item = (Item) getItem(position);
	    if (item instanceof InterestThumbnailItem) {
		ViewHolder holder;

		if (convertView == null) {
		    convertView = mInflater.inflate(R.layout.interest_thumbnail_item_view, parent, false);
		    holder = new ViewHolder();
		    holder.imageView = (AsyncImageView) convertView.findViewById(R.id.interest_thumbnail);
		    //holder.imageView.setImageProcessor(mImageProcessor);
		    holder.textViewTitle = (TextView) convertView.findViewById(R.id.interest_title);
		    holder.textViewSubTitle = (TextView) convertView.findViewById(R.id.interest_subtitle);
		    holder.textCounterYes = (TextView) convertView.findViewById(R.id.ct_yes_text);
		    holder.textCounterMaybe = (TextView) convertView.findViewById(R.id.ct_maybe_text);
		    holder.textCounterNo = (TextView) convertView.findViewById(R.id.ct_no_text);
		    holder.btnYes = (ImageButton) convertView.findViewById(R.id.ct_yes_img);
		    holder.btnMaybe = (ImageButton) convertView.findViewById(R.id.ct_maybe_img);
		    holder.btnNo = (ImageButton) convertView.findViewById(R.id.ct_no_img);

		    convertView.setTag(holder);
		} else {
		    holder = (ViewHolder) convertView.getTag();
		}

		InterestThumbnailItem tItem = (InterestThumbnailItem) item;
		holder.textViewTitle.setText(tItem.text);
		holder.textViewSubTitle.setText(tItem.subtext);
		holder.textCounterYes.setText(Integer.toString(tItem.numOfYes));
		holder.textCounterMaybe.setText(Integer.toString(tItem.numOfMaybe));
		holder.textCounterNo.setText(Integer.toString(tItem.numOfNo));
		holder.btnYes.setTag(new Long(tItem.getId()));
		holder.btnMaybe.setTag(new Long(tItem.getId()));
		holder.btnNo.setTag(new Long(tItem.getId()));
		//setTag(item.id);

		holder.btnYes.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View view) {
			Long interestId = (Long) view.getTag();
			Intent intent = new Intent(view.getContext(), GeatteFeedbackActivity.class);
			intent.putExtra(GeatteDBAdapter.KEY_INTEREST_ID, interestId);
			view.getContext().startActivity(intent);
		    }
		});

		holder.btnMaybe.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View view) {
			Long interestId = (Long) view.getTag();
			Intent intent = new Intent(view.getContext(), GeatteFeedbackActivity.class);
			intent.putExtra(GeatteDBAdapter.KEY_INTEREST_ID, interestId);
			view.getContext().startActivity(intent);
		    }
		});

		holder.btnNo.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View view) {
			Long interestId = (Long) view.getTag();
			Intent intent = new Intent(view.getContext(), GeatteFeedbackActivity.class);
			intent.putExtra(GeatteDBAdapter.KEY_INTEREST_ID, interestId);
			view.getContext().startActivity(intent);
		    }
		});

		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "ShopinionMainActivity:getView() : async image set to bytearray for length= " + tItem.thumbnail.length);
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
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionMainActivity:onListItemClick() BEGIN");
	}
	if (v instanceof InterestThumbnailItemView) {
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, "ShopinionMainActivity:onListItemClick() START");
	    }
	    InterestThumbnailItem item = (InterestThumbnailItem) l.getAdapter().getItem(position);
	    if (item == null) {
		Log.w(Config.LOGTAG, "ShopinionMainActivity:onListItemClick() : item is null");
	    } else {
		Intent intent = new Intent(this, GeatteFeedbackActivity.class);
		intent.putExtra(GeatteDBAdapter.KEY_INTEREST_ID, item.getId());
		startActivity(intent);
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "ShopinionMainActivity:onListItemClick() END");
		}
	    }
	}
    }

    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

	switch (position) {
	case 0:
	    Intent intent = new Intent(this, ShopinionGridActivity.class);
	    startActivity(intent);
	    break;

	default:
	    return super.onHandleActionBarItemClick(item, position);
	}

	return true;
    }

    @Override
    public void onMIBtnClick(View v ) {
	Intent intent = new Intent(this, ShopinionMainActivity.class);
	startActivity(intent);
    }

}