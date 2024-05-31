/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.util.StringUtils;


/**
 * The GetCourseListResponse contains an array of 'course' data that can be displayed
 * on the client.  
 * 
 * @author nblomberg
 *
 */
public class GetCourseListResponse extends RpcResponse {

    /** The list of courses */
    private ArrayList<DomainOption> courseList = new ArrayList<DomainOption>();

    /** The fully qualified paths to any courses that were automatically upconverted to the latest schema version */
    private Set<String> upconvertedCourses = new HashSet<>();

    /**
     * Indicates whether or not there are courses in the user's workspace with file paths that are not in the 
     * correct format of 'CourseName/CourseName.course.xml' where 'CourseName' matches the name element 
     * in the course.xml file  
     */
    private boolean hasInvalidPaths = false;

    /**
     * Default Constructor
     *
     * Required for GWT
     */
    public GetCourseListResponse() {
    }
    
    /**
     * An rpc that is used to get the existing session information of a user (if it exists).
     * 
     * @param userSessionId - The user session id from the server.
     * @param browserSessionId - The browser session id from the server.
     * @param success - true if the rpc is a success, false otherwise.
     * @param response - Error or success response.
     * @param courses - The courses that were retrieved from the server.  If null, the default empty list will be used.
     */
    public GetCourseListResponse(String userSessionId, String browserSessionId, boolean success, String response, ArrayList<DomainOption> courses) {
       
        super(userSessionId, browserSessionId, success, response);
        
        if(courses != null){
            courseList = courses;
        }
    }
    
    /**
     * Sets whether or not there are courses in the user's workspace with file paths that are not in the 
     * correct format of 'CourseName/CourseName.course.xml' where 'CourseName' matches the name element 
     * in the course.xml file  
     *
     * @param hasInvalidPaths true if there are course paths that do not match the 'CourseName/CourseName.course.xml' format
     */
    public void setHasInvalidPaths(boolean hasInvalidPaths) {
    	this.hasInvalidPaths = hasInvalidPaths;
    }
    
    /**
     * Accessor to get the screen state of the response.
     * @return ScreenEnum - The screen state of the response (should not be null).
     */
    public ArrayList<DomainOption> getCourseList() {
        return courseList;
    }

    /**
     * Retrieves the set of courses that were automatically upconverted.
     * @return the upconverted courses. Can be empty.
     */
    public Set<String> getUpconvertedCourses() {
        return upconvertedCourses;
    }
    
    /**
     * Gets whether or not there are courses in the user's workspace with file paths that are not in the 
     * correct format of 'CourseName/CourseName.course.xml' where 'CourseName' matches the name element 
     * in the course.xml file  
     *
     * @return true if there are course paths that do not match the 'CourseName/CourseName.course.xml' format, false otherwise
     */
    public boolean hasInvalidPaths() {
    	return hasInvalidPaths;
    }
   
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        
        sb.append("[GetCourseListResponse: ");
        sb.append("courseList = {");
        for(DomainOption option : courseList) {
            sb.append(option.toString()).append(", ");
        }
        sb.append("}");
        sb.append(", upconvertedCourses = {");
        sb.append(StringUtils.join(", ", getUpconvertedCourses()));
        sb.append("}");
        sb.append(", hasInvalidPaths = ").append(hasInvalidPaths);
        sb.append(", userSessionId = ").append(getUserSessionId());
        sb.append(", browserSessionId = ").append(getBrowserSessionId());
        sb.append(", success = ").append(isSuccess());
        sb.append(", response = ").append(getResponse());
        sb.append(", additionalInformation = ").append(getAdditionalInformation());
        sb.append("]");
        
        return sb.toString();
    }
}
