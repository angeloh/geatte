package com.geatte.android.app;

import java.io.File;

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
	    return ret;
	}

    }
}