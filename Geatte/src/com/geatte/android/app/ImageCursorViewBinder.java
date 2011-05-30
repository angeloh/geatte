package com.geatte.android.app;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class ImageCursorViewBinder implements ViewBinder {

    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
	int nImageIndex = cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_PATH);
	if(nImageIndex==columnIndex)
	{
	    ImageView imageView = (ImageView)view;

	    String savedImagePath = cursor.getString(
		    cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_PATH));

	    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
	    bitmapOptions.inSampleSize = 8;
	    Bitmap imgBitmap = BitmapFactory.decodeFile(savedImagePath, bitmapOptions);
	    imageView.setImageBitmap(imgBitmap);
	    return true;
	}
	return false;
    }

}
