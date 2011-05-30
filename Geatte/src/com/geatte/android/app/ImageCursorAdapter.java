package com.geatte.android.app;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

public class ImageCursorAdapter extends SimpleCursorAdapter {
    public ImageCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
	super(context, layout, c, from, to);
	setViewBinder(new ImageCursorViewBinder());
    }
}