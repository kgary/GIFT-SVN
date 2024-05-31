/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * Specialization of ConfigurationException used for module connection runtime exceptions.
 * 
 * @author cragusa
 *
 */
public class ModuleConnectionConfigurationException extends ConfigurationException {
	
	/**
     * default serial version UID
     */
	private static final long serialVersionUID = 1L;	

    /**
     * Class constructor
     * 
     * @param reason user friendly information about the exception.  Can't be null.
     * @param details developer friendly information about the exception. Can't be null.
     * @param cause the exception that caused this exception to be created.  Can be null.
     */
	public ModuleConnectionConfigurationException(String reason, String details, Throwable cause) {
		super(reason, details, cause);
	}

}
