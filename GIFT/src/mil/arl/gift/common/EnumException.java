/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;

/**
 * An EnumException occurs when an Enum class is unable to retrieve an enumeration
 * successfully.
 * 
 * @author mhoffman
 *
 */
public class EnumException extends RuntimeException implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create an exception with the specified message.
     *
     * @param msg The message assigned to this exception.
     * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public EnumException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
