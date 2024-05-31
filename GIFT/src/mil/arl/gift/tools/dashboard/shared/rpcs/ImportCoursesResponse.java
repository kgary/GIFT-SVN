/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.gwt.client.RpcResponse;

/**
 * A response containing information about file conflicts between 
 * the files to be imported and the files existing on the server
 * 
 * @author bzahid
 */
public class ImportCoursesResponse extends RpcResponse {
	
    /** 
     * Maps survey image filenames from a course import that already exist in the
     * 'surveyWebResources' folder and prompts to overwrite the images.  
     */
	private Map<String, String> imagesAndPromptsMap = new HashMap<String, String>();
	
	/**
	 * Maps course names from a course import that already exist in the user's workspace
	 * and prompts to rename them.
	 */
	private Map<String, String> coursesAndPromptsMap = new HashMap<String, String>();
	
	/**
	 * Public no-arg constructor. Required by GWT RPC.
	 */
	public ImportCoursesResponse() {
		
	}
	
	/**
	 * Returns whether or not the import contains conflicting files.
	 * 
	 * @return true if conflicts were found, false otherwise.
	 */
	public boolean hasConflicts() {
		return !imagesAndPromptsMap.isEmpty() || !coursesAndPromptsMap.isEmpty();
	}
		
	/**
	 * Returns a map of filenames and user-friendly messages with details about the file conflicts
	 * 
	 * @return a map of filenames and user-friendly messages with details about the file conflicts
	 */
	public Map<String, String> getImageOverwritePrompts() {
		return imagesAndPromptsMap;
	}
	
	/**
	 * Returns a map of course file paths and user-friendly messages with details about the file conflicts
	 * 
	 * @return a map of course file pathss and user-friendly messages with details about the file conflicts
	 */
	public Map<String, String> getCourseRenamePrompts() {
		return coursesAndPromptsMap;
	}
		
	/**
	 * Stores a path to a conflicting image in the "surveyWebResources" directory with 
	 * a prompt to overwrite the file
	 *  
	 * @param imageFile The path to the conflicting image file
	 * @param overwritePrompt The prompt to overwrite the file
	 */
	public void addImageOverwritePrompt(String imageFile, String overwritePrompt) {
		imagesAndPromptsMap.put(imageFile, overwritePrompt);
	}
	
	/**
	 * Stores a path to a conflicting course in the user's workspace with a
	 * prompt to rename the course.
	 * 
	 * @param courseFile The conflicting course file
	 * @param renamePrompt A prompt to rename the conflicting course
	 */
	public void addCourseRenamePrompt(String courseFile, String renamePrompt) {
		coursesAndPromptsMap.put(courseFile, renamePrompt);
	}
	
	@Override
	public String toString() {
	    
	    StringBuffer sb = new StringBuffer();
	    
	    sb.append("[ImportCoursesResponse: ");
	    if(!imagesAndPromptsMap.isEmpty()) {
	        sb.append("imagesAndPromptsMap = {");
	        for(String imagePath : imagesAndPromptsMap.keySet()) {
	            sb.append(imagePath).append(" : ");
	            sb.append(imagesAndPromptsMap.get(imagePath));
	            sb.append(", ");
	        }
	        sb.append("}");
	    }
	    
	    if(!coursesAndPromptsMap.isEmpty()) {
            sb.append("coursesAndPromptsMap = {");
            for(String coursePath : coursesAndPromptsMap.keySet()) {
                sb.append(coursePath).append(" : ");
                sb.append(imagesAndPromptsMap.get(coursePath));
                sb.append(", ");
            }
            sb.append("}");
        }
        sb.append(", userSessionId = ").append(getUserSessionId());
        sb.append(", browserSessionId = ").append(getBrowserSessionId());
        sb.append(", success = ").append(isSuccess());
        sb.append(", response = ").append(getResponse());
        sb.append(", additionalInformation = ").append(getAdditionalInformation());
	    sb.append("]");
	    
	    return sb.toString();
	}
}
