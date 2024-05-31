/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor;

import java.io.File;

import mil.arl.gift.common.module.AbstractModuleProperties;

/**
 * Contains the Sensor module property values.
 * 
 * @author mhoffman
 *
 */
public class SensorModuleProperties extends AbstractModuleProperties {

    /** the properties file name*/
    public static final String PROPERTIES_FILE = "sensor"+File.separator+"sensor.properties";
    
    /** singleton instance of this class */
    private static SensorModuleProperties instance = null;
    
    /** 
     * sensor module specific property names 
     */
    
    /** path to sensor module configuration file containing sensors, filters and writers */
    public static final String SENSOR_CONFIG_FILE = "SensorConfigurationFile";
    
    /** minimum amount of seconds between the same error being sent over the network by a sensor instance */
    private static final String MIN_SEC_BTW_ERRORS = "MinSecBtwErrors";
    private static final double DEFAULT_MIN_SEC_BTW_ERRORS = 5.0;
    
    /**
     * Return the singleton instance of this class
     * 
     * @return SensorModuleProperties
     */
    public static synchronized SensorModuleProperties getInstance(){
        
        if(instance == null){
            instance = new SensorModuleProperties();
        }
        
        return instance;
    }
    
    /**
     * Class constructor
     */
    private SensorModuleProperties(){
        super(PROPERTIES_FILE);
    }
    
    /**
     * Return the sensor configuration file
     * 
     * @return String
     */
    public String getSensorConfigFile(){
        return getPropertyValue(SENSOR_CONFIG_FILE);
    }
    
    /**
     * Return the minimum amount of seconds between the same error being sent over the network by a sensor instance
     * 
     * @return double
     */
    public double getMinSecBtwErrors(){
        return getPropertyDoubleValue(MIN_SEC_BTW_ERRORS, DEFAULT_MIN_SEC_BTW_ERRORS);
    }
}
