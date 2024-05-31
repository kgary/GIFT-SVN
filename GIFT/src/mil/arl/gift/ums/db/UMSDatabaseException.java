/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db;

/**
 * This exception is used for UMS database errors.
 *  
 * @author mhoffman
 *
 */
public class UMSDatabaseException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Class constructor
     * 
     * @param message - the custom message about the error
     */
    public UMSDatabaseException(String message){
        super(message);
    }
    
    /**
     * Class constructor - with exception parameter to specify the original exception that caused this validation
     * exception to be thrown.
     * 
     * @param message - the custom message about the error
     * @param exception original exception to reference in this new exception
     */
    public UMSDatabaseException(String message, Exception exception){
        super(message, exception);
    }
}
