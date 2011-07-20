package com.geatte.app.shared;

import java.util.ArrayList;
import java.util.List;

public class CommonUtils {

    public static List<String> splitStringBySemiColon(String str) {
	if (str == null || str.isEmpty()) {
	    return new ArrayList<String>();
	}
	str = str.trim();
	String[] list = str.split(";");
	List<String> ret = new ArrayList<String>();
	for (String n: list) {
	    ret.add(n.trim());
	}
	return ret;
    }
}