/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.CourseRecord;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.course.CourseFileValidationException;
import mil.arl.gift.common.course.MetadataFileValidationException;
import mil.arl.gift.common.course.PedagogyFileValidationException;
import mil.arl.gift.common.course.SensorFileValidationException;
import mil.arl.gift.common.course.dkf.DKFValidationException;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.common.experiment.ExperimentUtil.DataCollectionUserRole;
import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DownloadableFileRef;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.ProhibitedUserException;
import mil.arl.gift.domain.DomainCourseFileHandler.CourseOptionsWrapper;
import mil.arl.gift.lms.impl.common.LmsInvalidCourseRecordException;
import mil.arl.gift.lms.impl.common.LmsInvalidStudentIdException;
import mil.arl.gift.lms.impl.common.LmsIoException;
import mil.arl.gift.tools.services.ServicesProperties;
import mil.arl.gift.ums.db.UMSDatabaseException;
import mil.arl.gift.ums.db.survey.SurveyValidationException;
import mil.arl.gift.ums.db.table.DbDataCollection;

/**
 * This is the interface for data collection services.
 * 
 * @author mhoffman
 *
 */
public interface DataCollectionServicesInterface {
    
    static final int GATHERED_EXPERIMENT_DATA = 20;
    
    static final int SUBJECT_DATA_DELETED = 40;
    static final int EXPERIMENT_COURSE_DELETED = 40;
    static final int TOTAL_PROGRESS = 100;
    
    /** desktop directory where experiment, prior to July 2019, course folders are copied to (after that they stay in authored workspace folder) */
    static final File EXPERIMENT_DIRECTORY = new File(ServicesProperties.getInstance().getExperimentsDirectory());
    
    /** desktop directory where experiment data exports will go */
    static final File EXPORT_DIRECTORY = new File(ServicesProperties.getInstance().getExportDirectory());
    
    /** desktop directory where message log files (e.g. domain session messages) are placed */
    static final String DOMAIN_SESSIONS_LOG_DIR = PackageUtil.getDomainSessions();

    /**
     * Create a new experiment for the course provided.
     * 
     * @param experimentName the name of the experiment.  Doesn't have to be unique.  Can't be null.
     * @param experimentDescription information about the experiment.  Can be null.
     * @param authorUsername the user name of the user that is creating the experiment
     * @param courseId the unique identifier of the course the experiment will use. This is the relative path to the course.xml 
     * and the key in the courseOptionsWrapper.domainOptions map.
     * @param dataSetType the data collection data set type to be created.
     * @param courseOptionsWrapper contains information about all courses available to the user and is used to retrieve 
     * the course identified for the experiment
     * @param forceCourseValidation whether to force the course validation check to occur.  This will ignore the optimization logic 
     * that checks the last successful course validation date against the last modified date on the course folder to determine if 
     * course validation is needed.
     * @param forceSurveyValidation whether to force the course's surveys to be checked.  This will ignore the optimization logic
     * that checks the last successful course validation date against the last modified date on the survey context to determine if 
     * survey validation is needed.
     * @param progressIndicator used to update progress on the creation of the experiment. Can be null.
     * @return the new instance of the experiment in the database.  Contains the generated experiment id as well as the URL for users
     * to run the experiment.
     * @throws IllegalArgumentException if there was a problem with an argument to the method
     * @throws DetailedException if there was a problem creating the experiment
     * @throws URISyntaxException if there was a problem with retrieving the course from the server
     */
    public DbDataCollection createDataCollectionItem(String experimentName, String experimentDescription, String authorUsername, String courseId, 
            DataSetType dataSetType, CourseOptionsWrapper courseOptionsWrapper, boolean forceCourseValidation, boolean forceSurveyValidation, 
            final ProgressIndicator progressIndicator) throws IllegalArgumentException, DetailedException, URISyntaxException;
    
    /**
     * Create a new 'course tile' (course data) published course db entry if one doesn't already exists for the specified course.
     * A published course should exist for every course created in order to expose the publish course report generation logic.
     * @param username the owner of the new published course.  This should be the course owner however in Desktop mode
     * that is a wildcard (Public) owner and therefore it will be the user making the first request.
     * @param courseRecord contains information about the course that is needed to populate the publish course entry
     * w/o asking the user for more information.
     */
    public void createDefaultDataCollectionItemIfNeeded(String username, CourseRecord courseRecord);
    
    /**
     * Deletes an existing experiment and all associated subject data.
     * 
     * @param username the user trying to delete the experiment for permissions checks
     * @param experimentId the id of the experiment to delete.  It must exist already.
     * @param progressIndicator used to update progress on deleting the experiment and subject's data
     * @throws IllegalArgumentException if there was a problem with an argument to the method
     * @throws DetailedException if there was a problem deleting the experiment
     */
    public void deleteDataCollectionItem(String username, String experimentId, ProgressIndicator progressIndicator) throws IllegalArgumentException, DetailedException;
    
    /**
     * Retrieve the list of existing experiments the user has access too.
     * 
     * @param username the username to use to lookup existing experiments
     * @return collection of experiments accessible by this user.  Won't be null but can be empty.
     */
    public Set<DbDataCollection> getDataCollectionItems(String username);
    
    /**
     * Retrieve the latest representation of a specific experiment in the database.
     * 
     * @param experimentId the unique identifier of an experiment to retrieve
     * @return the database representation of that experiment entry
     * @throws DetailedException if the experiment entry could not be found in the database
     */
    public DbDataCollection getDataCollectionItem(String experimentId) throws DetailedException;
    
    /**
     * Update the attributes of an existing experiment
     * For example the user may have changed the name of the experiment or the description.  In addition the user
     * may have changed the status of the experiment (e.g. paused).
     * 
     * @param username the username of the user updated the data collection item, used for permission checking
     * @param experiment the experiment database entry to update
     * @return whether the data collection item was updated in the database
     * @throws DetailedException if there was a problem updating the existing experiment, including that the experiment could not be found.
     */
    public boolean updateDataCollectionItem(String username, DbDataCollection experiment) throws DetailedException;
    
    /**
     * Export the experiment's data by gathering and zipping the subject's data.
     * 
     * @param username the user trying to delete the experiment for permissions checks
     * @param filename the name of the zip file to create (don't include the .zip extension)
     * @param experimentId the unique identifier of an experiment to gather subject's data for
     * @param exportConvertedBinaryLogs true to also export the converted human
     *        readable binary files.
     * @param progressIndicator used to show progress of zipping the experiment data
     * @return information about the exported data, more importantly the URL for downloading the zip
     * @throws DetailedException if there was a problem with the subject's data or creating the zip
     */
    public DownloadableFileRef exportDataCollectionItemData(String username, String filename, String experimentId, boolean exportConvertedBinaryLogs, ProgressIndicator progressIndicator) throws DetailedException;
    
    /**
     * Return the total MegaBytes of the data for all the subject's in the specified experiment.
     * 
     * @param experimentId the unique identifier of an experiment to gather subject's data for and calculate a size in MB
     * @param progressIndicator used to show progress of zipping the experiment data
     * @return the number of MegaBytes the subjects data takes before being compressed in a zip
     * @throws DetailedException if there was a problem with the subject's data
     */
    public float getExportDataCollectionItemDataSize(String experimentId, ProgressIndicator progressIndicator) throws DetailedException;
    
    /**
     * Export the experiment's course by performing a course export.
     * 
     * @param username the user trying to delete the experiment for permissions checks
     * @param filename the name of the zip file to create (don't include the .zip extension)
     * @param experimentId the unique identifier of an experiment to export the course folder
     * @param progressIndicator used to show progress of exporting the course folder
     * @return information about the export progress, more importantly the URL for downloading the zip
     * @throws DetailedException if there was a problem with the export
     */
    public DownloadableFileRef exportDataCollectionItemCourse(String username, String filename, String experimentId, ProgressIndicator progressIndicator) throws DetailedException;
    
    /**
     * Return the total MegaBytes of the data for the experiment course export.
     * 
     * @param experimentId the unique identifier of an experiment to export the course folder for and calculate a size in MB
     * @param progressIndicator used to show progress of exporting
     * @return the number of MegaBytes the experiment course folder export takes before being compressed in a zip
     * @throws DetailedException if there was a problem with the export
     */
    public float getExportDataCollectionItemCourseSize(String experimentId, ProgressIndicator progressIndicator) throws DetailedException;
    
    /**
     * This will determine if the experiment's course folder is currently valid.
     * 
     * @param experimentId the unique identifier of the experiment to check if it's course folder is currently valid
     * @throws FileValidationException if there was an issue validating (against the schema) the file
     * @throws DKFValidationException  if there was a problem validating a DKF (including DKFs referenced by course and training app reference files)
     * @throws CourseFileValidationException  if there was a problem validating a course file
     * @throws ConfigurationException  if there was a general problem not specific to a file
     * @throws SensorFileValidationException  if there was a problem validating a sensor configuration file
     * @throws PedagogyFileValidationException if there was a problem validating a pedagogical configuration file
     * @throws MetadataFileValidationException  if there was a problem validating a metadata file (including referenced by course files)
     * @throws ProhibitedUserException if the username is on the prohibited list, meaning the username can't be used in this gift instance (e.g. Public).
     */
    public void validateDataCollectionItemCourse(String username, String experimentId) throws FileValidationException, DKFValidationException, 
        CourseFileValidationException, ConfigurationException, SensorFileValidationException, PedagogyFileValidationException, 
        MetadataFileValidationException, IllegalArgumentException, FileNotFoundException, SurveyValidationException, 
        LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException, DetailedException, UMSDatabaseException, ProhibitedUserException;

    /**
     * Run an ERT report using the properties provided.
     * 
     * @param username the user trying to delete the experiment for permissions checks
     * @param experimentId the unique identifier of the experiment to generate a report for
     * @param progressIndicator used to show progress of generating the experiment report
     * @param expReportProperties contains information about the report to generated (e.g. column to merge by, column to sort by)
     * @return information about the report generated, more importantly the URL for downloading the file
     * @throws DetailedException if there was a severe problem with generating the report
     */
    public DownloadableFileRef generateReport(String username, String experimentId, ProgressIndicator progressIndicator, final ReportProperties reportProperties) throws DetailedException;
    
    /**
     * Determines if the course has any data collection database rows (i.e. is this course a published course).  
     * 
     * @param username The username making the request. Not currently used.
     * @param coursePath The course relative path (domain id) to the course. e.g. Public/Hemorrhage Control Lesson/HemorrhageControl.course.xml
     * @param ignoreTypes optional list of data set types to ignore when performing this check.  This is useful if you don't care
     * about the automatically created 'course data/tile' published course type as an example.
     * @return True if there are any published courses that belong to the course.
     */
    public boolean doesCourseHaveDataCollectionDataSets(String username, String coursePath, DataSetType ...ignoreTypes);
    
    /**
     * Ends ALL data collection data sets for the course.  A data collection that has ended
     * means that users are no longer to take the course for the data set, and that no new
     * results will come in for it.
     * 
     * @param username The username making the request.
     * @param courseId The id of the course (from the course db table).
     */
    public void endDataCollectionDataSets(String username, String courseId);
    
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
    public boolean checkDataCollectionForPermissions(DbDataCollection dbExperiment, String username, DataCollectionUserRole... requiredPermissions);
    
    /**
     * Return the published courses (Data Collection objects) that reference the specific source course folder and of the specific data set type.
     * @param courseFolder the workspace path to the course folder that is taken by learners.  Can't be null or empty. e.g. "Public/Hello World"
     * @param dataSetType the enumerated type of publish course to search for.  Can't be null.
     * @return the published courses found.  Can be empty but won't be null.
     * @throws DetailedException if there was a problem retrieving the published courses.
     */
    public List<DbDataCollection> getPublishedCoursesOfType(final String courseFolder, final DataSetType dataSetType) throws DetailedException;

    /**
     * Updates an experiment with the given name and description
     *
     * @param username the user updating the experiment
     * @param experimentId the ID of the experiment to update. e.g. 906e8784-c55e-4882-8068-dd55a1f6a9c6
     * @param name the new name for the experiment
     * @param description the new description for the experiment
     * @return the updated experiment.  Won't be null.
     */
    public DbDataCollection updateDataCollectionItem(String username,
            String experimentId, String name, String description);
}
