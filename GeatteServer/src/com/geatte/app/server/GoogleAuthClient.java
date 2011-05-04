package com.geatte.app.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

public class GoogleAuthClient {
    private static final Logger log = Logger.getLogger(GoogleAuthClient.class.getName());

    public static String getAuthToken(String account, String password) {
	HttpURLConnection connection = null;
	try {
	    URL url = null;
	    try {
		url = new URL("https://www.google.com/accounts/ClientLogin");
	    } catch (MalformedURLException e) {
		throw new ServletException(e.getCause());
	    }
	    connection = (HttpURLConnection) url.openConnection();
	    connection.setDoOutput(true);
	    connection.setUseCaches(false);
	    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

	    log.log(Level.INFO, "GoogleAuthClient.getAuthToken() : connect to ClientAuth " + url.toString());
	} catch (Exception e) {
	    log.log(Level.WARNING, "GoogleAuthClient.getAuthToken() : failed to setup http connection", e);
	}

	StringBuilder sb = new StringBuilder();

	addEncodedParameter(sb, "accountType", "GOOGLE");
	addEncodedParameter(sb, "Email", account);
	addEncodedParameter(sb, "Passwd", password);
	addEncodedParameter(sb, "service", "ac2dm");
	addEncodedParameter(sb, "source", "geatte-geatte-0.1");
	String data = sb.toString();

	DataOutputStream stream;
	try {
	    stream = new DataOutputStream(connection.getOutputStream());
	    stream.writeBytes(data);
	    stream.flush();
	    stream.close();

	} catch (IOException e) {
	    log.log(Level.WARNING, "GoogleAuthClient.getAuthToken() : failed to write parameters to output stream", e);
	}


	BufferedReader reader;
	String token = null;
	try {
	    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	    String line;
	    String tokenIdentifier = "Auth=";
	    String errorIdentifier = "Error=";
	    StringBuilder errors = new StringBuilder();
	    while ((line = reader.readLine()) != null) {
		if (line.startsWith(tokenIdentifier)) {
		    token = line.substring(tokenIdentifier.length());
		} else if (line.startsWith(errorIdentifier)) {
		    String error = line.substring(errorIdentifier.length());
		    errors.append(error + System.getProperty("line.separator"));
		}
	    }
	    reader.close();
	} catch (IOException e) {
	    log.log(Level.WARNING, "GoogleAuthClient.getAuthToken() : failed to read the response from connection", e);
	}

	log.log(Level.INFO, "GoogleAuthClient.getAuthToken() : Got token = " + token);

	return token;
    }

    public static void addEncodedParameter(StringBuilder sb, String name, String value) {
	if (sb.length() > 0) {
	    sb.append("&");
	}

	try {
	    sb.append(URLEncoder.encode(name, "UTF-8"));
	    sb.append("=");
	    sb.append(URLEncoder.encode(value, "UTF-8"));
	} catch (UnsupportedEncodingException e) {
	    log.log(Level.WARNING, "GoogleAuthClient.addEncodedParameter() : failed to add encoded parameter", e);
	}
    }

}