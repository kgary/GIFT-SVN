/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain;

import java.io.File;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.module.AbstractModuleProperties;
import mil.arl.gift.net.util.Util;

/**
 * Contains the Domain module property values.
 * 
 * @author mhoffman
 *
 */
public class DomainModuleProperties extends AbstractModuleProperties {

    /** the properties file name*/
    private static final String PROPERTIES_FILE = "domain"+File.separator+"domain.properties";
    
    protected static final String BYPASS_SURVEY_QUESTIONS = "BypassSurveyQuestions";
    protected static final String RESTART_SCENARIO = "RestartScenario";
    protected static final String AUTO_COMPLETE_SCENARIO = "AutoCompleteScenario";
    protected static final String ENTITY_TABLE_ENTITY_TIMEOUT_MILLIS     ="EntityTableEntityTimeoutMillis";
    protected static final String ENTITY_TABLE_DEAD_RECKONING_INTERVAL   ="EnityTableDeadReckoningInterval";

    protected static final String BYPASS_CHAT_WINDOWS = "BypassChatWindows";
    private static final String VALIDATE_COURSE_AT_STARTUP = "ValidateCoursesAtStartup";
    private static final String SHOW_NODE_STATUS_TOOL = "ShowNodeStatusTool";
    private static final String COMM_APP_DIR    = "CommunicationAppDirectory";
    private static final String COMM_APP_FILE   = "CommunicationAppFile";
    
    /** Default property values */  
    public static final int DEFAULT_AUTO_COMPLETE_TIME = -1;
    
    /** singleton instance of this class */
    private static DomainModuleProperties instance = null;
    
    /**
     * Return the singleton instance of this class
     * 
     * @return UMSProperties
     */
    public static synchronized DomainModuleProperties getInstance(){
        
        if(instance == null){
            instance = new DomainModuleProperties();
        }
        
        return instance;
    }
    
    /**
     * Class constructor
     */
    private DomainModuleProperties(){
        super(PROPERTIES_FILE);
        
        //
        //check for required properties
        //
        String host = getDomainContentServerHost();
        if(host == null || host.isEmpty()){
            throw new ConfigurationException("Invalid domain content server host property.",
                    "The domain content server host domain module property can't be null or empty.",
                    null);
        }
        
        int port = getDomainContentServerPort();
        if(!Util.isValidPort(port)){
            throw new ConfigurationException("Invalid domain content server port property.",
                    "The domain content server port domain module property of "+port+" is not valid.",
                    null);
        }
        
        String commAppFile = getPropertyValue(COMM_APP_FILE);
        if(commAppFile == null || commAppFile.isEmpty()){
            throw new ConfigurationException("Invalid "+COMM_APP_FILE+" property.",
                    "The communication application file property can't be null or empty.",
                    null);
        }
        
        String commAppDir = getPropertyValue(COMM_APP_DIR);
        if(commAppDir == null || commAppDir.isEmpty()){
            throw new ConfigurationException("Invalid "+COMM_APP_DIR+" property.",
                    "The communication application directory property can't be null or empty.",
                    null);
        }

    }
    
    /**
     * Get the user-specified value to be used as the entity timeout in the entity table
     * (A known entity that isn't updated within this time period will be removed from the table.)
     * 
     * @return timeout value in milliseconds.
     */
    public int getEntityTableEntityTimeoutMillis() {        
        return getPropertyIntValue(ENTITY_TABLE_ENTITY_TIMEOUT_MILLIS, EntityTable.DEFAULT_ENTITY_TIMEOUT_MILLIS);
    }
    
    
    /**
     * The interval between Dead Reckoning calculations.
     * 
     * @return the interval in milliseconds.
     */
    public int getEnityTableDeadReckoningInterval() {        
        return getPropertyIntValue(ENTITY_TABLE_DEAD_RECKONING_INTERVAL, EntityTable.DEFAULT_DEAD_RECKONING_INTERVAL_MILLIS);
    }
    
    /**
     * Returns whether the domain module should bypass given the tutor any survey questions
     * 
     * @return boolean
     */
    public boolean shouldBypassSurveyQuestions(){
        return getPropertyBooleanValue(BYPASS_SURVEY_QUESTIONS);
    }
    
    /**
     * Returns whether the domain module should allow the user to bypass chat windows shown by the tutor
     * 
     * @return boolean
     */
    public boolean shouldBypassChatWindows(){
        return getPropertyBooleanValue(BYPASS_CHAT_WINDOWS);
    }
    
    /**
     * Returns whether the domain module should restart the training application
     * at the end of a domain session
     * 
     * @return boolean If the training application should be restarted
     */
    public boolean getRestartScenario() {
        return getPropertyBooleanValue(RESTART_SCENARIO);
    }
    
    /**
     * Returns the amount of time until the scenario will auto complete after
     * being started.
     * 
     * @return int - amount of time in seconds.  
     *              Note: a value less than zero means property should not be used
     */
    public int getAutoCompleteScenario(){        
        return getPropertyIntValue(AUTO_COMPLETE_SCENARIO, DEFAULT_AUTO_COMPLETE_TIME);    
    }
    
    /**
     * Returns whether the domain module should validate all courses when the module starts.
     * 
     * @return boolean if the course found should be validated when the domain module starts (DEFAULT: false)
     */
    public boolean shouldValidateCourseContentAtStartup(){
        return getPropertyBooleanValue(VALIDATE_COURSE_AT_STARTUP, false);
    }
    
    /**
     * Returns whether the performance node status tool should be shown.
     * 
     * @return boolean 
     */
    public boolean shouldShowNodeStatusTool(){
        return getPropertyBooleanValue(SHOW_NODE_STATUS_TOOL, false);
    }
    
    
    /**
     * Return the communication application file (Java Web Start jnlp) name to 
     * serve up for courses that need a gateway module instance.
     *  
     * @return String
     */
    public String getCommunicationAppFile(){        
        return getPropertyValue(COMM_APP_FILE);
    }
    
    /**
     * Return the communication application hosted directory where Java Web Start files are 
     * served up for courses that need a gateway module instance.
     *  
     * @return String
     */
    public String getCommunicationAppDirectory(){        
        return getPropertyValue(COMM_APP_DIR);
    }
}
