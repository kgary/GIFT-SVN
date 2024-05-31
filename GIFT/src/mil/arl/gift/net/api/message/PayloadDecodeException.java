/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message;

/**
 * This exception is used for when a message payload could not be decoded
 *
 * @author jleonard
 */
public class PayloadDecodeException extends MessageDecodeException {

    private static final long serialVersionUID = 1L;
    
    private Message incompleteMessage;

    /**
     * Class constructor
     *
     * @param className The class where the error occurred
     * @param incompleteMessage The message that couldn't decode the payload
     * @param e the original exception referenced by this new exception
     */
    public PayloadDecodeException(String className, Message incompleteMessage, Throwable e) {
        super(className, "The payload could not be decoded", e);

        this.incompleteMessage = incompleteMessage;
    }

    /**
     * Gets the message that was received with all the header information filled
     * out
     *
     * @return Message The message that couldn't decode the payload
     */
    public Message getIncompleteMessage() {

        return incompleteMessage;
    }
}
