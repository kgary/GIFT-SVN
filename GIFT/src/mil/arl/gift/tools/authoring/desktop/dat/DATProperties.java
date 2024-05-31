/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.dat;

import java.io.File;

import mil.arl.gift.common.io.CommonProperties;

/**
 * Contains the DAT property values.
 * 
 * @author mhoffman
 *
 */
public class DATProperties extends CommonProperties {

    /** the properties file name*/
    private static final String PROPERTIES_FILE = "tools"+File.separator+"authoring"+File.separator+"dat"+File.separator+"dat.properties";
    
    /** singleton instance of this class */
    private static DATProperties instance = null;
    
    /** DAT specific property names */
    private static final String DKF_SCHEMA_FILE = "DKFSchemaFile"; 
    
    /**
     * Return the singleton instance of this class
     * 
     * @return DATProperties
     */
    public static synchronized DATProperties getInstance(){
        
        if(instance == null){
            instance = new DATProperties();
        }
        
        return instance;
    }
    
    /**
     * Class constructor
     */
    private DATProperties(){
        super(PROPERTIES_FILE);
    }
    
    /**
     * Return the DKF schema file name
     * 
     * @return string
     */
    public String getSchemaFilename(){
        return getPropertyValue(DKF_SCHEMA_FILE);
    }

}
