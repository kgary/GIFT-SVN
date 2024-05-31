/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.conversion;


/**
 * This exception is thrown if the user tries to convert a file whose version is
 * not supported for conversion.  The conversion wizard can convert files that are
 * version 2.0 and later.
 * 
 * @author mzellars
 *
 */
public class UnsupportedVersionException extends Exception {
	
	private static final long serialVersionUID = 1L;
	 
	/**
	 * Class constructor
	 * 
	 * @param message - the message indicating that this file can't be converted because its version is unsupported.
	 */
	public UnsupportedVersionException(String message) {
		super(message);
	}
}
