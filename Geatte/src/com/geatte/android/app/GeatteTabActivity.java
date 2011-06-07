package com.geatte.android.app;

import greendroid.app.GDTabActivity;

import com.geatte.android.app.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class GeatteTabActivity extends GDTabActivity {

    private int mMyInterstStartFrom = 1;
    private int mFriendInterstStartFrom = 1;
    private int mCurrentTab = 0;
    private TabHost mTabHost;
    private final Handler mHandler = new Handler();
    private ProgressDialog mDialog;

    public static enum TABS {
	MYINTERESTS(0),
	FRIENDINTERESTS(1);
	private final int index;   // in kilograms
	TABS(int index) {
	    this.index = index;
	}
	public int getIndex() {
	    return index;
	}
    };
    // @Override
    //    public void onCreate(Bundle savedInstanceState) {
    //	super.onCreate(savedInstanceState);
    //	setContentView(R.layout.geatte_tab);
    //
    //	Resources res = getResources(); // Resource object to get Drawables
    //	TabHost tabHost = getTabHost();  // The activity TabHost
    //	TabHost.TabSpec spec;  // Resusable TabSpec for each tab
    //	Intent intent;  // Reusable Intent for each tab
    //
    //	mMyInterstStartFrom = getIntent().getIntExtra(Config.EXTRA_MYGEATTE_STARTFROM, 1);
    //	mFriendInterstStartFrom = getIntent().getIntExtra(Config.EXTRA_MYGEATTE_STARTFROM, 1);
    //	mCurrentTab = getIntent().getIntExtra(Config.EXTRA_CURRENT_TAB, 0);
    //
    //	// Do the same for the other tabs
    //	intent = new Intent().setClass(this, GeatteListActivity.class);
    //	intent.putExtra(Config.EXTRA_MYGEATTE_STARTFROM, mMyInterstStartFrom);
    //	spec = tabHost.newTabSpec("GeatteList").setIndicator("My Interests")
    //	.setContent(intent);
    //	tabHost.addTab(spec);
    //
    //	intent = new Intent().setClass(this, GeatteListOthersActivity.class);
    //	intent.putExtra(Config.EXTRA_FRIENDGEATTE_STARTFROM, mFriendInterstStartFrom);
    //	spec = tabHost.newTabSpec("GeatteListOthersActivity").setIndicator("Friend's Interests")
    //	.setContent(intent);
    //	tabHost.addTab(spec);
    //
    //	tabHost.setCurrentTab(mCurrentTab);
    //    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(Config.LOGTAG, "GeatteTabActivity:onCreate(): START");

	mDialog = ProgressDialog.show(GeatteTabActivity.this, "Loading", "Please wait...", true);

	mTabHost = getTabHost();  // The activity TabHost

	mTabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);

	mMyInterstStartFrom = getIntent().getIntExtra(Config.EXTRA_MYGEATTE_STARTFROM, 1);
	mFriendInterstStartFrom = getIntent().getIntExtra(Config.EXTRA_MYGEATTE_STARTFROM, 1);
	mCurrentTab = getIntent().getIntExtra(Config.EXTRA_CURRENT_TAB, 0);

	Intent intent1 = new Intent().setClass(getApplicationContext(), GeatteListActivity.class);
	intent1.putExtra(Config.EXTRA_MYGEATTE_STARTFROM, mMyInterstStartFrom);

	setupTab(intent1, "My Geattes");

	Intent intent2 = new Intent().setClass(getApplicationContext(), GeatteListOthersActivity.class);
	intent2.putExtra(Config.EXTRA_FRIENDGEATTE_STARTFROM, mFriendInterstStartFrom);

	setupTab(intent2, "Friend's Geattes");

	mTabHost.setCurrentTab(mCurrentTab);

	mHandler.postDelayed(new Runnable() {
	    public void run() {
		if (mDialog != null && mDialog.isShowing()) {
		    try {
			Log.d(Config.LOGTAG, "GeatteAllFeedbackActivity:onCreate(): try to dismiss mDialog");
			mDialog.dismiss();
			mDialog = null;
		    } catch (Exception e) {
			Log.w(Config.LOGTAG, "GeatteAllFeedbackActivity:onCreate(): failed to dismiss mDialog", e);
		    }
		}
	    }
	},500);

	Log.d(Config.LOGTAG, "GeatteTabActivity:onCreate(): END");
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	Log.d(Config.LOGTAG, "GeatteTabActivity:onDestroy(): START");
	Log.d(Config.LOGTAG, "GeatteTabActivity:onDestroy(): END");
    }

    @Override
    public int createLayout() {
	return R.layout.geatte_tab;
    }

    private void setupTab(final Intent intent, final String tag) {
	View tabview = createTabView(mTabHost.getContext(), tag);

	TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(intent);
	mTabHost.addTab(setContent);
    }

    private static View createTabView(final Context context, final String text) {
	View view = LayoutInflater.from(context).inflate(R.layout.geatte_tabs_bg, null);
	TextView tv = (TextView) view.findViewById(R.id.tabsText);
	tv.setText(text);
	return view;
    }
}