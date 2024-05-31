/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.db.AbstractHibernateUtil;
import mil.arl.gift.common.io.ClassFinderUtil;

/**
 * This is the hibernate util class for UMS database. It will help configure the
 * connection to the UMS database.
 *
 * @author mhoffman
 *
 */
public class UMSHibernateUtil extends AbstractHibernateUtil {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(UMSHibernateUtil.class);

    /** name of the hibernate configuration file to use for this util */
    private static final String CONFIG_FILE = "ums"+File.separator+"ums.hibernate.cfg.xml";

    /** the folder that contains SQL scripts files to run (if needed) to bring the UMS db up to date with the latest development */
    private static final String SQL_SCRIPTS_FOLDER = "config" + File.separator + "ums" + File.separator + "sql" + File.separator;

    /** contains the statement to create the branch path history table which is a new table for GIFT 2017-1 */
    private static final File CREATE_BRANCHPATHHISTORY_TABLE = new File(SQL_SCRIPTS_FOLDER + "branchpathhistory.table.create.sql");
    
    /** contains the statement to update the branch path history table with the experiment ID column for GIFT 2020-1 */
    private static final File UPDATE_BRANCHPATHHISTORY_TABLE = new File(SQL_SCRIPTS_FOLDER + "branchpathhistory.table.update.sql");

    /** contains the statement to create the LTI user record table which is a new table for GIFT 2017-1 */
    private static final File CREATE_LTIUSERRECORD_TABLE = new File(SQL_SCRIPTS_FOLDER + "ltiuserrecord.table.create.sql");

    /** contains the statement to create the LTI course table which is a new table for GIFT 2017-1 */
    private static final File CREATE_COURSE_TABLE = new File(SQL_SCRIPTS_FOLDER + "course.table.create.sql");

    /** contains the statement to create the data collection lti results table which is a new table for GIFT 2017-1 */
    private static final File CREATE_DATA_COLLECTION_LTI_RESULTS_TABLE = new File(SQL_SCRIPTS_FOLDER + "datacollectionltiresults.table.create.sql");

    /** contains the statement to add the last modified column to the SurveyContext table which is a new column for GIFT 2017-1 */
    private static final File UPDATE_SURVEY_CONTEXT_TABLE_LAST_MODIFIED = new File(SQL_SCRIPTS_FOLDER + "surveyContextLastModified.table.update.sql");

    /** contains the statement to alter the description column of the experiment table from varchar(255) to long varchar */
    private static final File EXPERIMENT_DESCRIPTION = new File(SQL_SCRIPTS_FOLDER + "experimentDescription.table.update.sql");

    /** contains the statement to create the data collection permissions table which is a new table for GIFT 2019-1 */
    private static final File CREATE_DATA_COLLECTION_PERMISSIONS_TABLE = new File(SQL_SCRIPTS_FOLDER + "dataCollectionPermission.table.create.sql");

    /** contains the statement to add the ability to group courses into collections */
    private static final File COURSE_COLLECTION = new File(SQL_SCRIPTS_FOLDER + "courseCollection.table.create.sql");
    
    /** contains the statement to add a domain source id column to the domain session table */
    private static final File UPDATE_DOMAIN_SESSION_SOURCE_ID = new File(SQL_SCRIPTS_FOLDER + "domainsession.domainSourceId.table.update.sql");
    
    /** contains the statement to add a published date column to the experiment table */
    private static final File UPDATE_EXPERIMENT_PUBLISHED_DATE = new File(SQL_SCRIPTS_FOLDER + "experiment.publishedDate.table.update.sql");

    /** the singleton instance of this class */
    private static UMSHibernateUtil instance = null;

    /**
     * Return the singleton instance of this class
     *
     * @return the singleton instance
     * @throws ConfigurationException if there was a problem connecting to the UMS database
     */
    public static UMSHibernateUtil getInstance() throws ConfigurationException {

        if (instance == null) {
            instance = new UMSHibernateUtil();
        }

        return instance;
    }

    /**
     * Class constructor
     * @throws ConfigurationException if there was a problem connecting to the UMS database
     */
    private UMSHibernateUtil() throws ConfigurationException {
        super(CONFIG_FILE);
    }

    /**
     * Class constructor - allows another hibernate configuration file to be used.
     *
     * @param configFile - a hibernate configuration file to use
     * @throws ConfigurationException
     */
    protected UMSHibernateUtil(String configFile) throws ConfigurationException{
        super(configFile);

        //make sure the singleton is set (I think this should be re-factored)
        instance = this;
    }

    /**
     * Build the Configuration
     *
     * @return Configuration
     */
    @Override
    protected Configuration buildConfiguration() {

        try {
            Configuration configuration = new Configuration();

            //add all annotated class (@Entity classes)
            //-tells hibernate which classes are to be persisted (create table)
            List<Class<?>> annotatedClasses = ClassFinderUtil.getClassesWithAnnotation("mil.arl.gift.ums.db.table", javax.persistence.Entity.class);
            if(logger.isInfoEnabled()){
                logger.info("Found "+annotatedClasses.size()+" databases classes.");
            }
            for(Class<?> annotatedClass : annotatedClasses){
                logger.info("Adding database class named "+annotatedClass.getName());
                configuration.addAnnotatedClass(annotatedClass);
            }

            return configuration;

        } catch (Throwable ex) {

            // Make sure you log the exception, as it might be swallowed
            logger.error("Initial Configuration creation failed.", ex);

            throw new ExceptionInInitializerError(ex);
        }

    }

    /**
     * Run additional statements to finish initializing the UMS db.
     *
     * @throws ConfigurationException
     */
    public void postStaticInit() throws ConfigurationException{

        //create the branch path history table (new for GIFT 2017-1)
        try{
            applySqlFile("BRANCHPATHHISTORY", CREATE_BRANCHPATHHISTORY_TABLE, true);
        }catch(Exception e){
            throw new ConfigurationException("Failed to dynamically create the BranchPathHistory table.",
                    "There was an exception thrown while trying to run the table create SQL statement from '"+CREATE_BRANCHPATHHISTORY_TABLE+"'.", e);
        }
        
        //update an existing branch path history table if it does not have an experiment ID column (new for GIFT 2020-1)
        try{
            applySqlFileIfColumnNotExist("BRANCHPATHHISTORY", "EXPERIMENTID_PK", UPDATE_BRANCHPATHHISTORY_TABLE);
        }catch(Exception e){
            throw new ConfigurationException("Failed to dynamically update the BranchPathHistory table.",
                    "There was an exception thrown while trying to run the table update SQL statement from '"+UPDATE_BRANCHPATHHISTORY_TABLE+"'.", e);
        }

        //create the LTI user record table (new for GIFT 2017-1)
        try{
            applySqlFile("LTIUSERRECORD", CREATE_LTIUSERRECORD_TABLE, true);
        }catch(Exception e){
            throw new ConfigurationException("Failed to dynamically create the LtiUserRecord table.",
                    "There was an exception thrown while trying to run the table create SQL statement from '"+CREATE_LTIUSERRECORD_TABLE+"'.", e);
        }

        try {
            applySqlFile("COURSE", CREATE_COURSE_TABLE, true);

        }catch(Exception e){
            throw new ConfigurationException("Failed to dynamically create the Course table.",
                    "There was an exception thrown while trying to run the table create SQL statement from '"+CREATE_COURSE_TABLE+"'.", e);
        }

        try {
           applySqlFile("DATACOLLECTIONRESULTSLTI", CREATE_DATA_COLLECTION_LTI_RESULTS_TABLE, true);

        }catch(Exception e){
            throw new ConfigurationException("Failed to dynamically create the DataCollectionResultsLti table.",
                    "There was an exception thrown while trying to run the table create SQL statement from '"+CREATE_DATA_COLLECTION_LTI_RESULTS_TABLE+"'." , e);
        }

        try {
            applySqlFileIfColumnNotExist("SURVEYCONTEXT", "LASTMODIFIED", UPDATE_SURVEY_CONTEXT_TABLE_LAST_MODIFIED);

         }catch(Exception e){
             throw new ConfigurationException("Failed to dynamically add the last modified date column to the SurveyContext table.",
                     "There was an exception thrown while trying to run the table SQL statement from '"+UPDATE_SURVEY_CONTEXT_TABLE_LAST_MODIFIED+"'." , e);
         }

        try {
            applySqlFileIfColumnNotOfType("EXPERIMENT", "DESCRIPTION", "LONG VARCHAR", EXPERIMENT_DESCRIPTION);

        }catch(Exception e){
            throw new ConfigurationException("Failed to dynamically alter the Experiment table.",
                    "There was an exception thrown while trying to run the table alter SQL statement from '"+EXPERIMENT_DESCRIPTION+"'." , e);
        }

        try {
            applySqlFile("DATACOLLECTIONPERMISSION", CREATE_DATA_COLLECTION_PERMISSIONS_TABLE, true);

        }catch(Exception e){
            throw new ConfigurationException("Failed to dynamically create the data collection permission table.",
                    "There was an exception thrown while trying to run the table create SQL statement from '"+CREATE_DATA_COLLECTION_PERMISSIONS_TABLE+"'.", e);
        }

        try {
            applySqlFile("COURSECOLLECTION", COURSE_COLLECTION, true);
        } catch (Exception e) {
            throw new ConfigurationException("Failed to dynamically create the experiment collection table.",
                    "There was an exception thrown while trying to run the table create SQL statement from "
                            + COURSE_COLLECTION + "'.",
                    e);
        }
        
        try {
            applySqlFileIfColumnNotExist("DOMAINSESSION", "DOMAINSOURCEID", UPDATE_DOMAIN_SESSION_SOURCE_ID);

         }catch(Exception e){
             throw new ConfigurationException("Failed to dynamically add the domain source id column to the DomainSession table.",
                     "There was an exception thrown while trying to run the table SQL statement from '"+UPDATE_DOMAIN_SESSION_SOURCE_ID+"'." , e);
         }
        
        try {
            applySqlFileIfColumnNotExist("EXPERIMENT", "PUBLISHEDDATE", UPDATE_EXPERIMENT_PUBLISHED_DATE);

         }catch(Exception e){
             throw new ConfigurationException("Failed to dynamically add the published date column to the Experiment table.",
                     "There was an exception thrown while trying to run the table SQL statement from '"+UPDATE_EXPERIMENT_PUBLISHED_DATE+"'." , e);
         }

    }

    /**
     * Shutdown the database system, removing user locks that may be present for database files.
     */
    public static void shutdownDatabaseSystem(){

    	try {
            //MH: add login timeout to some arbitrary value of 5 seconds because on some computer the db.lck derby file
            //wasn't being deleted until ~90 seconds elapsed, even with the UMS module main method shutdown hook.
            //ticket #1064
            DriverManager.getConnection("jdbc:derby://localhost:1527/derbyDb/GiftUms;shutdown=true;loginTimeout=5");
    	}catch (@SuppressWarnings("unused") SQLException ex) {
    		//expect exception as per apache derby documentation, therefore ignore it
    	}
    }



}
