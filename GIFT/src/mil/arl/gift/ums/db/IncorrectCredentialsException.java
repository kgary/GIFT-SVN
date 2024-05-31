/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db;

/**
 * UMS exception for when the incorrect credentials was provided for a user.
 * 
 * @author mhoffman
 *
 */
public class IncorrectCredentialsException extends UMSDatabaseException {

    private static final long serialVersionUID = 1L;
    
    public IncorrectCredentialsException(String message){
        super(message);
    }
    
    public IncorrectCredentialsException(String message, Exception exception){
        super(message, exception);
    }

}
