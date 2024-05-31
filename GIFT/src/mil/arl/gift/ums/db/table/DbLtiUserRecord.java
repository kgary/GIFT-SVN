/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import mil.arl.gift.common.util.CompareUtil;

/** 
 * The DbLtiUserRecord class represents the database implementation for the ltiuserrecord table in the ums
 * database.
 * 
 * @author nblomberg
 *
 */
@Entity
@Table(name = "ltiuserrecord")
public class DbLtiUserRecord implements Serializable{  
    
    private static final long serialVersionUID = 1L;
    
    /** The consumer key is typically a UUID that uniquely identifies a Tool Consumer. This key must be unique for each consumer. */
    String consumerKey;
    /** The consumerId is an id that is unique for a user within a specific Tool Consumer.  This id could collide with other ids in other 
     *  Tool Consumers, so it is only unique within a specific Tool Consumer.
     */ 
    String consumerId;
    
    /** The timestamp represents an epoch value of the last time the LtiLaunch request was received for the user */
    private Date launchRequestTimestamp;
    
    private DbGlobalUser globalUser;
    
    public DbLtiUserRecord() {
        
    }
    
    /**
     * Gets the consumer key for the lti user.
     * 
     * @return the consumerKey
     */
    @Id
    @Column(name = "consumerKey_PK")
    public String getConsumerKey() {
        return consumerKey;
    }

    /**
     * set the consumer key for the lti user.
     * @param consumerKey This typically is a UUID that must be unique for each Tool Consumer.
     */
    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    /**
     * Gets the consumer id that is represented by the 'user_id' field from an LTI launch request.
     * This parameter is set from a Tool Consumer and can be represented in different ways from different
     * LTI Consumer systems.  
     * 
     * @return the consumerId The consumer id that was used as the 'user_id' field from the LTI launch request.
     */
    @Id
    @Column(name = "consumerId_PK")
    public String getConsumerId() {
        return consumerId;
    }

    /**
     * Sets the consumer id that is represented by the 'user_id' field from an LTI launch request.
     * This parameter is set from a Tool Consumer and can be represented in different ways from different
     * LTI Consumer systems.  
     * 
     * @param consumerId The consumer id that was used as the 'user_id' field from the LTI launch request.
     */
    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }
    
    @ManyToOne
    @JoinColumn(name="globalId_FK")
    public DbGlobalUser getGlobalUser() {
        return globalUser;
    }
    
    public void setGlobalUser(DbGlobalUser globalUser) {
        this.globalUser = globalUser;
    }

    /**
     * Gets the last time that an lti launch request was received for the lti user.  The timestamp is an
     * epoch value in milliseconds of when the last lti launch request was received.
     * 
     * @return the launchRequestTimestamp Epoch value in milliseconds that represents the timestamp of the last lti launch request.
     */
    @Temporal(TemporalType.TIMESTAMP)
    public Date getLaunchRequestTimestamp() {
        return launchRequestTimestamp;
    }

    /**
     * Sets the last time that an lti launch request was received for the lti user.  
     * The timestamp is when the last lti launch request was received.
     * 
     * @param launchRequestTimestamp that represents the timestamp of the last lti launch request.
     */
    public void setLaunchRequestTimestamp(Date launchRequestTimestamp) {
        this.launchRequestTimestamp = launchRequestTimestamp;
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (obj == null) {
            
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            
            return false;
        }
        
        DbLtiUserRecord other = (DbLtiUserRecord) obj;
        
        
        if (CompareUtil.equalsNullSafe(this.getConsumerKey(), other.getConsumerKey()) &&
            CompareUtil.equalsNullSafe(this.getConsumerId(), other.getConsumerId()) &&
            CompareUtil.equalsNullSafe(this.getGlobalUser(), other.getGlobalUser()) &&
            CompareUtil.equalsNullSafe(this.getLaunchRequestTimestamp(),  other.getLaunchRequestTimestamp())){
            return true;
        }
        

        return false;        
    }
    
    @Override
    public int hashCode() {
        
        int hash = 7;        
        hash = 89 * hash;
        
        // Consumer key and consumer id should be a unique record.
        if(getConsumerKey() != null){
            hash += getConsumerKey().hashCode();
        }
        
        if(getConsumerId() != null){
            hash += getConsumerId().hashCode();
        }
        
        return hash;
    }
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DbLtiUserRecord = [");
        sb.append(" consumerKey = ").append(getConsumerKey());
        sb.append(", consumerId = ").append(getConsumerId());
        sb.append(", globalUser = ").append(getGlobalUser());
        sb.append(", launchRequestTimestamp = ").append(getLaunchRequestTimestamp());
        sb.append("]");
        return sb.toString();

    }
}
