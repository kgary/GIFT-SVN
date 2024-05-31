/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.nuxeo;

import mil.arl.gift.common.io.DetailedException;

/**
 * Exception indicating that a quota has been exceeded for a resource
 * 
 */
public class QuotaExceededException extends DetailedException {
    private static final long serialVersionUID = -8245633761412815199L;
    
    /**
     * 
     * @param reason the general, user friendly message for the error
     * @param details The more detailed developer error message
     * @param cause the exception that caused the QuotaExceededException, can be null
     */
    public QuotaExceededException(String reason, String details, Throwable cause) {
        super(reason, details, cause);
    }
}
