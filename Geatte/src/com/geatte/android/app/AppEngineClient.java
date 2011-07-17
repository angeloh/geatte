package com.geatte.android.app;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * AppEngine client. Handles auth.
 */
public class AppEngineClient {

    private static final String AUTH_URL = Config.BASE_URL + "/_ah/login";
    private static final String AUTH_TOKEN_TYPE = "ah";

    private final Context mContext;
    private final String mUserEmail;

    public AppEngineClient(Context context, String accountName) {
	this.mContext = context;
	this.mUserEmail = accountName;
    }

    public HttpResponse makeRequestNoAuth(String urlPath, List<NameValuePair> params) throws Exception {
	HttpResponse res = makeRequestNoRetryNoAuth(urlPath, params);
	if (res.getStatusLine().getStatusCode() == 500) {
	    res = makeRequestNoRetryNoAuth(urlPath, params);
	}
	return res;
    }

    public HttpResponse makeRequestWithParams(String urlPath, List<NameValuePair> params) throws Exception {
	HttpResponse res = makeRequestNoRetryWithParams(urlPath, params, false);
	if (res.getStatusLine().getStatusCode() == 500) {
	    res = makeRequestNoRetryWithParams(urlPath, params, true);
	}
	return res;
    }

    public HttpResponse makeRequestWithEntity(String urlPath,  HttpEntity httpEntity) throws Exception {
	HttpResponse res = makeRequestNoRetryWithEntity(urlPath, httpEntity, false);
	if (res.getStatusLine().getStatusCode() == 500) {
	    res = makeRequestNoRetryWithEntity(urlPath, httpEntity, true);
	}
	return res;
    }

    public HttpResponse makeRequestWithJSONStringEntity(String urlPath,  HttpEntity httpEntity) throws Exception {
	HttpResponse res = makeRequestNoRetryWithJSONStringEntity(urlPath, httpEntity, false);
	if (res.getStatusLine().getStatusCode() == 500) {
	    res = makeRequestNoRetryWithJSONStringEntity(urlPath, httpEntity, true);
	}
	return res;
    }

    private HttpResponse makeRequestNoRetryNoAuth(String urlPath, List<NameValuePair> params) throws Exception {

	// Make POST request
	DefaultHttpClient client = new DefaultHttpClient();
	URI uri = new URI(Config.BASE_URL + urlPath);
	HttpPost post = new HttpPost(uri);
	UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
	post.setEntity(entity);
	post.setHeader("X-Same-Domain", "1"); // XSRF
	HttpResponse res = client.execute(post);
	return res;
    }

    private HttpResponse makeRequestNoRetryWithParams(String urlPath, List<NameValuePair> params, boolean renewToken)
    throws Exception {

	StringBuilder ascidCookie = new StringBuilder();
	HttpResponse res = getServerAscidCookie(renewToken, ascidCookie);
	if (res != null) {
	    return res;
	}
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "AppEngineClient:makeRequestNoRetryWihtParams() : got ACSID cookie = "
		    + ascidCookie.toString());
	}

	// Make POST request
	URI uri = new URI(Config.BASE_URL + urlPath);
	HttpPost post = new HttpPost(uri);
	UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
	post.setEntity(entity);
	post.setHeader("Cookie", ascidCookie.toString());
	post.setHeader("X-Same-Domain", "1"); // XSRF

	Log.d(Config.LOGTAG, "AppEngineClient:makeRequestNoRetryWihtParams() : make post request to uri = "
		+ uri.toString());

	DefaultHttpClient client = new DefaultHttpClient();
	res = client.execute(post);
	return res;
    }

    private HttpResponse makeRequestNoRetryWithEntity(String urlPath, HttpEntity httpEntity, boolean renewToken)
    throws Exception {

	StringBuilder ascidCookie = new StringBuilder();
	HttpResponse res = getServerAscidCookie(renewToken, ascidCookie);
	if (res != null) {
	    return res;
	}
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "AppEngineClient:makeRequestNoRetryWithEntity() : got ACSID cookie = "
		    + ascidCookie.toString());
	}

	// Make POST request
	URI uri = new URI(Config.BASE_URL + urlPath);
	HttpPost post = new HttpPost(uri);
	post.setEntity(httpEntity);
	post.setHeader("Cookie", ascidCookie.toString());
	post.setHeader("X-Same-Domain", "1"); // XSRF

	Log.d(Config.LOGTAG, "AppEngineClient:makeRequestNoRetryWithEntity() : make post request to uri = "
		+ uri.toString());

	DefaultHttpClient client = new DefaultHttpClient();
	res = client.execute(post);
	return res;
    }

    private HttpResponse makeRequestNoRetryWithJSONStringEntity(String urlPath, HttpEntity httpEntity, boolean renewToken)
    throws Exception {

	StringBuilder ascidCookie = new StringBuilder();
	HttpResponse res = getServerAscidCookie(renewToken, ascidCookie);
	if (res != null) {
	    return res;
	}
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "AppEngineClient:makeRequestNoRetryWithJSONStringEntity() : got ACSID cookie = "
		    + ascidCookie.toString());
	}

	// Make POST request
	URI uri = new URI(Config.BASE_URL + urlPath);
	HttpPost post = new HttpPost(uri);
	post.setEntity(httpEntity);
	post.setHeader("Cookie", ascidCookie.toString());
	post.setHeader("X-Same-Domain", "1"); // XSRF
	post.setHeader("Accept", "application/json");
	post.setHeader("Content-type", "application/json");

	Log.d(Config.LOGTAG, "AppEngineClient:makeRequestNoRetryWithJSONStringEntity() : make post request to uri = "
		+ uri.toString());

	DefaultHttpClient client = new DefaultHttpClient();
	res = client.execute(post);
	return res;
    }

    private HttpResponse getServerAscidCookie(boolean renewToken, StringBuilder cookie) throws Exception {
	// Get auth token for account
	Account account = new Account(mUserEmail, "com.google");
	String authToken = getAuthToken(mContext, account);
	if (authToken == null) {
	    Log.w(Config.LOGTAG, "AppEngineClient:getServerAscidCookie() : authToken is null");
	    throw new PendingAuthException(mUserEmail);
	}
	if (renewToken) { // invalidate the cached token
	    Log.d(Config.LOGTAG, "AppEngineClient:getServerAscidCookie() : renew authToken");
	    AccountManager accountManager = AccountManager.get(mContext);
	    accountManager.invalidateAuthToken(account.type, authToken);
	    authToken = getAuthToken(mContext, account);
	}

	// Get ACSID cookie
	DefaultHttpClient client = new DefaultHttpClient();
	String continueURL = Config.BASE_URL;
	URI uri = new URI(AUTH_URL + "?continue=" + URLEncoder.encode(continueURL, "UTF-8") + "&auth=" + authToken);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, "AppEngineClient:getServerAscidCookie() : get auth cookie, trying to connect to "
		    + uri.toString());
	}

	HttpGet method = new HttpGet(uri);
	final HttpParams getParams = new BasicHttpParams();
	HttpClientParams.setRedirecting(getParams, false); // continue is not
	// used
	method.setParams(getParams);

	HttpResponse res = client.execute(method);
	Header[] headers = res.getHeaders("Set-Cookie");
	if (res.getStatusLine().getStatusCode() != 302 || headers.length == 0) {
	    Log.w(Config.LOGTAG,
		    "AppEngineClient:getServerAscidCookie() : failed to continue to make request " +
	    "because status code is not 302 or header lendth is zero");
	    return res;
	}

	String ascidCookie = null;
	for (Header header : headers) {
	    if (header.getValue().indexOf("ACSID=") >= 0) {
		// let's parse it
		String value = header.getValue();
		String[] pairs = value.split(";");
		ascidCookie = pairs[0];
	    }
	}
	cookie.append(ascidCookie);
	return null;
    }

    private String getAuthToken(Context context, Account account) {
	String authToken = null;
	AccountManager accountManager = AccountManager.get(context);
	try {
	    AccountManagerFuture<Bundle> future = accountManager.getAuthToken(account, AUTH_TOKEN_TYPE, false, null,
		    null);
	    Bundle bundle = future.getResult();
	    authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
	    // User will be asked for "App Engine" permission.
	    if (authToken == null) {
		// No auth token - will need to ask permission from user.
		Intent intent = new Intent(Config.INTENT_ACTION_AUTH_PERMISSION);
		intent.putExtra(Config.EXTRA_KEY_ACCOUNT_BUNDLE, bundle);
		context.sendBroadcast(intent);

		Log.w(Config.LOGTAG,
			"AppEngineClient:getAuthToken() : there is no auth token available for this account = "
			+ account + ", try to run intent " + Config.INTENT_ACTION_AUTH_PERMISSION);
	    }
	} catch (OperationCanceledException e) {
	    Log.w(Config.LOGTAG, e.getMessage());
	} catch (AuthenticatorException e) {
	    Log.w(Config.LOGTAG, e.getMessage());
	} catch (IOException e) {
	    Log.w(Config.LOGTAG, e.getMessage());
	}
	return authToken;
    }

    public class PendingAuthException extends Exception {
	private static final long serialVersionUID = 1L;

	public PendingAuthException(String message) {
	    super(message);
	}
    }
}
