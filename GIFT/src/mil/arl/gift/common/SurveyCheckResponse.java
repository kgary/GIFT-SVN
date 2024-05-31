/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.List;
import java.util.Map;

/**
 * This class contains responses to a survey check request.
 * 
 * @author mhoffman
 *
 */
public class SurveyCheckResponse {
    
    /** collection of responses, mapped in the same manner as the survey check request */
    private Map<String, List<ResponseInterface>> responses;
    
    /**
     * Class constructor - set attribute(s).
     * 
     * @param responses - collection of responses, mapped in the same manner as the survey check request.  Can't be null or empty.
     */
    public SurveyCheckResponse(Map<String, List<ResponseInterface>> responses){
        
        if(responses == null || responses.isEmpty()){
            throw new IllegalArgumentException("The responses must contain at least one element.");
        }
        
        this.responses = responses;
    }
    
    /**
     * Return the collection of responses, mapped in the same manner as the survey check request.
     * 
     * @return Map<String, List<ResponseInterface>> - the responses
     */
    public Map<String, List<ResponseInterface>> getResponses(){
        return responses;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[SurveyCheckResponse: ");
        
        sb.append("responses = {");
        for(String key : responses.keySet()){
            sb.append(" [ ").append(key).append(" : ").append(responses.get(key)).append(" ],");
        }
        sb.append("}");
        
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Inner interface used for classes that want to provide responses to requests.
     * 
     * @author mhoffman
     *
     */
    public static interface ResponseInterface{
        
    }
    
    /**
     * Inner class that encompasses a successful response to a survey check request
     * 
     * @author mhoffman
     *
     */
    public static class SuccessResponse implements ResponseInterface{
        
    }
    
    /**
     * Inner class that encompasses a failure response to a survey check request
     * 
     * @author mhoffman
     *
     */
    public static class FailureResponse implements ResponseInterface{
        
        /** The failure message to provide as a response */
        private String message;
        
        /** The index of the course object that this survey check response corresponds to */
        private Integer courseObjectIndex = null;
        
        /**
         * Class constructor - set attribute(s)
         * 
         * @param message The failure message to provide as a response
         */
        public FailureResponse(String message){
            
            if(message == null || message.length() == 0){
                throw new IllegalArgumentException("The message must contain content.");
            }
            
            this.message = message;
        }
        
        /**
         * Class constructor - set attribute(s)
         * 
         * @param message The failure message to provide as a response
         * @param indexThe index of the course object that this survey check request corresponds to. Can be null.
         */
        public FailureResponse(String message, Integer courseObjectIndex){
            
            if(message == null || message.length() == 0){
                throw new IllegalArgumentException("The message must contain content.");
            }
            
            if(courseObjectIndex != null && courseObjectIndex < 0) {
            	throw new IllegalArgumentException("The course object index cannot be negative.");
            }
            
            this.message = message;
            this.courseObjectIndex = courseObjectIndex;
        }
        
        /**
         * Return the failure message to provide as a response
         * 
         * @return String - the failure message
         */
        public String getMessage(){
            return message;
        }
        
        /**
         * Return the index of the course object that this survey check response corresponds to
         * 
         * @return the index of the course object. Can be null.
         */
        public Integer getCourseObjectIndex() {
        	return courseObjectIndex;
        }
        
        /**
         * Set the index of the course object that this survey check response corresponds to
         * 
         * @param courseObjectIndex the index of the course object. Can be null.
         */
        public void setCourseObjectIndex(Integer courseObjectIndex){
            this.courseObjectIndex = courseObjectIndex;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[FailureResponse: ");
            sb.append("message = ").append(getMessage());
            sb.append("courseObjectIndex = ").append(getCourseObjectIndex());
            sb.append("]");
            return sb.toString();
        }
    }
}
