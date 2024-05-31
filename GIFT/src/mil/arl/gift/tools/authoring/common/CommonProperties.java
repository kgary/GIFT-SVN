/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common;

import java.io.File;

/**
 * Contains the common authoring property values.
 * 
 * @author mhoffman
 *
 */
public class CommonProperties extends mil.arl.gift.common.io.CommonProperties {

    /** the properties file name*/
    private static final String PROPERTIES_FILE = "tools"+File.separator+"authoring"+File.separator+"authoring.common.properties";
    
    /** singleton instance of this class */
    private static CommonProperties instance = null;
    
    private static final String USE_DB = "UseDBConnection";
    
    public static final String COMMON_NAMESPACE = "http://GIFT.com/common";

    
    /**
     * Return the singleton instance of this class
     * 
     * @return PCATProperties
     */
    public static synchronized CommonProperties getInstance(){
        
        if(instance == null){
            instance = new CommonProperties();
        }
        
        return instance;
    }
    
    /**
     * Class constructor
     */
    private CommonProperties(){
        super(PROPERTIES_FILE);
    }

    
    /**
     * Return whether or not the CAT should establish a connection to the survey database
     * for retrieving and validating information.
     * 
     * @return boolean
     */
    public boolean shouldUseDBConnection(){
        return getPropertyBooleanValue(USE_DB);
    }
}
