/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.experiment;

import java.io.Serializable;
import java.util.Date;

import mil.arl.gift.common.lti.LtiUserId;

/**
 * The DataCollectionResultsLti class is a client side (gwt) compatible class that is used
 * to contain the DbDataCollectionResults data for the client.  This class stores the results
 * of a data collection (experiment) of an lti type.
 * 
 * @author nblomberg
 *
 */
public class DataCollectionResultsLti implements Serializable {

	private static final long serialVersionUID = -3449863746309592830L;
	
	/** The lti user id. */
	private LtiUserId ltiUserId;

	/** The data set id that the results belong to. */
	private String dataSetId;
	/** times defined when a user started the course and when the course was ended */
    private Date startTime;
    private Date endTime;    
    
    /** the domain session message log file name (e.g. domainSession7_uId1_2015-08-21_15-13-17.log) */
    private String messageLogFilename;
    
    /**
     * Public no-arg constructor required by GWT RPC
     */
    public DataCollectionResultsLti(){
    	
    }
    
    /**
     * Creates a new experiment subject
     * 
     * @param id the subject's ID
     * @param startTime time when the user started the experiment course
     * @param endTime time when the user ended the experiment course
     * @param messageLogFileName the domain session message log file name
     */
    public DataCollectionResultsLti(LtiUserId ltiUserId, String dataSetId, String messageLogFileName, Date startTime, Date endTime){
    	
    	if(ltiUserId == null){
    		throw new IllegalArgumentException("The lti user id cannot be null.");
    	}
    	
    	if(dataSetId == null){
            throw new IllegalArgumentException("The data set id cannot be null.");
        }
    	
    	this.ltiUserId = ltiUserId;
    	this.dataSetId = dataSetId;
    	
    	this.startTime = startTime;
    	this.endTime = endTime;
    	this.messageLogFilename = messageLogFileName;   	
    }
	 
	    
    /**
     * Sets this experiment subject's ID
     * 
     * @param experimentSubjectId the new ID
     */
    public void setLtiUserId(LtiUserId ltiUserId){
        this.ltiUserId = ltiUserId;
    }
    
    /**
     * Gets this experiment subject's ID
     * 
     * @return the ID
     */
    public LtiUserId getLtiUserId(){
        return ltiUserId;
    }

    /**
     * @return the dataSetId
     */
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
     * Gets the time when this subject started the experiment course
     * 
     * @return the start time
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the time when this subject started the experiment course
     * 
     * @param startTime the start time
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    /**
     * Gets the time when this subject ended the experiment course
     * 
     * @return the end time
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Sets the time when this subject ended the experiment course
     * 
     * @param endTime the end time
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets the subject's domain session message log file name
     * 
     * @return the log file name
     */
    public String getMessageLogFilename() {
        return messageLogFilename;
    }

    /**
     *
     * Gets the subject's domain session message log file name
     * 
     * @param messageLogFilename the log file name
     */
    public void setMessageLogFilename(String messageLogFilename) {
        this.messageLogFilename = messageLogFilename;
    }
    
    @Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("[DataCollectionResultsLti:");
		sb.append(" ltiUserId = ").append(getLtiUserId());
		sb.append(", dataSetId = ").append(getDataSetId());
		sb.append(", startTime = ").append(getStartTime());
		sb.append(", endTime = ").append(getEndTime());
		sb.append(", messageLogFileName = ").append(getMessageLogFilename());
		sb.append("]");

		return sb.toString();
	}
}
