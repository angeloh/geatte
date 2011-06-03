package com.geatte.android.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

/**
 * A service activity to get all contacts' phone numbers and send to server for filter.
 * Then store to db for future use of geatte contact list view.
 *
 */
public class GeatteContactsService extends Service {
    private static final String GET_CONTACTS_PATH = "/geattecontacts";

    /** Called when the service is first created. */
    @Override
    public void onCreate() {


	// Start up the thread running the service.  Note that we create a
	// separate thread because the service normally runs in the process's
	// main thread, which we don't want to block.
	Thread thr = new Thread(null, mTask, "AlarmService_Service");
	thr.start();
    }

    @Override
    public void onDestroy() {
    }


    private JSONObject queryAllRawContacts() {

	final String[] projection = new String[] { RawContacts.CONTACT_ID,
		// the contact id column
		RawContacts.DELETED // column if this contact is deleted
	};

	//	final Cursor rawContacts = managedQuery( RawContacts.CONTENT_URI,
	//		// the uri for raw contact provider
	//		projection, null, // selection = null, retrieve all entries
	//		null, // not required because selection does not contain parameters
	//		null); // do not order

	Cursor rawContacts = getContentResolver().query(RawContacts.CONTENT_URI, projection, null, null, null);

	final int contactIdColumnIndex = rawContacts.getColumnIndex(RawContacts.CONTACT_ID);
	final int deletedColumnIndex = rawContacts.getColumnIndex(RawContacts.DELETED);


	JSONObject json = new JSONObject();
	JSONArray jsonArray = new JSONArray();
	if (rawContacts.moveToFirst()) { // move the cursor to the first entry
	    while (!rawContacts.isAfterLast()) { // still a valid entry left?
		final int contactId = rawContacts.getInt(contactIdColumnIndex);
		final boolean deleted = (rawContacts.getInt(deletedColumnIndex) == 1);
		if (!deleted) {
		    queryAllPhoneNumbersForContact(jsonArray, String.valueOf(contactId));
		}
		rawContacts.moveToNext(); // move to the next entry
	    }
	}

	rawContacts.close();

	try {
	    json.put(Config.CONTACT_LIST, jsonArray);
	} catch (JSONException ex) {
	    Log.e(Config.LOGTAG,
		    "GeatteContactsService:queryAllRawContacts: json exception", ex);
	}
	return json;
    }

    /**
     * The function that runs in our worker thread
     */
    Runnable mTask = new Runnable() {
	public void run() {

	    JSONObject allContactsJson = queryAllRawContacts();

	    Log.d(Config.LOGTAG, "GeatteContactsService:Runnable Attemp to send json to contacts servlet: " + allContactsJson);

	    HttpClient client = new DefaultHttpClient();
	    HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
	    HttpResponse response;
	    try {
		HttpPost post = new HttpPost(Config.BASE_URL + GET_CONTACTS_PATH);
		StringEntity se = new StringEntity("JSON: " + allContactsJson.toString());
		se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		post.setEntity(se);
		response = client.execute(post);

		if (response.getStatusLine().getStatusCode() == 400 || response.getStatusLine().getStatusCode() == 500) {
		    Log.e(Config.LOGTAG, "GeatteContactsService Error: " + response.getStatusLine().getStatusCode()
			    + " " + response.getEntity().getContent());
		}

		if (response.getEntity() != null) {

		    JSONObject jResponse = null;
		    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
		    "UTF-8"));

		    char[] tmp = new char[2048];
		    StringBuffer body = new StringBuffer();
		    while (true) {
			int cnt = reader.read(tmp);
			if (cnt <= 0) {
			    break;
			}
			body.append(tmp, 0, cnt);
		    }
		    try {
			jResponse = new JSONObject(body.toString());
		    } catch (JSONException e) {
			Log.e(Config.LOGTAG,
				"GeatteContactsService:Runnable: unable to read response after send contacts to server", e);
		    }

		    Log.d(Config.LOGTAG, "GeatteContactsService:Runnable Response: " + jResponse);

		    // process json
		    processJsonResponse(jResponse);
		}

	    } catch (Exception ex) {
		Log.e(Config.LOGTAG, "GeatteContactsActivity:sendJson() Error: ", ex);
	    }

	    // Done with our work... stop the service!
	    GeatteContactsService.this.stopSelf();
	}
    };

    private void processJsonResponse(JSONObject jResponse) {
	JSONArray contactArray;

	final GeatteDBAdapter dbHelper = new GeatteDBAdapter(this);

	try {

	    dbHelper.open();

	    contactArray = jResponse.getJSONArray(Config.CONTACT_LIST);

	    for (int i = 0; i < contactArray.length(); i++) {
		try {
		    String phone = contactArray.getJSONObject(i).getString(Config.CONTACT_PHONE_NUMBER).toString();
		    String contactIdStr = contactArray.getJSONObject(i).getString(Config.CONTACT_ID).toString();
		    int contactId = Integer.parseInt(contactIdStr);
		    String name = queryNameForContact(contactId);
		    long ret = dbHelper.insertOrUpdateContact(phone, contactId, name);
		    if (ret >= 0) {
			Log.d(Config.LOGTAG, " GeatteContactsService:processJsonResponse: saved contact for phone = " + phone
				+ ", contactId = " + contactId + ", name = " + name + " to DB SUCCESSUL!");
		    } else {
			Log.d(Config.LOGTAG, " GeatteContactsService:processJsonResponse: updated contact for phone = " + phone
				+ ", contactId = " + contactId + ", name = " + name + " to DB SUCCESSUL!");
		    }
		} catch (Exception e) {
		    Log.e(Config.LOGTAG, "GeatteContactsService:processJsonResponse: exception", e);
		}
	    }

	} catch (JSONException ex) {
	    Log.e(Config.LOGTAG, "GeatteContactsService:processJsonResponse: json exception", ex);
	} catch (Exception e) {
	    Log.e(Config.LOGTAG, "GeatteContactsService:processJsonResponse: exception", e);
	} finally {
	    dbHelper.close();
	}

    }

    //    private ContactEntry queryDetailsForContactSpinnerEntry(int contactId) {
    //	final String[] projection = new String[] {
    //		Contacts.DISPLAY_NAME, // the name of the contact
    //		Contacts.PHOTO_ID // the id of the column in the data table for the image
    //	};
    //
    //	//	final Cursor contact = managedQuery(Contacts.CONTENT_URI, projection, Contacts._ID + "=?",
    //	//		// filter entries on the basis of the contact id
    //	//		new String[] { String.valueOf(contactId) },
    //	//		// the parameter to which the contact id column is compared to
    //	//		null);
    //	Cursor contact = getContentResolver().query(Contacts.CONTENT_URI, projection, Contacts._ID + "=?", new String[] { String.valueOf(contactId) }, null);
    //
    //	if (contact.moveToFirst()) {
    //	    final String name = contact.getString(contact.getColumnIndex(Contacts.DISPLAY_NAME));
    //	    final String photoId = contact.getString(contact.getColumnIndex(Contacts.PHOTO_ID));
    //	    final Bitmap photo;
    //	    if (photoId != null) {
    //		photo = queryContactBitmap(photoId);
    //	    } else {
    //		photo = null;
    //	    }
    //	    contact.close();
    //	    return new ContactEntry(contactId, photo, name);
    //	}
    //	contact.close();
    //	return null;
    //    }

    //    private Bitmap queryContactBitmap(String photoId) {
    //	//	final Cursor photo = managedQuery(Data.CONTENT_URI, new String[] { Photo.PHOTO }, // column
    //	//		// where the blob is stored
    //	//		Data._ID + "=?", // select row by id
    //	//		new String[] { photoId }, // filter by the given photoId
    //	//		null);
    //
    //	Cursor photo = getContentResolver().query(Data.CONTENT_URI, new String[] { Photo.PHOTO }, Data._ID + "=?", new String[] { photoId }, null);
    //
    //	final Bitmap photoBitmap;
    //	if (photo.moveToFirst()) {
    //	    byte[] photoBlob = photo.getBlob(photo.getColumnIndex(Photo.PHOTO));
    //	    photoBitmap = BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.length);
    //	} else {
    //	    photoBitmap = null;
    //	}
    //	photo.close();
    //	return photoBitmap;
    //    }

    //    public void queryAllPhoneNumbersForContact(int contactId, List<ListViewEntry> content) {
    //	final String[] projection = new String[] { Phone.NUMBER, Phone.TYPE, };
    //
    //	//	final Cursor phone = managedQuery(Phone.CONTENT_URI, projection, Data.CONTACT_ID + "=?", new String[] { String
    //	//		.valueOf(contactId) }, null);
    //
    //	Cursor phone = getContentResolver().query(Phone.CONTENT_URI, projection, Data.CONTACT_ID + "=?", new String[] { String
    //		.valueOf(contactId) }, null);
    //
    //	if (phone.moveToFirst()) {
    //	    final int contactNumberColumnIndex = phone.getColumnIndex(Phone.NUMBER);
    //	    final int contactTypeColumnIndex = phone.getColumnIndex(Phone.TYPE);
    //
    //	    while (!phone.isAfterLast()) {
    //		final String number = phone.getString(contactNumberColumnIndex);
    //		final int type = phone.getInt(contactTypeColumnIndex);
    //		content.add(new ListViewEntry(number, Phone.getTypeLabelResource(type), R.string.type_phone));
    //		phone.moveToNext();
    //	    }
    //
    //	}
    //	phone.close();
    //    }

    private String queryNameForContact(int contactId) {
	final String[] projection = new String[] {
		Contacts.DISPLAY_NAME, // the name of the contact
	};

	Cursor contact = getContentResolver().query(Contacts.CONTENT_URI, projection, Contacts._ID + "=?", new String[] { String.valueOf(contactId) }, null);

	if (contact.moveToFirst()) {
	    final String name = contact.getString(contact.getColumnIndex(Contacts.DISPLAY_NAME));
	    contact.close();
	    return name;
	}
	contact.close();
	return null;
    }

    public void queryAllPhoneNumbersForContact(JSONArray jsonArray, String contactId) {
	final String[] projection = new String[] { Phone.NUMBER, Phone.TYPE};

	//	final Cursor phone = managedQuery(Phone.CONTENT_URI, projection, Data.CONTACT_ID + "=?", new String[] { String
	//		.valueOf(contactId) }, null);

	Cursor phone = getContentResolver().query(Phone.CONTENT_URI, projection, Data.CONTACT_ID + "=?", new String[] { contactId }, null);

	if (phone.moveToFirst()) {
	    final int contactNumberColumnIndex = phone.getColumnIndex(Phone.NUMBER);
	    final int contactTypeColumnIndex = phone.getColumnIndex(Phone.TYPE);

	    while (!phone.isAfterLast()) {
		final String number = phone.getString(contactNumberColumnIndex);
		final int type = phone.getInt(contactTypeColumnIndex);
		try {
		    JSONObject json = new JSONObject();
		    json.put(Config.CONTACT_PHONE_NUMBER, number);
		    json.put(Config.CONTACT_ID, contactId);
		    json.put(Config.CONTACT_ID, contactId);
		    jsonArray.put(json);
		} catch (JSONException ex) {
		    Log.e(Config.LOGTAG,
			    "GeatteContactsService:queryAllPhoneNumbersForContact: json exception", ex);
		}
		//content.add(new ListViewEntry(number, Phone.getTypeLabelResource(type), R.string.type_phone));
		phone.moveToNext();
	    }

	}
	phone.close();
    }

    //    public void queryAllEmailAddressesForContact(int contactId, List<ListViewEntry> content) {
    //	final String[] projection = new String[] { Email.DATA,
    //		// use Email.ADDRESS for API-Level 11+
    //		Email.TYPE };
    //
    //	//	final Cursor email = managedQuery(Email.CONTENT_URI, projection, Data.CONTACT_ID + "=?", new String[] { String
    //	//		.valueOf(contactId) }, null);
    //
    //	Cursor email = getContentResolver().query(Email.CONTENT_URI, projection, Data.CONTACT_ID + "=?", new String[] { String
    //		.valueOf(contactId) }, null);
    //
    //	if (email.moveToFirst()) {
    //	    final int contactEmailColumnIndex = email.getColumnIndex(Email.DATA);
    //	    final int contactTypeColumnIndex = email.getColumnIndex(Email.TYPE);
    //
    //	    while (!email.isAfterLast()) {
    //		final String address = email.getString(contactEmailColumnIndex);
    //		final int type = email.getInt(contactTypeColumnIndex);
    //		content.add(new ListViewEntry(address, Email.getTypeLabelResource(type), R.string.type_email));
    //		email.moveToNext();
    //	    }
    //
    //	}
    //	email.close();
    //    }

    /**
     * This is the object that receives interactions from clients.  See RemoteService
     * for a more complete example.
     */
    private final IBinder mBinder = new Binder() {

	@Override
	protected boolean onTransact(int code, Parcel data, Parcel reply,
		int flags) throws RemoteException {
	    return super.onTransact(code, data, reply, flags);
	}
    };

    @Override
    public IBinder onBind(Intent intent) {
	return mBinder;
    }

}
