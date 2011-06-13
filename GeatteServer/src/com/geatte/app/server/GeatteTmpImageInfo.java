package com.geatte.app.server;

import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Blob;

@PersistenceCapable(identityType = IdentityType.APPLICATION )
public class GeatteTmpImageInfo {

    @PrimaryKey
    @Persistent
    private String id;

    @Persistent
    private Blob image;

    @Persistent
    private Date createdDate = new Date();

    public GeatteTmpImageInfo(String id, Blob image) {
	super();
	this.setId(id);
	this.setImage(image);
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getId() {
	return id;
    }

    public Date getCreatedDate() {
	return createdDate;
    }

    public void setImage(Blob image) {
	this.image = image;
    }

    public Blob getImage() {
	return this.image;
    }

    @SuppressWarnings("unchecked")
    public static GeatteTmpImageInfo getImageInfoForImageId(PersistenceManager pm, String imageId) {
	Query query = pm.newQuery(GeatteTmpImageInfo.class);

	query.setFilter("id == idParam");
	query.declareParameters("String idParam");

	List<GeatteTmpImageInfo> qresult = (List<GeatteTmpImageInfo>) query.execute(imageId);
	query.closeAll();

	if (qresult.size() > 0) {
	    return qresult.get(0);
	}
	return null;
    }

}
