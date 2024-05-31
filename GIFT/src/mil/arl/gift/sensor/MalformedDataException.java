/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor;

import mil.arl.gift.common.io.DetailedException;

/**
 * A malformed data exception class used when reading sensor data.
 * 
 * @author mhoffman
 *
 */
public class MalformedDataException extends DetailedException {

    private static final long serialVersionUID = 1L;

    /**
     * Set attributes
     * 
     * @param reason user friendly information about the exception.  Can't be null.
     * @param details developer friendly information about the exception. Can't be null.
     * @param cause the exception that caused this exception to be created.  Can be null.
     */
    public MalformedDataException(String reason, String details, Throwable cause){
        super(reason, details, cause);
    }
}
