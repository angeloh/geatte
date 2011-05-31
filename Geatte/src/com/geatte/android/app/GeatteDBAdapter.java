package com.geatte.android.app;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Custom adapter for Geatte Review model objects.
 * 
 */
public class GeatteDBAdapter {

    //TABLE interests
    public static final String KEY_INTEREST_ID = "_id";
    public static final String KEY_INTEREST_GEATTE_ID = "geatte_id";
    public static final String KEY_INTEREST_TITLE = "title";
    public static final String KEY_INTEREST_DESC = "desc";
    public static final String KEY_INTEREST_CREATED_DATE = "created_date";

    //TABLE feedbacks
    public static final String KEY_FEEDBACK_ID = "_id";
    public static final String KEY_FEEDBACK_GEATTE_ID = "geatte_id";
    public static final String KEY_FEEDBACK_VOTER = "voter";
    public static final String KEY_FEEDBACK_VOTE = "vote";
    public static final String KEY_FEEDBACK_COMMENT = "feedback";
    public static final String KEY_FEEDBACK_UPDATED_DATE = "updated_date";

    //TABLE images
    public static final String KEY_IMAGE_ID = "_id";
    public static final String KEY_IMAGE_AS_ID = "image_id";
    public static final String KEY_IMAGE_INTEREST_ID = "interest";
    public static final String KEY_IMAGE_PATH = "image_path";
    //public static final String KEY_IMAGE_HASH = "hash";

    //TABLE contacts
    public static final String KEY_CONTACT_ID = "_id";
    public static final String KEY_CONTACT_PHONE = "phone";
    public static final String KEY_CONTACT_EMAIL = "email";
    public static final String KEY_CONTACT_LINK = "link";

    //TABLE friend_interests
    public static final String KEY_FRIEND_INTEREST_ID = "_id";
    public static final String KEY_FRIEND_INTEREST_TITLE = "f_title";
    public static final String KEY_FRIEND_INTEREST_DESC = "f_desc";
    public static final String KEY_FRIEND_INTEREST_FROM = "f_from_number";
    public static final String KEY_FRIEND_INTEREST_CREATED_DATE = "f_created_date";

    //TABLE fi_images
    public static final String KEY_FI_IMAGE_ID = "_id";
    public static final String KEY_FI_IMAGE_AS_ID = "fi_image_id";
    public static final String KEY_FI_IMAGE_INTEREST_ID = "fi_interest";
    public static final String KEY_FI_IMAGE_PATH = "fi_image_path";

    private static final String DATABASE_NAME = "geattedb";
    private static final int DATABASE_VERSION = 2;
    private static final int CURSOR_LIMIT = 8;

    private static final String DB_TABLE_INTERESTS = "interests";
    private static final String DB_TABLE_FEEDBACKS = "feedbacks";
    private static final String DB_TABLE_IMAGES = "images";
    private static final String DB_TABLE_FRIEND_INTERESTS = "friend_interests";
    private static final String DB_TABLE_FI_IMAGES = "fi_images";

    /**
     * Database creation sql statement
     */
    private static final String DB_CREATE_INTERESTS =
	"CREATE TABLE " + DB_TABLE_INTERESTS + " (" + KEY_INTEREST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
	KEY_INTEREST_GEATTE_ID +" TEXT," +
	KEY_INTEREST_TITLE +" TEXT NOT NULL," +
	KEY_INTEREST_DESC +" TEXT," +
	KEY_INTEREST_CREATED_DATE + " DATE" +
	");";

    private static final String DB_CREATE_FEEDBACKS =
	"CREATE TABLE " + DB_TABLE_FEEDBACKS + " (" + KEY_FEEDBACK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
	KEY_FEEDBACK_VOTER + " TEXT," +
	KEY_FEEDBACK_VOTE + " TEXT," +
	KEY_FEEDBACK_COMMENT + " TEXT," +
	KEY_FEEDBACK_UPDATED_DATE + " DATE," +
	KEY_FEEDBACK_GEATTE_ID + " TEXT," +
	"FOREIGN KEY (" +KEY_FEEDBACK_GEATTE_ID +") REFERENCES " + DB_TABLE_INTERESTS + " (" + KEY_INTEREST_GEATTE_ID + ")" +
	");";

    private static final String DB_CREATE_IMAGES =
	"CREATE TABLE " + DB_TABLE_IMAGES + " (" + KEY_IMAGE_ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
	KEY_IMAGE_PATH +" TEXT NOT NULL," +
	//KEY_IMAGE_HASH + " BLOB," +//TODO UNIQUE
	KEY_IMAGE_INTEREST_ID + " INTEGER," +
	"FOREIGN KEY (" + KEY_IMAGE_INTEREST_ID + ") REFERENCES " + DB_TABLE_INTERESTS + " (" + KEY_INTEREST_ID + ")" +
	");";

    private static final String DB_CREATE_FRIEND_INTERESTS =
	"CREATE TABLE " + DB_TABLE_FRIEND_INTERESTS + " (" + KEY_FRIEND_INTEREST_ID +" TEXT PRIMARY KEY," +
	KEY_FRIEND_INTEREST_TITLE +" TEXT NOT NULL," +
	KEY_FRIEND_INTEREST_DESC +" TEXT," +
	KEY_FRIEND_INTEREST_FROM +" TEXT NOT NULL," +
	KEY_FRIEND_INTEREST_CREATED_DATE +" TEXT" +
	");";

    private static final String DB_CREATE_FI_IMAGES =
	"CREATE TABLE " + DB_TABLE_FI_IMAGES + " (" + KEY_FI_IMAGE_ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
	KEY_FI_IMAGE_PATH +" TEXT NOT NULL," +
	//KEY_IMAGE_HASH + " BLOB," +//TODO UNIQUE
	KEY_FI_IMAGE_INTEREST_ID + " TEXT," +
	"FOREIGN KEY (" + KEY_FI_IMAGE_INTEREST_ID + ") REFERENCES " + DB_TABLE_FRIEND_INTERESTS + " (" + KEY_FRIEND_INTEREST_ID + ")" +
	");";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

	DatabaseHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	    db.execSQL(DB_CREATE_INTERESTS);
	    db.execSQL(DB_CREATE_FEEDBACKS);
	    db.execSQL(DB_CREATE_IMAGES);
	    db.execSQL(DB_CREATE_FRIEND_INTERESTS);
	    db.execSQL(DB_CREATE_FI_IMAGES);
	}

	/*	private void processDelete(long rowId) {
	    String[] args = { String.valueOf(rowId) };
	    SQLiteDatabase myDB = null;
	    myDB = this.openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
	    myDB.delete("tracks", "_ID=?", args);
	    myDB.delete("waypoints", "trackidfk=?", args);
	    cur.requery();
	}*/

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    db.execSQL("DROP TABLE IF EXISTS "+DB_TABLE_INTERESTS);
	    db.execSQL("DROP TABLE IF EXISTS "+DB_TABLE_FEEDBACKS);
	    db.execSQL("DROP TABLE IF EXISTS "+DB_TABLE_IMAGES);
	    db.execSQL("DROP TABLE IF EXISTS "+DB_CREATE_FRIEND_INTERESTS);
	    db.execSQL("DROP TABLE IF EXISTS "+DB_CREATE_FI_IMAGES);

	    onCreate(db);
	}

    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public GeatteDBAdapter(Context ctx) {
	this.mCtx = ctx;
    }

    /**
     * Open the interests database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public GeatteDBAdapter open() throws SQLException {
	mDbHelper = new DatabaseHelper(mCtx);
	mDb = mDbHelper.getWritableDatabase();
	return this;
    }

    public void close() {
	mDbHelper.close();
    }


    /**
     * Create a new interest using the title and desc provided. If the interest is
     * successfully created return the new rowId for that interest, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the interest
     * @param desc the desc of the interest
     * @return rowId or -1 if failed
     */
    public long insertInterest(String title, String desc) {
	ContentValues initialValues = new ContentValues();
	initialValues.put(KEY_INTEREST_TITLE, title);
	initialValues.put(KEY_INTEREST_DESC, desc);
	// set the format to sql date time
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	initialValues.put(KEY_INTEREST_CREATED_DATE, dateFormat.format(new Date()));

	return mDb.insert(DB_TABLE_INTERESTS, null, initialValues);
    }

    public long insertFeedback(String geatteId, String voter, String vote, String comment) {
	ContentValues initialValues = new ContentValues();
	initialValues.put(KEY_FEEDBACK_GEATTE_ID, geatteId);
	initialValues.put(KEY_FEEDBACK_VOTER, voter);
	initialValues.put(KEY_FEEDBACK_VOTE, vote);
	initialValues.put(KEY_FEEDBACK_COMMENT, comment);
	// set the format to sql date time
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	initialValues.put(KEY_FEEDBACK_UPDATED_DATE, dateFormat.format(new Date()));

	return mDb.insert(DB_TABLE_FEEDBACKS, null, initialValues);
    }

    public long insertImage(long interestId, String imagePath) {
	ContentValues initialValues = new ContentValues();
	initialValues.put(KEY_IMAGE_INTEREST_ID, interestId);
	if (imagePath != null) {
	    initialValues.put(KEY_IMAGE_PATH, imagePath);
	    //initialValues.put(KEY_IMAGE_HASH, getHashFromByteArray(byteArray));

	    return mDb.insert(DB_TABLE_IMAGES, null, initialValues);
	} else {
	    return -1;
	}
    }

    /**
     * Insert friend's interest to db
     * 
     * @param geatteId geatte id sent from server
     * @param title geatte title
     * @param desc geatte desc
     * @param fromNumber geatte sent from
     * @param imagePath image path
     * @param createdDate created date
     * @return the row ID of the newly inserted row
     */
    public long insertFriendInterest(String geatteId, String title, String desc, String fromNumber, String createdDate) {
	if (geatteId == null || fromNumber == null) {
	    return -1;
	}
	if (title == null) {
	    title = "New Geatte";
	}
	ContentValues initialValues = new ContentValues();
	initialValues.put(KEY_FRIEND_INTEREST_ID, geatteId);
	initialValues.put(KEY_FRIEND_INTEREST_TITLE, title);
	initialValues.put(KEY_FRIEND_INTEREST_DESC, desc);
	initialValues.put(KEY_FRIEND_INTEREST_FROM, fromNumber);
	initialValues.put(KEY_FRIEND_INTEREST_CREATED_DATE, createdDate);

	return mDb.insert(DB_TABLE_FRIEND_INTERESTS, null, initialValues);
    }

    public long insertFIImage(String geatteId, String imagePath) {
	if (geatteId == null || imagePath == null) {
	    return -1;
	}
	ContentValues initialValues = new ContentValues();
	initialValues.put(KEY_FI_IMAGE_INTEREST_ID, geatteId);
	initialValues.put(KEY_FI_IMAGE_PATH, imagePath);

	return mDb.insert(DB_TABLE_FI_IMAGES, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteInterest(long rowId) {
	String geatteId = getGeatteIdFromInterestId((int)rowId);
	if (geatteId != null) {
	    mDb.delete(DB_TABLE_FEEDBACKS, KEY_FEEDBACK_GEATTE_ID + "=" + geatteId, null);
	}
	mDb.delete(DB_TABLE_IMAGES, KEY_IMAGE_INTEREST_ID + "=" + rowId, null);
	return mDb.delete(DB_TABLE_INTERESTS, KEY_INTEREST_ID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of my interests in the database given limit and offset
     * 
     * @param limit query limit
     * @param startFrom query startFrom, 1 to n
     * @return Cursor over all notes
     */
    public Cursor fetchMyInterestsLimit(int limit, int startFrom) {
	int offset = startFrom - 1;
	String query = "SELECT " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_TITLE + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_DESC + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_CREATED_DATE + ", " +
	DB_TABLE_IMAGES + "." + KEY_IMAGE_ID + " AS " + KEY_IMAGE_AS_ID + ", " +
	DB_TABLE_IMAGES + "." + KEY_IMAGE_PATH + " " +
	" FROM " +
	DB_TABLE_INTERESTS + " JOIN " + DB_TABLE_IMAGES + " ON " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + "=" +
	DB_TABLE_IMAGES + "." + KEY_IMAGE_INTEREST_ID +
	" ORDER BY " + DB_TABLE_INTERESTS + "." + KEY_INTEREST_CREATED_DATE + " DESC" +
	" LIMIT " + limit +
	" OFFSET " + offset;

	/*	String query = "SELECT " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_TITLE + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_DESC +
	" FROM " +
	DB_TABLE_INTERESTS;*/

	Log.i(Config.LOGTAG, "fetch my interests query string = " + query);

	Cursor cursor = mDb.rawQuery(query, null);
	return cursor;
    }

    /**
     * Return a Cursor positioned at the interest that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching interest, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchMyInterest(long rowId) throws SQLException {
	String query = "SELECT " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_TITLE + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_DESC + ", " +
	DB_TABLE_IMAGES + "." + KEY_IMAGE_ID + " AS " + KEY_IMAGE_AS_ID + ", " +
	DB_TABLE_IMAGES + "." + KEY_IMAGE_PATH + " " +
	"FROM " +
	DB_TABLE_INTERESTS + " JOIN " + DB_TABLE_IMAGES + " ON " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + "=" +
	DB_TABLE_IMAGES + "." + KEY_IMAGE_INTEREST_ID +
	" WHERE " + DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + "=" + rowId;

	/*	String query = "SELECT " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_TITLE + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_DESC + " " +
	"FROM " +
	DB_TABLE_INTERESTS +
	" WHERE " + DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + "=\"" + rowId + "\"";*/

	Log.i(Config.LOGTAG, "fetch interest query string = " + query);

	Cursor cursor = mDb.rawQuery(query, null);

	if (cursor != null) {
	    cursor.moveToFirst();
	}
	return cursor;
    }

    /**
     * Return a Cursor positioned at the interest that matches the given geatteId
     * 
     * @param geatteId geatteId
     * @return Cursor positioned to matching interest, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchMyInterest(String geatteId) throws SQLException {
	long interestId = this.getInterestIdFromGeatteId(geatteId);
	return fetchMyInterest(interestId);
    }

    /**
     * Return a Cursor positioned at the feedbacks that matches the given geatteId
     * 
     * @param geatteId geatteId
     * @return Cursor positioned to matching interest, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchMyInterestFeedback(String geatteId) throws SQLException {
	String query = "SELECT " +
	DB_TABLE_FEEDBACKS + "." + KEY_FEEDBACK_GEATTE_ID + ", " +
	DB_TABLE_FEEDBACKS + "." + KEY_FEEDBACK_VOTER + ", " +
	DB_TABLE_FEEDBACKS + "." + KEY_FEEDBACK_VOTE + ", " +
	DB_TABLE_FEEDBACKS + "." + KEY_FEEDBACK_COMMENT + ", " +
	DB_TABLE_FEEDBACKS + "." + KEY_FEEDBACK_UPDATED_DATE + " " +
	"FROM " +
	DB_TABLE_FEEDBACKS +
	" WHERE " + DB_TABLE_FEEDBACKS + "." + KEY_FEEDBACK_GEATTE_ID + "=\"" + geatteId + "\"";

	Log.i(Config.LOGTAG, "fetch feedbacks query string = " + query);

	Cursor cursor = mDb.rawQuery(query, null);
	return cursor;
    }

    /**
     * Return a Cursor for all feedbacks
     * 
     * @return Cursor positioned to matching interest, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchAllMyInterestFeedback() throws SQLException {
	String query = "SELECT " +
	DB_TABLE_FEEDBACKS + "." + KEY_FEEDBACK_GEATTE_ID + ", " +
	DB_TABLE_FEEDBACKS + "." + KEY_FEEDBACK_VOTER + ", " +
	DB_TABLE_FEEDBACKS + "." + KEY_FEEDBACK_VOTE + ", " +
	DB_TABLE_FEEDBACKS + "." + KEY_FEEDBACK_COMMENT + ", " +
	DB_TABLE_FEEDBACKS + "." + KEY_FEEDBACK_UPDATED_DATE + " " +
	"FROM " + DB_TABLE_FEEDBACKS +
	" ORDER BY " + DB_TABLE_FEEDBACKS + "." + KEY_FEEDBACK_UPDATED_DATE + " DESC";

	Log.i(Config.LOGTAG, "fetch all feedbacks query string = " + query);

	try {
	    Cursor cursor = mDb.rawQuery(query, null);
	    Log.i(Config.LOGTAG, "return cursor fetchAllMyInterestFeedback()");
	    return cursor;
	} catch (Exception ex) {
	    Log.e(Config.LOGTAG, "ERROR to fetch all feedbacks ", ex);
	}
	return null;
    }

    /**
     * Return a Cursor over the list of friends' interests in the database given limit and offset
     * 
     * @param limit query limit
     * @param startFrom query startFrom, 1 to n
     * @return Cursor over friends' interests
     */
    public Cursor fetchFriendInterestsLimit(int limit, int startFrom) throws SQLException {
	int offset = startFrom - 1;
	String query = "SELECT " +
	DB_TABLE_FRIEND_INTERESTS + "." + KEY_FRIEND_INTEREST_ID + ", " +
	DB_TABLE_FRIEND_INTERESTS + "." + KEY_FRIEND_INTEREST_TITLE + ", " +
	DB_TABLE_FRIEND_INTERESTS + "." + KEY_FRIEND_INTEREST_DESC + ", " +
	DB_TABLE_FRIEND_INTERESTS + "." + KEY_FRIEND_INTEREST_FROM + ", " +
	DB_TABLE_FRIEND_INTERESTS + "." + KEY_FRIEND_INTEREST_CREATED_DATE + ", " +
	DB_TABLE_FI_IMAGES + "." + KEY_FI_IMAGE_ID + " AS " + KEY_FI_IMAGE_AS_ID + ", " +
	DB_TABLE_FI_IMAGES + "." + KEY_FI_IMAGE_PATH + " " +
	"FROM " +
	DB_TABLE_FRIEND_INTERESTS + " JOIN " + DB_TABLE_FI_IMAGES + " ON " +
	DB_TABLE_FRIEND_INTERESTS + "." + KEY_FRIEND_INTEREST_ID + "=" +
	DB_TABLE_FI_IMAGES + "." + KEY_FI_IMAGE_INTEREST_ID +
	" ORDER BY " + DB_TABLE_FRIEND_INTERESTS + "." + KEY_FRIEND_INTEREST_CREATED_DATE + " DESC" +
	" LIMIT " + limit +
	" OFFSET " + offset;

	Log.i(Config.LOGTAG, "fetch friend's interest query string = " + query);

	Cursor cursor = mDb.rawQuery(query, null);

	return cursor;
    }

    public Cursor fetchFriendInterest(String geatteId) throws SQLException {
	String query = "SELECT " +
	DB_TABLE_FRIEND_INTERESTS + "." + KEY_FRIEND_INTEREST_ID + ", " +
	DB_TABLE_FRIEND_INTERESTS + "." + KEY_FRIEND_INTEREST_TITLE + ", " +
	DB_TABLE_FRIEND_INTERESTS + "." + KEY_FRIEND_INTEREST_DESC + ", " +
	DB_TABLE_FRIEND_INTERESTS + "." + KEY_FRIEND_INTEREST_FROM + ", " +
	DB_TABLE_FRIEND_INTERESTS + "." + KEY_FRIEND_INTEREST_CREATED_DATE + ", " +
	DB_TABLE_FI_IMAGES + "." + KEY_FI_IMAGE_ID + " AS " + KEY_FI_IMAGE_AS_ID + ", " +
	DB_TABLE_FI_IMAGES + "." + KEY_FI_IMAGE_PATH + " " +
	"FROM " +
	DB_TABLE_FRIEND_INTERESTS + " JOIN " + DB_TABLE_FI_IMAGES + " ON " +
	DB_TABLE_FRIEND_INTERESTS + "." + KEY_FRIEND_INTEREST_ID + "=" +
	DB_TABLE_FI_IMAGES + "." + KEY_FI_IMAGE_INTEREST_ID +
	" WHERE " + DB_TABLE_FRIEND_INTERESTS + "." + KEY_FRIEND_INTEREST_ID + "=\"" + geatteId + "\"";

	/*	String query = "SELECT " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_TITLE + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_DESC + " " +
	"FROM " +
	DB_TABLE_INTERESTS +
	" WHERE " + DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + "=\"" + rowId + "\"";*/

	Log.i(Config.LOGTAG, "fetch friend's interest query string = " + query);

	Cursor cursor = mDb.rawQuery(query, null);

	if (cursor != null) {
	    cursor.moveToFirst();
	}
	return cursor;
    }

    /**
     * Update the interest using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of interest to update
     * @param title value to set interest title to
     * @param desc value to set interest desc to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateInterest(long rowId, String title, String desc) {
	ContentValues args = new ContentValues();
	args.put(KEY_INTEREST_TITLE, title);
	args.put(KEY_INTEREST_DESC, desc);
	// set the format to sql date time
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	args.put(KEY_INTEREST_CREATED_DATE, dateFormat.format(new Date()));

	return mDb.update(DB_TABLE_INTERESTS, args, KEY_INTEREST_ID + "=" + rowId, null) > 0;
    }

    public boolean updateInterestGeatteId(long rowId, String geatteId) {
	ContentValues initialValues = new ContentValues();
	initialValues.put(KEY_INTEREST_GEATTE_ID, geatteId);

	return mDb.update(DB_TABLE_INTERESTS, initialValues, KEY_INTEREST_ID + "=" + rowId, null) > 0;
    }

    public boolean updateImage(long imageId, String imagePath) {
	ContentValues args = new ContentValues();
	if (imagePath != null) {
	    args.put(KEY_IMAGE_PATH, imagePath);
	    //args.put(KEY_IMAGE_HASH, getHashFromByteArray(byteArray));

	    return mDb.update(DB_TABLE_IMAGES, args, KEY_IMAGE_ID + "=" + imageId, null) > 0;
	} else {
	    return false;
	}
    }

    public String getGeatteIdFromInterestId(long interestId){
	Cursor cursor = mDb.query(DB_TABLE_INTERESTS, new String []{KEY_INTEREST_GEATTE_ID}, KEY_INTEREST_ID + "=" + interestId, null, null, null, null);

	String geatteId = null;
	if (cursor != null) {
	    if(cursor.moveToFirst()) {
		geatteId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_INTEREST_GEATTE_ID));
	    }
	}

	if (geatteId == null) {
	    Log.w(Config.LOGTAG, "unable to get geatte id for interest id = " + interestId + ", return null!!");
	}

	return geatteId;

    }

    public long getInterestIdFromGeatteId(String geatteId){
	if (geatteId == null) {
	    return -1;
	}
	Cursor cursor = mDb.query(DB_TABLE_INTERESTS, new String []{KEY_INTEREST_ID}, KEY_INTEREST_GEATTE_ID + "='" + geatteId +"'", null, null, null, null);

	long interestId = -1;
	if (cursor != null) {
	    if (cursor.moveToFirst()) {
		interestId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_INTEREST_ID));
	    }
	}

	if (interestId == -1) {
	    Log.w(Config.LOGTAG, "unable to get interest id for geatte id = " + geatteId + ", return -1!!");
	}
	return interestId;
    }

    /*private byte[] getBitmapAsByteArray(Bitmap bitmap) {
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	// Middle parameter is quality, but since PNG is lossless, it doesn't matter
	bitmap.compress(CompressFormat.PNG, 0, outputStream);
	return outputStream.toByteArray();
    }

    private byte[] getHashFromByteArray(byte[] byteArray) {
	MessageDigest md;
	try {
	    md = MessageDigest.getInstance("SHA-1");
	    md.update(byteArray); // It's the same bitmap data that you got from getBitmapAsByteArray
	    byte[] digest = md.digest();
	    return digest;
	} catch (NoSuchAlgorithmException e) {
	    Log.w(TAG, "exception in getHashFromByteArray", e);
	}
	return null;

    }*/

}
