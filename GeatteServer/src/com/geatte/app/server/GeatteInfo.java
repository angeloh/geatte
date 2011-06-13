package com.geatte.app.server;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Blob;

@PersistenceCapable(identityType = IdentityType.APPLICATION )
public class GeatteInfo {

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

}
