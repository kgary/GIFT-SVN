/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import java.util.List;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for getting the list of practice applications for a Merrill's branch point
 * 
 * @author nroberts
 */
public class GetPracticeApplications implements Action<GetPracticeApplicationsResult> {

	private String username;
	
	private String courseFilePath;
	
	private List<String> requiredConcepts;
	
	private List<String> otherCourseConcepts;

    /**
     * No-arg constructor. Needed for serialization.
     */
    public GetPracticeApplications() {
    	
    }

    /**
     * Creates a new action for getting the list of practice applications for a Merrill's branch point
     * 
     * @param username the username of the user making the request
     * @param courseFilePath the location of the course folder
     * @param requiredConcepts the concepts to look for
     * @param otherCourseConcepts the other course concepts not required but will be used to populate the other practice applications
     * available to the course
     */
    public GetPracticeApplications(String username, String courseFilePath, List<String> requiredConcepts, List<String> otherCourseConcepts) {
        
        if(courseFilePath == null || courseFilePath.isEmpty()){
            throw new IllegalArgumentException("The course file path can't be null or empty.");
        }
        
    	this.username = username;
    	this.courseFilePath = courseFilePath;
    	this.requiredConcepts = requiredConcepts;
    	this.otherCourseConcepts = otherCourseConcepts;
    }
    
	public String getUsername() {
		return username;
	}

	public String getCourseFilePath() {
		return courseFilePath;
	}

    /**
     * The required concepts to use when finding content files
     * 
     * @return collection of required concepts to use when searching for content files
     * for a adaptive courseflow phase
     */
    public List<String> getRequiredConcepts() {
        return requiredConcepts;
    }
    
    /**
     * The other course concepts not including in the required concepts.
     * 
     * @return collection of the other course concepts.
     */
    public List<String> getOtherCourseConcepts() {
        return otherCourseConcepts;
    }
    
    @Override
    public String toString() {        
        
        StringBuilder builder = new StringBuilder();
        builder.append("[GetPracticeApplications: username=");
        builder.append(username);
        builder.append(", courseFilePath=");
        builder.append(courseFilePath);
        builder.append(", requiredConcepts=");
        builder.append(requiredConcepts);
        builder.append(", otherCourseConcepts=");
        builder.append(otherCourseConcepts);
        builder.append("]");
        return builder.toString();
    }
}
