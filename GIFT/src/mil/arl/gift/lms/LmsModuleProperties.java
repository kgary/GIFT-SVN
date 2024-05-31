/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms;

import java.io.File;

import mil.arl.gift.common.module.AbstractModuleProperties;

/**
 * Contains the LMS module property values.
 * 
 * @author mhoffman
 *
 */
public class LmsModuleProperties extends AbstractModuleProperties {

    /** the properties file name*/
    private static final String PROPERTIES_FILE = "lms"+File.separator+"lms.properties";
    
    /** property names */
    private static final String LMS_CONNECTIONS_FILENAME = "LMS_Connections_Filename";
    
    /** singleton instance of this class */
    private static LmsModuleProperties instance = null;
    
    /**
     * Return the singleton instance of this class
     * 
     * @return LmsModuleProperties
     */
    public static synchronized LmsModuleProperties getInstance(){
        
        if(instance == null){
            instance = new LmsModuleProperties();
        }
        
        return instance;
    }
    
    /**
     * Class constructor
     */
    private LmsModuleProperties(){
        super(PROPERTIES_FILE);
    }
    
    /**
     * Return the name of the LMS connections file name used to configure the LMS module.
     * 
     * @return String the LMS connections file name to use
     */
    public String getLMSConnectionsFile(){
        return getPropertyValue(LMS_CONNECTIONS_FILENAME);
    }

}
