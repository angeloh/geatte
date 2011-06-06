package com.geatte.android.app;

import com.geatte.android.app.R;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

/**
 * "List" of my geattes screen
 * 
 */
public class GeatteListActivity extends ListActivity {

    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_SHOW = 1;

    private static final int MENU_GET_NEXT_PAGE = Menu.FIRST;
    //private static final int MENU_DELETE_GEATTE = Menu.FIRST + 1;

    private static final int NUM_RESULTS_PER_PAGE = 8;

    private int mStartFrom = 1;
    private TextView empty;
    private ProgressDialog progressDialog;
    private GeatteDBAdapter mDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(Config.LOGTAG, "GeatteListActivity:onCreate(): START");

	this.setContentView(R.layout.geatte_list);

	this.empty = (TextView) findViewById(R.id.empty);

	// get start from, an int, from extras
	mStartFrom = getIntent().getIntExtra(Config.EXTRA_MYGEATTE_STARTFROM, 1);

	// no need to fill data here cause resume is called after tab is created
	//fillData();

	// set list properties
	//listView.setItemsCanFocus(false);
	//listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	Log.d(Config.LOGTAG, "GeatteListActivity:onCreate(): END");
    }

    @Override
    protected void onResume() {
	super.onResume();
	Log.d(Config.LOGTAG, "GeatteListActivity:onResume(): START");
	fillData();
	Log.d(Config.LOGTAG, "GeatteListActivity:onResume(): END");
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	Log.d(Config.LOGTAG, "GeatteListActivity:onDestroy(): START");

	Log.d(Config.LOGTAG, "GeatteListActivity:onDestroy(): END");
    }

    private void fillData() {
	mDbHelper = new GeatteDBAdapter(this);
	try {
	    mDbHelper.open();
	    this.progressDialog = ProgressDialog.show(this, " Working...", " Retrieving my geattes", true, false);

	    // Get all of the rows from the database and create the item list
	    Cursor myGeattesCursor = mDbHelper.fetchMyInterestsLimit(NUM_RESULTS_PER_PAGE, mStartFrom);
	    startManagingCursor(myGeattesCursor);

	    // Create an array to specify the fields we want to display in the list
	    String[] from = new String[]{GeatteDBAdapter.KEY_IMAGE_PATH, GeatteDBAdapter.KEY_INTEREST_TITLE, GeatteDBAdapter.KEY_INTEREST_DESC};

	    // and an array of the fields we want to bind those fields to
	    int[] to = new int[]{R.id.my_geatte_img, R.id.my_geatte_title, R.id.my_geatte_desc};

	    // Now create a simple cursor adapter and set it to display
	    ImageCursorAdapter cursorAdapter =
		new ImageCursorAdapter(this, R.layout.geatte_row, myGeattesCursor, from, to);
	    setListAdapter(cursorAdapter);

	    progressDialog.dismiss();

	    // set list properties
	    final ListView listView = getListView();
	    //listView.setItemsCanFocus(false);
	    //listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	    listView.setEmptyView(this.empty);
	    registerForContextMenu(listView);
	} finally {
	    if (mDbHelper != null) {
		mDbHelper.close();
	    }
	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	menu.add(0, GeatteListActivity.MENU_GET_NEXT_PAGE, 0, R.string.menu_get_next_page).setIcon(
		android.R.drawable.ic_menu_more);
	//		menu.add(0, GeatteListActivity.MENU_ADD_GEATTE, 0, R.string.menu_add_more_geatte).setIcon(
	//			android.R.drawable.ic_menu_edit);
	return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
	super.onCreateContextMenu(menu, v, menuInfo);
	//menu.add(0, MENU_DELETE_GEATTE, 0, R.string.menu_delete);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	Intent intent = null;
	switch (item.getItemId()) {
	case MENU_GET_NEXT_PAGE:
	    // increment the startFrom value and call this Activity again
	    intent = new Intent(this, GeatteTabActivity.class);
	    intent.putExtra(Config.EXTRA_MYGEATTE_STARTFROM, mStartFrom + GeatteListActivity.NUM_RESULTS_PER_PAGE);
	    startActivity(intent);
	    return true;
	    //	case MENU_ADD_GEATTE:
	    //	    createGeatte();
	    //	    return true;
	}
	return super.onMenuItemSelected(featureId, item);
    }

    private void createGeatte() {
	Intent i = new Intent(this, GeatteEditActivity.class);
	startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	super.onListItemClick(l, v, position, id);
	Intent intent = new Intent(this, GeatteFeedbackActivity.class);
	intent.putExtra(GeatteDBAdapter.KEY_INTEREST_ID, id);
	intent.putExtra(Config.EXTRA_MYGEATTE_STARTFROM, mStartFrom);
	startActivityForResult(intent, ACTIVITY_SHOW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	super.onActivityResult(requestCode, resultCode, intent);
	fillData();
    }
}
