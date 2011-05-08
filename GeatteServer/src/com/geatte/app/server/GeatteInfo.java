/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.geatte.app.server;

import java.util.Date;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;

@PersistenceCapable(identityType = IdentityType.DATASTORE )
public class GeatteInfo {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private String fromNumber;

    @Persistent
    private String toNumber;

    @Persistent
    private String geatteTitile;

    @Persistent
    private String geatteDesc;

    @Persistent
    private Blob image;

    @Persistent
    private Date createDate = new Date();

    @Persistent
    private Date updateDate;

    public GeatteInfo(Blob image) {
	super();
	this.setImage(image);
    }

    public Key getKey() {
	return key;
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

    public Date getCreateDate() {
	return createDate;
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

}
