package com.geatte.android.app;

import greendroid.app.GDApplication;
import android.content.Intent;
import android.net.Uri;

public class GeatteApplication extends GDApplication {

    @Override
    public Class<?> getHomeActivityClass() {
	return GeatteCanvas.class;
    }

    @Override
    public Intent getMainApplicationIntent() {
	return new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_url)));
    }

}
