package com.geatte.mobile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Custom adapter for Geatte Review model objects.
 * 
 */
public class GeatteDBAdapter {

    //TABLE interests
    public static final String KEY_INTEREST_ID = "_id";
    public static final String KEY_INTEREST_TITLE = "title";
    public static final String KEY_INTEREST_DESC = "desc";

    //TABLE feedbacks
    public static final String KEY_FEEDBACK_ID = "_id";
    public static final String KEY_FEEDBACK_INTEREST_ID = "interest";
    public static final String KEY_FEEDBACK_VOTE = "vote";
    public static final String KEY_FEEDBACK_COMMENT = "feedback";

    //TABLE images
    public static final String KEY_IMAGE_ID = "_id";
    public static final String KEY_IMAGE_AS_ID = "image_id";
    public static final String KEY_IMAGE_INTEREST_ID = "interest";
    public static final String KEY_IMAGE_IMAGE = "image";
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

    private static final String TAG = "GeatteReviewAdapter";
    private static final String DATABASE_NAME = "geattedb";
    private static final int DATABASE_VERSION = 2;

    private static final String DB_TABLE_INTERESTS = "interests";
    private static final String DB_TABLE_FEEDBACKS = "feedbacks";
    private static final String DB_TABLE_IMAGES = "images";
    /**
     * Database creation sql statement
     */
    private static final String DB_CREATE_INTERESTS =
	"CREATE TABLE " + DB_TABLE_INTERESTS + " (" + KEY_INTEREST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
	KEY_INTEREST_TITLE +" TEXT NOT NULL," +
	KEY_INTEREST_DESC +" TEXT" +
	");";

    private static final String DB_CREATE_FEEDBACKS =
	"CREATE TABLE " + DB_TABLE_FEEDBACKS + " (" + KEY_FEEDBACK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
	KEY_FEEDBACK_VOTE + " TEXT," +
	KEY_FEEDBACK_COMMENT + " TEXT," +
	KEY_FEEDBACK_INTEREST_ID + " INTEGER," +
	"FOREIGN KEY (" +KEY_FEEDBACK_INTEREST_ID +") REFERENCES interests (" + KEY_INTEREST_ID + ")" +
	");";

    private static final String DB_CREATE_IMAGES =
	"CREATE TABLE " + DB_TABLE_IMAGES + " (" + KEY_IMAGE_ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
	KEY_IMAGE_IMAGE + " BLOB," +
	//KEY_IMAGE_HASH + " BLOB," +//TODO UNIQUE
	KEY_IMAGE_INTEREST_ID + " INTEGER," +
	"FOREIGN KEY (" + KEY_IMAGE_INTEREST_ID + ") REFERENCES interests (" + KEY_INTEREST_ID + ")" +
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

	return mDb.insert(DB_TABLE_INTERESTS, null, initialValues);
    }

    public long insertFeedback(long interestId, String vote, String comment) {
	ContentValues initialValues = new ContentValues();
	initialValues.put(KEY_FEEDBACK_INTEREST_ID, interestId);
	initialValues.put(KEY_FEEDBACK_VOTE, vote);
	initialValues.put(KEY_FEEDBACK_COMMENT, comment);

	return mDb.insert(DB_TABLE_FEEDBACKS, null, initialValues);
    }

    public long insertImage(long interestId, Bitmap bitmap) {
	ContentValues initialValues = new ContentValues();
	initialValues.put(KEY_IMAGE_INTEREST_ID, interestId);
	if (bitmap != null) {
	    byte[] byteArray = getBitmapAsByteArray(bitmap);
	    initialValues.put(KEY_IMAGE_IMAGE, byteArray);
	    //initialValues.put(KEY_IMAGE_HASH, getHashFromByteArray(byteArray));

	    return mDb.insert(DB_TABLE_IMAGES, null, initialValues);
	} else {
	    return -1;
	}
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {
	mDb.delete(DB_TABLE_FEEDBACKS, KEY_FEEDBACK_INTEREST_ID + "=" + rowId, null);
	mDb.delete(DB_TABLE_IMAGES, KEY_IMAGE_INTEREST_ID + "=" + rowId, null);
	return mDb.delete(DB_TABLE_INTERESTS, KEY_INTEREST_ID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotes() {
	String query = "SELECT " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_TITLE + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_DESC + ", " +
	DB_TABLE_IMAGES + "." + KEY_IMAGE_ID + " AS " + KEY_IMAGE_AS_ID + ", " +
	DB_TABLE_IMAGES + "." + KEY_IMAGE_IMAGE + " " +
	" FROM " +
	DB_TABLE_INTERESTS + " JOIN " + DB_TABLE_IMAGES + " ON " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + "=" +
	DB_TABLE_IMAGES + "." + KEY_IMAGE_INTEREST_ID;

	/*	String query = "SELECT " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_TITLE + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_DESC +
	" FROM " +
	DB_TABLE_INTERESTS;*/

	Log.i(TAG, "query string = " + query);

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
    public Cursor fetchNote(long rowId) throws SQLException {
	String query = "SELECT " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_TITLE + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_DESC + ", " +
	DB_TABLE_IMAGES + "." + KEY_IMAGE_ID + " AS " + KEY_IMAGE_AS_ID + ", " +
	DB_TABLE_IMAGES + "." + KEY_IMAGE_IMAGE + " " +
	"FROM " +
	DB_TABLE_INTERESTS + " JOIN " + DB_TABLE_IMAGES + " ON " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + "=" +
	DB_TABLE_IMAGES + "." + KEY_IMAGE_INTEREST_ID +
	" WHERE " + DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + "=\"" + rowId + "\"";

	/*	String query = "SELECT " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_TITLE + ", " +
	DB_TABLE_INTERESTS + "." + KEY_INTEREST_DESC + " " +
	"FROM " +
	DB_TABLE_INTERESTS +
	" WHERE " + DB_TABLE_INTERESTS + "." + KEY_INTEREST_ID + "=\"" + rowId + "\"";*/

	Log.i(TAG, "query string = " + query);

	Cursor mCursor = mDb.rawQuery(query, null);

	if (mCursor != null) {
	    mCursor.moveToFirst();
	}
	return mCursor;
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

	return mDb.update(DB_TABLE_INTERESTS, args, KEY_INTEREST_ID + "=" + rowId, null) > 0;
    }

    public boolean updateImage(long imageId, Bitmap bitmap) {
	ContentValues args = new ContentValues();
	if (bitmap != null) {
	    byte[] byteArray = getBitmapAsByteArray(bitmap);
	    args.put(KEY_IMAGE_IMAGE, byteArray);
	    //args.put(KEY_IMAGE_HASH, getHashFromByteArray(byteArray));

	    return mDb.update(DB_TABLE_IMAGES, args, KEY_IMAGE_ID + "=" + imageId, null) > 0;
	} else {
	    return false;
	}
    }

    private byte[] getBitmapAsByteArray(Bitmap bitmap) {
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

    }

}
