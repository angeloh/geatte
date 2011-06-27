package com.geatte.app.server;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION )
public class GeatteVote {

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

    public Date getUpdateDate() {
	return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
	this.updateDate = updateDate;
    }

}
