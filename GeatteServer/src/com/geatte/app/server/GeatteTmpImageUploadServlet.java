package com.geatte.app.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class GeatteTmpImageUploadServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(GeatteTmpImageUploadServlet.class.getName());
    private static final String OK_STATUS = "OK";
    private static final String ERROR_STATUS = "ERROR";
    private String mUserEmail = null;
    private String mImageIdField = null;
    private Blob mImageBlobField = null;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
	log.log(Level.INFO, "GeatteTmpImageUpload.doPOST() : START GeatteUploadServlet.doPOST()");
	try {
	    resp.setContentType("application/json");

	    // Try ClientLogin
	    UserService userService = UserServiceFactory.getUserService();
	    User user = userService.getCurrentUser();
	    if (user != null) {
		mUserEmail = user.getEmail();
		log.log(Level.INFO,"GeatteTmpImageUpload.doPOST() : get user email thru ClientLogin :" + mUserEmail);
	    }
	    // ***** remove login temporarily *****
	    /*
	    if (mUserEmail == null) {
		resp.setStatus(500);
		resp.getWriter().println(ERROR_STATUS + "(Login is required)");
		log.warning("GeatteTmpImageUpload.doPOST() : can not get login user email!!");
		return;
	    }
	     */

	    ServletFileUpload upload = new ServletFileUpload();

	    FileItemIterator iterator = upload.getItemIterator(req);
	    while (iterator.hasNext()) {
		FileItemStream item = iterator.next();
		InputStream stream = item.openStream();

		if (item.isFormField()) {
		    if (item.getFieldName().equals(Config.GEATTE_IMAGE_RANDOM_ID_PARAM)){
			mImageIdField = Streams.asString(stream);
		    }
		    log.log(Level.INFO, "Got a form field: " + item.getFieldName());
		} else {
		    mImageBlobField = new Blob(IOUtils.toByteArray(stream));
		    log.log(Level.INFO, "GeatteTmpImageUpload.doPOST() : user sent an image file : " + item.getFieldName() + ", name : " + item.getName());
		}
	    }


	    if (mImageIdField == null) {
		resp.setStatus(400);
		resp.getWriter().println(ERROR_STATUS + "(Must specify " + Config.GEATTE_IMAGE_RANDOM_ID_PARAM + ")");
		log.severe("GeatteTmpImageUpload.doPOST() : Missing image id, " + Config.GEATTE_IMAGE_RANDOM_ID_PARAM + " is null");
		return;
	    }
	    if (mImageBlobField == null) {
		resp.setStatus(400);
		resp.getWriter().println(ERROR_STATUS + "(Must specify " + Config.GEATTE_IMAGE_BLOB_PARAM + ")");
		log.severe("GeatteTmpImageUpload.doPOST() : Missing image, " + Config.GEATTE_IMAGE_BLOB_PARAM + " is null");
		return;
	    }

	    if (saveToDb(resp)) {
		JSONObject json = new JSONObject();
		try {
		    json.put(Config.GEATTE_IMAGE_BLOB_RESP, OK_STATUS);
		    PrintWriter out = resp.getWriter();
		    json.write(out);
		    log.log(Level.INFO, "GeatteTmpImageUpload.doPOST() : Successfully uploaded a geatte image, return OK in JSON");
		} catch (JSONException e) {
		    throw new IOException(e);
		}
	    }
	    else {
		log.warning("GeatteTmpImageUpload.doPOST() : unable to save user's geatte image to db");
		JSONObject json = new JSONObject();
		try {
		    json.put(Config.GEATTE_IMAGE_BLOB_RESP, ERROR_STATUS);
		    PrintWriter out = resp.getWriter();
		    json.write(out);
		    log.log(Level.INFO, "GeatteTmpImageUpload.doPOST() : Failed uploaded a geatte image, return ERROR in JSON");
		} catch (JSONException e) {
		    throw new IOException(e);
		}
	    }

	    log.log(Level.INFO, "GeatteTmpImageUpload.doPOST() : END GeatteTmpImageUpload.doPOST()");
	} catch (Exception ex) {
	    throw new ServletException(ex);
	}
    }

    private boolean saveToDb(HttpServletResponse resp) throws IOException {

	PersistenceManager pm = DBHelper.getPMF(getServletContext()).getPersistenceManager();

	try {
	    GeatteTmpImageInfo imageInfo =  new GeatteTmpImageInfo(mImageIdField, mImageBlobField);

	    log.log(Level.INFO, "GeatteTmpImageUpload.saveToDb() : create a new GeatteTmpImageInfo");

	    pm.makePersistent(imageInfo);

	    log.log(Level.INFO, "GeatteTmpImageUpload.saveToDb() : Saved GeatteTmpImageInfo mImageIdField = " + mImageIdField);

	    return true;

	} catch (Exception e) {
	    resp.setStatus(400);
	    resp.getWriter().println(ERROR_STATUS + " (Error saving GeatteTmpImageInfo)");
	    log.log(Level.SEVERE, "GeatteTmpImageUpload.saveToDb() : Error saving GeatteTmpImageInfo.", e);
	} finally {
	    pm.close();
	}
	return false;
    }

}
