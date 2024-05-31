/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.gwt.client.RpcResponse;

/**
 * A response to course validation in the dashboard.  Contains information about the course being validated 
 * including recommendation enumeration which can be an indication of failed validation logic.
 * 
 * @author mhoffman
 *
 */
public class ValidateCourseResponse extends RpcResponse {

    /** contains info about the course that was validated */
    private DomainOption course;
    
    /**
     * Default Constructor
     *
     * Required for GWT
     */
    @SuppressWarnings("unused")
    private ValidateCourseResponse() {
    }
    
    /**
     * Set information about the course validation result.
     * 
     * @param userSessionId - The user session id from the server.
     * @param browserSessionId - The browser session id from the server.
     * @param success - true if the rpc is a success, false otherwise.
     * @param response - Error or success response message.  Should not be null if success is false.
     * @param course - information about the course that was validated.  If validation failed the recommendation enumeration value
     * should be one of the unavailable types.
     */
    public ValidateCourseResponse(String userSessionId, String browserSessionId, boolean success, String response, DomainOption course) {
       
        super(userSessionId, browserSessionId, success, response);
     
        if(success && course == null){
            throw new IllegalArgumentException("The course can't be null when indicating a successful response.");
        }
        
        this.course = course;
    }
    
    /**
     * Return information about the course that was validated. If validation failed the recommendation enumeration value
     * should be one of the unavailable types.
     * 
     * @return can be null if the response success value is false.
     */
    public DomainOption getCourse(){
        return course;
    }
}
