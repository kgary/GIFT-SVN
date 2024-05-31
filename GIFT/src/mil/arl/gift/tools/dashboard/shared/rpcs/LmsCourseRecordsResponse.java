/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import mil.arl.gift.common.LMSCourseRecords;
import mil.arl.gift.common.gwt.client.RpcResponse;

/**
 * The LmsCourseRecordsResponse contains the list of lmscourse records from the server.
 * 
 * @author nblomberg
 *
 */
public class LmsCourseRecordsResponse extends RpcResponse {
    
    // Contains the list of lms records that were found on the server. 
    private LMSCourseRecords courseRecords = null;
    
    /**
     * Default Constructor
     *
     * Required for GWT
     */
    public LmsCourseRecordsResponse() {
    }

    /**
     * Constructor - The response contains the lms course records that were retreived from the server.
     * 
     * @param userSessionId - The user session id from the server.
     * @param browserSessionId - The browser session id from the server.
     * @param success - true if the rpc is a success, false otherwise.
     * @param response - Error or success response.
     * @param records - The list of records that were found on the server.
     */
    public LmsCourseRecordsResponse(String userSessionId, String browserSessionId, boolean success, String response, LMSCourseRecords records) {       
        super(userSessionId, browserSessionId, success, response);
        
        courseRecords = records;
    }    
   
    /**
     * Accessor to get the course records.
     * 
     * @return LmsCourceRecords - The records for this response.  Can be null.
     */
    public LMSCourseRecords getCourseRecords() {
        return courseRecords;
    }
    
    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("[LmsCourseRecordsResponse: courseRecords = {");
        sb.append(courseRecords);
        sb.append("}");
        sb.append(",\n").append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
   
}
