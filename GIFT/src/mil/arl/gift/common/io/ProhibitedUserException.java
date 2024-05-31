/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

/**
 * Exception for when a login or other operation is using a prohibited username.
 * 
 * @author mhoffman
 *
 */
public class ProhibitedUserException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public ProhibitedUserException(String message){
        super(message);
    }

}
