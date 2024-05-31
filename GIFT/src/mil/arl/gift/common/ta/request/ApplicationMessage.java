/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.request;

import mil.arl.gift.common.DomainAssessmentContent;

/**
 * Contains parameters for showing a message in the external training application when that
 * application is running while in a GIFT course.
 * 
 * @author mhoffman
 *
 */
public class ApplicationMessage implements DomainAssessmentContent {

    private String message;
    
    /**
     * Set attribute
     * 
     * @param message the message to show in the external training application.  Can't be null or empty.
     */
    public ApplicationMessage(String message){
        
        if(message == null || message.isEmpty()){
            throw new IllegalArgumentException("The message can't be null or empty.");
        }
        
        this.message = message;
    }
    
    /**
     * Return the message to show in the external training application.
     * 
     * @return the message to show.  Won't be null or empty.
     */
    public String getMessage(){
        return message;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ApplicationMessage: message=");
        builder.append(message);
        builder.append("]");
        return builder.toString();
    }
    
}
