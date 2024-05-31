/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import generated.course.Concepts;
import mil.arl.gift.common.course.CourseConceptsUtil;

/**
 * The Instantiate Learner Request is used to instantiate the learner after the learner
 * has logged into the system.
 * 
 * @author mhoffman
 *
 */
public class InstantiateLearnerRequest{
    
    /** the LMS user name for this learner */
    private String lmsUserName = null;
    
    /** the XML Learner configuration information. */
    private String configuration = null;
    
    /** (optional) course concepts being used in this course, can be used to query for initial learner state information */
    private Concepts courseConcepts = null;
    
	/**
	 * Class constructor - no lms user name provided
	 */
    public InstantiateLearnerRequest(){

    }
    
    /**
     * Class constructor 
     * 
     * @param lmsUserName - LMS user name for the learner being instantiated
     */
    public InstantiateLearnerRequest(String lmsUserName){
    	this.lmsUserName = lmsUserName;
    }
    
    /**
     * Return the LMS user name of this learner
     * 
     * @return String
     */
    public String getLMSUserName(){
    	return lmsUserName;
    }
    
    /**
     * Sets the Learner configuration.
     * 
     * @param config the XML Learner configuration or null if there is no learner configuration set.
     */
    public void setLearnerConfig(String config) {
    	this.configuration = config;
    }
    
    /**
     * Returns the Pedagogical configuration.
     * 
     * @return the Pedagogical configuration or null if no learner configuration was set.
     */
    public String getLearnerConfig() {
    	return configuration;
    }
    
    /**
     * Return the course concepts being used in this course, can be used to query for initial learner state information
     * @return can be null.
     */
    public Concepts getCourseConcepts() {
        return courseConcepts;
    }

    /**
     * Set the course concepts being used in this course, can be used to query for initial learner state information
     * @param courseConcepts can be null.
     */
    public void setCourseConcepts(Concepts courseConcepts) {
        this.courseConcepts = courseConcepts;
    }

	@Override
    public String toString(){

	    StringBuffer sb = new StringBuffer();
		sb.append("[InstantiateLearnerRequest: ");
		sb.append("LMS-username = ").append(getLMSUserName());
		
		if(getLearnerConfig() != null){
		    sb.append(", learner config = ").append(getLearnerConfig());
		}
		
		if(getCourseConcepts() != null){
		    sb.append(", concepts = ").append(CourseConceptsUtil.getConceptNameList(courseConcepts));
		}
		
		sb.append("]");

		return sb.toString();
    }
}
