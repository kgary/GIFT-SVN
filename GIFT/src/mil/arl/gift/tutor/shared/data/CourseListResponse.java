/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.data;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contains the response to a course list request for Simple login.
 * 
 * Note: this should only be used for the logic behind the course list for Simple login
 * 
 * @author mhoffman
 *
 */
public class CourseListResponse implements IsSerializable {

    /**
     * error information, if applicable
     */
    private String errorReason;
    private String errorDetails;
    
    /**
     * contains information about the courses found
     */
    private ArrayList<GwtDomainOption> courses;
    
    /**
     * Default constructor required for GWT.
     */
    public CourseListResponse(){ }
    
    /**
     * The course list request has ended in an error.
     * 
     * @param errorReason contains information about the error.  Can't be null or empty.
     * @param errorDetails contains optional details about the error.
     */
    public CourseListResponse(String errorReason, String errorDetails){
        
        if(errorReason == null || errorReason.isEmpty()){
            throw new IllegalArgumentException("The reason for an error can't be null or empty.");
        }
        
        this.errorReason = errorReason;
        this.errorDetails = errorDetails;
    }
    
    /**
     * The course list request resulted in zero or more courses being found.
     * 
     * @param courses information about the course.  Can be empty but not null.
     */
    public CourseListResponse(ArrayList<GwtDomainOption> courses){
        
        if(courses == null){
            throw new IllegalArgumentException("The course list can't be null.");
        }
        
        this.courses = courses;
    }
    
    /**
     * Return the reason behind an error in retrieving the course list.
     * 
     * @return error reason.  Will be null if there is no error and there are zero or 
     * more courses found
     */
    public String getErrorReason(){
        return errorReason;
    }
    
    /**
     * Return the details of the error.
     * 
     * @return error details.  Can be null.  Null doesn't indicate no error happened,
     * just that there are no details for an error.
     */
    public String getErrorDetails(){
        return errorDetails;
    }
    
    /**
     * Return the courses found for the course list request.
     * 
     * @return can be null if there was an error, otherwise it will contain zero or more entries.
     */
    public ArrayList<GwtDomainOption> getCourses(){
        return courses;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[CourseListResponse: ");
        
        if(getErrorReason() != null){
            sb.append("error reason = ").append(getErrorReason());
            sb.append(" details = ").append(getErrorDetails());
        }else{
            sb.append("courses found = ").append(getCourses().size());
        }
        
        sb.append("]");
        return sb.toString();
    }
}
