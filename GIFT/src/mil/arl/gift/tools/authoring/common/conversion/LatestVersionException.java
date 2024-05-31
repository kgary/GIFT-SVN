/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.conversion;


/**
 * This exception is thrown if the conversion wizard recognizes that the file
 * that is being converted is already at the latest version of GIFT and
 * doesn't need to be converted.
 * 
 * If this exception is thrown the user should be given the choice of
 * loading the file into the tool or canceling.
 * 
 * @author mzellars
 * 
 */
public class LatestVersionException extends Exception {

	private static final long serialVersionUID = 1L;
	 
	/**
	 * Class constructor
	 * 
	 * @param message - the message indicating that the file is already at the latest version 
	 */
	public LatestVersionException(String message) {
		super(message);
	}
}
