package com.geatte.app.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class GeatteInfoGetServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(GeatteInfoGetServlet.class.getName());
    private static final String ERROR_STATUS = "ERROR";

    /**
     * For debug - and possibly show the info, allow device selection.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	log.log(Level.INFO, "GeatteInfoGetServlet.doGet() : START GeatteInfoGetServlet.doGet()");
	try {
	    // Try ClientLogin
	    /*UserService userService = UserServiceFactory.getUserService();
	    User user = userService.getCurrentUser();
	    if (user != null) {
		mUserEmail = user.getEmail();
		log.log(Level.INFO,"GeatteInfoGetServlet.doGet() : get user email thru ClientLogin :" + mUserEmail);
	    }
	    if (mUserEmail == null) {
		res.setStatus(500);
		res.getWriter().println(ERROR_STATUS + "(Login is required)");
		log.warning("GeatteInfoGetServlet.doGet() : can not get login user email!!");
		return;
	    }*/

	    res.setContentType("application/json");

	    String geatteId = req.getParameter(Config.GEATTE_ID_PARAM);
	    if (geatteId == null) {
		res.setStatus(400);
		res.getWriter().println(ERROR_STATUS + " ( "+ Config.GEATTE_ID_PARAM +"  parameter missing)");
		log.warning("GeatteInfoGetServlet.doGet() : can not obtain geatte id from request!!");
		return;
	    }

	    log.log(Level.INFO, "GeatteInfoGetServlet.doGet() : Try to retrieve object GeatteInfo for geatteId = " + geatteId);

	    //Key key = KeyFactory.createKey(GeatteInfo.class.getSimpleName(), geatteId);
	    //Object id = JDOHelper.getObjectId(geatteId);

	    // Context-shared PMF.
	    PersistenceManager pm = DBHelper.getPMF(getServletContext()).getPersistenceManager();
	    //Object id = pm.newObjectIdInstance(GeatteInfo.class, geatteId);
	    //if (id == null) {
	    //		log.log(Level.WARNING, "GeatteInfoGetServlet.doGet() : unable to construct key object of GeatteInfo for geatteId = " + geatteId);
	    //	    }

	    GeatteInfo geatte = null;
	    try {
		Long id = Long.parseLong(geatteId);
		geatte = pm.getObjectById(GeatteInfo.class, id);
	    } catch (JDOObjectNotFoundException ex) {
		log.warning("GeatteInfoGetServlet.doGet() : can not obtain geatte from db for id = " + geatteId);
		throw new ServletException(ex);
	    } catch (NumberFormatException nfe) {
		log.warning("GeatteInfoGetServlet.doGet() : wrong format of geatteId = " + geatteId);
		throw new ServletException(nfe);
	    }
	    finally {
		pm.close();
	    }

	    if (geatte != null) {
		JSONObject geatteJson = new JSONObject();
		try {
		    //JSONArray devices = new JSONArray();
		    //JSONObject dijson = new JSONObject(); (geatteid==null ? "" : geatteid), Config.ENCODE_UTF8
		    geatteJson.put(Config.GEATTE_ID_PARAM, URLEncoder.encode(geatte.getId().toString(), Config.ENCODE_UTF8));
		    geatteJson.put(Config.GEATTE_FROM_NUMBER_PARAM, URLEncoder.encode(geatte.getFromNumber(), Config.ENCODE_UTF8));
		    geatteJson.put(Config.GEATTE_TITLE_PARAM, URLEncoder.encode((geatte.getGeatteTitile()==null ? "": geatte.getGeatteTitile()), Config.ENCODE_UTF8));
		    geatteJson.put(Config.GEATTE_DESC_PARAM, URLEncoder.encode((geatte.getGeatteDesc()==null ? "": geatte.getGeatteDesc()), Config.ENCODE_UTF8));
		    geatteJson.put(Config.GEATTE_CREATED_DATE_PARAM, URLEncoder.encode((geatte.getCreatedDateStr()==null ? "": geatte.getCreatedDateStr()), Config.ENCODE_UTF8));

		    PrintWriter out = res.getWriter();
		    geatteJson.write(out);
		    log.log(Level.INFO, "GeatteInfoGetServlet.doGet() : Successfully return geatte info json for id = " + geatte.getId().toString());
		} catch (JSONException e) {
		    throw new IOException(e);
		}
	    } else {
		log.warning("GeatteInfoGetServlet.doGet() : jdo object GeatteInfo is null for id = " + geatteId);
	    }
	    log.log(Level.INFO, "GeatteInfoGetServlet.doGet() : END GeatteInfoGetServlet.doGet()");
	} catch (Exception ex) {
	    throw new ServletException(ex);
	}
    }
}