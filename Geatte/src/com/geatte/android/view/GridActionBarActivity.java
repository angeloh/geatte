package com.geatte.android.view;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBar;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.cyrilmottier.android.greendroid.R;

/**
 * Manages a GridView.
 * 
 */
public class GridActionBarActivity extends GDActivity {

    private ListAdapter mAdapter;
    private GridView mGridView;
    private View mEmptyView;

    private Handler mHandler = new Handler();
    private boolean mFinishedStart = false;

    private Runnable mRequestFocus = new Runnable() {
	public void run() {
	    mGridView.focusableViewAvailable(mGridView);
	}
    };

    public GridActionBarActivity() {
	super();
    }

    public GridActionBarActivity(ActionBar.Type actionBarType) {
	super(actionBarType);
    }

    /**
     * This method will be called when an item in the grid is selected.
     * Subclasses should override. Subclasses can call
     * getGridView().getItemAtPosition(position) if they need to access the data
     * associated with the selected item.
     * 
     * @param g The GridView where the click happened
     * @param v The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id The row id of the item that was clicked
     */
    protected void onGridItemClick(GridView g, View v, int position, long id) {
    }

    /**
     * Set the currently selected grid item to the specified position with the
     * adapter's data
     * 
     * @param position The position to select in the managed {@link GridView}
     */
    public void setSelection(int position) {
	mGridView.setSelection(position);
    }

    /**
     * Get the position of the currently selected grid item.
     * 
     * @return The position of the currently selected {@link GridView} item.
     */
    public int getSelectedItemPosition() {
	return mGridView.getSelectedItemPosition();
    }

    /**
     * Get the {@link ListAdapter} item ID of the currently selected grid item.
     * 
     * @return The identifier of the selected {@link GridView} item.
     */
    public long getSelectedItemId() {
	return mGridView.getSelectedItemId();
    }

    /**
     * Get the activity's {@link GridView} widget.
     * 
     * @return The {@link GridView} managed by the current
     *         {@link GridActionBarActivity}
     */
    public GridView getListView() {
	ensureLayout();
	return mGridView;
    }

    /**
     * Get the {@link ListAdapter} associated with this activity's
     * {@link GridView}.
     * 
     * @return The {@link ListAdapter} currently associated to the underlying
     *         {@link GridView}
     */
    public ListAdapter getAdapter() {
	return mAdapter;
    }

    /**
     * Provides the ListAdapter for the GridView handled by this
     * {@link GridActionBarActivity}
     * 
     * @param adapter The {@link ListAdapter} to set.
     */
    public void setListAdapter(ListAdapter adapter) {
	synchronized (this) {
	    ensureLayout();
	    mAdapter = adapter;
	    mGridView.setAdapter(adapter);
	    if (mGridView.getEmptyView() == null && mEmptyView != null) {
		mGridView.setEmptyView(mEmptyView);
	    }
	}
    }

    @Override
    public int createLayout() {
	return R.layout.geatte_grid_content_empty;
    }

    @Override
    protected boolean verifyLayout() {
	return super.verifyLayout() && mGridView != null;
    }

    @Override
    public void onPreContentChanged() {
	super.onPreContentChanged();

	mEmptyView = findViewById(android.R.id.empty);
	mGridView = (GridView) findViewById(R.id.gridview);
	if (mGridView == null) {
	    throw new RuntimeException("Your content must have a GridView whose id attribute is " + "'gridview'");
	}
    }

    @Override
    public void onPostContentChanged() {
	super.onPostContentChanged();

	if (mFinishedStart) {
	    setListAdapter(mAdapter);
	}

	mGridView.setOnItemClickListener(mOnItemClickListener);
	mHandler.post(mRequestFocus);
	mFinishedStart = true;
    }

    @Override
    public void setActionBarContentView(int resID) {
	throwSetActionBarContentViewException();
    }

    @Override
    public void setActionBarContentView(View view, LayoutParams params) {
	throwSetActionBarContentViewException();
    }

    @Override
    public void setActionBarContentView(View view) {
	throwSetActionBarContentViewException();
    }

    private void throwSetActionBarContentViewException() {
	throw new UnsupportedOperationException(
	"The setActionBarContentView method is not supported for GridActionBarActivity. In order to get a custom layout you must return a layout identifier in createLayout");

    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	    onGridItemClick((GridView) parent, v, position, id);
	}
    };

}
