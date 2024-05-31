/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name="giftuser")
public class DbUser {

    /** unique user id among all users of this GIFT instance */
	private int userId;
	
	private String gender;
	
	/** 
	 * a user name for the LMS database(s) being used by GIFT.  By default GIFT uses the local LMS created by GIFT which
	 * simply stores scoring information authored in a DKF.  However the LMS has the ability to connect to multiple LMS/LRS databases
	 * and therefore we could see this attribute turn into a list of user names based on the various connected accounts on those databases. 
	 */
	private String lmsUserName;
	
	/** 
	 * a user name that can be authenticated when accompanied with a password and is therefore unique.
	 * This is an optional field in order to support experiments which require anonymity.
	 */
	private String username;
	
	/** collection of GIFT domain sessions this user has started (not necessarily finished) in GIFT */
	private List<DbDomainSession> domainSessions;
	
	/**
	 * Default Constructor
	 */
	public DbUser(){
		
	}
	
	/**
	 * Class constructor 
	 * 
	 * @param gender the gender of the user
	 */
	public DbUser(String gender) {
		this.gender = gender;
	}
	
	//primary key, auto-generated sequentially
	@Id
	@Column(name="userId_PK")
	@TableGenerator(name="userid", table="userpktb", pkColumnName="userkey", pkColumnValue="uservalue", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.TABLE, generator="userid")
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	//can't be null
	@Column(nullable=false)
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	//created bi-directional 
	//now when User is deleted, the domain sessions are deleted
	@OneToMany(targetEntity=DbDomainSession.class, mappedBy="user", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<DbDomainSession> getDomainSessions() {
		return domainSessions;
	}
	public void setDomainSessions(List<DbDomainSession> domainSessions) {
		this.domainSessions = domainSessions;
	}
	
	/**
	 * Return the LMS user name for this user
	 * 
	 * @return String
	 */
	public String getLMSUserName() {
		return lmsUserName;
	}

	/**
	 * Set the LMS user name for this user
	 * 
	 * @param lmsUserName - the LMS user name for this user
	 */
	public void setLMSUserName(String lmsUserName) {
		this.lmsUserName = lmsUserName;
	}
	
	/**
	 * Return the username for this user
	 * 
	 * @return String the username for this user.  Can be null.
	 */
	public String getUsername(){
	    return username;
	}
	
	/**
	 * Set the user name for this user
	 * (For more information refer to the class attribute's javadoc)
	 * 
	 * @param username the user name for this user.  Can be null.
	 */
	@Column(unique=true)
	public void setUsername(String username){
	    this.username = username;
	}
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[User:");
	    sb.append(" userId = ").append(getUserId());
	    sb.append(", username = ").append(getUsername());
	    sb.append(", gender = ").append(getGender());
	    sb.append(", LMS user name = ").append(getLMSUserName());
	    sb.append("]");
		
		return sb.toString();
	}
}
