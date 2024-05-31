/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;

import java.io.Serializable;
import java.util.List;

/**
 * This is a wrapper around error details which includes the error details message
 * and an optional stack trace.  This is needed for when multiple issues are delivered 
 * to the client and the client needs to customize or group the error details.  E.g.
 * the GAT receives multiple course validation issues and would like to organize
 * the errors by severity as well as add severity message to the original error details message. 
 * 
 * @author mhoffman
 *
 */
public class ErrorDetails implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /** 
     * used to highlight when poor details are provided
     * Before the logic was to throw an illegalargumentexception but that made tracking down
     * issues on the client side harder because all we would see is an obfuscated stacktrace.
     */
    private static final String BAD_DETAILS = "IF YOU SEE THIS MESSAGE THEN LET A DEVELOPER KNOW BECAUSE THIS SHOULD CONTAIN SOMETHING USEFUL, NOT THIS!";

    private String details;
    
    private List<String> stacktrace;
    
    /**
     * Required for GWT.  Do not call.
     */
    public ErrorDetails(){ }
    
    /**
     * Set attributes.
     * 
     * @param details an author friendly message about the error.  Should not be null or empty.
     * @param stacktrace the serialized, GWT client friendly, stack trace for the error.  Can be null or empty.
     */
    public ErrorDetails(String details, List<String> stacktrace){
        this.setDetails(details);
        this.setStacktrace(stacktrace);
    }

    /**
     * Return an author friendly message about the error.
     * 
     * @return Can't be null or empty.
     */
    public String getDetails() {
        return details;
    }

    private void setDetails(String details) {
        
        if(details == null || details.isEmpty()){
            this.details = BAD_DETAILS;
        }else{        
            this.details = details;
        }
    }

    /**
     * Return the serialized, GWT client friendly, stack trace for the error.
     * 
     * @return Can be null or empty.
     */
    public List<String> getStacktrace() {
        return stacktrace;
    }

    private void setStacktrace(List<String> stacktrace) {
        this.stacktrace = stacktrace;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ErrorDetails: details=");
        builder.append(details);
        builder.append(", stacktrace=\n");
        if(stacktrace != null && !stacktrace.isEmpty()){

            for (String e : stacktrace) {
                builder.append(e).append("\n");
            }
            
        }else{
            builder.append("No stack trace available.\n");
        }

        builder.append("]");
        return builder.toString();
    }
    
    
}
