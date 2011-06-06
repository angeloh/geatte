package com.geatte.app.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class GeatteContactsServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(GeatteContactsServlet.class.getName());
    private static final String ERROR_STATUS = "ERROR";
    private String mCountryCodeField = null;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
	log.log(Level.INFO, "GeatteContactsServlet.doPOST() : START GeatteContactsServlet.doPOST()");
	try {
	    resp.setContentType("application/json");

	    GeatteContactsRequestInfo reqInfo = GeatteContactsRequestInfo.processRequest(req, resp, getServletContext());
	    if (reqInfo == null) {
		log.severe("GeatteContactsServlet.doPOST() : can not load RequestInfo!!");
		return;
	    }

	    mCountryCodeField = reqInfo.getParameterStr(Config.CONTACT_DEFAULT_COUNTRY_CODE);
	    if (mCountryCodeField == null) {
		log.warning("GeatteContactsServlet.doPOST() : Missing country code, " + Config.CONTACT_DEFAULT_COUNTRY_CODE + " is null");
		mCountryCodeField = "us"; //default as US
	    }

	    JSONArray jsonArray = reqInfo.getParameterJSONArray(Config.CONTACT_LIST);
	    if (jsonArray == null) {
		resp.setStatus(400);
		resp.getWriter().println(ERROR_STATUS + "(Must specify JSON" + Config.CONTACT_LIST + ")");
		log.severe("GeatteContactsServlet.doPOST() : Missing JSON array, " + Config.CONTACT_LIST + " is null");
		return;
	    }

	    JSONArray retJSONArray = filterGeatteExistingUsers(jsonArray, mCountryCodeField);
	    JSONObject retJson = new JSONObject();
	    try {
		retJson.put(Config.CONTACT_LIST, retJSONArray);
		PrintWriter out = resp.getWriter();
		retJson.write(out);
		log.log(Level.INFO, "GeatteContactsServlet.doPOST() : Successfully send back contacts to user in JSON = " + retJson);
	    } catch (JSONException jsonEX) {
		throw new IOException(jsonEX);
	    }

	    log.log(Level.INFO, "GeatteContactsServlet.doPOST() : END GeatteContactsServlet.doPOST()");
	} catch (Exception ex) {
	    throw new ServletException(ex);
	}
    }

    private JSONArray filterGeatteExistingUsers(JSONArray jsonArray, String defaultCountryCode) {
	// Context-shared PMF.
	PersistenceManager pm = DBHelper.getPMF(getServletContext()).getPersistenceManager();
	JSONArray retJSONArray = new JSONArray();

	try {
	    for (int i = 0 ; i < jsonArray.length(); i++) {
		JSONObject jObject = jsonArray.getJSONObject(i);
		String phone = jObject.getString(Config.CONTACT_PHONE_NUMBER).toString();
		//String contactId = jObject.getString(Config.CONTACT_ID).toString();
		String[] ret = new String[1];
		boolean isGeatteDevice = DeviceInfo.checkPhoneExistedHadDeviceInfo(pm, phone, defaultCountryCode, ret);
		if (isGeatteDevice) {
		    //put the contact back which is as same as the server device info
		    if (ret[0] != null) {
			jObject.remove(Config.CONTACT_PHONE_NUMBER);
			jObject.put(Config.CONTACT_PHONE_NUMBER, ret[0]);
		    } else {
			log.log(Level.WARNING, "GeatteContactsServlet.filterGeatteExistingUsers() : Error getting contact " +
				"phone number from ret array, use old one = " + phone);
		    }

		    retJSONArray.put(jObject);
		}

		if (retJSONArray.length() > 0) {
		    log.info("GeatteContactsServlet.filterGeatteExistingUsers() : finish filter the contacts, ret = " + retJSONArray);
		}
	    }
	} catch (Exception e) {
	    log.log(Level.WARNING, "GeatteContactsServlet.filterGeatteExistingUsers() : Error checking contacts", e);
	} finally {
	    pm.close();
	}
	return retJSONArray;

    }
}