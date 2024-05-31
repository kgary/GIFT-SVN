/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.scat;

import java.io.File;

import mil.arl.gift.common.io.CommonProperties;

/**
 * Contains the SCAT property values.
 * 
 * @author mhoffman
 *
 */
public class SCATProperties extends CommonProperties {

    /** the properties file name*/
    private static final String PROPERTIES_FILE = "tools"+File.separator+"authoring"+File.separator+"scat"+File.separator+"scat.properties";
    
    /** singleton instance of this class */
    private static SCATProperties instance = null;
    
    /** LCAT specific property names */
    private static final String SENSOR_SCHEMA_FILE = "SensorConfigSchemaFile";  
    private static final String TEST_SENSORS = "TestSensors";
    
    /**
     * Return the singleton instance of this class
     * 
     * @return SCATProperties
     */
    public static synchronized SCATProperties getInstance(){
        
        if(instance == null){
            instance = new SCATProperties();
        }
        
        return instance;
    }
    
    /**
     * Class constructor
     */
    private SCATProperties(){
        super(PROPERTIES_FILE);
    }
    
    /**
     * Return the Sensor Config schema file name
     * 
     * @return string
     */
    public String getSchemaFilename(){
        return getPropertyValue(SENSOR_SCHEMA_FILE);
    }
    
    /**
     * Return whether to test the sensors in a sensor configuration file
     * during the user initiated GIFT Validation process.
     * 
     * @return boolean
     */
    public boolean shouldTestSensors(){
        return getPropertyBooleanValue(TEST_SENSORS);
    }

}
