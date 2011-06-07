package com.geatte.android.app;

import com.geatte.android.app.R;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * "List" of friends geattes screen
 * 
 */
public class GeatteListOthersActivity extends ListActivity {

    private static final int MENU_GET_NEXT_PAGE = Menu.FIRST;
    //private static final int MENU_DELETE_GEATTE = Menu.FIRST + 1;

    private static final int NUM_RESULTS_PER_PAGE = 5;

    private final Handler mHandler = new Handler();
    private int mStartFrom = 1;
    private TextView empty;
    private GeatteDBAdapter mDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(Config.LOGTAG, "GeatteListOthersActivity:onCreate(): START");

	this.setContentView(R.layout.geatte_list);

	this.empty = (TextView) findViewById(R.id.empty);

	// get start from, an int, from extras
	mStartFrom = getIntent().getIntExtra(Config.EXTRA_FRIENDGEATTE_STARTFROM, 1);

	// no need to fill data here cause resume is called after tab is created
	// fillData();
	Log.d(Config.LOGTAG, "GeatteListOthersActivity:onCreate(): END");
    }

    @Override
    protected void onResume() {
	super.onResume();
	Log.d(Config.LOGTAG, "GeatteListOthersActivity:onResume(): START");
	mHandler.postDelayed(new Runnable() {
	    public void run() {
		fillData();
	    }
	},250);
	Log.d(Config.LOGTAG, "GeatteListOthersActivity:onResume(): END");
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	Log.d(Config.LOGTAG, "GeatteListOthersActivity:onDestroy(): START");
	Log.d(Config.LOGTAG, "GeatteListOthersActivity:onDestroy(): END");
    }

    private void fillData() {

	// create db helper
	mDbHelper = new GeatteDBAdapter(this);
	try {
	    mDbHelper.open();

	    // Get all of the rows from the database and create the item list
	    Cursor myGeattesCursor = mDbHelper.fetchFriendInterestsLimit(NUM_RESULTS_PER_PAGE, mStartFrom);
	    startManagingCursor(myGeattesCursor);

	    // Create an array to specify the fields we want to display in the list (only TITLE)
	    String[] from = new String[]{GeatteDBAdapter.KEY_FI_IMAGE_PATH, GeatteDBAdapter.KEY_FRIEND_INTEREST_TITLE, GeatteDBAdapter.KEY_FRIEND_INTEREST_DESC};

	    // and an array of the fields we want to bind those fields to
	    int[] to = new int[]{R.id.fi_geatte_img, R.id.fi_geatte_desc, R.id.fi_geatte_title};

	    // Now create a simple cursor adapter and set it to display
	    SimpleCursorAdapter cursorAdapter =
		new SimpleCursorAdapter(this, R.layout.geatte_row_fi, myGeattesCursor, from, to);
	    setListAdapter(cursorAdapter);

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
	menu.add(0, GeatteListOthersActivity.MENU_GET_NEXT_PAGE, 0, R.string.menu_get_next_page).setIcon(
		android.R.drawable.ic_menu_more);

	return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
	super.onCreateContextMenu(menu, v, menuInfo);
	//	menu.add(0, MENU_DELETE_GEATTE, 0, R.string.menu_delete);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	Intent intent = null;
	switch (item.getItemId()) {
	case MENU_GET_NEXT_PAGE:
	    // increment the startFrom value and call this Activity again
	    intent = new Intent(this, GeatteTabActivity.class);
	    intent.putExtra(Config.EXTRA_FRIENDGEATTE_STARTFROM, mStartFrom + GeatteListOthersActivity.NUM_RESULTS_PER_PAGE);
	    intent.putExtra(Config.EXTRA_CURRENT_TAB, GeatteTabActivity.TABS.FRIENDINTERESTS.getIndex());
	    startActivity(intent);
	    return true;
	}
	return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	super.onListItemClick(l, v, position, id);
	//	Intent intent = new Intent(this, GeatteVotingActivity.class);
	//	intent.putExtra(GeatteDBAdapter.KEY_INTEREST_ID, id);
	//	intent.putExtra(Config.EXTRA_FRIENDGEATTE_STARTFROM, mStartFrom);
	//	startActivityForResult(intent, ACTIVITY_SHOW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	super.onActivityResult(requestCode, resultCode, intent);
	fillData();
    }
}
