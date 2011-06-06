package com.geatte.app.server;

import java.io.IOException;
import java.io.Reader;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

/**
 * Common code and helpers to handle a request and manipulate contacts filter request
 * 
 */
public class GeatteContactsRequestInfo {
    private static final Logger log = Logger.getLogger(GeatteContactsRequestInfo.class.getName());
    private static final String ERROR_STATUS = "ERROR";
    private static final String LOGIN_REQUIRED_STATUS = "LOGIN_REQUIRED";
    private static final String JSON_REQUIRED_STATUS = "JSON_REQUIRED";

    private String userEmail;

    private ServletContext ctx;

    private GeatteContactsRequestInfo() {
    }

    public GeatteContactsRequestInfo(String userEmail, ServletContext ctx) {
	this.userEmail = userEmail;
	this.ctx = ctx;
    }

    JSONObject jsonParams;

    public boolean isAuth() {
	return userEmail != null;
    }

    /**
     * Authenticate the user, check headers and pull the contacts json data.
     * 
     * @return null if authentication fails.
     * @throws IOException
     */
    public static GeatteContactsRequestInfo processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx)
    throws IOException {

	// Basic XSRF protection
	if (req.getHeader("X-Same-Domain") == null) {
	    resp.setStatus(400);
	    resp.getWriter().println(ERROR_STATUS + " (Missing X-Same-Domain header)");
	    log.warning("RequestInfo:processRequest() : Missing X-Same-Domain");
	    return null;
	}

	User user = null;
	GeatteContactsRequestInfo reqInfo = new GeatteContactsRequestInfo();
	reqInfo.ctx = ctx;
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
	    log.info("GeatteContactsRequestInfo:processRequest() : user is null, try ClientLogin");
	    // Try ClientLogin
	    UserService userService = UserServiceFactory.getUserService();
	    user = userService.getCurrentUser();
	    if (user != null) {
		reqInfo.userEmail = user.getEmail();
		log.info("GeatteContactsRequestInfo:processRequest() : get user email thru ClientLogin :" + reqInfo.userEmail);
	    }
	}

	if ("application/json".equals(req.getContentType())) {
	    Reader reader = req.getReader();
	    // where is readFully ?
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
		log.info("GeatteContactsRequestInfo:processRequest() : reqInfo.jsonParams :" + reqInfo.jsonParams.toString());
	    } catch (JSONException e) {
		resp.setStatus(500);
		return null;
	    }
	} else {
	    resp.setStatus(400);
	    resp.getWriter().println(JSON_REQUIRED_STATUS);
	    log.warning("GeatteContactsRequestInfo:processRequest() : can not get json from request!!");
	    return null;
	}

	if (reqInfo.userEmail == null) {
	    resp.setStatus(400);
	    resp.getWriter().println(LOGIN_REQUIRED_STATUS);
	    log.warning("GeatteContactsRequestInfo:processRequest() : can not get login user email!!");
	    return null;
	}

	return reqInfo;
    }

    public String getParameterStr(String name) {
	if (jsonParams != null) {
	    return jsonParams.optString(name, null);
	}
	return null;
    }

    public JSONArray getParameterJSONArray(String name) {
	if (jsonParams != null) {
	    return jsonParams.optJSONArray(name);
	}
	return null;
    }

    @Override
    public String toString() {
	return userEmail + " " + " " + jsonParams;
    }

    public String getUserEmail() {
	return userEmail;
    }

    public void setUserEmail(String userEmail) {
	this.userEmail = userEmail;
    }

}
