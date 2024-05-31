/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import mil.arl.gift.tools.authoring.server.gat.shared.action.util.StringListResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for getting the list of files corresponding to a Merrill quadrant
 * 
 * @author nroberts
 */
public class GetMetadataFilesForMerrillQuadrant implements Action<StringListResult> {

	private String username;
	
	private String contentFilePath;
	
	private String quadrantName;

    /**
     * No-arg constructor. Needed for serialization.
     */
    public GetMetadataFilesForMerrillQuadrant() {
    	
    }

    /**
     * Creates a new action to get the list of files corresponding to a Merrill quadrant
     * 
     * @param username the username of the user making the request
     * @param contentFilePath the location of the content file to get metadata for
     * @param quadrantName the name of the Merrill's quadrant
     * @param concepts the concepts to look for
     */
    public GetMetadataFilesForMerrillQuadrant(String username, String courseFilePath, String quadrantName) {
    	this.username = username;
    	this.contentFilePath = courseFilePath;
    	this.quadrantName = quadrantName;
    }
    
	public String getUsername() {
		return username;
	}

	public String getContentFilePath() {
		return contentFilePath;
	}

	public String getQuadrantName() {
		return quadrantName;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[GetMetadataFilesForMerrillQuadrant: ");
        sb.append("username = ").append(username);
        sb.append(", contentFilePath").append(contentFilePath);
        sb.append(", quadrantName").append(quadrantName);
        sb.append("]");

        return sb.toString();
    } 
}
