/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This class contains a get experiment request used to retrieve experiment contents from the database.
 *
 * @author nroberts
 */
public class GetExperimentRequest {

    /** ID for the experiment being requested */
    private String experimentId;

    /**
     * Class constructor
     * 
     * @param experimentId ID of the experiment being requested
     */
    public GetExperimentRequest(String experimentId) {    
        
        if(experimentId == null){
            throw new IllegalArgumentException("The experiment id cannot be null.");
        }
        this.experimentId = experimentId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[GetExperimentRequest: ");
        sb.append("experiment id = ");
        sb.append(getExperimentId());
        sb.append("]");

        return sb.toString();
    }

}
