/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This class contains a get survey request used to retrieve survey contents from the database.
 *
 * @author cragusa
 */
public class GetSurveyRequest {

    /** label for the survey being requested */
    private String giftKey;
    
    /** unique id for the survey context that is associated with the survey being requested */
    private int surveyContextId;

    /**
     * Class constructor
     * 
     * @param surveyContextId unique id for the survey context that is associated with the survey being requested 
     * @param giftKey label for the survey being requested
     */
    public GetSurveyRequest(int surveyContextId, String giftKey) {    
        
        if(surveyContextId < 0){
            throw new IllegalArgumentException("The survey context id of "+surveyContextId+" must be non-negative.");
        }
        this.surveyContextId = surveyContextId;
        
        if(giftKey == null){
            throw new IllegalArgumentException("The gift key can't be null.");
        }
        this.giftKey = giftKey;
    }

    public String getGiftKey() {
        return giftKey;
    }

    public int getSurveyContextId() {
        return surveyContextId;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[GetSurveyRequest: ");
        sb.append("survey context id = ").append(getSurveyContextId());
        sb.append(", gift key = ").append(getGiftKey());
        sb.append("]");

        return sb.toString();
    }

}
