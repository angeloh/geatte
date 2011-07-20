package com.geatte.app.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Blob;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

@PersistenceCapable(identityType = IdentityType.APPLICATION )
public class GeatteInfo {
    private static final Logger log = Logger.getLogger(GeatteInfo.class.getName());

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;

    @Persistent
    private String fromNumber;

    @Persistent
    private String countryCode;

    @Persistent
    private String toNumber;

    @Persistent
    private String geatteTitile;

    @Persistent
    private String geatteDesc;

    @Persistent
    private Blob image;

    @Persistent
    private Date createdDate = new Date();

    @Persistent
    private Date updateDate;

    public GeatteInfo(String fromNumber, String countryCode, String toNumber, String geatteTitile, String geatteDesc, Blob image) {
	super();
	this.setFromNumber(fromNumber);
	this.setCountryCode(countryCode);
	this.setToNumber(toNumber);
	this.setGeatteTitile(geatteTitile);
	this.setGeatteDesc(geatteDesc);
	this.setImage(image);
    }

    public GeatteInfo(Blob image) {
	super();
	this.setImage(image);
    }

    public Long getId() {
	return id;
    }

    private void update() {
	setUpdateDate(new Date () );
    }

    public String getFromNumber() {
	return fromNumber;
    }

    public void setFromNumber(String fromNumber) {
	this.fromNumber = fromNumber;
    }

    public String getToNumber() {
	return toNumber;
    }

    public void setToNumber(String toNumber) {
	this.toNumber = toNumber;
    }

    public Date getUpdateDate() {
	return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
	this.updateDate = updateDate;
    }

    public Date getCreatedDate() {
	return createdDate;
    }

    public String getCreatedDateStr() {
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String str = dateFormat.format(createdDate);
	return str;
    }

    public void setImage(Blob image) {
	this.image = image;
	this.update();
    }

    public Blob getImage() {
	return this.image;
    }

    public String getGeatteTitile() {
	return geatteTitile;
    }

    public void setGeatteTitile(String geatteTitile) {
	this.geatteTitile = geatteTitile;
    }

    public String getGeatteDesc() {
	return geatteDesc;
    }

    public void setGeatteDesc(String geatteDesc) {
	this.geatteDesc = geatteDesc;
    }

    public String getCountryCode() {
	return countryCode;
    }

    public void setCountryCode(String countryCode) {
	this.countryCode = countryCode;
    }

    @SuppressWarnings("unchecked")
    public static List<GeatteInfo> getAllItemInfoFromNumber(PersistenceManager pm, String fromNumber, String defaultCountryCode, Date lastSyncDate) {

	// trim dash '-' '(' ')'from given number
	fromNumber = fromNumber.replaceAll("-", "").replaceAll("\\(", "").replaceAll("\\)", "").trim();

	Map<String, Object> map = new HashMap<String, Object>();

	Query query = pm.newQuery(GeatteInfo.class);

	if (lastSyncDate != null) {
	    query.setFilter("fromNumber == fromNumberParam && createdDate > lastSyncDateParam");
	    query.declareParameters("String fromNumberParam, java.util.Date lastSyncDateParam");
	    map.put("lastSyncDateParam", lastSyncDate);
	} else {
	    query.setFilter("fromNumber == fromNumberParam");
	    query.declareParameters("String fromNumberParam");
	}
	map.put("fromNumberParam", fromNumber);
	query.setOrdering("createdDate desc");

	// first get the number as is
	List<GeatteInfo> qresult = (List<GeatteInfo>) query.executeWithMap(map);
	// copy to array - we need to close the query
	List<GeatteInfo> result = new ArrayList<GeatteInfo>();
	for (GeatteInfo di : qresult) {
	    result.add(di);
	}
	log.log(Level.FINER, "GeatteInfo.getAllItemInfoFromNumber(): get result size is : " +
		result.size() + ", fromNumberParam = " + fromNumber);

	// try to compare by adding prefix +
	String preNumber = "+" + fromNumber;
	if (fromNumber.indexOf("+") < 0) {
	    map.put("fromNumberParam", preNumber);
	    qresult = (List<GeatteInfo>) query.executeWithMap(map);
	    for (GeatteInfo di : qresult) {
		result.add(di);
	    }
	    log.log(Level.FINER, "GeatteInfo.getAllItemInfoFromNumber(): after perfix get " +
		    "result size is : " + result.size() + ", fromNumberParam = " + preNumber);
	}

	// try to compare by reformat with default country code
	PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
	PhoneNumber numberProto = null;
	try {
	    numberProto = phoneUtil.parse(fromNumber, defaultCountryCode);
	} catch (NumberParseException npe) {
	    log.log(Level.WARNING, "GeatteInfo.getAllItemInfoFromNumber(): NumberParseException was thrown: "
		    , npe);
	}

	if (numberProto != null && phoneUtil.isValidNumber(numberProto)) {
	    String formatNumber = phoneUtil.format(numberProto, PhoneNumberFormat.E164);
	    map.put("fromNumberParam", formatNumber);
	    //avoid duplicates
	    if (!formatNumber.equals(fromNumber) && !formatNumber.equals(preNumber)) {
		qresult = (List<GeatteInfo>) query.executeWithMap(map);
		for (GeatteInfo di : qresult) {
		    result.add(di);
		}
		log.log(Level.FINER, "GeatteInfo.getAllItemInfoFromNumber(): after format" +
			" get result size is : " + result.size() + ", fromNumberParam = " + formatNumber);
	    }
	} else {
	    log.log(Level.WARNING, "GeatteInfo.getAllItemInfoFromNumber() : number can not use " +
		    "default country code, fromNumber = " + fromNumber +
		    ", defaultCountryCode = " + defaultCountryCode);
	}

	query.closeAll();
	return result;
    }

    @SuppressWarnings("unchecked")
    public static List<GeatteInfo> getAllItemInfoToNumber(PersistenceManager pm, String toNumber, String defaultCountryCode, Date lastSyncDate) {

	// trim dash '-' '(' ')'from given number
	toNumber = toNumber.replaceAll("-", "").replaceAll("\\(", "").replaceAll("\\)", "").trim();

	Map<String, Object> map = new HashMap<String, Object>();

	Query query = pm.newQuery(GeatteInfo.class);

	if (lastSyncDate != null) {
	    query.setFilter("toNumber == toNumberParam && createdDate > lastSyncDateParam");
	    query.declareParameters("String toNumberParam, java.util.Date lastSyncDateParam");
	    map.put("lastSyncDateParam", lastSyncDate);
	} else {
	    query.setFilter("toNumber == toNumberParam");
	    query.declareParameters("String toNumberParam");
	}
	map.put("toNumberParam", toNumber);
	query.setOrdering("createdDate desc");

	// first get the number as is
	List<GeatteInfo> qresult = (List<GeatteInfo>) query.executeWithMap(map);
	// copy to array - we need to close the query
	List<GeatteInfo> result = new ArrayList<GeatteInfo>();
	for (GeatteInfo di : qresult) {
	    result.add(di);
	}

	// try to compare by adding prefix +
	String preNumber = "+" + toNumber;
	if (toNumber.indexOf("+") < 0) {
	    map.put("toNumberParam", preNumber);
	    qresult = (List<GeatteInfo>) query.executeWithMap(map);
	    for (GeatteInfo di : qresult) {
		result.add(di);
	    }
	}

	// try to compare by reformat with default country code
	PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
	PhoneNumber numberProto = null;
	try {
	    numberProto = phoneUtil.parse(toNumber, defaultCountryCode);
	} catch (NumberParseException npe) {
	    log.log(Level.WARNING, "GeatteInfo.getAllItemInfoToNumber(): NumberParseException was thrown: "
		    , npe);
	}

	if (numberProto != null && phoneUtil.isValidNumber(numberProto)) {
	    String formatNumber = phoneUtil.format(numberProto, PhoneNumberFormat.E164);
	    map.put("toNumberParam", formatNumber);
	    // avoid duplicates
	    if (!formatNumber.equals(toNumber) && !formatNumber.equals(preNumber)) {
		qresult = (List<GeatteInfo>) query.executeWithMap(map);
		for (GeatteInfo di : qresult) {
		    result.add(di);
		}
	    }
	} else {
	    log.log(Level.WARNING, "GeatteInfo.getAllItemInfoToNumber() : number can not use " +
		    "default country code, toNumber = " + toNumber +
		    ", defaultCountryCode = " + defaultCountryCode);
	}

	query.closeAll();
	return result;
    }

}
