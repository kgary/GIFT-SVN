/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

import mil.arl.gift.common.util.StringUtils;

/**
 * Contains the result of a variable states request usually sent to the training application.
 * This class is used to wrap the variable state values in order to link this result with the 
 * originating request.
 * @author mhoffman
 *
 */
public class VariablesStateResult implements TrainingAppState {

    /** a unique id found in the variable states request, linking this result with that request. */
    private String requestId;
    
    /** contains the state of variable(s) based on the request */
    private VariablesState variablesState;
    
    /**
     * Set attributes 
     * @param requestId a unique id found in the variable States request, linking this result with that request.
     * Can't be null or empty.
     * @param variablesState contains the state of variable(s) based on the request.  Can't be null.
     */
    public VariablesStateResult(String requestId, VariablesState variablesState){
        
        if(StringUtils.isBlank(requestId)){
            throw new IllegalArgumentException("The request id can't be null or empty");
        }
        
        this.requestId = requestId;
        
        if(variablesState == null){
            throw new IllegalArgumentException("The variables state can't be null");
        }
        
        this.variablesState = variablesState;
    }

    /**
     * Return the variables state requested.
     * 
     * @return can't be null.
     */
    public VariablesState getVariablesState() {
        return variablesState;
    }

    /**
     * Return a unique id found in the variables state request, linking this result with that request.
     * @return can't be null.
     */
    public String getRequestId() {
        return requestId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[VariableStatesResult: variablesState = ");
        builder.append(variablesState);
        builder.append(", requestId = ");
        builder.append(requestId);
        builder.append("]");
        return builder.toString();
    }
}
