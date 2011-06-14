package com.geatte.app.server;

import java.io.IOException;
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

import com.google.android.c2dm.server.C2DMessaging;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;

@SuppressWarnings("serial")
public class GeatteVoteServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(GeatteVoteServlet.class.getName());
    private static final String OK_STATUS = "OK";
    private static final String ERROR_STATUS = "ERROR";

    private String mGeatteIdField;
    private String mGeatteVoterField;
    private String mGeatteVoteRespField;
    private String mGeatteFeedbackField;


    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	log.log(Level.INFO, "GeatteVoteServlet.doPOST() : START GeatteVoteServlet.doPOST()");

	resp.setContentType("text/plain");

	GeatteVoteRequestInfo reqInfo = GeatteVoteRequestInfo.processRequest(req, resp, getServletContext());
	if (reqInfo == null) {
	    log.severe("GeatteVoteServlet.doPOST() : can not load RequestInfo!!");
	    return;
	}

	mGeatteIdField = reqInfo.getParameter(Config.GEATTE_ID_PARAM);

	if (mGeatteIdField == null) {
	    resp.setStatus(400);
	    resp.getWriter().println(ERROR_STATUS + "(Must specify " + Config.GEATTE_ID_PARAM + ")");
	    log.severe("GeatteVoteServlet.doPOST() : Missing geatte id, " + Config.GEATTE_ID_PARAM + " is null");
	    return;
	} else {
	    log.log(Level.INFO, "GeatteVoteServlet.doPOST() : user sent a " + Config.GEATTE_ID_PARAM + " = " + mGeatteIdField);
	}

	mGeatteVoterField = reqInfo.getParameter(Config.FRIEND_GEATTE_VOTER);

	if (mGeatteVoterField == null) {
	    resp.setStatus(400);
	    resp.getWriter().println(ERROR_STATUS + "(Must specify " + Config.FRIEND_GEATTE_VOTER + ")");
	    log.severe("GeatteVoteServlet.doPOST() : Missing geatte voter, " + Config.FRIEND_GEATTE_VOTER + " is null");
	    return;
	} else {
	    log.log(Level.INFO, "GeatteVoteServlet.doPOST() : user sent a " + Config.FRIEND_GEATTE_VOTER + " = "
		    + mGeatteVoterField);
	}

	mGeatteVoteRespField = reqInfo.getParameter(Config.FRIEND_GEATTE_VOTE_RESP);

	if (mGeatteVoteRespField == null) {
	    resp.setStatus(400);
	    resp.getWriter().println(ERROR_STATUS + "(Must specify " + Config.FRIEND_GEATTE_VOTE_RESP + ")");
	    log.severe("GeatteVoteServlet.doPOST() : Missing geatte vote resp, " + Config.FRIEND_GEATTE_VOTE_RESP + " is null");
	    return;
	} else {
	    log.log(Level.INFO, "GeatteVoteServlet.doPOST() : user sent a " + Config.FRIEND_GEATTE_VOTE_RESP + " = "
		    + mGeatteVoteRespField);
	}

	mGeatteFeedbackField = reqInfo.getParameter(Config.FRIEND_GEATTE_FEEDBACK);

	if (mGeatteFeedbackField != null) {
	    log.log(Level.INFO, "GeatteVoteServlet.doPOST() : user sent a " + Config.FRIEND_GEATTE_FEEDBACK + " = "
		    + mGeatteFeedbackField);
	}

	//after save to db, send geatte vote to owner
	String geatteVoteId = null;
	if ((geatteVoteId = saveToDb(resp)) != null ) {
	    String geatteOwnerNumber = getGeatteVoteOwnerNumber(resp);
	    log.log(Level.INFO, "GeatteVoteServlet.doPOST() : ready to send geatte id = " + mGeatteIdField + ", geatte vote '" + geatteVoteId  + "' to owner phoneNumber = " + geatteOwnerNumber);

	    // the message push to device
	    Map<String, String[]> params = new HashMap<String, String[]>();
	    params.put("data.geatteid_vote", new String[]{mGeatteIdField});
	    params.put("data.geatte_voter", new String[]{mGeatteVoterField});
	    params.put("data.geatte_vote_resp", new String[]{mGeatteVoteRespField});
	    params.put("data.geatte_vote_feedback", new String[]{mGeatteFeedbackField});

	    submitGeatteVoteTask(geatteOwnerNumber, params);
	    log.log(Level.INFO, "GeatteVoteServlet.doPOST() : sent geatte id = " + mGeatteIdField + ",  geatte vote '" + geatteVoteId  + "' to owner phoneNumber = " + geatteOwnerNumber);
	}


	log.log(Level.INFO, "GeatteVoteServlet.doPOST() : END GeatteVoteServlet.doPOST()");

    }

    private String saveToDb(HttpServletResponse resp) throws IOException {
	// Context-shared PMF.
	PersistenceManager pm = DBHelper.getPMF(getServletContext()).getPersistenceManager();

	try {
	    // Create new GeatteVote
	    GeatteVote geatteVote = null;

	    log.log(Level.INFO, "GeatteVoteServlet.saveToDb() : create a new GeatteVote");

	    geatteVote = new GeatteVote(mGeatteIdField, mGeatteVoterField, mGeatteVoteRespField, mGeatteFeedbackField);

	    pm.makePersistent(geatteVote);

	    log.log(Level.INFO, "GeatteVoteServlet.saveToDb() : Saved GeatteVote geatteId = " + mGeatteIdField
		    + ", friendGeatteVoter = " + mGeatteVoterField + ", friendGeatteVoteResp = " + mGeatteVoteRespField
		    + ", friendGeatteFeedback = " + mGeatteFeedbackField + ", id = " + geatteVote.getId().toString());

	    return geatteVote.getId().toString();

	} catch (Exception e) {
	    resp.setStatus(400);
	    resp.getWriter().println(ERROR_STATUS + " (Error saving geatteVote)");
	    log.log(Level.SEVERE, "GeatteVoteServlet.saveToDb() : Error saving GeatteVoteServlet.saveToDb.", e);
	} finally {
	    pm.close();
	}
	return null;
    }

    private String getGeatteVoteOwnerNumber(HttpServletResponse resp) throws ServletException, IOException {
	// Context-shared PMF.
	PersistenceManager pm = DBHelper.getPMF(getServletContext()).getPersistenceManager();

	GeatteInfo geatte = null;
	try {
	    Long id = Long.parseLong(mGeatteIdField);
	    geatte = pm.getObjectById(GeatteInfo.class, id);

	    resp.getWriter().println(OK_STATUS);
	    return geatte.getFromNumber();
	} catch (JDOObjectNotFoundException ex) {
	    resp.setStatus(400);
	    resp.getWriter().println(ERROR_STATUS + " (can not obtain geatte from db)");
	    log.warning("GeatteVoteServlet.getGeatteVoteOwnerNumber() : can not obtain geatte from db for id = " + mGeatteIdField);
	    throw new ServletException(ex);
	} catch (NumberFormatException nfe) {
	    resp.setStatus(400);
	    resp.getWriter().println(ERROR_STATUS + " (wrong format of geatteId)");
	    log.warning("GeatteVoteServlet.getGeatteVoteOwnerNumber() : wrong format of geatteId = " + mGeatteIdField);
	    throw new ServletException(nfe);
	} finally {
	    pm.close();
	}

    }

    private void submitGeatteVoteTask(String ownerNumber, Map<String, String[]> params) {
	log.log(Level.INFO, "GeatteVoteServlet.submitGeatteVoteTask() : START submit geatte vote to " + ownerNumber);
	boolean delayWhileIdle = true;
	String collapseKey = Integer.toString((int)((Math.random()*5000) + 5001));
	Queue dmQueue = QueueFactory.getQueue("geatteVote");
	//Queue dmQueue = QueueFactory.getDefaultQueue();
	try {
	    TaskOptions url = TaskOptions.Builder.withUrl(GeatteSendServlet.URI)
	    .param(Config.GEATTE_TO_NUMBER_PARAM, ownerNumber).param(C2DMessaging.PARAM_COLLAPSE_KEY, collapseKey);
	    if (delayWhileIdle) {
		url.param(C2DMessaging.PARAM_DELAY_WHILE_IDLE, "1");
	    }
	    for (String key : params.keySet()) {
		String[] values = params.get(key);
		url.param(key, URLEncoder.encode(values[0], Config.ENCODE_UTF8));
	    }

	    // Task queue implements the exponential backoff
	    //long jitter = (int) Math.random() * DATAMESSAGING_MAX_JITTER_MSEC;
	    //url.countdownMillis(jitter);

	    TaskHandle add = dmQueue.add(url);
	    log.log(Level.INFO, "GeatteVoteServlet.submitGeatteVoteTask() : add one task to queue, url = " + url.getUrl());
	} catch (UnsupportedEncodingException e) {
	    // Ignore - UTF8 should be supported
	    log.log(Level.SEVERE, "GeatteVoteServlet.submitGeatteVoteTask : Unexpected error", e);
	}
	log.log(Level.INFO, "GeatteVoteServlet.submitGeatteVoteTask() : END submit geatte vote to " + ownerNumber);

    }

}
