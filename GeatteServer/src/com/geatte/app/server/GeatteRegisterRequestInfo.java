package com.geatte.app.server;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

/**
 * Common code and helpers to handle a request and manipulate device info.
 * 
 */
public class GeatteRegisterRequestInfo {
    private static final Logger log = Logger.getLogger(GeatteRegisterRequestInfo.class.getName());
    private static final String ERROR_STATUS = "ERROR";
    private static final String LOGIN_REQUIRED_STATUS = "LOGIN_REQUIRED";

    public List<DeviceInfo> devices = new ArrayList<DeviceInfo>();

    private String userEmail;

    private ServletContext ctx;
    public String deviceRegistrationID;

    /**
     * Authenticate using the req, fetch devices.
     */
    private GeatteRegisterRequestInfo() {
    }

    public GeatteRegisterRequestInfo(String userEmail, ServletContext ctx) {
	this.userEmail = userEmail;
	this.ctx = ctx;
	if (ctx != null) {
	    initDevices(ctx);
	}
    }

    // Request parameters - transitioning to JSON, but need to support existing
    // code.
    Map<String, String[]> parameterMap;
    JSONObject jsonParams;

    public boolean isAuth() {
	return userEmail != null;
    }

    /**
     * Authenticate the user, check headers and pull the registration data.
     * 
     * @return null if authentication fails.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static GeatteRegisterRequestInfo processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx)
    throws IOException {

	// Basic XSRF protection
	/*if (req.getHeader("X-Same-Domain") == null) {
	    resp.setStatus(400);
	    resp.getWriter().println(ERROR_STATUS + " (Missing X-Same-Domain header)");
	    log.warning("GeatteRegisterRequestInfo:processRequest() : Missing X-Same-Domain");
	    return null;
	}*/

	User user = null;
	GeatteRegisterRequestInfo reqInfo = new GeatteRegisterRequestInfo();
	reqInfo.ctx = ctx;
	//	OAuthService oauthService = OAuthServiceFactory.getOAuthService();
	//	try {
	//	    user = oauthService.getCurrentUser();
	//	    if (user != null) {
	//		ri.userName = user.getEmail();
	//	    }
	//	} catch (Throwable t) {
	//	    log.log(Level.SEVERE, "Oauth error ", t);
	//	    user = null;
	//	}

	if (user == null) {
	    log.info("GeatteRegisterRequestInfo:processRequest() : user is null, try ClientLogin");
	    // Try ClientLogin
	    UserService userService = UserServiceFactory.getUserService();
	    user = userService.getCurrentUser();
	    if (user != null) {
		reqInfo.userEmail = user.getEmail();
		log.info("GeatteRegisterRequestInfo:processRequest() : get user email thru ClientLogin :" + reqInfo.userEmail);
	    }
	}

	if ("application/json".equals(req.getContentType())) {
	    Reader reader = req.getReader();
	    // where is readFully ?
	    char[] tmp = new char[2048];
	    StringBuffer body = new StringBuffer();
	    while (true) {
		int cnt = reader.read(tmp);
		if (cnt <= 0) {
		    break;
		}
		body.append(tmp, 0, cnt);
	    }
	    try {
		reqInfo.jsonParams = new JSONObject(body.toString());
		log.info("GeatteRegisterRequestInfo:processRequest() : reqInfo.jsonParams :" + reqInfo.jsonParams.toString());
	    } catch (JSONException e) {
		resp.setStatus(500);
		return null;
	    }
	} else {
	    reqInfo.parameterMap = req.getParameterMap();
	    for (String key : reqInfo.parameterMap.keySet()) {
		log.info("GeatteRegisterRequestInfo:processRequest() : reqInfo.parameterMap :" + key + "=" + reqInfo.parameterMap.get(key).toString());
	    }
	}

	reqInfo.deviceRegistrationID = reqInfo.getParameter(Config.DEV_REG_ID_PARAM);
	log.info("RequestInfo:processRequest() : reqInfo.deviceRegistrationID = " + reqInfo.deviceRegistrationID);
	if (reqInfo.deviceRegistrationID != null) {
	    reqInfo.deviceRegistrationID = reqInfo.deviceRegistrationID.trim();
	    if ("".equals(reqInfo.deviceRegistrationID)) {
		reqInfo.deviceRegistrationID = null;
	    }
	    log.info("GeatteRegisterRequestInfo:processRequest() : after trim() reqInfo.deviceRegistrationID = " + reqInfo.deviceRegistrationID);
	}

	// ***** remove login temporarily *****
	/*
	if (reqInfo.userEmail == null) {
	    resp.setStatus(400);
	    resp.getWriter().println(LOGIN_REQUIRED_STATUS);
	    log.warning("GeatteRegisterRequestInfo:processRequest() : can not get login user email!!");
	    return null;
	}*/

	if (ctx != null) {
	    reqInfo.initDevices(ctx);
	}

	return reqInfo;
    }

    public String getParameter(String name) {
	if (jsonParams != null) {
	    return jsonParams.optString(name, null);
	} else {
	    String res[] = parameterMap.get(name);
	    if (res == null || res.length == 0) {
		return null;
	    }
	    return res[0];
	}
    }


    @Override
    public String toString() {
	return userEmail + " " + devices.size() + " " + jsonParams;
    }

    private void initDevices(ServletContext ctx) {
	// Context-shared PMF.
	PersistenceManager pm = DBHelper.getPMF(ctx).getPersistenceManager();

	try {
	    devices = DeviceInfo.getDeviceInfoForUserEmail(pm, userEmail);
	    // cleanup for multi-device
	    /*if (devices.size() > 1) {
		// Make sure there is no 'bare' registration
		// Keys are sorted - check the first
		DeviceInfo first = devices.get(0);
		Key oldKey = first.getKey();
		if (oldKey.toString().indexOf("#") < 0) {
		    log.warning("Removing old-style key " + oldKey.toString());
		    // multiple devices, first is old-style.
		    devices.remove(0);
		    pm.deletePersistent(first);
		}
	    }*/
	    log.log(Level.INFO, "RequestInfo:initDevices() : loading devices " + devices.size() + " for user = " + userEmail);
	} catch (Exception e) {
	    log.log(Level.WARNING, "RequestInfo:initDevices() : Error loading registrations ", e);
	} finally {
	    pm.close();
	}

    }

    // We need to iterate again - can be avoided with a query.
    // delete will fail if the pm is different than the one used to
    // load the object - we must close the object when we're done
    public void deleteRegistration(String regId) {
	if (ctx == null) {
	    return;
	}
	PersistenceManager pm = DBHelper.getPMF(ctx).getPersistenceManager();
	try {
	    List<DeviceInfo> registrations = DeviceInfo.getDeviceInfoForUserEmail(pm, userEmail);
	    for (int i = 0; i < registrations.size(); i++) {
		DeviceInfo deviceInfo = registrations.get(i);
		if (deviceInfo.getDeviceRegistrationID().equals(regId)) {
		    pm.deletePersistent(deviceInfo);
		    // Keep looping in case of duplicates

		    log.log(Level.INFO, "RequestInfo:deleteRegistration() : deleted deviceInfo for " + deviceInfo.getDeviceRegistrationID());
		}
	    }
	} catch (JDOObjectNotFoundException e) {
	    log.warning("RequestInfo:deleteRegistration() : User unknown");
	} catch (Exception e) {
	    log.warning("RequestInfo:deleteRegistration() : Error unregistering device: " + e.getMessage());
	} finally {
	    pm.close();
	}

    }

    public String getDeviceRegistrationID() {
	return deviceRegistrationID;
    }

    public void setDeviceRegistrationID(String deviceRegistrationID) {
	this.deviceRegistrationID = deviceRegistrationID;
    }

    public String getUserEmail() {
	return userEmail;
    }

    public void setUserEmail(String userEmail) {
	this.userEmail = userEmail;
    }

}
