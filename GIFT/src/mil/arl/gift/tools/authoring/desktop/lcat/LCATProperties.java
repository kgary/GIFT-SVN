/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.lcat;

import java.io.File;

import mil.arl.gift.common.io.CommonProperties;

/**
 * Contains the LCAT property values.
 * 
 * @author mhoffman
 *
 */
public class LCATProperties extends CommonProperties {

    /** the properties file name*/
    private static final String PROPERTIES_FILE = "tools"+File.separator+"authoring"+File.separator+"lcat"+File.separator+"lcat.properties";
    
    /** singleton instance of this class */
    private static LCATProperties instance = null;
    
    /** LCAT specific property names */
    private static final String LEARNER_SCHEMA_FILE = "LearnerConfigSchemaFile";    
    
    /**
     * Return the singleton instance of this class
     * 
     * @return LCATProperties
     */
    public static synchronized LCATProperties getInstance(){
        
        if(instance == null){
            instance = new LCATProperties();
        }
        
        return instance;
    }
    
    /**
     * Class constructor
     */
    private LCATProperties(){
        super(PROPERTIES_FILE);
    }
    
    /**
     * Return the Learner Config schema file name
     * 
     * @return string
     */
    public String getSchemaFilename(){
        return getPropertyValue(LEARNER_SCHEMA_FILE);
    }

}
