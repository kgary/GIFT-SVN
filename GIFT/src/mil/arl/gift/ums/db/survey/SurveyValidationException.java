/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.survey;

/**
 * This exception is used for survey validation errors.
 * 
 * @author mhoffman
 *
 */
public class SurveyValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Provides the error message.
     * 
     * @param message - the custom message about the validation error
     */
    public SurveyValidationException(String message){
		super(message);
	}
    
    /**
     * Provides the error message with exception parameter to specify the original exception that caused this validation
     * exception to be thrown.
     * 
     * @param message - the custom message about the validation error
     * @param exception original exception to reference in this new exception
     */
    public SurveyValidationException(String message, Exception exception){
        super(message, exception);
    }

}
