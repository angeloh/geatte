package com.geatte.android.app;

import java.util.ArrayList;
import java.util.List;

import com.cyrilmottier.android.greendroid.R;
import com.geatte.android.view.InterestFriendThumbnailItem;
import com.geatte.android.view.InterestFriendThumbnailItemView;
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
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.item.Item;
import android.app.ProgressDialog;
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

public class ShopinionFIListActivity extends ListActionBarActivity {

    private final Handler mHandler = new Handler();
    private InterestThumbnailItemAdapter mImageAdapter = null;
    private ProgressDialog mDialog;

    public ShopinionFIListActivity() {
	super(ActionBar.Type.Dashboard);
    }

    @Override
    public int createLayout() {
	return R.layout.shopinin_friend_interests;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionFIListActivity:onCreate() START");
	}

	ActionBarItem actionBarItem = getActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(
		R.drawable.grid).setContentDescription(R.string.tab_grid);
	addActionBarItem(actionBarItem);
	addActionBarItem(Type.AllFriends);

	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionFIListActivity:onCreate(): END");
	}
    }

    @Override
    protected void onResume() {
	super.onResume();
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionFIListActivity:onResume(): START");
	}
	mDialog = ProgressDialog.show(ShopinionFIListActivity.this, "Loading", "Please wait...", true);
	mHandler.postDelayed(new Runnable() {
	    public void run() {
		fillList();
	    }
	},50);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionFIListActivity:onResume(): END");
	}
    }

    @Override
    public void onPause() {
	super.onPause();
	if (getListAdapter() != null) {
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, "ShopinionFIListActivity:onPause(): execute gc");
	    }
	    for (int i = 0; i < getListAdapter().getCount(); i++) {
		if (getListAdapter().getItem(i) instanceof InterestFriendThumbnailItem) {
		    InterestFriendThumbnailItem item = (InterestFriendThumbnailItem) getListAdapter().getItem(i);
		    item.thumbnail = null;
		}
	    }
	}
    }

    private void fillList() {
	try {
	    List<Item> items = getFIGeatteItems();
	    if (items.size() == 0) {
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "ShopinionFIListActivity:fillList() : No geatte available!!");
		}
	    }

	    mImageAdapter = new InterestThumbnailItemAdapter(this, items);
	    setListAdapter(mImageAdapter);

	    mHandler.postDelayed(new Runnable() {
		public void run() {
		    if (mDialog != null && mDialog.isShowing()) {
			try {
			    if(Config.LOG_DEBUG_ENABLED) {
				Log.d(Config.LOGTAG, "ShopinionFIListActivity:fillList(): try to dismiss mDialog");
			    }
			    mDialog.dismiss();
			    mDialog = null;
			} catch (Exception e) {
			    Log.w(Config.LOGTAG, "ShopinionFIListActivity:fillList(): failed to dismiss mDialog", e);
			}
		    }
		}
	    },10);

	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "ShopinionFIListActivity:fillList() :  ERROR ", e);
	}
    }

    private List<Item> getFIGeatteItems() {
	List<Item> items = new ArrayList<Item>();

	final GeatteDBAdapter mDbHelper = new GeatteDBAdapter(this);
	Cursor fiCur = null;
	try {
	    mDbHelper.open();
	    fiCur = mDbHelper.fetchAllFriendInterestsWithBlob();

	    fiCur.moveToFirst();
	    int counter = 0;
	    while (fiCur.isAfterLast() == false) {
		++counter;
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "ShopinionFIListActivity:getFIGeatteItems() : Process friend interest = " + counter);
		}

		long fInterestId = fiCur.getLong(fiCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FRIEND_INTEREST_ID));
		String fImagePath = fiCur.getString(fiCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FI_IMAGE_PATH));
		byte[] fImageThumbnail = fiCur.getBlob(fiCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FI_IMAGE_THUMBNAIL));
		String fInterestTitle = fiCur.getString(fiCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FRIEND_INTEREST_TITLE));
		String fInterestDesc = fiCur.getString(fiCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FRIEND_INTEREST_DESC));
		String fInterestSentOn = fiCur.getString(fiCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FRIEND_INTEREST_CREATED_DATE));
		String fInterestSentFrom = fiCur.getString(fiCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FRIEND_INTEREST_FROM));

		List<String> comments = mDbHelper.fetchFIFeedback(Long.toString(fInterestId));

		if (Config.LOG_DEBUG_ENABLED) {
		    Log.i(Config.LOGTAG, "ShopinionFIListActivity:getFIGeatteItems() : add one ThumbnailAsyncBitmapItem, " +
			    "fInterestId = " + fInterestId + ", fImagePath = " + fImagePath + ", fInterestTitle = " +
			    fInterestTitle + ", fInterestDesc = " + fInterestDesc + ", comments = " + comments.toString());
		}

		String sendByText = "@" + mDbHelper.fetchContactFirstName(fInterestSentFrom);
		String sendOnText = "by " + fInterestSentOn;
		String voteText = "NA";
		String voteFeedbackText = "";
		if (comments.get(0) != null) {
		    voteText = comments.get(0);
		}
		//TODO could output all comments
		if (comments.size() > 1 && comments.get(1) != null) {
		    voteFeedbackText = comments.get(1);
		}

		items.add(new InterestFriendThumbnailItem(fInterestId, fInterestTitle, fInterestDesc,
			fImagePath, fImageThumbnail, sendByText, sendOnText, voteText, voteFeedbackText));

		fiCur.moveToNext();

	    }
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "ShopinionFIListActivity:getFIGeatteItems() :  ERROR ", e);
	} finally {
	    if (fiCur != null) {
		fiCur.close();
	    }
	    mDbHelper.close();
	}
	return items;
    }

    static private class InterestThumbnailItemAdapter extends ItemAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private ImageProcessor mImageProcessor;

	class ViewHolder {
	    public AsyncImageView imageView;
	    public TextView textViewTitle;
	    public TextView textViewSubTitle;
	    public TextView SentBy;
	    public TextView SentOn;
	    public TextView VoteText;
	    public TextView VoteFeedback;
	    public ImageButton btnVoteImage;
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
	    if (item instanceof InterestFriendThumbnailItem) {
		ViewHolder holder;

		if (convertView == null) {
		    convertView = mInflater.inflate(R.layout.interest_f_thumbnail_item_view, parent, false);
		    holder = new ViewHolder();
		    holder.imageView = (AsyncImageView) convertView.findViewById(R.id.f_interest_thumbnail);
		    //holder.imageView.setImageProcessor(mImageProcessor);
		    holder.textViewTitle = (TextView) convertView.findViewById(R.id.f_interest_title);
		    holder.textViewSubTitle = (TextView) convertView.findViewById(R.id.f_interest_subtitle);
		    holder.SentBy = (TextView) convertView.findViewById(R.id.fi_sent_by_text);
		    holder.SentOn = (TextView) convertView.findViewById(R.id.fi_sent_on_text);
		    holder.VoteText = (TextView) convertView.findViewById(R.id.fi_vote_text);
		    holder.VoteFeedback = (TextView) convertView.findViewById(R.id.fi_vote_feedback);
		    holder.btnVoteImage = (ImageButton) convertView.findViewById(R.id.fi_vote_img_btn);
		    convertView.setTag(holder);
		} else {
		    holder = (ViewHolder) convertView.getTag();
		}

		InterestFriendThumbnailItem tItem = (InterestFriendThumbnailItem) item;
		holder.textViewTitle.setText(tItem.text);
		holder.textViewSubTitle.setText(tItem.subtext);
		holder.SentBy.setText(tItem.sendByText);
		holder.SentOn.setText(tItem.sendOnText);
		holder.VoteText.setText(tItem.voteText);
		holder.VoteFeedback.setText(tItem.voteFeedbackText);
		holder.btnVoteImage.setTag(new Long(tItem.getId()));
		//setTag(item.id);

		holder.btnVoteImage.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View view) {
			Long geatteId = (Long) view.getTag();
			Intent intent = new Intent(view.getContext(), ShopinionVotingActivity.class);
			intent.putExtra(Config.GEATTE_ID_PARAM, Long.toString(geatteId));
			intent.putExtra(Config.ACTION_VOTING_BAR_HOME, Config.BACK_STYLE.LIST.toString());
			view.getContext().startActivity(intent);
		    }
		});

		if (tItem.voteText.equals(Config.LIKE.YES.toString())) {
		    holder.btnVoteImage.setImageResource(R.drawable.ct_yes);
		}
		else if (tItem.voteText.equals(Config.LIKE.MAYBE.toString())) {
		    holder.btnVoteImage.setImageResource(R.drawable.ct_maybe);
		}
		else if (tItem.voteText.equals(Config.LIKE.NO.toString())) {
		    holder.btnVoteImage.setImageResource(R.drawable.ct_no);
		}
		else {
		    holder.btnVoteImage.setImageResource(R.drawable.ct_maybe);
		}

		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "ShopinionFIListActivity:getView() : async image set to bytearray for length= " + tItem.thumbnail.length);
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
	    Log.d(Config.LOGTAG, "ShopinionFIListActivity:onListItemClick() BEGIN");
	}
	if (v instanceof InterestFriendThumbnailItemView) {
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, "ShopinionFIListActivity:onListItemClick() START");
	    }
	    InterestFriendThumbnailItem item = (InterestFriendThumbnailItem) l.getAdapter().getItem(position);
	    if (item == null) {
		Log.w(Config.LOGTAG, "ShopinionFIListActivity:onListItemClick() : item is null");
	    } else {
		Intent intent = new Intent(this, ShopinionVotingActivity.class);
		intent.putExtra(Config.GEATTE_ID_PARAM, Long.toString(item.getId()));
		intent.putExtra(Config.ACTION_VOTING_BAR_HOME, Config.BACK_STYLE.LIST.toString());
		startActivity(intent);
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "ShopinionFIListActivity:onListItemClick() END");
		}
	    }
	}
    }

    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

	switch (position) {
	case 0:
	    Intent intent = new Intent(this, ShopinionFIGridActivity.class);
	    startActivity(intent);
	    break;
	case 1:
	    onShowAllContacts(item.getItemView());
	    break;

	default:
	    return super.onHandleActionBarItemClick(item, position);
	}

	return true;
    }

    public void onShowAllContacts(View v) {
	Intent intent = new Intent(getApplicationContext(), ShopinionContactInfoActivity.class);
	startActivity(intent);
    }

}