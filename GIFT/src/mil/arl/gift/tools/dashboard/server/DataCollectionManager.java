/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import static mil.arl.gift.common.util.StringUtils.isBlank;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.CourseRecord;
import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.ert.GenerateReportStatus;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.experiment.DataCollectionItem.DataCollectionPermission;
import mil.arl.gift.common.experiment.ExperimentUtil.DataCollectionUserRole;
import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.experiment.ExperimentUtil.ExperimentStatus;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DownloadableFileRef;
import mil.arl.gift.common.io.ExportProperties;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.ProhibitedUserException;
import mil.arl.gift.common.metrics.MetricsSender;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.DomainCourseFileHandler.CourseOptionsWrapper;
import mil.arl.gift.lms.impl.common.LmsInvalidCourseRecordException;
import mil.arl.gift.lms.impl.common.LmsInvalidStudentIdException;
import mil.arl.gift.lms.impl.common.LmsIoException;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.db.DbServicesInterface;
import mil.arl.gift.tools.services.experiment.DataCollectionServicesInterface;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import mil.arl.gift.tools.services.file.ExportManager;
import mil.arl.gift.ums.db.HibernateObjectReverter;
import mil.arl.gift.ums.db.UMSDatabaseException;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.survey.SurveyValidationException;
import mil.arl.gift.ums.db.table.DbCourseCollection;
import mil.arl.gift.ums.db.table.DbDataCollection;
import mil.arl.gift.ums.db.table.DbDataCollectionPermission;

/**
 * An object that handles requests to create and delete data collection items and monitors progress on a per-user basis
 *
 * @author nroberts
 */
public class DataCollectionManager {

     /** used to convert hibernate table classes into their common class representation */
    private static HibernateObjectReverter hibernateToGift = new HibernateObjectReverter(UMSDatabaseManager.getInstance());

    /** The logger for the class */
    private static Logger logger = LoggerFactory.getLogger(DataCollectionManager.class);

    /** A mapping from each username to the progress of their currently running experiment creation process, if one exists */
    private ConcurrentHashMap<String, ProgressIndicator> usernameToCreateProgress = new ConcurrentHashMap<String, ProgressIndicator>();

    /** A mapping from each username to the progress of their currently running experiment deletion process, if one exists */
    private ConcurrentHashMap<String, ProgressIndicator> usernameToDeleteProgress = new ConcurrentHashMap<String, ProgressIndicator>();

    /** A mapping from each username to the progress of their currently running experiment course export, if one exists */
    private ConcurrentHashMap<String, ProgressIndicator> usernameToCourseExportProgress = new ConcurrentHashMap<String, ProgressIndicator>();

    /** A mapping from each username to the progress of their currently running experiment raw data export, if one exists */
    private ConcurrentHashMap<String, ProgressIndicator> usernameToRawDataExportProgress = new ConcurrentHashMap<String, ProgressIndicator>();

    /** A mapping from each username to the progress of their currently running experiment report export, if one exists */
    private ConcurrentHashMap<String, GenerateReportStatus> usernameToReportExportProgress = new ConcurrentHashMap<String, GenerateReportStatus>();

    /** Singleton instance of this class*/
    private static DataCollectionManager singleton = null;

    /** A metrics sender used to track the time spent on asynchronous operations. If null, no metrics will be tracked. */
    private MetricsSender metrics = null;

    /**
     * Creates a new data collection manager
     */
    private DataCollectionManager(){

    }

    /**
     * Creates a new data collection item.
     *
     * @param name the name of the data collection. Doesn't have to be unique.
     *        Can't be null.
     * @param description information about the data collection. Can be null.
     * @param username the user name of the user that is creating the data
     *        collection
     * @param courseId the unique identifier of the course the experiment will
     *        use
     * @param dataSetType the type of data collection (experiment, lti, etc).
     * @return the data collection object created
     */
    public DataCollectionItem createDataCollectionItem(String name, String description, String username, String courseId, DataSetType dataSetType)
            throws URISyntaxException, IllegalArgumentException, DetailedException, FileNotFoundException, SurveyValidationException,
            LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException, UMSDatabaseException, ProhibitedUserException{

        ProgressIndicator progress = new ProgressIndicator(0, "Overall Publish Course Creation");

        usernameToCreateProgress.put(username, progress);

        CourseOptionsWrapper courseWrapper = new CourseOptionsWrapper();

        ServicesManager.getInstance().getFileServices().getCourses(username,
                courseWrapper,
                false,
                null);

        DbDataCollection dbExperiment = ServicesManager.getInstance().getDataCollectionServices()
                .createDataCollectionItem(name, description, username, courseId,
                        dataSetType, courseWrapper,
                        DashboardProperties.getInstance().shouldValidateCourseContentAtCourseListRequest(), DashboardProperties.getInstance().shouldValidateSurveyAtCourseListRequest(), progress);

        DataCollectionItem experiment = hibernateToGift.convertExperiment(dbExperiment);

        return experiment;
    }

    /**
     * Gets the progress of the experiment creation being run for the specified user
     *
     * @param username the username of the user to get the progress for
     * @return the progress of the experiment creation
     */
    public ProgressIndicator getDataCollectionItemCreationProgress(String username){
        return usernameToCreateProgress.get(username);
    }

    /**
     * Cancels the data collection item creation being run for the given user, if one exists
     *
     * @param username the username of the user for whom to cancel the experiment creation
     */
    public void cancelDataCollectionItemCreation(String username){

        if(username == null){
            throw new IllegalArgumentException("Username cannot be null");
        }

        ProgressIndicator progress = usernameToCreateProgress.get(username);
        progress.setShouldCancel(true);
    }

    /**
     * Deletes a data collection object
     *
     * @param username the user name of the user that is deleting the data collection
     * @param experimentId the unique identifier of the data collection
     */
    public void deleteDataCollectionItem(String username, String experimentId) throws URISyntaxException, IllegalArgumentException, DetailedException, FileNotFoundException, SurveyValidationException, LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException, UMSDatabaseException{

        ProgressIndicator progress = new ProgressIndicator(0, "Overall Publish Course Deletion");

        usernameToDeleteProgress.put(username, progress);

        ServicesManager.getInstance().getDataCollectionServices().deleteDataCollectionItem(username, experimentId, progress);
    }

    /**
     * Remove the delete data collection item progress indicator because it is no longer needed for the user
     * @param username the user that was deleting a data collection object
     */
    public void cleanupDeleteDataCollectionItemProgressIndicator(String username){
        usernameToDeleteProgress.remove(username);
    }

    /**
     * Remove the create data collection item progress indicator because it is no longer needed for the user
     * @param username the user that was creating a data collection object
     */
    public void cleanupCreateDataCollectionItemProgressIndicator(String username){
        usernameToCreateProgress.remove(username);
    }

    /**
     * Remove the data collection course export progress indicator because it is no longer needed for the user
     * @param username the user that was exporting a data collection course
     */
    public void cleanupCourseExportProgressIndicator(String username){
        usernameToCourseExportProgress.remove(username);
    }

    /**
     * Remove the export data collection raw data progress indicator because it is no longer needed for the user
     * @param username the user that was exporting the raw data of a data collection object
     */
    public void cleanupRawDataExportProgressIndicator(String username){
        usernameToRawDataExportProgress.remove(username);
    }

    /**
     * Gets the progress of the experiment deletion being run for the specified user
     *
     * @param username the username of the user to get the progress for
     * @return the progress of the experiment deletion.  Can be null.
     */
    public ProgressIndicator getDataCollectionItemDeletionProgress(String username){
        return usernameToDeleteProgress.get(username);
    }

    /**
     * Cancels the experiment deletion being run for the given user, if one exists
     *
     * @param username the username of the user for whom to cancel the experiment deletion
     */
    public void cancelDataCollectionItemDeletion(String username){

        if(username == null){
            throw new IllegalArgumentException("Username cannot be null");
        }

        ProgressIndicator progress = usernameToDeleteProgress.get(username);
        progress.setShouldCancel(true);
    }

    /**
     * Exports the course being used by the given data collection item
     *
     * @param username the user name of the user that is requesting the export
     * @param experimentId the unique identifier of the experiment
     */
    public DownloadableFileRef exportDataCollectionItemCourse(String username, String experimentId)
            throws URISyntaxException, IllegalArgumentException, DetailedException, FileNotFoundException, SurveyValidationException,
            LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException, UMSDatabaseException, ProhibitedUserException{

        DownloadableFileRef result = null;

        ProgressIndicator progress = new ProgressIndicator(0, "Overall Export");

        usernameToCourseExportProgress.put(username, progress);

        DataCollectionServicesInterface expServices = ServicesManager.getInstance().getDataCollectionServices();

        DbDataCollection experiment = expServices.getDataCollectionItem(experimentId);
        if (experiment == null) {
            throw new DetailedException("The export course request failed.",
                    "The published course could not be found with id: " + experimentId, null);
        }

        if (experiment.isDataSetType(DataSetType.LTI)) {
            // published courses that are LTI type have courses that live in the domain / workspace location instead of 'experiments'.
            // The export should happen the same as a normal course export since the lti course is the same course as in the dashboard.
            // In this case, the published course sourceCourseId is used to get the path to the source course file and then call into the
            // file services export course functionality.
            String exportFileName = getDataCollectionItemCourseFileName(username, experiment.getDataSetType());

            AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
            DbServicesInterface dbServices = ServicesManager.getInstance().getDbServices();
            // Fetch the record even if the deleted flag is set to true.
            CourseRecord courseRecord = dbServices.getCourseById(experiment.getSourceCourseId(), true);

            if (courseRecord == null) {
                throw new DetailedException("The export course request failed.",
                        "The course record could not be found for source course id: " + experiment.getSourceCourseId(), null);
            }
            // LTI courses are located in the normal workspace folder.
            CourseOptionsWrapper wrapper = new CourseOptionsWrapper();
            fileServices.getCourse(username, courseRecord.getCoursePath(), wrapper, false, false, false, null);

            ArrayList<DomainOption> domains = new ArrayList<DomainOption>();
            for (DomainOption domainOption : wrapper.domainOptions.values()) {
                domains.add(domainOption);
            }

            // This should only return one result back.
            if (domains.size() > 1) {
                throw new DetailedException("The export course request failed.",
                    "Found more than one domain for course path: " + courseRecord.getCoursePath(), null);
            }

            ExportProperties properties = new ExportProperties(username, domains, exportFileName, progress);
            result = fileServices.exportCourses(properties);
        } else if (experiment.isDataSetType(DataSetType.EXPERIMENT) || experiment.isDataSetType(DataSetType.COURSE_DATA)) {
            // Original logic for exporting experiment type of published courses.  The course folder lives in the 'experiment' location.
            String exportFileName = getDataCollectionItemCourseFileName(username, experiment.getDataSetType());
            result = expServices.exportDataCollectionItemCourse(username, exportFileName, experimentId, progress);
        } else {
            logger.error("Error occurred while exporting the course.  Unsupported published course type: " + experiment.getDataSetType());
            throw new DetailedException("The export course request failed.", "Unable to export published course type: " + experiment.getDataSetType() , null);
        }


        // Make sure there is a result by now.
        if (result == null) {
            throw new DetailedException("The export course request failed.",
                    "The downloadable file reference could not be found.", null);
        }

        final DownloadableFileRef referencetoExportResult = result;

        Thread deleteExportFileThread = new Thread("Delete Experiment Course Export File Thread"){

            @Override
            public void run(){

            	final long start = System.currentTimeMillis();

                try {

                    synchronized (this) {
                        //wait 30 minutes and then delete the export file if it still exists
                        wait(1800000);
                    }

                    ExportManager.getInstance().deleteExportFile(referencetoExportResult);

                } catch (InterruptedException e) {
                    logger.warn("An exception occurred while waiting to delete an export file.", e);
                }

                if(metrics != null){
                	metrics.endTrackingRpc("deleteDataCollectionItemCourseThread", start);
                }
            }
        };

        deleteExportFileThread.start();

        return result;
    }

    /**
     * Helper function to construct the export course filename based on the type of published course being exported (lti, experiment, etc.)
     * @param username The name of the user exporting the course.
     * @param dataSetType The type of published course being exported (lti, experiment, etc).
     * @return The filename (without extension) that will be used for the export.
     */
    private String getDataCollectionItemCourseFileName(String username, DataSetType dataSetType) {

        //construct export file name using current time and user information
        LocalDateTime currentDate = LocalDateTime.now();
        String prefix = "unknown";
        if (dataSetType.compareTo(DataSetType.LTI) == 0) {
            prefix = "lti";
        } else if (dataSetType.compareTo(DataSetType.EXPERIMENT) == 0) {
            prefix = "experiment";
        } else if (dataSetType.compareTo(DataSetType.COURSE_DATA) == 0) {
            prefix = "data";
        }

        StringBuilder exportFileName = new StringBuilder();
        exportFileName.append(prefix);
        exportFileName.append("_course_");
        exportFileName.append(currentDate.getYear());
        exportFileName.append("-");
        exportFileName.append(currentDate.getMonthValue());
        exportFileName.append("-");
        exportFileName.append(currentDate.getDayOfMonth());
        exportFileName.append("_");
        exportFileName.append(currentDate.getHour());
        exportFileName.append("-");
        exportFileName.append(currentDate.getMinute());
        exportFileName.append("-");
        exportFileName.append(currentDate.getSecond());
        exportFileName.append("_");
        exportFileName.append(username);

        return exportFileName.toString();
    }

    /**
     * Gets the progress of the data collection item course export being run for the specified user
     *
     * @param username the username of the user to get the progress for
     * @return the progress of the experiment course export
     */
    public ProgressIndicator getExportDataCollectionItemCourseProgress(String username){
        return usernameToCourseExportProgress.get(username);
    }

    /**
     * Cancels the data collection item course export being run for the given user, if one exists
     *
     * @param username the username of the user for whom to cancel the experiment course export
     */
    public void cancelExportDataCollectionItemCourse(String username){

        if(username == null){
            throw new IllegalArgumentException("Username cannot be null");
        }

        ProgressIndicator progress = usernameToCourseExportProgress.get(username);
        progress.setShouldCancel(true);
    }

    /**
     * Exports the raw data being used by the given data collection item
     *
     * @param username the user name of the user that is requesting the export
     * @param experimentId the unique identifier of the experiment
     * @param exportConvertedBinaryLogs true to also export the converted human
     *        readable binary files.
     */
    public DownloadableFileRef exportDataCollectionItemRawData(String username, String experimentId, boolean exportConvertedBinaryLogs) throws URISyntaxException, IllegalArgumentException, DetailedException, FileNotFoundException, SurveyValidationException, LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException, UMSDatabaseException{

        DownloadableFileRef result = null;

        ProgressIndicator progress = new ProgressIndicator(0, "Overall Export");

        usernameToRawDataExportProgress.put(username, progress);

        //construct export file name using current time and user information
        LocalDateTime currentDate = LocalDateTime.now();

        StringBuilder exportFileName = new StringBuilder();
        exportFileName.append("experiment_raw_data");
        exportFileName.append(currentDate.getYear());
        exportFileName.append("-");
        exportFileName.append(currentDate.getMonthValue());
        exportFileName.append("-");
        exportFileName.append(currentDate.getDayOfMonth());
        exportFileName.append("_");
        exportFileName.append(currentDate.getHour());
        exportFileName.append("-");
        exportFileName.append(currentDate.getMinute());
        exportFileName.append("-");
        exportFileName.append(currentDate.getSecond());
        exportFileName.append("_");
        exportFileName.append(username);

        result = ServicesManager.getInstance().getDataCollectionServices().exportDataCollectionItemData(username, exportFileName.toString(), experimentId, exportConvertedBinaryLogs, progress);

        final DownloadableFileRef referencetoExportResult = result;

        Thread deleteExportFileThread = new Thread("Delete Experiment Raw Data Export File Thread"){

            @Override
            public void run(){

            	final long start = System.currentTimeMillis();

                try {

                    synchronized (this) {
                        //wait 30 minutes and then delete the export file if it still exists
                        wait(1800000);
                    }

                    ExportManager.getInstance().deleteExportFile(referencetoExportResult);

                } catch (InterruptedException e) {
                    logger.warn("An exception occurred while waiting to delete an export file.", e);
                }

                if(metrics != null){
                	metrics.endTrackingRpc("deleteDataCollectionItemRawDataThread", start);
                }
            }
        };

        deleteExportFileThread.start();

        return result;
    }

    /**
     * Gets the progress of the experiment raw data export being run for the specified user
     *
     * @param username the username of the user to get the progress for
     * @return the progress of the experiment raw data export
     */
    public ProgressIndicator getExportDataCollectionItemRawDataProgress(String username){
        return usernameToRawDataExportProgress.get(username);
    }

    /**
     * Cancels the experiment raw data export being run for the given user, if one exists
     *
     * @param username the username of the user for whom to cancel the experiment raw data export
     */
    public void cancelExportDataCollectionItemRawData(String username){

        if(username == null){
            throw new IllegalArgumentException("Username cannot be null");
        }

        ProgressIndicator progress = usernameToRawDataExportProgress.get(username);
        progress.setShouldCancel(true);
    }

    /**
     * Calculates the size of a expriment course export using the given information
     *
     * @param username the username of the user for whom the calculation is being done
     * @param experimentId the ID of the experiment
     * @return the predicted size of the export file in MB
     * @throws IllegalArgumentException if any arguments are null or if the list of courses to export is empty
     * @throws IOException if there was a problem determining the size of the course export
     * @throws URISyntaxException if there was a problem build a URI to access the files
     */
    public double getCourseExportSize(String username, String experimentId) throws IllegalArgumentException, IOException, URISyntaxException{

        logger.debug("Starting experiment course export for user: " + username);

        ProgressIndicator progress = new ProgressIndicator(0, "Calculating Size");

        //construct export file name using current time and user information
        LocalDateTime currentDate = LocalDateTime.now();

        StringBuilder exportFileName = new StringBuilder();
        exportFileName.append("experiment_course_");
        exportFileName.append(currentDate.getYear());
        exportFileName.append("-");
        exportFileName.append(currentDate.getMonthValue());
        exportFileName.append("-");
        exportFileName.append(currentDate.getDayOfMonth());
        exportFileName.append("_");
        exportFileName.append(currentDate.getHour());
        exportFileName.append("-");
        exportFileName.append(currentDate.getMinute());
        exportFileName.append("-");
        exportFileName.append(currentDate.getSecond());
        exportFileName.append("_");
        exportFileName.append(username);

        return ServicesManager.getInstance().getDataCollectionServices().getExportDataCollectionItemCourseSize(experimentId, progress);
    }

    /**
     * Calculates the size of a raw data export using the given information
     *
     * @param username the username of the user for whom the calculation is being done
     * @param experimentId the ID of the experiment
     * @return the predicted size of the export file in MB
     * @throws IllegalArgumentException if any arguments are null or if the list of courses to export is empty
     * @throws IOException if there was a problem determining the size of the course export
     * @throws URISyntaxException if there was a problem build a URI to access the files
     */
    public double getRawDataExportSize(String username, String experimentId) throws IllegalArgumentException, IOException, URISyntaxException{

        logger.debug("Starting experiment course export for user: " + username);

        ProgressIndicator progress = new ProgressIndicator(0, "Calculating Size");

        //construct export file name using current time and user information
        LocalDateTime currentDate = LocalDateTime.now();

        StringBuilder exportFileName = new StringBuilder();
        exportFileName.append("experiment_raw_");
        exportFileName.append(currentDate.getYear());
        exportFileName.append("-");
        exportFileName.append(currentDate.getMonthValue());
        exportFileName.append("-");
        exportFileName.append(currentDate.getDayOfMonth());
        exportFileName.append("_");
        exportFileName.append(currentDate.getHour());
        exportFileName.append("-");
        exportFileName.append(currentDate.getMinute());
        exportFileName.append("-");
        exportFileName.append(currentDate.getSecond());
        exportFileName.append("_");
        exportFileName.append(username);

        return ServicesManager.getInstance().getDataCollectionServices().getExportDataCollectionItemDataSize(experimentId, progress);
    }

    /**
     * Gets this class' singleton instance
     *
     * @return the singleton instance
     */
    public static DataCollectionManager getInstance(){

        if(singleton == null){
            singleton = new DataCollectionManager();
        }

        return singleton;
    }

    /**
     * Gets the list of published courses authored by the user with the given username
     *
     * @param username the name of the experiment author
     * @return the list of experiments
     */
    public ArrayList<DataCollectionItem> getDataCollectionItems(String username){

        ArrayList<DataCollectionItem> experiments = new ArrayList<DataCollectionItem>();

        for(DbDataCollection dbExperiment : ServicesManager.getInstance().getDataCollectionServices().getDataCollectionItems(username)){

            try{

                DataCollectionItem experiment = hibernateToGift.convertExperiment(dbExperiment);

                experiments.add(experiment);

            } catch(IllegalArgumentException e){

                if(dbExperiment.getId() != null){
                    logger.warn("An exception occurred while getting the published course '"+dbExperiment.getName()+"' with ID: " + dbExperiment.getId() + " for "+username+".", e);

                } else {
                    logger.warn("An exception occurred while getting the published course '"+dbExperiment.getName()+"' for "+username+".", e);
                }
            }
        }

        return experiments;
    }

    /**
     * Updates an experiment with the given name and description
     *
     * @param username the user updating the experiment
     * @param experimentId the ID of the experiment to update
     * @param name the new name for the experiment
     * @param description the new description for the experiment
     * @return the updated experiment
     */
    public DataCollectionItem updateDataCollectionItem(String username,
            String experimentId, String name, String description){

        return hibernateToGift.convertExperiment(ServicesManager.getInstance().getDataCollectionServices().updateDataCollectionItem(username, experimentId, name, description));
        }

    /**
     * Update the published course permission information for a single user.
     *
     * @param username the user requesting the permission change for another user.  Can't be null.
     * @param experimentId the unique id of this published course in this gift instance.  Can't be null.
     * @param permission the permission information to apply for a user on the published course.  Can't be null.
     * @return the updated information for the published course including updated permissions.
     */
    public DataCollectionItem updateDataCollectionPermissions(String username,
            String experimentId, DataCollectionPermission permission){

        DbDataCollection dbExperiment = ServicesManager.getInstance().getDataCollectionServices().getDataCollectionItem(experimentId);

        if(dbExperiment != null){

            //
            // find and update user permissions
            //
            Set<DbDataCollectionPermission> existingPermissions = dbExperiment.getPermissions();
            if(existingPermissions == null){
                existingPermissions = new HashSet<DbDataCollectionPermission>();
            }

            if(existingPermissions.isEmpty()){
                // add published course author as an owner as an owner is required
                // (published course created before permissions existed and now the user is changing permissions for another user)
                DbDataCollectionPermission ownerPermission = new DbDataCollectionPermission();
                ownerPermission.setDataCollectionId(dbExperiment.getId());
                ownerPermission.setUsername(dbExperiment.getAuthorUsername());
                ownerPermission.setDataCollectionUserRole(DataCollectionUserRole.OWNER);
                existingPermissions.add(ownerPermission);
            }

            // set the incoming user permission change
            DbDataCollectionPermission dbPermission = new DbDataCollectionPermission();
            dbPermission.setDataCollectionId(dbExperiment.getId());
            dbPermission.setUsername(permission.getUsername());
            dbPermission.setDataCollectionUserRole(permission.getDataCollectionUserRole());

            if(existingPermissions.remove(dbPermission)){
                if(logger.isInfoEnabled()){
                    logger.info("Attempting to update existing user permissions for "+permission.getUsername()+" to role "+permission.getDataCollectionUserRole()+" for\n"+dbExperiment);
                }
            }else{
                if(logger.isInfoEnabled()){
                    logger.info("Attempting to add new user permissions for "+permission.getUsername()+" of role "+permission.getDataCollectionUserRole()+" for\n"+dbExperiment);
                }
            }

            existingPermissions.add(dbPermission);

            ServicesManager.getInstance().getDataCollectionServices().updateDataCollectionItem(username, dbExperiment);

            // retrieve the db version of this published course
            DbDataCollection updatedExperiment = ServicesManager.getInstance().getDataCollectionServices().getDataCollectionItem(experimentId);

            return hibernateToGift.convertExperiment(updatedExperiment);

        } else {
            throw new DetailedException(
                    "An attempt was made to update the published course, but the published course could not be found in the database",
                    "Attempted to update published course '" + (experimentId != null ? experimentId : "")+ "', but "
                            + "the published course returned from the database was null.",
                    null);
        }
    }

    /**
     * Gets the experiment with the given experiment ID
     *
     * @param experimentId the ID of the experiment to get
     * @return the experiment
     * @throws DetailedException  if the experiment entry could not be found in the database
     */
    public DataCollectionItem getDataCollectionItem(String experimentId) throws DetailedException {

        DbDataCollection dbExperiment = ServicesManager.getInstance().getDataCollectionServices().getDataCollectionItem(experimentId);

        return hibernateToGift.convertExperiment(dbExperiment);
    }

    /**
     * Sets whether or not the experiment with the given ID is active
     *
     * @param username the user updating the experiment
     * @param experimentId the ID of the experiment to update
     * @param status the new status of the experiment
     * @return the updated experiment
     */
    public DataCollectionItem setDataCollectionItemStatus(String username, String experimentId, ExperimentStatus status){

        DbDataCollection dbExperiment = ServicesManager.getInstance().getDataCollectionServices().getDataCollectionItem(experimentId);

        if(dbExperiment != null){

            if (dbExperiment.getStatus().equals(ExperimentStatus.ENDED)) {
                throw new DetailedException("Unable to update published course status",
                        "An attempt was made to update the published course status, but the published course is already deactivated.", null);
            }
            dbExperiment.setStatus(status);

            ServicesManager.getInstance().getDataCollectionServices().updateDataCollectionItem(username, dbExperiment);

            DbDataCollection updatedExperiment = ServicesManager.getInstance().getDataCollectionServices().getDataCollectionItem(experimentId);

            return hibernateToGift.convertExperiment(updatedExperiment);

        } else {
            throw new DetailedException(
                    "An attempt was made to update the published course, but the published course could not be found in the database",
                    "Attempted to update published course '" + (experimentId != null ? experimentId : "")+ "', but "
                            + "the published course returned from the database was null.",
                    null);
        }
    }

    /**
     * Generates and exports a report for the given experiment
     *
     * @param username the user name of the user that is requesting the export
     * @param experimentId the unique identifier of the experiment
     * @param properties the properties that should be used to control how the report is generated
     */
    public void exportDataCollectionItemReport(final String username, final String experimentId, final ReportProperties properties) throws URISyntaxException, IllegalArgumentException, DetailedException, FileNotFoundException, SurveyValidationException, LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException, UMSDatabaseException{

    	final ProgressIndicator progress = new ProgressIndicator(0, "Overall Export");

        final GenerateReportStatus status = new GenerateReportStatus();
        status.setProgress(progress);

        usernameToReportExportProgress.put(username, status);

        // ideally this would not be set here, and even if it were set here the file extension should be determined
        // by the writer and its implementation
        properties.setFileName("GIFT.Report.csv");

        Thread generateReportThread = new Thread("Generate Experiment Report Thread -"+username){

            @Override
            public void run(){

            	final long start = System.currentTimeMillis();

                try{

                    final DownloadableFileRef result = ServicesManager.getInstance().getDataCollectionServices().generateReport(username, experimentId, progress, properties);

                    status.setReportResult(result);

                    Thread deleteExportFileThread = new Thread("Delete Experiment Report Export File Thread"){

                        @Override
                        public void run(){

                        	final long deleteStart = System.currentTimeMillis();

                            try {

                                synchronized (this) {
                                    //wait 30 minutes and then delete the export file if it still exists
                                    wait(1800000);
                                }

                                usernameToReportExportProgress.remove(username);

                                ExportManager.getInstance().deleteExportFile(result);

                            } catch (InterruptedException e) {
                                logger.warn("An exception occurred while waiting to delete an export file.", e);
                            }

                            if(metrics != null){
                            	metrics.endTrackingRpc("deleteDataCollectionItemReportThread", deleteStart);
                            }
                        }
                    };

                    deleteExportFileThread.start();

                } catch(Throwable e){

                    if(e instanceof DetailedException){
                        status.setException((DetailedException)e);
                    } else {
                        status.setException(new DetailedException(
                                "An problem occurred while generating the published course report.",
                                e.toString(),
                                e));
                    }
                }

                if(metrics != null){
                	metrics.endTrackingRpc("exportDataCollectionItemReportThread", start);
                }

            }
        };

        generateReportThread.start();

        return;
    }

    /**
     * Gets the progress of the experiment report export being run for the specified user
     *
     * @param username the username of the user to get the progress for
     * @return the progress of the experiment report export
     */
    public GenerateReportStatus getExportDataCollectionItemReportProgress(String username){

        GenerateReportStatus status = usernameToReportExportProgress.get(username);

        if(status != null && status.getReportResult() != null){

            //need to remove status mapping once the report is done so that the client doesn't get outdated status info later
            usernameToReportExportProgress.remove(username);
        }

        return status;
    }

    /**
     * Cancels the experiment report export being run for the given user, if one exists
     *
     * @param username the username of the user for whom to cancel the experiment report export
     */
    public void cancelExportDataCollectionItemReport(String username){

        if(username == null){
            throw new IllegalArgumentException("Username cannot be null");
        }

        GenerateReportStatus status = usernameToReportExportProgress.get(username);

        if(status != null){

            ProgressIndicator progress = status.getProgress();

            if(progress != null){
                progress.setShouldCancel(true);
            }
        }
    }

    /**
     * Creates and adds a specified {@link DataCollectionItem} to a specified
     * course collection.
     *
     * @param username The name of the user who is performing the action. Used
     *        for authentication purposes.
     * @param collectionId The id of the course collection to which the
     *        {@link DataCollectionItem} should be added.
     * @param newCourse The description of the {@link DataCollectionItem} that
     *        should be added to the course collection. Should not yet exist in
     *        the database.
     * @return The newly created {@link DataCollectionItem} that was added to
     *         the specified course collection as it exists in the database.
     */
    public DataCollectionItem addCourseToCollection(String username, String collectionId, DataCollectionItem newCourse) {
        if (logger.isTraceEnabled()) {
            List<Object> params = Arrays.<Object>asList(username, collectionId, newCourse);
            logger.trace("addCourseToCollection(" + StringUtils.join(", ", params) + ")");
        }

        if (isBlank(username)) {
            throw new IllegalArgumentException("The parameter 'username' cannot be blank.");
        } else if (isBlank(collectionId)) {
            throw new IllegalArgumentException("The parameter 'collectionId' cannot be blank.");
        } else if (newCourse == null) {
            throw new IllegalArgumentException("The parameter 'newCourse' cannot be null.");
        }

        DataCollectionItem createdDcItem = null;
        try {
            createdDcItem = createDataCollectionItem(
                    newCourse.getName(),
                    newCourse.getDescription(),
                    username,
                    newCourse.getSourceCourseId(),
                    newCourse.getDataSetType());
        } catch (Throwable t) {
            final String errorMsg = "There was a problem creating the course " + newCourse + ".";
            logger.error(errorMsg, t);
            throw new DetailedException(
                    "Unable to add the course to the collection because there was a problem creating the course.",
                    errorMsg, t);
        }

        try {
            UMSDatabaseManager.getInstance().addCourseToCollection(username, collectionId, createdDcItem, null);
        } catch (Throwable t1) {
            try {
                final String errorMsg1 = String.format(
                        "Error while adding course '%s' to collection '%s'. Attempting rollback by deleting the course.",
                        createdDcItem.getId(), collectionId);
                logger.error(errorMsg1, t1);
                deleteDataCollectionItem(username, createdDcItem.getId());
            } catch (Throwable t2) {
                final String errorMsg2 = String.format(
                        "There was an issue deleting '%s' as part of the rollback of its addition of '%s'",
                        createdDcItem.getId(), collectionId);
                logger.error(errorMsg2, t2);
                return null;

            }
        }

        return createdDcItem;
    }

    /**
     * Changes the order of two courses within a course collection.
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
     */
    public void reorderCourseInCollection(String username, String collectionId, int oldIndex, int newIndex, List<DataCollectionItem> currentOrdering) {
        if (logger.isTraceEnabled()) {
            List<Object> params = Arrays.<Object>asList(username, collectionId, oldIndex, newIndex);
            logger.trace("reorderCourseInCollection(" + StringUtils.join(", ", params) + ")");
        }

        
        UMSDatabaseManager.getInstance().reorderCourseCollection(
                username, 
                collectionId, 
                oldIndex, 
                newIndex, 
                currentOrdering, 
                null);            
    }

    /**
     * Updates the metadata for a specified {@link DbCourseCollection}.
     *
     * @param username The name of the user who is requesting the action. Used
     *        for authentication purposes. Can't be blank.
     * @param collectionId The id of the collection whose metadata is being
     *        updated. Can't be blank.
     * @param name The new name to apply to the {@link DbCourseCollection}.
     *        Can't be blank.
     * @param description The new description to apply to the
     *        {@link DbCourseCollection}. Can't be null to indicate the description should be deleted.
     */
    public void editCourseCollectionMetadata(String username, String collectionId, String name, String description) {
        if (logger.isTraceEnabled()) {
            List<Object> params = Arrays.<Object>asList(username, collectionId, name, description);
            logger.trace("editCourseCollectionMetadata(" + StringUtils.join(", ", params) + ")");
        }

        /* Validate the parameters */
        if (isBlank(username)) {
            throw new IllegalArgumentException("The parameter 'username' cannot be blank.");
        } else if (isBlank(collectionId)) {
            throw new IllegalArgumentException("The parameter 'collectionId' cannot be blank.");
        } else if (isBlank(name)) {
            throw new IllegalArgumentException("The parameter 'name' cannot be blank.");
        }

        final UMSDatabaseManager dbMgr = UMSDatabaseManager.getInstance();
        final Session session = dbMgr.getCurrentSession();
        session.beginTransaction();

        try {
            DbCourseCollection dbCollection = dbMgr.selectRowById(collectionId, DbCourseCollection.class, session);

            /* Check the permissions for this action */
            final boolean isUserOwner = StringUtils.equals(dbCollection.getOwner(), username);
            final boolean isUserManager = dbMgr.getCourseCollectionPermissionForUser(username,
                    dbCollection) == DataCollectionUserRole.MANAGER;
            if (!isUserOwner && !isUserManager) {
                String string = String.format(
                        "The user '%s' does not have permission to edit the properties of collection '%s'",
                        username,
                        collectionId);
                throw new UnsupportedOperationException(string);
            }

            dbCollection.setName(name);
            dbCollection.setDescription(description);
            dbMgr.updateRow(dbCollection, session);

            session.getTransaction().commit();
        } catch (Throwable thrown) {
            String errorMsg = "There was a problem editing the metadata for the collection: " + collectionId;
            logger.error(errorMsg, thrown);
            if (session.isOpen()) {
                session.getTransaction().rollback();
            }

            throw new DetailedException(
                    "There was a problem editing the metadata of a course collection.",
                    errorMsg,
                    thrown);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Enables logging metrics via the given metrics sender. If no metrics sender is provided, metrics will not be logged by this instance.
     *
     * @param metrics the metrics sender to use to log metrics
     */
    public void setMetricsSender(MetricsSender metrics){
    	this.metrics = metrics;
    }
}
