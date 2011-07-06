package com.geatte.android.app;

import greendroid.app.GDListActivity;
import greendroid.image.ChainImageProcessor;
import greendroid.image.ImageProcessor;
import greendroid.image.MaskImageProcessor;
import greendroid.image.ScaleImageProcessor;
import greendroid.widget.AsyncImageView;
import greendroid.widget.ItemAdapter;
import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;

import java.util.ArrayList;
import java.util.List;

import com.cyrilmottier.android.greendroid.R;
import com.geatte.android.view.GeatteFeedbackItem;
import com.geatte.android.view.GeatteThumbnailItem;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class GeatteAllFeedbackXActivity extends GDListActivity {

    private final Handler mHandler = new Handler();
    private ProgressDialog mDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "START GeatteAllFeedbackXActivity:onCreate");
	}
	setTitle(R.string.show_all_feedbacks_title);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "END GeatteAllFeedbackXActivity:onCreate");
	}
    }

    @Override
    protected void onResume() {
	super.onResume();
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "GeatteAllFeedbackXActivity:onResume(): START");
	}
	mDialog = ProgressDialog.show(GeatteAllFeedbackXActivity.this, "Loading", "Please wait...", true);
	mHandler.postDelayed(new Runnable() {
	    public void run() {
		fillList();
	    }
	},250);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "GeatteAllFeedbackXActivity:onResume(): END");
	}
    }


    @Override
    public void onDestroy() {
	super.onDestroy();
    }

    private void fillList() {
	final GeatteThumbnailItem warnItem;
	try {
	    List<Item> items = getAllFeedbackItems();
	    if (items.size() == 0) {
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteAllFeedbackXActivity:fillList() No feedback available!!");
		}
		warnItem = new GeatteThumbnailItem("No feedback available", null, R.drawable.empty);
	    } else {
		warnItem = null;
	    }

	    final GeatteFeedbackItemAdapter adapter = new GeatteFeedbackItemAdapter(this, items);
	    setListAdapter(adapter);

	    mHandler.postDelayed(new Runnable() {
		public void run() {
		    if (warnItem != null) {
			adapter.insert(warnItem, 0);
			adapter.notifyDataSetChanged();
		    }
		    if (mDialog != null && mDialog.isShowing()) {
			try {
			    if(Config.LOG_DEBUG_ENABLED) {
				Log.d(Config.LOGTAG, "GeatteAllFeedbackXActivity:fillList(): try to dismiss mDialog");
			    }
			    mDialog.dismiss();
			    mDialog = null;
			} catch (Exception e) {
			    Log.w(Config.LOGTAG, "GeatteAllFeedbackXActivity:fillList(): failed to dismiss mDialog", e);
			}
		    }
		}
	    },500);
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "GeatteAllFeedbackXActivity:fillList() :  ERROR ", e);
	}

    }

    private List<Item> getAllFeedbackItems() {
	List<Item> items = new ArrayList<Item>();

	final GeatteDBAdapter mDbHelper = new GeatteDBAdapter(this);
	Cursor feedbackCur = null;
	try {
	    mDbHelper.open();
	    feedbackCur = mDbHelper.fetchAllMyInterestFeedback();
	    feedbackCur.moveToFirst();

	    int counter = 0;
	    while (feedbackCur.isAfterLast() == false) {
		++counter;
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "Process feedback = " + counter);
		}

		String geatteId = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_GEATTE_ID));
		String vote = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_VOTE));
		String voter = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_VOTER));
		String comment = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_COMMENT));
		// get contact name for this voter
		String voterName = mDbHelper.fetchContactFirstName(voter);
		// get voter contact thumbnail
		Integer contactId = mDbHelper.fetchContactId(voter);
		Bitmap contactBitmap = queryPhotoForContact(contactId);

		//String interestTitle = null;
		String interestImagePath = null;
		byte[] interestImageThumbnail = null;
		Cursor myInterestCur = mDbHelper.fetchMyInterestWithBlob(geatteId);
		try {
		    //interestTitle = myInterestCur.getString(myInterestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_INTEREST_TITLE));
		    interestImagePath = myInterestCur.getString(myInterestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_PATH));
		    interestImageThumbnail = myInterestCur.getBlob(myInterestCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_THUMBNAIL));
		} catch (Exception ex) {
		    Log.e(Config.LOGTAG, "GeatteAllFeedbackXActivity:getAllFeedbackItems() error to fetch interest geatteId = " + geatteId, ex);
		} finally{
		    myInterestCur.close();
		}

		StringBuilder sb = new StringBuilder(voterName).append(" said ").append(vote);

		if (contactBitmap != null) {
		    items.add(new GeatteFeedbackItem(sb.toString(), comment, contactBitmap, interestImagePath, interestImageThumbnail));
		} else {
		    items.add(new GeatteFeedbackItem(sb.toString(), comment, R.drawable.profile, interestImagePath, interestImageThumbnail));
		}

		feedbackCur.moveToNext();

	    }
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "GeatteAllFeedbackXActivity:getAllFeedbackItems() ERROR ", e);
	} finally {
	    if (feedbackCur != null) {
		feedbackCur.close();
	    }
	    mDbHelper.close();
	}
	return items;
    }

    private Bitmap queryPhotoForContact(int contactId) {
	final String[] projection = new String[] {
		//Contacts.DISPLAY_NAME,// the name of the contact
		Contacts.PHOTO_ID// the id of the column in the data table for the image
	};

	final Cursor contact = managedQuery(
		Contacts.CONTENT_URI,
		projection,
		Contacts._ID + "=?",// filter entries on the basis of the contact id
		new String[]{String.valueOf(contactId)},// the parameter to which the contact id column is compared to
		null);

	if(contact.moveToFirst()) {
	    //	    final String name = contact.getString(
	    //		    contact.getColumnIndex(Contacts.DISPLAY_NAME));
	    final String photoId = contact.getString(
		    contact.getColumnIndex(Contacts.PHOTO_ID));
	    final Bitmap photo;
	    if(photoId != null) {
		photo = queryContactBitmap(photoId);
	    } else {
		photo = null;
	    }
	    contact.close();
	    return photo;
	}
	contact.close();
	return null;
    }

    private Bitmap queryContactBitmap(String photoId) {
	Cursor photo = getContentResolver().query(Data.CONTENT_URI, new String[] { Photo.PHOTO },
		Data._ID + "=?", new String[] { photoId }, null);

	final Bitmap photoBitmap;
	if (photo.moveToFirst()) {
	    byte[] photoBlob = photo.getBlob(photo.getColumnIndex(Photo.PHOTO));
	    photoBitmap = BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.length);
	} else {
	    photoBitmap = null;
	}
	photo.close();
	return photoBitmap;
    }

    /**
     * A GeatteFeedbackItemAdapter is an extension of an ItemAdapter for
     * ThumbnailBitmapItem, SeparatorThumbnailItem, GeatteThumbnailItem
     * to return associated view.
     */
    private static class GeatteFeedbackItemAdapter extends ItemAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private ImageProcessor mImageProcessor;

	static class ViewHolder {
	    public ImageView contactImageView;
	    public AsyncImageView interestImageView;
	    public TextView textViewTitle;
	    public TextView textViewSubTitle;
	}

	public GeatteFeedbackItemAdapter(Context context, Item[] items) {
	    super(context, items);
	    mContext = context;
	    mInflater = LayoutInflater.from(context);
	    prepareImageProcessor(context);
	}

	public GeatteFeedbackItemAdapter(Context context, List<Item> items) {
	    super(context, items);
	    mContext = context;
	    mInflater = LayoutInflater.from(context);
	    prepareImageProcessor(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

	    final Item item = (Item) getItem(position);
	    if (item instanceof GeatteFeedbackItem) {
		ViewHolder holder;

		if (convertView == null) {
		    convertView = mInflater.inflate(R.layout.geatte_feedback_item_view, parent, false);
		    holder = new ViewHolder();
		    holder.interestImageView = (AsyncImageView) convertView.findViewById(R.id.geatte_feedback_item_interest_thumbnail);
		    holder.interestImageView.setImageProcessor(mImageProcessor);
		    holder.contactImageView = (ImageView) convertView.findViewById(R.id.geatte_feedback_item_contact_thumbnail);
		    holder.textViewTitle = (TextView) convertView.findViewById(R.id.geatte_feedback_item_title);
		    holder.textViewSubTitle = (TextView) convertView.findViewById(R.id.geatte_feedback_item_subtitle);

		    convertView.setTag(holder);
		} else {
		    holder = (ViewHolder) convertView.getTag();
		}

		GeatteFeedbackItem tItem = (GeatteFeedbackItem) item;
		holder.textViewTitle.setText(tItem.text);
		holder.textViewSubTitle.setText(tItem.subtitle);
		//setTag(item.id);

		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteAllFeedbackXActivity:getView() : async image set to bytearray for length= " + tItem.interestThumbnail.length);
		}
		holder.interestImageView.setImageBitmap(BitmapFactory.decodeByteArray(tItem.interestThumbnail, 0, tItem.interestThumbnail.length));

		if (tItem.contactBitmap != null) {
		    holder.contactImageView.setImageBitmap(tItem.contactBitmap);
		} else {
		    holder.contactImageView.setImageResource(tItem.contactDrawableId);
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
    public int createLayout() {
	return R.layout.geatte_feedback_list_content;
    }

}
