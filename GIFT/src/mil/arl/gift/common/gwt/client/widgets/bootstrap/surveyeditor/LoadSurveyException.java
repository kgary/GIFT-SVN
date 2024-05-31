/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor;

/**
 * Exception used to signal that an error has occurred during the loading of a survey from
 * the survey database into the Survey Editor panel.
 * 
 * @author nblomberg
 *
 */
public class LoadSurveyException extends Exception {

    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor - default
     * 
     * @param message - The message/reason for the exception.
     * @param cause - (optional) a caused by issue. 
     */
    public LoadSurveyException(String message, Throwable cause) {
        
        super(message, cause);
    }
    
    
}
