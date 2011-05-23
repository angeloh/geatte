package com.geatte.android.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class GeatteFeedback extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(Config.LOGTAG, "GeatteFeedback:onCreate");
	//	mDbHelper = new GeatteDBAdapter(this);
	//	mDbHelper.open();

	//setContentView(R.layout.geatte_vote);
	setTitle(R.string.app_name);
    }
}