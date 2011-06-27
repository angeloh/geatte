package com.geatte.android.app;

import greendroid.app.GDListActivity;
import greendroid.widget.ItemAdapter;
import greendroid.widget.item.Item;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cyrilmottier.android.greendroid.R;
import com.geatte.android.view.GeatteProgressItem;
import com.geatte.android.view.GeatteThumbnailCheckbox;
import com.geatte.android.view.GeatteThumbnailCheckboxView;
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
import android.widget.CheckBox;
import android.widget.ListView;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class GeatteContactSelectActivity extends GDListActivity {

    private static final int MENU_REFRESH = Menu.FIRST;
    private static final int MENU_INVITE = Menu.FIRST + 1;
    private final Handler mHandler = new Handler();
    private ListView mListView = null;
    ThumbnailCheckBoxItemAdapter mCheckboxAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(Config.LOGTAG, "GeatteContactSelectActivity:onCreate() START");
	setTitle(R.string.contacts_view_name);

	Button btnClear = (Button) findViewById(R.id.contacts_clean_btn);
	btnClear.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteContactSelectActivity:onCreate() : clean contacts selections");
		}
		clearSelections();
	    }
	});

	Button btnSave = (Button) findViewById(R.id.contacts_save_btn);
	btnSave.setOnClickListener(new OnClickListener() {

	    // redirect to geatte edit
	    public void onClick(View v) {
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteContactSelectActivity:onCreate() : save contacts selections");
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
		    Log.d(Config.LOGTAG, "GeatteContactSelectActivity:onCreate() : No contacts available!!");
		}
		warnItem = new GeatteThumbnailItem("Click menu to invite friends", null, R.drawable.email);
	    } else {
		warnItem = null;
	    }
	    final GeatteProgressItem progressItem = new GeatteProgressItem("Retrieving contacts", true);
	    items.add(progressItem);

	    mCheckboxAdapter = new ThumbnailCheckBoxItemAdapter(this, items);
	    setListAdapter(mCheckboxAdapter);

	    // Broadcast receiver to get notification from GeatteContactsService to update contact list
	    registerReceiver(receiver,
		    new IntentFilter(Config.INTENT_ACTION_UPDATE_CONTACTS));

	    mHandler.postDelayed(new Runnable() {
		public void run() {
		    if (warnItem != null) {
			mCheckboxAdapter.insert(warnItem, 0);
		    }
		    mCheckboxAdapter.remove(progressItem);
		    mCheckboxAdapter.notifyDataSetChanged();
		}
	    },500);
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "GeatteContactSelectActivity:onCreate() :  ERROR ", e);
	}

	mListView = getListView();
	mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

	Log.d(Config.LOGTAG, "GeatteContactSelectActivity:onCreate() END");
    }

    @Override
    protected void onResume() {
	super.onResume();
	Log.d(Config.LOGTAG, "GeatteContactSelectActivity:onResume(): START");
	mCheckboxAdapter.notifyDataSetChanged();
	Log.d(Config.LOGTAG, "GeatteContactSelectActivity:onResume(): END");
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

	    contactCur.moveToFirst();
	    int counter = 0;
	    while (contactCur.isAfterLast() == false) {
		++counter;
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteContactSelectActivity:getContacts() : Process contact = " + counter);
		}

		String contactPhone = contactCur.getString(contactCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_CONTACT_PHONE_NUMBER));
		String contactId = contactCur.getString(contactCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_CONTACT_ID));
		String contactName = contactCur.getString(contactCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_CONTACT_NAME));
		int contactIdInt = Integer.parseInt(contactId);
		Bitmap contactBitmap = queryPhotoForContact(contactIdInt);

		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteContactSelectActivity:getContacts() : add one GeatteThumbnailCheckbox, contactPhone = " + contactPhone
			    + ", contactId = " + contactId + ", contactName = " + contactName);
		}
		if (contactBitmap != null) {
		    items.add(new GeatteThumbnailCheckbox(contactName, null, contactPhone, contactBitmap));
		} else {
		    items.add(new GeatteThumbnailCheckbox(contactName, null, contactPhone, R.drawable.profile));
		    if(Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, "GeatteContactSelectActivity:getContacts() : contact has no profile photo available, " +
				"use default for contactName = " + contactName);
		    }
		}

		contactCur.moveToNext();

	    }
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "GeatteContactSelectActivity:getContacts() :  ERROR ", e);
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

    private void clearSelections() {
	for (int i = 0; i < this.mListView.getCount(); i++) {
	    GeatteThumbnailCheckboxView checkboxView = (GeatteThumbnailCheckboxView) this.mListView.getChildAt(i);
	    checkboxView.setCheckboxChecked(false);
	}
	this.mCheckboxAdapter.clearSelectedContacts();
    }

    /**
     * A ThumbnailCheckBoxItemAdapter is an extension of an ItemAdapter for
     * GeatteThumbnailCheckbox
     * to return associated view.
     */
    private class ThumbnailCheckBoxItemAdapter extends ItemAdapter implements
    OnClickListener {

	private Context mContext;
	private ArrayList<String> selectedContacts = new ArrayList<String>();

	public ThumbnailCheckBoxItemAdapter(Context context, Item[] items) {
	    super(context, items);
	    mContext = context;
	    loadSelections();
	}

	public ThumbnailCheckBoxItemAdapter(Context context, List<Item> items) {
	    super(context, items);
	    mContext = context;
	    loadSelections();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

	    final Item item = (Item) getItem(position);
	    if (item instanceof GeatteThumbnailCheckbox) {
		GeatteThumbnailCheckboxView checkboxView = (GeatteThumbnailCheckboxView) item.newView(mContext, null);
		checkboxView.prepareItemView();
		checkboxView.setObject(item);
		if (this.selectedContacts.contains(((GeatteThumbnailCheckbox) item).phone)) {
		    checkboxView.setCheckboxChecked(true);
		}
		checkboxView.setCheckboxClickListener(this);
		checkboxView.setCheckboxTag(((GeatteThumbnailCheckbox) item).phone);
		return (View) checkboxView;
	    } else {
		return super.getView(position, convertView, parent);
	    }
	}

	@Override
	public void onClick(View v) {
	    CheckBox cBox = (CheckBox) v;
	    String phone = (String) cBox.getTag();

	    if (cBox.isChecked()) {
		if (!this.selectedContacts.contains(phone)) {
		    this.selectedContacts.add(phone);
		}
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteContactSelectActivity:ThumbnailCheckBoxItemAdapter select phone = " + phone);
		}
	    } else {
		if (this.selectedContacts.contains(phone)) {
		    this.selectedContacts.remove(phone);
		}
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteContactSelectActivity:ThumbnailCheckBoxItemAdapter un-select phone = " + phone);
		}
	    }

	    saveSelections();

	}

	public void clearSelectedContacts() {
	    this.selectedContacts.clear();
	    saveSelections();
	}

	private String getSavedItems() {
	    String savedItems = "";

	    for (int i = 0; i < selectedContacts.size(); i++) {

		if (savedItems.length() > 0) {
		    savedItems += ";" + this.selectedContacts.get(i);
		} else {
		    savedItems += this.selectedContacts.get(i);
		}

	    }
	    return savedItems;
	}

	private void saveSelections() {
	    // save the selections in the shared preference in private mode for the user
	    Context context = getApplicationContext();
	    final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = prefs.edit();
	    String selectContacts = getSavedItems();
	    editor.putString(Config.PREF_SELECTED_CONTACTS, selectContacts);
	    editor.commit();
	}

	private void loadSelections() {
	    // if the selections were previously saved load them
	    Context context = getApplicationContext();
	    final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);

	    if (prefs.contains(Config.PREF_SELECTED_CONTACTS)) {
		String savedItems = prefs.getString(Config.PREF_SELECTED_CONTACTS, "");
		this.selectedContacts.addAll(Arrays.asList(savedItems.split(";")));
	    }
	}
    }

    @Override
    public int createLayout() {
	return R.layout.geatte_contacts_list_content;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	menu.add(0, GeatteContactSelectActivity.MENU_REFRESH, 0, R.string.menu_refresh).setIcon(
		android.R.drawable.ic_menu_rotate);
	menu.add(0, GeatteContactSelectActivity.MENU_INVITE, 0, R.string.menu_invite).setIcon(
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

	    mCheckboxAdapter = new ThumbnailCheckBoxItemAdapter(GeatteContactSelectActivity.this, items);
	    setListAdapter(mCheckboxAdapter);

	    mHandler.postDelayed(new Runnable() {
		public void run() {
		    if (warnItem != null) {
			mCheckboxAdapter.insert(warnItem, 0);
		    }
		    mCheckboxAdapter.remove(progressItem);
		    mCheckboxAdapter.notifyDataSetChanged();
		}
	    },500);
	    Log.d(Config.LOGTAG, "updateContactsTask:onPostExecute() END");
	}
    }

}