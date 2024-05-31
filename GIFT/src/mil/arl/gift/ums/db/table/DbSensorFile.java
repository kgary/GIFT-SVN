/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="sensorfile")
public class DbSensorFile extends DbDataFile {
	
	private DbDomainSession domainSession;
	private String sensorType;

	/**
	 * Default Constructor - required for Hibernate
	 */
	public DbSensorFile(){
		super();
	}
	
	/**
	 * Class constructor
	 * 
	 * @param domainSession - the session associated with this sensor data
	 * @param sensorType - the sensor type that produced the sensor data
	 * @param fileName - the sensor file name
	 */
	public DbSensorFile(DbDomainSession domainSession, String sensorType, String fileName) {
		super(fileName);
		this.domainSession = domainSession;
		this.sensorType = sensorType;
	}
	
	//if a sensor file is deleted, don't delete the session
	@OneToOne(cascade=CascadeType.DETACH, fetch=FetchType.LAZY)
	@JoinColumn(name="sessionId_FK")
	public DbDomainSession getDomainSession() {
		return domainSession;
	}
	public void setDomainSession(DbDomainSession domainSession) {
		this.domainSession = domainSession;
	}
	
	@Column(nullable=false)
	public String getSensorType() {
		return sensorType;
	}
	public void setSensorType(String sensorType) {
		this.sensorType = sensorType;
	}
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[SensorFile:");
	    sb.append(super.toString());
	    sb.append(", ").append(getDomainSession());
	    sb.append(", sensorType = ").append(getSensorType());
	    sb.append("]");
		
		return sb.toString();
	}
}
