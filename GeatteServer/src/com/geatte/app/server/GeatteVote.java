package com.geatte.app.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

@PersistenceCapable(identityType = IdentityType.APPLICATION )
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
    private String geatteVoteResp;

    @Persistent
    private String geatteFeedback;

    @Persistent
    private Date createdDate = new Date();

    @Persistent
    private Date updateDate;

    public GeatteVote(String geatteId, String geatteVoter, String geatteVoteResp, String geatteFeedback) {
	super();
	this.setGeatteId(geatteId);
	this.setGeatteVoter(geatteVoter);
	this.setGeatteVoteResp(geatteVoteResp);
	this.setGeatteFeedback(geatteFeedback);
    }

    public GeatteVote(String geatteVoteResp) {
	super();
	this.setGeatteVoteResp(geatteVoteResp);
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
    public static List<GeatteVote> getAllVoteFromMe(PersistenceManager pm, String fromNumber, String defaultCountryCode) {

	// trim dash '-' '(' ')'from given number
	fromNumber = fromNumber.replaceAll("-", "").replaceAll("\\(", "").replaceAll("\\)", "").trim();


	Query query = pm.newQuery(GeatteVote.class);

	query.setFilter("geatteVoter == geatteVoterParam");
	query.declareParameters("String geatteVoterParam");

	// first get the number as is
	List<GeatteVote> qresult = (List<GeatteVote>) query.execute(fromNumber);
	// copy to array - we need to close the query
	List<GeatteVote> result = new ArrayList<GeatteVote>();
	for (GeatteVote di : qresult) {
	    result.add(di);
	}

	// if no results back, try to compare by adding prefix +
	if (result.size() == 0) {
	    String preNumber = "+" + fromNumber;
	    qresult = (List<GeatteVote>) query.execute(preNumber);
	    for (GeatteVote di : qresult) {
		result.add(di);
	    }
	}

	// if still no results, try to compare by reformat with default country code
	if (result.size() == 0) {
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
		qresult = (List<GeatteVote>) query.execute(formatNumber);
		for (GeatteVote di : qresult) {
		    result.add(di);
		}
	    } else {
		log.log(Level.WARNING, "GeatteVote.getAllVoteFromMe() : number can not use default country code, fromNumber = "
			+ fromNumber + ", defaultCountryCode = " + defaultCountryCode);
	    }
	}

	query.closeAll();
	return result;
    }

    @SuppressWarnings("unchecked")
    public static List<GeatteVote> getAllVoteToMe(PersistenceManager pm, List<String> listOfIds) {
	Query query = pm.newQuery(GeatteVote.class, ":p.contains(geatteId)");
	query.setFilter("geatteId == :listOfIds");

	List<GeatteVote> list = (List<GeatteVote>) query.execute(listOfIds);
	List<GeatteVote> result = new ArrayList<GeatteVote>();
	for (GeatteVote di : list) {
	    result.add(di);
	}

	query.closeAll();
	return result;
    }

}
