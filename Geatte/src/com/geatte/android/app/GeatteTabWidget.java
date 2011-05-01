package com.geatte.android.app;

import com.geatte.android.app.R;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class GeatteTabWidget extends TabActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.geatte_tabs);

	Resources res = getResources(); // Resource object to get Drawables
	TabHost tabHost = getTabHost();  // The activity TabHost
	TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	Intent intent;  // Reusable Intent for each tab

	// Create an Intent to launch an Activity for the tab (to be reused)
	intent = new Intent().setClass(this, GeatteApp.class);

	// Initialize a TabSpec for each tab and add it to the TabHost
	spec = tabHost.newTabSpec("GeatteApp").setIndicator("Geatte",
		res.getDrawable(R.drawable.tab_buttons))
		.setContent(intent);
	tabHost.addTab(spec);

	// Do the same for the other tabs
	intent = new Intent().setClass(this, GeatteList.class);
	spec = tabHost.newTabSpec("GeatteList").setIndicator("My Interests",
		res.getDrawable(R.drawable.tab_buttons))
		.setContent(intent);
	tabHost.addTab(spec);

	intent = new Intent().setClass(this, GeatteListOthers.class);
	spec = tabHost.newTabSpec("GeatteListOthers").setIndicator("Friend's Interests",
		res.getDrawable(R.drawable.tab_buttons))
		.setContent(intent);
	tabHost.addTab(spec);

	tabHost.setCurrentTab(1);
    }
}