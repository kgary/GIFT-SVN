/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.spl;

import java.io.File;

import mil.arl.gift.common.module.AbstractModuleProperties;

/**
 * Handles parsing and retrieving properties for the SingleProcessLauncher.
 * 
 * @author cdettmering
 */
public class SingleProcessProperties extends AbstractModuleProperties {
	
    /** the properties file name*/
    private static final String PROPERTIES_FILE = "tools"+File.separator+"spl"+File.separator+"spl.properties";

	
	/** Property name to retrieve timeout value */
	private static final String TIMEOUT = "Timeout";
    
    /**
     * Creates a new SingleProcessProperties.
     */
    public SingleProcessProperties() {
    	super(PROPERTIES_FILE);
    }
    
	/**
	 * Gets the timeout value in seconds
	 * @return The amount of time to wait before declaring a GIFT timeout.
	 */
	public int getTimeout() {
		return getPropertyIntValue(TIMEOUT, 15);
	}
}
