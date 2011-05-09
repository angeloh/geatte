package com.geatte.app.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;

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

@SuppressWarnings("serial")
public class GeatteUploadServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(GeatteUploadServlet.class.getName());
    //private static final String OK_STATUS = "OK";
    private static final String ERROR_STATUS = "ERROR";
    private String mUserEmail = null;
    private String mGeatteIdField = null;
    private String mFromNumberField = null;
    private String mToNumberField = null;
    private String mGeatteTitleField = null;
    private String mGeatteDescField = null;
    private Blob mImageBlobField = null;


    public void doPostOrig(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	log.log(Level.INFO, "GeatteUploadServlet.doPOST() : START GeatteUploadServlet.doPOST()");

	resp.setContentType("text/plain");

	RequestInfo reqInfo = RequestInfo.processRequest(req, resp, getServletContext());
	if (reqInfo == null) {
	    log.severe("GeatteUploadServlet.doPOST() : can not load RequestInfo!!");
	    return;
	}

	String fromNumber = reqInfo.getParameter(Config.FROM_NUMBER_PARAM);

	if (fromNumber == null) {
	    resp.getWriter().println(ERROR_STATUS + "(Must specify " + Config.FROM_NUMBER_PARAM + ")");
	    log.severe("GeatteUploadServlet.doPOST() : Missing from number, " + Config.FROM_NUMBER_PARAM + " is null");
	    return;
	} else {
	    log.log(Level.INFO, "GeatteUploadServlet.doPOST() : user sent a " + Config.FROM_NUMBER_PARAM + " = " + fromNumber);
	}

	String toNumber = reqInfo.getParameter(Config.TO_NUMBER_PARAM);

	if (toNumber == null) {
	    resp.getWriter().println(ERROR_STATUS + "(Must specify " + Config.TO_NUMBER_PARAM + ")");
	    log.severe("GeatteUploadServlet.doPOST() : Missing to number, " + Config.TO_NUMBER_PARAM + " is null");
	    return;
	} else {
	    log.log(Level.INFO, "GeatteUploadServlet.doPOST() : user sent a " + Config.TO_NUMBER_PARAM + " = "
		    + toNumber);
	}

	String title = reqInfo.getParameter(Config.GEATTE_TITLE_PARAM);

	if (title == null) {
	    resp.getWriter().println(ERROR_STATUS + "(Must specify " + Config.GEATTE_TITLE_PARAM + ")");
	    log.severe("GeatteUploadServlet.doPOST() : Missing geatte title, " + Config.GEATTE_TITLE_PARAM + " is null");
	    return;
	} else {
	    log.log(Level.INFO, "GeatteUploadServlet.doPOST() : user sent a " + Config.GEATTE_TITLE_PARAM + " = "
		    + title);
	}

	String desc = reqInfo.getParameter(Config.GEATTE_DESC_PARAM);

	if (desc != null) {
	    log.log(Level.INFO, "GeatteUploadServlet.doPOST() : user sent a " + Config.GEATTE_DESC_PARAM + " = "
		    + title);
	}


	InputStream stream = req.getInputStream();
	Blob imageBlob = new Blob(IOUtils.toByteArray(stream));

	// Get the image representation
	/*ServletFileUpload upload = new ServletFileUpload();
	FileItemIterator iter = upload.getItemIterator(req);
	Blob imageBlob = null;
	while (iter.hasNext()) {
	    FileItemStream item = iter.next();
	    InputStream stream = item.openStream();

	    if (!item.isFormField()) {
		imageBlob = new Blob(IOUtils.toByteArray(stream));
		log.log(Level.INFO, "GeatteUploadServlet.doPOST() : user sent an image file : " + item.getFieldName() + ", name : " + item.getName());
	    }
	}
	 */
	if (imageBlob == null) {
	    resp.getWriter().println(ERROR_STATUS + "(Must specify " + Config.GEATTE_IMAGE_PARAM + ")");
	    log.severe("GeatteUploadServlet.doPOST() : Missing geatte image, " + Config.GEATTE_IMAGE_PARAM + " is null");
	    return;
	}

    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
	log.log(Level.INFO, "GeatteUploadServlet.doPOST() : START GeatteUploadServlet.doPOST()");
	try {
	    // Try ClientLogin
	    UserService userService = UserServiceFactory.getUserService();
	    User user = userService.getCurrentUser();
	    if (user != null) {
		mUserEmail = user.getEmail();
		log.log(Level.INFO,"GeatteUploadServlet.doPOST() : get user email thru ClientLogin :" + mUserEmail);
	    }
	    if (mUserEmail == null) {
		res.setStatus(500);
		res.getWriter().println(ERROR_STATUS + "(Login is required)");
		log.warning("RequestInfo:processRequest() : can not get login user email!!");
		return;
	    }

	    ServletFileUpload upload = new ServletFileUpload();
	    res.setContentType("text/plain");

	    FileItemIterator iterator = upload.getItemIterator(req);
	    while (iterator.hasNext()) {
		FileItemStream item = iterator.next();
		InputStream stream = item.openStream();

		if (item.isFormField()) {
		    if (item.getFieldName().equals(Config.FROM_NUMBER_PARAM)) {
			mFromNumberField = Streams.asString(stream);
		    } else if (item.getFieldName().equals(Config.TO_NUMBER_PARAM)) {
			mToNumberField = Streams.asString(stream);
		    } else if (item.getFieldName().equals(Config.GEATTE_TITLE_PARAM)) {
			mGeatteTitleField = Streams.asString(stream);
		    } else if (item.getFieldName().equals(Config.GEATTE_DESC_PARAM)){
			mGeatteDescField = Streams.asString(stream);
		    } else if (item.getFieldName().equals(Config.GEATTE_ID_PARAM)){
			mGeatteIdField = Streams.asString(stream);
		    }
		    log.log(Level.INFO, "Got a form field: " + item.getFieldName());
		} else {
		    mImageBlobField = new Blob(IOUtils.toByteArray(stream));
		    log.log(Level.INFO, "GeatteUploadServlet.doPOST() : user sent an image file : " + item.getFieldName() + ", name : " + item.getName());
		}
	    }

	    if (mFromNumberField == null) {
		res.setStatus(400);
		res.getWriter().println(ERROR_STATUS + "(Must specify " + Config.FROM_NUMBER_PARAM + ")");
		log.severe("GeatteUploadServlet.doPOST() : Missing from number, " + Config.FROM_NUMBER_PARAM + " is null");
		return;
	    }
	    if (mToNumberField == null) {
		res.setStatus(400);
		res.getWriter().println(ERROR_STATUS + "(Must specify " + Config.TO_NUMBER_PARAM + ")");
		log.severe("GeatteUploadServlet.doPOST() : Missing to number, " + Config.TO_NUMBER_PARAM + " is null");
		return;
	    }
	    if (mGeatteTitleField == null) {
		mGeatteTitleField = "new geatte";
	    }
	    if (mImageBlobField == null) {
		res.setStatus(400);
		res.getWriter().println(ERROR_STATUS + "(Must specify " + Config.GEATTE_IMAGE_PARAM + ")");
		log.severe("GeatteUploadServlet.doPOST() : Missing image, " + Config.GEATTE_IMAGE_PARAM + " is null");
		return;
	    }
	    //after save to db, send geatte to contacts
	    String geatteId = null;
	    if ((geatteId = saveToDb(res)) != null ) {
		log.log(Level.INFO, "GeatteUploadServlet.doPOST() : ready to send geatte '" + geatteId  + "' to phoneNumbers = " + mToNumberField);
		Map<String, String[]> params = new HashMap<String, String[]>();
		params.put("data.geatteid", new String[]{geatteId});
		submitGeatteTask(mToNumberField, params);
		log.log(Level.INFO, "GeatteUploadServlet.doPOST() : sent geatte '" + geatteId  + "' to phoneNumbers = " + mToNumberField);
	    }


	    log.log(Level.INFO, "GeatteUploadServlet.doPOST() : END GeatteUploadServlet.doPOST()");
	} catch (Exception ex) {
	    throw new ServletException(ex);
	}
    }

    private String saveToDb(HttpServletResponse res) throws IOException {
	// Context-shared PMF.
	PersistenceManager pm = C2DMessaging.getPMF(getServletContext()).getPersistenceManager();

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
	    geatteInfo.setToNumber(this.mToNumberField);
	    geatteInfo.setGeatteTitile(this.mGeatteTitleField);
	    geatteInfo.setGeatteDesc(this.mGeatteDescField);

	    pm.makePersistent(geatteInfo);

	    log.log(Level.INFO, "GeatteUploadServlet.doPOST() : Saved geatteInfo fromNumber = " + mFromNumberField
		    + ", toNumber = " + mToNumberField + ", geatteTitile = " + mGeatteTitleField
		    + ", geatteDesc = " + mGeatteDescField + ", key = " + geatteInfo.getKey().toString());

	    res.getWriter().println(geatteInfo.getKey());
	    return Long.toString(geatteInfo.getKey().getId());

	} catch (Exception e) {
	    res.setStatus(400);
	    res.getWriter().println(ERROR_STATUS + " (Error saving geatteInfo)");
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
	//Queue dmQueue = QueueFactory.getQueue("c2dm");
	Queue dmQueue = QueueFactory.getDefaultQueue();
	try {
	    TaskOptions url = TaskOptions.Builder.withUrl(GeatteSendServlet.URI)
	    .param(Config.TO_NUMBER_PARAM, toNumbers).param(C2DMessaging.PARAM_COLLAPSE_KEY, collapseKey);
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
	    log.log(Level.INFO, "GeatteUploadServlet.submitGeatteTask() : add one task to queue, url = " + url);
	} catch (UnsupportedEncodingException e) {
	    // Ignore - UTF8 should be supported
	    log.log(Level.SEVERE, "Unexpected error", e);
	}
	log.log(Level.INFO, "GeatteUploadServlet.submitGeatteTask() : END submit geatte to " + toNumbers);

    }

}
