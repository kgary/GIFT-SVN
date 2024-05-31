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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import mil.arl.gift.common.lti.LtiUserId;


/**
 * The DbDataCollectionResultsLti class contains the hibernate mappings into the datacollectionresultslti database
 * table.  This class is used to store / track the results of a data collection that has the type of 'lti'.  In this 
 * case the lti user record information is used along with the data set id to map which log file(s) may belong to the user.
 * 
 * This is so that later on, an instructor can do a lookup on all the domain session log files based on the data set id in the
 * table.
 * 
 * @author nblomberg
 *
 */
@Entity
@Table(name = "datacollectionresultslti")
public class DbDataCollectionResultsLti implements Serializable {  
    

    private static final long serialVersionUID = 1L;
    
    /** The UUID for the course.  This id should never change and must be unique for each course. */
    private String consumerKey;
    /** The owner of the course.  A '*' indicates that the course is accessible for all users (such as in desktop mode or public courses). */
    private String consumerId;
    /** The relative path to the course.  This can change if the user renames the course. */
    private Date startTime;
    
    private Date endTime;
    
    private String dataSetId;
    
    private String messageLogFileName;
   
    /**
     * Constructor - needed for serialization
     */
    public DbDataCollectionResultsLti() {
        
    }
    
    /**
     * Constructor
     * 
     * @param ltiUserId The lti user id to be used.
     * @param dataSetId The data collection data set id that the results belong to.
     * @param logFileName The domain session log file name.
     * @param startTime The time that the result session was started.  
     */
    public DbDataCollectionResultsLti(LtiUserId ltiUserId, String dataSetId, String logFileName, Date startTime) {
        setConsumerKey(ltiUserId.getConsumerKey());
        setConsumerId(ltiUserId.getConsumerId());
        setDataSetId(dataSetId);
        setMessageLogFileName(logFileName);
        setStartTime(startTime);
    }

    /**
     * @return the consumerKey
     */
    @Id
    @Column(name = "consumerkey_fk")
    public String getConsumerKey() {
        return consumerKey;
    }

    /**
     * @param consumerKey the consumerKey to set
     */
    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    /**
     * @return the consumerId
     */
    @Id
    @Column(name = "consumerid_fk")
    public String getConsumerId() {
        return consumerId;
    }

    /**
     * @param consumerId the consumerId to set
     */
    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    /**
     * @return the startTime
     */
    @Id
    @Column(name = "starttime_pk")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    @Temporal(TemporalType.TIMESTAMP)
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * @return the dataSetId
     */
    @Column(name = "datasetid_fk")
    public String getDataSetId() {
        return dataSetId;
    }

    /**
     * @param dataSetId the dataSetId to set
     */
    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    /**
     * @return the messageLogFileName
     */
    public String getMessageLogFileName() {
        return messageLogFileName;
    }

    /**
     * @param messageLogFileName the messageLogFileName to set
     */
    public void setMessageLogFileName(String messageLogFileName) {
        this.messageLogFileName = messageLogFileName;
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
        
        if (getStartTime() != null) {
            hash += getStartTime().hashCode();
        }
        
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {

        if (obj == null) {

            return false;
        }

        if (getClass() != obj.getClass()) {

            return false;
        }
     
        DbDataCollectionResultsLti other = (DbDataCollectionResultsLti)obj;
        
        if (other.getConsumerKey() != null && other.getConsumerKey().equals(this.getConsumerKey())) {
            return false;
        }
        
        if (other.getConsumerId() != null && other.getConsumerId().equals(this.getConsumerId())) {
            return false;
        }
        
        if (other.getStartTime() != null && other.getStartTime().equals(this.getStartTime())) {
            return false;
        }
        
        return true;
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DbDataCollectionResultsLti = [");
        sb.append(" consumerKey = ").append(getConsumerKey());
        sb.append(", consumerId = ").append(getConsumerId());
        sb.append(", dataSetId = ").append(getDataSetId());
        sb.append(", messageLogFileName = ").append(getMessageLogFileName());
        sb.append(", startTime = ").append(getStartTime());
        sb.append(", endTime = ").append(getEndTime());
        sb.append("]");
        return sb.toString();

    }
}
