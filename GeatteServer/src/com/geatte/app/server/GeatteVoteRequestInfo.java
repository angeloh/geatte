package com.geatte.app.server;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

/**
 * Common code and helpers to handle a request for geatte vote request
 * 
 */
public class GeatteVoteRequestInfo {
    private static final Logger log = Logger.getLogger(GeatteVoteRequestInfo.class.getName());
    private static final String ERROR_STATUS = "ERROR";
    private static final String LOGIN_REQUIRED_STATUS = "LOGIN_REQUIRED";

    private String mUserEmail;

    private String mGeatteId;
    private String mGeatteVoter;
    private String mGeatteVoteResp;
    private String mGeatteFeedback;

    private GeatteVoteRequestInfo() {
    }

    public GeatteVoteRequestInfo(String userEmail, ServletContext ctx) {
	this.mUserEmail = userEmail;
    }

    // Request parameters - transitioning to JSON, but need to support existing code.
    Map<String, String[]> parameterMap;
    JSONObject jsonParams;

    /**
     * Authenticate the user, check headers and pull the geatte vote data.
     * 
     * @return null if authentication fails.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static GeatteVoteRequestInfo processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx)
    throws IOException {

	// Basic XSRF protection
	if (req.getHeader("X-Same-Domain") == null) {
	    resp.setStatus(400);
	    resp.getWriter().println(ERROR_STATUS + " (Missing X-Same-Domain header)");
	    log.warning("GeatteVoteRequestInfo:processRequest() : Missing X-Same-Domain");
	    return null;
	}

	User user = null;
	GeatteVoteRequestInfo reqInfo = new GeatteVoteRequestInfo();
	//	OAuthService oauthService = OAuthServiceFactory.getOAuthService();
	//	try {
	//	    user = oauthService.getCurrentUser();
	//	    if (user != null) {
	//		ri.userName = user.getEmail();
	//	    }
	//	} catch (Throwable t) {
	//	    log.log(Level.SEVERE, "Oauth error ", t);
	//	    user = null;
	//	}

	if (user == null) {
	    log.info("GeatteVoteRequestInfo:processRequest() : user is null, try ClientLogin");
	    // Try ClientLogin
	    UserService userService = UserServiceFactory.getUserService();
	    user = userService.getCurrentUser();
	    if (user != null) {
		reqInfo.mUserEmail = user.getEmail();
		log.info("GeatteVoteRequestInfo:processRequest() : get user email thru ClientLogin :" + reqInfo.mUserEmail);
	    }
	}

	if ("application/json".equals(req.getContentType())) {
	    Reader reader = req.getReader();
	    char[] tmp = new char[2048];
	    StringBuffer body = new StringBuffer();
	    while (true) {
		int cnt = reader.read(tmp);
		if (cnt <= 0) {
		    break;
		}
		body.append(tmp, 0, cnt);
	    }
	    try {
		reqInfo.jsonParams = new JSONObject(body.toString());
		log.info("GeatteVoteRequestInfo:processRequest() : reqInfo.jsonParams :" + reqInfo.jsonParams.toString());
	    } catch (JSONException e) {
		resp.setStatus(500);
		return null;
	    }
	} else {
	    reqInfo.parameterMap = req.getParameterMap();

	    log.info("GeatteVoteRequestInfo:processRequest() : reqInfo.parameterMap :" + reqInfo.parameterMap.toString());
	}

	reqInfo.mGeatteId = reqInfo.getParameter(Config.GEATTE_ID_PARAM);
	log.info("GeatteVoteRequestInfo:processRequest() : reqInfo.geatteId = " + reqInfo.mGeatteId);
	if (reqInfo.mGeatteId != null) {
	    reqInfo.mGeatteId = reqInfo.mGeatteId.trim();
	    if ("".equals(reqInfo.mGeatteId)) {
		reqInfo.mGeatteId = null;
	    }
	    log.info("GeatteVoteRequestInfo:processRequest() : after trim() reqInfo.geatteId = " + reqInfo.mGeatteId);
	}

	if (reqInfo.mUserEmail == null) {
	    resp.setStatus(400);
	    resp.getWriter().println(LOGIN_REQUIRED_STATUS);
	    log.warning("GeatteVoteRequestInfo:processRequest() : can not get login user email!!");
	    return null;
	}

	reqInfo.mGeatteVoter = reqInfo.getParameter(Config.FRIEND_GEATTE_VOTER);
	reqInfo.mGeatteVoteResp = reqInfo.getParameter(Config.FRIEND_GEATTE_VOTE_RESP);
	reqInfo.mGeatteFeedback = reqInfo.getParameter(Config.FRIEND_GEATTE_FEEDBACK);

	return reqInfo;
    }

    public String getParameter(String name) {
	if (jsonParams != null) {
	    return jsonParams.optString(name, null);
	} else {
	    String res[] = parameterMap.get(name);
	    if (res == null || res.length == 0) {
		return null;
	    }
	    return res[0];
	}
    }


    @Override
    public String toString() {
	return mGeatteId + " " + mGeatteVoter + " " + mGeatteVoteResp;
    }

    public boolean isAuth() {
	return mUserEmail != null;
    }

    public String getUserEmail() {
	return mUserEmail;
    }

    public void setUserEmail(String userEmail) {
	this.mUserEmail = userEmail;
    }

    public String getGeatteId() {
	return mGeatteId;
    }

    public String getGeatteVoter() {
	return mGeatteVoter;
    }

    public String getGeatteVoteResp() {
	return mGeatteVoteResp;
    }

    public String getGeatteFeedback() {
	return mGeatteFeedback;
    }

}
