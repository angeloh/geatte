package com.geatte.mobile;

import android.database.Cursor;
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
	    imageView.setImageBitmap(BitmapFactory.decodeFile(savedImagePath));

	    return true;
	}
	return false;
    }

}
