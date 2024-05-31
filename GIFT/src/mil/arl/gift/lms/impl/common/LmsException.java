/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.common;

/**
 * Represents an LMS exception.
 * 
 * @author mhoffman
 *
 */
public class LmsException extends Exception {

	private static final long serialVersionUID = 1L;

	public LmsException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public LmsException(String msg) {
	    super(msg);
	}
}