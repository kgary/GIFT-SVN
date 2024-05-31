/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message;

/**
 * This exception is used for message encoding errors.
 * 
 * @author mhoffman
 *
 */
public class MessageEncodeException extends RuntimeException {

	/**
     * default serial UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Class constructor 
     * 
     * @param className the codec message class that was unable to encode correctly
     * @param message a message about the exception 
     */
    public MessageEncodeException(String className, String message){
		super(className + " was unable to encode message because " + message);
	}
    
    /**
     * Class constructor 
     * 
     * @param className the codec message class that was unable to encode correctly
     * @param message a message about the exception 
     * @param cause the cause of this encode exception
     */
    public MessageEncodeException(String className, String message, Exception cause){
        super(className + " was unable to encode message because " + message, cause);
    }
}
