/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.common;

/**
 * Represents an exception caused when dealing with an invalid LMS course record.
 * 
 * @author mhoffman
 *
 */
public class LmsInvalidCourseRecordException extends LmsException {	

	private static final long serialVersionUID = 1L;

	public LmsInvalidCourseRecordException(String msg, Exception cause) {
		super(msg, cause);
	}
}