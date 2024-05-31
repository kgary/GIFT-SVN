/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor;

/**
 * Exception used to signal that an error has occurred during the saving of a survey to
 * the survey database from the Survey Editor panel.
 * 
 * @author nblomberg
 *
 */
public class SaveSurveyException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor - default
     * 
     * @param message - The message/reason for the exception.
     */
    public SaveSurveyException(String message) {
        
        super(message);
    }
    
    /**
     * Constructs a save survey exception with the given cause
     * 
     * @param message - The message/reason for the exception.
     * @param cause - the cause of the exception. Can be null.
     */
    public SaveSurveyException(String message, Throwable cause) {
        
        super(message, cause);
    }
    
    
}
