/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import mil.arl.gift.common.UserSessionType;
import mil.arl.gift.common.util.CompareUtil;

/**
 * The DbGlobalUser class is an implementation for the globaluser database table in the ums database.
 * A global user here is used to give a unique 'global' id to a user who could be coming from different login means of GIFT.
 * For example an LTI user would be given a 'global' id that is different than a normal user who logs into gift with a username/password.
 * Each of these various methods of logging in can be tagged in the the global user table to uniquely identify the user in the
 * gift instance.  The id is unique in the current gift instance.  It is not guaranteed to be unique across multiple gift instances.
 * 
 * This is different than the giftuser table in the ums database table.  The giftuser table is meant for logged in 'normal' users
 * of gift that authenticate via redmine with a username/password.  The global user table is meant to be a one stop place where
 * all various login systems in the future could go to get a unique id that can be used across the entire gift instance to identify
 * the user and the user session.  
 * 
 * @author nblomberg
 *
 */
@Entity
@Table(name="globaluser")
public class DbGlobalUser {


    /** 
     * The global id for the user.  This is unique for the current gift instance.  It is not guaranteed to be unique
     * across multiple gift instances.
     */
	private Integer globalId;
	
	/**
	 * The type of user identified by the id.  This provides further context in the database on where to go to 
	 * find more information about the user.  For example, if the id is for an LTI user, then the global id can be
	 * looked up in the ltiuserrecord table.  In the future, if the id is for a normal gift user, then the id could
	 * be looked up in the gift user table (as a foreign key).
	 * 
	 */
	private UserSessionType userType;

	
	/**
	 * Default Constructor
	 */
	public DbGlobalUser(){
		
	}
	
	//primary key, auto-generated sequentially
	@Id
	@Column(name="globalId_PK")
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Integer getGlobalId() {
		return globalId;
	}
	
	public void setGlobalId(Integer globalId) {
		this.globalId = globalId;
	}
	
	@Column(name="userType") 
    @Enumerated(EnumType.STRING) 
	public UserSessionType getUserType() {
	    return userType;
	}
	
	public void setUserType(UserSessionType userType) {
        this.userType = userType;
    }
	
	@Override
    public boolean equals(Object obj) {
	    if (obj == null) {
            
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            
            return false;
        }
        
        DbGlobalUser other = (DbGlobalUser) obj;
        
        if (CompareUtil.equalsNullSafe(this.getGlobalId(), other.getGlobalId()) &&
            CompareUtil.equalsNullSafe(this.getUserType(), other.getUserType())) {
            return true;
        }
        
        return false;
	}
	
	@Override
	public int hashCode() {
	    int hash = 7;        
        hash = 89 * hash;
        
        // Consumer key and consumer id should be a unique record.
        if(getGlobalId() != null){
            hash += getGlobalId().hashCode();
        }
        
        if(getUserType() != null){
            hash += getUserType().hashCode();
        }
        
        return hash;
	}

	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[GlobalUser:");
	    sb.append(" globalId = ").append(getGlobalId());
	    sb.append(", userType = ").append(getUserType());
	    sb.append("]");
		
		return sb.toString();
	}
}
