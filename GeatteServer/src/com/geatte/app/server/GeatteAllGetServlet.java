package com.geatte.app.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.geatte.app.shared.CommonUtils;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

/**
 * This API is servicing for application to sync data from server. There are 4 types of data to get from this service.
 * my_items : items caller created and sent to friends
 * my_votes : votes caller sent to friend's items
 * friend_items : items friends sent to caller
 * friend_votes : votes from friends to comment on caller's items
 * </p>
 * API accepts 4 parameters :
 * phoneNumber : number to search on for all data, this number is phone number from caller.
 * countryCode* : country code from caller.
 * lastSyncDate* : last sync date time to download data from server. If not given, caller will get all historical data.
 *        Format : 'yyyy-MM-dd HH:mm:ss'
 *        Sample : '2011-01-01 01:00:00'
 * reqType* : type of data caller is interested to retrieve from server.
 *        Format : my_items my_votes friend_items friend_votes, separated by semicolon ';'.
 *        Sample : 'my_items;friend_items' gets caller's items and items sent to him/her.
 * 
 * '*' means parameter is optional.
 */
@SuppressWarnings("serial")
public class GeatteAllGetServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(GeatteAllGetServlet.class.getName());
    private static final String ERROR_STATUS = "ERROR";
    public static enum REQ_TYPE {MY_ITEMS, MY_VOTES, FRIEND_ITEMS, FRIEND_VOTES}

    /**
     * For debug - and possibly show the info, allow device selection.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	log.log(Level.INFO, "GeatteAllGetServlet.doGet() : START GeatteAllGetServlet.doGet()");
	try {
	    // Try ClientLogin
	    /*
	     * UserService userService = UserServiceFactory.getUserService();
	     * User user = userService.getCurrentUser(); if (user != null) {
	     * mUserEmail = user.getEmail();log.log(Level.INFO,
	     * "GeatteInfoGetServlet.doGet() : get user email thru ClientLogin :"
	     * + mUserEmail); } if (mUserEmail == null) { res.setStatus(500);
	     * res.getWriter().println(ERROR_STATUS + "(Login is required)");
	     * log.warning(
	     * "GeatteInfoGetServlet.doGet() : can not get login user email!!");
	     * return; }
	     */

	    res.setContentType("application/json");

	    boolean isMyItems = false;
	    boolean isMyVotes = false;
	    boolean isFriendItems = false;
	    boolean isFriendVotes = false;

	    String reqTypeStr = req.getParameter(Config.QUERY_REQ_TYPE_PARAM);

	    if(reqTypeStr == null) {
		isMyItems = isMyVotes = isFriendItems = isFriendVotes = true;
	    }
	    else {
		reqTypeStr = URLDecoder.decode((reqTypeStr==null ? "" : reqTypeStr), Config.ENCODE_UTF8);
		reqTypeStr = reqTypeStr.toUpperCase();
		List<String> reqTypes = CommonUtils.splitStringBySemiColon(reqTypeStr);
		for (String reqType : reqTypes) {
		    log.log(Level.FINEST, "GeatteAllGetServlet.doGet() : get regType = " + reqType);
		    if (reqType.equals(REQ_TYPE.MY_ITEMS.toString())) {
			isMyItems = true;
		    } else if (reqType.equals(REQ_TYPE.MY_VOTES.toString())) {
			isMyVotes = true;
		    } else if (reqType.equals(REQ_TYPE.FRIEND_ITEMS.toString())) {
			isFriendItems = true;
		    } else if (reqType.equals(REQ_TYPE.FRIEND_VOTES.toString())) {
			isFriendVotes = true;
		    }
		}
	    }

	    String phoneNumber = req.getParameter(Config.DEV_PHONE_NUMBER_PARAM);
	    phoneNumber = URLDecoder.decode((phoneNumber==null ? "" : phoneNumber), Config.ENCODE_UTF8);
	    String countryCode = req.getParameter(Config.DEV_PHONE_COUNTRY_ISO_PARAM);
	    countryCode = URLDecoder.decode((countryCode==null ? "us" : countryCode), Config.ENCODE_UTF8);
	    String lastSyncDateStr = req.getParameter(Config.QUERY_LAST_SYNC_DATE_PARAM);
	    lastSyncDateStr = URLDecoder.decode((lastSyncDateStr==null ? "" : lastSyncDateStr), Config.ENCODE_UTF8);

	    if (phoneNumber == null || phoneNumber.isEmpty()) {
		res.setStatus(400);
		res.getWriter().println(ERROR_STATUS + " ( " + Config.DEV_PHONE_NUMBER_PARAM + "  parameter missing)");
		log.warning("GeatteAllGetServlet.doGet() : can not obtain phoneNumber from request!!");
		return;
	    }

	    Date lastSyncDate = null;
	    if (lastSyncDateStr == null || lastSyncDateStr.isEmpty()) {
		lastSyncDateStr = "2011-01-01 01:00:00";
	    }
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    try {
		lastSyncDate = (Date) dateFormat.parse(lastSyncDateStr);
	    } catch (ParseException pe) {
		log.log(Level.WARNING,
			"GeatteAllGetServlet.doGet() : lastSyncDate format is wrong, format should be 'yyyy-MM-dd HH:mm:ss', lastSyncDate = "
			+ lastSyncDateStr, pe);
		lastSyncDate = (Date) dateFormat.parse("2011-01-01 01:00:00");
	    }

	    log.log(Level.INFO, "GeatteAllGetServlet.doGet() : Try to retrieve object All Info for phoneNumber = "
		    + phoneNumber);

	    PersistenceManager pm = DBHelper.getPMF(getServletContext()).getPersistenceManager();

	    List<GeatteInfo> allItemInfoFromMe = null;
	    List<GeatteInfo> allItemInfoToMe = null;
	    List<GeatteVote> allVoteFromMe = null;
	    try {
		if (isMyItems) {
		    allItemInfoFromMe = GeatteInfo.getAllItemInfoFromNumber(pm, phoneNumber, countryCode, lastSyncDate);
		}
		if (isFriendItems) {
		    allItemInfoToMe = GeatteInfo.getAllItemInfoToNumber(pm, phoneNumber, countryCode, lastSyncDate);
		}
		if (isMyVotes) {
		    allVoteFromMe = GeatteVote.getAllVoteFromMe(pm, phoneNumber, countryCode, lastSyncDate);
		}
	    } catch (JDOObjectNotFoundException ex) {
		log.warning("GeatteAllGetServlet.doGet() : can not obtain geatte from db for phoneNumber = "
			+ phoneNumber);
	    } finally {
		pm.close();
	    }

	    JSONObject retObject = new JSONObject();
	    JSONArray fromMeArray = new JSONArray();
	    JSONArray toMeArray = new JSONArray();
	    JSONArray fromMeVoteArray = new JSONArray();
	    JSONArray toMeVoteArray = new JSONArray();


	    if (allItemInfoFromMe != null) {
		for (GeatteInfo myItem : allItemInfoFromMe) {
		    JSONObject jObject = new JSONObject();
		    try {
			jObject.put(Config.GEATTE_ID_PARAM, URLEncoder.encode(myItem.getId().toString(),
				Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_FROM_NUMBER_PARAM, URLEncoder.encode(myItem.getFromNumber(),
				Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_TO_NUMBER_PARAM, URLEncoder.encode(myItem.getToNumber(),
				Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_TITLE_PARAM, URLEncoder.encode((myItem.getGeatteTitile() == null ? ""
				: myItem.getGeatteTitile()), Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_DESC_PARAM, URLEncoder.encode((myItem.getGeatteDesc() == null ? ""
				: myItem.getGeatteDesc()), Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_CREATED_DATE_PARAM, URLEncoder.encode(
				(myItem.getCreatedDateStr() == null ? "" : myItem.getCreatedDateStr()),
				Config.ENCODE_UTF8));
			fromMeArray.put(jObject);
		    } catch (JSONException e) {
			log.warning("GeatteAllGetServlet.doGet() : json processing error for my items geatteId = "
				+ myItem.getId().toString());
		    }
		}
		retObject.put("my_items", fromMeArray);
	    }

	    if (allItemInfoToMe != null) {
		for (GeatteInfo sentToMeItem : allItemInfoToMe) {
		    JSONObject jObject = new JSONObject();
		    try {
			jObject.put(Config.GEATTE_ID_PARAM, URLEncoder.encode(sentToMeItem.getId().toString(),
				Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_FROM_NUMBER_PARAM, URLEncoder.encode(sentToMeItem.getFromNumber(),
				Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_TO_NUMBER_PARAM, URLEncoder.encode(sentToMeItem.getToNumber(),
				Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_TITLE_PARAM, URLEncoder.encode(
				(sentToMeItem.getGeatteTitile() == null ? "" : sentToMeItem.getGeatteTitile()),
				Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_DESC_PARAM, URLEncoder.encode(
				(sentToMeItem.getGeatteDesc() == null ? "" : sentToMeItem.getGeatteDesc()),
				Config.ENCODE_UTF8));
			jObject.put(Config.GEATTE_CREATED_DATE_PARAM, URLEncoder.encode((sentToMeItem
				.getCreatedDateStr() == null ? "" : sentToMeItem.getCreatedDateStr()),
				Config.ENCODE_UTF8));
			toMeArray.put(jObject);
		    } catch (JSONException e) {
			log
			.warning("GeatteAllGetServlet.doGet() : json processing error for friends' items geatteId = "
				+ sentToMeItem.getId().toString());
		    }
		}
		retObject.put("friend_items", toMeArray);
	    }

	    if (allVoteFromMe != null) {
		for (GeatteVote fromMeVote : allVoteFromMe) {
		    JSONObject jObject = new JSONObject();
		    try {
			jObject.put(Config.GEATTE_ID_PARAM, URLEncoder.encode(fromMeVote.getGeatteId(),
				Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_VOTER, URLEncoder.encode(fromMeVote.getGeatteVoter(),
				Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_TO, URLEncoder.encode(fromMeVote.getGeatteOwner(),
				Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_RESP, URLEncoder.encode(fromMeVote.getGeatteVoteResp(),
				Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_FEEDBACK, URLEncoder.encode(
				(fromMeVote.getGeatteFeedback() == null ? "" : fromMeVote.getGeatteFeedback()),
				Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_CREATED_DATE, URLEncoder.encode(fromMeVote.getCreatedDateStr(),
				Config.ENCODE_UTF8));
			fromMeVoteArray.put(jObject);
		    } catch (JSONException e) {
			log.warning("GeatteAllGetServlet.doGet() : json processing error for geatte vote from me geatteId = "
				+ fromMeVote.getGeatteId());
		    }

		}
		retObject.put("my_votes", fromMeVoteArray);
	    }

	    pm = DBHelper.getPMF(getServletContext()).getPersistenceManager();
	    List<GeatteVote> allVoteToMe = null;
	    try {
		if (isFriendVotes) {
		    allVoteToMe = GeatteVote.getAllVoteToMe(pm, phoneNumber, countryCode, lastSyncDate);
		}
	    } catch (JDOObjectNotFoundException ex) {
		log.warning("GeatteAllGetServlet.doGet() : can not obtain geatte votes from db for a list of item ids");
	    } finally {
		pm.close();
	    }

	    if (allVoteToMe != null) {
		for (GeatteVote toMeVote : allVoteToMe) {
		    JSONObject jObject = new JSONObject();
		    try {
			jObject.put(Config.GEATTE_ID_PARAM, URLEncoder.encode(toMeVote.getGeatteId(),
				Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_VOTER, URLEncoder
				.encode(toMeVote.getGeatteVoter(), Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_TO, URLEncoder.encode(toMeVote.getGeatteOwner(),
				Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_RESP, URLEncoder.encode(toMeVote.getGeatteVoteResp(),
				Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_FEEDBACK, URLEncoder.encode((toMeVote.getGeatteFeedback() == null ? ""
				: toMeVote.getGeatteFeedback()), Config.ENCODE_UTF8));
			jObject.put(Config.VOTE_CREATED_DATE, URLEncoder.encode(toMeVote.getCreatedDateStr(),
				Config.ENCODE_UTF8));
			toMeVoteArray.put(jObject);
		    } catch (JSONException e) {
			log.warning("GeatteAllGetServlet.doGet() : json processing error for geatte vote to me geatteId = "
				+ toMeVote.getGeatteId());
		    }
		}
		retObject.put("friend_votes", toMeVoteArray);
	    }

	    log.log(Level.INFO, "GeatteAllGetServlet.doGet() : Send back to client = " + retObject);

	    PrintWriter out = res.getWriter();
	    retObject.write(out);
	    log.log(Level.INFO, "GeatteAllGetServlet.doGet() : END GeatteAllGetServlet.doGet()");
	} catch (Exception ex) {
	    throw new ServletException(ex);
	}
    }
}