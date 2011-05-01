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
import android.widget.SimpleCursorAdapter;

/**
 * "List" of friends geattes screen
 * 
 */
public class GeatteListOthers extends ListActivity {

    private static final String CLASSTAG = GeatteListOthers.class.getSimpleName();
    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_SHOW = 1;

    private static final int MENU_ADD_GEATTE = Menu.FIRST;
    private static final int MENU_DELETE_GEATTE = Menu.FIRST + 1;

    //private static final int MENU_GET_NEXT_PAGE = Menu.FIRST;
    //private static final int NUM_RESULTS_PER_PAGE = 8;

    //private TextView empty;
    private ProgressDialog progressDialog;
    private GeatteDBAdapter dbHelper;

    /*    private final Handler handler = new Handler() {
	@Override
	public void handleMessage(final Message msg) {
	    Log.v(Constants.LOGTAG, " " + ReviewList.CLASSTAG + " worker thread done, setup ReviewAdapter");
	    progressDialog.dismiss();
	    if ((reviews == null) || (reviews.size() == 0)) {
		empty.setText("No Data");
	    } else {
		reviewAdapter = new ReviewAdapter(ReviewList.this, reviews);
		setListAdapter(reviewAdapter);
	    }
	}
    };*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.v(Config.LOGTAG, " " + GeatteListOthers.CLASSTAG + " onCreate");

	this.setContentView(R.layout.geatte_list);

	//this.empty = (TextView) findViewById(R.id.empty);

	// create db helper
	dbHelper = new GeatteDBAdapter(this);
	dbHelper.open();
	fillData();

	// set list properties
	final ListView listView = getListView();
	//listView.setItemsCanFocus(false);
	//listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	//listView.setEmptyView(this.empty);
	registerForContextMenu(listView);
    }

    private void fillData() {

	this.progressDialog = ProgressDialog.show(this, " Working...", " Retrieving my geattes", true, false);

	// Get all of the rows from the database and create the item list
	Cursor myGeattesCursor = dbHelper.fetchAllNotes();
	startManagingCursor(myGeattesCursor);

	// Create an array to specify the fields we want to display in the list (only TITLE)
	//String[] from = new String[]{GeatteDBAdapter.KEY_INTEREST_TITLE, GeatteDBAdapter.KEY_IMAGE_IMAGE};
	String[] from = new String[]{GeatteDBAdapter.KEY_INTEREST_TITLE};

	// and an array of the fields we want to bind those fields to
	//int[] to = new int[]{R.id.my_geatte_title, R.id.my_geatte_img};
	int[] to = new int[]{R.id.my_geatte_title};

	// Now create a simple cursor adapter and set it to display
	SimpleCursorAdapter cursorAdapter =
	    new SimpleCursorAdapter(this, R.layout.geatte_row, myGeattesCursor, from, to);
	setListAdapter(cursorAdapter);

	progressDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	//menu.add(0, GeatteList.MENU_GET_NEXT_PAGE, 0, R.string.menu_get_next_page).setIcon(
	//	android.R.drawable.ic_menu_more);
	menu.add(0, GeatteListOthers.MENU_ADD_GEATTE, 0, R.string.menu_add_more_geatte).setIcon(
		android.R.drawable.ic_menu_edit);
	return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
	super.onCreateContextMenu(menu, v, menuInfo);
	menu.add(0, MENU_DELETE_GEATTE, 0, R.string.menu_delete);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	//Intent intent = null;
	switch (item.getItemId()) {
	/*case MENU_GET_NEXT_PAGE:
	    // increment the startFrom value and call this Activity again
	    intent = new Intent(Constants.INTENT_ACTION_VIEW_LIST);
	    intent.putExtra(Constants.STARTFROM_EXTRA, getIntent().getIntExtra(Constants.STARTFROM_EXTRA, 1)
		    + ReviewList.NUM_RESULTS_PER_PAGE);
	    startActivity(intent);
	    return true;*/
	case MENU_ADD_GEATTE:
	    createGeatte();
	    return true;
	}
	return super.onMenuItemSelected(featureId, item);
    }

    private void createGeatte() {
	Intent i = new Intent(this, GeatteEdit.class);
	startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	super.onListItemClick(l, v, position, id);
	Intent i = new Intent(this, GeatteDetail.class);
	i.putExtra(GeatteDBAdapter.KEY_INTEREST_ID, id);
	startActivityForResult(i, ACTIVITY_SHOW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	super.onActivityResult(requestCode, resultCode, intent);
	fillData();
    }
}
