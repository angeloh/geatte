package com.geatte.app.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GeatteImagePurgeServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(GeatteImagePurgeServlet.class.getName());
    public static final String URI = "/tasks/geatteimagepurge";
    private static final String ERROR_STATUS = "ERROR";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

	String imageId = req.getParameter(Config.GEATTE_IMAGE_RANDOM_ID_PARAM);

	log.info("GeatteImagePurgeServlet:doPost() : try to purge image = " + imageId);
	boolean ret = purgeImage(imageId);
	resp.setStatus(200);
	if (ret) {
	    resp.getOutputStream().write("OK".getBytes());
	    log.info("GeatteImagePurgeServlet:doPost() : purge image SUCCEEDED");
	} else {
	    resp.setStatus(500);
	    resp.getOutputStream().write(ERROR_STATUS.getBytes());
	    log.log(Level.WARNING, "GeatteSendServlet:doPost() : purge image FAILED");
	}
    }

    private boolean purgeImage(String imageId) {
	ServletContext ctx = this.getServletContext();
	PersistenceManager pm = DBHelper.getPMF(ctx).getPersistenceManager();
	try {
	    GeatteTmpImageInfo imageInfo = GeatteTmpImageInfo.getImageInfoForImageId(pm, imageId);
	    pm.deletePersistent(imageInfo);
	    log.log(Level.INFO, "[DEBUG] GeatteImagePurgeServlet : deleted imageInfo for " + imageInfo.getId());
	    return true;
	} catch (Exception e) {
	    log.warning("GeatteImagePurgeServlet : Error : " + e.getMessage());
	} finally {
	    pm.close();
	}
	return false;
    }


}
