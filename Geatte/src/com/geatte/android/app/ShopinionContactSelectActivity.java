package com.geatte.android.app;

import greendroid.app.GDListActivity;
import greendroid.widget.ItemAdapter;
import greendroid.widget.ActionBar.OnActionBarListener;
import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;

import com.geatte.android.view.GeatteContactItem;
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
import android.widget.Toast;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ShopinionContactSelectActivity extends GDListActivity {

    private static final int MENU_REFRESH = Menu.FIRST;
    private static final int MENU_INVITE = Menu.FIRST + 1;
    private final Handler mHandler = new Handler();
    private ListView mListView = null;
    private ProgressDialog mDialog;
    ThumbnailCheckBoxItemAdapter mCheckboxAdapter = null;
    private String mImageRandomId;
    private String mGeatteId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionContactSelectActivity:onCreate() START");
	}

	Bundle extras = getIntent().getExtras();
	mImageRandomId = (String) (extras != null ? extras.get(Config.EXTRA_IMAGE_RANDOM_ID) : null);

	// Broadcast receiver to get notification from GeatteContactsService to update contact list
	registerReceiver(receiver,
		new IntentFilter(Config.INTENT_ACTION_UPDATE_CONTACTS));

	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionContactSelectActivity:onCreate() END");
	}
    }

    @Override
    public int createLayout() {
	return R.layout.shopinion_contacts_selector_content;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
	super.onPostCreate(savedInstanceState);

	setTitle(R.string.contacts_view_name);

	Button btnClear = (Button) findViewById(R.id.contacts_clean_btn);
	btnClear.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "ShopinionContactSelectActivity:onPostCreate() : clean contacts selections");
		}
		clearSelections();
	    }
	});

	Button btnBack = (Button) findViewById(R.id.contacts_back_btn);
	btnBack.setOnClickListener(new OnClickListener() {
	    // redirect to geatte edit
	    public void onClick(View v) {
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "ShopinionContactSelectActivity:onPostCreate() : save contacts selections");
		}
		setResult(RESULT_OK);
		finish();
	    }
	});

	Button btnSend = (Button) findViewById(R.id.contacts_send_btn);
	btnSend.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		final SharedPreferences prefs = getApplicationContext().getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
		String selectedContacts = prefs.getString(Config.PREF_SELECTED_CONTACTS, null);
		if (selectedContacts != null && selectedContacts.length() > 0) {
		    mDialog = ProgressDialog.show(ShopinionContactSelectActivity.this, "Sending to friends", "Please wait...", true);
		    new UploadInterestTask().execute();
		} else {
		    Toast.makeText(getApplicationContext(), "Please Select Any Friend To Send", Toast.LENGTH_SHORT).show();
		}
	    }
	});

	mListView = getListView();
	mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    private void fillList() {
	try {
	    final GeatteThumbnailItem warnItem;
	    List<Item> items = getContacts();
	    if (items.size() == 0) {
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "ShopinionContactSelectActivity:onCreate() : No contacts available!!");
		}
		warnItem = new GeatteThumbnailItem("Click menu to invite friends", null, R.drawable.email);
	    } else {
		warnItem = null;
	    }
	    final GeatteProgressItem progressItem = new GeatteProgressItem("Retrieving contacts", true);
	    items.add(progressItem);

	    mCheckboxAdapter = new ThumbnailCheckBoxItemAdapter(this, items);
	    setListAdapter(mCheckboxAdapter);

	    mHandler.postDelayed(new Runnable() {
		public void run() {
		    if (warnItem != null) {
			mCheckboxAdapter.insert(warnItem, 0);
		    }
		    mCheckboxAdapter.remove(progressItem);
		    mCheckboxAdapter.notifyDataSetChanged();
		}
	    },50);
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "ShopinionContactSelectActivity:fillList() :  ERROR ", e);
	}
    }

    @Override
    protected void onResume() {
	super.onResume();
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionContactSelectActivity:onResume(): START");
	}
	fillList();
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "ShopinionContactSelectActivity:onResume(): END");
	}
    }

    @Override
    public void onPause() {
	super.onPause();
	if (getListAdapter() != null) {
	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, "ShopinionContactSelectActivity:onPause(): execute gc");
	    }
	    for (int i = 0; i < getListAdapter().getCount(); i++) {
		if (getListAdapter().getItem(i) instanceof GeatteContactItem) {
		    GeatteContactItem item = (GeatteContactItem) getListAdapter().getItem(i);
		    if (item.contactBitmap != null) {
			item.contactBitmap.recycle();
		    }
		    item.contactBitmap = null;
		}
	    }
	}
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
		    Log.d(Config.LOGTAG, "ShopinionContactSelectActivity:getContacts() : Process contact = " + counter);
		}

		String contactPhone = contactCur.getString(contactCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_CONTACT_PHONE_NUMBER));
		String contactId = contactCur.getString(contactCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_CONTACT_ID));
		String contactName = contactCur.getString(contactCur.getColumnIndexOrThrow(GeatteDBAdapter.KEY_CONTACT_NAME));
		int contactIdInt = Integer.parseInt(contactId);
		Bitmap contactBitmap = queryPhotoForContact(contactIdInt);

		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "ShopinionContactSelectActivity:getContacts() : add one GeatteThumbnailCheckbox, contactPhone = " + contactPhone
			    + ", contactId = " + contactId + ", contactName = " + contactName);
		}
		if (contactBitmap != null) {
		    items.add(new GeatteThumbnailCheckbox(contactName, null, contactPhone, contactBitmap));
		} else {
		    items.add(new GeatteThumbnailCheckbox(contactName, null, contactPhone, R.drawable.profile));
		    if(Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, "ShopinionContactSelectActivity:getContacts() : contact has no profile photo available, " +
				"use default for contactName = " + contactName);
		    }
		}

		contactCur.moveToNext();

	    }
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "ShopinionContactSelectActivity:getContacts() :  ERROR ", e);
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
	    } else if (item instanceof GeatteThumbnailItem) {
		ItemView cell = item.newView(mContext, null);
		cell.prepareItemView();
		cell.setObject(item);
		return (View) cell;
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
		    Log.d(Config.LOGTAG, "ShopinionContactSelectActivity:ThumbnailCheckBoxItemAdapter select phone = " + phone);
		}
	    } else {
		if (this.selectedContacts.contains(phone)) {
		    this.selectedContacts.remove(phone);
		}
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "ShopinionContactSelectActivity:ThumbnailCheckBoxItemAdapter un-select phone = " + phone);
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
    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	menu.add(0, ShopinionContactSelectActivity.MENU_REFRESH, 0, R.string.menu_refresh).setIcon(
		android.R.drawable.ic_menu_rotate);
	menu.add(0, ShopinionContactSelectActivity.MENU_INVITE, 0, R.string.menu_invite).setIcon(
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
	    startService(new Intent(this.getApplicationContext(), GeatteContactsService.class));
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

	    mCheckboxAdapter = new ThumbnailCheckBoxItemAdapter(ShopinionContactSelectActivity.this, items);
	    setListAdapter(mCheckboxAdapter);

	    mHandler.postDelayed(new Runnable() {
		public void run() {
		    if (warnItem != null) {
			mCheckboxAdapter.insert(warnItem, 0);
		    }
		    mCheckboxAdapter.remove(progressItem);
		    mCheckboxAdapter.notifyDataSetChanged();
		}
	    },50);
	    Log.d(Config.LOGTAG, "updateContactsTask:onPostExecute() END");
	}
    }

    class UploadInterestTask extends AsyncTask<String, String, String> {
	@Override
	protected String doInBackground(String... strings) {
	    try {
		String title = getCaptionFromPref(false);
		String desc = getDescFromPref(false);

		Context context = getApplicationContext();
		final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
		String selectedContacts = prefs.getString(Config.PREF_SELECTED_CONTACTS, null);

		if (selectedContacts == null || selectedContacts.length() == 0) {
		    Log.e(Config.LOGTAG, " GeatteUploadTask:doInBackground(): selectedContacts is null, invalid selectedContacts = "
			    + selectedContacts);
		}

		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		String myNumber = DeviceRegistrar.getPhoneNumber(getApplicationContext());
		entity.addPart(Config.GEATTE_FROM_NUMBER_PARAM, new StringBody(myNumber));
		String myCountryCode = DeviceRegistrar.getPhoneConuntryCode(getApplicationContext());
		entity.addPart(Config.GEATTE_COUNTRY_ISO_PARAM, new StringBody(myCountryCode));
		entity.addPart(Config.GEATTE_TO_NUMBER_PARAM, new StringBody(selectedContacts));
		entity.addPart(Config.GEATTE_TITLE_PARAM, new StringBody(title));
		entity.addPart(Config.GEATTE_DESC_PARAM, new StringBody(desc));
		entity.addPart(Config.GEATTE_IMAGE_RANDOM_ID_PARAM, new StringBody(mImageRandomId));

		String accountName = prefs.getString(Config.PREF_USER_EMAIL, null);

		AppEngineClient client = new AppEngineClient(getApplicationContext(), accountName);
		HttpResponse response = client.makeRequestWithEntity(Config.ITEM_UPLOAD_PATH, entity);

		int respStatusCode = response.getStatusLine().getStatusCode();

		if (response.getEntity() != null) {

		    JSONObject jResponse = null;
		    BufferedReader reader = new BufferedReader(
			    new InputStreamReader(
				    response.getEntity().getContent(), "UTF-8"));

		    char[] tmp = new char[2048];
		    StringBuffer body = new StringBuffer();
		    while (true) {
			int cnt = reader.read(tmp);
			if (cnt <= 0) {
			    break;
			}
			body.append(tmp, 0, cnt);
		    }

		    if (respStatusCode == 400 || respStatusCode == 500) {

			//when resp is RETRY, redirect to geatte canvas
			if (body.toString().contains(Config.RETRY_STATUS)) {
			    if (mDialog != null && mDialog.isShowing()) {
				try {
				    if(Config.LOG_DEBUG_ENABLED) {
					Log.d(Config.LOGTAG, " GeatteUploadTask:doInBackground(): try to dismiss mDialog");
				    }
				    mDialog.dismiss();
				    mDialog = null;
				} catch (Exception ex) {
				    Log.w(Config.LOGTAG, " GeatteUploadTask:doInBackground(): failed to dismiss mDialog", ex);
				}
			    }
			    this.publishProgress(getString(R.string.upload_text_retry));
			    Log.w(Config.LOGTAG, " GeatteUploadTask Got RETRY, body = " + body.toString());
			    return Config.RETRY_STATUS;
			} else {
			    Log.w(Config.LOGTAG, " GeatteUploadTask Error: " + respStatusCode + " " + body.toString());
			    throw new Exception(" GeatteUploadTask Error: " + respStatusCode + " " + body.toString());
			}
		    }

		    try {
			jResponse = new JSONObject(URLDecoder.decode((body.toString()==null ? "" : body.toString()), Config.ENCODE_UTF8));
		    } catch (JSONException e) {
			Log.e(Config.LOGTAG, " GeatteUploadTask:doInBackground(): unable to read response after upload geatte to server", e);
		    }

		    if (jResponse != null) {
			mGeatteId = jResponse.getString(Config.GEATTE_ID_PARAM);
			Log.d(Config.LOGTAG, " GeatteUploadTask:doInBackground : GOT geatteId = " + mGeatteId);
		    }

		    if(Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, " GeatteUploadTask Response: " + jResponse);
		    }

		    return mGeatteId;
		}
	    } catch (Exception e) {
		Log.e(Config.LOGTAG, e.getMessage(), e);
		if (mDialog != null && mDialog.isShowing()) {
		    try {
			if(Config.LOG_DEBUG_ENABLED) {
			    Log.d(Config.LOGTAG, " GeatteUploadTask:doInBackground(): try to dismiss mDialog");
			}
			mDialog.dismiss();
			mDialog = null;
		    } catch (Exception ex) {
			Log.w(Config.LOGTAG, " GeatteUploadTask:doInBackground(): failed to dismiss mDialog", ex);
		    }
		}
		this.publishProgress(getString(R.string.upload_text_error));
	    }
	    return null;
	}

	@Override
	protected void onProgressUpdate(String... values) {
	    super.onProgressUpdate(values);
	    if (values.length > 0) {
		Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_LONG).show();
	    }
	}

	@Override
	protected void onPostExecute(String geatteId) {
	    if (geatteId != null && geatteId.equals(Config.RETRY_STATUS)) {
		if(Config.LOG_DEBUG_ENABLED) {
		    Log.d(Config.LOGTAG, "GeatteUploadTask:onPostExecute(): got retry");
		}
		return;
	    }
	    try {
		if (mDialog != null && mDialog.isShowing()) {
		    try {
			if(Config.LOG_DEBUG_ENABLED) {
			    Log.d(Config.LOGTAG, "GeatteUploadTask:onPostExecute(): try to dismiss mDialog");
			}
			mDialog.dismiss();
			mDialog = null;
		    } catch (Exception e) {
			Log.w(Config.LOGTAG, "GeatteUploadTask:onPostExecute(): failed to dismiss mDialog", e);
		    }
		}
		Intent resultIntent = new Intent();
		if (geatteId != null) {
		    Toast.makeText(getApplicationContext(), "Geatte sent successfully", Toast.LENGTH_LONG).show();
		    resultIntent.putExtra(Config.GEATTE_ID_PARAM, geatteId);
		}
		setResult(Activity.RESULT_OK, resultIntent);
	    } catch (Exception e) {
		Log.e(Config.LOGTAG, e.getMessage(), e);
		setResult(RESULT_CANCELED);
		Toast.makeText(getApplicationContext(), getString(R.string.upload_text_error), Toast.LENGTH_LONG)
		.show();
	    } finally {
		finish();
	    }
	}
    }

    private String getCaptionFromPref(boolean reset) {
	final SharedPreferences prefs = this.getApplicationContext().getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	String caption = prefs.getString(Config.PREF_SEND_CAPTION, "");

	if (reset) {
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.remove(Config.PREF_SEND_CAPTION);
	    editor.commit();
	}
	return caption;
    }

    private String getDescFromPref(boolean reset) {
	final SharedPreferences prefs = this.getApplicationContext().getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	String caption = prefs.getString(Config.PREF_SEND_DESC, "");

	if (reset) {
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.remove(Config.PREF_SEND_DESC);
	    editor.commit();
	}
	return caption;
    }

    @Override
    public void onPreContentChanged() {
	super.onPreContentChanged();
	getActionBar().setOnActionBarListener(mActionBarOnVotingListener);
    }

    private OnActionBarListener mActionBarOnVotingListener = new OnActionBarListener() {
	public void onActionBarItemClicked(int position) {
	    if (position == OnActionBarListener.HOME_ITEM) {
		setResult(RESULT_OK);
		finish();
	    } else {
		if (!onHandleActionBarItemClick(getActionBar().getItem(position), position)) {
		    if (Config.LOG_DEBUG_ENABLED) {
			Log.w(Config.LOGTAG, "Click on item at position " + position + " dropped down to the floor");
		    }
		}
	    }
	}
    };

}