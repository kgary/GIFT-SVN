/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner;

import java.io.File;

import mil.arl.gift.common.module.AbstractModuleProperties;

/**
 * Contains the Learner module property values.
 * 
 * @author mhoffman
 *
 */
public class LearnerModuleProperties extends AbstractModuleProperties {

    /** the properties file name*/
    private static final String PROPERTIES_FILE = "learner"+File.separator+"learner.properties";
    
    private static final String CONFIG_FILE = "LearnerConfigurationFile";
    private static final String DEFAULT_CONFIG_FILE = "config"+File.separator+"learner"+File.separator+"configurations" + File.separator+"Default.learnerconfig.xml";
    
    /** property key for the time between LMS queries for learner records in minutes property */
    private static final String MIN_DURATION_QUERY_PROP = "MinDurationBetweenRecordsQuery";
    
    /** property key for the stale learner cleanup duration in hours property */
    private static final String STALE_LEARNER_CLEANUP_PROP = "StaleLearnerCleanup";    
    
    /** default amount of time until the remove learner operation happens */
    private static final double DEAFULT_TIME_UNTIL_REMOVAL_HOUR = 24; // 1 day
    
    /** default amount of time between querying the LMS for new records */
    private static final double DEAFULT_DURATION_BETWEEN_RECORDS_QUERY_MIN = 10; // 10 min
    
    /** singleton instance of this class */
    private static LearnerModuleProperties instance = null;
   
    /**
     * Return the singleton instance of this class
     * 
     * @return LearnerProperties
     */
    public static synchronized LearnerModuleProperties getInstance(){
        
        if(instance == null){
            instance = new LearnerModuleProperties();
        }
        
        return instance;
    }
    
    /**
     * Class constructor
     */
    private LearnerModuleProperties(){
        super(PROPERTIES_FILE);
    }
    
    /**
     * Return the learner configuration file name to use to configure this learner module
     * 
     * @return String the GIFT relative path to the learner module configuration file.
     */
    public String getLearnerConfigurationFilename(){
        return getPropertyValue(CONFIG_FILE, DEFAULT_CONFIG_FILE);
    }
    
    /**
     * Return the minimum amount of time (in minutes) between querying for new records from the LMS module.
     * A higher number means the current in memory learner state will be used more frequently.  This
     * is useful in cases where learners are running courses in GIFT and other systems are not updating
     * the LMS.  Also less time will be spent asking for the latest records in the LMS
     * module at the beginning of a course thereby making course starting faster.  
     * This value will be ignored if there is no in-memory representation of the learner 
     * in the learner module.
     * @return time in minutes, default is {@value #DEAFULT_DURATION_BETWEEN_RECORDS_QUERY_MIN}
     */
    public double getMinDurationBetweenRecordsQuery(){
        return getPropertyDoubleValue(MIN_DURATION_QUERY_PROP, DEAFULT_DURATION_BETWEEN_RECORDS_QUERY_MIN);
    }
    
    /**
     * Return the minimum amount of time (in hours) until a learner's in memory representation is removed because
     * that learner hasn't started a course over this duration.  The learner state will be re-initialized by 
     * querying for records from the LMS upon the start of the next course.  This query can add some time
     * to the start of that course.  This property is meant to prevent having to query for the entire
     * history of this learner from the LMS if the learner is taking multiple courses w/in this time frame. 
     * @return time in hours, default is {@value #DEAFULT_TIME_UNTIL_REMOVAL_HOUR}
     */
    public double getStaleLearnerCleanupDuration(){
        return getPropertyDoubleValue(STALE_LEARNER_CLEANUP_PROP, DEAFULT_TIME_UNTIL_REMOVAL_HOUR);
    }
}
