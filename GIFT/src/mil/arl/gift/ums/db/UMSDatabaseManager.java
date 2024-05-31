/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db;

import static mil.arl.gift.common.util.StringUtils.isBlank;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.TableGenerator;

import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Session.LockRequest;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.BranchPathHistory;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.CourseRecord;
import mil.arl.gift.common.LoginRequest;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.UserData;
import mil.arl.gift.common.UserSessionType;
import mil.arl.gift.common.db.AbstractHibernateDatabaseManager;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.GenderEnum;
import mil.arl.gift.common.enums.QuestionTypeEnum;
import mil.arl.gift.common.enums.SurveyElementTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.experiment.ExperimentUtil;
import mil.arl.gift.common.experiment.ExperimentUtil.DataCollectionUserRole;
import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.experiment.ExperimentUtil.ExperimentStatus;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.EventSourceFileFilter;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.ProhibitedUserException;
import mil.arl.gift.common.lti.TrustedLtiConsumer;
import mil.arl.gift.common.util.CompareUtil;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.ums.db.table.DbBranchPathHistory;
import mil.arl.gift.ums.db.table.DbCategory;
import mil.arl.gift.ums.db.table.DbCourse;
import mil.arl.gift.ums.db.table.DbCourseCollection;
import mil.arl.gift.ums.db.table.DbCourseCollectionEntry;
import mil.arl.gift.ums.db.table.DbDataCollection;
import mil.arl.gift.ums.db.table.DbDataCollectionPermission;
import mil.arl.gift.ums.db.table.DbDataCollectionResultsLti;
import mil.arl.gift.ums.db.table.DbDataFile;
import mil.arl.gift.ums.db.table.DbDomainSession;
import mil.arl.gift.ums.db.table.DbEventFile;
import mil.arl.gift.ums.db.table.DbExperimentSubject;
import mil.arl.gift.ums.db.table.DbExperimentSubject.DbExperimentSubjectId;
import mil.arl.gift.ums.db.table.DbFolder;
import mil.arl.gift.ums.db.table.DbGlobalUser;
import mil.arl.gift.ums.db.table.DbListOption;
import mil.arl.gift.ums.db.table.DbLtiUserRecord;
import mil.arl.gift.ums.db.table.DbOptionList;
import mil.arl.gift.ums.db.table.DbPropertyKey;
import mil.arl.gift.ums.db.table.DbPropertyValue;
import mil.arl.gift.ums.db.table.DbQuestion;
import mil.arl.gift.ums.db.table.DbQuestionCategory;
import mil.arl.gift.ums.db.table.DbQuestionProperty;
import mil.arl.gift.ums.db.table.DbQuestionPropertyValue;
import mil.arl.gift.ums.db.table.DbQuestionResponse;
import mil.arl.gift.ums.db.table.DbQuestionType;
import mil.arl.gift.ums.db.table.DbSensorFile;
import mil.arl.gift.ums.db.table.DbSurvey;
import mil.arl.gift.ums.db.table.DbSurveyContext;
import mil.arl.gift.ums.db.table.DbSurveyContextSurvey;
import mil.arl.gift.ums.db.table.DbSurveyElement;
import mil.arl.gift.ums.db.table.DbSurveyElementProperty;
import mil.arl.gift.ums.db.table.DbSurveyElementType;
import mil.arl.gift.ums.db.table.DbSurveyPage;
import mil.arl.gift.ums.db.table.DbSurveyPageProperty;
import mil.arl.gift.ums.db.table.DbSurveyPageResponse;
import mil.arl.gift.ums.db.table.DbSurveyProperty;
import mil.arl.gift.ums.db.table.DbSurveyResponse;
import mil.arl.gift.ums.db.table.DbUser;


/**
 * This class is the main interface to the UMS database back-end. It contains
 * methods to select, insert, update and delete rows in the database tables.
 *
 * @author mhoffman
 */
public class UMSDatabaseManager extends AbstractHibernateDatabaseManager {

    /**
     * The handler that accepts a session and performs some action on it.
     * 
     * @author tflowers
     *
     * @param <T> the type of data that the handler produces.
     */
    interface SafeSessionHandler<T> {
        /**
         * Execute some action on the session.
         * 
         * @param session the session to perform the action in. Can't be null.
         * @return the data that is produced by the action.
         */
        T execute(Session session);
    }

    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(UMSDatabaseManager.class);

    /** The LTI consumer key property */
    private static final String LTI_CONSUMERKEY_PROPERTY = "consumerKey";
    
    /** The LTI consumer id property */
    private static final String LTI_CONSUMERID_PROPERTY = "consumerId";
    private static final String NOT_AVAILABLE = "N/A";

    /**
     * Specific ordering of survey data tables (no survey response tables) based
     * on the logic in DerbyDatabaseUtil.importTables method and foreign key
     * constraints
     */
    private static List<Class<? extends Object>> SURVEY_DATA_TABLES;

    /**
     * Specific ordering of user data tables (including survey response tables)
     * based on the logic in DerbyDatabaseUtil.importTables method and foreign
     * key constraints
     */
    private static List<Class<? extends Object>> USER_DATA_TABLES;

    /**
     * Specific ordering of experiment data tables based on the logic in
     * DerbyDatabaseUtil.importTables method and foreign key constraints
     */
    private static List<Class<? extends Object>> EXPERIMENT_DATA_TABLES;

    /**
     * Specific ordering of survey response tables (no survey data tables) based
     * on the logic in DerbyDatabaseUtil.importTables method and foreign key
     * constraints
     */
    private static List<Class<? extends Object>> SURVEY_RESPONSE_TABLES;

    /**
     * Contains all visibleToUserNames and editableToUserNames tables needed for
     * the logic in DerbyDatabaseUtil.importTables method to prevent foreign key
     * constraint violations by deleting these tables before all others.
     */
    private static List<String> PERMISSION_TABLES;

    static{

        List<Class<? extends Object>> surveyDataTables = new ArrayList<>();

        surveyDataTables.add(DbSurveyElementType.class);
        surveyDataTables.add(DbSurveyContext.class);

        //before survey
        surveyDataTables.add(DbFolder.class);

        //before survey page
        surveyDataTables.add(DbSurvey.class);

        //after survey  -- only here for development sake because the work was done to determine the key constraint ordering
//        surveyDataTables.add(DbSurveyResponse.class);

        surveyDataTables.add(DbSurveyContextSurvey.class);
        surveyDataTables.add(DbSurveyPage.class);

        //after survey page  -- only here for development sake because the work was done to determine the key constraint ordering
//        surveyDataTables.add(DbSurveyPageResponse.class);

        //after survey page
        surveyDataTables.add(DbSurveyElement.class);

        surveyDataTables.add(DbQuestionType.class);
        surveyDataTables.add(DbQuestion.class);

        //before survey element property
        surveyDataTables.add(DbPropertyValue.class);

        surveyDataTables.add(DbPropertyKey.class);

        //after property key
        surveyDataTables.add(DbSurveyProperty.class);
        surveyDataTables.add(DbSurveyPageProperty.class);
        surveyDataTables.add(DbSurveyElementProperty.class);

        //before question property value
        surveyDataTables.add(DbOptionList.class);

        //after option list  -- only here for development sake because the work was done to determine the key constraint ordering
//        surveyDataTables.add(DbQuestionResponse.class);

        //before question property
        surveyDataTables.add(DbQuestionPropertyValue.class);

        surveyDataTables.add(DbQuestionProperty.class);

        surveyDataTables.add(DbListOption.class);

        //before question category
        surveyDataTables.add(DbCategory.class);

        surveyDataTables.add(DbQuestionCategory.class);

        SURVEY_DATA_TABLES = Collections.unmodifiableList(surveyDataTables);

        /////////////////////////////////////////////////////////////////////

        List<Class<? extends Object>> surveyResponseTables = new ArrayList<>();

        surveyResponseTables.add(DbSurveyResponse.class);
        surveyResponseTables.add(DbSurveyPageResponse.class);
        surveyResponseTables.add(DbQuestionResponse.class);

        SURVEY_RESPONSE_TABLES = Collections.unmodifiableList(surveyResponseTables);

        /////////////////////////////////////////////////////////////////////

        List<Class<? extends Object>> userDataTables = new ArrayList<>();

        userDataTables.add(DbUser.class);

        userDataTables.add(DbDataFile.class);
        userDataTables.add(DbEventFile.class);
        userDataTables.add(DbDataCollection.class);
        userDataTables.add(DbCourseCollection.class);
        userDataTables.add(DbCourseCollectionEntry.class);
        userDataTables.add(DbDataCollectionPermission.class);
        userDataTables.add(DbExperimentSubject.class);  //after experiment (in reverse order deletes), before domain session (in reverse order deletes)
        userDataTables.add(DbDomainSession.class);

        userDataTables.add(DbSensorFile.class);

        userDataTables.add(DbSurveyResponse.class);
        userDataTables.add(DbSurveyPageResponse.class);
        userDataTables.add(DbQuestionResponse.class);

        userDataTables.add(DbCourse.class);

        USER_DATA_TABLES = Collections.unmodifiableList(userDataTables);

        //////////////////////////////////////////////////////////////////////

        List<Class<? extends Object>> experimentDataTables = new ArrayList<>();

        /** NOTE: if you add another table to the list here make sure to add logic in getUnusedDomainSessionLogFiles() */
        experimentDataTables.add(DbExperimentSubject.class);
        experimentDataTables.add(DbDataCollection.class);

        EXPERIMENT_DATA_TABLES = Collections.unmodifiableList(experimentDataTables);

        /////////////////////////////////////////////////////////////////////

        List<String> permissionTables = new ArrayList<>();
        // Note: Permission table names are not case sensitive and the order does not matter.

        permissionTables.add("CategoryVisibleToUsernames");
        permissionTables.add("CategoryEditableToUsernames");

        permissionTables.add("FolderVisibleToUsernames");
        permissionTables.add("FolderEditableToUsernames");

        permissionTables.add("OptionListVisibleToUsernames");
        permissionTables.add("OptionListEditableToUsernames");

        permissionTables.add("QuestionVisibleToUsernames");
        permissionTables.add("QuestionEditableToUsernames");

        permissionTables.add("SurveyVisibleToUsernames");
        permissionTables.add("SurveyEditableToUsernames");

        permissionTables.add("SurveyContextVisibleToUsernames");
        permissionTables.add("SurveyContextEditableToUsernames");

        PERMISSION_TABLES = Collections.unmodifiableList(permissionTables);
    }

    /** singleton instance of this class */
    private static UMSDatabaseManager instance = null;
    
    /** used to convert db classes to common classes */
    private HibernateObjectReverter reverter;

    /**
     * Return the singleton instance of this class
     *
     * @return the singleton instance
     * @throws ConfigurationException if there was a problem connecting to the UMS database
     */
    public static UMSDatabaseManager getInstance() throws ConfigurationException {

        if (instance == null) {
            instance = new UMSDatabaseManager();
        }

        return instance;
    }

    /**
     * Class constructor - use default hibernate util
     * @throws ConfigurationException if there was a problem connecting to the UMS database
     */
    protected UMSDatabaseManager() throws ConfigurationException {
        super(UMSHibernateUtil.getInstance());

        try{
            initGiftUmsDb();
            reverter = new HibernateObjectReverter(this);
        }catch(Exception e){
            throw new ConfigurationException("Failed to initialize UMS database connection.", "An exception was thrown that reads:\n"+e.getMessage(), e);
        }
    }

    /**
     * Class constructor - use specified hibernate util
     *
     * @param umsHibernateUtil the hibernate util to use instead of the default
     * @throws ConfigurationException if there was a problem connecting to the UMS database
     */
    protected UMSDatabaseManager(UMSHibernateUtil umsHibernateUtil) throws ConfigurationException{
        super(umsHibernateUtil);

        try{
            initGiftUmsDb();
        }catch(Exception e){
            throw new ConfigurationException("Failed to initialize UMS database connection.", "An exception was thrown that reads:\n"+e.getMessage(), e);
        }
    }

    /**
     * Verifies that the UMS database has rows in the question type and property
     * key tables that are associated with the common enumerations, creating
     * them if they do not exist
     * @throws Exception if there was a problem initializing the GIFT UMS db
     */
    private void initGiftUmsDb() throws Exception {

        for (QuestionTypeEnum questionType : QuestionTypeEnum.VALUES()) {

            DbQuestionType dbQuestionType = selectRowByExample(new DbQuestionType(questionType.getName()), DbQuestionType.class);

            if (dbQuestionType == null) {

                dbQuestionType = new DbQuestionType(questionType.getName(), questionType.getDisplayName());

                insertRow(dbQuestionType);
            }
        }

        for (SurveyElementTypeEnum surveyElementType : SurveyElementTypeEnum.VALUES()) {

            DbSurveyElementType dbSurveyElementType = selectRowByExample(new DbSurveyElementType(surveyElementType.getName()), DbSurveyElementType.class);

            if (dbSurveyElementType == null) {

                dbSurveyElementType = new DbSurveyElementType(surveyElementType.getName(), surveyElementType.getDisplayName());

                insertRow(dbSurveyElementType);
            }
        }

        for (SurveyPropertyKeyEnum propertyKey : SurveyPropertyKeyEnum.VALUES()) {

            DbPropertyKey dbPropertyKey = selectRowByExample(new DbPropertyKey(propertyKey.getName()), DbPropertyKey.class);

            if (dbPropertyKey == null) {

                dbPropertyKey = new DbPropertyKey(propertyKey.getName());

                insertRow(dbPropertyKey);
            }
        }

        UMSHibernateUtil.getInstance().postStaticInit();

    }

    @Override
    public void cleanup() throws Exception{
        super.cleanup();
    }

    /**
     * Return the UMS db representation of the user uniquely identified by the specified
     * GIFT username.
     *
     * @param username a unique GIFT username to retrieve user information for.
     * @param createNonExistingUser whether or not to create the user entry in the UMS db based
     * solely on the GIFT username provided.  Note: the username will be used as the LMS username.
     * @return that user's information.  Will not be null.
     * @throws UserNotFoundException the user was not found and not allowed to create the non-existing user
     * @throws ProhibitedUserException if the username is on the prohibited list, meaning the username can't be used in this gift instance (e.g. Public).
     * @throws UMSDatabaseException failed to create the non-existent user
     */
    public DbUser getUserByUsername(String username, boolean createNonExistingUser) throws UserNotFoundException, UMSDatabaseException, ProhibitedUserException {

        DbUser user = null;
        String queryString = "from DbUser as giftuser where giftuser.username = '"+username+"'";

        try {
            List<DbUser> users = selectRowsByQuery(DbUser.class, queryString, -1, 1);
            if(!users.isEmpty()){
                user = users.get(0);
            }
        } catch (Exception e) {
            logger.error("Caught exception while trying to select User DB row by username of " + username, e);
        }

        if(user == null){

            if(createNonExistingUser){
                //create new user for this username

                if(CommonProperties.getProhibitedNames().contains(username.toLowerCase())){
                    //username is prohibited
                    throw new ProhibitedUserException("Unable to create a new user in the database for username of '"+username+"' because that username is prohibited.");
                }

                //TODO: need way to pass in the other information about the user, possible send a request back to the caller OR
                //      provide a user interface on the LMS history webpage to change it.
                DbUser newUser = new DbUser(GenderEnum.MALE.getName());
                newUser.setLMSUserName(username);
                newUser.setUsername(username);

                try{
                    insertRow(newUser);

                    //send user data back

                    UserData userData = new UserData(newUser.getUserId(), newUser.getLMSUserName(), GenderEnum.valueOf(newUser.getGender()));
                    userData.setUsername(newUser.getUsername());

                    if(logger.isInfoEnabled()){
                        logger.info("created user = " + userData);
                    }

                    return newUser;

                }catch(Exception e){
                    throw new UMSDatabaseException("Failed to create new user in the database for username of '"+username+"'.", e);
                }
            }else{
                throw new UserNotFoundException("Can't find user by username of '"+username+"'.");
            }
        }else{
            return user;
        }

    }

    /**
     * Retrieve the branch path history database entry based on the values provided in the parameter.
     *
     * @param branchPathHistory contains ids to use to search for a row in the database.  Can't be null.
     * @return the database entry, null if not found.
     */
    public DbBranchPathHistory getBranchPathHistory(BranchPathHistory branchPathHistory){

        if(branchPathHistory == null){
            throw new IllegalArgumentException("The branch path history can't be null.");
        }
        
        try {

            final String queryString = "from DbBranchPathHistory as BranchPathHistory where " +
                    "BranchPathHistory.courseId = '" + branchPathHistory.getCourseId()+"' and " +
                    "BranchPathHistory.branchId = " + branchPathHistory.getBranchId() + " and " +
                    "BranchPathHistory.pathId = " +branchPathHistory.getPathId() + " and " +
                    "BranchPathHistory.experimentId = '" + (branchPathHistory.getExperimentId() != null ? branchPathHistory.getExperimentId() : NOT_AVAILABLE) + "'";

            List<DbBranchPathHistory> list = selectRowsByQuery(DbBranchPathHistory.class, queryString, -1, 1);
            if(list.isEmpty()){
                return null;
            }else{
                return list.get(0);
            }


        } catch (Exception e) {
            throw new DetailedException("Failed to retrieve the branch path history from the database.",
                    "There was a problem while retrieving the branch path history for "+branchPathHistory+".  The exception reads:\nd"+e.getMessage(), e);
        }

    }

    /**
     * Updates an existing branch path history entry in the database -or- creates the row if it doesn't exist.
     *
     * @param branchPathHistory contains branch path information to place in the database.  Can't be null.
     * @throws DetailedException if there was a problem updating (or creating) the database entry
     */
    public void updateBranchPathHistoryProperties(BranchPathHistory branchPathHistory) throws DetailedException{

        if(branchPathHistory == null){
            throw new IllegalArgumentException("The branch path history can't be null.");
        }
        
        // ignore this message if it was sent because a path is ending
        if (branchPathHistory.isPathEnding()) {
            return;
        }

        //retrieve existing entry
        DbBranchPathHistory dbOldBranchPathHistory = getBranchPathHistory(branchPathHistory);
        if(dbOldBranchPathHistory == null){
            //creating entry in db

            DbBranchPathHistory dbNewBranchPathHistory = new DbBranchPathHistory();

            dbNewBranchPathHistory.setCourseId(branchPathHistory.getCourseId());
            dbNewBranchPathHistory.setExperimentId(branchPathHistory.getExperimentId() != null ? branchPathHistory.getExperimentId() : NOT_AVAILABLE);
            dbNewBranchPathHistory.setBranchId(branchPathHistory.getBranchId());
            dbNewBranchPathHistory.setPathId(branchPathHistory.getPathId());

            if(branchPathHistory.shouldIncrement()){
                dbNewBranchPathHistory.setActualCnt(branchPathHistory.getActualCnt() + 1);
                dbNewBranchPathHistory.setCnt(branchPathHistory.getCnt() + 1);
            }else{
                dbNewBranchPathHistory.setActualCnt(branchPathHistory.getActualCnt());
                dbNewBranchPathHistory.setCnt(branchPathHistory.getCnt());
            }

            try{
                insertRow(dbNewBranchPathHistory);
                if(logger.isInfoEnabled()){
                    logger.info("Successfully inserted branch path history entry of "+dbNewBranchPathHistory);
                }
            }catch(Exception e){
                throw new DetailedException("Failed to add branch path history.",
                        "The branch path history could not be inserted into the database.\n"+dbNewBranchPathHistory, e);
            }

        }else{

            if(logger.isDebugEnabled()){
                logger.debug("Attempting to update existing branch path history entry of:\n"+dbOldBranchPathHistory+", to\n"+branchPathHistory);
            }

            //apply updates
            if(branchPathHistory.shouldIncrement()){
                dbOldBranchPathHistory.setActualCnt(dbOldBranchPathHistory.getActualCnt()+1);
                dbOldBranchPathHistory.setCnt(dbOldBranchPathHistory.getCnt()+1);
            }else{
                dbOldBranchPathHistory.setActualCnt(branchPathHistory.getActualCnt());
                dbOldBranchPathHistory.setCnt(branchPathHistory.getCnt());
            }

            if(updateRow(dbOldBranchPathHistory)){
                if(logger.isInfoEnabled()){
                    logger.info("Successfully updated branch path history entry : "+dbOldBranchPathHistory);
                }
            }else{
                logger.error("Failed to update branch path history entry to : "+dbOldBranchPathHistory);
                throw new DetailedException("Failed to update the branch path history entry in the UMS database.", "There was a problem updating the branch path history - "+dbOldBranchPathHistory, null);
            }
        }
    }

    /**
     * Update the timestamp to an existing LTI user record with the current
     * time.
     *
     * @param consumerKey The consumer key is typically a UUID that uniquely
     *        identifies a Tool Consumer. This key must be unique for each
     *        consumer. Can't be null or empty.
     * @param consumerId The consumerId is an id that is unique for a user
     *        within a specific Tool Consumer. This id could collide with other
     *        ids in other Tool Consumers, so it is only unique within a
     *        specific Tool Consumer. Can't be null or empty.
     * @param timestamp The date/timestamp that the Tool Consumer launched the
     *        request.
     */
    public void updateLtiUserRecord(String consumerKey, String consumerId, Date timestamp){

        DbLtiUserRecord ltirecord = getLtiUserRecord(consumerKey, consumerId, false);
        if(ltirecord == null){
            throw new DetailedException("Failed to update the LTI user record entry in the UMS database.",
                    "Unable to find the LTI user record with consumer '"+consumerKey+"' and consumer id '"+consumerId+"'", null);
        }

        ltirecord.setLaunchRequestTimestamp(timestamp);

        if(updateRow(ltirecord)){
            if(logger.isInfoEnabled()){
                logger.info("Successfully updated LTI user record entry : "+ltirecord + ", timestamp = " + ltirecord.getLaunchRequestTimestamp().getTime());
            }
        }else{
            logger.error("Failed to update LTI user record entry to : "+ltirecord);
            throw new DetailedException("Failed to update the LTI user record entry in the UMS database.",
                    "There was a problem updating the LTI user record - "+ltirecord, null);
        }
    }

    /**
     * Retrieve the LTI user record GIFT database entry based on the values provided in the parameter.
     *
     * @param consumerKey The consumer key is typically a UUID that uniquely identifies a Tool Consumer.
     * This key must be unique for each consumer. Can't be null or empty.
     * @param consumerId The consumerId is an id that is unique for a user within a specific Tool Consumer.
     * This id could collide with other ids in other Tool Consumers, so it is only unique within a specific Tool Consumer.
     * Can't be null or empty.
     * @param bypassDbCache True to bypass the hibernate cache. This is useful in cases where the latest value (specifically timestamp
     * value of the lti record should be explicitly retrieved from the database).  False to let hibernate choose (which may pull from
     * the hibernate cache).
     * @return the database entry, null if not found.
     */
    public DbLtiUserRecord getLtiUserRecord(String consumerKey, String consumerId, boolean bypassDbCache){

        if(consumerKey == null || consumerKey.isEmpty()){
            throw new IllegalArgumentException("The consumer key can't be null or empty.");
        }else if(consumerId == null || consumerId.isEmpty()){
            throw new IllegalArgumentException("The consumer id can't be null or empty.");
        }

        try {

            DbLtiUserRecord dbRecord = null;

            if (bypassDbCache) {
                dbRecord = this.selectRowByTwoTupleCompositeIdBypassCache(LTI_CONSUMERKEY_PROPERTY,
                        consumerKey, LTI_CONSUMERID_PROPERTY, consumerId, DbLtiUserRecord.class);
            } else {
                dbRecord = this.selectRowByTwoTupleCompositeId(LTI_CONSUMERKEY_PROPERTY,
                        consumerKey, LTI_CONSUMERID_PROPERTY, consumerId, DbLtiUserRecord.class);
            }

            if(dbRecord == null){
                return null;
            }else{
                return dbRecord;
            }


        } catch (Exception e) {
            throw new DetailedException("Failed to retrieve the LTI user record from the database.",
                    "There was a problem while retrieving the LTI user record for consumer key '"+consumerKey+"' and consumer id '"+consumerId+"'.  The exception reads:\n"+e.getMessage(), e);
        }
    }

    /**
     * Retrieves the lti user record based on a global user id.
     *
     * @param globalId The global user id of the user to lookup.
     * @return The lti user record based on the global id (if found).  Null is returned if not found.
     */
    public DbLtiUserRecord getLtiUserRecordFromGlobalId(Integer globalId) {

        if (globalId == null) {
            throw new IllegalArgumentException("The global id parameter cannot be null.");
        }

        try {
            String queryString = "from DbLtiUserRecord as ltiuserrecord where ltiuserrecord.globalUser.globalId = "+globalId+" and ltiuserrecord.globalUser.userType = '"+UserSessionType.LTI_USER.toString()+"'";
            List<DbLtiUserRecord> dbRecords = selectRowsByQuery(DbLtiUserRecord.class, queryString, -1, 1);

            DbLtiUserRecord dbRecord = null;
            if(!dbRecords.isEmpty()){
                dbRecord = dbRecords.get(0);
            }
            return dbRecord;
        } catch (Exception e) {
            throw new DetailedException("Failed to retrieve the LTI user record from the database.",
                    "There was a problem while retrieving the LTI user record for global id '"+globalId+"'.  The exception reads:\n"+e.getMessage(), e);
        }
    }

    /**
     * Create a new LTI user record in the GIFT database.  This will also create a new GIFT user with a new GIFT user id.
     *
     * @param consumer The trusted lti consumer object containing the consumer key and the internal consumer name.  The internal
     * consumer name is used to generate a unique string name that is created in the giftuser table to identify the lti user.
     * @param consumerId The consumerId is an id that is unique for a user within a specific Tool Consumer.
     * This id could collide with other ids in other Tool Consumers, so it is only unique within a specific Tool Consumer.
     * Can't be null or empty.
     * @param timestamp The date/timestamp that the Tool Consumer launched the request.
     * @return the record that was created.  This also contains a reference to the gift user that was created
     * @throws UMSDatabaseException if a LTI record already exists for the consumer information provided or there was a problem
     * creating either the new GIFT user entry or the lti user record entry.
     */
    public DbLtiUserRecord createLtiUserRecord(TrustedLtiConsumer consumer, String consumerId, Date timestamp) throws UMSDatabaseException{

        if (consumer == null) {
            throw new UMSDatabaseException("Consumer parameter cannot be null.");
        }

        //make sure the lti user record doesn't already exist
        if(getLtiUserRecord(consumer.getConsumerKey(), consumerId, false) != null){
            throw new UMSDatabaseException("Can't create a new LTI User record because there is already an entry identified by the consumer key '"+consumer.getConsumerKey()+"' and consumer id '"+consumerId+"'.");
        }

        DbGlobalUser globalUser = new DbGlobalUser();
        try {

            globalUser.setUserType(UserSessionType.LTI_USER);
            insertRow(globalUser);
        }  catch(Exception e) {
            throw new UMSDatabaseException("Failed to create the LTI user record for the consumer key '"+consumer.getConsumerKey()+"' and consumer id '"+consumerId+
                    "' because there was an exception while creating the new Global GIFT User.", e);
        }

        //second create the lti user record
        DbLtiUserRecord ltirecord = new DbLtiUserRecord();
        ltirecord.setGlobalUser(globalUser);
        ltirecord.setConsumerId(consumerId);
        ltirecord.setConsumerKey(consumer.getConsumerKey());
        ltirecord.setLaunchRequestTimestamp(timestamp);


        try{
            insertRow(ltirecord);

            if(logger.isInfoEnabled()){
                logger.info("created new LTI user record of " + ltirecord);
            }

            return ltirecord;

        }catch(Exception e){
            throw new UMSDatabaseException("Failed to create the LTI user record for the consumer key '"+consumer.getConsumerKey()+"' and consumer id '"+consumerId+
                    "' because there was an exception while creating that record.", e);
        }
    }


    /**
     * Sets the 'deleted' flag on the course record and sets the record as 'deleted'.  Note
     * that the course record is not physically removed from the db table so that the UUID
     * that was used for it will remain in the table to guarantee that another UUID cannot
     * be created to recycle that id.
     *
     * @param courseRecord The course record to be deleted.
     * @return True if the course record was successfully deleted, false otherwise.
     */
    public boolean deleteCourseRecord(CourseRecord courseRecord) {

        boolean success = false;
        if (courseRecord != null) {

            DbCourse course = new DbCourse(courseRecord);

            // Set the flag on the course record to be deleted.
            course.setDeleted(true);

            try {
                // update the course record object to with the 'deleted' flag set to true.
                this.updateRow(course);
                success = true;
            } catch (Exception e) {
                logger.error("Exception caught deleting course record: ", e);
            }
        }

        return success;
    }

    /**
     * Gets the course record data based on the source path of the course.
     *
     * @param coursePath The source path of the course. e.g. Public/Hemorrhage Control Lesson/HemorrhageControl.course.xml
     * @return The course record object (if found).  Null is returned if the course is not found or if there is an error.  A course
     * can not be found if the course was manually copied to GIFT workspace folder and has never been saved.
     * @throws DetailedException if there was a problem getting the course by the provided path.
     */
    public CourseRecord getCourseByPath(String coursePath) throws DetailedException{

        Session session = getCurrentSession();
        try {

            session.beginTransaction();
            Query query = session.createQuery("from DbCourse as course where course.coursePath = :coursePathValue");
            query.setParameter("coursePathValue", coursePath);

            List<DbCourse> dbRecords = selectRowsByQuery(DbCourse.class, query, -1, -1, session);
            if(dbRecords.isEmpty()){
                return null;
            }else{

                //select the first non-deleted entry (i.e. the row with isdelete = false)
                //Note: ideally this would be added to the SQL search query above but there is an issue with
                //      hibernate query language translating java.lang.boolean values.  Since there shouldn't be
                //      many rows with the same course path in the table this shouldn't loop much.
                DbCourse dbRecord = null;
                for(DbCourse courseRecord : dbRecords){

                    if(!courseRecord.isDeleted()){
                        dbRecord = courseRecord;
                        break;
                    }
                }

                if(dbRecord == null){
                    return null;
                }

                CourseRecord courseRecord = new CourseRecord(dbRecord.getCourseId(), dbRecord.getCoursePath(),
                        dbRecord.getOwnerName(), dbRecord.isDeleted());
                return courseRecord;
            }


        } catch (Exception e) {
            throw new DetailedException("Failed to retrieve the course information from the database.",
                    "There was a problem while retrieving the course record based on the path '"+coursePath+"'.  The exception reads:\n"+e.getMessage(), e);
        } finally{

            if(session != null && session.isOpen()){
                session.close();
            }
        }
    }

    /**
     * Gets the course record data based on the course id (uuid).
     *
     * @param courseId The UUID of the course.
     * @param session optional session to perform the query in.  If null a new session is created and closed.
     * @return The course record object (if found).  Null is returned if the course is not found or if there is an error.
     * @throws DetailedException if there was a problem getting the course by the provided id.
     */
    public CourseRecord getCourseById(String courseId, Session session) throws DetailedException{

        try {
            DbCourse dbRecord = this.selectRowById(courseId, DbCourse.class, session);
            if(dbRecord == null){
                return null;
            }else{
                CourseRecord courseRecord = new CourseRecord(dbRecord.getCourseId(), dbRecord.getCoursePath(),
                        dbRecord.getOwnerName(), dbRecord.isDeleted());
                return courseRecord;
            }


        } catch (Exception e) {
            throw new DetailedException("Failed to retrieve the course information from the database.",
                    "There was a problem while retrieving the course record based on the id '"+courseId+"'.  The exception reads:\n"+e.getMessage(), e);
        }
    }

    /**
     * Creates a course record in the database.  This inserts a new record in the 'course' database table.
     *
     * @param courseRecord The course record object data to insert into the database.
     * @return True if successful, false if there was an error or not successful.
     * @throws UMSDatabaseException if there was a problem creating a new course record.
     */
    public CourseRecord createCourseRecord(CourseRecord courseRecord) throws UMSDatabaseException{

        if (courseRecord == null) {
            throw new IllegalArgumentException("The 'courseRecord' parameter cannot be null.");
        }

        String relativeFilePath = courseRecord.getCoursePath();
        if (relativeFilePath == null || relativeFilePath.isEmpty()) {
            throw new IllegalArgumentException("The 'relativeFilePath' parameter cannot be null or empty.");
        }

        if (relativeFilePath.startsWith(Constants.FORWARD_SLASH) ||
                relativeFilePath.startsWith(Constants.BACKWARD_SLASH)) {
            // The course runtime for some reason does not use a forward or backward slash, and fails, so when the file is inserted
            // into the database, make sure the path is in a format that is expected by the runtime.
            throw new IllegalArgumentException ("The 'relativeFilePath' parameter cannot begin with a forward or backward slash.");
        }

        DbCourse course = new DbCourse(courseRecord);

        CourseRecord insertedRecord = null;
        try{
            insertRow(course);

            if(logger.isInfoEnabled()){
                logger.info("created new course record: " + course);
            }

            insertedRecord = new CourseRecord(course.getCourseId(), course.getCoursePath(), course.getOwnerName(), false);

        }catch(Exception e){
            throw new UMSDatabaseException("Failed to create the course for the course record '"+courseRecord+"', " +
                    " because there was an exception while creating that record.", e);
        }
        return insertedRecord;
    }

    /**
     * Updates a course record in the database. This updates an existing record
     * in the 'course' database table.
     *
     * @param username the user requesting the update. Used to check for
     *        permission to modify the course. Can't be null or empty.
     * @param courseRecord The course record object data to update in the
     *        database. Can't be null.
     * @param session the session to perform the update in. If null the current
     *        session will be used to create a transaction and close the
     *        session.
     * @return whether the course record was updated in the database
     * @throws UMSDatabaseException if there was a problem updating the course
     *         record.
     */
    public boolean updateCourseRecord(String username, CourseRecord courseRecord, Session session)
            throws UMSDatabaseException {

        if (courseRecord == null) {
            throw new IllegalArgumentException("The 'courseRecord' parameter cannot be null.");
        }

        String relativeFilePath = courseRecord.getCoursePath();
        if (relativeFilePath == null || relativeFilePath.isEmpty()) {
            throw new IllegalArgumentException("The 'relativeFilePath' parameter cannot be null or empty.");
        }

        if (relativeFilePath.startsWith(Constants.FORWARD_SLASH)
                || relativeFilePath.startsWith(Constants.BACKWARD_SLASH)) {
            /* The course runtime for some reason does not use a forward or
             * backward slash, and fails, so when the file is inserted into the
             * database, make sure the path is in a format that is expected by
             * the runtime. */
            throw new IllegalArgumentException(
                    "The 'relativeFilePath' parameter cannot begin with a forward or backward slash.");
        }

        try {
            return executeWithinSession(session, new SafeSessionHandler<Boolean>() {
                @Override
                public Boolean execute(Session session) {
                    
                    // Note: should select the db row instead of building it from courseRecord in case
                    // the hibernate object is already loaded in this session
                    DbCourse dbRecord = selectRowById(courseRecord.getCourseId(), DbCourse.class, session);
                    String oldCoursePath = dbRecord.getCoursePath();
                    
                    dbRecord.setCoursePath(courseRecord.getCoursePath());
                    dbRecord.setDeleted(courseRecord.isDeleted());

                    if (logger.isInfoEnabled()) {
                        logger.info("Updating with the course record: " + courseRecord);
                    }
                    
                    if(!updateRow(dbRecord, session)){
                        // error
                        return false;
                    }
                    
                    /*
                     * Update domain sessions with the new course folder if any exist
                     */
                    updateDomainSessionSourceId(oldCoursePath, dbRecord.getCoursePath(), session);

                    /* Update experiments with the new course folder if any
                     * exist */
                    List<DbDataCollection> experiments = getExperimentsByCourseId(dbRecord.getCourseId(), session);
                    if (experiments != null && !experiments.isEmpty()) {
                        FileTreeModel model = FileTreeModel.createFromRawPath(dbRecord.getCoursePath());
                        final String courseFolder = model.getParentTreeModel().getRelativePathFromRoot();

                        for (DbDataCollection experiment : experiments) {
                            if (logger.isInfoEnabled()) {
                                logger.info("Updating experiment '" + experiment.getId() + "' with course folder: "
                                        + courseFolder);
                            }
                            experiment.setCourseFolder(courseFolder);
                            
                            if(experiment.isDataSetType(DataSetType.COURSE_DATA)){
                                // the name for 'course data' published course type matches the course name
                                experiment.setName(model.getParentTreeModel().getFileOrDirectoryName());  
                            }
                            updateRow(experiment, session);
                        }
                    }

                    return true;
                }
            });
        } catch (Exception e) {
            throw new UMSDatabaseException("Failed to update the course for the course record '" + courseRecord + "', "
                    + " because there was an exception while updating that record.", e);
        }
    }
    
    /**
     * Create a new 'course tile' (course data) published course db entry if one doesn't already exists for the specified course.
     * A published course should exist for every course created in order to expose the publish course report generation logic.
     * @param username the owner of the new published course.  This should be the course owner however in Desktop mode
     * that is a wildcard (Public) owner and therefore it will be the user making the first request.  Can't be null or empty.
     * @param courseRecord contains information about the course that is needed to populate the publish course entry
     * w/o asking the user for more information. Can't be null or empty.
     */
    public void createDefaultDataCollectionItemIfNeeded(String userName, CourseRecord courseRecord){
        
        String courseFilePath = courseRecord.getCoursePath();
        String courseFolderPath = courseFilePath.substring(0, courseFilePath.lastIndexOf("/"));
        String courseFolderName = courseFolderPath.substring(courseFolderPath.indexOf("/")+1, courseFolderPath.length());
        
        List<DbDataCollection> dataCollections = getPublishedCoursesOfType(courseFolderPath, DataSetType.COURSE_DATA, true, null);
        
        if(dataCollections.isEmpty()){
            
            String publishCourseOwner = courseRecord.isPublicOwner() ? userName : courseRecord.getOwnerName();
        
            //
            // create new experiment table entry
            //
            DbDataCollection dbExperiment;
            try{
                dbExperiment = UMSDatabaseManager.getInstance().createExperiment(courseFolderName, 
                        "Use this to generated reports on shared courses that are taken from the 'Take a Course' page. This published course was created automatically for you.",
                        publishCourseOwner, DataSetType.COURSE_DATA, null);
            }catch(Exception e){
                throw new DetailedException("Failed to create the data set entry in the database.", 
                        "An error occurred while trying to create the data set entry for '"+courseRecord+"' : "+e.getMessage(), e);
            }
            
            dbExperiment.setCourseFolder(courseFolderPath);
            dbExperiment.setSourceCourseId(courseRecord.getCourseId());         
    
            //apply updates to db
            UMSDatabaseManager.getInstance().updateExperimentProperties(publishCourseOwner, dbExperiment, null);
        }
    }

    /**
     * Create a new experiment entry in the experiment table of the database using the provided attributes.
     *
     * @param name the name of the experiment.  Doesn't have to be unique but can't be null.
     * @param description any useful information about the experiment.  Can be null.
     * @param author the user that authored the experiment
     * @param dataSetType the data collection data set type to be created. 
     * @param session the session to perform the action in. If null the current
     *        session will be used to create a transaction and close the
     *        session.
     * @return the representation of the experiment in the database, including the generated experiment id
     * @throws UMSDatabaseException if there was a problem creating the experiment entry in the database
     */
    public DbDataCollection createExperiment(String name, String description, String author, DataSetType dataSetType, Session session)
            throws UMSDatabaseException {

        final boolean isLocalSession = session == null;
        if (isLocalSession) {
            session = getCurrentSession();
            session.beginTransaction();
        }

        DbDataCollection newExperiment = new DbDataCollection();
        newExperiment.setAuthorUsername(author);
        newExperiment.setName(name);
        newExperiment.setDescription(description);
        newExperiment.setDataSetType(dataSetType);
        newExperiment.setStatus(ExperimentStatus.RUNNING);
        newExperiment.setPublishedDate(new Date());

        try{
            insertRow(newExperiment, session);

            if(logger.isInfoEnabled()){
                logger.info("created new data set of " + newExperiment);
            }


            //set the owner permission object as well
            DbDataCollectionPermission permission = new DbDataCollectionPermission();
            permission.setDataCollectionId(newExperiment.getId());
            permission.setDataCollectionUserRole(DataCollectionUserRole.OWNER);
            permission.setUsername(author);

            newExperiment.setPermissions(new HashSet<>(Arrays.asList(permission)));

            updateRow(newExperiment, session);

            if (isLocalSession && session != null) {
                session.getTransaction().commit();
            }

            return newExperiment;

        } catch (Exception e) {
            if (isLocalSession && session != null && session.isOpen()) {
                session.getTransaction().rollback();
            }

            throw new UMSDatabaseException("Failed to create "+author+"'s new data set named '"+name+"' in the UMS database.", e);
        } finally {
            if (isLocalSession && session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Deletes an experiment from the experiments table in the database.
     *
     * @param experimentId the experiment to delete
     * @param session the session to perform the query in. If null the current
     *        session will be used to create a transaction and close the
     *        session.
     * @throws DetailedException if there was a problem deleting the experiment
     *         entry
     */
    public void deleteExperiment(String experimentId, Session session) throws DetailedException {

        boolean shouldCloseSession = session == null;
        /* TODO: use hibernate's OneToMany annotation to automatically delete
         * all subjects when deleting the experiment we want to eager load
         * subjects and lazy load LTI results because we are only using the
         * subject collection in this method. */
        DbDataCollection experiment = getExperiment(experimentId, true, false, session);

        Transaction transaction = null;
        try{
            //lock experiment in the db
            LockOptions lockOptions = new LockOptions();
            lockOptions.setLockMode(LockMode.PESSIMISTIC_WRITE);  //Note: LockMode.WRITE causes exception

            if (session == null) {
                session = createNewSession();
                transaction = session.beginTransaction();
            } else {
                transaction = session.getTransaction();
            }

            LockRequest lockRequest = session.buildLockRequest(lockOptions);
            lockRequest.lock(experiment);

            //lock and delete domain sessions
            //Note:
            // i. the "experimentid_fk" comes from DbDomainSession annotation for DbExperimentSubject subject variable
            // ii. the experiment id parameter needs to be wrapped with double quotes so the UUID string isn't split by the "-" characters in it
            // iii. selectRowsByExample didn't work after trying several different ways to build the DbDomainSession example object (it would return all domain sessions)
            List<DbDomainSession> experimentDomainSessions = selectRowsByText(DbDomainSession.class, session, "experimentid_fk", "\""+experiment.getId()+"\"");
            for(DbDomainSession subjectDomainSession : experimentDomainSessions){
                lockRequest.lock(subjectDomainSession);
                deleteRow(subjectDomainSession, session);
            }

            /* Delete any course collection entries for this experiment */
            executeDeleteSQLQuery(
                    "DELETE FROM " + getSchemaFromConfig() + ".CourseCollectionEntry WHERE courseId_FK = '" + experimentId + "'",
                    session);

            //lock and delete subjects
            for(DbExperimentSubject subject : experiment.getSubjects()){
                lockRequest.lock(subject);
                deleteRow(subject, session);
            }

            //delete experiment (that now has no subjects)
            deleteRow(experiment, session);

            if (shouldCloseSession) {
                transaction.commit();
            }

        }catch(Exception e){

            logger.error("Failed to delete the data set "+experimentId, e);

            if(transaction != null){
                transaction.rollback();
            }

            throw new DetailedException("Failed to delete the Data Set named '"+experiment.getName()+"'.",
                    "There was an exception while locking, retrieving and deleting the data set "+experiment+" : "+e.getMessage(), e);
        } finally {
            if (session != null && shouldCloseSession) {
                session.close();
            }
        }

    }

    /**
     * Retrieve the experiments the provided user has access too.
     *
     * @param author a GIFT username used to lookup experiments accessible by that user
     * @param eagerFetchSubjects true to eager fetch the experiment's lazy loaded subjects. The
     *        "Eager" term applies to a hibernate term for "Eager" fetching which is a more
     *        expensive database call. For cases that only need the {@link DbDataCollection}
     *        (without the subjects), set eagerFetchSubjects to false; however if the full object
     *        needs to be filled out, set eagerFetchSubjects to true.
     * @param eagerFetchLtiResults true to eager fetch the experiment's lazy loaded LTI results. The
     *        "Eager" term applies to a hibernate term for "Eager" fetching which is a more
     *        expensive database call. For cases that only need the {@link DbDataCollection}
     *        (without the ltiResults), set eagerFetchLtiResults to false; however if the full
     *        object needs to be filled out, set eagerFetchLtiResults to true.
     * @return collection of Experiments accessible by the username. Can be empty but not null.
     */
    public Set<DbDataCollection> getExperiments(String author, boolean eagerFetchSubjects, boolean eagerFetchLtiResults) {

        // query for all data collection objects that this author is the owner of or has been given permissions to
        // NOTE: ideally this query would use the 'group by e.id' to remove duplicates but I could never get the syntax correct for HQL
        //       therefore a HashSet was used to removed duplicates in the code below.
        String queryString = "select e from DbDataCollection e LEFT JOIN e.permissions dcp WHERE (e.id = dcp.dataCollectionId AND dcp.username='" + author + "') OR e.authorUsername='" + author + "'";

        Set<DbDataCollection> experiments = null;
        if (eagerFetchSubjects || eagerFetchLtiResults) {

            Session session = null;
            try {
                session = getCurrentSession();
                session.beginTransaction();

                List<DbDataCollection> experimentsList = selectRowsByQuery(DbDataCollection.class, queryString, -1, -1, session);
                if (experimentsList != null) {
                    //removes duplicates - see note near queryString instantiation
                    experiments = new HashSet<>(experimentsList);
                    for (DbDataCollection collection : experiments) {
                        if (eagerFetchSubjects) {
                            Hibernate.initialize(collection.getSubjects());
                        }

                        if (eagerFetchLtiResults) {
                            Hibernate.initialize(collection.getDataCollectionResultsLti());
                        }
                    }
                }
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        } else {
            List<DbDataCollection> experimentsList = selectRowsByQuery(DbDataCollection.class, queryString, -1, -1);
            if(experimentsList != null){
                //removes duplicates - see note near queryString instantiation
                experiments = new HashSet<>(experimentsList);
            }
        }

        return experiments;
    }

    /**
     * Return the published courses (Db Data Collection objects) that reference the specific source course folder 
     * and of the specific data set type.
     * @param courseFolder the workspace path to the course folder that is taken by learners.  Can't be null or empty. e.g. "Public/Hello World"
     * @param dataSetType the enumerated type of publish course to search for.  Can't be null.
     * @param excludeEnded whether to remove data collections that have the status of ExperimentStates.ENDED from the returned collection.
     * @param session the session to perform the database operations in.  If null a new session will be created and closed.
     * @return the published courses found.  Can be empty but won't be null.
     */
    public List<DbDataCollection> getPublishedCoursesOfType(final String courseFolder, final DataSetType dataSetType, boolean excludeEnded, Session session){
        
        try {
            return executeWithinSession(session, new SafeSessionHandler<List<DbDataCollection>>() {
                @Override
                public List<DbDataCollection> execute(Session session) {
                    final String queryString = "from DbDataCollection as dataCollection where dataCollection.courseFolder = '"
                            + courseFolder + "' AND dataCollection.dataSetType = '"+dataSetType.name()+"'";

                    List<DbDataCollection> collections = selectRowsByQuery(DbDataCollection.class, queryString, -1, -1, session);
                    
                    if(excludeEnded){
                        // are there any non-ended publish course entries? - this is to handle any possible case where the course was deleted
                        // and then another course with the same name was created in the same workspace folder.  Want to ignore previous
                        // published courses for 'course data' type that are now ended.
                        Iterator<DbDataCollection> itr = collections.iterator();
                        while(itr.hasNext()){
                            DbDataCollection dataCollection = itr.next();
                            if(ExperimentStatus.ENDED.equals(dataCollection.getStatus())){
                                itr.remove();
                            }
                        }
                    }
                    
                    return collections;
                }
            });
        } catch (Exception e) {
            logger.error("Failed to retrieve a published course of type "+dataSetType+" for the course at '" + courseFolder + "'.", e);
            return null;
        }
    }
    
    /**
     * Return the published courses (Data Collection objects) that reference the specific source course folder 
     * and of the specific data set type.
     * @param courseFolder the workspace path to the course folder that is taken by learners. 
     * Can't be null or empty. e.g. "Public/Hello World"
     * @param dataSetType the enumerated type of publish course to search for. Can't be null.
     * @param excludeEnded whether to remove data collections that have the status of ExperimentStates.ENDED from the returned collection.
     * @param session the session to perform the database operations in. If null a new session will be created and closed.
     * @return the published courses found. Can be empty but won't be null.
     */
    public List<DataCollectionItem> getPublishedCourseItemsOfType(final String courseFolder, final DataSetType dataSetType, boolean excludeEnded, Session session){
        
        List<DataCollectionItem> dataCollectionItems = new ArrayList<>(1);
        List<DbDataCollection> dataCollections = getPublishedCoursesOfType(courseFolder, dataSetType, excludeEnded, session);
        for(DbDataCollection dataCollection : dataCollections){
            
            DataCollectionItem dataCollectionItem = reverter.convertExperiment(dataCollection);
            dataCollectionItems.add(dataCollectionItem);
        }
        
        return dataCollectionItems;
    }


    /**
     * Gets any experiments from the experiment table that belong to the source
     * course id. The subject and LTI result collections have been lazy loaded
     * so they will not be accessible in the objects returned from this method.
     *
     * @param courseId The source course id that the experiment belongs to (maps
     *        to the sourcecourseid column in the database table).
     * @param session the session to perform the action in. If null the current
     *        session will be used to create a transaction and close the
     *        session.
     * @return collection of Experiments that belong to the source course id.
     *         Can be empty but not null.
     */
    public List<DbDataCollection> getExperimentsByCourseId(final String courseId, Session session) {

        try {
            return executeWithinSession(session, new SafeSessionHandler<List<DbDataCollection>>() {
                @Override
                public List<DbDataCollection> execute(Session session) {
                    final String queryString = "from DbDataCollection as dataCollection where dataCollection.sourceCourseId = '"
                            + courseId + "'";

                    /* Lazy loaded subject and LTI result collections because
                     * the methods using the returned list doesn't need them. */
                    return selectRowsByQuery(DbDataCollection.class, queryString, -1, -1, session);
                }
            });
        } catch (Exception e) {
            logger.error("Failed to get experiments with course id '" + courseId + "'.", e);
            return null;
        }
    }

    /**
     * Retrieve the user permissions for the course collection.
     * 
     * @param username the user to retrieve permissions for.
     * @param collection the course collection the user is checking.
     * @return the {@link DataCollectionUserRole}.
     */
    public DataCollectionUserRole getCourseCollectionPermissionForUser(String username, DbCourseCollection collection) {
        DataCollectionUserRole collectionUserRole = null;
        for (DbCourseCollectionEntry entry : collection.getEntries()) {

            /* Find the user's permission for this entry */
            DataCollectionUserRole entryUserRole = null;
            for (DbDataCollectionPermission perm : entry.getCourse().getPermissions()) {
                if (StringUtils.equals(perm.getUsername(), username)) {
                    entryUserRole = perm.getDataCollectionUserRole();
                }
            }

            /* If the user has no permission for this entry, then they have no
             * permission for the entire collection. Return early. */
            if (entryUserRole == null) {
                return null;
            }

            /* If this role is lower than the current collection role, update
             * the current collection role */
            if (collectionUserRole == null || entryUserRole.compareTo(collectionUserRole) > 0) {
                collectionUserRole = entryUserRole;
            }
        }

        return collectionUserRole;
    }

    /**
     * Creates a new course collection with some provided initial data.
     *
     * @param name The display name of the new {@link DbCourseCollection} to
     *        create. Can't be null.
     * @param owner The user who will own this {@link DbCourseCollection}. Can't
     *        be null or empty.
     * @param description An optional description used to describe the course
     *        collection in more detail. Can be null.
     * @return The newly created {@link DbCourseCollection}. Can't be null.
     * @throws UMSDatabaseException If there was an issue inserting the course
     *         collection into the database.
     */
    public DbCourseCollection createCourseCollection(String name, String owner, String description)
            throws UMSDatabaseException {
        if (name == null) {
            throw new IllegalArgumentException("The parameter 'name' cannot be null.");
        } else if (isBlank(owner)) {
            throw new IllegalArgumentException("The parameter 'owner' cannot be blank.");
        }

        DbCourseCollection dbCourseCollection = new DbCourseCollection();
        dbCourseCollection.setName(name);
        dbCourseCollection.setOwner(owner);
        dbCourseCollection.setDescription(description);

        try {
            insertRow(dbCourseCollection);
            return dbCourseCollection;
        } catch (Exception e) {
            throw new UMSDatabaseException("Failed to create new course collection named '" + name + "' in the UMS database.", e);
        }
    }

    /**
     * Reorders the course collection's experiments.
     * 
     * @param username The name of the user performing the action. Used for
     *        authentication purposes. Can't be blank.
     * @param collectionId The id of the course collection for which the course
     *        order is changing. Can't be blank.
     * @param oldIndex The index of the item to move. Must be within the
     *        indexing bounds of the targeted course collection. Cannot be
     *        negative and should be within the bounds of the collection being
     *        modified.
     * @param newIndex The index of the item to move. Must be within the
     *        indexing bounds of the targeted course collection. Cannot be
     *        negative and should be within the bounds of the collection being
     *        modified.
     * @param currentOrdering The order of the collection of element at the time
     *        the action was requested. Can't be null or empty.
     * @param session the session to perform the reorder in. If null the current
     *        session will be used to create a transaction and close the
     *        session.
     */
    public void reorderCourseCollection(String username, String collectionId, int oldIndex, int newIndex, List<DataCollectionItem> currentOrdering, Session session) {
        /* Validate the parameters */
        if (isBlank(username)) {
            throw new IllegalArgumentException("The parameter 'username' cannot be blank.");
        } else if (isBlank(collectionId)) {
            throw new IllegalArgumentException("The parameter 'collectionId' cannot be blank.");
        } else if (currentOrdering == null || currentOrdering.isEmpty()) {
            throw new IllegalArgumentException("The parameter 'currentOrdering' cannot be null or empty.");
        } else if (oldIndex < 0 || oldIndex >= currentOrdering.size()) {
            throw new IllegalArgumentException(
                    "The parameter 'oldIndex' must be within the bounds of 'currentOrdering'.");
        } else if (newIndex < 0 || newIndex >= currentOrdering.size()) {
            throw new IllegalArgumentException(
                    "The parameter 'newIndex' must be within the bounds of 'currentOrdering'.");
        }

        try {
            executeWithinSession(session, new SafeSessionHandler<Void>() {

                @Override
                public Void execute(Session session) {
                    DbCourseCollection dbCollection = getCourseCollectionById(collectionId, session);

                    /* Check permissions for the operation */
                    DataCollectionUserRole collectionPerm = getCourseCollectionPermissionForUser(username,
                            dbCollection);
                    boolean isManager = collectionPerm == DataCollectionUserRole.MANAGER;
                    boolean isOwner = StringUtils.equals(dbCollection.getOwner(), username);

                    /* If the user doesn't have permissions, throw an
                     * exception. */
                    if (!isManager && !isOwner) {
                        final String errorMsg = String.format(
                                "The user '%s' does not have permission to reorder courses in the collection '%s'",
                                username, collectionId);
                        throw new UnsupportedOperationException(errorMsg);
                    }

                    /* Lock the rows that will be edited by the SQL
                     * statements. */
                    LockRequest lockRequest = session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE));
                    lockRequest.lock(dbCollection);

                    int bottomBound = oldIndex > newIndex ? newIndex : oldIndex;
                    int topBound = oldIndex < newIndex ? newIndex : oldIndex;
                    for (int i = bottomBound; i <= topBound; i++) {
                        String courseId = currentOrdering.get(i).getId();
                        DbDataCollection collection = selectRowById(courseId, DbDataCollection.class, session);
                        lockRequest.lock(collection);
                    }

                    /* Check to make sure the collection's elements haven't
                     * changed since the request was made */
                    boolean collectionsChanged = false;
                    if (dbCollection.getEntries().size() == currentOrdering.size()) {
                        for (int i = 0; i < dbCollection.getEntries().size(); i++) {
                            final String dbId = dbCollection.getEntries().get(i).getId();
                            final String passedId = currentOrdering.get(i).getId();
                            if (StringUtils.equals(dbId, passedId)) {
                                collectionsChanged = true;
                                break;
                            }
                        }
                    } else {
                        collectionsChanged = true;
                    }

                    if (collectionsChanged) {
                        throw new UnsupportedOperationException(
                                "The collection has changed since the request for reorder was created.");
                    }

                    int safeSpace = dbCollection.getEntries().size();

                    final String schema = getSchemaFromConfig();
                    final String MOVE_TO_SAFE = String.format(
                            "UPDATE %s.COURSECOLLECTIONENTRY SET COLLECTIONINDEX = %d WHERE COLLECTIONINDEX = %d",
                            schema, safeSpace, oldIndex);
                    String SHIFT;
                    final boolean isMovingUp = oldIndex < newIndex;
                    if (isMovingUp) {
                        SHIFT = String.format(
                                "UPDATE %s.COURSECOLLECTIONENTRY SET COLLECTIONINDEX = COLLECTIONINDEX - 1 WHERE COLLECTIONINDEX > %d AND COLLECTIONINDEX <= %d",
                                schema, oldIndex, newIndex);
                    } else {
                        SHIFT = String.format(
                                "UPDATE %s.COURSECOLLECTIONENTRY SET COLLECTIONINDEX = COLLECTIONINDEX + 1 WHERE COLLECTIONINDEX >= %d AND COLLECTIONINDEX < %d",
                                schema, newIndex, oldIndex);
                    }
                    final String MOVE_BACK = String.format(
                            "UPDATE %s.COURSECOLLECTIONENTRY SET COLLECTIONINDEX = %d WHERE COLLECTIONINDEX = %d",
                            schema, newIndex, safeSpace);

                    executeUpdateSQLQuery(MOVE_TO_SAFE, session);
                    executeUpdateSQLQuery(SHIFT, session);
                    executeUpdateSQLQuery(MOVE_BACK, session);

                    return null;
                }
            });
        } catch (Exception e) {
            String errorMsg = String.format(
                    "There was a problem reordering the course '%d' to '%d' in the collection '%s'", oldIndex, newIndex,
                    collectionId);

            logger.error(errorMsg, e);
            throw new DetailedException("There was a problem reordering a course collection", errorMsg, e);
        }
    }

    /**
     * Gets the {@link DbCourseCollection} object identified by a course group
     * id.
     *
     * @param courseCollectionId The id of the course collection whose courses should be
     *        fetched.
     * @param session the session to perform the query in. If null the current
     *        session will be used to create a transaction and close the
     *        session.
     * @return The {@link List} of {@link DbDataCollection} that belongs to the
     *         specified course group.
     */
    public DbCourseCollection getCourseCollectionById(String courseCollectionId, Session session) {
        final boolean isLocalSession = session == null;
        if (session == null) {
            session = getCurrentSession();
            session.beginTransaction();
        }

        try {
            return selectRowById(courseCollectionId, DbCourseCollection.class, session);
        } catch (Exception ex) {
            String message = String.format("There was a problem getting the course collection '%s'", courseCollectionId);
            logger.error(message, ex);
            if (isLocalSession && session.isOpen()) {
                session.getTransaction().rollback();
            }
        } finally {
            if (isLocalSession && session.isOpen()) {
                session.close();
            }
        }

        return null;
    }

    /**
     * Gets every {@link CourseCollection} that a given use has access to.
     *
     * @param username The name of the user for which to query all
     *        {@link DbCourseCollection}.
     * @return The {@link Collection} of {@link DbCourseCollection} containing
     *         all {@link DbCourseCollection} that the user has access to. Can
     *         be empty. Can't be null.
     */
    public Collection<DbCourseCollection> getCourseCollectionsByUser(String username) {
        /* This query selects all the DbCourseCollection objects where the
         * provided username is either an owner of the course collection or has
         * some permission defined for one of the courses/experiments inside the
         * collection. */
        String queryString = "from DbCourseCollection as outer_collection where outer_collection.id in ("
                + "select distinct collection.id from DbCourseCollection as collection "
                + "join collection.entries as entry "
                + "join entry.course as course "
                + "join course.permissions as perimission "
                + "where perimission.username = '" + username + "') OR outer_collection.owner = '" + username + "'";

        return selectRowsByQuery(DbCourseCollection.class, queryString, -1, -1);
    }

    /**
     *
     * @param user The name of the user who is performing this operation. Used
     *        for authentication purposes. Can't be null or empty.
     * @param collectionId The id of the {@link DbCourseCollection} to which to
     *        add the course. Can't be null or empty.
     * @param course The course which is to be added to the collection. Can't be
     *        null.
     * @param session the session to perform the query in. If null the current
     *        session will be used to create a transaction and close the
     *        session.
     */
    public void addCourseToCollection(String user, String collectionId, DataCollectionItem course, Session session) {

        /* Validate the parameters that have been supplied to the method. */
        if (isBlank(user)) {
            throw new IllegalArgumentException("The parameter 'user' cannot be blank.");
        } else if (isBlank(collectionId)) {
            throw new IllegalArgumentException("The parameter 'collectionId' cannot be blank.");
        } else if (course == null) {
            throw new IllegalArgumentException("The parameter 'course' cannot be null.");
        }

        final boolean isLocalSession = session == null;
        if (session == null) {
            session = UMSDatabaseManager.getInstance().getCurrentSession();
            session.beginTransaction();
        }

        try {
            final DbCourseCollection dbCollection = selectRowById(collectionId, DbCourseCollection.class, session);

            /* Confirm that the acting user is the owner of the course
             * collection */
            if (!StringUtils.equals(dbCollection.getOwner(), user)) {
                final String errorMsg = String.format(
                        "The user '%s' is not the owner of the course '%s' and therefore cannot add a course to it",
                        user, collectionId);
                throw new UnsupportedOperationException(errorMsg);
            }

            /* Create the new entry */
            final DbCourseCollectionEntry newEntry = new DbCourseCollectionEntry();
            newEntry.setId(collectionId);
            newEntry.setPosition(dbCollection.getEntries().size());
            newEntry.setCourse(getExperiment(course.getId(), false, false, session));
            insertRow(newEntry, session);

            /* Apply the changes to the database */
            session.getTransaction().commit();
        } catch (Exception ex) {
            final String message = String.format(
                    "There was a problem adding the course '%s' to the collection '%s'",
                    course.toString(),
                    collectionId);
            logger.error(message, ex);
            if (isLocalSession && session.isOpen()) {
                session.getTransaction().rollback();
            }
        } finally {
            if (isLocalSession && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Deletes a {@link DbCourseCollection} from the database with a given id.
     *
     * @param courseCollectionId The id of the {@link DbCourseCollection} that
     *        should be deleted.
     * @throws DetailedException If the specified {@link DbCourseCollection}
     *         could not be deleted from the database.
     */
    public void deleteCourseCollection(String courseCollectionId) throws DetailedException {

        final Session session = UMSDatabaseManager.getInstance().getCurrentSession();
        session.beginTransaction();

        try {

            /* Get all of the mappings that map the specified collection to an
             * experiment. */
            final String orphanQuery = "select entry.course.id from DbCourseCollectionEntry entry "
                    + "where entry.id = '" + courseCollectionId + "' "
                    + "group by entry.course "
                    + "having count(entry.id) = 1";
            List<String> coursesToDelete = selectRowsByQuery(String.class, orphanQuery, -1, -1, session);

            /* Delete the mapping between the collections and the experiments */
            final String entryQuery = "delete from " + getSchemaFromConfig() + ".CourseCollectionEntry "
                    + "where collectionid_fk = '" + courseCollectionId + "'";
            executeDeleteSQLQuery(entryQuery, session);

            /* If the course collection was not empty, delete all the courses
             * that were contained within it. */
            if (!coursesToDelete.isEmpty()) {
                for (String courseId : coursesToDelete) {
                    deleteExperiment(courseId, session);
                }
            }

            /* Delete the collection itself */
            DbCourseCollection dbCourseCollection = selectRowById(courseCollectionId, DbCourseCollection.class,
                    session);
            deleteRow(dbCourseCollection, session);
            session.getTransaction().commit();

        } catch (Exception e) {
            if (session.isOpen()) {
                session.getTransaction().rollback();
                session.close();
            }

            throw new DetailedException(
                    "There was a problem deleting a course collection",
                    "There was a problem deleting the course collection with id '" + courseCollectionId + "'",
                    e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Return the experiment entry for the experiment id provided.
     *
     * @param experimentId the unique id of an experiment to retrieve from the
     *        database
     * @param eagerFetchSubjects true to eager fetch the experiment's lazy
     *        loaded subjects. The "Eager" term applies to a hibernate term for
     *        "Eager" fetching which is a more expensive database call. For
     *        cases that only need the O{@link DbDataCollection} (without the
     *        subjects), set eagerFetchSubjects to false; however if the full
     *        object needs to be filled out, set eagerFetchSubjects to true.
     * @param eagerFetchLtiResults true to eager fetch the experiment's lazy
     *        loaded LTI results. The "Eager" term applies to a hibernate term
     *        for "Eager" fetching which is a more expensive database call. For
     *        cases that only need the {@link DbDataCollection} (without the
     *        ltiResults), set eagerFetchLtiResults to false; however if the
     *        full object needs to be filled out, set eagerFetchLtiResults to
     *        true.
     * @param session the session to perform the update in. If null the current
     *        session will be used to create a transaction and close the
     *        session.
     * @return the experiment entry found in the database. Will be null if the
     *         entry was not found.
     */
    public DbDataCollection getExperiment(String experimentId, boolean eagerFetchSubjects, boolean eagerFetchLtiResults, Session session) {

        DbDataCollection experiment = null;
        boolean shouldCloseSession = session == null;
        if (session == null) {
            session = getCurrentSession();
            session.beginTransaction();
        }

        try {
            if (eagerFetchSubjects || eagerFetchLtiResults) {

                experiment = selectRowById(experimentId, DbDataCollection.class, session);
                if(experiment != null){
                    if (eagerFetchSubjects) {
                        Hibernate.initialize(experiment.getSubjects());
                    }
    
                    if (eagerFetchLtiResults) {
                        Hibernate.initialize(experiment.getDataCollectionResultsLti());
                    }
                }
            } else {
                experiment = selectRowById(experimentId, DbDataCollection.class, session);
            }
        } finally {
            if (session.isOpen() && shouldCloseSession) {
                session.close();
            }
        }

        return experiment;
    }

    /**
     * Return the subject db entry uniquely identified by the experiment and subject id provided.
     *
     * @param experimentId the unique id of an experiment
     * @param subjectId the unique id of a subject in that experiment
     * @return the subject db entry identified for those two keys
     */
    public DbExperimentSubject getExperimentSubject(String experimentId, int subjectId){
        // we want to eager load subjects, but lazy load LTI results because we are only using subjects in this method.
        DbDataCollection experiment = getExperiment(experimentId, true, false, null);
        for(DbExperimentSubject subject : experiment.getSubjects()){

            if(subject.getExperimentSubjectId().getSubjectId() == subjectId){
                return subject;
            }
        }

        return null;
    }

    /**
     * Add the subject to the experiment.  The experiment is referenced in the subject object, therefore that value
     * can't be null.
     *
     * @param subject the subject to add to the database
     * @param experiment the experiment with which the subject is being added.
     * @throws DetailedException if a problem occurred while trying to add the subject to the experiment.
     */
    public void addSubjectToExperiment(DbExperimentSubject subject, DbDataCollection experiment) throws DetailedException{

        if(subject == null){
            throw new IllegalArgumentException("The subject can't be null.");
        }else if(subject.getExperimentSubjectId() != null){
            throw new IllegalArgumentException("The subject provided has an id of "+subject.getExperimentSubjectId()+" but this is suppose to be a new subject.");
        }else if(experiment == null){
            throw new IllegalArgumentException("The data set can't be null.");
        }

        //get the experiments next subject id
        int nextSubjectId = getExperimentNextSubjectId(experiment.getId());
        DbExperimentSubjectId subjectId = new DbExperimentSubjectId(experiment, nextSubjectId);
        subject.setExperimentSubjectId(subjectId);

        try{
            insertRow(subject);
            if(logger.isInfoEnabled()){
                logger.info("Successfully inserted data set subject of "+subject);
            }
        }catch(Exception e){
            throw new DetailedException("Failed to add subject to data set.", "The subject could not be inserted into the database.\n"+subject, e);
        }
    }

    /**
     * Checks if the provided experiment contains a permission user role that matches with one of the required permissions.
     * If the data collection does not contain any permissions, the provided username will be compared to the data collection author to see if they are a match.
     *
     * @param dbExperiment the data collection item to check user permission.  Can't be null.
     * @param username the user trying to gain access to the data collection.  Can't be null or empty.
     * @param requiredPermissions the collection of user roles to check against. If at least one of
     * these user roles is found within the data collection item, then the user is considered to have permission.
     * @return true if the data collection item has a permission user role contained within the provided collection of
     * permissions or if the data collection item has no permissions but its author is the same as the provided user; false otherwise.
     */
    public boolean checkDataCollectionForPermissions(DbDataCollection dbExperiment, String username,
            DataCollectionUserRole... requiredPermissions) {

        if (dbExperiment == null || isBlank(username) || requiredPermissions == null
                || requiredPermissions.length == 0) {
            return false;
        }

        // check permissions
        List<DataCollectionUserRole> requiredPermissionsList = Arrays.asList(requiredPermissions);
        Set<DbDataCollectionPermission> permissions = dbExperiment.getPermissions();
        boolean hasPermissions = false;
        if(permissions != null && !permissions.isEmpty()){
            for(DbDataCollectionPermission permission : permissions){

                if(StringUtils.equalsIgnoreCase(permission.getUsername(), username) &&
                        requiredPermissionsList.contains(permission.getDataCollectionUserRole())){

                    hasPermissions = true;
                    break;
                }
            }
        }else{
            //this could be a data collection item created before permissions were added

            if(StringUtils.equalsIgnoreCase(dbExperiment.getAuthorUsername(), username)){
                hasPermissions = true;
            }
        }

        return hasPermissions;
    }

    /**
     * Update any properties of the experiment in the experiments table.
     *
     * @param username the user requesting the update. Used to check for
     *        permission to modify the experiment properties. Can't be null or
     *        empty.
     * @param dbExperiment the experiment to update. Can't be null and must have
     *        a valid experiment id.
     * @param session the session to perform the update in. If null the current
     *        session will be used to create a transaction and close the
     *        session.
     * @return whether the experiment properties was updated.
     * @throws DetailedException if there was a problem updating that entry,
     *         including if that entry doesn't exist in the database.
     */
    public boolean updateExperimentProperties(String username, DbDataCollection dbExperiment, Session session) throws DetailedException{

        //retrieve by id first
        // we want to lazy load subjects and LTI results because we aren't using either collection in this method.
        DbDataCollection oldExperiment = getExperiment(dbExperiment.getId(), false, false, session);
        if(oldExperiment == null){
            throw new DetailedException("Unable to update the published course named '"+dbExperiment.getName()+"'.", "Failed to find the published course based on the id "+dbExperiment.getId(), null);
        }

        if(logger.isDebugEnabled()){
            logger.debug("Attempting to update existing data set entry of:\n"+oldExperiment+", to\n"+dbExperiment);
        }

        // whether the property change includes a status change
        boolean changingStatus = dbExperiment.getStatus() != null && oldExperiment.getStatus() != dbExperiment.getStatus();

        //does this user have permissions to change the experiment properties
        boolean hasPropertiesPermission = checkDataCollectionForPermissions(oldExperiment, username, DataCollectionUserRole.OWNER, DataCollectionUserRole.MANAGER);

        if(hasPropertiesPermission){

            // Adding New Permission Rules
            // 1. must have an owner in the new permissions and it must match the author
            // 2. only a single owner is allowed
            // 3. null roles mean the user permissions should be removed
            // 4. only 1 role per user
            boolean hasOwner = false;
            boolean noPermisionsPreviously = oldExperiment.getPermissions() == null || oldExperiment.getPermissions().isEmpty();
            Set<DbDataCollectionPermission> newPermissions = dbExperiment.getPermissions();
            if(newPermissions != null && !newPermissions.isEmpty()){
                // the update has permission info that might need to be updated

                Iterator<DbDataCollectionPermission> permissionItr = newPermissions.iterator();
                while(permissionItr.hasNext()){

                    DbDataCollectionPermission permission = permissionItr.next();

                    if(permission.getDataCollectionUserRole() == DataCollectionUserRole.OWNER){
                        //owner rules:

                        if(hasOwner){
                            throw new DetailedException("Failed to update the published course in the database.",
                                    "Tried to assign more than 1 owner to the published course named '"+dbExperiment.getName()+"'  The owner is already "+oldExperiment.getAuthorUsername()+".\n(id "+dbExperiment.getId()+")", null);
                        }else if(StringUtils.equalsIgnoreCase(permission.getUsername(), oldExperiment.getAuthorUsername())){
                            hasOwner = true;
                        }

                    }else if(permission.getDataCollectionUserRole() == null){
                        //remove user permissions
                        permissionItr.remove();
                    }
                }
            }else if(noPermisionsPreviously){
                // this experiment pre-dates permissions and the experiment update also has not permissions,
                // create owner permissions

                if(StringUtils.equalsIgnoreCase(oldExperiment.getAuthorUsername(), username)){
                    hasOwner = true;

                    newPermissions = new HashSet<>();
                    DbDataCollectionPermission ownerPermission = new DbDataCollectionPermission();
                    ownerPermission.setDataCollectionId(dbExperiment.getId());
                    ownerPermission.setDataCollectionUserRole(DataCollectionUserRole.OWNER);
                    ownerPermission.setUsername(username);
                    newPermissions.add(ownerPermission);
                    dbExperiment.setPermissions(newPermissions);
                }

            }

            if(!hasOwner){
                throw new DetailedException("Failed to update the published course in the database.",
                        "The updated published course named '"+dbExperiment.getName()+"' is missing a user assigned as the owner.\n(id "+dbExperiment.getId()+")", null);
            }

            //apply updates
            oldExperiment.setDescription(dbExperiment.getDescription());
            oldExperiment.setName(dbExperiment.getName());
            oldExperiment.setStatus(dbExperiment.getStatus());
            oldExperiment.setCourseFolder(dbExperiment.getCourseFolder());
            oldExperiment.setSourceCourseId(dbExperiment.getSourceCourseId());
            oldExperiment.setDataSetType(dbExperiment.getDataSetType());
            oldExperiment.setPermissions(dbExperiment.getPermissions());

        }else if(changingStatus){

            //does this user have permissions to change the experiment status
            boolean hasStatusPermission = checkDataCollectionForPermissions(oldExperiment, username, DataCollectionUserRole.values());

            if(hasStatusPermission){
                //apply status update (ONLY)
                oldExperiment.setStatus(dbExperiment.getStatus());
            }else{
                //the experiment status is NOT changing AND the user does NOT have permissions to change the experiment properties
                throw new DetailedException("Failed to update the published course in the database.",
                        "The user '"+username+"' doesn't have permission to update the '"+dbExperiment.getName()+"' properties.\n(id "+dbExperiment.getId()+")", null);
            }

        } else {

            //the experiment status is NOT changing AND the user does NOT have permissions to change the experiment properties
            throw new DetailedException("Failed to update the published course in the database.",
                    "The user '"+username+"' doesn't have permission to update the '"+dbExperiment.getName()+"' properties.\n(id "+dbExperiment.getId()+")", null);
        }

        boolean updated;
        if (session == null) {
            updated = updateRow(oldExperiment);
        } else {
            updated = updateRow(oldExperiment, session);
        }

        if (updated) {
            if (logger.isInfoEnabled()) {
                logger.info("Successfully updated data set entry : " + oldExperiment);
            }
            return updated;
        } else {
            logger.error("Failed to update published course to : " + dbExperiment);
            throw new DetailedException("Failed to update the published course in the database.",
                    "There was a problem updating the published course '" + dbExperiment.getName()
                            + "' properties.\n(id " + dbExperiment.getId() + ")",
                    null);
        }
    }

    /**
     * Returns the next subject id for this experiment.
     * Note: this will lock the experiment database entry while retrieving and updating the next subject id value.
     *
     * @param experimentId the unique identifier of the experiment to look up the next subject id for
     * @return the next subject id for this experiment
     * @throws DetailedException if there was a problem locking, retrieving or updating the next subject id for an experiment
     */
    public int getExperimentNextSubjectId(String experimentId) throws DetailedException{

        // we want to lazy load subjects and LTI results because we aren't using either collection in this method.
        DbDataCollection experiment = getExperiment(experimentId, false, false, null);

        Session session = null;
        Transaction transaction = null;
        try{
            //lock it in the db
            LockOptions lockOptions = new LockOptions();
            lockOptions.setLockMode(LockMode.PESSIMISTIC_WRITE);  //Note: LockMode.WRITE causes exception
            session = getCurrentSession();
            transaction = session.beginTransaction();
            session.buildLockRequest(lockOptions).lock(experiment);

            //get the value
            int nextIdValue = experiment.getSubjectNextId();

            //increment the value
            experiment.setSubjectNextId(nextIdValue+1);
            updateRow(experiment, session);
            transaction.commit();

            return nextIdValue;

        }catch(Exception e){

            logger.error("Failed to get the next subject id for the data set "+experimentId, e);

            if(transaction != null){
                transaction.rollback();
            }

            throw new DetailedException("Failed to retrieve the next experiment subject id.",
                    "There was an exception while locking, retrieving and updating the experiment next subject id for data set "+experiment+" : "+e.getMessage(), e);
        
        } finally {
            
            if(session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Login the user to the UMS database.
     *
     * @param request the request to log in a user.  Can't be null.
     * @param deploymentMode the type of deployment GIFT is running in.  This has implications for whether the request
     * should be completed or not.  E.g. if running in non-Experiment mode and have no internet, the login should still work.
     * @return UserData information about the user logged in
     * @throws UserNotFoundException if the login could not be completed for any reason
     */
    public UserData loginUser(LoginRequest request, DeploymentModeEnum deploymentMode) throws UserNotFoundException{

        if(request == null){
            throw new IllegalArgumentException("The request can't be null.");
        }

        if (CompareUtil.equalsNullSafe(request.getUserType(), UserSessionType.GIFT_USER)) {
            return loginGiftUser(request, deploymentMode);
        } else if (CompareUtil.equalsNullSafe(request.getUserType(), UserSessionType.LTI_USER)) {
            return loginLtiUser(request, deploymentMode);
        } else {
            throw new UserNotFoundException("Cannot login user, because the type of login for type: " + request.getUserType() +
                    " is not implemented.");
        }

    }

    /**
     * Private function to login an lti user (user from an LTI Tool Consumer)
     *
     * @param request the request to log in a GIFT user.  Can't be null.
     * @param deploymentMode the type of deployment GIFT is running in.  This has implications for whether the request
     * should be completed or not.  E.g. if running in non-Experiment mode and have no internet, the login should still work.
     * @return UserData information about the user logged in
     * @throws UserNotFoundException if the LTI user could not be found.
     */
    private UserData loginLtiUser(LoginRequest request, DeploymentModeEnum deploymentMode) throws UserNotFoundException{
        DbGlobalUser globalUser = selectRowById(request.getUserId(), DbGlobalUser.class);

        if (globalUser != null) {

            DbLtiUserRecord ltiUser = this.getLtiUserRecordFromGlobalId(globalUser.getGlobalId());
            if (ltiUser != null) {
                UserData userData = new UserData(globalUser.getGlobalId(),null, GenderEnum.MALE);
                return userData;
            } else {
                throw new UserNotFoundException("Unable to login the lti user because the lti user record could not be found.");
            }

        } else {
            throw new UserNotFoundException("Unable to login the lti user because the global user record could not be found.");
        }

    }


    /**
     * Private function to login a normal GIFT user, where GIFT user means a user who signs in with a username/password
     * via Redmine authentication.
     *
     * @param request the request to log in a GIFT user.  Can't be null.
     * @param deploymentMode the type of deployment GIFT is running in.  This has implications for whether the request
     * should be completed or not.  E.g. if running in non-Experiment mode and have no internet, the login should still work.
     * @return UserData information about the user logged in
     * @throws UserNotFoundException if the GIFT user could not be found.
     */
    private UserData loginGiftUser(LoginRequest request, DeploymentModeEnum deploymentMode) throws UserNotFoundException{
        //find user in db
        DbUser user = selectRowById(request.getUserId(), DbUser.class);

        if(user == null){
            throw new UserNotFoundException("Unable to find an existing anonymous user with the id = " + request.getUserId() + " (i.e. a user associated with that user id in the database with no username)");
        }else if(user.getUsername() == null || user.getUsername().equals(request.getUsername())){
            /* The user in the database has no user name. This is used by:
             *    Desktop Deployment Simple Login
             *    Simple Deployment Simple Login
             * OR
             * The user's name in the database matches the name on the request. This is used by:
             *    Desktop Deployment Dashboard Login
             *    Server Deployment Dashboard Login
             *
             * Either way reply with respond successfully
            */

            UserData userData = new UserData(user.getUserId(), user.getLMSUserName(), GenderEnum.valueOf(user.getGender()));
            userData.setUsername(user.getUsername());
            return userData;
        }else{
            throw new UserNotFoundException("The incorrect username of "+request.getUsername()+" was provided for user with id = " + request.getUserId());
        }
    }

    /**
     * Retrieves all the GIFT users that are currently in the UMS database.  This normally means these users
     * have logged into this GIFT instance successfully in the lifetime of the UMS database.<br/>
     * Note: this can be expensive depending on the number of users in the database.  Suggest not calling this in server deployment mode.
     *
     * @return list of all users.  Can be empty but not null.
     */
    public List<DbUser> getAllUsers() {
        return selectAllRows(DbUser.class);
    }


    /**
     * Gets a list of experiment data table names
     *
     * @return List<String> The list of experiment data table names.  Can be empty but not null.
     */
    @SuppressWarnings("unused")
    private List<String> getExperimentDataTableNames() {
        return getTableNames(EXPERIMENT_DATA_TABLES);
    }
    
    /**
     * Get the list of domain session message log file names that are NOT referenced by any
     * published course (GIFT experiment or LTI) in this GIFT instance.
     * 
     * @return can be empty but won't be null.
     */
    @SuppressWarnings("unchecked")
    public Set<File> getUnusedDomainSessionLogFiles(){
        
        String selectStatement = "select MESSAGELOGFILENAME from " + UMSDatabaseManager.getInstance().getSchemaFromConfig() + ".experimentsubject";
        List<Object> results = UMSDatabaseManager.getInstance().executeSelectSQLQuery(selectStatement);
        
        // get all the LTI log files names
        selectStatement = "select MESSAGELOGFILENAME from " + UMSDatabaseManager.getInstance().getSchemaFromConfig() + ".datacollectionresultslti";
        results.addAll(UMSDatabaseManager.getInstance().executeSelectSQLQuery(selectStatement));
        
        Set<String> referencedLogFilesSet = new HashSet<>((List<String>)(List<?>)results);
        
        // get all the domain session log files
        File domainSessionLogDir = new File(PackageUtil.getDomainSessions());
        File[] messageLogFiles = domainSessionLogDir.listFiles(new EventSourceFileFilter());
        
        Set<File> messageLogFilesSet = new HashSet<>(Arrays.asList(messageLogFiles));
        Iterator<File> logFilesItr = messageLogFilesSet.iterator();
        while(logFilesItr.hasNext()){

            File file = logFilesItr.next();

            if(file.isDirectory()){
                for(File child : file.listFiles()){
                    String path = file.getName() + File.separator + child.getName();
                    
                    if(referencedLogFilesSet.contains(path)){
                        logFilesItr.remove();
                        continue;
                    }
                }
            }
        }
        return messageLogFilesSet;
    }

    @Override
    public void clearUserData() throws HibernateException{

        StringBuffer sb = new StringBuffer();
        sb.append("Clear User Data results:\n");

        Session session = createNewSession();

        session.beginTransaction();

        //
        // Build delete SQL statements for user and experiment subject data tables
        //
        List<String> tableNames = getUserDataTableNames();

        for(int i = tableNames.size()-1; i >= 0; i--){

            //Note: the table order is based on INSERT key constraints, DELETE constraints are the opposite
            SQLQuery query = session.createSQLQuery("DELETE FROM app."+tableNames.get(i));
            int rowsAffected = query.executeUpdate();

            sb.append("Deleted ").append(rowsAffected).append(" from ").append(tableNames.get(i)).append("\n");
        }

        //
        // Remove concept based Knowledge Assessment generated surveys (generated from survey context concept question bank) since their responses
        // were just deleted and the SAS UI doesn't allow access to those surveys
        //

        //Filter 1: first retrieve all surveys that have the reserved survey name unique to concept based generated surveys
        DbSurvey surveyExample = new DbSurvey();
        surveyExample.setName(mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_GENERATED_SURVEY_NAME);
        List<DbSurvey> knowledgeAssessmentSurveys = selectRowsByExample(surveyExample, DbSurvey.class, session);
        sb.append("Found ").append(knowledgeAssessmentSurveys.size()).append(" potential knowledge assessment generated surveys to delete.\n");
        for(DbSurvey survey : knowledgeAssessmentSurveys){

            //Filter 2: Check that the survey is in a Survey Context - it must be in order to be a generated survey
            if(!survey.getSurveyContextSurveys().isEmpty()){

                //Filter 3: is the Gift Key used in all survey context(s) the EMAP GIFT key
                boolean deleteSurvey = false;
                for(DbSurveyContextSurvey context : survey.getSurveyContextSurveys()){

                    String giftKey = context.getGiftKey();

                    if(giftKey.matches(mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_GIFT_KEY_REGEX)){
                        deleteSurvey = true;

                    }else{
                        deleteSurvey = false;
                        break;
                    }

                }//end for

                if(deleteSurvey){

                    //
                    // Delete the survey - because of the way our hibernate classes are setup the order of operations is
                    //                     as follows:
                    //                     1) delete 'survey context survey' table rows referenced by the survey to delete
                    //                     2) remove the deleted 'survey context survey' object from the survey context's
                    //                        set of 'survey context survey'(s). If this is not done, an "deleted object would
                    //                        be re-saved by cascade (remove deleted object from associations)" exception is thrown.
                    //                     3) finally, delete the survey row from the survey table.
                    //
                    for(DbSurveyContextSurvey surveyContextSurvey : survey.getSurveyContextSurveys()){

                        try{
                            deleteRow(surveyContextSurvey, session);
                            surveyContextSurvey.getSurveyContext().getSurveyContextSurveys().remove(surveyContextSurvey);
                        }catch(Exception e){
                            logger.error("failed to delete survey from survey context.", e);
                            sb.append(" ERROR: unable to delete ").append(surveyContextSurvey).append("because ").append(e.getMessage());
                        }
                    }

                    sb.append("Found knowledge assessment generated survey ...");
                    try{
                        deleteRow(survey, session);
                        sb.append(" Deleted ").append(survey).append(" (including the survey context information)");
                    }catch(Exception e){
                        logger.error("failed to delete survey.", e);
                        sb.append(" ERROR: unable to delete ").append(survey).append("because ").append(e.getMessage());
                    }


                }else{
                    sb.append("ERROR: the potential knowledge assessment generated survey of ").append(survey).append(" has at least one incorrect GIFT key, therefore it won't be deleted.");
                }

            }else{
                sb.append("ERROR: the potential knowledge assessment generated survey of ").append(survey).append(" is not associated with a survey context, therefore it won't be deleted.");
            }

            sb.append("\n");
        }

        session.getTransaction().commit();
        session.close();

        if(logger.isInfoEnabled()){
            logger.info(sb.toString());
        }
    }

    /**
     * Return the Hibernate survey context survey found using the two parameters provided.</br>
     * This method is needed incase the gift key string has special characters that could cause a query string to escape.
     *
     * @param surveyContextId unique survey context id to use as part one of the table key
     * @param giftKey unique gift key in the survey context to use as part two of the table key
     * @return the hibernate survey context survey found in the db, null if not found.
     */
    public DbSurveyContextSurvey getSurveyContextSurvey(int surveyContextId, String giftKey){

        Session session = createNewSession();
        try {
            Query query = session.createQuery("from DbSurveyContextSurvey as scs where scs.surveyContext.surveyContextId = " + surveyContextId +
                    " and scs.giftKey = :giftkey");
            query.setString("giftkey", giftKey);
            List<DbSurveyContextSurvey> dbSurveyContextSurveys = selectRowsByQuery(DbSurveyContextSurvey.class, query, -1, 1);
            return dbSurveyContextSurveys.isEmpty() ? null : dbSurveyContextSurveys.get(0);
            
        } finally {
            
            if(session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Create the specified number of users in the UMS database, starting with the id specified.
     *
     * The creation logic is atomic, meaning all or nothing.
     *
     * @param startingId the starting user id to use when creating new users.  If the id is taken this method will return false
     *                  and no users will be created.
     * @param numberOfUsers the number of new users to create.
     * @return boolean whether or not all users were created in the database
     */
    public boolean createUsers(int startingId, int numberOfUsers){

        if(startingId <= 0){
            throw new IllegalArgumentException("The starting id of "+startingId+" must be greater than zero");
        }else if(numberOfUsers <= 0){
            throw new IllegalArgumentException("The number of users of "+numberOfUsers+" must be greater than zero");
        }

        Session session = null;
        try{

            session = createNewSession();
            session.beginTransaction();

            //start by setting the next user id generated value
            SQLQuery query = session.createSQLQuery("UPDATE app.userpktb SET SEQUENCE_NEXT_HI_VALUE="+startingId+" WHERE userkey='uservalue'");
            query.executeUpdate();

            //have to commit and then restart the transaction in order for the user
            //table to get the latest user key value that was just updated
            session.getTransaction().commit();
            session.beginTransaction();

            //
            // insert users into db
            //
            for(int id = startingId; id <= startingId + numberOfUsers; id++){

                DbUser user = new DbUser(GenderEnum.MALE.getName());
                user.setLMSUserName("User-"+id);

                try{
                    insertRow(user, session);
                }catch(Exception e){
                    throw new Exception("Failed to insert "+user+".", e);
                }

            }

            session.getTransaction().commit();
            session.close();

        }catch(Exception e){
            logger.error("There was an exception thrown while trying to create "+numberOfUsers+" new users in the database.", e);

            if(session != null){
                session.getTransaction().rollback();
                session.close();
            }

            return false;
        }

        return true;
    }

    /**
     * This method will cleanup the id generator tables (listed below) by making sure each table only
     * has 1 row (for some unknown reason, sometimes the tables start having 2 rows with the same exact column values) as well
     * as setting the value for the next id to the next highest value.  The next highest value is based on the highest
     * value found for rows that use this table's id generator logic, plus the allocation size set in the get methods TableGenerator
     * tag.
     *
     * Id Generator Table names:  (* means this method cleans that table)
     * *categorypktb
     * *filepktb
     * *folderpktb
     * *listoptionpktb
     * *optionlistpktb
     * *propertykeypktb
     * *propertyvaluepktb
     * *questionpktb
     * *questionpropertyvaluepktb
     * *questionresponsepktb
     * *questiontypepktb
     * sessionpktb
     * *surveycontextpktb
     * *surveyelementpktb
     * *surveyelementtypepktb
     * *surveypagepktb
     * *surveypageresponsepktb
     * *surveypktb
     * *surveyresponsepktb
     * userpktb
     *
     * @throws Exception if there was a problem with any of the tables including retrieving the table's id generator table, that generator
     * table's value or updating that generator table's rows based on the next highest value to use.
     */
    public void cleanupIdGenerators() throws Exception{

        StringBuffer sb = new StringBuffer();
        sb.append("Cleaning up Id generator table(s):\n");

        Session session = createNewSession();
        session.beginTransaction();

        resetNextIdGeneratorValue(DbCategory.class, "getCategoryId", sb, session);
        resetNextIdGeneratorValue(DbDataFile.class, "getFileId", sb, session);
        resetNextIdGeneratorValue(DbFolder.class, "getFolderId", sb, session);
        resetNextIdGeneratorValue(DbListOption.class, "getListOptionId", sb, session);
        resetNextIdGeneratorValue(DbOptionList.class, "getOptionListId", sb, session);
        resetNextIdGeneratorValue(DbPropertyKey.class, "getId", sb, session);
        resetNextIdGeneratorValue(DbPropertyValue.class, "getPropertyValueId", sb, session);
        resetNextIdGeneratorValue(DbQuestionResponse.class, "getQuestionResponseId", sb, session);
        resetNextIdGeneratorValue(DbQuestionType.class, "getId", sb, session);
        resetNextIdGeneratorValue(DbQuestion.class, "getQuestionId", sb, session);
        resetNextIdGeneratorValue(DbQuestionPropertyValue.class, "getId", sb, session);
        resetNextIdGeneratorValue(DbDomainSession.class, "getSessionId", sb, session);
        resetNextIdGeneratorValue(DbSurveyContext.class, "getSurveyContextId", sb, session);
        resetNextIdGeneratorValue(DbSurveyElement.class, "getSurveyElementId", sb, session);
        resetNextIdGeneratorValue(DbSurveyElementType.class, "getId", sb, session);
        resetNextIdGeneratorValue(DbSurveyPage.class, "getSurveyPageId", sb, session);
        resetNextIdGeneratorValue(DbSurveyPageResponse.class, "getSurveyPageResponseId", sb, session);
        resetNextIdGeneratorValue(DbSurvey.class, "getSurveyId", sb, session);
        resetNextIdGeneratorValue(DbSurveyResponse.class, "getSurveyResponseId", sb, session);

        session.getTransaction().commit();
        session.close();

        if(logger.isInfoEnabled()){
            logger.info(sb.toString());
        }
    }

    /**
     * Return the highest id value found given the UMS table class and the Id getter method name provided.
     *
     * @param tableClass the UMS hibernate table class to search for the highest id value.  Used in reflection calls.
     * @param getIdMethodName the UMS hibernate table class id getter method name.  Used in reflection calls.
     * @return int the next highest id for the id generator table.  This could be the initial value if a highest value other
     * than the initial is not found.
     * @throws DetailedException if the id of the survey was unable to be retrieved.
     */
    private int getHighestValue(Class<?> tableClass, String getIdMethodName)
            throws DetailedException {

        Method idMethod;
        try {
            idMethod = tableClass.getMethod(getIdMethodName);
        } catch (Exception e) {
            throw new DetailedException("Failed to find the 'getId' method.",
                    "The method '" + getIdMethodName + "' could not be found in class '" + tableClass + "'.", e);
        }

        TableGenerator generator = idMethod.getAnnotation(TableGenerator.class);
        int initialValue = generator.initialValue();

        int highestId = initialValue;
        List<?> surveys = selectAllRows(tableClass);
        for(Object row : surveys){

            try {
                int value = (int) idMethod.invoke(row);

                if (value > highestId) {
                    highestId = value;
                }
            } catch (Exception e) {
                throw new DetailedException("Caught exception trying to retrieve the survey id.",
                        "Failed to retrieve the survey id using method '" + getIdMethodName + "' in class '"
                                + tableClass + "'.",
                        e);
            }
        }

        return highestId;
    }

    /**
     * This method will delete any existing rows in the id generator table and insert a new row that contains
     * the next highest id value to use.
     *
     * @param tableClass the UMS hibernate table class to search for the highest id value.  Used in reflection calls.
     * @param getIdMethodName the UMS hibernate table class id getter method name.  Used in reflection calls.
     * @param information used to place useful logging information for the status of what was changed in the database.
     * @param session the hibernate session to do the database queries through
     * @throws DetailedException if the id of the survey was unable to be retrieved.
     */
    private void resetNextIdGeneratorValue(Class<?> tableClass, String getIdMethodName,
            StringBuffer information, Session session) throws DetailedException {

        Method idMethod;
        try {
            idMethod = tableClass.getMethod(getIdMethodName);
        } catch (Exception e) {
            throw new DetailedException("Failed to find the 'getId' method.",
                    "The method '" + getIdMethodName + "' could not be found in class '" + tableClass + "'.", e);
        }

        TableGenerator generator = idMethod.getAnnotation(TableGenerator.class);

        int currentHighIdValue = getHighestValue(tableClass, getIdMethodName);

        String nameUse = generator.name();
        String rowKey = generator.pkColumnValue();
        String columnName = generator.pkColumnName();
        String tableName = generator.table();
        int allocationSize = generator.allocationSize();
        int valueSize = currentHighIdValue + allocationSize;

        information.append("The highest id for ").append(nameUse).append(" was ").append(currentHighIdValue).append(".  Setting the new highest Id to ").append(valueSize).append(".\n");
        currentHighIdValue += allocationSize;

        SQLQuery query = session.createSQLQuery("DELETE FROM app."+tableName+" WHERE "+columnName+"='"+rowKey+"'");
        int rowsAffected = query.executeUpdate();
        information.append("Deleted ").append(rowsAffected).append(" row(s) from ").append(tableName).append("\n");

        query = session.createSQLQuery("INSERT INTO app."+tableName+" VALUES ('"+rowKey+"',"+currentHighIdValue+")");
        rowsAffected = query.executeUpdate();
        information.append("Inserted ").append(rowsAffected).append(" row(s) into ").append(tableName).append("\n");
    }

    @Override
    public List<Class<? extends Object>> getUserDataTableClasses() {
        return USER_DATA_TABLES;
    }

    /**
     * Gets the list of hibernate classes for the survey data classes.
     * The list follows an ordering of table names based on key constraints in those tables.
     * The ordering is based on SQL INSERT operations where, for example, a foreign key
     * must exist in a table before creating an entry that references that foreign key in a different table.
     *
     * @return List<Class<? extends Object>> The list of hibernate classes for the survey data classes.
     *          Note: the returned list is unmodifiable because it follows the key constraint ordering for INSERT operations.
     */
    public static List<Class<? extends Object>> getSurveyDataTableClasses() {
        return SURVEY_DATA_TABLES;
    }

    /**
     * Gets the list of hibernate classes for the survey response classes.
     * The list follows an ordering of table names based on key constraints in those tables.
     * The ordering is based on SQL INSERT operations where, for example, a foreign key
     * must exist in a table before creating an entry that references that foreign key in a different table.
     *
     * @return List<Class<? extends Object>> The list of hibernate classes for the survey response classes.
     *          Note: the returned list is unmodifiable because it follows the key constraint ordering for INSERT operations.
     */
    public static List<Class<? extends Object>> getSurveyResponseTableClasses() {
        return SURVEY_RESPONSE_TABLES;
    }

    /**
     * Gets the list of hibernate table names for all visible and editable to usernames tables.
     *
     * @return List<String> The list of hibernate classes for the survey response classes.
     */
    public static List<String> getPermissionsTableNames() {
        return PERMISSION_TABLES;
    }

    /**
     * Used to interact with the UMS database via scripts or outside of the UMS module.
     *
     * @param args - used to identify which logic to execution.  To see the list of arguments, run w/o an argument.
     */
    public static void main(String[] args){

        //use UMS log4j
        PropertyConfigurator.configure(PackageUtil.getConfiguration() + "/ums/ums.log4j.properties");

        boolean displayHelp = args.length == 0;
        
        final String CLEAR_CMD = "clear";
        final String CREATE_USERS_CMD = "create-users";
        final String RECREATE_DB_CMD = "recreate-db";
        final String CLEANUP_IDS_CMD = "cleanupIdGenerators";
        final String CLEAR_UNUSED_LOG_FILES_CMD = "clearUnusedLogFiles";
        final String DIRECT_LOG_FILES_CMD = "directLogFiles";

        for(int i = 0; i < args.length; i++){

            switch(args[i]){

            case CLEAR_CMD:
                //Clear the user data from the database

                try{
                    System.out.println("Starting to clear user data...");
                    UMSDatabaseManager.getInstance().clearUserData();
                    System.out.println("Finished clearing user data. ");

                }catch(Throwable e){
                    System.out.println("Caught exception while trying to clear user data.");
                    e.printStackTrace();
                    displayHelp = true;
                }

                break;

            case CREATE_USERS_CMD:
                //create N users starting with the specified user id

                if(args.length > i + 1){
                    //there is at least one more element
                    String[] values = args[i+1].split(":");

                    //increment index of argument since the next argument was consumed
                    i++;

                    if(values.length == 2){

                        int startingId = Integer.valueOf(values[0]);
                        int numOfUsers = Integer.valueOf(values[1]);

                        if(startingId <= 0){
                            System.out.println("Incorrect starting id of "+startingId+" for create-user argument of "+args[i+1]+".  Check the help for usage.");
                            displayHelp = true;
                            break;
                        }else if(numOfUsers <= 0){
                            System.out.println("Incorrect number of users of "+numOfUsers+" for create-user argument of "+args[i+1]+".  Check the help for usage.");
                            displayHelp = true;
                            break;
                        }else{
                            //create users

                            try{
                                if(UMSDatabaseManager.getInstance().createUsers(startingId, numOfUsers)){
                                    System.out.println("Successfully created "+numOfUsers+" users starting with user id of "+startingId+".");
                                }else{
                                    System.err.println("Failed to create all "+numOfUsers+" users, starting with user id of "+startingId+".  Check the log for more details.");
                                }
                            }catch(ConfigurationException e){
                                e.printStackTrace();
                            }
                        }

                    }else{
                        System.out.println("Incorrect syntax for create-user argument of "+args[i+1]+".  Check the help for usage.");
                        displayHelp = true;
                    }

                }else{
                    System.out.println("There are no more arguments after the create-user argument.  Check the help for usage.");
                    displayHelp = true;
                }

                break;

            case RECREATE_DB_CMD:
                try{
                    UMSDatabaseManager.getInstance().recreateDB();
                }catch(ConfigurationException e){
                    e.printStackTrace();
                }
                break;

            case CLEANUP_IDS_CMD:

                try{
                    System.out.println("Starting to cleanup Id generator tables...");
                    UMSDatabaseManager.getInstance().cleanupIdGenerators();
                    System.out.println("Finished cleanup. ");

                }catch(Throwable e){
                    System.out.println("Caught exception while trying to cleanup Id generator tables.");
                    e.printStackTrace();
                    displayHelp = true;
                }

                break;
                
            case DIRECT_LOG_FILES_CMD:
             // Move legacy domain session logs from output/messages to output/domainSessions, 
             // the folder where domain sessions logs and other files associated with them are written to

                try{
                    System.out.println("Starting to move all of the domain session log files to domainSession folder...");
                    List<DbExperimentSubject> experimentSubjects = UMSDatabaseManager.getInstance().selectAllRows(DbExperimentSubject.class);
                    List<DbDataCollectionResultsLti> ltiSubjects = UMSDatabaseManager.getInstance().selectAllRows(DbDataCollectionResultsLti.class);

                    File messageLogDir = new File(PackageUtil.getMessageLog());
                    File[] messageLogFiles = messageLogDir.listFiles(new EventSourceFileFilter());
                    Set<File> fileSet = new HashSet<>();
                    
                    // create domain sessions root folder if it doesn't exist
                    File domainSessionsFolder = new File(PackageUtil.getDomainSessions());
                    if(!domainSessionsFolder.exists()){
                        domainSessionsFolder.mkdir();
                    }
                    
                    for(DbExperimentSubject subject : experimentSubjects){

                        String oldLocation = PackageUtil.getMessageLog() + File.separator + subject.getMessageLogFilename();
                        Path source = Paths.get(oldLocation);
                        
                        if (Files.exists(source)) {
                            String folderName = source.getFileName().toString().replace(".log", "");
                            String newLocation = folderName + File.separator + source.getFileName().toString();
                            Path target = Paths.get(PackageUtil.getDomainSessions() + File.separator + newLocation);
      
                            Path folderPath = Paths.get(PackageUtil.getDomainSessions() + File.separator + folderName);
                            if(!Files.exists(folderPath)){
                            Files.createDirectory(folderPath);
                            }
    
                            try{
                                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                                File file = new File(newLocation);
                                subject.setMessageLogFilename(newLocation);
                                fileSet.add(file);
                                UMSDatabaseManager.getInstance().updateRow(subject);
                            } catch(IOException e){
                                System.out.println("Caught Exception while trying to move and update Experiment record:\n"+subject);
                                e.printStackTrace();
                            }
                        }
                    }
                    
                    for(DbDataCollectionResultsLti subject : ltiSubjects){

                        String oldLocation = PackageUtil.getMessageLog() + File.separator + subject.getMessageLogFileName();
                        Path source = Paths.get(oldLocation);
                        
                        if (Files.exists(source)) {
                            String folderName = source.getFileName().toString().replace(".log", "");
                            String newLocation = folderName + File.separator + source.getFileName().toString();
                            Path target = Paths.get(PackageUtil.getDomainSessions() + File.separator + newLocation);
      
                            Path folderPath = Paths.get(PackageUtil.getDomainSessions() + File.separator + folderName);
                            if(!Files.exists(folderPath)){
                            Files.createDirectory(folderPath);
                            }
    
                            try{
                                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                                File file = new File(newLocation);
                                subject.setMessageLogFileName(newLocation);
                                fileSet.add(file);
                                UMSDatabaseManager.getInstance().updateRow(subject);
                            } catch(IOException e){
                                System.out.println("Caught Exception while trying to move and update LTI record:\n"+subject);
                                e.printStackTrace();
                            }
                        }
                    }

                    for(File file : messageLogFiles){
                        
                        String src = file.getPath();
                        Path source = Paths.get(src);
                        
                        if (Files.exists(source)) {
                            String folderName = source.getFileName().toString().replace(".log", "");
                            if (!folderName.substring(0, 6).equals("system")) {
                                String newLocation = folderName + File.separator + source.getFileName().toString();
                                Path target = Paths.get(PackageUtil.getDomainSessions() + File.separator + newLocation);
                                
                                Path folderPath = Paths.get(PackageUtil.getDomainSessions() + File.separator + folderName);
                                if(!Files.exists(folderPath)){
                                    Files.createDirectory(folderPath);
                                }
        
                                if(!fileSet.contains(file)){
                                    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                                }
                            }
                        }
                    }
                    
                    // move logIndex.json
                    // MH: not performing this move because the log entries in the index would not be updated with this logic
                    // Instead we will delete the file, forcing a re-indexing the next time game master is visited.   Favorites data will be lost.                 
                    File logIndexJSONFile = new File(PackageUtil.getMessageLog() + File.separator + "logIndex.json");
                    if(logIndexJSONFile.exists()){
//                        Path source = Paths.get(logIndexJSONFile.getAbsolutePath());
//                        Path target = Paths.get(PackageUtil.getDomainSessions() + File.separator + "logIndex.json");
//                        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                        logIndexJSONFile.delete();
                    }
                    
                    System.out.println("Finished file transfer. ");

                }catch(Throwable e){
                    System.out.println("Caught exception while trying to move log files.");
                    e.printStackTrace();
                    displayHelp = true;
                }
                
                
                break;
                
            case CLEAR_UNUSED_LOG_FILES_CMD:
            
                try{
                    Set<File> unusedFiles = UMSDatabaseManager.getInstance().getUnusedDomainSessionLogFiles();
                    System.out.println("Deleting "+unusedFiles.size()+" unused domain session message log files.");
                    int successCnt = 0;
                    
                    for(File file : unusedFiles){
                        for (File child : file.listFiles()){
                           if(child.delete()){
                               successCnt++;
                           }
                        }
                        file.delete();
                    }
                    System.out.println("Finished deleting files. "+successCnt+" out of "+unusedFiles.size()+" deleted.");
                    
                }catch(Throwable e){
                    System.out.println("Caught exception while trying to clear unused domain session log files.");
                    e.printStackTrace();
                    displayHelp = true;
                }
            
                break;

            default:
                displayHelp = true;
            }//end switch

            if(displayHelp){
                break;
            }

        }//end for

        if(displayHelp){
            System.out.println("Usage: UMSDatabaseManager <option>\n\n" +
                    "\t "+CLEAR_CMD+"\t\t\t\t\t\t clear rows from tables created by executing user and domain sessions.\n\n" +
                    "\t "+CREATE_USERS_CMD+" <starting-id>:<number-of-users>\t create the specified number of users in the database starting \n" +
                    "\t "+RECREATE_DB_CMD+"\t erase all data in the db and recreate the database \n"+
                    "\t "+CLEANUP_IDS_CMD+"\t set all the Id generator table values to the next highest value based on the rows found in the database.\n" +
                    "\t "+DIRECT_LOG_FILES_CMD+"\t move all of the domain session log files to the domainSession folder.\n" +
                    "\t "+CLEAR_UNUSED_LOG_FILES_CMD+"\t permanently deletes domain session message log files that aren't referenced in a published course.\n" +
                    "\t\t\t\t\t\t\t at the specified starting user id (LMS user name = \"User-<id>\")");
        }

        System.out.println("\nGood-bye");
    }

    /**
     * Safely executes some code within a session. Performs the all the
     * necessary boiler plate for getting a session (if one wasn't provided) and
     * properly cleaning the session up.
     *
     * @param session The {@link Session} that the code should be executed
     *        within. If null, the current session will be retrieved and managed
     *        by the method.
     * @param handler The handler to execute between the creation and clean up
     *        of a session (if a session wasn't provided). Can't be null.
     * @return the result of the handler
     * @throws Exception if an exception is caught during execution of the handler.
     */
    private <T> T  executeWithinSession(Session session, SafeSessionHandler<T> handler) throws Exception {
        if (handler == null) {
            throw new IllegalArgumentException("The parameter 'handler' cannot be null.");
        }

        boolean isLocalSession = session == null;
        if (session == null) {
            session = getCurrentSession();
            if (session == null) {
                throw new NullPointerException(
                        "The 'getCurrentSession' message returned null. Unable to get a valid session.");
            }

            session.beginTransaction();
        }

        try {
            T result = handler.execute(session);

            if (isLocalSession) {
                session.getTransaction().commit();
            }

            return result;
        } catch (Exception e) {
            if (isLocalSession && session.isOpen()) {
                session.getTransaction().rollback();
            }

            throw e;
        } finally {
            if (isLocalSession && session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * Gets the IDs of all of the domain sessions that are associated with users with the given user ID and
     * are NOT part of an experiment. This can be used to differentiate between domain sessions that are run
     * by regular users and domain sessions that are run by experiment subjects.
     * 
     * @param userId the user ID of the domain sessions to look for
     * @return a list of domain session IDs. Can be empty but will not be null.
     */
    public List<Integer> getNonExperimentDomainSessionIds(int userId){
        
        /* 
         * We found in #5571 that we can no longer just query for all domain sessions that do not 
         * have experiment IDs associated with them, since this fails to account for COURSE_DATA experiments that
         * are now created for courses in the Take a Course page automatically as part of #4921.
         * 
         * Instead, this query has been changed to cross-reference the Experiments table to include
         * domain sessions that are part of COURSE_DATA experiments.
         */
        
        return UMSDatabaseManager.getInstance().executeSelectSQLQuery(
                "select dSession.SESSIONID_PK "
                        + "from " + UMSDatabaseManager.getInstance().getSchemaFromConfig() + ".DomainSession as dSession "
                        + "where "
                        + "dSession.USERID_FK = " + userId + " "
                        + "AND ("
                              /* Domain sessions from legacy courses on Take a Course page have no experiment IDs. */
                            + "dSession.EXPERIMENTID_FK is null "
                            + "OR "
                              /* Domain sessions from newer course on Take a Course page will have experiment IDs, so
                               * we must check if the experiments are COURSE_DATA experiments */
                            + "dSession.EXPERIMENTID_FK in ("
                                + "select experiment.EXPERIMENTID_PK "
                                + "from " + UMSDatabaseManager.getInstance().getSchemaFromConfig() + ".Experiment as experiment "
                                + "where experiment.DATASETTYPE = '" + ExperimentUtil.DataSetType.COURSE_DATA.name() 
                            + "')"
                        + ")", 
                Integer.class);
    }
    
    /**
     * Update the domain session's course source id column value with the new value provided.
     * @param oldCoursePath the previous/old path to the course xml.  This is an identification of the source of this domain, 
     * i.e. the course workspace path e.g. mhoffman/test/test.course.xml  
     * @param newCoursePath the new path to the course xml.  This is an identification of the source of this domain, 
     * i.e. the course workspace path e.g. mhoffman/test new/test new.course.xml  
     * @param session the session to perform the update in.  If null, the session is created and closed before returning.
     * @return the number of domain session rows updated.
     */
    public int updateDomainSessionSourceId(String oldCoursePath, String newCoursePath, Session session){
        
        // get domain sessions for the course (DbDomainSession)
        String query = "from DbDomainSession as dSession "
                + "where dSession.domainSourceId = '" + oldCoursePath + "'";
        if(logger.isDebugEnabled()){
            logger.debug("Executing query to get domain sessions for course:\n"+query);
        }
        
        boolean isLocalSession = session == null;
        try{
            if(isLocalSession){
                session = getCurrentSession();
                session.beginTransaction();
            }
            
            List<DbDomainSession> dSessions = UMSDatabaseManager.getInstance().selectRowsByQuery(DbDomainSession.class, query, 
                    -1, -1, session);
            for(DbDomainSession dSession : dSessions){
                
                dSession.setDomainSourceId(newCoursePath);
                
                try{
                    updateRow(dSession, session);
                }catch(Exception e){
                    throw new DetailedException("Failed to update a domain session with the new course source path", 
                            "There was an exception while trying to update the course path from '"+oldCoursePath+"' to '"+newCoursePath+"' in\n"+dSession, e);
                }
            }
            
            return dSessions.size();
        }finally{
            
            if(isLocalSession && session != null && session.isOpen()){
                //end the session by releasing the JDBC connection and cleaning up
                session.close();
            }
        }
    }
    
    /**
     * Return the list of domain session log file names created when users took the course provided.
     * 
     * @param course contains information about the course in order to look up the log files created
     * @return a list of domain session log files names for the course (e.g. domainSession329_uId1_2020-09-21_16-28-53.log)
     * Can be empty but not null.
     */
    public List<String> getCourseDomainSessionLogFileNames(CourseRecord course){
               
        // get domain sessions for the course (DbDomainSession)
        String query = "from DbDomainSession as dSession "
                + "where dSession.domainSourceId = '" + course.getCoursePath() + "'";
        if(logger.isDebugEnabled()){
            logger.debug("Executing query to get domain sessions for course:\n"+query);
        }
        
        List<DbDomainSession> dSessions = UMSDatabaseManager.getInstance().selectRowsByQuery(DbDomainSession.class, query, 
                        -1, -1);
        
        List<String> logFileNames;
        if(dSessions.isEmpty()){
            logFileNames = new ArrayList<>(0);
        }else{
            //
            // get domain session log file names for each domain session (DbEventFile for each DbDomainSession)
            //
            StringBuilder logFileIdsSB = new StringBuilder("(");
            for(int index = 0; index < dSessions.size(); index++){
                DbDomainSession dSession = dSessions.get(index);
                if(dSession.getEventFile() != null){
                    if(index > 0){
                        logFileIdsSB.append(",");
                    }
                    logFileIdsSB.append(dSession.getEventFile().getFileId());
                }            
            }
            logFileIdsSB.append(")");
            
            query = "select dFile.FILENAME "
                    + "from " + UMSDatabaseManager.getInstance().getSchemaFromConfig() + ".DataFile as dFile "
                    + "where dFile.FILEID_PK IN " + logFileIdsSB.toString();
            if(logger.isDebugEnabled()){
                logger.debug("Executing query to get log file names:\n"+query);
            }
            
            logFileNames = UMSDatabaseManager.getInstance().executeSelectSQLQuery(query, 
                            String.class);
        }
        return logFileNames;
    }
}
