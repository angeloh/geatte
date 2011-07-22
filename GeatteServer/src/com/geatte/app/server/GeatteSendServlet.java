package com.geatte.app.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.geatte.app.shared.CommonUtils;
import com.google.android.c2dm.server.C2DMessaging;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONObject;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GeatteSendServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(GeatteSendServlet.class.getName());
    private static final int MAX_RETRY = 3;
    public static final String RETRY_COUNT = "X-AppEngine-TaskRetryCount";
    public static final String URI = "/tasks/geattesend";
    public static enum MSG_TYPE {NEW_ITEM, VOTE, MESSAGE}
    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

	String geatteId = req.getParameter(Config.GEATTE_ID_PARAM);
	String fromNumber = req.getParameter(Config.GEATTE_FROM_NUMBER_PARAM);
	String toNumbers = req.getParameter(Config.GEATTE_TO_NUMBER_PARAM);
	String defaultCountryCode = req.getParameter(Config.GEATTE_COUNTRY_ISO_PARAM);
	String collapse = req.getParameter(C2DMessaging.PARAM_COLLAPSE_KEY);
	boolean delayWhenIdle = null != req.getParameter(C2DMessaging.PARAM_DELAY_WHILE_IDLE);
	try {
	    toNumbers = URLDecoder.decode((toNumbers==null ? "" : toNumbers), Config.ENCODE_UTF8);
	    defaultCountryCode = URLDecoder.decode((defaultCountryCode==null ? "" : defaultCountryCode), Config.ENCODE_UTF8);
	    collapse = URLDecoder.decode((collapse==null ? "" : collapse), Config.ENCODE_UTF8);
	} catch (UnsupportedEncodingException ex) {
	    log.log(Level.WARNING, "GeatteSendServlet:doPost() : Error", ex);
	}

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

	List<String> numberList = CommonUtils.splitStringBySemiColon(toNumbers);
	List<DeviceInfo> allDevices = getDevices(numberList, defaultCountryCode);
	if (geatteId != null) {
	    saveDevicesToItem(allDevices, geatteId);
	}

	Map<String, String[]> params = req.getParameterMap();

	StringBuilder errorMsg = new StringBuilder();
	boolean sentOk = false;
	for (DeviceInfo device : allDevices) {
	    try {
		log.info("GeatteSendServlet:doPost() : going to send geatte to " + device.getPhoneNumber());
		if (device.getType().equalsIgnoreCase(DeviceInfo.TYPE_IOS)) {
		    sentOk = doSendIOS(params, device);
		}else {
		    sentOk = doSendC2DM(collapse, delayWhenIdle, params, device);
		}

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
	if (errorMsg.toString().isEmpty()) {
	    resp.setStatus(200);
	    resp.getOutputStream().write("OK".getBytes());
	    log.info("GeatteSendServlet:doPost() : send geatte to all numbers SUCCEEDED");
	} else {
	    resp.setStatus(500);
	    resp.getOutputStream().write(errorMsg.toString().getBytes());
	    log.log(Level.WARNING, "GeatteSendServlet:doPost() : send geatte to all numbers with error = " + errorMsg.toString());
	}
    }

    private boolean doSendC2DM(String collapse, boolean delayWhenIdle, Map<String, String[]> params, DeviceInfo device)
    throws IOException {
	boolean sentOk;
	sentOk = C2DMessaging.get(getServletContext()).sendNoRetry(device.getDeviceRegistrationID(),
		collapse, params, delayWhenIdle);
	return sentOk;
    }

    private boolean doSendIOS(Map<String, String[]> params, DeviceInfo deviceInfo) {
	try {
	    MSG_TYPE msgType = MSG_TYPE.NEW_ITEM;
	    URL url = new URL("https://go.urbanairship.com/api/push/");

	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    connection.setRequestMethod("POST");
	    connection.setDoOutput(true);

	    String appKey = Config.URBAN_APP_KEY;
	    String appMasterSecret = Config.URBAN_APP_MASTERSECRET;

	    String authString = appKey + ":" + appMasterSecret;
	    String authStringBase64 = Base64.encode(authString.getBytes());
	    authStringBase64 = authStringBase64.trim();

	    connection.setRequestProperty("Content-type", "application/json");
	    connection.setRequestProperty("Authorization", "Basic " + authStringBase64);

	    JSONObject object = new JSONObject();
	    object.put("aps", deviceInfo.getDeviceName());

	    JSONArray devices = new JSONArray();
	    devices.put(deviceInfo.getDeviceRegistrationID());
	    object.put("device_tokens", devices);

	    JSONArray tags = new JSONArray();

	    for (Object keyObj : params.keySet()) {
		String key = (String) keyObj;
		if (key.startsWith("data.")) {
		    if (key.equals("data.geatteid")) {
			msgType = MSG_TYPE.NEW_ITEM;
		    } else if (key.equals("data.geatte_vote_resp")) {
			msgType = MSG_TYPE.VOTE;
		    }
		    String[] values = params.get(key);
		    String value = (values[0]==null ? "" : values[0]);
		    tags.put(key+"="+value);
		}
	    }

	    object.put("tags", tags);

	    JSONObject aps = new JSONObject();
	    if (msgType == MSG_TYPE.NEW_ITEM) {
		aps.put("alert", "You friend need you on Shopinion.");
	    } else if (msgType == MSG_TYPE.VOTE) {
		aps.put("alert", "You friend send back a vote on Shopinion.");
	    }
	    object.put("aps", aps);

	    log.log(Level.INFO, "GeatteSendServlet.doSendIOS() : PUSH to Urban json = " + object);

	    OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
	    osw.write(object.toString());
	    osw.close();

	    int responseCode = connection.getResponseCode();
	    log.log(Level.INFO, "GeatteSendServlet.doSendIOS() : PUSH to Urban OK, resp = " + responseCode);
	    return true;
	} catch (Exception e) {
	    log.log(Level.WARNING, "GeatteSendServlet.doSendIOS() : Error PUSH to Urban.", e);
	}
	return false;
    }

    private List<DeviceInfo> getDevices(List<String> numberList, String defaultCountryCode) {
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

    private void saveDevicesToItem(List<DeviceInfo> allDevices, String geatteId) {
	PersistenceManager pm = DBHelper.getPMF(getServletContext()).getPersistenceManager();
	try {
	    Set<String> phones = new HashSet<String>();
	    for (DeviceInfo dInfo : allDevices) {
		phones.add(dInfo.getPhoneNumber());
	    }

	    Long id = Long.parseLong(geatteId);
	    GeatteInfo geatte = pm.getObjectById(GeatteInfo.class, id);
	    geatte.setToDeviceKeys(phones);
	    pm.makePersistent(geatte);
	    log.info("GeatteSendServlet.saveDevicesToItem() : obtain geatte for id = " + geatteId + ", save device phone numbers to db");
	} catch (JDOObjectNotFoundException ex) {
	    log.warning("GeatteSendServlet.saveDevicesToItem() : can not obtain geatte from db for id = " + geatteId);
	} catch (NumberFormatException nfe) {
	    log.warning("GeatteSendServlet.saveDevicesToItem() : wrong format of geatteId = " + geatteId);
	} finally {
	    pm.close();
	}

    }
}
