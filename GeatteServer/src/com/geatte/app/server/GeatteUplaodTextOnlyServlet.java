package com.geatte.app.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import com.google.android.c2dm.server.C2DMessaging;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

@SuppressWarnings("serial")
public class GeatteUplaodTextOnlyServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(GeatteUplaodTextOnlyServlet.class.getName());
    //private static final String OK_STATUS = "OK";
    private static final String RETRY_STATUS = "RETRY";
    private static final String ERROR_STATUS = "ERROR";
    private String mUserEmail = null;
    private String mGeatteIdField = null;
    private String mFromNumberField = null;
    private String mCountryCodeField = null;
    private String mToNumberField = null;
    private String mGeatteTitleField = null;
    private String mGeatteDescField = null;
    private String mImageRandomId = null;
    private Blob mImageBlobField = null;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
	log.log(Level.INFO, "GeatteUplaodTextOnlyServlet.doPOST() : START GeatteUplaodTextOnlyServlet.doPOST()");
	try {
	    resp.setContentType("application/json");

	    // Try ClientLogin
	    UserService userService = UserServiceFactory.getUserService();
	    User user = userService.getCurrentUser();
	    if (user != null) {
		mUserEmail = user.getEmail();
		log.log(Level.INFO,"GeatteUplaodTextOnlyServlet.doPOST() : get user email thru ClientLogin :" + mUserEmail);
	    }
	    if (mUserEmail == null) {
		resp.setStatus(500);
		resp.getWriter().println(ERROR_STATUS + "(Login is required)");
		log.warning("GeatteUplaodTextOnlyServlet.doPOST() : can not get login user email!!");
		return;
	    }

	    ServletFileUpload upload = new ServletFileUpload();

	    FileItemIterator iterator = upload.getItemIterator(req);
	    while (iterator.hasNext()) {
		FileItemStream item = iterator.next();
		InputStream stream = item.openStream();

		if (item.isFormField()) {
		    if (item.getFieldName().equals(Config.GEATTE_FROM_NUMBER_PARAM)) {
			mFromNumberField = Streams.asString(stream);
		    } else if (item.getFieldName().equals(Config.GEATTE_COUNTRY_ISO_PARAM)) {
			mCountryCodeField = Streams.asString(stream);
		    } else if (item.getFieldName().equals(Config.GEATTE_TO_NUMBER_PARAM)) {
			mToNumberField = Streams.asString(stream);
		    } else if (item.getFieldName().equals(Config.GEATTE_TITLE_PARAM)) {
			mGeatteTitleField = Streams.asString(stream);
		    } else if (item.getFieldName().equals(Config.GEATTE_DESC_PARAM)){
			mGeatteDescField = Streams.asString(stream);
		    } else if (item.getFieldName().equals(Config.GEATTE_ID_PARAM)){
			mGeatteIdField = Streams.asString(stream);
		    } else if (item.getFieldName().equals(Config.GEATTE_IMAGE_RANDOM_ID_PARAM)) {
			mImageRandomId = Streams.asString(stream);
		    }
		    log.log(Level.INFO, "GeatteUplaodTextOnlyServlet.doPOST() Got a form field: " + item.getFieldName());
		}
	    }

	    if (mCountryCodeField == null) {
		log.warning("GeatteUplaodTextOnlyServlet.doPOST() : Missing country code, " + Config.GEATTE_COUNTRY_ISO_PARAM + " is null");
		mCountryCodeField = "us"; //default as US
	    }

	    if (mFromNumberField == null) {
		resp.setStatus(400);
		resp.getWriter().println(ERROR_STATUS + "(Must specify " + Config.GEATTE_FROM_NUMBER_PARAM + ")");
		log.severe("GeatteUplaodTextOnlyServlet.doPOST() : Missing from number, " + Config.GEATTE_FROM_NUMBER_PARAM + " is null");
		return;
	    } else {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber numberProto = null;
		try {
		    numberProto = phoneUtil.parse(mFromNumberField, mCountryCodeField);
		} catch (NumberParseException npe) {
		    log.log(Level.WARNING, "GeatteUplaodTextOnlyServlet.doPOST(): NumberParseException was thrown: "
			    , npe);
		}

		if (numberProto != null && phoneUtil.isValidNumber(numberProto)) {
		    mFromNumberField = phoneUtil.format(numberProto, PhoneNumberFormat.E164);
		} else {
		    log.log(Level.WARNING, "GeatteUplaodTextOnlyServlet.doPOST() : Invalid phone number so use passed-in number, " + Config.DEV_PHONE_NUMBER_PARAM
			    + " = " + mFromNumberField + ", countryIso = " + mFromNumberField);
		}

	    }

	    if (mToNumberField == null) {
		resp.setStatus(400);
		resp.getWriter().println(ERROR_STATUS + "(Must specify " + Config.GEATTE_TO_NUMBER_PARAM + ")");
		log.severe("GeatteUplaodTextOnlyServlet.doPOST() : Missing to number, " + Config.GEATTE_TO_NUMBER_PARAM + " is null");
		return;
	    }

	    if (mImageRandomId == null) {
		resp.setStatus(400);
		resp.getWriter().println(ERROR_STATUS + "(Must specify " + Config.GEATTE_IMAGE_RANDOM_ID_PARAM + ")");
		log.severe("GeatteUplaodTextOnlyServlet.doPOST() : Missing image id, " + Config.GEATTE_IMAGE_RANDOM_ID_PARAM + " is null");
		return;
	    }

	    if (mGeatteTitleField == null) {
		mGeatteTitleField = "new geatte";
	    }

	    GeatteTmpImageInfo imageInfo = getUploadedImage(mImageRandomId);
	    if (imageInfo == null) {
		resp.setStatus(400);
		resp.getWriter().println(RETRY_STATUS);
		log.severe("GeatteUplaodTextOnlyServlet.doPOST() : Missing image info, can not load from image table with mImageRandomId = " + mImageRandomId);
		return;
	    }
	    mImageBlobField = imageInfo.getImage();

	    if (mImageBlobField == null) {
		resp.setStatus(400);
		resp.getWriter().println(RETRY_STATUS);
		log.severe("GeatteUplaodTextOnlyServlet.doPOST() : Missing image, can not load image from image table with mImageRandomId = " + mImageRandomId);
		return;
	    }

	    //after save to db, send geatte to contacts
	    String geatteId = null;
	    if ((geatteId = saveToDb(resp)) != null ) {
		log.log(Level.INFO, "GeatteUplaodTextOnlyServlet.doPOST() : ready to send geatte '" + geatteId  + "' to phoneNumbers = " + mToNumberField);
		// the message push to device
		Map<String, String[]> params = new HashMap<String, String[]>();
		params.put("data.geatteid", new String[]{geatteId});

		submitGeatteTask(mToNumberField, params);
		log.log(Level.INFO, "GeatteUplaodTextOnlyServlet.doPOST() : sent geatte '" + geatteId  + "' to phoneNumbers = " + mToNumberField);

		//image already saved to geatte info, delete from image info
		deleteImageInfo(imageInfo);

		JSONObject geatteJson = new JSONObject();
		try {
		    geatteJson.put(Config.GEATTE_ID_PARAM, geatteId);
		    PrintWriter out = resp.getWriter();
		    geatteJson.write(out);
		    log.log(Level.INFO, "GeatteUplaodTextOnlyServlet.doPOST() : Successfully send a geatte, return geatte id in JSON = " + geatteId);
		} catch (JSONException e) {
		    throw new IOException(e);
		}
	    }
	    else {
		log.warning("GeatteUplaodTextOnlyServlet.doPOST() : unable to save user's geatte to db");
	    }

	    log.log(Level.INFO, "GeatteUplaodTextOnlyServlet.doPOST() : END GeatteUploadServlet.doPOST()");
	} catch (Exception ex) {
	    throw new ServletException(ex);
	}
    }

    private GeatteTmpImageInfo getUploadedImage(String imageRandomId) {
	ServletContext ctx = getServletContext();
	if (ctx == null) {
	    return null;
	}
	PersistenceManager pm = DBHelper.getPMF(ctx).getPersistenceManager();
	try {
	    GeatteTmpImageInfo imageInfo = GeatteTmpImageInfo.getImageInfoForImageId(pm, imageRandomId);

	    //retries
	    if (imageInfo == null) {
		int retries = 0;
		while (retries < 2 && imageInfo == null) {
		    try {
			Thread.sleep(500);
			imageInfo = GeatteTmpImageInfo.getImageInfoForImageId(pm, imageRandomId);
			retries++;
		    } catch (InterruptedException e) {
			// ignore
		    }
		}
	    }
	    return imageInfo;
	} catch (Exception e) {
	    log.warning("GeatteUplaodTextOnlyServlet.getUploadedImage() : Error : " + e.getMessage());
	} finally {
	    pm.close();
	}
	return null;
    }

    private void deleteImageInfo(GeatteTmpImageInfo imageInfo) {
	ServletContext ctx = getServletContext();
	if (ctx == null) {
	    return;
	}
	PersistenceManager pm = DBHelper.getPMF(ctx).getPersistenceManager();
	try {
	    pm.deletePersistent(imageInfo);
	    log.log(Level.INFO, "[DEBUG] GeatteUplaodTextOnlyServlet.getUploadedImage() : deleted imageInfo for " + imageInfo.getId());
	} catch (Exception e) {
	    log.warning("GeatteUplaodTextOnlyServlet.getUploadedImage() : Error : " + e.getMessage());
	} finally {
	    pm.close();
	}
    }


    private String saveToDb(HttpServletResponse resp) throws IOException {
	// Context-shared PMF.
	PersistenceManager pm = DBHelper.getPMF(getServletContext()).getPersistenceManager();

	try {
	    // Get geatte if it already exists, else create
	    GeatteInfo geatteInfo = null;

	    if (mGeatteIdField == null) {
		log.log(Level.INFO, "GeatteUploadServlet.doPOST() : create a new GeatteInfo");

		geatteInfo = new GeatteInfo(mImageBlobField);
	    } else {
		log.log(Level.INFO, "GeatteUploadServlet.doPOST() : find a GeatteInfo by key = " + mGeatteIdField);

		Key key = KeyFactory.createKey(GeatteInfo.class.getSimpleName(), mGeatteIdField);
		try {
		    geatteInfo = pm.getObjectById(GeatteInfo.class, key);
		} catch (JDOObjectNotFoundException e) {
		    log.log(Level.SEVERE, "GeatteUploadServlet.doPOST() : failed to get GeatteInfo by key = " + mGeatteIdField);
		}
		// update image
		geatteInfo.setImage(mImageBlobField);
	    }

	    geatteInfo.setFromNumber(this.mFromNumberField);
	    geatteInfo.setCountryCode(this.mCountryCodeField);
	    geatteInfo.setToNumber(this.mToNumberField);
	    geatteInfo.setGeatteTitile(this.mGeatteTitleField);
	    geatteInfo.setGeatteDesc(this.mGeatteDescField);

	    pm.makePersistent(geatteInfo);

	    log.log(Level.INFO, "GeatteUploadServlet.doPOST() : Saved geatteInfo fromNumber = " + mFromNumberField
		    + ", toNumber = " + mToNumberField + ", geatteTitile = " + mGeatteTitleField
		    + ", geatteDesc = " + mGeatteDescField + ", id = " + geatteInfo.getId().toString());

	    //res.getWriter().println(geatteInfo.getId().toString());
	    return geatteInfo.getId().toString();

	} catch (Exception e) {
	    resp.setStatus(400);
	    resp.getWriter().println(ERROR_STATUS + " (Error saving geatteInfo)");
	    log.log(Level.SEVERE, "GeatteUploadServlet.doPOST() : Error saving geatteInfo.", e);
	} finally {
	    pm.close();
	}
	return null;
    }

    private void submitGeatteTask(String toNumbers, Map<String, String[]> params) {
	log.log(Level.INFO, "GeatteUploadServlet.submitGeatteTask() : START submit geatte to " + toNumbers);
	boolean delayWhileIdle = true;
	String collapseKey = "1";
	Queue dmQueue = QueueFactory.getQueue("geatte_send");
	//Queue dmQueue = QueueFactory.getDefaultQueue();
	try {
	    TaskOptions url = TaskOptions.Builder.withUrl(GeatteSendServlet.URI)
	    .param(Config.GEATTE_TO_NUMBER_PARAM, toNumbers).param(Config.GEATTE_COUNTRY_ISO_PARAM, mCountryCodeField)
	    .param(C2DMessaging.PARAM_COLLAPSE_KEY, collapseKey);
	    if (delayWhileIdle) {
		url.param(C2DMessaging.PARAM_DELAY_WHILE_IDLE, "1");
	    }
	    for (String key : params.keySet()) {
		String[] values = params.get(key);
		url.param(key, URLEncoder.encode(values[0], C2DMessaging.UTF8));
	    }

	    // Task queue implements the exponential backoff
	    //long jitter = (int) Math.random() * DATAMESSAGING_MAX_JITTER_MSEC;
	    //url.countdownMillis(jitter);

	    TaskHandle add = dmQueue.add(url);
	    log.log(Level.INFO, "GeatteUploadServlet.submitGeatteTask() : add one task to queue, url = " + url.getUrl());
	} catch (UnsupportedEncodingException e) {
	    // Ignore - UTF8 should be supported
	    log.log(Level.SEVERE, "Unexpected error", e);
	}
	log.log(Level.INFO, "GeatteUploadServlet.submitGeatteTask() : END submit geatte to " + toNumbers);

    }

}
