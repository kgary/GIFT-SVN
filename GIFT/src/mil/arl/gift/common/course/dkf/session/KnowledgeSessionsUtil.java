/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.session;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility class to consolidate common logic for knowledge sessions handling.
 * This is meant to be a static utility class only (not singleton).
 *  
 * @author nblomberg
 *
 */
public class KnowledgeSessionsUtil  {

    
   
    /**
     * Constructor - private
     */
    private KnowledgeSessionsUtil() {
        
    }
    
    
    /**
     * Filters a map of knowledge sessions by a course id. This will return a new hashmap of 
     * filtered entities.  
     * 
     * @param courseSourceId The course source id to filter on.  Cannot be null or empty.
     * @param knowledgeSessionMap The knowledge session map to filter.  
     * @return A new filtered map that includes sessions matching the course id that was filtered on.  
     * Can be empty if there were no matches found.
     */
    public static Map<Integer, AbstractKnowledgeSession> filterKnowledgeSessions(String courseSourceId, Map<Integer, AbstractKnowledgeSession> knowledgeSessionMap) {
        
        if (courseSourceId == null || courseSourceId.isEmpty()) {
            throw new IllegalArgumentException("The courseId cannot be null or empty.");
        }
        
        if (knowledgeSessionMap == null) {
            throw new IllegalArgumentException("The knowledgeSessionMap cannot be null.");
        }
        
        Map<Integer, AbstractKnowledgeSession> filteredMap = new HashMap<>();
       
        Iterator<Integer> keyItr = knowledgeSessionMap.keySet().iterator();
        while(keyItr.hasNext()){
            Integer key = keyItr.next();
            AbstractKnowledgeSession aKnowledgeSession = knowledgeSessionMap.get(key); 
            if (aKnowledgeSession.getCourseSourceId().compareTo(courseSourceId) == 0) {
                filteredMap.put(key, aKnowledgeSession);
            }
        }
        
        return filteredMap;
    }

}
