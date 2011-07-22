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

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class GeatteVote {
    private static final Logger log = Logger.getLogger(GeatteVote.class.getName());

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;

    @Persistent
    private String geatteId;

    @Persistent
    private String geatteVoter;

    @Persistent
    private String geatteOwner;

    @Persistent
    private String geatteVoteResp;

    @Persistent
    private String geatteFeedback;

    @Persistent
    private Date createdDate = new Date();

    @Persistent
    private Date updateDate;

    public GeatteVote(String geatteId, String geatteVoter, String geatteOwner, String geatteVoteResp, String geatteFeedback) {
	super();
	this.setGeatteId(geatteId);
	this.setGeatteVoter(geatteVoter);
	this.setGeatteOwner(geatteOwner);
	this.setGeatteVoteResp(geatteVoteResp);
	this.setGeatteFeedback(geatteFeedback);
    }

    public Long getId() {
	return id;
    }

    private void update() {
	setUpdateDate(new Date ());
    }

    public String getGeatteId() {
	return geatteId;
    }

    public void setGeatteId(String geatteId) {
	this.geatteId = geatteId;
    }

    public String getGeatteVoter() {
	return geatteVoter;
    }

    public void setGeatteVoter(String geatteVoter) {
	this.geatteVoter = geatteVoter;
    }

    public String getGeatteOwner() {
	return geatteOwner;
    }

    public void setGeatteOwner(String geatteOwner) {
	this.geatteOwner = geatteOwner;
    }

    public String getGeatteVoteResp() {
	return geatteVoteResp;
    }

    public void setGeatteVoteResp(String geatteVoteResp) {
	this.geatteVoteResp = geatteVoteResp;
	this.update();
    }

    public String getGeatteFeedback() {
	return geatteFeedback;
    }

    public void setGeatteFeedback(String geatteFeedback) {
	this.geatteFeedback = geatteFeedback;
    }

    public Date getCreatedDate() {
	return createdDate;
    }

    public String getCreatedDateStr() {
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String str = dateFormat.format(createdDate);
	return str;
    }

    public Date getUpdateDate() {
	return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
	this.updateDate = updateDate;
    }

    @SuppressWarnings("unchecked")
    public static List<GeatteVote> getAllVoteFromMe(PersistenceManager pm, String fromNumber, String defaultCountryCode, Date lastSyncDate) {

	// trim dash '-' '(' ')'from given number
	fromNumber = fromNumber.replaceAll("-", "").replaceAll("\\(", "").replaceAll("\\)", "").trim();

	Map<String, Object> map = new HashMap<String, Object>();

	Query query = pm.newQuery(GeatteVote.class);

	if (lastSyncDate != null) {
	    query.setFilter("geatteVoter == geatteVoterParam && createdDate > lastSyncDateParam");
	    query.declareParameters("String geatteVoterParam, java.util.Date lastSyncDateParam");
	    map.put("lastSyncDateParam", lastSyncDate);
	} else {
	    query.setFilter("geatteVoter == geatteVoterParam");
	    query.declareParameters("String geatteVoterParam");
	}
	map.put("geatteVoterParam", fromNumber);
	query.setOrdering("createdDate desc");

	// first get the number as is
	List<GeatteVote> qresult = (List<GeatteVote>) query.executeWithMap(map);
	// copy to array - we need to close the query
	List<GeatteVote> result = new ArrayList<GeatteVote>();
	for (GeatteVote di : qresult) {
	    result.add(di);
	}

	// if no results back, try to compare by adding prefix +
	String preNumber = "+" + fromNumber;
	if (fromNumber.indexOf("+") < 0) {
	    map.put("geatteVoterParam", preNumber);
	    qresult = (List<GeatteVote>) query.executeWithMap(map);
	    for (GeatteVote di : qresult) {
		result.add(di);
	    }
	}

	// if still no results, try to compare by reformat with default country code
	PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
	PhoneNumber numberProto = null;
	try {
	    numberProto = phoneUtil.parse(fromNumber, defaultCountryCode);
	} catch (NumberParseException npe) {
	    log.log(Level.WARNING, "GeatteVote.getAllVoteFromMe(): NumberParseException was thrown: "
		    , npe);
	}

	if (numberProto != null && phoneUtil.isValidNumber(numberProto)) {
	    String formatNumber = phoneUtil.format(numberProto, PhoneNumberFormat.E164);
	    map.put("geatteVoterParam", formatNumber);
	    //avoid duplicates
	    if (!formatNumber.equals(fromNumber) && !formatNumber.equals(preNumber)) {
		qresult = (List<GeatteVote>) query.executeWithMap(map);
		for (GeatteVote di : qresult) {
		    result.add(di);
		}
	    }
	} else {
	    log.log(Level.WARNING, "GeatteVote.getAllVoteFromMe() : number can not use default country code, fromNumber = "
		    + fromNumber + ", defaultCountryCode = " + defaultCountryCode);
	}

	query.closeAll();
	return result;
    }

    @SuppressWarnings("unchecked")
    public static List<GeatteVote> getAllVoteToMe(PersistenceManager pm, String toNumber, String defaultCountryCode, Date lastSyncDate) {

	// trim dash '-' '(' ')'from given number
	toNumber = toNumber.replaceAll("-", "").replaceAll("\\(", "").replaceAll("\\)", "").trim();

	Map<String, Object> map = new HashMap<String, Object>();

	Query query = pm.newQuery(GeatteVote.class);

	if (lastSyncDate != null) {
	    query.setFilter("geatteOwner == geatteOwnerParam && createdDate > lastSyncDateParam");
	    query.declareParameters("String geatteOwnerParam, java.util.Date lastSyncDateParam");
	    map.put("lastSyncDateParam", lastSyncDate);
	} else {
	    query.setFilter("geatteOwner == geatteOwnerParam");
	    query.declareParameters("String geatteOwnerParam");
	}
	map.put("geatteOwnerParam", toNumber);
	query.setOrdering("createdDate desc");

	// first get the number as is
	List<GeatteVote> qresult = (List<GeatteVote>) query.executeWithMap(map);
	// copy to array - we need to close the query
	List<GeatteVote> result = new ArrayList<GeatteVote>();
	for (GeatteVote di : qresult) {
	    result.add(di);
	}

	// if no results back, try to compare by adding prefix +
	String preNumber = "+" + toNumber;
	if (toNumber.indexOf("+") < 0) {
	    map.put("geatteOwnerParam", preNumber);
	    qresult = (List<GeatteVote>) query.executeWithMap(map);
	    for (GeatteVote di : qresult) {
		result.add(di);
	    }
	}

	// if still no results, try to compare by reformat with default country code
	PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
	PhoneNumber numberProto = null;
	try {
	    numberProto = phoneUtil.parse(toNumber, defaultCountryCode);
	} catch (NumberParseException npe) {
	    log.log(Level.WARNING, "GeatteVote.getAllVoteToMe(): NumberParseException was thrown: "
		    , npe);
	}

	if (numberProto != null && phoneUtil.isValidNumber(numberProto)) {
	    String formatNumber = phoneUtil.format(numberProto, PhoneNumberFormat.E164);
	    map.put("geatteOwnerParam", formatNumber);
	    //avoid duplicates
	    if (!formatNumber.equals(toNumber) && !formatNumber.equals(preNumber)) {
		qresult = (List<GeatteVote>) query.executeWithMap(map);
		for (GeatteVote di : qresult) {
		    result.add(di);
		}
	    }
	} else {
	    log.log(Level.WARNING, "GeatteVote.getAllVoteToMe() : number can not use default country code, toNumber = "
		    + toNumber + ", defaultCountryCode = " + defaultCountryCode);
	}

	query.closeAll();
	return result;
    }

    @SuppressWarnings("unchecked")
    public static List<GeatteVote> getAllVoteToItems(PersistenceManager pm, List<String> listOfIds, Date lastSyncDate) {
	if (listOfIds.size() == 0 || lastSyncDate == null) {
	    //return empty array
	    return new ArrayList<GeatteVote>();
	}
	Map<String, Object> map = new HashMap<String, Object>();
	log.log(Level.INFO, "GeatteVote.getAllVoteToMe(): number of ids to retrieve : " + listOfIds.size());
	Query query = pm.newQuery(GeatteVote.class, ":p.contains(geatteId)");

	query.setFilter("geatteId == listOfIds && createdDate > lastSyncDateParam");
	query.declareParameters("java.util.List listOfIds, java.util.Date lastSyncDateParam");
	map.put("lastSyncDateParam", lastSyncDate);
	query.setOrdering("createdDate desc");

	List<GeatteVote> result = new ArrayList<GeatteVote>();
	int s = listOfIds.size();
	int q = s/20;
	int r = s%20;
	int j = 0;
	List<String> partialIds;
	for (int i = 1; i <= q; i++) {
	    partialIds = listOfIds.subList(j, i*20);
	    j = i*20;
	    map.put("listOfIds", partialIds);
	    List<GeatteVote> list = (List<GeatteVote>) query.executeWithMap(map);
	    for (GeatteVote di : list) {
		result.add(di);
	    }
	}

	if (r > 0) {
	    partialIds = listOfIds.subList(j, j+r);
	    map.put("listOfIds", partialIds);
	    List<GeatteVote> list = (List<GeatteVote>) query.executeWithMap(map);
	    for (GeatteVote di : list) {
		result.add(di);
	    }
	}

	query.closeAll();

	log.log(Level.INFO, "GeatteVote.getAllVoteToMe(): number of vote has retrieved : " + result.size());

	return result;
    }

}
