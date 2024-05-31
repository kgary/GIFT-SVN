/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.cat;

import java.io.File;

import mil.arl.gift.common.io.CommonProperties;

/**
 * Contains the CAT property values.
 * 
 * @author mhoffman
 *
 */
public class CATProperties extends CommonProperties {

    /** the properties file name*/
    private static final String PROPERTIES_FILE = "tools"+File.separator+"authoring"+File.separator+"cat"+File.separator+"cat.properties";
    
    /** singleton instance of this class */
    private static CATProperties instance = null;
    
    /** CAT specific property names */
    private static final String COURSE_SCHEMA_FILE = "CourseSchemaFile";   
    private static final String GENERATE_LESSON_MATERIAL_FILE = "GenerateLessonMaterialFile";
    
    /**
     * Return the singleton instance of this class
     * 
     * @return CATProperties
     */
    public static synchronized CATProperties getInstance(){
        
        if(instance == null){
            instance = new CATProperties();
        }
        
        return instance;
    }
    
    /**
     * Class constructor
     */
    private CATProperties(){
        super(PROPERTIES_FILE);
    }
    
    /**
     * Return the course schema file name
     * 
     * @return string - the schema filename
     */
    public String getSchemaFilename(){
        return getPropertyValue(COURSE_SCHEMA_FILE);
    }
    
    /**
     * Return whether or not to generated a lesson material file based on the entries
     * authored in a course file being saved.
     * 
     * @return boolean - the flag
     */
    public boolean getGenerateLessonMaterialFile(){
        return getPropertyBooleanValue(GENERATE_LESSON_MATERIAL_FILE);
    }

}
