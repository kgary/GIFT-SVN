/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import mil.arl.gift.common.gwt.client.DetailedRpcResponse;

/**
 * Result containing the results of copying a course.
 * 
 * @author bzahid
 */
public class CopyCourseResult extends DetailedRpcResponse {

	/** Whether or not the course already exists */
	private boolean courseAlreadyExists = false;	
		
	/**
	 * Class constructor.
	 */
	public CopyCourseResult() {
		super();
	}
	
	/**
	 * Gets whether or not the course already exists
	 * 
	 * @return true if a course with the given name already exists
	 */
	public boolean courseAlreadyExists() {
		return courseAlreadyExists;
	}
	
	/**
	 * Sets whether or not the course already exists
	 * 
	 * @param courseAlreadyExists true if a course with the given name already exists
	 */
	public void setCourseAlreadyExists(boolean courseAlreadyExists) {
		this.courseAlreadyExists = courseAlreadyExists;
	}
	
	@Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[CopyCourseResult: ");
        sb.append("courseAlreadyExists: ").append(courseAlreadyExists);
        sb.append(", ").append(super.toString());
        sb.append("]");
        return sb.toString();
        
    }
	
}
