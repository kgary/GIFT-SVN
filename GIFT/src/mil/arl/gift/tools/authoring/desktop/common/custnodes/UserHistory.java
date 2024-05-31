/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.common.custnodes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import mil.arl.gift.common.io.CommonProperties;

/**
 * This class is responsible for loading the user history of entries for the XML editor custom nodes.  
 * The values of the entries will be used to populate some dialog comboboxes.
 * 
 * @author mhoffman
 *
 */
public class UserHistory extends CommonProperties {

    /** the user history for DAT */
    private static final String HISTORY_FILE = "tools"+File.separator+"authoring"+File.separator+"AuthoringToolsUserHistory.txt";   
    
    /**
     * Properties
     */
    
    // DAT
    public static final String DKFS = "dkf";
    public static final String CONDITION = "condition";
    public static final String LEARNER_ACTIONS = "learnerActionsFiles";
    public static final String SIMILE_CONFIGS = "SIMILEConfigs";
    
    // CAT
    public static final String FILES = "Files";
    public static final String INTEROP = "interop";
    public static final String LESSON_MATERIALS = "lessonMaterialFiles";
    
    // SCAT
    public static final String WRITER = "writer";
    public static final String SENSOR = "sensor";
    public static final String FILTER = "filter";
    
    // LCAT
    public static final String TRANSLATOR = "translator";
    public static final String PREDICTOR = "predictor";
    public static final String CLASSIFIER = "classifier";
    
    
    /** singleton instance of this class */
    private static UserHistory instance = null;
    
    /**
     * Return the singleton instance of this class
     * 
     * @return UserHistory
     */
    public static synchronized UserHistory getInstance(){
        
        if(instance == null){
            instance = new UserHistory();
        }
        
        return instance;
    }
    
    /**
     * Private Constructor - parse file
     */
    private UserHistory(){ 
        super(HISTORY_FILE);
    }
    
    @Override
    public void setProperty(String propertyName, String value) throws FileNotFoundException, IOException{
        super.setProperty(propertyName, value);
    }
    
    @Override
    public void setProperty(String propertyName, String[] values) throws FileNotFoundException, IOException{
        super.setProperty(propertyName, values);
    }
}
