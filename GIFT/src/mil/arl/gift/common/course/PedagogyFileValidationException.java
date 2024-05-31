/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course;

import mil.arl.gift.common.io.FileValidationException;

/**
 * This exception is used to contain information about an issue with configuring a pedagogical
 * model based on a pedagogical configuration file (e.g. eMAP).
 * 
 * @author mhoffman
 *
 */
public class PedagogyFileValidationException extends FileValidationException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Set attributes
     * 
     * @param reason user friendly information about the exception.  Can't be null.
     * @param details developer friendly information about the exception. Can't be null.
     * @param pedagogyFileName the file name of the pedagogy file that caused the exception. Can't be null.
     * @param cause the exception that caused this exception to be created.  Can be null.
     */
    public PedagogyFileValidationException(String reason, String details, String pedagogyFileName, Throwable cause){
        super(reason, details, pedagogyFileName, cause);

    }    
}
