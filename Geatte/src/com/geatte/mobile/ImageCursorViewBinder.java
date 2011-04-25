package com.geatte.mobile;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class ImageCursorViewBinder implements ViewBinder {

    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
	int nImageIndex = cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_IMAGE);
	if(nImageIndex==columnIndex)
	{
	    ImageView imageView = (ImageView)view;

	    byte[] byteArr = cursor.getBlob(
		    cursor.getColumnIndexOrThrow(GeatteDBAdapter.KEY_IMAGE_IMAGE));

	    imageView.setImageBitmap(BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length));

	    return true;
	}
	return false;
    }

}
