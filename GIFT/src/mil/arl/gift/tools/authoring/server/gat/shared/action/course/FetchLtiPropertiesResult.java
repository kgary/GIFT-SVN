/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;


/**
 * Class that represents the result of a FetchLtiProperties rpc to the server.
 * 
 * @author nblomberg
 *
 */
public class FetchLtiPropertiesResult extends GatServiceResult{

    /** The course id (UUID) that represents the course. */
	private String courseId;
	
	/** Indicates if the course has data collection data sets created/in use.  This means that
	 * the course has dependencies on data collection. 
	 */
	private boolean hasDataSets = false;
	
	/**
	 * Class constructor
	 * For serialization only.
	 */
	public FetchLtiPropertiesResult() {
    }


	/** 
	 * Gets the course id (UUID) that refers to the course. 
	 * 
	 * @param The UUID of the course that is used for the lti launch request.
	 */
    public String getCourseId() {
        return courseId;
    }

    /** 
     * Sets the course id (UUID) that refers to the course.
     * 
     * @param courseId The UUID of the course that is used for the lti launch request.
     */
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
    
    

    /**
     * @return the hasDataSets
     */
    public boolean hasDataSets() {
        return hasDataSets;
    }


    /**
     * @param hasDataSets the hasDataSets to set
     */
    public void setHasDataSets(boolean hasDataSets) {
        this.hasDataSets = hasDataSets;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[FetchLtiPropertiesResult: ");
        builder.append(" courseId=").append(getCourseId());
        builder.append(" hasDataSets=").append(hasDataSets());
        builder.append(", ").append(super.toString());
        builder.append("]");
        return builder.toString();
    }
	
	
	
}
