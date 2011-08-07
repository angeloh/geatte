package com.geatte.android.app;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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

    public static String convertUTCToLocal(String utcDateStr) {
	if (utcDateStr == null) {
	    return null;
	}

	SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	Date utcDate = null;
	try {
	    utcDate = utcFormat.parse(utcDateStr);
	} catch (ParseException e) {
	    Log.w(Config.LOGTAG, " GeatteDBAdapter:insertFriendInterest() failed to parse createdDate " + utcDateStr);
	}
	if (utcDate == null) {
	    return utcDateStr;
	}

	Calendar cal = Calendar.getInstance();
	TimeZone tz = cal.getTimeZone();

	SimpleDateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	localFormat.setTimeZone(tz);
	String ret = localFormat.format(utcDate);
	return ret;
    }

}