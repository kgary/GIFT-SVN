/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums;

import java.io.File;

import mil.arl.gift.common.module.AbstractModuleProperties;

/**
 * Contains the UMS module property values.
 *
 * @author mhoffman
 *
 */
public class UMSModuleProperties extends AbstractModuleProperties {

    /** the properties file name */
    private static final String PROPERTIES_FILE = "ums"+File.separator+"ums.properties";
    
    private static final String SYSTEM_MSG_LOG_DURATION = "SystemMessageLogDuration";
    
    /** default hours to use for the system message log duration if the property value is not specified correctly*/
    private static final int DEFAULT_SYSTEM_MSG_LOG_DURATION = 24;

    /** singleton instance of this class */
    private static UMSModuleProperties instance = null;

    /**
     * Return the singleton instance of this class
     *
     * @return UMSProperties
     */
    public static synchronized UMSModuleProperties getInstance() {

        if (instance == null) {
            instance = new UMSModuleProperties();
        }

        return instance;
    }

    /**
     * Class constructor
     */
    private UMSModuleProperties() {
        super(PROPERTIES_FILE);
    }
    
    /**
     * Return the system message log duration property value.
     * If the property value is not specified or is less than 1, 
     * the default duration will be used.
     * 
     * @return the duration in hours that a system message log file should contain
     * messages for.
     */
    public int getSystemMsgLogDuration(){
        
        int value = getPropertyIntValue(SYSTEM_MSG_LOG_DURATION, DEFAULT_SYSTEM_MSG_LOG_DURATION);
        if(value <= 0){
            value = DEFAULT_SYSTEM_MSG_LOG_DURATION;
        }
        
        return value; 
    }
}
