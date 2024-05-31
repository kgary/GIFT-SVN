/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db;

/**
 * UMS exception for when a user was not found.
 * 
 * @author mhoffman
 *
 */
public class UserNotFoundException extends UMSDatabaseException {

    private static final long serialVersionUID = 1L;
    
    public UserNotFoundException(String message){
        super(message);
    }
    
    public UserNotFoundException(String message, Exception exception){
        super(message, exception);
    }

}
