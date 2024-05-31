/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.mat;

import java.io.File;

import mil.arl.gift.common.io.CommonProperties;

/**
 * Contains the MAT property values.
 * 
 * @author mhoffman
 *
 */
public class MATProperties extends CommonProperties {

    /** the properties file name*/
    private static final String PROPERTIES_FILE = "tools"+File.separator+"authoring"+File.separator+"mat"+File.separator+"mat.properties";
    
    /** singleton instance of this class */
    private static MATProperties instance = null;
    
    /** MAT specific property names */
    private static final String METADATA_SCHEMA_FILE = "MetadataSchemaFile";  
    
    /**
     * Return the singleton instance of this class
     * 
     * @return MATProperties
     */
    public static synchronized MATProperties getInstance(){
        
        if(instance == null){
            instance = new MATProperties();
        }
        
        return instance;
    }
    
    /**
     * Class constructor
     */
    private MATProperties(){
        super(PROPERTIES_FILE);
    }
    
    /**
     * Return the Metadata schema file name
     * 
     * @return string
     */
    public String getSchemaFilename(){
        return getPropertyValue(METADATA_SCHEMA_FILE);
    }

}
