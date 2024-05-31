/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.tarat;

import java.io.File;

import mil.arl.gift.common.io.CommonProperties;

/**
 * Contains the TARAT property values.
 * 
 * @author mhoffman
 *
 */
public class TARATProperties extends CommonProperties {

    /** the properties file name*/
    private static final String PROPERTIES_FILE = "tools"+File.separator+"authoring"+File.separator+"tarat"+File.separator+"tarat.properties";
    
    /** singleton instance of this class */
    private static TARATProperties instance = null;
    
    /** TARAT specific property names */
    private static final String TRAINING_APP_REF_SCHEMA_FILE = "TrainingAppRefSchemaFile";  
    
    /**
     * Return the singleton instance of this class
     * 
     * @return TARATProperties
     */
    public static synchronized TARATProperties getInstance(){
        
        if(instance == null){
            instance = new TARATProperties();
        }
        
        return instance;
    }
    
    /**
     * Class constructor
     */
    private TARATProperties(){
        super(PROPERTIES_FILE);
    }
    
    /**
     * Return the Training Application Reference schema file name
     * 
     * @return string
     */
    public String getSchemaFilename(){
        return getPropertyValue(TRAINING_APP_REF_SCHEMA_FILE);
    }

}
