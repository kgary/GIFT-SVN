/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.server;

import java.util.List;

import mil.arl.gift.common.ert.event.AbstractEvent;
import mil.arl.gift.common.ert.event.DomainSessionEvent;

/**
 * Contains common report generation logic used by both DataCollectionServices and ErtRpcServiceImpl.
 * 
 * @author mhoffman
 *
 */
public class ReportGenerationUtil {

    
    /**
     * Return the elapsed DKF time for the event provided if the event happened during a DKF session (i.e. between a lesson started and lesson completed).  
     * 
     * @param lessonStartedEvents collection of lesson started events, in order that they happened, during a domain session.  If null or empty
     * this method returns null.
     * @param lessonCompletedEvents collection of lesson completed events, in order that they happened, during a domain session.  The size of this
     * collection should be equal to the lessonStartedEvents collection to indicate that all DKF lessons had a beginning and end.  Null would indicate
     * that the one and only lesson started event had no lesson completed because the course end prematurely.
     * @param eventToUpdate the event to get the elapsed DKF time for.  If null this method returns null.
     * @return the elapsed DKF time in seconds for the event provided.  Can be null under several circumstances (see entire Javadoc).
     */
    public static Double getElapsedDKFTime(List<AbstractEvent> lessonStartedEvents, List<AbstractEvent> lessonCompletedEvents, DomainSessionEvent eventToUpdate){
        
        if(lessonStartedEvents == null){
            return null;
        }else if(eventToUpdate == null){
            return null;
        }
        
        long eventTime = eventToUpdate.getTime();
        
        // determine if the event is after a DKF start
        for(int startIndex = 0; startIndex < lessonStartedEvents.size(); startIndex++){
            
            AbstractEvent lessonStartedEvent = lessonStartedEvents.get(startIndex);
            if(eventTime >= lessonStartedEvent.getTime()){
                // the event happened after this lesson started event, the event maybe
                // in this DKF window
                
                if(lessonCompletedEvents != null && lessonCompletedEvents.size() > startIndex){
                    //there is a corresponding lesson completed to the lesson started
                    
                    AbstractEvent lessonCompletedEvent = lessonCompletedEvents.get(startIndex);
                    if(eventTime <= lessonCompletedEvent.getTime()){
                        // found the appropriate DKF window for this event
                        return Double.valueOf((eventTime - lessonStartedEvent.getTime()) / 1000.0);
                    }
                }else{
                    // this current DKF window never ended with a lesson completed which most likely means
                    // the course ended prematurely some time after the DKF started
                    
                    return Double.valueOf((eventTime - lessonStartedEvent.getTime()) / 1000.0);
                }
            }
        }
        
        return null;
        
    }
}
