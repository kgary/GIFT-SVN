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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.CourseRecord;
import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.course.CourseFileValidationException;
import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.course.MetadataFileValidationException;
import mil.arl.gift.common.course.PedagogyFileValidationException;
import mil.arl.gift.common.course.SensorFileValidationException;
import mil.arl.gift.common.course.dkf.DKFValidationException;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.EventType;
import mil.arl.gift.common.ert.GenerateReportStatus;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.common.ert.event.AbstractEvent;
import mil.arl.gift.common.ert.event.BranchPathHistoryAggregate;
import mil.arl.gift.common.ert.event.BranchPathHistoryEvent;
import mil.arl.gift.common.ert.event.DomainSessionEvent;
import mil.arl.gift.common.ert.event.MidLessonAssessmenRemediationAgreggate;
import mil.arl.gift.common.ert.event.ParticipantAttemptCnt;
import mil.arl.gift.common.ert.event.PerformanceAssessmentEvent;
import mil.arl.gift.common.ert.event.TimeOnTaskAggregate;
import mil.arl.gift.common.ert.server.AbstractEventSourceParser;
import mil.arl.gift.common.ert.server.EventSourceUtil;
import mil.arl.gift.common.ert.server.MessageLogEventSourceParser;
import mil.arl.gift.common.ert.server.ReportGenerationUtil;
import mil.arl.gift.common.ert.server.ReportWriter;
import mil.arl.gift.common.experiment.ExperimentUtil.DataCollectionUserRole;
import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.experiment.ExperimentUtil.ExperimentStatus;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DownloadableFileRef;
import mil.arl.gift.common.io.ExportProperties;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.FileUtil.DeleteProgressData;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.ProhibitedUserException;
import mil.arl.gift.common.io.ZipUtils;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.logger.ProtobufMessageLogReader;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.domain.DomainCourseFileHandler.CourseOptionsWrapper;
import mil.arl.gift.lms.impl.common.LmsInvalidCourseRecordException;
import mil.arl.gift.lms.impl.common.LmsInvalidStudentIdException;
import mil.arl.gift.lms.impl.common.LmsIoException;
import mil.arl.gift.tools.export.ExportCourseUtil;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.db.DbServicesInterface;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import mil.arl.gift.ums.db.UMSDatabaseException;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.survey.SurveyValidationException;
import mil.arl.gift.ums.db.table.DbDataCollection;
import mil.arl.gift.ums.db.table.DbDataCollectionPermission;
import mil.arl.gift.ums.db.table.DbDataCollectionResultsLti;
import mil.arl.gift.ums.db.table.DbExperimentSubject;

/**
 * This class provides logic for managing data collection items in GIFT.
 *
 * @author mhoffman
 *
 */
public class DataCollectionServices implements DataCollectionServicesInterface {

    // progress indicator constants -- based on two phases of roughly identical work
    // a certain amount is predefined in the interface, so the rest of the progress is split here
    static final int GATHERED_COLUMN_DATA_PROGRESS = (100 - GATHERED_EXPERIMENT_DATA)/2;
    static final int REPORT_CREATED_PROGRESS = 100;

    private static final long MAX_PARTICIPANT_DATA_SIZE_MB = 1024;
    private static final long MAX_PARTICIPANT_DATA_SIZE_BYTES = MAX_PARTICIPANT_DATA_SIZE_MB * 1024 * 1024;

    /** gathering events task description for progress indicator */
    private static final String GATHERING_DATA_TASK_DESC = "Gathering data";

    /** adding events for a data set task description for progress indicator */
    private static final String ADDING_EVENTS_TASK_DESC = "Adding events for data set. . .";

    /** adding events to report task description for progress indicator */
    private static final String ADDING_EVENTS_TO_REPORT_TASK_DESC = "Adding events to report. . .";

    /** testing writing to the report file task description for progress indicator */
    private static final String TESTING_WRITER_TASK_DESC = "Testing writer";

    /** deleting participant data task description for progress indicator */
    private static final String DELETING_PARTICIPANT_DATA_TASK_DESC = "Deleting participant data";

    /** deleting the course task description for progress indicator */
    private static final String DELETING_COURSE_COPY_TASK_DESC = "Deleting copy of course";

    /** deleting database records task description for progress indicator */
    private static final String DELETING_DB_ENTRIES_TASK_DESC = "Deleting database entries";

    /** zipping data for export task description for progress indicator */
    private static final String ZIPPING_DATA_TASK_DESC = "Zipping data";

    /** creating download URL task description for progress indicator */
    private static final String BUILDING_DOWNLOAD_URL_TASK_DESC = "Building download URL";

    /** starting report creation task description for progress indicator */
    private static final String STARTING_REPORT_GENERATION_TASK_DESC = "Starting event report generation";

    /** parsing log files task description for progress indicator */
    private static final String PARSING_DATA_SET_TASK_DESC = "Parsing data set results to identify events...";

    /** initializing report generation process task description for progress indicator */
    private static final String INIT_GENERATE_REPORT = "Initializing...";

    /** calculating participant size task description for progress indicator */
    private static final String DETERMINE_PARTICIPANT_DATA_SIZE_GENERATE_REPORT = "Determing total participant data file size";

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DataCollectionServices.class);

    /**
     * The desktop folder proxy of the experiment desktop directory (e.g. Domain/Experiments)
     * Will be null if {@link #getLegacyExperimentFolderProxy()} has not been called yet.<br/>
     * Pre-July 2019 (#4077) - experiments were placed in Domain/Experiments/<br/>
     * Post - experiments reference authored workspace course folders (e.g. Domain/workspaces/<username>/<course folder>/ [Desktop mode])
     */
    private static DesktopFolderProxy legacyExperimentFolderProxy = null;
        
    /**
     * Return the experiment desktop directory for this GIFT instance.  This directory contains
     * experiment course folders and doesn't include the Imports, Exports, templates or runtime directories.
     *
     * @return the desktop folder proxy of the experiment desktop directory (e.g. Domain/Experiments)<br/>
     * Pre-July 2019 (#4077) - experiments were placed in Domain/Experiments/<br/>
     * Post - experiments reference authored workspace course folders (e.g. Domain/workspaces/<username>/<course folder>/ [Desktop mode])
     */
    @SuppressWarnings("unused")
    private DesktopFolderProxy getLegacyExperimentFolderProxy(){

        if(legacyExperimentFolderProxy == null){
            //this logic is here so if there is an exception the caller will be notified each method call
            legacyExperimentFolderProxy = new DesktopFolderProxy(EXPERIMENT_DIRECTORY);
        }

        return legacyExperimentFolderProxy;
    }

    @Override
    public void createDefaultDataCollectionItemIfNeeded(String username, CourseRecord courseRecord){
        UMSDatabaseManager.getInstance().createDefaultDataCollectionItemIfNeeded(username, courseRecord);
    }

    @Override
    public DbDataCollection createDataCollectionItem(String experimentName, String experimentDescription, String authorUsername, String courseId,
            DataSetType dataSetType,
            CourseOptionsWrapper courseOptionsWrapper,
            boolean forceCourseValidation,
            boolean forceSurveyValidation,
            ProgressIndicator progressIndicator)
            throws IllegalArgumentException, DetailedException,
            URISyntaxException {

        //
        // validate course
        //
        DomainOption courseData = courseOptionsWrapper.domainOptions.get(courseId);

        if(courseData == null){
            throw new IllegalArgumentException("Unable to find the course information in the provided CourseOptionsWrapper.  This needs to be prepopulated with the parsed course information.");
        }

        try {
            boolean validateCourse = forceCourseValidation;
            if(!forceCourseValidation){
                //whether the course folder has changed since the last successful validation
                validateCourse = ServicesManager.getInstance().getFileServices().hasCourseChangedSinceLastValidation(courseData.getDomainId(), authorUsername);

            }

            boolean validateSurvey = forceSurveyValidation;
            if(!forceSurveyValidation){
                //only check if the default validation logic is requested (i.e. null params) or survey validation is allowed but not forced [if forced there would be no need to check this timestamp]
                Date lastSuccessfulValidation = ServicesManager.getInstance().getFileServices().getCourseLastSuccessfulValidationDate(courseData.getDomainId(), authorUsername);
                validateSurvey = ServicesManager.getInstance().getDbServices().hasSurveyContextChangeSinceLastValidation(lastSuccessfulValidation, courseData.getSurveyContextId(), authorUsername);

            }

            //only validate if warranted, otherwise schema validation has already happened when the DomainOption class was created
            if(validateCourse || validateSurvey){
                ServicesManager.getInstance().getFileServices().getCourse(authorUsername, courseData.getDomainId(), courseOptionsWrapper,
                        validateCourse,
                        validateSurvey,
                        true,
                        progressIndicator);

                courseData = courseOptionsWrapper.domainOptions.get(courseId);
            }

        }catch(Throwable t){
            throw new DetailedException("Failed to create the "+dataSetType.toString().toLowerCase()+" named '"+experimentName+"' because the course failed to pass GIFT validation.",
                    "There was an error while validating the course named '"+courseData.getDomainName()+"'.", t);
        }

        if(courseData.getDomainOptionRecommendation() != null && courseData.getDomainOptionRecommendation().getDomainOptionRecommendationEnum().isUnavailableType()){
            throw new DetailedException("Failed to create the "+dataSetType.toString().toLowerCase()+" named '"+experimentName+"' because the course failed to pass GIFT validation.",
                    "The following issue(s) were found with the course named '"+courseData.getDomainName()+"'.\n\n"+
                    "<b>Reason:</b> "+courseData.getDomainOptionRecommendation().getReason()+"\n"+
                    "<b>Details:</b> "+courseData.getDomainOptionRecommendation().getDetails(), null);
        }

        AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
        FileTreeModel courseTreeModel = fileServices.getFileTreeByPath(authorUsername, courseId);
        String courseFolder = courseTreeModel.getParentTreeModel().getRelativePathFromRoot();
        
        // only one published course can exist per course when using course data type, this is because
        // the data set is domain session logs for the course not for a published course object like the GIFT Experiment type.
        // Therefore if more than one published course course data type was allowed, what would be the difference among them?
        if(DataSetType.COURSE_DATA.equals(dataSetType)){
            
            if(!UMSDatabaseManager.getInstance().getPublishedCoursesOfType(courseFolder, dataSetType, true, null).isEmpty()){
                throw new DetailedException("There is already a 'Course Tile' Published Course for the '"+experimentName+"' course.", 
                        "Only one 'course tile' published course is allowed per course because the data collected corresponds to anyone that takes this course rather than a unique URL (e.g. Experiments).", 
                        null);
            }
        }
        
        // make sure a course record exists in case the course was manually copied into the GIFT workspace folder, bypassing
        // UI logic.
        CourseRecord course = ServicesManager.getInstance().getDbServices().createCourseRecordIfNeeded(authorUsername,
                courseId, true);

        DbDataCollection dbExperiment = null;
        if(DataSetType.COURSE_DATA.equals(dataSetType)){
            // check if a 'course tile' published course exists for this course.  All courses should be published
            // this way.  It it already exists, don't create a new db row

            List<DbDataCollection> publishedCourses = getPublishedCoursesOfType(courseFolder, DataSetType.COURSE_DATA);
            if(!publishedCourses.isEmpty()){
                dbExperiment = publishedCourses.get(0);
            }
        }

        if(dbExperiment == null){
        //
            // create new experiment table entry
        //
        try{
            dbExperiment = UMSDatabaseManager.getInstance().createExperiment(experimentName, experimentDescription, authorUsername, dataSetType, null);
        }catch(Exception e){
            throw new DetailedException("Failed to create the data set entry in the database.", "An error occurred while trying to create the data set entry for '"+experimentName+"' : "+e.getMessage(), e);
        }

            dbExperiment.setCourseFolder(courseFolder);

        if (course != null) {
            dbExperiment.setSourceCourseId(course.getCourseId());
        } else {
            throw new DetailedException("Failed to create the data set entry in the database.",
                    "An error occurred while creating the course record for course: " + courseId, null);
        }

        //apply updates to db
        UMSDatabaseManager.getInstance().updateExperimentProperties(authorUsername, dbExperiment, null);
        }


        return dbExperiment;
    }

    @Override
    public void deleteDataCollectionItem(String username, String experimentId, ProgressIndicator progressIndicator)
            throws IllegalArgumentException, DetailedException {

        // we want to eager load subjects, but lazy load LTI results because we are only using subjects in this method.
        DbDataCollection dbExperiment = UMSDatabaseManager.getInstance().getExperiment(experimentId, true, false, null);
        if(dbExperiment == null){
            throw new DetailedException("Failed to delete the data set.", "Failed to find the data set with id '"+experimentId+"'.", null);
        }

        if(logger.isInfoEnabled()){
            logger.info("Deleting data set : "+dbExperiment);
        }

        // check permissions
        boolean canDelete = checkDataCollectionForPermissions(dbExperiment, username, DataCollectionUserRole.OWNER);

        if(!canDelete){
            throw new DetailedException("Failed to delete data set", "The user '"+username+"' doesn't have permissions to delete this data set with id '"+experimentId+"'.", null);
        }


        if (dbExperiment.isDataSetType(DataSetType.EXPERIMENT)) {
            //
            // delete subject's data (i.e. message log files)
            //
            progressIndicator.setTaskDescription(DELETING_PARTICIPANT_DATA_TASK_DESC);
            try{
                int cnt = 1;
                for(DbExperimentSubject subject : dbExperiment.getSubjects()){

                    String filename = subject.getMessageLogFilename();
                    File logFile = new File(DOMAIN_SESSIONS_LOG_DIR + File.separator + filename);
                    logFile.delete();
                    progressIndicator.setPercentComplete(cnt / dbExperiment.getSubjects().size() * SUBJECT_DATA_DELETED);
                    cnt++;
                }
            }catch(Exception e){
                throw new DetailedException("Failed to fully delete the data set named '"+dbExperiment.getName()+"'.", "There was an error while deleting participant's data : "+e.getMessage(), e);
            }

            if(logger.isInfoEnabled()){
                logger.info("Finished deleting participant's data.  Starting to delete the data set's copy of the course folder. "+dbExperiment);
            }

            progressIndicator.setPercentComplete(SUBJECT_DATA_DELETED);

            //
            // delete experiment course folder if legacy
            //
            if (StringUtils.isBlank(dbExperiment.getSourceCourseId())) {
                progressIndicator.setTaskDescription(DELETING_COURSE_COPY_TASK_DESC);
            try{
                String courseFoldername = dbExperiment.getCourseFolder();

                if(courseFoldername == null || courseFoldername.isEmpty()){
                     throw new IllegalArgumentException("The experiment course folder cannot be null or empty.");
                }

                File courseFolder = new File(EXPERIMENT_DIRECTORY + File.separator + courseFoldername).getParentFile();

                //make sure that the Experiment Directory or any ancestor directory isn't deleted
                String relativePath = FileFinderUtil.getRelativePath(EXPERIMENT_DIRECTORY, courseFolder);
                if(relativePath.startsWith("..")){
                    //ERROR
                    throw new IllegalArgumentException("The data set course folder of '"+courseFoldername+"' is not a descendant folder of the Experiments directory.");
                }

                if(courseFolder.exists()){
                    int descendantFileCnt = FileUtil.countFilesInDirectory(courseFolder);
                    if(descendantFileCnt == 0){
                        //in case the experiment folder exists but it has no children
                        descendantFileCnt = 1;
                    }

                    DeleteProgressData deleteProgressData = new DeleteProgressData(descendantFileCnt, EXPERIMENT_COURSE_DELETED, SUBJECT_DATA_DELETED);
                    FileUtil.deleteDirectory(courseFolder, deleteProgressData, progressIndicator);
                }else{
                    logger.warn("Failed to delete the data set "+experimentId+" because the data set course folder of '"+courseFolder+"' wasn't found.  Maybe it was deleted in a previous attempt to delete the data set that failed.");
                }
            }catch(Exception e){
                throw new DetailedException("Failed to fully delete the data set named '"+dbExperiment.getName()+"'.", "The participant's data was deleted already, however there was an error while deleteing the data set's copy of the course folder : "+e.getMessage(), e);
            }

            if(logger.isInfoEnabled()){
                    logger.info("Finished deleting the data set's copy of the course.");
            }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Starting to delete the database entries for the data set. " + dbExperiment);
        }

        progressIndicator.increasePercentComplete(EXPERIMENT_COURSE_DELETED);

        //delete experiment and subject db entries
        try{
            progressIndicator.setTaskDescription(DELETING_DB_ENTRIES_TASK_DESC);
            UMSDatabaseManager.getInstance().deleteExperiment(dbExperiment.getId(), null);
        }catch(Exception e){
            throw new DetailedException("Failed to delete the data set entry for '"+dbExperiment.getName()+"' in the database.", "An error occurred while trying to delete the data set entry for:\n"+dbExperiment+"\nreason: "+e.getMessage(), e);
        }

        progressIndicator.setPercentComplete(TOTAL_PROGRESS);

        if(logger.isInfoEnabled()){
            logger.info("Finished deleting : "+dbExperiment);
        }
    }

    @Override
    public Set<DbDataCollection> getDataCollectionItems(String username) {
        return UMSDatabaseManager.getInstance().getExperiments(username, false, false);
    }

    @Override
    public boolean updateDataCollectionItem(String username, DbDataCollection experiment) throws DetailedException{
        return UMSDatabaseManager.getInstance().updateExperimentProperties(username, experiment, null);
    }

    @Override
    public DbDataCollection updateDataCollectionItem(String username,
            String experimentId, String name, String description){
        
        DbDataCollection dbExperiment = ServicesManager.getInstance().getDataCollectionServices().getDataCollectionItem(experimentId);

        if(dbExperiment != null){

            dbExperiment.setName(name);
            dbExperiment.setDescription(description);

            updateDataCollectionItem(username, dbExperiment);

            DbDataCollection updatedExperiment = getDataCollectionItem(experimentId);

            return updatedExperiment;

        } else {
            throw new DetailedException(
                    "An attempt was made to update the published course, but the published course could not be found in the database",
                    "Attempted to update published course '" + (experimentId != null ? experimentId : "")+ "', but "
                            + "the published course returned from the database was null.",
                    null);
        }    
    }

    @Override
    public DownloadableFileRef exportDataCollectionItemData(String username, String filename, String experimentId, boolean exportConvertedBinaryLogs, ProgressIndicator progressIndicator)
            throws DetailedException {

        // we want to eager load subjects and LTI results because we are using both collections in this method.
        DbDataCollection experiment = UMSDatabaseManager.getInstance().getExperiment(experimentId, true, true, null);
        if(experiment == null){
            throw new DetailedException("Failed to export data set results.", "Failed to find the data set with id '"+experimentId+"'.", null);
        }

        if(logger.isInfoEnabled()){
            logger.info("Exporting data set results for "+experiment+" to '"+filename+"'.");
        }

        // check permissions
        boolean canExport = checkDataCollectionForPermissions(experiment, username, DataCollectionUserRole.OWNER, DataCollectionUserRole.MANAGER);

        if(!canExport){
            throw new DetailedException("Failed to export data set results", "The user '"+username+"' doesn't have permissions to export the data set results for this data set with id '"+experimentId+"'.", null);
        }

        progressIndicator.setTaskDescription(GATHERING_DATA_TASK_DESC);

        List<File> logs = new ArrayList<>();
        if (experiment.isDataSetType(DataSetType.EXPERIMENT)) {
            //gather domain session logs
            List<DbExperimentSubject> subjects = new ArrayList<>(experiment.getSubjects());
            if(subjects.isEmpty()){
                throw new DetailedException("Unable to export results for the 'Experiment' data set '"+experiment.getName()+"'.", "No participants have started the course yet, therefore there is no data to export.", null);
            }

            logs = getSubjectData(subjects, progressIndicator, GATHERED_EXPERIMENT_DATA, false);
        } else if(experiment.isDataSetType(DataSetType.LTI)){

            //gather domain session logs
            List<DbDataCollectionResultsLti> results = new ArrayList<>(experiment.getDataCollectionResultsLti());
            if(results.isEmpty()){
                throw new DetailedException("Unable to export results for the 'LTI' data set '"+experiment.getName()+"'.", "No participants have started the course yet, therefore there is no data to export.", null);
            }
            logs = this.getDataCollectionResultsLti(results, experiment.getName(), progressIndicator, GATHERED_EXPERIMENT_DATA, false);
        } else if(experiment.isDataSetType(DataSetType.COURSE_DATA)){
            
            //gather domain session logs
            List<DbExperimentSubject> subjects = new ArrayList<>(experiment.getSubjects());
            if(subjects.isEmpty()){
                throw new DetailedException("Unable to export results for the 'Course Tile' data set '"+experiment.getName()+"'.", "No participants have started the course yet, therefore there is no data to export.", null);
        }

            logs = getSubjectData(subjects, progressIndicator, GATHERED_EXPERIMENT_DATA, false);
        }

        /* Convert the protobuf binary logs to human readable and add them to
         * the export log list. */
        if (exportConvertedBinaryLogs) {
            ListIterator<File> logItr = logs.listIterator();
            while (logItr.hasNext()) {
                final File logFile = logItr.next();
                /* If the log file is a directory, then the whole directory will
                 * be zipped, don't need to add any converted files directly to
                 * the list. */
                if (logFile.isDirectory()) {
                    convertProtoBinLogDir(logFile);
                } else {
                    /* If the log file is a file, then the converted file needs
                     * to be directly added to the list. */
                    final File outputFile = convertProtoBinLogFile(logFile);
                    if (outputFile != null) {
                        logItr.add(outputFile);
                    }
                }
            }
            
        }

        progressIndicator.setPercentComplete(GATHERED_EXPERIMENT_DATA);

        //create zip where the export contents will be placed
        File exportZipFile = new File(EXPORT_DIRECTORY.getAbsolutePath() + File.separator + filename + Constants.ZIP);
        exportZipFile.deleteOnExit();

        try{
            progressIndicator.setTaskDescription(ZIPPING_DATA_TASK_DESC);

            //zip logs
            ZipUtils.zipFiles(logs, exportZipFile);

            /* Delete any converted logs now that zipping is complete */
            if (exportConvertedBinaryLogs) {
                for (File file : logs) {
                    removeConvertedProtoBinLogs(file);
                }
            }

            progressIndicator.setPercentComplete(TOTAL_PROGRESS);
            progressIndicator.setTaskDescription(BUILDING_DOWNLOAD_URL_TASK_DESC);

            //build download URL
            URL exportURL = ServicesManager.getExportURL(exportZipFile);

            if(logger.isInfoEnabled()){
                logger.info("Finished exporting data set results ("+logs.size()+" log files) for "+experiment+" to '"+exportZipFile+"'.");
            }

            return new DownloadableFileRef(exportURL.toString(), exportZipFile.getPath());
        }catch(IllegalArgumentException | IOException e){

            exportZipFile.delete();
            throw new DetailedException("Failed to export results for the data set '"+experiment.getName()+"'.",
                    "There was an error while zipping the data set results :"+e.getMessage(), e);
        }
    }

    /**
     * Convert protobuf binary logs in the given directory to human readable.
     * This will create new files and put them in the same directory as the
     * original log.
     * 
     * @param file the directory containing the binary log files to convert.
     */
    private void convertProtoBinLogDir(File file) {
        if (!file.isDirectory()) {
            logger.error("The file '" + file + "' must be a directory.");
            return;
        } else if (!file.exists()) {
            logger.error("The directory '" + file + "' must exist.");
            return;
        }

        for (File childFile : file.listFiles()) {
            if (childFile.isDirectory()) {
                convertProtoBinLogDir(childFile);
            } else {
                convertProtoBinLogFile(childFile);
            }
        }
    }

    /**
     * Convert a protobuf binary log to human readable. This will create a new
     * file and put it in the same directory as the log.
     * 
     * @param file the binary log file to convert.
     * @return the converted log file. Can be null if the given file is a
     *         directory, if it doesn't exist, or if it isn't a protobuf binary
     *         log file.
     */
    private File convertProtoBinLogFile(File file) {
        if (file.isDirectory()) {
            logger.error("The file '" + file + "' must not be a directory.");
            return null;
        } else if (!file.exists()) {
            logger.error("The file '" + file + "' must exist.");
            return null;
        } else if (!ProtobufMessageLogReader.isProtobufLogFile(file.getName())) {
            return null;
        }

        try {
            ProtobufMessageLogReader logReader = new ProtobufMessageLogReader();
            final File outputFile = new File(file.getParentFile(),
                    ProtobufMessageLogReader.removeProtobufLogFileExtension(file)
                            + ProtobufMessageLogReader.CONVERTED_LOG_FILE_EXTENSION);
            logReader.writeLogAsJSON(file, outputFile);
            return outputFile;
        } catch (Exception e) {
            logger.warn("Unable to convert the protobuf binary log '" + file + "' to human readable.", e);
            return null;
        }
    }
    

    /**
     * Removes the converted binary protobuf log file.
     * 
     * @param file the file that is being checked
     */
    private void removeConvertedProtoBinLogs(File file) {
        if (!file.isDirectory()) {
            if (file.exists() && ProtobufMessageLogReader.isConvertedProtobufLogFile(file)) {
            file.delete();
            }
            return;
        }

        for (File childFile : file.listFiles()) {
            removeConvertedProtoBinLogs(childFile);
        }
    }

    /**
     * Return the list of domain session folders or the files within containing the experiment data for the subjects listed.
     *
     * @param subjects the subjects to retrieve experiment data for
     * @param progressIndicator used to show progress for the calling overall task
     * @param percentOfOverallTask the percentage retrieving the subject data is for the overall task that called this method
     * @param noDirectories whether to return the list of files found in the domain session folders (true) or the domain session folders (false).
     * @return the list of domain session folders or files containing the experiment data for the subjects.  Can be empty if there are no subjects, but not null.
     * Note: if a domain session log file (or folder) was not found an error is logged but the retrieval continues without throwing an exception.
     */
    private List<File> getSubjectData(List<DbExperimentSubject> subjects, ProgressIndicator progressIndicator, 
            int percentOfOverallTask, boolean noDirectories) {

        List<File> logs = new ArrayList<>();
        int cnt = 1;
        for(DbExperimentSubject subject : subjects){

            String logFilename = subject.getMessageLogFilename();

            //in rare cases when the participant didn't actually reach the first
            //course object, the log file name can be null
            if(logFilename == null){
                continue;
            }

            File logFile = new File(DOMAIN_SESSIONS_LOG_DIR + File.separator + logFilename);
            if (logFile.exists()) {
                // add the domain session folder
                logs.add(noDirectories ? logFile : logFile.getParentFile());
            } else {
                // don't prevent the export of subject data if a log file is missing
                logger.error(
                        "Could not find log file for the participant:\n" + "id = "
                                + subject.getExperimentSubjectId().getSubjectId() + ",\n" + "log file name = "
                                + subject.getMessageLogFilename() + ".",
                        new DetailedException(
                                "Unable to export data for the data set '"
                                        + subject.getExperimentSubjectId().getExperiment().getName() + "'.",
                                "Failed to find the log file for the participant:\n" + "id = "
                                        + subject.getExperimentSubjectId().getSubjectId() + ",\n" + "log file name = "
                                        + subject.getMessageLogFilename() + ".",
                                null));
            }

            progressIndicator.setPercentComplete(percentOfOverallTask*cnt/subjects.size());
            cnt++;
        }

        return logs;
    }

    /**
     * Gets the data collection results for an experiment that is a data set type of 'lti'.
     *
     * @param results The data collection result set for the lti users.
     * @param experimentName The name of the experiment.
     * @param progressIndicator The progress indicator to be used.
     * @param percentOfOverallTask The percent of overall task completion.
     * @param noDirectories whether to return the list of files found in the domain session folders (true) or the domain session folders (false).
     * @return The list of files found for the data collection results.
     * Note: if a domain session log file (or folder) was not found an error is logged but the retrieval continues without throwing an exception.
     */
    private List<File> getDataCollectionResultsLti(List<DbDataCollectionResultsLti> results, String experimentName, 
            ProgressIndicator progressIndicator, int percentOfOverallTask, boolean noDirectories){

        List<File> logs = new ArrayList<>();
        int cnt = 1;
        for(DbDataCollectionResultsLti result : results){

            String logFilename = result.getMessageLogFileName();
            File logFile = new File(DOMAIN_SESSIONS_LOG_DIR + File.separator + logFilename);
            if (logFile.exists()) {
                logs.add(noDirectories ? logFile : logFile.getParentFile());
            } else {
                // don't prevent the export of subject data if a log file is missing
                logger.error("Could not find log file '" + result.getMessageLogFileName() + "'.",
                        new DetailedException("Unable to export data for the data set '" + result + "'.",
                                "Failed to find the log file for the participant:\n" + "id = " + result.getConsumerId()
                                        + ",\n" + "log file name = " + result.getMessageLogFileName() + ".",
                                null));
            }

            progressIndicator.setPercentComplete(percentOfOverallTask*cnt/results.size());
            cnt++;
        }

        return logs;
    }

    @Override
    public float getExportDataCollectionItemDataSize(String experimentId, ProgressIndicator progressIndicator)
            throws DetailedException {

        // we want to eager load subjects, but lazy load LTI results because we are only using subjects in this method.
        DbDataCollection experiment = UMSDatabaseManager.getInstance().getExperiment(experimentId, true, false, null);
        if(experiment == null){
            throw new DetailedException("Failed to calculate data set export size.", "Failed to find the data set with id '"+experimentId+"'.", null);
        }

        if(logger.isInfoEnabled()){
            logger.info("Calculating data set result size for "+experiment);
        }

        //gather domain session logs
        List<DbExperimentSubject> subjects = new ArrayList<>(experiment.getSubjects());
        if(subjects.isEmpty()){
            throw new DetailedException("Unable to export results for the data set '"+experiment.getName()+"'.", "No participants have started the course yet, therefore there is no data to export.", null);
        }

        progressIndicator.setTaskDescription(GATHERING_DATA_TASK_DESC);

        float fileSizeBytes = 0;
        int cnt = 1;
        for(DbExperimentSubject subject : subjects){

            String logFilename = subject.getMessageLogFilename();
            File logFile = new File(DOMAIN_SESSIONS_LOG_DIR + File.separator + logFilename);
            progressIndicator.setPercentComplete(cnt/subjects.size() * GATHERED_EXPERIMENT_DATA);
            cnt++;

            if(logFile.exists()){
                // no longer bailing if the log file doesn't exist.  
                // This accompanies the recent logic in getSubjectData to continue if a log is missing.
            fileSizeBytes += FileUtil.getSize(logFile);
        }

        }

        progressIndicator.setPercentComplete(TOTAL_PROGRESS);
        float fileSizeMB = FileUtil.byteToMb(fileSizeBytes);
        if(logger.isInfoEnabled()){
            logger.info("Calculated experiment data size of "+fileSizeMB+" MB for "+experiment);
        }

        return fileSizeMB;
    }

    @Override
    public DownloadableFileRef exportDataCollectionItemCourse(String username, String filename,
            String experimentId, ProgressIndicator progressIndicator)
            throws DetailedException {

        // we want to lazy load subjects and LTI results because we aren't using either collection in this method.
        DbDataCollection experiment = UMSDatabaseManager.getInstance().getExperiment(experimentId, false, false, null);
        if(experiment == null){
            throw new DetailedException("Failed to export data set course.", "Failed to find the data set with id '"+experimentId+"'.", null);
        }

        // check permissions
        boolean canExport = checkDataCollectionForPermissions(experiment, username, DataCollectionUserRole.OWNER, DataCollectionUserRole.MANAGER);

        if(!canExport){
            throw new DetailedException("Failed to export data set course", "The user '"+username+"' doesn't have permissions to export the data set course for this data set with id '"+experimentId+"'.", null);
        }

        //create zip where the export contents will be placed
        File exportZipFile = new File(EXPORT_DIRECTORY.getAbsolutePath() + File.separator + filename + Constants.ZIP);
        exportZipFile.deleteOnExit();

        //get the course folder instance
        // Pre-July 2019 (#4077) - look in Domain/Experiments/
        // Post - look in Domain/workspaces/ [Desktop mode]
        List<AbstractFolderProxy> courseFolders = new ArrayList<>();
        AbstractFolderProxy courseFolderProxy = null;
        
        // first check older published course
        File courseFolder = new File(EXPERIMENT_DIRECTORY.getAbsolutePath() + File.separator + experiment.getCourseFolder());
        try {
            courseFolderProxy = new DesktopFolderProxy(courseFolder);
        } catch (@SuppressWarnings("unused") IllegalArgumentException oldCheckException) {
            // probably not an older published course, see if its a newer made published course location, i.e. the authored workspaces directory 
            
            try{
                courseFolderProxy = ServicesManager.getInstance().getFileServices().getCourseFolder(experiment.getCourseFolder(), username);
            }catch(IOException | DetailedException newCheckException){
                throw new DetailedException("Failed to retrieve the course folder for the data set named '"+experiment.getName()+"' from either the deprecated location or the authored workspace location.",
                        "There was a problem retrieving the data set course folder from '"+courseFolder+"' OR '"+experiment.getCourseFolder()+"' (workspace folder).", 
                        newCheckException);
            }
        }
        
        // if reaches here than won't be null because of throwing exception above
        courseFolders.add(courseFolderProxy);

        //build export properties assuming that only the course folder and resources and needed and NO user data should be exported
        List<DomainOption> domainOptions = new ArrayList<>();
        domainOptions.add(new DomainOption(experiment.getName(), experiment.getCourseFolder(), null, username));
        ExportProperties exportProperties = new ExportProperties(experiment.getAuthorUsername(), domainOptions, filename, progressIndicator);

        //export the course folder
        ExportCourseUtil exportCourseUtil = new ExportCourseUtil();
        try{
            exportCourseUtil.export(courseFolders, exportProperties, exportZipFile);

            URL exportURL = ServicesManager.getExportURL(exportZipFile);

            return new DownloadableFileRef(exportURL.toString(), exportZipFile.getPath());
        }catch(IOException e){

            exportZipFile.delete();
            throw new DetailedException("Failed to export course folder for the data set named '"+experiment.getName()+"'.",
                    "There was an error while exporting course :"+e.getMessage(), e);
        }
    }

    @Override
    public float getExportDataCollectionItemCourseSize(String experimentId,
            ProgressIndicator progressIndicator) throws DetailedException {
        // we want to lazy load subjects and LTI results because we aren't using either collection in this method.
        DbDataCollection experiment = UMSDatabaseManager.getInstance().getExperiment(experimentId, false, false, null);
        if(experiment == null){
            throw new DetailedException("Failed to calculate the data set course folder size .", "Failed to find the data set with id '"+experimentId+"'.", null);
        }

        //get the course folder instance
        List<AbstractFolderProxy> courseFolders = new ArrayList<>();
        try{
            File courseFolder = new File(EXPERIMENT_DIRECTORY.getAbsolutePath() + File.separator + experiment.getCourseFolder());
            AbstractFolderProxy courseFolderProxy = new DesktopFolderProxy(courseFolder);
            courseFolders.add(courseFolderProxy);
        }catch(IllegalArgumentException e){
            throw new DetailedException("Failed to retrieve the course folder for the data set named '"+experiment.getName()+"'.",
                    "There was a problem retrieving the data set course folder of '"+experiment.getCourseFolder()+"' : "+e.getMessage(), e);
        }

        //calculate the export size
        ExportCourseUtil exportCourseUtil = new ExportCourseUtil();
        try{
            return exportCourseUtil.getCourseFoldersSize(courseFolders);
        }catch(IOException e){
            throw new DetailedException("Failed to calculate the course export file size.",
                    "There was a problem while calculating the file size of the course export : "+e.getMessage(), e);
        }
    }

    @Override
    public void validateDataCollectionItemCourse(String username, String experimentId) throws FileValidationException, DKFValidationException,
            CourseFileValidationException, ConfigurationException, SensorFileValidationException, PedagogyFileValidationException,
            MetadataFileValidationException, IllegalArgumentException, FileNotFoundException, SurveyValidationException,
            LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException, DetailedException, UMSDatabaseException, ProhibitedUserException {
        // we want to lazy load subjects and LTI results because we aren't using either collection in this method.
        DbDataCollection experiment = UMSDatabaseManager.getInstance().getExperiment(experimentId, false, false, null);
        if(experiment == null){
            throw new DetailedException("Failed to validate data set course.", "Failed to find the data set with id '"+experimentId+"'.", null);
        }

        // Lti and Experiment courses can be on desktop or in Nuxeo (server mode).  The normal course validation logic is applied here
        // since the courses for the lti type of data set exist in the original course location.
        AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
        DbServicesInterface dbServices = ServicesManager.getInstance().getDbServices();
        CourseRecord courseRecord = dbServices.getCourseById(experiment.getSourceCourseId(), false);

        if (courseRecord == null) {
            throw new DetailedException("Failed to validate the data set course.", "Failed to find the course associated with the data set.", null);
        }

        //
        // First retrieve the course w/o GIFT validation (schema validation happens though)
        //
        CourseOptionsWrapper wrapper = new CourseOptionsWrapper();
        fileServices.getCourse(username, courseRecord.getCoursePath(), wrapper, false, false, true, null);
        if (wrapper.parseFailedFiles != null && !wrapper.parseFailedFiles.isEmpty()) {

            // Build an informative message showing which files failed to validate.
            StringBuilder sb = new StringBuilder();
            sb.append("The following files in the course failed to validate: <br/>");
            sb.append("<ul>");
            for (String fileName : wrapper.parseFailedFiles) {
                sb.append("<li>").append(fileName).append("</li><br/>");
            }
            sb.append("</ul><br/>");
            sb.append("Please use the Course Creator tool to resolve the validation issues in the course.");

            throw new FileValidationException("There was an error parsing one or more files in the course.",
                    sb.toString(), courseRecord.getCoursePath(), null);
        }

        //
        // Second validate based on modification dates found
        //
        DomainCourseFileHandler courseHandler = wrapper.courseFileNameToHandler.get(courseRecord.getCoursePath());

        Date lastSuccessful = DomainCourseFileHandler.getLastSuccessfulValidation(courseHandler.getCourse());

        //whether the course folder has changed since the last successful course validation
        boolean hasCourseChanged = true;
        if(lastSuccessful != null){
            hasCourseChanged = ServicesManager.getInstance().getFileServices().hasCourseChangedSinceLastValidation(courseHandler.getCourse(), courseHandler.getCourseFolder(), username);
        }

        //whether the course folder has changed since the last change to the survey context
        boolean hasSurveyContextChanged = false;
        if(courseHandler.getCourse().getSurveyContext() != null){
            hasSurveyContextChanged = ServicesManager.getInstance().getDbServices().hasSurveyContextChangeSinceLastValidation(lastSuccessful, courseHandler.getCourse().getSurveyContext().intValue(), experiment.getAuthorUsername());
        }

        // check the course (enable the validation flags to force validation of the file).
        fileServices.getCourse(username, courseRecord.getCoursePath(), wrapper, hasCourseChanged, hasSurveyContextChanged, true, null);

        DomainOption domainOption = wrapper.domainOptions.get(courseRecord.getCoursePath());
        if(domainOption != null && domainOption.getDomainOptionRecommendation() != null){

            CourseValidationResults results = domainOption.getDomainOptionRecommendation().getCourseValidationResulst();
            if(results != null && (results.hasCriticalIssue() || results.hasImportantIssues())){
                throw new CourseFileValidationException("There was an issue during course validation.", "One or more issues where reported during validation of this experiment.", courseRecord.getCoursePath(), results.getFirstError());
            }
        }
    }

    @Override
    public DbDataCollection getDataCollectionItem(String experimentId) throws DetailedException {
        // we want to eager load subjects and LTI results because we want to return the full DbDataCollection from this method
        DbDataCollection experiment = UMSDatabaseManager.getInstance().getExperiment(experimentId, true, true, null);
        if(experiment == null){
            throw new DetailedException("Failed to find the published course.", "Failed to find the published course with id '"+experimentId+"'.  Has it been deleted?", null);
        }

        return experiment;
    }


    /**
     * Generates a report given an experimentId and reportProperties describing the type of report
     *  NOTE: report properties does not have columns added by the UI in the style of the old ERT, thus
     *        this method requires two passes through the log files - once to determine columns and once to create the report
     *  NOTE: sourceIDs in reportProperties is a dummy value and ignored
     *
     * @param experitmentId unique identifier for the experiment; used to select log files
     * @param progressIndicator used to provide visual feedback for the user. If it has a subtaskProcessIndicator that will also be used
     * @param reportProperties describes the report to produce; must be enhanced after identifying columns found in the log files
     * @return a DownloadableFileRef contains the URL of the report that can be used to download the report off of the server
     * @throws DetailedException for out of memory or if the parsing throws an error
     */
    @Override
    public DownloadableFileRef generateReport(String username, String experimentId, ProgressIndicator progressIndicator, ReportProperties reportProperties)
            throws DetailedException {

        // ensure the progress indicator has a subtask process indicator so it can be used without checking for existence
        ProgressIndicator subtaskProgressIndicator = progressIndicator.getSubtaskProcessIndicator();
        if (subtaskProgressIndicator == null) {
            subtaskProgressIndicator = new ProgressIndicator(INIT_GENERATE_REPORT);
            progressIndicator.setSubtaskProgressIndicator(subtaskProgressIndicator);
        }

        // we want to eager load subjects and LTI results because we are using both collections in this method.
        DbDataCollection experiment = UMSDatabaseManager.getInstance().getExperiment(experimentId, true, true, null);
        if(experiment == null){
            throw new DetailedException("Failed to export data set report.", "Failed to find the data set with id '"+experimentId+"'.", null);
        }

        // check permissions
        Set<DbDataCollectionPermission> permissions = experiment.getPermissions();
        boolean canRunReports = false;
        if(permissions != null){
            for(DbDataCollectionPermission permission : permissions){

                if(StringUtils.equalsIgnoreCase(permission.getUsername(), username)){
                    //every user role can run reports
                    canRunReports = true;
                    break;
                }
            }
        }else{
            //this could be a data collection item created before permissions were added

            if(StringUtils.equalsIgnoreCase(experiment.getAuthorUsername(), username)){
                canRunReports = true;
            }
        }

        if(!canRunReports){
            throw new DetailedException("Failed to generated report", "The user '"+username+"' doesn't have permissions to run reports on this data set.", null);
        }

        //gather domain session logs
        List<File> logs = new ArrayList<>();
        if (experiment.isDataSetType(DataSetType.EXPERIMENT)) {
            Set<DbExperimentSubject> subjects = experiment.getSubjects();
            if(subjects.isEmpty()){
                throw new DetailedException("Unable to export the report for the 'Experiment' data set '"+experiment.getName()+"'.", "No participants have started the course yet, therefore there is no data to export.", null);
            }

            if(logger.isInfoEnabled()){
                logger.info("Gathering data set participant data for '"+experiment+"'.");
            }

            //get all existing subject log files from db
            progressIndicator.setTaskDescription(GATHERING_DATA_TASK_DESC);

            logs = getSubjectData(new ArrayList<>(subjects), progressIndicator, GATHERED_EXPERIMENT_DATA, true);
        } else if(experiment.isDataSetType(DataSetType.LTI)){
            Set<DbDataCollectionResultsLti> ltiResult = experiment.getDataCollectionResultsLti();
            if(ltiResult.isEmpty()){
                throw new DetailedException("Unable to export the report for the 'LTI' data set '"+experiment.getName()+"'.", "No participants have started the course yet, therefore there is no data to export.", null);
            }

            if(logger.isInfoEnabled()){
                logger.info("Gathering data set participant data for '"+experiment+"'.");
            }

            //get all existing subject log files from db
            progressIndicator.setTaskDescription(GATHERING_DATA_TASK_DESC);

            logs = getDataCollectionResultsLti(new ArrayList<>(ltiResult), experiment.getName(), progressIndicator, GATHERED_EXPERIMENT_DATA, true);
        }else if(experiment.isDataSetType(DataSetType.COURSE_DATA)){
            Set<DbExperimentSubject> subjects = experiment.getSubjects();
            if(subjects.isEmpty()){
                throw new DetailedException("Unable to export the report for the 'Course Tile' data set '"+experiment.getName()+"'.", "No participants have started the course yet, therefore there is no data to export.", null);
        }

            if(logger.isInfoEnabled()){
                logger.info("Gathering data set participant data for '"+experiment+"'.");
            }

            //get all existing subject log files from db
            progressIndicator.setTaskDescription(GATHERING_DATA_TASK_DESC);

            logs = getSubjectData(new ArrayList<>(subjects), progressIndicator, GATHERED_EXPERIMENT_DATA, true);
        }

        progressIndicator.setPercentComplete(GATHERED_EXPERIMENT_DATA);

        // check total file size
        if(logs != null && CommonProperties.getInstance().isServerDeploymentMode()){
            progressIndicator.setTaskDescription(DETERMINE_PARTICIPANT_DATA_SIZE_GENERATE_REPORT);
            long totalFilesSizeBytes = 0;
            for(File file : logs){

                try{
                    totalFilesSizeBytes += file.length();
                }catch(@SuppressWarnings("unused") Exception e){
                    //ignore, this is best effort
                }

                if(totalFilesSizeBytes > MAX_PARTICIPANT_DATA_SIZE_BYTES){
                    throw new DetailedException("Failed to create report for the data set named '"+experiment.getName()+"'.",
                            "The total file size for the participant data is over "+MAX_PARTICIPANT_DATA_SIZE_MB+" MB.  Currently GIFT is unable to handle a request of this size while still maintaining a useable server for everyone else. You will need to:<br/>"+
                    "1. download your raw participant data for your published course by using the 'Export Raw Data' button.<br/>"+
                    "2. download, install and run a local instance of GIFT on your computer.  Visit https://gifttutoring.org/projects/gift/files to download the latest GIFT instance.<br/>"+
                    "3. use the ERT to analyze your data.  The ERT can be accessed by starting the control panel (GIFT/scripts/launchControlPanel.bat)", null);
                }
            }
        }

        // create a new reportProperties that includes the columns found in the log files
        reportProperties = addReportPropertiesColumns(username, experiment, reportProperties, logs, progressIndicator, GATHERED_EXPERIMENT_DATA, GATHERED_COLUMN_DATA_PROGRESS);

        // create the report
        // Note: the new ERT logic passes the list of files to the report generator, rather than getting them from sourceIDs in reportProperties
        String filename=null;
        filename = generateReportImplementation(experiment, reportProperties, progressIndicator, GATHERED_COLUMN_DATA_PROGRESS, REPORT_CREATED_PROGRESS, logs);

        // create a DownloadableFileRef in the export directory from the filename returned
        File reportFile = new File(EXPORT_DIRECTORY.getAbsolutePath() + File.separator + filename);
        reportFile.deleteOnExit();

        progressIndicator.setPercentComplete(100);
        try{
            URL exportURL = ServicesManager.getExportURL(reportFile);
            return new DownloadableFileRef(exportURL.toString(), reportFile.getPath());
        }catch(IOException e){
           reportFile.delete();
           logger.error("An io error occurred creating a downloadable fileref for data set " +experiment.getName()+"'. the messsage was " + e.getMessage());
           throw new DetailedException("Failed to create report for the data set named '"+experiment.getName()+"'.",
                    "There was an error while constructing the URL for the report to be downloaded from.  The error message reads:\n"+e.getMessage(), e);
        }
    }

    /**
     * generate a report based on the reportProperties and log files passed in.  Report progress to user through progressIndicator
     *
     * @param reportProperties the incoming ReportProperties describing the events and columns for the report
     * @param progressIndicator used to provide visual feedback for the user (has a non-null subtask PI
     * @param progressStart integer 0-100 representing where the progressIndicator should start
     * @param progressEnd integer 0-100 representing where the progressIndicator should end when method completes
     * @param logs list of files to process looking for matching events and columns for those events
     * @return filename for the generated report
     * @throws DetailedException for file, IO, and parsing errors
     */
    private String generateReportImplementation(DbDataCollection experiment, final ReportProperties reportProperties, ProgressIndicator progressIndicator, int progressStart, int progressEnd, List<File> logs) throws DetailedException {

        progressIndicator.setPercentComplete(progressStart);
        progressIndicator.setTaskDescription(STARTING_REPORT_GENERATION_TASK_DESC);

        //
        //Create csv writer with column header information
        //
        if(logger.isInfoEnabled()){
            logger.info("Creating report for data set " +experiment+"'.");
        }

        List<EventReportColumn> reportColumns = reportProperties.getReportColumns();
        reportColumns.removeAll(Collections.singletonList(null));  //remove any null objects in the collection

        final ReportWriter writer = new ReportWriter(reportProperties, true);
        writer.setEmptyCellValue(reportProperties.getEmptyCellValue());
        writer.setWriteLocation(EXPORT_DIRECTORY.getAbsolutePath());

        GenerateReportStatus reportStatus = new GenerateReportStatus(progressIndicator.getSubtaskProcessIndicator());
        writer.setProgressIndicator(reportStatus);

        //
        // Test writer - attempt to detect some issues early, before waiting on report contents to be gathered
        //
        if(logger.isInfoEnabled()){
            logger.info("Testing writer before attempting to build report for file " + reportProperties.getFileName());
        }
        progressIndicator.setTaskDescription(TESTING_WRITER_TASK_DESC);
        try {
            writer.writeTest();
            if(logger.isInfoEnabled()){
                logger.info("Finished testing writer for file " + reportProperties.getFileName());
            }
        } catch (Exception e) {
            logger.error("Caught exception while testing writer for file " + reportProperties.getFileName(), e);
            throw new DetailedException("Unable to write report",
                    "There was an exception while testing whether the report file could be created. The error message reads:\n"+e.getMessage()+
                            ".\n\nThe most common cause of this error is that files can't be created in the directory "+EXPORT_DIRECTORY.getAbsolutePath()+". Do you know if the user account running GIFT has permissions to write files in that directory?." , e);
        }


        try{
            //
            //Add events for each source to writer
            //

            // used to keep track of attempts for each participant
            Map<Integer, ParticipantAttemptCnt> participantToAttemptCnt = new HashMap<>();
            boolean participantAttemptCounted;

            progressIndicator.setTaskDescription(ADDING_EVENTS_TASK_DESC);
            int numFiles = logs.size();
            int currentFileNum = 0;
            ProgressIndicator subtaskProgressIndicator = progressIndicator.getSubtaskProcessIndicator();
            subtaskProgressIndicator.setTaskDescription(ADDING_EVENTS_TO_REPORT_TASK_DESC);
            
            //Key: User ID for each unique Domain Session Event
            //Value: List of Domain Session IDs that are used by User ID for each unique Domain Session Event
            HashMap<Integer, List<Integer>> userDomainSessionEventsMap = new HashMap<Integer, List<Integer>>();
            boolean dsEventFound = false;
            
            if(reportProperties.isColumnEnabled(EventReportColumn.COURSE_ATTEMPT_COL)) {
                for(File file : logs) {
                	dsEventFound = false;
                    AbstractEventSourceParser parser = EventSourceUtil.getEventParser(file, reportProperties);
                    for (EventType eventType : reportProperties.getEventTypeOptions()) {
                        List<AbstractEvent> events = parser.getEventsByType(eventType);
                        if(events != null) {
                            for (AbstractEvent event : events) {
                                if(event instanceof DomainSessionEvent){
                                    DomainSessionEvent dsEvent = (DomainSessionEvent) event;
                                    dsEventFound = true;
                                    if(userDomainSessionEventsMap.containsKey(dsEvent.getUserId())){
                                        List<Integer> domainSessionIdsList = userDomainSessionEventsMap.get(dsEvent.getUserId());
                                        if(!domainSessionIdsList.contains(dsEvent.getDomainSessionId())) {
                                            domainSessionIdsList.add(dsEvent.getDomainSessionId());
                                        }
                                    } else {
                                        List<Integer> emptyIdsList = new ArrayList<Integer>();
                                        emptyIdsList.add(dsEvent.getDomainSessionId());
                                        userDomainSessionEventsMap.put(dsEvent.getUserId(), emptyIdsList);
                                    }
                                }
                                if(dsEventFound) {
                                	break;
                                }
                            }
                        }
                        if(dsEventFound) {
                        	break;
                        }
                    }
                }
                
                // Why Sort?
                // Since domain session log files can be read in any order, the domain session ids list for each user
                // might not be in numerical order. Domain Sessions ids are unique and are always increasing by one for 
                // each new domain session in a single GIFT instance.  By sorting each list, the attempt number for
                // each domain session is determined by the sorted index in the list.
                for(Map.Entry<Integer, List<Integer>> domainSessionEvent : userDomainSessionEventsMap.entrySet()) {
                    Collections.sort(domainSessionEvent.getValue());
                }
            }
            
            for(File file : logs) {

                if(logger.isInfoEnabled()){
                    logger.info("Adding events for file named "+file.getName()+" to writer.");
                }

                AbstractEventSourceParser parser = EventSourceUtil.getEventParser(file, reportProperties);

                if(parser == null){
                    logger.error("Unable to find an event parser for file named "+file.getName() +
                            "This can happen if there is no logic to parse that particular file type or the format of the file is not what GIFT is expecting.");
                    throw new DetailedException("Unable to parse data set results", "Unable to find an event parser for file named "+file.getName()
                            +". This can happen if there is no logic to parse that particular file type or the format of the file is not what GIFT is expecting.", null);
                }

                participantAttemptCounted = false;

                //
                // Search for participant id
                //
                Integer participantId = parser.getParticipantId();
                
                // capture lesson started events for elapsed DKF time
                List<AbstractEvent> lessonStartedEvents = null;
                
                // capture lesson completed events for elapsed DKF time
                List<AbstractEvent> lessonCompletedEvents = null;
                
                if(parser instanceof MessageLogEventSourceParser){
                    
                    MessageLogEventSourceParser messageParser = (MessageLogEventSourceParser)parser;
                    EventType lessonStartedEventType = messageParser.getEventTypeByMessageType(MessageTypeEnum.LESSON_STARTED);
                    if(lessonStartedEventType != null){
                        lessonStartedEvents = parser.getEventsByType(lessonStartedEventType);                                         
                    }
                    
                    EventType lessonCompletedEventType = messageParser.getEventTypeByMessageType(MessageTypeEnum.LESSON_COMPLETED);
                    if(lessonCompletedEventType != null){
                        lessonCompletedEvents = parser.getEventsByType(lessonCompletedEventType);                                         
                    }
                }

                boolean conductMidLessonAssessmentAnalysis = false;
                int userId = -1, domainSessionId = -1;

                //add the event contents for each event type
                for (EventType eventType : reportProperties.getEventTypeOptions()) {

                    if(reportProperties.isSelected(eventType)) {

                        if(logger.isInfoEnabled()){
                            logger.info("Adding event type of "+eventType+" to report");
                        }

                        List<AbstractEvent> events = parser.getEventsByType(eventType);

                        if(events != null){

                            // capture performance assessment events for time on task analysis
                            List<PerformanceAssessmentEvent> perfAssEvents = null;

                            // capture branch path history events for duration analysis
                            List<BranchPathHistoryEvent> branchPathHistoryEvents = null;          

                            for (AbstractEvent event : events) {

                                    if(event instanceof DomainSessionEvent){
                                        DomainSessionEvent dsEvent = (DomainSessionEvent) event;
    
                                        if(participantId != null){
                                            // make sure the participant id column is populated for every domain session event
                                            // in order to facilitate merge by participant id logic if selected to do so
                                            dsEvent.setParticipantId(participantId);
                                            writer.addHeaderColumn(EventReportColumn.PARTICIPANT_ID_COL);
    
                                            if(!participantAttemptCounted){
                                                // make sure participant attempt count is set/incremented for each attempt by the same participant
                                                ParticipantAttemptCnt participantAttemptCnt = participantToAttemptCnt.get(participantId);
                                                if(participantAttemptCnt == null){
                                                    participantAttemptCnt = new ParticipantAttemptCnt(dsEvent.getUserId(), dsEvent.getDomainSessionId(), participantId);
                                                    participantToAttemptCnt.put(participantId, participantAttemptCnt);
                                                    writer.addHeaderColumn(EventReportColumn.ATTEMPT_COL);
                                                }else{
                                                    participantAttemptCnt.incrementAttemptCnt();
                                                }
    
                                                participantAttemptCounted = true;
                                            }
                                        }
                                        
                                        if(reportProperties.isColumnEnabled(EventReportColumn.COURSE_ATTEMPT_COL) && userDomainSessionEventsMap.containsKey(dsEvent.getUserId())) {
                                            List<Integer> courseAttempts = userDomainSessionEventsMap.get(dsEvent.getUserId());
                                            dsEvent.setCourseAttempt(courseAttempts.indexOf(dsEvent.getDomainSessionId()) + 1);
                                        }
                                        
                                        Double elapsedDKFTime = ReportGenerationUtil.getElapsedDKFTime(lessonStartedEvents, lessonCompletedEvents, dsEvent);
                                        dsEvent.setElapsedDKFTime(elapsedDKFTime);
                                     }
    
                                    if(event instanceof PerformanceAssessmentEvent){
    
                                        if(perfAssEvents == null){
                                            perfAssEvents = new ArrayList<>();
                                        }
    
                                        perfAssEvents.add((PerformanceAssessmentEvent) event);
    
                                    } else if(event instanceof BranchPathHistoryEvent){
    
                                        if(branchPathHistoryEvents == null){
                                            branchPathHistoryEvents = new ArrayList<>();
                                        }
    
                                        branchPathHistoryEvents.add((BranchPathHistoryEvent) event);
                                    }
                                    
                                    writer.addRow(event.toRow());
                                } //end for loop on events from the current log
    
                                //
                                // Aggregate analysis over events of interest from report properties and that are in the event source
                                //
                                if(perfAssEvents != null && !perfAssEvents.isEmpty()){
                                    // conduct time on task analysis
                                    PerformanceAssessmentEvent firstEvent = perfAssEvents.get(0);
                                    userId = firstEvent.getUserId();
                                    domainSessionId = firstEvent.getDomainSessionId();
    
                                    conductMidLessonAssessmentAnalysis = true;
    
                                    TimeOnTaskAggregate timeOnTaskAggregate = new TimeOnTaskAggregate(perfAssEvents, userId, domainSessionId, participantId);
                                    Double elapsedDKFTime = ReportGenerationUtil.getElapsedDKFTime(lessonStartedEvents, lessonCompletedEvents, timeOnTaskAggregate);
                                    timeOnTaskAggregate.setElapsedDKFTime(elapsedDKFTime);
                                    writer.addHeaderColumns(timeOnTaskAggregate.getColumns());
                                    writer.addRow(timeOnTaskAggregate.toRow());
                                }
    
                                if(branchPathHistoryEvents != null && !branchPathHistoryEvents.isEmpty()){
                                    // conduct branch path duration analysis
                                    BranchPathHistoryEvent firstEvent = branchPathHistoryEvents.get(0);
                                    userId = firstEvent.getUserId();
                                    domainSessionId = firstEvent.getDomainSessionId();
    
                                    BranchPathHistoryAggregate aggregate = new BranchPathHistoryAggregate(branchPathHistoryEvents, userId, domainSessionId, participantId);
                                    writer.addHeaderColumns(aggregate.getColumns());
                                    writer.addRow(aggregate.toRow());
                                }
                            }
                        }
                    } // end for loop on events to include in this report

                //
                // Aggregate analysis on mid lesson assessments (surveys) and remediation content
                //
                if(conductMidLessonAssessmentAnalysis){
                    MidLessonAssessmenRemediationAgreggate midLesson =
                            new MidLessonAssessmenRemediationAgreggate(((MessageLogEventSourceParser)parser).getMessageLogReader(), userId, domainSessionId, participantId);
                    writer.addHeaderColumns(midLesson.getColumns());
                    writer.addRow(midLesson.toRow());
                }

                currentFileNum++;
                progressIndicator.setPercentComplete( (progressEnd - progressStart)*currentFileNum/numFiles + progressStart);
                subtaskProgressIndicator.setPercentComplete(currentFileNum/numFiles);
            }  //end for loop on sources to include in this report

            //
            // Aggregate info captured across event sources
            //
            for(ParticipantAttemptCnt attemptCnt : participantToAttemptCnt.values()){
                writer.addRow(attemptCnt.toRow());
            }

        }catch(Throwable e){
            logger.error("Caught exception/error while trying to parse the content in the selected event sources (i.e. files).", e);
        throw new DetailedException("Unable create the report from experiment data.",
                "There was a problem while trying to write the report for experiment named '"+experiment.getName()+"'.  The error message reads:\n"+e.getMessage(), e);
        }

        if(logger.isInfoEnabled()){
            logger.info("Writing event report file for data set " +experiment.getName()+"'.");
        }
        try {
            //write all the added event contents to file
            writer.write();
            if(logger.isInfoEnabled()){
                logger.info("Finished creating report file for data set " +experiment.getName()+"'.");
            }
        } catch (Throwable e) {
            logger.error("Caught exception/error while writing report " +experiment.getName()+"'.", e);
            throw new DetailedException("Unable to write the report from data set report.",
                    "There was a problem while trying to write the report for data set named '"+experiment.getName()+"'.  The error message reads:\n"+e.getMessage(), e);
        }

        return reportProperties.getFileName();
    }


    /**
     * Returns a new ReportProperties object based on the one passed in but with columns added for all events from
     *  the list of log files that match events selected in reportProperties
     *
     * @param username the username of the user creating the report.  Can't be null or empty.
     * @param experiment the experiment that a report is being generated for.
     * @param reportProperties the incoming ReportProperties with events that don't have columns
     * @param logs list of files to process looking for matching events and columns for those events
     * @param progressIndicator used to provide visual feedback for the user
     * @param progressStart integer 0-100 representing where the progressIndicator should start
     * @param progressEnd integer 0-100 representing where the progressIndicator should end when method completes
     * @return a new ReportProperties as close to the original
     * @throws DetailedException for out of memory or if the parsing throws an error
     */
    private ReportProperties addReportPropertiesColumns(String username, DbDataCollection experiment, ReportProperties reportProperties, List<File> logs, ProgressIndicator progressIndicator, int progressStart, int progressEnd) throws DetailedException {

        Set<EventReportColumn> eventColumns = new HashSet<>();
        List<EventReportColumn> eventReportColumnList = reportProperties.getReportColumns();
        ReportProperties newReportProperties = null;

        // loop through all files, identifying a parser along with events and columns

        progressIndicator.setTaskDescription(PARSING_DATA_SET_TASK_DESC);
        ProgressIndicator subtaskProgressIndicator = progressIndicator.getSubtaskProcessIndicator();
        subtaskProgressIndicator.setTaskDescription(PARSING_DATA_SET_TASK_DESC);
        int numFiles = logs.size();
        int currentFileNum = 0;
        for(File file : logs) {
            AbstractEventSourceParser parser = null;
            try {
                parser = EventSourceUtil.getEventParser(file, reportProperties);
            } catch (Exception e) {
                logger.error("Caught exception/error while parsing file " +file.getName() +" for data set " + experiment.getName()
                        + "This can happen if there is no logic to parse that particular file type or the format of the file is not what GIFT is expecting." , e);
                throw new DetailedException("Unable to parse data set results to create report.",
                        "Unable to parse the file '"+file.getName()
                        +". This can happen if there is no logic to parse that particular file type or the format of the file is not what GIFT is expecting. The error message reads:\n"+e.getMessage(), e);

            }
            if(parser == null){
                logger.error("Unable to find an event parser for file named "+file.getName() +
                        "This can happen if there is no logic to parse that particular file type or the format of the file is not what GIFT is expecting.");
                throw new DetailedException("Unable to parse data set results", "Unable to find an event parser for file named "+file.getName()
                        +". This can happen if there is no logic to parse that particular file type or the format of the file is not what GIFT is expecting.", null);
            }

            // Build a list of columns from any event included in the original reportProperties object
            // Uses a set of event columns to check for duplicates and a list of eventReportColumns to maintain column order
            for(EventType eType : parser.getTypesOfEvents()){

                if(logger.isInfoEnabled()){
                    logger.info("checking event type " + eType.getName());
                }

                if(reportProperties.getEventTypeOptions().contains(eType)) {

                    if(logger.isInfoEnabled()){
                        logger.info("Found matching event "+eType.getName() +" and " + eType.getEventColumns().size()+" columns");
                    }

                    for(EventReportColumn erc : eType.getEventColumns()) {
                        // Should be faster to do the contains on the Set, then add to the List
                        if (!eventColumns.contains(erc)) {
                            eventColumns.add(erc);
                            eventReportColumnList.add(erc);
                        }
                    }
                }
            }
            currentFileNum++;
            progressIndicator.setPercentComplete( (progressEnd - progressStart)*currentFileNum/numFiles + progressStart);
            subtaskProgressIndicator.setPercentComplete(currentFileNum/numFiles);
        }

        List<EventType> et = new ArrayList<>(reportProperties.getEventTypeOptions());

        //TODO: future.  If the new UI adds capabilities, or if ReportProperties is enhanced in the future,
        //      there might be more attributes that need to be set in the new ReportProperties object
        //      The root issue is that one cannot add columns to ReportProperties, so a new one has to be created instead of modifying the original
        newReportProperties = new ReportProperties(username, reportProperties.getEventSourceIds(), et, eventReportColumnList, reportProperties.getEmptyCellValue(),
                reportProperties.getFileName());
        newReportProperties.setMergeByColumn(reportProperties.getMergeByColumn());

        // set all events as selected in the new ReportProperties object
        for (EventType eventType : et) {
            newReportProperties.setSelected(eventType, true);
        }

        return newReportProperties;
    }

    @Override
    public boolean doesCourseHaveDataCollectionDataSets(String username, String coursePath, DataSetType ...ignoreTypes) {
        boolean hasDataCollection = false;

        DbServicesInterface dbServices = ServicesManager.getInstance().getDbServices();
        UMSDatabaseManager umsDb = UMSDatabaseManager.getInstance();

        CourseRecord courseRecord = dbServices.getCourseByPath(coursePath);

        if (courseRecord != null) {
            List<DbDataCollection> experiments = umsDb.getExperimentsByCourseId(courseRecord.getCourseId(), null);
            if (CollectionUtils.isNotEmpty(experiments)) {
                
                if(ignoreTypes != null){
                    // remove data set types to ignore, if provided
                    for(DataSetType ignoreType : ignoreTypes){
                        
                        Iterator<DbDataCollection> itr = experiments.iterator();
                        while(itr.hasNext()){
                            DbDataCollection dataCollection= itr.next();
                            if(dataCollection.getDataSetType().equals(ignoreType)){
                                itr.remove();
            }
        }

                    }
                }
                
                if(StringUtils.isNotBlank(username)){
                    // remove data set types this user doesn't have permissions too
                    Iterator<DbDataCollection> itr = experiments.iterator();
                    while(itr.hasNext()){
                        DbDataCollection dataCollection= itr.next();
                        
                        boolean hasPermission = false;
                        if(CollectionUtils.isNotEmpty(dataCollection.getPermissions())){
                            Set<DbDataCollectionPermission> permissions = dataCollection.getPermissions();
                            for(DbDataCollectionPermission permission : permissions){
                                
                                if(username.equals(permission.getUsername())){
                                    hasPermission = true;
                                    break;
                                }
                            }
                        }
                        
                        if(!hasPermission){
                            itr.remove();
                        }
                    }
                }
                
                // check if still not empty after removing those data set types to ignore
                hasDataCollection = CollectionUtils.isNotEmpty(experiments);
            }
        }

        return hasDataCollection;
    }

    @Override
    public void endDataCollectionDataSets(String username, String courseId) {

        DbServicesInterface dbServices = ServicesManager.getInstance().getDbServices();
        UMSDatabaseManager umsDb = UMSDatabaseManager.getInstance();

        CourseRecord courseRecord = dbServices.getCourseByPath(courseId);

        if (courseRecord != null) {
            List<DbDataCollection> experiments = umsDb.getExperimentsByCourseId(courseRecord.getCourseId(), null);
            if (experiments != null && !experiments.isEmpty()) {
                for (DbDataCollection experiment : experiments) {

                    experiment.setStatus(ExperimentStatus.ENDED);
                    ServicesManager.getInstance().getDataCollectionServices().updateDataCollectionItem(username, experiment);
                }
            }
        }
    }

    @Override
    public boolean checkDataCollectionForPermissions(DbDataCollection dbExperiment, String username,
            DataCollectionUserRole... requiredPermissions) {

        return UMSDatabaseManager.getInstance().checkDataCollectionForPermissions(dbExperiment, username, requiredPermissions);
    }

    @Override
    public List<DbDataCollection> getPublishedCoursesOfType(final String courseFolder, final DataSetType dataSetType) throws DetailedException{
        return UMSDatabaseManager.getInstance().getPublishedCoursesOfType(courseFolder, dataSetType, true, null);        
}

}
