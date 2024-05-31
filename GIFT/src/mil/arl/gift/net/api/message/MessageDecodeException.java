/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message;

/**
 * This exception is used for message decoding errors.
 * 
 * @author mhoffman
 *
 */
public class MessageDecodeException extends RuntimeException {

	/**
     * default serial UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Class constructor
     *
     * @param className the codec class that was unable to decode a message
     * @param message a message about the exception
     */
    public MessageDecodeException(String className, String message) {
        super(className + " was unable to decode message because " + message);
    }

    /**
     * Class constructor
     *
     * @param className the codec class that was unable to decode a message
     * @param message a message about the exception
     * @param e the original exception referenced by this new exception
     */
    public MessageDecodeException(String className, String message, Throwable e) {
        super(className + " was unable to decode message because " + message, e);
    }
}
