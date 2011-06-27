package com.geatte.android.app;

import greendroid.app.GDListActivity;
import greendroid.widget.ItemAdapter;
import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;

import java.util.ArrayList;
import java.util.List;

import com.cyrilmottier.android.greendroid.R;
import com.geatte.android.view.GeatteThumbnailItem;
import com.geatte.android.view.SeparatorThumbnailItem;
import com.geatte.android.view.ThumbnailBitmapItem;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class GeatteFeedbackActivity extends GDListActivity {

    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setTitle(R.string.app_name);

	Bundle extras = getIntent().getExtras();
	final String geatteId = extras != null ? extras.getString(Config.GEATTE_ID_PARAM) : null;
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "GeatteFeedbackActivity:onCreate get extra geatteId = " + geatteId);
	}
	final Long interestId = extras != null ? extras.getLong(GeatteDBAdapter.KEY_INTEREST_ID) : null;
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "GeatteFeedbackActivity:onCreate get extra interestId = " + interestId);
	}

	List<Item> items = new ArrayList<Item>();
	final GeatteThumbnailItem warnItem;
	final ThumbnailBitmapItem geatteItem;

	if (geatteId != null || (interestId != null && interestId != 0L)) {
	    ThumbnailBitmapItem item = createFeedbackItemsFromFetchResult(geatteId, interestId, items);
	    if (item == null) {
		geatteItem = null;
		warnItem = new GeatteThumbnailItem("No geatte available", null, R.drawable.invalid);
	    } else {
		geatteItem = item;
		warnItem = null;
	    }
	}
	else {
	    warnItem = new GeatteThumbnailItem("Invalid geatte", null, R.drawable.invalid);
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
    }

    private ThumbnailBitmapItem createFeedbackItemsFromFetchResult(String geatteId, Long interestId, List<Item> items) {
	ThumbnailBitmapItem geatteItem = null;

	final GeatteDBAdapter mDbHelper = new GeatteDBAdapter(this);
	Cursor myInterestCur = null;
	Cursor feedbackCur = null;
	try {
	    mDbHelper.open();

	    // always convert to geatteId
	    if (interestId != null && interestId != 0L) {
		geatteId = mDbHelper.getGeatteIdFromInterestId(interestId);
	    }

	    if (geatteId != null) {
		myInterestCur = mDbHelper.fetchMyInterest(geatteId);
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteFeedbackActivity:createFeedbackItemsFromFetchResult fetched my interest by geatteId = " + geatteId);
		}
	    } else {
		Log.w(Config.LOGTAG, "GeatteFeedbackActivity:createFeedbackItemsFromFetchResult invalid interestId and geatteId, ignore create!!");
		return null;
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
		List<Item> yesItems = new ArrayList<Item>();
		List<Item> noItems = new ArrayList<Item>();
		List<Item> maybeItems = new ArrayList<Item>();

		feedbackCur = mDbHelper.fetchMyInterestFeedback(geatteId);
		feedbackCur.moveToFirst();

		while (feedbackCur.isAfterLast() == false) {
		    String vote = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_VOTE));
		    if (vote.equals(Config.LIKE.YES.toString())) {
			if (yesItems.size() == 0) {
			    //yesItems.add(new SeparatorItem(feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_VOTER))));
			    yesItems.add(new SeparatorThumbnailItem("Go Get It!!", R.drawable.yes));
			}
			String voter = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_VOTER));
			// get contact name for this voter
			String voterName = mDbHelper.fetchContactName(voter);
			// get voter contact thumbnail
			Integer contactId = mDbHelper.fetchContactId(voter);
			Bitmap contactBitmap = queryPhotoForContact(contactId);

			String text = new StringBuilder(voterName).append(" LOVE it!").toString();
			String comment = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_COMMENT));

			if (contactBitmap != null) {
			    yesItems.add(new GeatteThumbnailItem(text, comment, contactBitmap));
			} else {
			    yesItems.add(new GeatteThumbnailItem(text, comment, R.drawable.profile));
			}

		    }
		    if (vote.equals(Config.LIKE.NO.toString())) {
			if (noItems.size() == 0) {
			    //yesItems.add(new SeparatorItem(feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_VOTER))));
			    noItems.add(new SeparatorThumbnailItem("Don't Get It!!", R.drawable.no));
			}
			String voter = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_VOTER));
			// get contact name for this voter
			String voterName = mDbHelper.fetchContactName(voter);
			// get voter contact thumbnail
			Integer contactId = mDbHelper.fetchContactId(voter);
			Bitmap contactBitmap = queryPhotoForContact(contactId);

			String text = new StringBuilder(voterName).append(" said NO!").toString();
			String comment = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_COMMENT));

			if (contactBitmap != null) {
			    noItems.add(new GeatteThumbnailItem(text, comment, contactBitmap));
			} else {
			    noItems.add(new GeatteThumbnailItem(text, comment, R.drawable.profile));
			}
		    }

		    if (vote.equals(Config.LIKE.MAYBE.toString())) {
			if (maybeItems.size() == 0) {
			    //yesItems.add(new SeparatorItem(feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_VOTER))));
			    maybeItems.add(new SeparatorThumbnailItem("Think Twice!!", R.drawable.maybe));
			}
			String voter = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_VOTER));
			// get contact name for this voter
			String voterName = mDbHelper.fetchContactName(voter);
			// get voter contact thumbnail
			Integer contactId = mDbHelper.fetchContactId(voter);
			Bitmap contactBitmap = queryPhotoForContact(contactId);

			String text = new StringBuilder(voterName).append(" said MAYBE!").toString();
			String comment = feedbackCur.getString(feedbackCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_FEEDBACK_COMMENT));

			if (contactBitmap != null) {
			    maybeItems.add(new GeatteThumbnailItem(text, comment, contactBitmap));
			} else {
			    maybeItems.add(new GeatteThumbnailItem(text, comment, R.drawable.profile));
			}
		    }

		    feedbackCur.moveToNext();

		}

		items.addAll(yesItems);
		items.addAll(maybeItems);
		items.addAll(noItems);
	    }
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "GeatteFeedbackActivity:createFeedbackItemsFromFetchResult ERROR ", e);
	} finally {
	    if (myInterestCur != null) {
		myInterestCur.close();
	    }

	    if (feedbackCur != null) {
		feedbackCur.close();
	    }

	    mDbHelper.close();
	}

	return geatteItem;
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

    @Override
    public int createLayout() {
	return R.layout.geatte_feedback_list_content;
    }

}
