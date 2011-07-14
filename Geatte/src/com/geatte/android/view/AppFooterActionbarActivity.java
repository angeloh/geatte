package com.geatte.android.view;

import greendroid.app.GDActivity;
import greendroid.util.Config;
import greendroid.widget.ActionBar;
import android.app.ListActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import com.geatte.android.app.R;

/**
 * An equivalent to {@link ListActivity} that manages a ListView.
 * 
 * @see {@link ListActivity}
 */
public abstract class AppFooterActionbarActivity extends GDActivity {

    private static final String LOG_TAG = AppFooterActionbarActivity.class.getSimpleName();

    protected View m_shopinion_footer = null;
    protected Button m_shopinion_mi_btn = null;
    protected Button m_shopinion_fi_btn = null;
    protected Button m_shopinion_snap_btn = null;
    protected Button m_shopinion_pick_btn = null;
    protected Button m_shopinion_all_btn = null;

    public AppFooterActionbarActivity() {
	super();
    }

    public AppFooterActionbarActivity(ActionBar.Type actionBarType) {
	super(actionBarType);
    }

    @Override
    public int createLayout() {
	if (Config.GD_INFO_LOGS_ENABLED) {
	    Log.i(LOG_TAG, "No layout specified : creating the default layout");
	}

	switch (getActionBarType()) {
	case Dashboard:
	case Empty:
	case Normal:
	default:
	    return R.layout.default_app_footer_actionbar;
	}
    }

    @Override
    protected boolean verifyLayout() {
	return super.verifyLayout() && m_shopinion_footer != null;
    }

    @Override
    public void onPreContentChanged() {
	super.onPreContentChanged();
	m_shopinion_footer = findViewById(R.id.shopinion_footer);

	View tmpView = findViewById(R.id.shopinion_mi_btn);
	if (tmpView != null) {
	    m_shopinion_mi_btn = (Button) tmpView;
	}
	tmpView = findViewById(R.id.shopinion_fi_btn);
	if (tmpView != null) {
	    m_shopinion_fi_btn = (Button) tmpView;
	}
	tmpView = findViewById(R.id.shopinion_snap_btn);
	if (tmpView != null) {
	    m_shopinion_snap_btn = (Button) tmpView;
	}
	tmpView = findViewById(R.id.shopinion_pick_btn);
	if (tmpView != null) {
	    m_shopinion_pick_btn = (Button) tmpView;
	}
	tmpView = findViewById(R.id.shopinion_all_btn);
	if (tmpView != null) {
	    m_shopinion_all_btn = (Button) tmpView;
	}
	if (m_shopinion_footer == null) {
	    throw new RuntimeException("Your content must have a LinearLayout whose id attribute is " + "'shopinion_footer'");
	}
    }

    @Override
    public void onPostContentChanged() {
	super.onPostContentChanged();
	if (m_shopinion_mi_btn != null) {
	    m_shopinion_mi_btn.setOnClickListener( new OnClickListener(){
		public void onClick(View v ) {
		    onMIBtnClick(v);
		}
	    });
	}
	if (m_shopinion_fi_btn != null) {
	    m_shopinion_fi_btn.setOnClickListener( new OnClickListener(){
		public void onClick(View v ) {
		    onFIBtnClick(v);
		}
	    });
	}
	if (m_shopinion_snap_btn != null) {
	    m_shopinion_snap_btn.setOnClickListener( new OnClickListener(){
		public void onClick(View v ) {
		    onSnapBtnClick(v);
		}
	    });
	}
	if (m_shopinion_pick_btn != null) {
	    m_shopinion_pick_btn.setOnClickListener( new OnClickListener(){
		public void onClick(View v ) {
		    onPickBtnClick(v);
		}
	    });
	}
	if (m_shopinion_all_btn != null) {
	    m_shopinion_all_btn.setOnClickListener( new OnClickListener(){
		public void onClick(View v ) {
		    onAllBtnClick(v);
		}
	    });
	}
    }

    public void onMIBtnClick(View v ) {
    }

    public void onFIBtnClick(View v ) {
    }

    public void onSnapBtnClick(View v ) {
    }

    public void onPickBtnClick(View v ) {
    }

    public void onAllBtnClick(View v ) {
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
	"The setActionBarContentView method is not supported for GDListActivity. In order to get a custom layout you must return a layout identifier in createLayout");

    }

}
