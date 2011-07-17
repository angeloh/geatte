package com.geatte.app.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import com.geatte.app.client.GreetingService;
import com.google.android.c2dm.server.C2DMessaging;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
@Deprecated
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {

    private static final Logger log = Logger.getLogger(GreetingServiceImpl.class.getName());

    //    public String greetServer(String input) throws IllegalArgumentException {
    //	// Verify that the input is valid.
    //	if (!FieldVerifier.isValidName(input)) {
    //	    // If the input is not valid, throw an IllegalArgumentException back
    //	    // to
    //	    // the client.
    //	    throw new IllegalArgumentException("Name must be at least 4 characters long");
    //	}
    //
    //	String serverInfo = getServletContext().getServerInfo();
    //	String userAgent = getThreadLocalRequest().getHeader("User-Agent");
    //
    //	// Escape data from the client to avoid cross-site script
    //	// vulnerabilities.
    //	input = escapeHtml(input);
    //	userAgent = escapeHtml(userAgent);
    //
    //	return "Hello, " + input + "!<br><br>I am running " + serverInfo + ".<br><br>It looks like you are using:<br>"
    //	+ userAgent;
    //    }

    /**
     * Escape an html string. Escaping data received from the client helps to
     * prevent cross-site script vulnerabilities.
     * 
     * @param html
     *            the html string to escape
     * @return the escaped string
     */
    //    private String escapeHtml(String html) {
    //	if (html == null) {
    //	    return null;
    //	}
    //	return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    //    }

    public String greetServer(String input) throws IllegalArgumentException {
	sendMessageToDevice(input);
	return "message sent";
    }

    private List<DeviceInfo> getDevices(String user) {
	// Context-shared PMF.
	PersistenceManager pm = DBHelper.getPMF(getServletContext()).getPersistenceManager();
	List<DeviceInfo> devices = new ArrayList<DeviceInfo>();
	try {
	    devices = DeviceInfo.getDeviceInfoForUserEmail(pm, user);
	    // cleanup for multi-device
	    /*if (devices.size() > 1) {
		// Make sure there is no 'bare' registration
		// Keys are sorted - check the first
		DeviceInfo first = devices.get(0);
		Key oldKey = first.getKey();
		if (oldKey.toString().indexOf("#") < 0) {
		    log.warning("Removing old-style key " + oldKey.toString());
		    // multiple devices, first is old-style.
		    devices.remove(0);
		    pm.deletePersistent(first);
		}
	    }*/
	} catch (Exception e) {
	    log.log(Level.WARNING, "Error loading registrations ", e);
	} finally {
	    pm.close();
	}
	return devices;

    }

    private void sendMessageToDevice(String user) {
	List<DeviceInfo> devices = getDevices(user);
	C2DMessaging push = C2DMessaging.get(getServletContext());
	boolean res = false;
	for (DeviceInfo deviceInfo : devices) {
	    try {
		res = push.sendNoRetry(deviceInfo.getDeviceRegistrationID(), "1", "payload",
			"this is message sent from geatte server", "debug");
	    } catch (IOException e) {
		log.log(Level.WARNING, "Error seding message to device", e);
	    }
	    log.log(Level.INFO, "sending message to device", deviceInfo.getDeviceRegistrationID() + ", res = " + res);
	}
    }

}
