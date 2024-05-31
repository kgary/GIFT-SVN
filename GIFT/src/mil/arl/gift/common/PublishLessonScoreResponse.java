/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.Map;

import mil.arl.gift.common.course.CourseRecordRef;

/**
 * This class contains a response to a publish lesson score request.  The response contains information
 * about the published scores in each of the LMS connections available.
 * 
 * @author mhoffman
 *
 */
public class PublishLessonScoreResponse {

    /**
     * contains the unique identifier for the score record for each LMS connection
     * 
     *  key: LMS connection
     *  value: unique reference of the score record
     */
    private Map<LMSConnectionInfo, CourseRecordRef> publishedRecordsByLMS;

    /**
     * Class constructor - set attribute(s).
     * 
     * @param publishedRecordByLMS contains the unique reference for the score record for each LMS connection.
     * Can be empty but not null.
     */
    public PublishLessonScoreResponse(Map<LMSConnectionInfo, CourseRecordRef> publishedRecordByLMS){
        
        if(publishedRecordByLMS == null){
            throw new IllegalArgumentException("The records can't be null.");
        }
        
        this.publishedRecordsByLMS = publishedRecordByLMS;
    }
    
    /**
     * Return the collection that contains the unique reference for the score record for each LMS connection
     * 
     * @return contains the unique reference for the score record for each LMS connection
     */
    public Map<LMSConnectionInfo, CourseRecordRef> getPublishedRecordsByLMS(){
        return publishedRecordsByLMS;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[PublishLessonScoreResponse: ");
        
        sb.append("publishedRecords = {");
        for(LMSConnectionInfo connection : getPublishedRecordsByLMS().keySet()){
            sb.append("\n{LMS = ").append(connection).append(", Id = ").append(getPublishedRecordsByLMS().get(connection)).append("},");
        }
        sb.append("}");
        
        sb.append("]");
        return sb.toString();
    }
}
