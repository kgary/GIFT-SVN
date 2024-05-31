/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.pcat;

import java.io.File;

import mil.arl.gift.common.io.CommonProperties;

/**
 * Contains the PCAT property values.
 * 
 * @author mhoffman
 *
 */
public class PCATProperties extends CommonProperties {

    /** the properties file name*/
    private static final String PROPERTIES_FILE = "tools"+File.separator+"authoring"+File.separator+"pcat"+File.separator+"pcat.properties";
    
    /** singleton instance of this class */
    private static PCATProperties instance = null;
    
    /** PCAT specific property names */
    private static final String EMAP_SCHEMA_FILE = "eMAPSchemaFile";  
    
    /**
     * Return the singleton instance of this class
     * 
     * @return PCATProperties
     */
    public static synchronized PCATProperties getInstance(){
        
        if(instance == null){
            instance = new PCATProperties();
        }
        
        return instance;
    }
    
    /**
     * Class constructor
     */
    private PCATProperties(){
        super(PROPERTIES_FILE);
    }
    
    /**
     * Return the eMAP schema file name
     * 
     * @return string
     */
    public String getSchemaFilename(){
        return getPropertyValue(EMAP_SCHEMA_FILE);
    }
}
