package com.geatte.android.app;

import greendroid.app.GDTabActivity;

import com.geatte.android.app.R;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class GeatteTabActivity extends GDTabActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.geatte_tab);

	Resources res = getResources(); // Resource object to get Drawables
	TabHost tabHost = getTabHost();  // The activity TabHost
	TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	Intent intent;  // Reusable Intent for each tab

	// Create an Intent to launch an Activity for the tab (to be reused)
	intent = new Intent().setClass(this, GeatteApp.class);

	// Do the same for the other tabs
	intent = new Intent().setClass(this, GeatteList.class);
	spec = tabHost.newTabSpec("GeatteList").setIndicator("My Interests")
	.setContent(intent);
	tabHost.addTab(spec);

	intent = new Intent().setClass(this, GeatteListOthersActivity.class);
	spec = tabHost.newTabSpec("GeatteListOthersActivity").setIndicator("Friend's Interests")
	.setContent(intent);
	tabHost.addTab(spec);

	tabHost.setCurrentTab(0);
    }
}