package com.geatte.android.app;

import java.io.File;

import android.util.Log;

public class CommonUtils {

    public static int getResizeRatio(String filePath, int expectedSizeKB, int origResize) {
	if (filePath == null) {
	    return origResize;
	}

	File file = new File(filePath);
	long fileSize = file.length();
	if (!file.exists()) {
	    return origResize;
	}

	double curFileSizeKB = (double)fileSize/1024;

	double newRatioD = expectedSizeKB/curFileSizeKB;
	double newResize = origResize/newRatioD;
	int ret = 1;
	if (newResize < 1.0) {
	    return ret;
	} else {
	    ret = (int) Math.ceil(newResize);

	    if(Config.LOG_DEBUG_ENABLED) {
		Log.d(Config.LOGTAG, " CommonUtils:getResizeRatio() old resize = " + origResize + ", new resize = " + ret);
	    }
	    return ret;
	}

    }

    public static int getResizeRatio(int curSizeByte, int expectedSizeKB, int origResize) {

	double curFileSizeKB = (double)curSizeByte/1024;

	double newRatioD = expectedSizeKB/curFileSizeKB;
	double newResize = origResize/newRatioD;
	int ret = 1;
	if (newResize < 1.0) {
	    return ret;
	} else {
	    ret = (int) Math.ceil(newResize);
	    return ret;
	}

    }
}