/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.experiment;

/**
 * Used to contain information about a subject being created for an experiment.
 * This is normally used in a message sent from the domain to the tutor module.
 * 
 * @author mhoffman
 *
 */
public class SubjectCreated {

    private String preSessionId;
    
    private String courseId;
    
    /**
     * Set attributes
     * 
     * @param courseId a unique identifier for the course being executed by a subject of an experiment
     * @param preSessionId a unique identifier used by the tutor to identify this pre session logic and 
     * allow it to link the client's original experiment course request to incoming domain messages.
     */
    public SubjectCreated(String courseId, String preSessionId){
        
        if(courseId == null || courseId.isEmpty()){
            
        }else if(preSessionId == null || preSessionId.isEmpty()){
            
        }
        
        this.courseId = courseId;
        this.preSessionId = preSessionId;
    }

    /**
     * Return the unique identifier for the course being executed by a subject of an experiment
     * 
     * @return will not be null or empty
     */
    public String getCourseId() {
        return courseId;
    }

    /**
     * Return the unique identifier used by the tutor to identify this pre session logic and 
     * allow it to link the client's original experiment course request to incoming domain messages.
     * 
     * @return will not be null or empty
     */
    public String getPreSessionId() {
        return preSessionId;
    }

    @Override 
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[SubjectCreated: ");
        sb.append("course id = ").append(getCourseId());
        sb.append(", pre session id = ").append(getPreSessionId());
        sb.append("]");
        return sb.toString();
    }

}
