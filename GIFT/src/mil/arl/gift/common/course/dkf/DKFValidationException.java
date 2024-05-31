/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf;

import mil.arl.gift.common.io.FileValidationException;

/**
 * This exception is used to contain information about an issue with configuring a domain
 * knowledge/assessment based on DKF.
 * 
 * @author mhoffman
 *
 */
public class DKFValidationException extends FileValidationException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * No-arg constructor needed by GWT RPC. This constructor does not create a valid instance of this class and should not be used 
     * under most circumstances
     */
    public DKFValidationException() {
        super();
    }
    
    /**
     * Set attributes
     * 
     * @param reason user friendly information about the exception.  Can't be null.
     * @param details developer friendly information about the exception. Can't be null.
     * @param dkfilename the course folder relative file name
     * @param cause the exception that caused this exception to be created.  Can be null.
     */
    public DKFValidationException(String reason, String details, String dkfilename, Throwable cause){
        super(reason, details, dkfilename, cause);

    }    
    
    
}
