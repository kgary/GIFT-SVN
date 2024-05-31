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
 * This exception is used to contain information about an issue with configuring a course
 * based on a course file.
 * 
 * @author mhoffman
 *
 */
public class CourseFileValidationException extends FileValidationException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Set attributes
     * 
     * @param reason user friendly information about the exception.  Can't be null.
     * @param details developer friendly information about the exception. Can't be null.
     * @param courseFileName the file name of the course that caused the exception. Can't be null.
     * @param cause the exception that caused this exception to be created.  Can be null.
     */
    public CourseFileValidationException(String reason, String details, String courseFileName, Throwable cause){
        super(reason, details, courseFileName, cause);

    }    
}
