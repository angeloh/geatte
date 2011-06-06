package com.geatte.app.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.c2dm.server.C2DMessaging;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GeatteSendServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(GeatteSendServlet.class.getName());
    private static final int MAX_RETRY = 3;
    public static final String RETRY_COUNT = "X-AppEngine-TaskRetryCount";
    public static final String URI = "/tasks/geattesend";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

	String toNumbers = req.getParameter(Config.GEATTE_TO_NUMBER_PARAM);
	String defaultCountryCode = req.getParameter(Config.GEATTE_COUNTRY_ISO_PARAM);

	log.info("GeatteSendServlet:doPost() : send geatte to numbers = " + toNumbers + ", defaultCountryCode = " + defaultCountryCode);
	//String geatteId = req.getParameter(Config.GEATTE_ID_PARAM);
	String retryCount = req.getHeader(RETRY_COUNT);
	log.info("GeatteSendServlet:doPost() : send geatte retryCount = " + retryCount);
	if (retryCount != null) {
	    int retryCnt = Integer.parseInt(retryCount);
	    if (retryCnt > MAX_RETRY) {
		log.severe("GeatteSendServlet:doPost() : Too many retries, drop geatte for :" + toNumbers);
		resp.setStatus(200);
		return; // will not try again.
	    }
	}

	List<String> numberList = splitNumbers(toNumbers);
	List<DeviceInfo> allDevices = getDevices(numberList, defaultCountryCode);

	Map<String, String[]> params = req.getParameterMap();
	String collapse = req.getParameter(C2DMessaging.PARAM_COLLAPSE_KEY);
	boolean delayWhenIdle = null != req.getParameter(C2DMessaging.PARAM_DELAY_WHILE_IDLE);

	StringBuilder errorMsg = new StringBuilder();
	boolean sentOk = false;
	for (DeviceInfo device : allDevices) {
	    try {
		log.info("GeatteSendServlet:doPost() : going to send geatte to " + device.getPhoneNumber());
		sentOk = C2DMessaging.get(getServletContext()).sendNoRetry(device.getDeviceRegistrationID(),
			collapse, params, delayWhenIdle);
		log.info("GeatteSendServlet:doPost() sendNoRetry's result : " + sentOk + ", deviceRegistrationID = "
			+ device.getDeviceRegistrationID() + ", phoneNumber = " + device.getPhoneNumber());
		// if (sentOk) {
		// } else {
		// resp.setStatus(500); // retry this task
		// }
	    } catch (IOException ex) {
		log.log(Level.WARNING, "GeatteSendServlet:doPost() : Error seding message to device", ex);
		errorMsg.append("#").append(("Non-retriable error:" + ex.toString()).getBytes());
	    }
	    //log.log(Level.INFO, "sending geatte to device", device.getDeviceRegistrationID() + ", sentOk = " + sentOk);
	}
	resp.setStatus(200);
	if (errorMsg.toString().isEmpty()) {
	    resp.getOutputStream().write("OK".getBytes());
	    log.info("GeatteSendServlet:doPost() : send geatte to all numbers SUCCEEDED");
	} else {
	    resp.setStatus(500);
	    resp.getOutputStream().write(errorMsg.toString().getBytes());
	    log.log(Level.WARNING, "GeatteSendServlet:doPost() : send geatte to all numbers with error = " + errorMsg.toString());
	}
    }

    private List<String> splitNumbers(String numbers) {

	if (numbers == null || numbers.isEmpty()) {
	    return new ArrayList<String>();
	}
	numbers = numbers.trim();
	String[] list = numbers.split(";");
	return Arrays.asList(list);
    }

    private List<DeviceInfo> getDevices(List<String> numberList, String defaultCountryCode) {
	// Context-shared PMF.
	PersistenceManager pm = DBHelper.getPMF(getServletContext()).getPersistenceManager();
	List<DeviceInfo> allDevices = new ArrayList<DeviceInfo>();
	try {
	    for (String number : numberList) {
		List<DeviceInfo> devicesForNumber = DeviceInfo.getDeviceInfoForNumber(pm, number, defaultCountryCode);
		if (devicesForNumber != null) {
		    allDevices.addAll(devicesForNumber);
		}
		log.info("GeatteSendServlet:getDevices() : get devices for number = " + number);
	    }
	} catch (Exception e) {
	    log.log(Level.WARNING, "GeatteSendServlet:getDevices() : Error loading devices ", e);
	} finally {
	    pm.close();
	}
	return allDevices;

    }
}
