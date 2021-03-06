package com.geatte.app.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

@SuppressWarnings("serial")
public class RegisterServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(RegisterServlet.class.getName());
    private static final String OK_STATUS = "OK";
    private static final String ERROR_STATUS = "ERROR";

    private static int MAX_DEVICES = 10;

    /**
     * For debug - and possibly show the info, allow device selection.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

	GeatteRegisterRequestInfo reqInfo = GeatteRegisterRequestInfo.processRequest(req, resp, getServletContext());
	if (reqInfo == null) {
	    return;
	}

	resp.setContentType("application/json");
	JSONObject regs = new JSONObject();
	try {
	    regs.put("userEmail", reqInfo.getUserEmail());

	    JSONArray devices = new JSONArray();
	    for (DeviceInfo di : reqInfo.devices) {
		JSONObject dijson = new JSONObject();
		dijson.put("key", di.getKey().toString());
		dijson.put("deviceName", di.getDeviceName());
		dijson.put("type", di.getType());
		dijson.put("registrationId", di.getDeviceRegistrationID());
		dijson.put("timestamp", di.getRegistrationTimestamp());
		devices.put(dijson);
	    }
	    regs.put("devices", devices);

	    PrintWriter out = resp.getWriter();
	    regs.write(out);
	} catch (JSONException e) {
	    throw new IOException(e);
	}

    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	log.log(Level.INFO, "RegisterServlet.doPOST() : START RegisterServlet.doPOST()");

	resp.setContentType("text/plain");

	GeatteRegisterRequestInfo reqInfo = GeatteRegisterRequestInfo.processRequest(req, resp, getServletContext());
	if (reqInfo == null) {
	    log.severe("RegisterServlet.doPOST() : can not load RequestInfo!!");
	    return;
	}

	// Because the deviceRegistrationId isn't static, we use a static
	// identifier for the device. (Can be null in older clients)
	String deviceId = reqInfo.getParameter(Config.DEVICE_ID_PARAM);

	if (deviceId == null) {
	    resp.setStatus(400);
	    resp.getWriter().println(ERROR_STATUS + "(Must specify " + Config.DEVICE_ID_PARAM + ")");
	    log.severe("RegisterServlet.doPOST() : Missing device id, " + Config.DEVICE_ID_PARAM + " is null");
	    return;
	} else {
	    log.log(Level.INFO, "RegisterServlet.doPOST() : user sent a " + Config.DEVICE_ID_PARAM + " = " + deviceId);
	}

	String phoneNumber = reqInfo.getParameter(Config.DEV_PHONE_NUMBER_PARAM);

	if (phoneNumber == null) {
	    resp.setStatus(400);
	    resp.getWriter().println(ERROR_STATUS + "(Must specify " + Config.DEV_PHONE_NUMBER_PARAM + ")");
	    log.severe("RegisterServlet.doPOST() : Missing phone number, " + Config.DEV_PHONE_NUMBER_PARAM + " is null");
	    return;
	} else {
	    log.log(Level.INFO, "RegisterServlet.doPOST() : user sent a " + Config.DEV_PHONE_NUMBER_PARAM + " = "
		    + phoneNumber);
	}

	String phoneCountryIso = reqInfo.getParameter(Config.DEV_PHONE_COUNTRY_ISO_PARAM);

	if (phoneCountryIso == null) {
	    //default is us
	    phoneCountryIso = "us";
	    log.severe("RegisterServlet.doPOST() : Missing phone country iso, " + Config.DEV_PHONE_COUNTRY_ISO_PARAM + " is null");
	    return;
	} else {
	    log.log(Level.INFO, "RegisterServlet.doPOST() : user sent a " + Config.DEV_PHONE_COUNTRY_ISO_PARAM + " = "
		    + phoneCountryIso);
	}

	PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
	PhoneNumber numberProto = null;
	try {
	    numberProto = phoneUtil.parse(phoneNumber, phoneCountryIso);
	} catch (NumberParseException npe) {
	    log.log(Level.WARNING, "RegisterServlet.doPOST(): NumberParseException was thrown: "
		    , npe);
	}

	if (numberProto != null && phoneUtil.isValidNumber(numberProto)) {
	    phoneNumber = phoneUtil.format(numberProto, PhoneNumberFormat.E164);
	} else {
	    log.log(Level.WARNING, "RegisterServlet.doPOST() : Invalid phone number so use passed-in number, " + Config.DEV_PHONE_NUMBER_PARAM
		    + " = " + phoneNumber + ", countryIso = " + phoneCountryIso);
	}

	if (reqInfo.deviceRegistrationID == null) {
	    resp.setStatus(400);
	    resp.getWriter().println(ERROR_STATUS + "(Must specify " + Config.DEV_REG_ID_PARAM + ")");
	    log.severe("RegisterServlet.doPOST() : Missing registration id, reqInfo.deviceRegistrationID is null");
	    return;
	} else {
	    log.log(Level.INFO, "RegisterServlet.doPOST() : reqInfo.deviceRegistrationID = "
		    + reqInfo.deviceRegistrationID);
	}

	String deviceName = reqInfo.getParameter(Config.DEVICE_NAME_PARAM);

	if (deviceName == null) {
	    deviceName = "Phone";
	    log.log(Level.INFO, "RegisterServlet.doPOST() : use default " + Config.DEVICE_NAME_PARAM + " = "
		    + deviceName);
	} else {
	    log.log(Level.INFO, "RegisterServlet.doPOST() : user sent a " + Config.DEVICE_NAME_PARAM + " = "
		    + deviceName);
	}
	// TODO: generate the device name by adding a number suffix for multiple
	// devices of same type. Change android app to send model/type.

	String deviceType = reqInfo.getParameter(Config.DEVICE_TYPE_PARAM);

	if (deviceType == null) {
	    deviceType = DeviceInfo.TYPE_AC2DM;
	    log.log(Level.INFO, "RegisterServlet.doPOST() : use default " + Config.DEVICE_TYPE_PARAM + " = "
		    + deviceType);
	} else {
	    log.log(Level.INFO, "RegisterServlet.doPOST() : user sent a " + Config.DEVICE_TYPE_PARAM + " = "
		    + deviceType);
	}

	// Context-shared PMF.
	PersistenceManager pm = DBHelper.getPMF(getServletContext()).getPersistenceManager();

	try {
	    List<DeviceInfo> registrations = reqInfo.devices;

	    if (registrations.size() > MAX_DEVICES) {
		log.log(Level.INFO, "RegisterServlet.doPOST() : user has too many devices, registrations.size = "
			+ registrations.size());
		// we could return an error - but user can't handle it yet.
		// we can't let it grow out of bounds.
		// TODO: we should also define a 'ping' message and
		// expire/remove
		// unused registrations
		DeviceInfo oldest = registrations.get(0);
		if (oldest.getRegistrationTimestamp() == null) {
		    log.log(Level.INFO,
			    "RegisterServlet.doPOST() : user has too many devices, trying to remove old one = "
			    + oldest.getDeviceRegistrationID());
		    pm.deletePersistent(oldest);
		} else {
		    long oldestTime = oldest.getRegistrationTimestamp().getTime();
		    for (int i = 1; i < registrations.size(); i++) {
			if (registrations.get(i).getRegistrationTimestamp().getTime() < oldestTime) {
			    oldest = registrations.get(i);
			    oldestTime = oldest.getRegistrationTimestamp().getTime();
			}
		    }
		    log.log(Level.INFO,
			    "RegisterServlet.doPOST() : user has too many devices, trying to remove old one = "
			    + oldest.getDeviceRegistrationID());
		    pm.deletePersistent(oldest);
		}
	    }

	    // Get device if it already exists, else create
	    // String suffix = (deviceId != null ? "#" +
	    // Long.toHexString(Math.abs(deviceId.hashCode())) : "");
	    // Key key = KeyFactory.createKey(DeviceInfo.class.getSimpleName(),
	    // reqInfo.userName + suffix);
	    Key key = KeyFactory.createKey(DeviceInfo.class.getSimpleName(), deviceId);

	    DeviceInfo device = null;
	    try {
		device = pm.getObjectById(DeviceInfo.class, key);
	    } catch (JDOObjectNotFoundException e) {
	    }
	    if (device == null) {
		log.log(Level.INFO, "RegisterServlet.doPOST() : can not find a DeviceInfo by key = " + key
			+ ", create a new one");

		//clean devices for same phone number, only one device allowed for one phone
		try {
		    List<DeviceInfo> devicesForSameNumber = DeviceInfo.getDeviceInfoForNumber(pm, phoneNumber, phoneCountryIso);
		    for (DeviceInfo deviceSameNumber : devicesForSameNumber) {
			pm.deletePersistent(deviceSameNumber);
		    }
		} catch (JDOObjectNotFoundException e) {
		}

		device = new DeviceInfo(key, reqInfo.deviceRegistrationID);
		device.setType(deviceType);
	    } else {
		log.log(Level.INFO, "RegisterServlet.doPOST() : find a DeviceInfo by key = " + key
			+ ", update deviceRegistrationID to " + reqInfo.deviceRegistrationID);

		// update registration id
		device.setDeviceRegistrationID(reqInfo.deviceRegistrationID);
		device.setRegistrationTimestamp(new Date());
	    }

	    device.setPhoneNumber(phoneNumber); // update phoneNumber
	    device.setCountryCode(phoneCountryIso); //update country code
	    device.setUserEmail(reqInfo.getUserEmail()); // update user email
	    device.setDeviceName(deviceName); // update display name
	    // TODO: only need to write if something changed, for chrome nothing
	    // changes, we just create a new channel
	    pm.makePersistent(device);

	    log.log(Level.INFO, "RegisterServlet.doPOST() : Registered device userEmail = " + reqInfo.getUserEmail()
		    + ", deviceType = " + deviceType + ", deviceRegistrationID = " + reqInfo.deviceRegistrationID
		    + ", key = " + key);

	    // if type is IOS, register to Urban
	    if (device.getType().equalsIgnoreCase(DeviceInfo.TYPE_IOS)) {
		doRegisterIOSToUrban(device);
		resp.getWriter().println(OK_STATUS);
	    } else if (device.getType().equalsIgnoreCase(DeviceInfo.TYPE_ANDROID) ||
		    device.getType().equalsIgnoreCase(DeviceInfo.TYPE_AC2DM)) {
		resp.getWriter().println(OK_STATUS);
	    } else {
		resp.getWriter().println(ERROR_STATUS + "(Wrong " + Config.DEVICE_TYPE_PARAM + ")");
	    }

	    log.log(Level.INFO, "RegisterServlet.doPOST() : END RegisterServlet.doPOST()");
	} catch (Exception e) {
	    resp.setStatus(500);
	    resp.getWriter().println(ERROR_STATUS + " (Error registering device)");
	    log.log(Level.WARNING, "RegisterServlet.doPOST() : Error registering device.", e);
	} finally {
	    pm.close();
	}
    }

    /*private boolean doRegisterIOSToUrban(DeviceInfo deviceInfo) {
	try {
	    UrbanAirshipClient client = new UrbanAirshipClient(Config.URBAN_ACCOUNT, Config.URBAN_PASS);
	    Device device = new Device();
	    device.setiOSDeviceToken(deviceInfo.getDeviceRegistrationID());
	    if (deviceInfo.getDeviceName() != null && !deviceInfo.getDeviceName().isEmpty()) {
		device.setAlias(deviceInfo.getDeviceName());
	    }
	    client.register(device);
	    return true;
	} catch (Exception e) {
	    log.log(Level.WARNING, "RegisterServlet.doRegisterIOSToUrban() : Error registering to Urban.", e);
	}
	return false;
    }*/

    private boolean doRegisterIOSToUrban(DeviceInfo deviceInfo) {
	try {
	    URL url = new URL("https://go.urbanairship.com/api/device_tokens/"+deviceInfo.getDeviceRegistrationID());

	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    connection.setRequestMethod("PUT");
	    connection.setDoOutput(true);

	    String appKey = Config.URBAN_APP_KEY;
	    String appMasterSecret = Config.URBAN_APP_MASTERSECRET;

	    String authString = appKey + ":" + appMasterSecret;
	    String authStringBase64 = Base64.encode(authString.getBytes());
	    authStringBase64 = authStringBase64.trim();

	    connection.setRequestProperty("Content-type", "application/json");
	    connection.setRequestProperty("Authorization", "Basic " + authStringBase64);

	    JSONObject object = new JSONObject();
	    if (deviceInfo.getDeviceName() != null && !deviceInfo.getDeviceName().isEmpty()) {
		object.put("alias", deviceInfo.getDeviceName());
	    }
	    object.put("tz", "America/Los_Angeles");

	    //	    String jsonBodyString = "{\"alias\": \""+ deviceInfo.getDeviceName() +
	    //	    "\", \"tz\": \"America/Los_Angeles\" }";

	    OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
	    osw.write(object.toString());
	    osw.close();

	    int responseCode = connection.getResponseCode();
	    log.log(Level.INFO, "RegisterServlet.doRegisterIOSToUrban() : registering to Urban OK, resp = " + responseCode);
	    return true;
	} catch (Exception e) {
	    log.log(Level.WARNING, "RegisterServlet.doRegisterIOSToUrban() : Error registering to Urban.", e);
	}
	return false;
    }

}
