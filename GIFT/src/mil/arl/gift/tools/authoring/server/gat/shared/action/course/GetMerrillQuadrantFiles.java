/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.metadata.QuadrantRequest;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for getting the list of files corresponding to an adaptive courseflow phase
 * 
 * @author nroberts
 */
public class GetMerrillQuadrantFiles implements Action<GetMerrillQuadrantFilesResult> {

	private String username;
	
	private String courseFilePath;
	
	/**
	 * mapping of adaptive courseflow phase (e.g. Rule) to the metadata search criteria for that phase.
	 */
	private Map<MerrillQuadrantEnum, QuadrantRequest> quadrantToRequest = new HashMap<MerrillQuadrantEnum, QuadrantRequest>();

    /**
     * No-arg constructor. Needed for serialization.
     */
    public GetMerrillQuadrantFiles() {
    	
    }

    /**
     * Creates a new action to get the list of files corresponding to a Merrill quadrant
     * 
     * @param username the username of the user making the request.  Used for read permission checks.
     * @param courseFilePath the location of the course folder.  Can't be null or empty.  Must exist.
     */
    public GetMerrillQuadrantFiles(String username, String courseFilePath) {
        setCourseFilePath(courseFilePath);
    	this.username = username;    	
    }
    
    private void setCourseFilePath(String courseFilePath){
        
        if(courseFilePath == null || courseFilePath.isEmpty()){
            throw new IllegalArgumentException("The course file path can't be null or empty (unless remediation content is included).");
        }
        
        this.courseFilePath = courseFilePath;
    }
    
    /**
     * Add a metadata search request for a specific adaptive courseflow phase (e.g. Rule).
     * 
     * @param request the request to add.  If null this method does nothing.  If another request already
     * exist for the phase then this request will replace the pre-existing request.
     */
    public void addRequest(QuadrantRequest request){
        
        if(request == null){
            return;
        }
        
        quadrantToRequest.put(request.getAdpativePhase(), request);
    }
    
    /**
     * Return the mapping of adaptive courseflow phase (e.g. Rule) to the metadata search criteria for that phase.
     * 
     * @return can be empty but not null.
     */
    public Map<MerrillQuadrantEnum, QuadrantRequest> getRequests(){
        return quadrantToRequest;
    }
    
    /**
     * the username of the user making the request.  Used for read permission checks.
     * 
     * @return a gift username
     */
	public String getUsername() {
		return username;
	}

	/**
	 * the location of the course folder to search for metadata.  
	 * 
	 * @return workspace relative path to a course folder.  Won't be null or empty.
	 */
	public String getCourseFilePath() {
		return courseFilePath;
	}

    @Override
    public String toString() {        
        
        StringBuilder builder = new StringBuilder();
        builder.append("[GetMerrillQuadrantFiles: username=");
        builder.append(username);
        builder.append(", courseFilePath=");
        builder.append(courseFilePath);
        builder.append(", quadrantRequests = {\n");
        for(MerrillQuadrantEnum quadrant : quadrantToRequest.keySet()){
            builder.append(quadrant).append(" : ").append(quadrantToRequest.get(quadrant)).append("\n");
        }
        builder.append("}");
        builder.append("]");
        return builder.toString();
    }
    
    
    
    
}
