package com.geatte.app.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/**
 * Registration info.
 *
 * An account may be associated with multiple phones,
 * and a phone may be associated with multiple accounts.
 *
 * registrations lists different phones registered to that account.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class DeviceInfo {
    public static final String TYPE_AC2DM = "ac2dm";
    public static final String TYPE_CHROME = "chrome";

    /**
     * The device id
     *
     * Device-id can be specified by device
     */
    @PrimaryKey
    @Persistent
    private Key key;

    /**
     * The phone number
     */
    @Persistent
    private String phoneNumber;

    /**
     * The ID used for sending messages to.
     */
    @Persistent
    private String deviceRegistrationID;

    /**
     * The email address
     */
    @Persistent
    private String userEmail;


    /**
     * Current supported types:
     *   (default) - ac2dm, regular froyo+ devices using C2DM protocol
     *
     * New types may be defined - for example for sending to chrome.
     */
    @Persistent
    private String type;

    /**
     * Friendly name for the device. May be edited by the user.
     */
    @Persistent
    private String deviceName;

    /**
     * For statistics - and to provide hints to the user.
     */
    @Persistent
    private Date registrationTimestamp;

    @Persistent
    private Boolean debug;

    public DeviceInfo(Key key, String deviceRegistrationID) {
	this.key = key;
	this.deviceRegistrationID = deviceRegistrationID;
	this.setRegistrationTimestamp(new Date()); // now
    }

    public DeviceInfo(Key key) {
	this.key = key;
    }

    public Key getKey() {
	return key;
    }

    public void setKey(Key key) {
	this.key = key;
    }

    public String getPhoneNumber() {
	return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
	this.phoneNumber = phoneNumber;
    }

    // Accessor methods for properties added later (hence can be null)

    public String getDeviceRegistrationID() {
	return deviceRegistrationID;
    }

    public void setDeviceRegistrationID(String deviceRegistrationID) {
	this.deviceRegistrationID = deviceRegistrationID;
    }

    public String getUserEmail() {
	return userEmail;
    }

    public void setUserEmail(String userEmail) {
	this.userEmail = userEmail;
    }


    public boolean getDebug() {
	return (debug != null ? debug.booleanValue() : false);
    }

    public void setDebug(boolean debug) {
	this.debug = new Boolean(debug);
    }

    public void setType(String type) {
	this.type = type;
    }

    public String getType() {
	return type != null ? type : "";
    }

    public void setDeviceName(String name) {
	this.deviceName = name;
    }

    public String getDeviceName() {
	return deviceName != null ? deviceName : "";
    }

    public void setRegistrationTimestamp(Date registrationTimestamp) {
	this.registrationTimestamp = registrationTimestamp;
    }

    public Date getRegistrationTimestamp() {
	return registrationTimestamp;
    }

    @SuppressWarnings("unchecked")
    public static List<DeviceInfo> getDeviceInfoForUserEmail(PersistenceManager pm, String userEmail) {
	Query query = pm.newQuery(DeviceInfo.class);
	//query.setFilter("key >= '" + user + "' && key < '" + user + "$'");

	query.setFilter("userEmail == userEmailParam");
	query.declareParameters("String userEmailParam");

	List<DeviceInfo> qresult = (List<DeviceInfo>) query.execute(userEmail);
	// Copy to array - we need to close the query
	List<DeviceInfo> result = new ArrayList<DeviceInfo>();
	for (DeviceInfo di : qresult) {
	    result.add(di);
	}
	query.closeAll();
	return result;
    }

    public static DeviceInfo getDeviceInfoForDeviceId(PersistenceManager pm, String deviceId) {
	DeviceInfo dInfo = pm.getObjectById(DeviceInfo.class, deviceId);
	return dInfo;
    }
}
