package com.geatte.app.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class GeatteAllGetServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(GeatteAllGetServlet.class.getName());
    private static final String ERROR_STATUS = "ERROR";

    /**
     * For debug - and possibly show the info, allow device selection.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	log.log(Level.INFO, "GeatteAllGetServlet.doGet() : START GeatteAllGetServlet.doGet()");
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

	    String phoneNumber = req.getParameter(Config.DEV_PHONE_NUMBER_PARAM);
	    String countryCode = req.getParameter(Config.DEV_PHONE_COUNTRY_ISO_PARAM);
	    if (phoneNumber == null) {
		res.setStatus(400);
		res.getWriter().println(ERROR_STATUS + " ( "+ Config.DEV_PHONE_NUMBER_PARAM +"  parameter missing)");
		log.warning("GeatteAllGetServlet.doGet() : can not obtain phoneNumber from request!!");
		return;
	    }

	    log.log(Level.INFO, "GeatteAllGetServlet.doGet() : Try to retrieve object All Info for phoneNumber = " + phoneNumber);


	    // Context-shared PMF.
	    PersistenceManager pm = DBHelper.getPMF(getServletContext()).getPersistenceManager();

	    List<GeatteInfo> allItemInfoFromMe = null;
	    List<GeatteInfo> allItemInfoToMe = null;
	    List<GeatteVote> allVoteFromMe = null;
	    try {
		allItemInfoFromMe = GeatteInfo.getAllItemInfoFromNumber(pm, phoneNumber, countryCode);
		allItemInfoToMe = GeatteInfo.getAllItemInfoToNumber(pm, phoneNumber, countryCode);
		allVoteFromMe = GeatteVote.getAllVoteFromMe(pm, phoneNumber, countryCode);
	    } catch (JDOObjectNotFoundException ex) {
		log.warning("GeatteAllGetServlet.doGet() : can not obtain geatte from db for phoneNumber = " + phoneNumber);
	    }
	    finally {
		pm.close();
	    }

	    JSONObject retObject = new JSONObject();
	    JSONArray fromMeArray = new JSONArray();
	    JSONArray toMeArray = new JSONArray();
	    JSONArray fromMeVoteArray = new JSONArray();
	    JSONArray toMeVoteArray = new JSONArray();

	    List<String> listOfIds = new ArrayList<String>();
	    if (allItemInfoFromMe != null) {
		for (GeatteInfo myItem : allItemInfoFromMe) {
		    JSONObject jObject = new JSONObject();
		    try {
			jObject.put(Config.GEATTE_ID_PARAM, URLEncoder.encode(myItem.getId().toString(), Config.ENCODE_UTF8));
			listOfIds.add(myItem.getId().toString());
			jObject.put(Config.GEATTE_FROM_NUMBER_PARAM, URLEncoder.encode(myItem.getFromNumber(), Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_TO_NUMBER_PARAM, URLEncoder.encode(myItem.getToNumber(), Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_TITLE_PARAM, URLEncoder.encode((myItem.getGeatteTitile()==null ? "": myItem.getGeatteTitile()), Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_DESC_PARAM, URLEncoder.encode((myItem.getGeatteDesc()==null ? "": myItem.getGeatteDesc()), Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_CREATED_DATE_PARAM, URLEncoder.encode((myItem.getCreatedDateStr()==null ? "": myItem.getCreatedDateStr()), Config.ENCODE_UTF8));
			fromMeArray.put(jObject);
			log.log(Level.INFO, "GeatteAllGetServlet.doGet() : Successfully return geatte info from me json for geatteId = " + myItem.getId().toString());
		    } catch (JSONException e) {
			log.warning("GeatteAllGetServlet.doGet() : json processing error for my items geatteId = " + myItem.getId().toString());
		    }

		}
	    }

	    if (allItemInfoToMe != null){
		for (GeatteInfo sentToMeItem : allItemInfoToMe) {
		    JSONObject jObject = new JSONObject();
		    try {
			jObject.put(Config.GEATTE_ID_PARAM, URLEncoder.encode(sentToMeItem.getId().toString(), Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_FROM_NUMBER_PARAM, URLEncoder.encode(sentToMeItem.getFromNumber(), Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_TO_NUMBER_PARAM, URLEncoder.encode(sentToMeItem.getToNumber(), Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_TITLE_PARAM, URLEncoder.encode((sentToMeItem.getGeatteTitile()==null ? "": sentToMeItem.getGeatteTitile()), Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_DESC_PARAM, URLEncoder.encode((sentToMeItem.getGeatteDesc()==null ? "": sentToMeItem.getGeatteDesc()), Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_CREATED_DATE_PARAM, URLEncoder.encode((sentToMeItem.getCreatedDateStr()==null ? "": sentToMeItem.getCreatedDateStr()), Config.ENCODE_UTF8));
			toMeArray.put(jObject);
			log.log(Level.INFO, "GeatteAllGetServlet.doGet() : Successfully return geatte info to me json for geatteId = " + sentToMeItem.getId().toString());
		    } catch (JSONException e) {
			log.warning("GeatteAllGetServlet.doGet() : json processing error for friends' items geatteId = " + sentToMeItem.getId().toString());
		    }

		}

	    }

	    if (allVoteFromMe != null){
		for (GeatteVote fromMeVote : allVoteFromMe) {
		    JSONObject jObject = new JSONObject();
		    try {
			jObject.put(Config.GEATTE_ID_PARAM, URLEncoder.encode(fromMeVote.getGeatteId(), Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_VOTER, URLEncoder.encode(fromMeVote.getGeatteVoter(), Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_RESP, URLEncoder.encode(fromMeVote.getGeatteVoteResp(), Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_FEEDBACK, URLEncoder.encode((fromMeVote.getGeatteFeedback()==null ? "": fromMeVote.getGeatteFeedback()), Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_CREATED_DATE, URLEncoder.encode(fromMeVote.getCreatedDateStr(), Config.ENCODE_UTF8));
			fromMeVoteArray.put(jObject);
			log.log(Level.INFO, "GeatteAllGetServlet.doGet() : Successfully return geatte vote from me json for geatteId = " + fromMeVote.getGeatteId());
		    } catch (JSONException e) {
			log.warning("GeatteAllGetServlet.doGet() : json processing error for geatte vote from me geatteId = " + fromMeVote.getGeatteId());
		    }

		}

	    }
	    retObject.put("my_items", fromMeArray);
	    retObject.put("friend_items", toMeArray);
	    retObject.put("my_votes", fromMeVoteArray);

	    pm = DBHelper.getPMF(getServletContext()).getPersistenceManager();
	    List<GeatteVote> allVoteToMe = null;
	    try {
		allVoteToMe = GeatteVote.getAllVoteToMe(pm, listOfIds);
	    } catch (JDOObjectNotFoundException ex) {
		log.warning("GeatteAllGetServlet.doGet() : can not obtain geatte votes from db for a list of item ids");
	    }
	    finally {
		pm.close();
	    }

	    if (allVoteToMe != null){
		for (GeatteVote toMeVote : allVoteToMe) {
		    JSONObject jObject = new JSONObject();
		    try {
			jObject.put(Config.GEATTE_ID_PARAM, URLEncoder.encode(toMeVote.getGeatteId(), Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_VOTER, URLEncoder.encode(toMeVote.getGeatteVoter(), Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_RESP, URLEncoder.encode(toMeVote.getGeatteVoteResp(), Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_FEEDBACK, URLEncoder.encode((toMeVote.getGeatteFeedback()==null ? "": toMeVote.getGeatteFeedback()), Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_CREATED_DATE, URLEncoder.encode(toMeVote.getCreatedDateStr(), Config.ENCODE_UTF8));
			toMeVoteArray.put(jObject);
			log.log(Level.INFO, "GeatteAllGetServlet.doGet() : Successfully return geatte vote to me json for geatteId = " + toMeVote.getGeatteId());
		    } catch (JSONException e) {
			log.warning("GeatteAllGetServlet.doGet() : json processing error for geatte vote to me geatteId = " + toMeVote.getGeatteId());
		    }
		}
	    }

	    retObject.put("friend_votes", toMeVoteArray);

	    log.log(Level.INFO, "GeatteAllGetServlet.doGet() : Send back to client = " + retObject);

	    PrintWriter out = res.getWriter();
	    retObject.write(out);
	    log.log(Level.INFO, "GeatteAllGetServlet.doGet() : END GeatteAllGetServlet.doGet()");
	} catch (Exception ex) {
	    throw new ServletException(ex);
	}
    }
}