package com.geatte.app.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

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
    private static final Logger log = Logger.getLogger(DeviceInfo.class.getName());
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
     * The country code
     */
    @Persistent
    private String countryCode;

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

    /**
     * @return the countryCode
     */
    public String getCountryCode() {
	return countryCode;
    }

    /**
     * @param countryCode the countryCode to set
     */
    public void setCountryCode(String countryCode) {
	this.countryCode = countryCode;
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

    /**
     * Get the list of device info for given number
     * 
     * @param pm persistence manager
     * @param number phone number
     * @param defaultCountryCode default country code
     * @return list of devices for this number
     */
    @SuppressWarnings("unchecked")
    public static List<DeviceInfo> getDeviceInfoForNumber(PersistenceManager pm, String number, String defaultCountryCode) {

	// trim dash '-' from given number
	number = number.replaceAll("-", "");

	Query query = pm.newQuery(DeviceInfo.class);

	query.setFilter("phoneNumber == phoneNumberParam");
	query.declareParameters("String phoneNumberParam");

	// first get the number as is
	List<DeviceInfo> qresult = (List<DeviceInfo>) query.execute(number);
	// copy to array - we need to close the query
	List<DeviceInfo> result = new ArrayList<DeviceInfo>();
	for (DeviceInfo di : qresult) {
	    result.add(di);
	}

	// if no results back, try to compare by adding prefix +
	if (result.size() == 0) {
	    String preNumber = "+" + number;
	    qresult = (List<DeviceInfo>) query.execute(preNumber);
	    for (DeviceInfo di : qresult) {
		result.add(di);
	    }
	}

	// if still no results, try to compare by reformat with default country code
	if (result.size() == 0) {
	    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
	    PhoneNumber numberProto = null;
	    try {
		numberProto = phoneUtil.parse(number, defaultCountryCode);
	    } catch (NumberParseException npe) {
		log.log(Level.WARNING, "DeviceInfo.getDeviceInfoForNumber(): NumberParseException was thrown: "
			, npe);
	    }

	    if (numberProto != null && phoneUtil.isValidNumber(numberProto)) {
		String formatNumber = phoneUtil.format(numberProto, PhoneNumberFormat.E164);
		qresult = (List<DeviceInfo>) query.execute(formatNumber);
		for (DeviceInfo di : qresult) {
		    result.add(di);
		}
	    } else {
		log.log(Level.WARNING, "DeviceInfo.getDeviceInfoForNumber() : to number can not use default country code, toNumber = "
			+ number + ", defaultCountryCode = " + defaultCountryCode);
	    }
	}

	query.closeAll();
	return result;
    }

    public static boolean checkPhoneExistedHadDeviceInfo(PersistenceManager pm, String number, String defaultCountryCode, String [] retNum) {
	Query query = pm.newQuery(DeviceInfo.class);

	query.setResult("count(this)");
	query.setFilter("phoneNumber == phoneNumberParam");
	query.declareParameters("String phoneNumberParam");

	// trim dash '-' from given number
	number = number.replaceAll("-", "");

	int count = 0;

	// first get the number as is
	count = (Integer) query.execute(number);
	if (count > 0) {
	    retNum[0] = number;
	}

	// if no results back, try to compare by adding prefix +
	if (count == 0) {
	    String preNumber = "+" + number;
	    count = (Integer) query.execute(preNumber);

	    if (count > 0) {
		retNum[0] = preNumber;
	    }
	}

	// if still no results, try to compare by reformat with default country code
	if (count == 0) {
	    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
	    PhoneNumber numberProto = null;
	    try {
		numberProto = phoneUtil.parse(number, defaultCountryCode);
	    } catch (NumberParseException npe) {
		log.log(Level.WARNING, "DeviceInfo.checkPhoneExistedHadDeviceInfo(): NumberParseException was thrown: "
			, npe);
	    }

	    if (numberProto != null && phoneUtil.isValidNumber(numberProto)) {
		String formatNumber = phoneUtil.format(numberProto, PhoneNumberFormat.E164);
		count = (Integer) query.execute(formatNumber);
		if (count > 0) {
		    retNum[0] = formatNumber;
		}
	    } else {
		log.log(Level.WARNING, "DeviceInfo.checkPhoneExistedHadDeviceInfo() : contact number can not use default country code, contact number = "
			+ number + ", defaultCountryCode = " + defaultCountryCode);
	    }
	}

	query.closeAll();
	return count > 0;
    }

    public static DeviceInfo getDeviceInfoForDeviceId(PersistenceManager pm, String deviceId) {
	DeviceInfo dInfo = pm.getObjectById(DeviceInfo.class, deviceId);
	return dInfo;
    }

}
