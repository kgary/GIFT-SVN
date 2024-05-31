/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import mil.arl.gift.common.gwt.client.RpcResponse;

/**
 * Contains a summary of the courses that were renamed
 */
public class CorrectCoursePathsResult extends RpcResponse {

    /** Describes which courses were renamed and their new names */
    private String renamedCourses = "";
    
    /** Describes courses that failed to be renamed and the errors that caused the failure */
	private String failedCourses = "";
    
	/**
	 * Class constructor.
	 */
	public CorrectCoursePathsResult() {
		super();
	}
	
	/**
	 * Adds course details to the renamed courses summary
	 * 
	 * @param oldCourseName The original name of the course before it was renamed
	 * @param newCourseName The new name of the course
	 */
	public void addRenamedCourse(String oldCourseName, String newCourseName) {
	    
	    /* 
	     * Note: Using String instead of StringBuilder/StringBuffer because they are not
	     * serializable. See https://github.com/gwtproject/gwt/issues/9462
	     */
	    renamedCourses += "<li>" + oldCourseName + " <i class=\"fa fa-arrow-right\"></i> " + newCourseName + "</li>";
	}
	
	/**
	 * Adds course details to the failure summary
	 *  
	 * @param courseName The course that failed to be renamed
	 * @param reason A user friendly message about the problem
	 * @param details The error details
	 */
	public void addFailedCourse(String courseName, String reason, String details) {
	    
	    /* 
         * Note: Using String instead of StringBuilder/StringBuffer because they are not
         * serializable. See https://github.com/gwtproject/gwt/issues/9462
         */
	    
	    failedCourses += "<li><b>" + courseName + "</b><ul><li>" + reason + "</li><li>" + details +"</li></ul></li>";
	}
	
	/**
	 * Gets the path correction summary. Can be empty
	 * 
	 * @return A summary describing which courses were renamed and any courses that failed during the operation.
	 */
	public String getSummary() {
	    
	    String summary = "";
	    
	    if(!renamedCourses.isEmpty()) {
	        summary = "The following courses were found with naming conventions that are no longer supported and "
	                + "have been renamed: <ul style=\"font-weight\": bold;\">" + renamedCourses + "</ul>";
        }
	    
	    if(!failedCourses.isEmpty()) {
	        
	        if(!renamedCourses.isEmpty()) {
	            summary += "<br/><br/>";
	        }
	        
	        summary += "An error occurred while correcting following courses. To prevent this error in the future, "
	                + "please edit the following courses and change the course name in the Course Creator:<ul>" + failedCourses + "</ul>";
        }
	    
	    return summary;
	}
	
	/**
	 * Gets whether or not there is a summary available
	 * 
	 * @return true if there are details available, false otherwise.
	 */
	public boolean hasSummary() {
	    return !(renamedCourses.isEmpty() && failedCourses.isEmpty());
	}
	
	@Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[CorrectCoursePathsResult: ");
        sb.append(", ").append(super.toString());
        sb.append("]");
        return sb.toString();
        
    }
	
}
