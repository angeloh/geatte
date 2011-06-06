package com.geatte.app.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Blob;

@SuppressWarnings("serial")
public class GeatteImageGetServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(GeatteImageGetServlet.class.getName());
    private static final String ERROR_STATUS = "ERROR";

    /**
     * For debug - and possibly show the info, allow device selection.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	log.log(Level.INFO, "GeatteImageGetServlet.doGet() : START GeatteImageGetServlet.doGet()");
	try {
	    // Try ClientLogin
	    /*UserService userService = UserServiceFactory.getUserService();
	    User user = userService.getCurrentUser();
	    if (user != null) {
		mUserEmail = user.getEmail();
		log.log(Level.INFO,"GeatteImageGetServlet.doGet() : get user email thru ClientLogin :" + mUserEmail);
	    }
	    if (mUserEmail == null) {
		res.setStatus(500);
		res.getWriter().println(ERROR_STATUS + "(Login is required)");
		log.warning("GeatteImageGetServlet.doGet() : can not get login user email!!");
		return;
	    }*/

	    //res.setContentType("application/json");

	    String geatteId = req.getParameter(Config.GEATTE_ID_PARAM);
	    if (geatteId == null) {
		res.setStatus(400);
		res.getWriter().println(ERROR_STATUS + " ( "+ Config.GEATTE_ID_PARAM +"  parameter missing)");
		log.warning("GeatteImageGetServlet.doGet() : can not obtain geatte id from request!!");
		return;
	    }
	    //Key key = KeyFactory.createKey(GeatteInfo.class.getSimpleName(), geatteId);

	    // Context-shared PMF.
	    PersistenceManager pm = DBHelper.getPMF(getServletContext()).getPersistenceManager();
	    //Object id = pm.newObjectIdInstance(GeatteInfo.class, geatteId);

	    GeatteInfo geatte = null;
	    try {
		Long id = Long.parseLong(geatteId);
		geatte = pm.getObjectById(GeatteInfo.class, id);
	    } catch (JDOObjectNotFoundException ex) {
		log.warning("GeatteImageGetServlet.doGet() : can not obtain geatte from db for id = " + geatteId);
		throw new ServletException(ex);
	    } catch (NumberFormatException nfe) {
		log.warning("GeatteImageGetServlet.doGet() : wrong format of geatteId = " + geatteId);
		throw new ServletException(nfe);
	    } finally {
		pm.close();
	    }

	    Blob image = geatte.getImage();

	    // serve the first image
	    res.setContentType("image/jpeg");
	    res.getOutputStream().write(image.getBytes());
	    log.log(Level.INFO, "GeatteImageGetServlet.doGet() : END GeatteImageGetServlet.doGet()");
	} catch (Exception ex) {
	    throw new ServletException(ex);
	}
    }
}