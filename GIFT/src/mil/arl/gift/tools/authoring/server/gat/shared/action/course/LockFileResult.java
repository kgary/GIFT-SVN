/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import mil.arl.gift.common.course.CourseFileAccessDetails;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * This is the gat RPC result when requesting to lock a GIFT file in a course folder (e.g. .course.xml, .dkf.xml, etc.).
 * 
 * @author mhoffman
 *
 */
public class LockFileResult extends GatServiceResult {
    
    /** contains information about the user(s) accessing a GIFT file in a course folder at this time */
    private CourseFileAccessDetails courseFileAccessDetails;
    
    /**
     * Required for GWT serialization
     */
    public LockFileResult(){ }
    
    /**
     * Set the information about the user(s) accessing a GIFT file in a course folder at this time.
     * 
     * @param courseFileAccessDetails should not be null as null indicates that no user access information is available
     * about a GIFT file in a course folder.
     */
    public void setCourseFileAccessDetails(CourseFileAccessDetails courseFileAccessDetails){
        this.courseFileAccessDetails = courseFileAccessDetails;
    }
    
    /**
     * Return the information about the user(s) accessing a GIFT file in a course folder at this time.
     * 
     * @return can be null if the result is a failure.  If a success than the value should not be null as an indication
     * that there is access information on a GIFT file in a course folder, even if no users are currently accessing it at this time.
     */
    public CourseFileAccessDetails getCourseFileAccessDetails(){
        return courseFileAccessDetails;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[LockFileResult: ");
        sb.append("courseFileAccessDetails = ").append(getCourseFileAccessDetails());
        sb.append(", ").append(super.toString());
        sb.append("]");
        return sb.toString();
    }

}
