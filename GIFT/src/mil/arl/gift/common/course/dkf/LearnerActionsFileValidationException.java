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
 * This exception is used to contain information about an issue with a learner actions file
 * used in a DKF.
 * 
 * @author mhoffman
 *
 */
public class LearnerActionsFileValidationException extends FileValidationException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Set attributes
     * 
     * @param reason user friendly information about the exception.  Can't be null.
     * @param details developer friendly information about the exception. Can't be null.
     * @param learnerActionsFilename the course folder relative file name
     * @param cause the exception that caused this exception to be created.  Can be null.
     */
    public LearnerActionsFileValidationException(String reason, String details, String learnerActionsFilename, Throwable cause){
        super(reason, details, learnerActionsFilename, cause);

    }    
    
    
}
