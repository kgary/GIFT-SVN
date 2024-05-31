/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message;

/**
 * This exception is used for exceptions thrown when handling an decoded message.
 *  
 * @author mhoffman
 *
 */
public class MessageHandlingException extends RuntimeException {

    /**
     * default serial UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Class constructor 
     * 
     * @param exception the exception thrown during handling of a message
     */
    public MessageHandlingException(Exception exception){
        super(exception);
    }
}
