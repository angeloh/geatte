package com.geatte.android.app;

import greendroid.app.GDListActivity;
import greendroid.widget.ItemAdapter;
import greendroid.widget.item.Item;
import java.util.ArrayList;
import java.util.List;

import com.cyrilmottier.android.greendroid.R;
import com.geatte.android.view.GeatteContactItem;
import com.geatte.android.view.GeatteContactItemView;
import com.geatte.android.view.GeatteProgressItem;
import com.geatte.android.view.GeatteThumbnailItem;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class GeatteContactInfoActivity extends GDListActivity {

    private static final int MENU_REFRESH = Menu.FIRST;
    private static final int MENU_INVITE = Menu.FIRST + 1;
    private final Handler mHandler = new Handler();
    private ListView mListView = null;
    ThumbnailItemAdapter mContactsAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(Config.LOGTAG, "GeatteContactInfoActivity:onCreate() START");
	setTitle(R.string.contacts_info_view_name);

	Button btnOk = (Button) findViewById(R.id.contacts_info_ok_btn);
	btnOk.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteContactInfoActivity:onCreate() : return to previous activity");
		}
		setResult(RESULT_OK);
		finish();
	    }
	});

	final GeatteThumbnailItem warnItem;
	try {
	    List<Item> items = getContacts();
	    if (items.size() == 0) {
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteContactInfoActivity:onCreate() : No contacts available!!");
		}
		warnItem = new GeatteThumbnailItem("Click menu to invite friends", null, R.drawable.email);
	    } else {
		warnItem = null;
	    }
	    final GeatteProgressItem progressItem = new GeatteProgressItem("Retrieving contacts", true);
	    items.add(progressItem);

	    mContactsAdapter = new ThumbnailItemAdapter(this, items);
	    setListAdapter(mContactsAdapter);

	    // Broadcast receiver to get notification from GeatteContactsService to update contact list
	    registerReceiver(receiver,
		    new IntentFilter(Config.INTENT_ACTION_UPDATE_CONTACTS));

	    mHandler.postDelayed(new Runnable() {
		public void run() {
		    if (warnItem != null) {
			mContactsAdapter.insert(warnItem, 0);
		    }
		    mContactsAdapter.remove(progressItem);
		    mContactsAdapter.notifyDataSetChanged();
		}
	    },500);
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "GeatteContactInfoActivity:onCreate() :  ERROR ", e);
	}

	mListView = getListView();
	mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

	Log.d(Config.LOGTAG, "GeatteContactInfoActivity:onCreate() END");
    }

    @Override
    protected void onResume() {
	super.onResume();
	Log.d(Config.LOGTAG, "GeatteContactInfoActivity:onResume(): START");
	mContactsAdapter.notifyDataSetChanged();
	Log.d(Config.LOGTAG, "GeatteContactInfoActivity:onResume(): END");
    }

    @Override
    public void onDestroy() {
	unregisterReceiver(receiver);
	super.onDestroy();
    }

    private List<Item> getContacts() {
	List<Item> items = new ArrayList<Item>();

	final GeatteDBAdapter mDbHelper = new GeatteDBAdapter(this);
	Cursor contactCur = null;
	try {
	    mDbHelper.open();
	    contactCur = mDbHelper.fetchAllContacts();

	    if (Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, "GeatteContactInfoActivity:getContacts() : Got cursor for all contacts");
	    }

	    contactCur.moveToFirst();
	    int counter = 0;
	    while (contactCur.isAfterLast() == false) {
		++counter;
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteContactInfoActivity:getContacts() : Process contact = " + counter);
		}

		String contactPhone = contactCur.getString(contactCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_CONTACT_PHONE_NUMBER));
		String contactId = contactCur.getString(contactCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_CONTACT_ID));
		String contactName = contactCur.getString(contactCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_CONTACT_NAME));
		int contactIdInt = Integer.parseInt(contactId);
		Bitmap contactBitmap = queryPhotoForContact(contactIdInt);

		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteContactInfoActivity:getContacts() : add one GeatteThumbnailCheckbox, contactPhone = " + contactPhone
			    + ", contactId = " + contactId + ", contactName = " + contactName);
		}
		if (contactBitmap != null) {
		    items.add(new GeatteContactItem(contactName, null, contactPhone, contactBitmap));
		} else {
		    items.add(new GeatteContactItem(contactName, null, contactPhone, R.drawable.profile));
		    if (Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, "GeatteContactInfoActivity:getContacts() : contact has no profile photo available, " +
				"use default for contactName = " + contactName);
		    }
		}

		contactCur.moveToNext();

	    }
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "GeatteContactInfoActivity:getContacts() :  ERROR ", e);
	} finally {
	    if (contactCur != null) {
		contactCur.close();
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
     * A ThumbnailItemAdapter is an extension of an ItemAdapter for
     * GeatteContactItem to return associated view.
     */
    private class ThumbnailItemAdapter extends ItemAdapter {

	private Context mContext;

	public ThumbnailItemAdapter(Context context, Item[] items) {
	    super(context, items);
	    mContext = context;
	}

	public ThumbnailItemAdapter(Context context, List<Item> items) {
	    super(context, items);
	    mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

	    final Item item = (Item) getItem(position);
	    if (item instanceof GeatteContactItem) {
		GeatteContactItemView contactView = (GeatteContactItemView) item.newView(mContext, null);
		contactView.prepareItemView();
		contactView.setObject(item);
		contactView.setTag(((GeatteContactItem) item).phone);
		return (View) contactView;
	    } else {
		return super.getView(position, convertView, parent);
	    }
	}

    }

    @Override
    public int createLayout() {
	return R.layout.geatte_contacts_info_list_content;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	menu.add(0, GeatteContactInfoActivity.MENU_REFRESH, 0, R.string.menu_refresh).setIcon(
		android.R.drawable.ic_menu_rotate);
	menu.add(0, GeatteContactInfoActivity.MENU_INVITE, 0, R.string.menu_invite).setIcon(
		android.R.drawable.ic_menu_share);

	return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	switch (item.getItemId()) {
	case MENU_REFRESH:
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, "trying to refresh contacts service");
	    }
	    startService(new Intent(this, GeatteContactsService.class));
	    return true;
	case MENU_INVITE:
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, "trying to send invite email");
	    }
	    Intent intent = new Intent(Intent.ACTION_SEND);
	    intent.setType("message/rfc822");
	    intent.putExtra(Intent.EXTRA_SUBJECT, Config.INVITE_EMAIL_SUBJECT);
	    intent.putExtra(Intent.EXTRA_TEXT, Config.INVITE_EMAIL_TEXT);
	    Intent mailer = Intent.createChooser(intent, null);
	    startActivity(mailer);
	    return true;
	}
	return super.onMenuItemSelected(featureId, item);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, "trying to update contact list");
	    }
	    updateContactList();
	}
    };

    private void updateContactList() {
	new updateContactsTask().execute();
    }

    private class updateContactsTask extends AsyncTask<Void, Void, List<Item>> {

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected List<Item> doInBackground(Void...unused) {
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, "updateContactsTask:doInBackground() : refresh contacts from db");
	    }
	    return getContacts();
	}

	/**
	 * On post execute.
	 * Close the progress dialog
	 */
	@Override
	protected void onPostExecute(List<Item> items) {
	    Log.d(Config.LOGTAG, "updateContactsTask:onPostExecute() START");
	    final GeatteThumbnailItem warnItem;
	    if (items.size() == 0) {
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "updateContactsTask:onPostExecute() : No contacts available!!");
		}
		warnItem = new GeatteThumbnailItem("Click menu to invite friends", null, R.drawable.email);
	    } else {
		warnItem = null;
	    }
	    final GeatteProgressItem progressItem = new GeatteProgressItem("Retrieving contacts", true);
	    items.add(progressItem);

	    mContactsAdapter = new ThumbnailItemAdapter(GeatteContactInfoActivity.this, items);
	    setListAdapter(mContactsAdapter);

	    mHandler.postDelayed(new Runnable() {
		public void run() {
		    if (warnItem != null) {
			mContactsAdapter.insert(warnItem, 0);
		    }
		    mContactsAdapter.remove(progressItem);
		    mContactsAdapter.notifyDataSetChanged();
		}
	    },500);
	    Log.d(Config.LOGTAG, "updateContactsTask:onPostExecute() END");
	}
    }

}