/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This exception is used for GIFT deployment mode errors.
 * 
 * @author mhoffman
 *
 */
public class DeploymentModeException extends RuntimeException {

    private static final long serialVersionUID = 1L;


    /**
     * Class constructor
     * 
     * @param message - the custom message about the error
     */
    public DeploymentModeException(String message){
		super(message);
	}
    
    /**
     * Class constructor - with exception parameter to specify the original exception that caused this validation
     * exception to be thrown.
     * 
     * @param message - the custom message about the error
     * @param exception original exception to reference in this new exception
     */
    public DeploymentModeException(String message, Exception exception){
        super(message, exception);
    }

}
