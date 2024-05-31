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
 * This class contains a collection of survey check requests.  Survey check requests contains
 * information about survey references that need to be verified against the Survey database.
 * 
 * @author mhoffman
 *
 */
public class SurveyListCheckRequest {

    /** 
     * Collection of survey check requests mapped by a key that provides some useful information about the requests mapped to it.
     * (e.g. a key of "Hemorrhage Control lesson" would mean the request are somehow related to a lesson of that name) */
    private Map<String, List<SurveyCheckRequest>> requests;
    
    /**
     * Class constructor - set attribute(s).
     * 
     * @param requests - collection of survey check requests mapped by a key that provides some useful information about the 
     *                  requests mapped to it.  Can't be null or empty.
     */
    public SurveyListCheckRequest(Map<String, List<SurveyCheckRequest>> requests){
        
        if(requests == null || requests.isEmpty()){
            throw new IllegalArgumentException("The requests must contain at least one element.");
        }
        
        this.requests = requests;
    }
    
    /**
     * Return the collection of survey check requests mapped by a key that provides some useful information about the requests mapped to it.
     * 
     * @return Map<String, List<SurveyCheckRequest>> - the collection for this survey check request object.  Won't be null.
     */
    public Map<String, List<SurveyCheckRequest>> getRequests(){
        return requests;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[SurveyListCheckRequest: ");
        
        sb.append("requests = {");
        for(String key : requests.keySet()){
            sb.append(" [ ").append(key).append(" : ").append(requests.get(key)).append(" ],");
        }
        sb.append("}");
        
        sb.append("]");
        return sb.toString();
    }
}
