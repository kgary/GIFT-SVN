/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services.file;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.Course;
import generated.course.ImageProperties;
import generated.course.LtiProperties;
import generated.course.Media;
import generated.course.PDFProperties;
import generated.course.SlideShowProperties;
import generated.course.VideoProperties;
import generated.course.WebpageProperties;
import generated.course.YoutubeVideoProperties;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.CourseRecord;
import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.DomainOption.DomainOptionPermissions;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.TrainingAppCourseObjectWrapper;
import mil.arl.gift.common.course.CourseFileAccessDetails;
import mil.arl.gift.common.course.CourseFileValidationException;
import mil.arl.gift.common.course.GIFTValidationResults;
import mil.arl.gift.common.course.MetadataFileValidationException;
import mil.arl.gift.common.course.PedagogyFileValidationException;
import mil.arl.gift.common.course.SensorFileValidationException;
import mil.arl.gift.common.course.TrainingAppUtil;
import mil.arl.gift.common.course.dkf.DKFValidationException;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.AbstractSchemaHandler.FileType;
import mil.arl.gift.common.io.CourseListFilter.CourseSourceOption;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.CourseListFilter;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DownloadableFileRef;
import mil.arl.gift.common.io.ExportProperties;
import mil.arl.gift.common.io.FileExistsException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.GiftScenarioProperties;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.io.MapTileProperties;
import mil.arl.gift.common.io.MapTileProperties.MapTileCoordinate;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.ProhibitedUserException;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.io.ZipUtils;
import mil.arl.gift.common.metadata.MetadataSearchResult;
import mil.arl.gift.common.metadata.QuadrantRequest;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.FillInTheBlankQuestion;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.common.metadata.MetadataWrapper.ContentTypeEnum;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.domain.DomainCourseFileHandler.CourseOptionsWrapper;
import mil.arl.gift.domain.knowledge.metadata.MetadataSchemaHandler;
import mil.arl.gift.net.nuxeo.WritePermissionException;
import mil.arl.gift.tools.authoring.common.ValidationUtil;
import mil.arl.gift.tools.authoring.common.conversion.UnsupportedVersionException;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.ServicesProperties;
import mil.arl.gift.tools.services.db.DbServicesInterface;
import mil.arl.gift.tools.services.experiment.DataCollectionServicesInterface;
import mil.arl.gift.ums.db.survey.Surveys;
import mil.arl.gift.ums.db.table.DbDataCollection;

/**
 * This class contains GIFT file system services to abstract the logic involved with
 * the various deployment modes of GIFT.
 *
 * @author mhoffman
 *
 */
public abstract class AbstractFileServices {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractFileServices.class);

    protected static final File DOMAIN_DIRECTORY = new File(ServicesProperties.getInstance().getDomainDirectory());
    protected static final File EXPORT_DIRECTORY = new File(ServicesProperties.getInstance().getExportDirectory());
    protected static final File UPLOAD_DIRECTORY = new File(ServicesProperties.getInstance().getUploadDirectory());
    protected static final File EXPERIMENT_DIRECTORY = new File(ServicesProperties.getInstance().getExperimentsDirectory());
    protected static final File TRAINING_APPS_DIRECTORY = new File(ServicesProperties.getInstance().getTrainingAppsDirectory());
    protected static final File TRAINING_APPS_MAPS_DIRECTORY = new File(TRAINING_APPS_DIRECTORY + File.separator
            + PackageUtil.getWrapResourcesDir() + File.separator + PackageUtil.getTrainingAppsMaps());
    protected static final File TRAINING_APPS_MAPS_PUBLIC_DIRECTORY = new File(TRAINING_APPS_MAPS_DIRECTORY + File.separator + "Public");

    /** name of the root folder where course folders should be placed for execution of that course */
    private static final String COURSE_RUNTIME_DIR_NAME = CommonProperties.getInstance().getDomainRuntimeDirectory();
    protected static final File COURSE_RUNTIME_DIR = new File(COURSE_RUNTIME_DIR_NAME);
    protected static final File RUNTIME_EXPERIMENT_DIR = new File(COURSE_RUNTIME_DIR_NAME + File.separator + "experiments");

    public static final String PUBLIC_WORKSPACE_FOLDER_NAME = "Public";
    public static final String SLIDE_SHOWS_FOLDER_NAME = "Slide Shows";

    protected static final String COPY_FILE_PROGRESS_DESC = "Copying file...";
    public static final String VALIDATE_COURSE_PROGRESS_DESC = "Validating Course";

    /** progress indicator description for starting to retrieve public training app objects */
    protected static final String GET_SHOWCASE_TA_OBJECTS_PROGRESS_DESC = "Retrieving showcase objects";

    /** progress indicator percent for starting to retrieve public training app objects */
    protected static final int GET_SHOWCASE_TA_OBJECTS_PROGRESS_PERC = 0;

    /** progress indicator description for starting to retrieve username training app objects */
    protected static final String GET_USERNAME_TA_OBJECTS_PROGRESS_DESC = "Retrieving your objects";

    /** progress indicator percent for starting to retrieve username training app objects */
    protected static final int GET_USERNAME_TA_OBJECTS_PROGRESS_PERC = 50;

    /** progress indicator description for starting to check training app objects */
    protected static final String CHECK_TA_OBJECTS_PROGRESS_DESC = "Checking the objects";

    /** progress indicator percent for starting to checktraining app objects */
    protected static final int CHECK_TA_OBJECTS_PROGRESS_PERC = 70;

    /** progress indicator description for starting to upconvert training app objects */
    protected static final String UPCONVERTING_TA_OBJECTS_PROGRESS_DESC = "Upconverting the objects (that need it)";

    private static final String TRAINING_APPS_LIB_FOLDER_NAME = "TrainingAppsLib";
    private static final String PUBLIC_TRAINING_APPS_LIB_FOLDER_NAME = "Public";

    /**
     * The default username for a user that accesses GIFT Wrap through the system tray icon in
     * Desktop mode. Must match
     * mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility#GIFT_WRAP_DESKTOP_USER}.
     */
    public final static String GIFT_WRAP_DESKTOP_USER = "GIFT_Wrap_local-user";

    protected static final String ALL_COURSES_PROGRESS_DESC = "Retrieving all courses";
    protected static final String SHOWCASE_COURSES_PROGRESS_DESC = "Retrieving showcase courses";
    protected static final String YOUR_COURSES_PROGRESS_DESC = "Retrieving your created courses";
    protected static final String SHARED_COURSES_PROGRESS_DESC = "Retrieving courses shared with you";
    protected static final String SURVEY_CHECK_PROGRESS_DESC = "Checking surveys in these courses";
    protected static final int COURSES_PERCENT_COMPLETE = 75;
    protected static final int SHOWCASE_COURSES_PERCENT_COMPLETE = 30;
    protected static final int YOUR_COURSES_PERCENT_COMPLETE = 60;
    protected static final int SURVEY_PERCENT_COMPLETE = 90;

    /**
     * how much time difference to ignore between the last modified time and the last successful validation time
     * this is needed because the validation happens first and that timestamp is then saved in the course.xml which
     * causes the last modified timestamp to always be after the validation time.
     */
    private static final long SAVE_BUFFER_MS = 3000;  //Note: 1000 was too small on GIFT Cloud

    /**
     * the folder where training application course objects not associated with courses are located
     * (normally Domain/workspace/Public/TrainingAppsLib/)
     */
    private static FileTreeModel trainingAppsLibTreeModel = null;

    /**
     * the folder where training application course objects not associated with courses or any
     * specific user are located (normally Domain/workspace/Public/TrainingAppsLib/Public)
     */
    private static FileTreeModel publicTrainingAppsLibTreeModel = null;

    /**
     * Return the folder to place the course folder the user wishes to run right now.
     *
     * @param runtimeRootFolderName the folder name that should be created/used at the root runtime folder level.  This typically is the username.
     * @return the folder to place the course folder the user wants to run
     */
    protected File getCourseRuntimeFolder(String runtimeRootFolderName){

        String timestamp = TimeUtil.formatCurrentTime();
        return new File(COURSE_RUNTIME_DIR_NAME + File.separator + runtimeRootFolderName + File.separator + timestamp);
    } 
    
    /**
     * Return the content type referenced by the training application provided.
     * @param trainingApplication contains references to the training application.
     * @return the content type referenced in the training application object.  Can be null if the content type could
     * not be determined.
     */
    public ContentTypeEnum getContentType(generated.course.TrainingApplication trainingApplication){
     
        if(trainingApplication != null){

            TrainingApplicationEnum appType = null;
            if(trainingApplication.getTrainingAppTypeEnum() == null){
                appType = TrainingAppUtil.getTrainingAppType(trainingApplication);
            }else if(trainingApplication.getTrainingAppTypeEnum() != null){
                appType = TrainingApplicationEnum.valueOf(trainingApplication.getTrainingAppTypeEnum());
            }
            
            if(appType != null){
                
                if(appType.equals(TrainingApplicationEnum.ARES)){
                    return ContentTypeEnum.ARES;
                }else if(appType.equals(TrainingApplicationEnum.DE_TESTBED)){
                    return ContentTypeEnum.DE_TESTBED;
                }else if(appType.equals(TrainingApplicationEnum.POWERPOINT)){
                    return ContentTypeEnum.POWERPOINT;
                }else if(appType.equals(TrainingApplicationEnum.TC3)){
                    return ContentTypeEnum.TC3;
                }else if(appType.equals(TrainingApplicationEnum.UNITY_DESKTOP) ||
                        appType.equals(TrainingApplicationEnum.UNITY_EMBEDDED)){
                    return ContentTypeEnum.UNITY;
                }else if(appType.equals(TrainingApplicationEnum.VBS)){
                    return ContentTypeEnum.VIRTUAL_BATTLESPACE;
                }else if(appType.equals(TrainingApplicationEnum.VR_ENGAGE)){
                    return ContentTypeEnum.VR_ENGAGE;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Return the content type referenced by the metadata provided.
     * @param username used to check for permissions to read/retrieve additional files referenced by the metadata.
     * @param courseFolder the course that contains this metadata and potentially the file the metadata references.
     * @param metadata contains a reference to some content of which this method wants to get the content type for.
     * @return the content type of the content referenced by the metadata.  If the content type can't be determined, null is returned.
     * If the metadata or course folder is null, null is returned.
     * @throws DetailedException if there was a problem reading the file that is referenced by the metadata.
     * @throws FileNotFoundException if the file referenced by the metadata couldn't be found in the course folder where it should be located.
     * @throws IllegalArgumentException if a parameter specified when parsing a file referenced by the metadata is not valid
     * @throws UnsupportedVersionException  if the file referenced by the metadata contains a GIFT XML format that is supported by this GIFT instance
     */
    public ContentTypeEnum getContentType(String username, AbstractFolderProxy courseFolder, generated.metadata.Metadata metadata) 
            throws DetailedException, FileNotFoundException, IllegalArgumentException, UnsupportedVersionException{
        
        if(metadata == null){
            return null;
        }else if(courseFolder == null){
            return null;
        }
        
        ContentTypeEnum contentTypeEnum = null;
        
        Serializable content = metadata.getContent();
        if(content instanceof generated.metadata.Metadata.LessonMaterial &&
                ((generated.metadata.Metadata.LessonMaterial)content).getValue() != null){
            
            generated.metadata.Metadata.LessonMaterial lessonMaterial = (generated.metadata.Metadata.LessonMaterial)content;
            String lessonMaterialFile = lessonMaterial.getValue();
            
            generated.course.LessonMaterialList lessonMaterialList = 
                    (generated.course.LessonMaterialList) unmarshalFile(username, trimWorkspaceFromPath(courseFolder.getFileId() + File.separator + lessonMaterialFile, username).getRelativePathFromRoot()).getUnmarshalled();
            if(lessonMaterialList == null){
                return null;
            }
            
            Media item = lessonMaterialList.getMedia().get(0);
            if(item.getMediaTypeProperties() instanceof SlideShowProperties) {
                contentTypeEnum = ContentTypeEnum.SLIDE_SHOW;
            }else if (item.getMediaTypeProperties() instanceof YoutubeVideoProperties) {
                contentTypeEnum = ContentTypeEnum.YOUTUBE_VIDEO;
            } else if (item.getMediaTypeProperties() instanceof LtiProperties) {
                contentTypeEnum = ContentTypeEnum.LTI_PROVIDER;
            } else if(item.getMediaTypeProperties() instanceof PDFProperties) {
                contentTypeEnum = ContentTypeEnum.PDF;
            } else if (item.getMediaTypeProperties() instanceof WebpageProperties) {
                
                if(UriUtil.isWebAddress(item.getUri())){
                    contentTypeEnum = ContentTypeEnum.WEB_ADDRESS;
                }else{
                    contentTypeEnum = ContentTypeEnum.LOCAL_WEBPAGE;
                }
            } else if (item.getMediaTypeProperties() instanceof ImageProperties) {
                contentTypeEnum = ContentTypeEnum.LOCAL_IMAGE;
            
            } else if (item.getMediaTypeProperties() instanceof VideoProperties) {
                contentTypeEnum = ContentTypeEnum.LOCAL_VIDEO;
            }
                
            
        } else if(content instanceof generated.metadata.Metadata.URL){
            contentTypeEnum = ContentTypeEnum.WEB_ADDRESS;
            
        }else if(content instanceof generated.metadata.Metadata.Simple && 
                ((generated.metadata.Metadata.Simple)content).getValue() != null){
                       
            String ref = ((generated.metadata.Metadata.Simple)content).getValue();
            if(StringUtils.endsWith(ref, Constants.ppt_show_supported_types)){
                contentTypeEnum = ContentTypeEnum.POWERPOINT;
            }else if(StringUtils.endsWith(ref, AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION)){
                contentTypeEnum = ContentTypeEnum.CONVERSATION_TREE;
            }else if(StringUtils.endsWith(ref, FileUtil.QUESTION_EXPORT_SUFFIX)){
                
                // determine highlight or summarize passage type
                
                AbstractQuestion abstractQuestion = 
                        (AbstractQuestion) unmarshalFile(username, trimWorkspaceFromPath(courseFolder.getFileId() + File.separator + ref, username).getRelativePathFromRoot()).getUnmarshalled();
                if(abstractQuestion == null){
                    return null;
                }
                
                if(abstractQuestion instanceof FillInTheBlankQuestion && abstractQuestion.getProperties() != null){
                        
                        if(abstractQuestion.getProperties().hasProperty(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)
                                && abstractQuestion.getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)){
                            contentTypeEnum = ContentTypeEnum.SUMMARIZE_PASSAGE;
                        }else{
                            contentTypeEnum = ContentTypeEnum.HIGHLIGHT_PASSAGE;
                        }
                }
            }else{
                // defaulting
                contentTypeEnum = ContentTypeEnum.LOCAL_WEBPAGE;
            }
        }else if(content instanceof generated.metadata.Metadata.TrainingApp &&
                ((generated.metadata.Metadata.TrainingApp)content).getValue() != null){
            
            String trainingAppRef = ((generated.metadata.Metadata.TrainingApp)content).getValue();
            generated.course.TrainingApplicationWrapper trainingAppWrapper = 
                    (generated.course.TrainingApplicationWrapper) unmarshalFile(username, trimWorkspaceFromPath(courseFolder.getFileId() + File.separator + trainingAppRef, username).getRelativePathFromRoot()).getUnmarshalled();
            if(trainingAppWrapper == null){
                return null;
            }
            
            generated.course.TrainingApplication trainingApp = trainingAppWrapper.getTrainingApplication();
            contentTypeEnum = getContentType(trainingApp);            
        }
        
        return contentTypeEnum;
    }

    /**
     * Attempts to lock all descendant files (not folders) under the file tree model provided.
     *
     * @param username information used to authenticate the request.
     * @param browserSessionKey browser identifier used to authenticate the request
     * @param directory the directory to lock all descendant files
     * @param lockedFiles a list of locked files.  This list will be empty if any of the descendant files could not be locked.  Can't be null.
     * @return true iff all descendant files were locked.
     * @throws IllegalArgumentException if there was a problem with a method argument
     */
    protected boolean lockAllFiles(String username, String browserSessionKey, FileTreeModel directory, List<FileTreeModel> lockedFiles)
            throws IllegalArgumentException{

        if(directory == null){
            throw new IllegalArgumentException("The file tree model can't be null.");
        }else if(!directory.isDirectory()){
            throw new IllegalArgumentException("The file tree model must be a directory.");
        }

        boolean failed = false;
        try{
            for(FileTreeModel child : directory.getSubFilesAndDirectories()){

                if(child.isDirectory()){
                    //failed if unable to lock all files
                    failed = !lockAllFiles(username, browserSessionKey, child, lockedFiles);
                }else if(lockFile(username, browserSessionKey, child.getRelativePathFromRoot(), false) != null){  //MH: TODO come back to this
                    lockedFiles.add(child);
                }else{
                    //found file that is already locked
                    failed = true;
                }

                if(failed){
                    break;
                }
            }
        }catch(Exception e){
            logger.error("Caught exception while trying to get locks on all files in "+directory+".", e);
            failed = true;
        }

        if(failed){

            //unlock all files locked by this method
            for(FileTreeModel lockedFile : lockedFiles){
                unlockFile(username, browserSessionKey, lockedFile.getRelativePathFromRoot());
            }

            lockedFiles.clear();
        }

        return !failed;
    }

    /**
     * Clean up a course that has been loaded. This normally means that the
     * loaded copy of the course folder (i.e. runtime course folder) will be
     * deleted.
     *
     * @param courseId the unique id of the course wanting to be cleaned up.
     * This id was provided as the return value to the load course method call.
     * @return boolean true iff the course was cleaned up. False can be returned
     * if the runtime course folder was not found or the cleanup failed.
     */
    public synchronized boolean cleanupCourse(String courseId){

        File courseFile = null;
        
        //
        // get course folder from course id (i.e. the Domain relative path to the course.xml file)
        //
        
        // under a username folder
        File usernameCourseFile = new File(COURSE_RUNTIME_DIR.getAbsolutePath() + File.separator + courseId);
        
        // under the experiments folder (of the runtime folder)
        File experimentsCourseFile = new File(RUNTIME_EXPERIMENT_DIR.getAbsolutePath() + File.separator + courseId);

        if(usernameCourseFile.exists()){
            courseFile = usernameCourseFile;
        }else if(experimentsCourseFile.exists()){
            courseFile = experimentsCourseFile;
        }
        
        if(courseFile != null && courseFile.exists()){
            
            // delete the course folder
            // An example of a path may look like this:  nblomberg/2015-06-23_10-00-00/Hemorrhage Control Lesson/HemorrhageControl.course.xml
            // In this case we want to get the folder with the timestamp, which is the parent of the course folder.
            File courseFolder = courseFile.getParentFile();

            if (courseFolder != null) {

                File timeStampFolder = courseFolder.getParentFile();
                try{
                    FileUtil.deleteDirectory(timeStampFolder);
                    return true;
                }catch(@SuppressWarnings("unused") IllegalArgumentException missingDirException){
                    // IGNORE - 09/21 MH: for some unknown reason Apache FileUtils will throw this even though the GIFT source
                    // checks if the directory exists multiple times.  Not sure if some other logic is also deleting this folder.
                    // e.g. java.lang.IllegalArgumentException: E:\work\GIFT\ARL SVN\branches\summarizeMedia_4989\GIFT\..\Domain\runtime\mhoffman\2021-09-16_10-04-14 does not exist
                }catch(IOException e){
                    logger.warn("Caught exception while trying to delete the course folder for course id of '"+courseId+"'.", e);
                }
            } else {
                logger.error("Unable to cleanup course '"+courseId+"' because a parent directory could not be found.");
            }

        }

        return false;
    }

    /**
     * Return whether or not the course folder identified by the course id has changed since the last successful
     * course validation.
     *
     * @param courseId the workspace relative path to the course.xml file. This id was provided as the return value to the
     * load course method call.  It should include the workspace folder name (e.g. Public, mhoffman).
     * @param username used to authenticate when retrieving the course folder.
     * @return true iff the last modified time on the course folder is more recent than the date of the last
     * successful course validation as saved in the course.xml.  True will be also be returned if there was a problem
     * checking the course.
     */
    public boolean hasCourseChangedSinceLastValidation(String courseId, String username){

        try{
            UnmarshalledFile uFile = unmarshalFile(username, courseId);
            generated.course.Course course = (generated.course.Course)uFile.getUnmarshalled();
            AbstractFolderProxy courseFolder = getCourseFolder(courseId, username);
            return hasCourseChangedSinceLastValidation(course, courseFolder, username);

        }catch(@SuppressWarnings("unused") Exception e){ }

        return true;
    }

    /**
     * Return whether or not the course folder has changed since the last successful course validation.
     *
     * @param course contains the optional last successful validation timestamp value
     * @param courseFolder the course folder to check the last modification date against
     * @param username used to authenticate when retrieving the course folder.
     * @return true iff the last modified time on the course folder is more recent than the date of the last
     * successful course validation as saved in the course.xml.  True will be also be returned if there was a problem
     * checking the course.
     */
    public boolean hasCourseChangedSinceLastValidation(generated.course.Course course, AbstractFolderProxy courseFolder, String username){

        try{

            Date lastSuccessful = DomainCourseFileHandler.getLastSuccessfulValidation(course);
            if(lastSuccessful != null){
                //compare to last modified date on course folder

                Date lastModified = getCourseFolderLastModified(courseFolder.getFileId(), username);

                if(lastModified != null){

                    //has the course been modified since the last successful validation
                    return lastModified.getTime() > lastSuccessful.getTime() + SAVE_BUFFER_MS;
                }

            }

        }catch(@SuppressWarnings("unused") Exception e){ }

        return true;
    }

    /**
     * Return the last modification date of the course folder.
     *
     * @param courseId the workspace relative path to the course.xml file. This id was provided as the return value to the
     * load course method call.  It should include the workspace folder name (e.g. Public, mhoffman).
     * @param username used to authenticate when retrieving the course folder.
     * @return the last modification date of the course folder.  Will be null if there was a problem retrieving the date.
     */
    public Date getCourseLastSuccessfulValidationDate(String courseId, String username){

        Date lastSuccessful = null;
        try{
            UnmarshalledFile uFile = unmarshalFile(username, courseId);
            generated.course.Course course = (generated.course.Course)uFile.getUnmarshalled();
            return DomainCourseFileHandler.getLastSuccessfulValidation(course);

        }catch(@SuppressWarnings("unused") Exception e){ }

        return lastSuccessful;
    }

    /**
     * Validate the file against the schema for that file as well as additional GIFT validation logic.
     * Note: if the file being validated is the course.xml than the entire course folder will be validated.  This will
     * also updated the course.xml last successful validation timestamp value.
     *
     * @param file the file to validate.  Must be a GIFT XML file type.
     * @param username used for authentication
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @param progressIndicator will be populated with any progress updates related to validating the course.  Can be null if progress updates
     * are not wanted.
     * @return validation results for the file
     * @throws FileNotFoundException if the file being validate doesn't exist
     * @throws IllegalArgumentException if there was a problem with the inputs to this method
     * @throws IOException if there was a problem retrieving the file or course folder for that file
     * @throws UnsupportedVersionException if the file being validated is an unsupported version
     * @throws FileValidationException if there was an issue validating (against the schema) the file
     * @throws DKFValidationException if there was a problem validating a DKF (including DKFs referenced by course and training app reference files)
     * @throws CourseFileValidationException if there was a problem validating a course file
     * @throws ConfigurationException if there was a general problem not specific to a file
     * @throws SensorFileValidationException  if there was a problem validating a sensor configuration file
     * @throws PedagogyFileValidationException if there was a problem validating a pedagogical configuration file
     * @throws MetadataFileValidationException  if there was a problem validating a metadata file (including referenced by course files)
     */
    public GIFTValidationResults validateFile(String file, String username, boolean failOnFirstSchemaError, ProgressIndicator progressIndicator)
            throws FileNotFoundException, IllegalArgumentException, IOException, UnsupportedVersionException,
            FileValidationException, DKFValidationException, CourseFileValidationException, ConfigurationException, SensorFileValidationException,
            PedagogyFileValidationException, MetadataFileValidationException{

        if(file == null){
            throw new IllegalArgumentException("The file can't be null");
        }

        //TODO: future logic might include the ability to pass in a serialized object that hasn't been saved to a file yet
        //      The work is in the various handlers and being able to handle parsing from an object and also reporting exception
        //      caused from a file versus an object being parsed in memory
//        if(serializableObject == null){
//            //read from file
//            serializableObject = unmarshalFile(username, file);
//        }

        //get course folder for file
        AbstractFolderProxy courseFolder = getCourseFolder(file, username);

        //get file
        FileProxy contentFile = getFile(file, username);

        FileType fileType = AbstractSchemaHandler.getFileType(file);

        GIFTValidationResults validationResults = ValidationUtil.validateFile(contentFile, courseFolder, fileType, failOnFirstSchemaError, progressIndicator);

        if(fileType == FileType.COURSE){

            if(!validationResults.hasCriticalIssue() && !validationResults.hasImportantIssues() && !validationResults.hasWarningIssues()){
                //update course.xml last successful validation time stamp --- best effort
                Date date = new Date();
                setCourseLastSuccessfulValidation(file, username, date);
                validationResults.setLastSuccessfulValidationDate(date);
            }else{
                //reset the date to null since we know it failed validation and the failure could have come from
                //something other than a change in the course folder (e.g. surveys, external URLs)
                setCourseLastSuccessfulValidation(file, username, null);
            }
        }

        return validationResults;
    }

    /**
     * Attempts to update the last successful validation timestamp in the course.xml provided.
     *
     * @param courseXMLFile contains the workspace relative path to the course.xml file in the course folder.
     * @param username used to retrieve and write the course.xml file
     * @param lastSuccessfulValidationDate the timestamp to use.  Null can be used to clear the value which is useful
     * when the course becomes in valid.
     */
    public void setCourseLastSuccessfulValidation(String courseXMLFile, String username, Date lastSuccessfulValidationDate){

        try{
            UnmarshalledFile uFile = unmarshalFile(username, courseXMLFile);
            generated.course.Course course = (generated.course.Course)uFile.getUnmarshalled();

            if(lastSuccessfulValidationDate != null){
                course.setLastSuccessfulValidation(DomainCourseFileHandler.ValidationTimeFormat.format(lastSuccessfulValidationDate));
            }else{
                course.setLastSuccessfulValidation(null);
            }

            marshalToFile(username, course, courseXMLFile, null);

        }catch(@SuppressWarnings("unused") Exception e){}
    }

    /**
     * Return whether or not the metadata content reference file is supported by GIFT's branch point course
     * element logic.
     *
     * [From MerrillsBranchPointHandler.java]
     *  ppsx, pps, ppsm = PowerPoint show
     *  trainingapp.xml = training application reference XML
     *  htm, html = local HTML file
     *
     * @param contentReference the content reference to check (e.g. a.pps, gift.png, trainingapp.xml, google.com)
     * @return boolean true iff the content file is supported.
     */
    public boolean isMBPContentTypeSupported(String contentReference){
        return MetadataSchemaHandler.isMetadataContentTypeSupported(contentReference);
    }

    /**
     * Find all the courses for the user. This method reports parsing and validation errors in the
     * course options wrapper object.
     *
     * @param username information used to authenticate the request.  In this case this information is used
     * to determine what courses the user has Read access too.
     * @param courseOptionsWrapper the object to populate with information about the courses found in the domain directory. Can't be null.
     * @param validateLogic whether or not to validate the courses found.  The validation logic referred to here is additional
     * GIFT validation logic (e.g. reachable URLs, DKF rules).  Note that this validation can take more time depending on the elements within each course
     * being validated.
     * @param validateSurveyReferences whether or not to validate the survey references found in the course found.  Note that this validation can take more time depending on the elements within each course
     * being validated.
     * @param progressIndicator used to provide progress updates to the caller.  Can be null.
     * @throws IllegalArgumentException if a parameter specified is not valid.
     * @throws FileNotFoundException if the domain directory specified was not found.
     * include logic like the survey element specified doesn't exist, not enough questions for a knowledge assessment survey)
     * @throws FileValidationException handles other types of exceptions
     * @throws DetailedException handle other types of exceptions
     * @throws ProhibitedUserException if the username is on the prohibited list, meaning the username can't be used in this gift instance (e.g. Public).
     */
    public void getCourses(String username, CourseOptionsWrapper courseOptionsWrapper,
            boolean validateLogic, ProgressIndicator progressIndicator)
                    throws IllegalArgumentException, FileNotFoundException, FileValidationException, DetailedException, ProhibitedUserException {
        getCourses(username, courseOptionsWrapper, null, validateLogic, progressIndicator);
    }

    /**
     * Find all the courses for the user. This method reports parsing and validation errors in the
     * course options wrapper object.
     *
     * @param username information used to authenticate the request.  In this case this information is used
     * to determine what courses the user has Read access too.
     * @param courseOptionsWrapper the object to populate with information about the courses found in the domain directory. Can't be null.
     * @param courseListFilter the filter object that is used to specify which types of courses should be returned
     * by the request. If it is null then no filtering should be performed
     * @param validateLogic whether or not to validate the courses found.  The validation logic referred to here is additional
     * GIFT validation logic (e.g. reachable URLs, DKF rules).  Note that this validation can take more time depending on the elements within each course
     * being validated.
     * @param validateSurveyReferences whether or not to validate the survey references found in the course found.  Note that this validation can take more time depending on the elements within each course
     * being validated.
     * @param progressIndicator used to provide progress updates to the caller.  Can be null.
     * @throws IllegalArgumentException if a parameter specified is not valid.
     * @throws FileNotFoundException if the domain directory specified was not found.
     * @throws FileValidationException handles other types of exceptions
     * @throws DetailedException handle other types of exceptions
     * @throws ProhibitedUserException if the username is on the prohibited list, meaning the username can't be used in this gift instance (e.g. Public).
     */
    public abstract void getCourses(String username, CourseOptionsWrapper courseOptionsWrapper,
            CourseListFilter courseListFilter, boolean validateLogic, ProgressIndicator progressIndicator)
                    throws IllegalArgumentException, FileNotFoundException,
                    FileValidationException,
                    DetailedException, ProhibitedUserException;

    /**
     * Find the course for the user. This method reports parsing and validation errors in the
     * course options wrapper object.
     *
     * @param username information used to authenticate the request.  In this case this information is used
     * to determine what courses the user has Read access too.
     * @param courseId the unique id of the course, used to retrieve the course.  This is the workspace relative path of the course.xml file.
     * @param courseOptionsWrapper the object to populate with information about the course found in the domain directory. Can't be null.
     * It is possible the course failed to be parsed/validated, therefore check the domain options collection and parseFailedFiles collection.
     * @param validateLogic whether or not to validate the course found.  The validation logic referred to here is additional
     * GIFT validation logic (e.g. reachable URLs, DKF rules).  Note that this validation can take more time depending on the elements within each course
     * being validated.
     * @param validateSurveyReferences whether or not to validate the survey references found in the course found.  Note that this validation can take more time depending on the elements within each course
     * being validated.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @param progressIndicator will be populated with any progress updates related to validating the course.  Can be null if progress updates
     * are not wanted.
     * @throws IllegalArgumentException if a parameter specified is not valid.
     * @throws FileNotFoundException if the domain directory specified was not found.
     * @throws DetailedException handle other types of exceptions
     * @throws ProhibitedUserException if the username is on the prohibited list, meaning the username can't be used in this gift instance (e.g. Public).
     */
    public abstract void getCourse(String username, String courseId, CourseOptionsWrapper courseOptionsWrapper,
            boolean validateLogic, boolean validateSurveyReferences, boolean failOnFirstSchemaError,
            ProgressIndicator progressIndicator)
                    throws IllegalArgumentException, FileNotFoundException,
                    DetailedException, ProhibitedUserException;

    /**
     * Return the last modified date for the course folder.
     *
     * @param courseFolderPath the path to the course folder.  For server mode this can start with '/default-domain/workspaces' or 'Workspaces' or from the
     * workspace folder name such as 'Public' or 'mhoffman'.  For desktop this can be an absolute path to the folder or relative to the workspaces folder (i.e. relative to Domain/workspaces/)
     * @param username used for authentication when retrieving the last modified date
     * @return the last modified date for the folder.  Will return null of the course folder path provided is null, empty or doesn't exist.
     * @throws IOException if there was a problem retrieving the last modified date from the course folder
     */
    public abstract Date getCourseFolderLastModified(String courseFolderPath, String username) throws IOException;

    /**
     * Finds the training application course objects in the search folder specified or if null, the
     * default training application course object library location.
     *
     * @param username information used to authenticate the request.
     * @param progressIndicator (optional) used to communicate progress of retrieving the training app objects
     * @return map of training application course objects found in searched location. key: path to
     *         the course object folder (i.e. contains the trainingApp.xml file) in the searched
     *         location. The root of the file tree model will be a workspace sub-folder (e.g.
     *         Public, <username>). value: a wrapper around a training application XML object in
     *         that trainingApp.xml file or a validation exception for the file
     * @throws DetailedException if there was a problem searching or parsing the training
     *         application course objects.
     */
    public abstract Map<FileTreeModel, TrainingAppCourseObjectWrapper> getTrainingAppCourseObjects(String username, ProgressIndicator progressIndicator) throws DetailedException;

    /**
     * Renames the provided folder within training apps lib and any descendent files that have the
     * same name.
     *
     * @param username information used to authenticate the request.
     * @param filePath The path to the folder that should be renamed. The root of the path must be a
     *        workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param newName The new name of the folder
     * @return true if the operation was successful, false otherwise
     * @throws DetailedException If there was a problem renaming the file.
     */
    public abstract boolean renameTrainingAppsLibFolder(String username, String filePath, String newName)
            throws DetailedException;

    /**
     * Finds all the media files in the course folder.  Media files are non GIFT XML files.
     *
     * @param username information used to authenticate the request.
     * @param courseFolderPath the path to the course folder in which to search for media files.
     * The root must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @return the file tree representation for the media files found
     */
    public abstract FileTreeModel getMediaFiles(String username, String courseFolderPath);

    /**
     * Load the specified course for the user to run with GIFT, i.e. the course folder is copied to a course runtime folder to be used
     * by GIFT.
     *
     * @param username the user wanting to run the specified course.  Used for authentication as well as naming the course runtime folder.
     * @param courseId the unique id of the course wanting to be executed.
     * @param courseOptionsWrapper contains information about the courses this user has access too
     * @param progressIndicator used to notify the caller of loading progress which can then be shown to the user. This will show progress status
     * values of loading and loaded as well as percent complete based on the number of files being copied.
     * @param runtimeRootFolderName the folder name that will be created/used at the root runtime level.  This typically is the username,
     * but for other load methods, such as lti, other unique names may be used at the root runtime level.
     * @return the new course id of the loaded course.  This id must be used to run the course.  Will be null if the user canceled the loading operation.
     * @throws IllegalArgumentException if a parameter specified is not valid
     * @throws DetailedException if there was a problem retrieving the course folder or copying it to the runtime course folder location.
     * @throws URISyntaxException if there was a problem build a URI to access the files
     */
    public abstract String loadCourse(String username, String courseId, CourseOptionsWrapper courseOptionsWrapper,
            final ProgressIndicator progressIndicator, String runtimeRootFolderName) throws IllegalArgumentException, DetailedException, URISyntaxException;

    /**
     * Load the specified course for an experiment, i.e. the course folder is
     * copied to a experiment course runtime folder to be used by GIFT.
     *
     * @param username the user wanting to run the specified experiment. Used
     *        for authentication.
     * @param experimentId unique identifier of the experiment being created.
     * @param sourceCourseXmlPath the relative path (with respect to the
     *        workspace folder) of the course xml that the experiment was
     *        published from.
     * @param courseOptionsWrapper contains information about the courses this
     *        user has access too
     * @param progressIndicator used to notify the caller of loading progress
     *        which can then be shown to the user. This will show progress
     *        status values of loading and loaded as well as percent complete
     *        based on the number of files being copied.
     * @return the new experiment folder id of the loaded experiment. This will
     *         be a path relative to the runtime experiments folder.
     * @throws IllegalArgumentException if a parameter specified is not valid
     * @throws DetailedException if there was a problem retrieving the course
     *         folder or copying it to the runtime course folder location.
     * @throws URISyntaxException if there was a problem build a URI to access
     *         the files
     */
    public abstract String loadExperiment(String username, String experimentId, String sourceCourseXmlPath,
            CourseOptionsWrapper courseOptionsWrapper, final ProgressIndicator progressIndicator)
            throws IllegalArgumentException, DetailedException, URISyntaxException;

    /**
     * Load the specified LTI course for the user to run with GIFT, i.e. the course folder is copied to a course runtime folder to be used
     * by GIFT.
     *
     * @param username the user wanting to run the specified course.  Used for authentication as well as naming the course runtime folder.
     * @param courseId the unique id of the course wanting to be executed.
     * @param courseOptionsWrapper contains information about the courses this user has access too
     * @param progressIndicator used to notify the caller of loading progress which can then be shown to the user. This will show progress status
     * values of loading and loaded as well as percent complete based on the number of files being copied.
     * @param runtimeRootFolderName the folder name that will be created/used at the root runtime level.  This typically is the username,
     * but for other load methods, such as lti, other unique names may be used at the root runtime level.
     * @return the new course id of the loaded course.  This id must be used to run the course.  Will be null if the user canceled the loading operation.
     * @throws IllegalArgumentException if a parameter specified is not valid
     * @throws DetailedException if there was a problem retrieving the course folder or copying it to the runtime course folder location.
     * @throws URISyntaxException if there was a problem build a URI to access the files
     */
    public abstract String loadLTICourse(String username, String courseId, CourseOptionsWrapper courseOptionsWrapper,
            final ProgressIndicator progressIndicator, String runtimeRootFolderName) throws IllegalArgumentException, DetailedException, URISyntaxException;

    /**
     * Return the root (top-level) folder that represents the user's workspace folder.
     *
     * For desktop this is Domain/workspace/{@literal <}username{@literal >}/
     * For server this is /default-domain/workspaces/{@literal <}username{@literal >}/
     *
     * @param username information used to authenticate the request and get the appropriately named folder.
     * @return the root folder that represents the user's workspace folder.  This will not contain the ancestors to that folder (i.e. Domain/workspace).
     * @throws DetailedException if there was a problem accessing the files to build the tree
     */
    public abstract FileTreeModel getUsersWorkspace(String username) throws DetailedException;

    /**
     * Return a new file tree model that contains the content files (references) under the course folder for the
     * given quadrant and collection of concepts.
     *
     * @param username information used to authenticate the request and get the appropriate files
     * @param courseFolderPath the path to the course folder in which to search for content files.
     * The root must be a workspace sub-folder (e.g. Public, &lt;username&gt;). Can't be null.
     * @param quadrantToRequest mapping of adaptive courseflow phase (e.g. Rule) to the metadata search criteria information
     * to use to search for metadata content in the course folder.  Can't be null.  If empty no metadata results are returned.
     * @return the result of the metadata search which contains a mapping of adaptive courseflow quadrant (e.g. Rule) to
     * the metadata in the course folder that satisfied the search criteria for that phase (for each phase requested).
     * @throws DetailedException if there was a problem accessing the files to build the tree
     * @throws URISyntaxException if there was a problem building a URI to access the files
     */
    public abstract MetadataSearchResult getMetadataContentFileTree(String username, String courseFolderPath,
            Map<MerrillQuadrantEnum, QuadrantRequest> quadrantToRequest)
            throws DetailedException, URISyntaxException;

    /**
     * Return the metadata file found for the given content file and quadrant.  The metadata file must reside in the same
     * folder as the content file.
     *
     * @param username used to authenticate the request
     * @param content the content file (e.g. PowerPoint show) to find a metadata file for.  The root of this tree should be the workspace
     * folder.  The parent to the content file will be used to find a sibling metadata file (i.e. in the same folder as the content file)/
     * @param quadrant the quadrant to filter metadata on.  This is important because a user could decide to use
     * the same content file across quadrant types.  Optional, can be null if the caller doesn't care about which quadrant the metadata is for.
     * @return the file names of the metadata files found that reference the content file and the specified quadrant.  The metadata file will be in
     * the same directory as the content file.  Can be empty but not null if a metadata file was not found.
     * @throws DetailedException if there was a problem accessing the files to build the tree
     * @throws URISyntaxException if there was a problem building a URI to access the files
     */
    public abstract List<String> getMetadataForContent(String username, FileTreeModel content, MerrillQuadrantEnum quadrant)
            throws DetailedException, URISyntaxException;

    /**
     * Return the root (top-level) folder to search for and save files.
     * For server that is "Workspaces" (as in /default-domain/workspaces/).
     * For desktop that is "workspace" (as in Domain/workspace).
     *
     * @param username information used to authenticate the request.
     * @return the root folder for searching and saving files of any type.
     * @throws DetailedException if there was a problem accessing the files to build the tree
     */
    public abstract FileTreeModel getRootTree(String username) throws DetailedException;

    /**
     * Return the File tree containing all descendant directories as well as files within those
     * directories that match the extension of the file type specified.
     *
     * @param username information used to authenticate the request.
     * @param fileType file type to filter on.  If null, all files will be included in the returned model
     * @return a file tree model representative of the file provided filtering on the file type specified (if any)
     * @throws IllegalArgumentException if a parameter specified is not valid
     * @throws DetailedException if there was a problem accessing the files to build the tree
     */
    public abstract FileTreeModel getFileTree(String username, FileType fileType) throws IllegalArgumentException, DetailedException;

    /**
     * Return the File tree containing all descendant directories as well as files within those
     * directories that match the extension of the file extension(s) specified.
     *
     * @param username information used to authenticate the request.
     * @param extensions file extensions (e.g. ".course.xml") to filter on.  If empty, all files will be included in the returned model
     * @return a file tree model representative of the file provided filtering on the file extension(s) specified (if any)
     * @throws IllegalArgumentException if a parameter specified is not valid
     * @throws DetailedException if there was a problem accessing the files to build the tree
     */
    public abstract FileTreeModel getFileTree(String username, String... extensions) throws IllegalArgumentException, DetailedException;

    /**
     * Unlocks the given file if it is currently locked.
     *
     * @param username information used to authenticate the request.
     * @param browserSessionKey the browser identifier used to authenticate the request.
     * @param workspaceRelativePath File that needs to be unlocked.  Can't be null or empty. e.g. mhoffman/test/text.course.xml, Public/Hello World/hello world.course.xml
     * @throws IllegalArgumentException if there was a problem with an argument
     */
    public abstract void unlockFile(String username, String browserSessionKey, String workspaceRelativePath) throws IllegalArgumentException;

    /**
     * Unlocks all files that are currently locked for the given browser session.
     *
     * @param username information used to authenticate the request. Cannot be null.
     * @param browserSessionKey the browser identifier used to figure out which browser session's locks should be removed. Cannot be null.
     * @throws IllegalArgumentException if there was a problem with an argument
     */
    public abstract void unlockAllFiles(String userName, String browserId);
    
    /**
     * Determines if the given file is locked.
     *
     * @param username information used to authenticate the request.
     * @param workspaceRelativePath File whose lock status is in question.  Can't be null or empty.  e.g. mhoffman/test/text.course.xml, Public/Hello World/hello world.course.xml
     * @return true if the file is locked, false otherwise.  False will be returned if the file doesn't exists.
     * @throws DetailedException if there was a problem accessing the file
     * @throws IllegalArgumentException if there was a problem with the file
     */
    public abstract boolean isLockedFile(String username, String workspaceRelativePath) throws DetailedException, IllegalArgumentException;

    /**
     * Attempts to lock the given file.
     *
     * @param username the user requesting access to a file.
     * @param browserSessionKey the browser that the user is making the request
     * from
     * @param workspaceRelativePath File that needs to be locked. Can't be null
     * or empty. e.g. mhoffman/test/text.course.xml, Public/Hello World/hello
     * world.course.xml
     * @param initialAcquisition specifies whether this is an initial request to
     * acquire the lock or if it is a renewal
     * @return information about the users accessing the specified file
     * including the user provided. Will not be null.
     * @throws IllegalArgumentException if there was a problem with the file
     * @throws DetailedException if there was a problem accessing the file or
     * locking the file
     */
    public abstract CourseFileAccessDetails lockFile(String username, String browserSessionKey, String workspaceRelativePath, boolean initialAcquisition)
            throws IllegalArgumentException, DetailedException;


    /**
     * Deletes the file, if it exists. The file can't be locked. If the file is
     * a directory this method will attempt to acquire a lock on all descendant
     * files. If that is not possible an IOException is thrown.
     *
     * @param username information used to authenticate the request.
     * @param browserSessionKey the browser identifier information used to
     * authenticate the request
     * @param filePath the path to the file or directory to delete.The root of
     * the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param progress the progress indicator for the delete operation. Can be
     * null.
     * @return true if the file was deleted, false otherwise.
     * @throws DetailedException if there was a problem accessing the file or
     * acquiring the lock on all descendant files
     * @throws IllegalArgumentException if there was a problem with the file
     */
    public boolean deleteFile(String username, String browserSessionKey, String filePath, ProgressIndicator progress) throws DetailedException, IllegalArgumentException{
        return deleteFile(username, browserSessionKey, filePath, progress, true);
    }

    /**
     * Deletes the file, if it exists. The file can't be locked. If the file is
     * a directory this method will attempt to acquire a lock on all descendant
     * files. If that is not possible an IOException is thrown.
     *
     * @param username information used to authenticate the request.
     * @param browserSessionKey the browser identifier information used to
     * authenticate the request
     * @param filePath the path to the file or directory to delete.The root of
     * the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param progress the progress indicator for the delete operation. Can be
     * null.
     * @param updateCourseFolderLastModifiedDate true to update the course folder's last modified date; false otherwise
     * @return true if the file was deleted, false otherwise.
     * @throws DetailedException if there was a problem accessing the file or
     * acquiring the lock on all descendant files
     * @throws IllegalArgumentException if there was a problem with the file
     */
    public abstract boolean deleteFile(String username, String browserSessionKey, String filePath,
            ProgressIndicator progress, boolean updateCourseFolderLastModifiedDate)
            throws DetailedException, IllegalArgumentException;

    /**
     * Renames a course to the specified name. This method renames the course folder, the course.xml file, and
     * updates the name element in the course.xml
     *
     * @param username Information used to authenticate the request. Can't be null.
     * @param coursePath Contains the name of the course to be renamed.
     * @param newName The new name for the course (e.g. 'My Course') with no extensions or file separators.
     * @return the path to the named course.xml file, relative to the workspace folder.
     * @throws DetailedException if there was a problem renaming the course
     */
    public String renameCourse(String username, String coursePath, String newName) throws DetailedException {

        if(username == null){
            throw new IllegalArgumentException("The username of the user invoking this action cannot be null");
        }

        if(coursePath == null){
            throw new IllegalArgumentException("The path to the course to be renamed cannot be null");
        }

        if(newName == null){
            throw new IllegalArgumentException("The new name to give the course being renamed cannot be null");
        }

        /* Check if the user has write permissions for the file */
        boolean writePermissions = hasWritePermissions(username, coursePath);
        if (!writePermissions) {
            throw new DetailedException(
                    "Renaming the course failed because the user doesn't have write permission. The course was not renamed.",
                    "The user '" + username + "' does not have write permission to rename the course '" + coursePath
                            + "'.",
                    null);
        }

        newName = newName.trim();

        FileTreeModel courseFile = FileTreeModel.createFromRawPath(coursePath);
        final String originalCourseName = courseFile.getParentTreeModel().getFileOrDirectoryName();

        AbstractFolderProxy courseFolder;

        try {
            courseFolder = getCourseFolder(coursePath, username);
            FileProxy courseFileProxy = courseFolder.getRelativeFile(courseFile.getFileOrDirectoryName());
            DomainCourseFileHandler courseHandler = new DomainCourseFileHandler(courseFileProxy, courseFolder, false);

            /* Update the course name in the course.xml and write the file */
            Course course = courseHandler.getCourse();
            course.setName(newName);
            marshalToFile(username, course, courseFile.getRelativePathFromRoot(), null);

            /* Update the course folder name */
            String originalCourseSourcePath = courseFile.getParentTreeModel().getRelativePathFromRoot();
            try {
                renameFile(username, originalCourseSourcePath, newName);
            } catch (DetailedException e) {
                /* Revert the change in the course.xml file */
                course.setName(originalCourseName);
                marshalToFile(username, course, courseFile.getRelativePathFromRoot(), null);

                throw e;
            }
            
            courseFile.getParentTreeModel().setFileOrDirectoryName(newName);
            String updatedCourseSourcePath = courseFile.getParentTreeModel().getRelativePathFromRoot();

            /* Update the course.xml name */
            try {
                renameFile(username, courseFile.getRelativePathFromRoot(),
                        newName + AbstractSchemaHandler.COURSE_FILE_EXTENSION);
            } catch (DetailedException e) {
                /* Revert the change in the course.xml file */
                course.setName(originalCourseName);
                marshalToFile(username, course, courseFile.getRelativePathFromRoot(), null);

                /* Revert the change to the course folder name */
                renameFile(username, updatedCourseSourcePath, originalCourseName);

                throw e;
            }

            String renamedCoursePath = updatedCourseSourcePath
                    + Constants.FORWARD_SLASH + newName + AbstractSchemaHandler.COURSE_FILE_EXTENSION;

            /* Get the course from the db to update */
            final DbServicesInterface dbServices = ServicesManager.getInstance().getDbServices();
            CourseRecord courseRecord = dbServices.getCourseByPath(coursePath);
            
            if(course.getSurveyContext() != null) {
                SurveyContext surveyContext = Surveys.getSurveyContext(course.getSurveyContext().intValue(), false);
                for(SurveyContextSurvey contextSurvey : surveyContext.getContextSurveys()){
                    
                    Survey survey = contextSurvey.getSurvey();
                    
                    if(survey != null && survey.getProperties() != null 
                            && survey.getProperties().hasProperty(SurveyPropertyKeyEnum.MEDIA_FILE_SOURCE)){
                            
                        /* Make sure that attempting to copy media items from this survey properly pulls them
                         * from the course folder's destination location */
                        survey.getProperties().setPropertyValue(
                                SurveyPropertyKeyEnum.MEDIA_FILE_SOURCE, 
                                courseFile.getParentTreeModel().getRelativePathFromRoot(false));
                        
                        Surveys.updateSurvey(survey);
                    }
                }
            }
            

            /* Do not need to update the database if it already contains the
             * correct path */
            boolean courseRecordAlreadyUpToDate = StringUtils.equalsIgnoreCase(courseRecord.getCoursePath(), renamedCoursePath);
            if (!courseRecordAlreadyUpToDate) {

                /* New path is different, so update the database */
                courseRecord.setCoursePath(renamedCoursePath);
    
                /* Update course record */
                boolean success = dbServices.updateCourseRecord(username, courseRecord);
                if (!success) {
                    /* Revert the change in the course.xml file */
                    course.setName(originalCourseName);
                    marshalToFile(username, course, courseFile.getRelativePathFromRoot(), null);
    
                    /* Revert the change to the course folder name */
                    renameFile(username, updatedCourseSourcePath, originalCourseName);
                    courseFile.getParentTreeModel().setFileOrDirectoryName(originalCourseName);
    
                    /* Revert the change to the course.xml name */
                    renameFile(username, courseFile.getRelativePathFromRoot(),
                            originalCourseName + AbstractSchemaHandler.COURSE_FILE_EXTENSION);
    
                    throw new DetailedException(
                            "Failed to rename the course because there was an error. The name was not changed.",
                            "Unable to update the course metadata in the database with the new name '" + newName
                                    + "'. The course metadata was not changed.",
                            null);
                }
            }
            
            // update any data collections that reference the old course
            DataCollectionServicesInterface dataCollectionService = ServicesManager.getInstance().getDataCollectionServices();
            List<DbDataCollection> dataCollections = 
                    dataCollectionService.getPublishedCoursesOfType(originalCourseSourcePath, DataSetType.COURSE_DATA);
            for(DbDataCollection dataCollection : dataCollections){
                
                // the data collection course folder needs to be updated to the new path
                dataCollection.setCourseFolder(updatedCourseSourcePath);
                
                // the name for 'course data' published course type matches the course name
                dataCollection.setName(newName);  
                dataCollectionService.updateDataCollectionItem(username, dataCollection);                
            }
            
            return renamedCoursePath;

        } catch (FileNotFoundException e) {

            throw new DetailedException(
                    "An error occurred while attempting to rename the course.",
                    "Failed to find the course folder for the course being renamed.",
                    e
            );

        } catch (IOException e) {

            throw new DetailedException(
                    "An error occurred while attempting to rename the course.",
                    "Failed to modify the course file to update the course's internal name.",
                    e
            );
        }
    }

    /**
     * Renames the given file to the new name
     *
     * @param username information used to authenticate the request.
     * @param filePath The path to the file that should be renamed. The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param newName The new name of the file
     * @return true if the operation was successful, false otherwise
     * @throws DetailedException If there was a problem renaming the file.
     */
    public boolean renameFile(String username, String filePath, String newName) throws DetailedException{
        return renameFile(username, filePath, newName, true);
    }

    /**
     * Renames the given file to the new name
     *
     * @param username information used to authenticate the request.
     * @param filePath The path to the file that should be renamed. The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param newName The new name of the file
     * @param updateCourseFolderLastModifiedDate true to update the course folder's last modified date; false otherwise
     * @return true if the operation was successful, false otherwise
     * @throws DetailedException If there was a problem renaming the file.
     */
    public abstract boolean renameFile(String username, String filePath, String newName, boolean updateCourseFolderLastModifiedDate) throws DetailedException;

    /**
     * Unmarshals the XML file according to the schema mapped to the file type.
     *
     * @param username information used to authenticate the request. Can't be null.
     * @param filePath the path to the workspace file to retrieve and unmarshall. Cannot point to a folder.
     * The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @return the Serializable object that represents the XML file
     * @throws DetailedException if there was a problem reading the file
     * @throws FileNotFoundException if the file specified was not found
     * @throws IllegalArgumentException if a parameter specified is not valid
     * @throws UnsupportedVersionException if the file contains a GIFT XML format that is supported by this GIFT instance
     */
    public abstract UnmarshalledFile unmarshalFile(String username, String filePath)
            throws DetailedException, FileNotFoundException, IllegalArgumentException, UnsupportedVersionException;

    /**
     * Marshals the object to the file.
     *
     * @param username information used to authenticate the request.
     * @param serializableObject the generated class contents to write to the file
     * @param file the path file to write too. The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param version the GIFT version of the generated class contents. Can be null. If the version is null, the latest GIFT version will be used.
     * @return whether the contents where schema valid or not.  The file will still be written even if not schema valid.
     * @throws IllegalArgumentException if a parameter specified is not valid
     * @throws FileNotFoundException if the file exists but is a directory rather than a regular file, does not exist but cannot be created, or cannot be opened for any other reason
     * @throws IOException if there was a problem updating the file
     * @throws DetailedException if there was a severe problem marshalling the file
     * @throws WritePermissionException if the user does not have write permission to the file
     */
    public boolean marshalToFile(String username, Serializable serializableObject, String file, String version)
            throws IllegalArgumentException, FileNotFoundException, DetailedException, WritePermissionException{
        return marshalToFile(username, serializableObject, file, version, false);
    }

    /**
     * Marshals the object to the file.
     *
     * @param username information used to authenticate the request.
     * @param serializableObject the generated class contents to write to the file
     * @param file the path file to write too. The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param version the GIFT version of the generated class contents. Can be null. If the version is null, the latest GIFT version will be used.
     * @param useParentAsCourse true to use the parent folder of the file provided as the course folder. This folder is used to update the date modified timestamp. This should always be false if the object being marshalled resides within a course folder.
     * @return whether the contents where schema valid or not.  The file will still be written even if not schema valid.
     * @throws IllegalArgumentException if a parameter specified is not valid
     * @throws FileNotFoundException if the file exists but is a directory rather than a regular file, does not exist but cannot be created, or cannot be opened for any other reason
     * @throws IOException if there was a problem updating the file
     * @throws DetailedException if there was a severe problem marshalling the file
     * @throws WritePermissionException if the user does not have write permission to the file
     */
    public abstract boolean marshalToFile(String username, Serializable serializableObject, String file, String version, boolean useParentAsCourse)
            throws IllegalArgumentException, FileNotFoundException, DetailedException, WritePermissionException;

    /**
     * Create a folder with the given username in the training apps lib directory.
     *
     * @param username information used to authenticate the request.
     * @return the proxy representation of the created folder.
     * @throws DetailedException if there was a problem accessing the parent directory or creating
     *         the new directory
     * @throws IllegalArgumentException if there was a problem with the method arguments: 1) the
     *         parent directory arguments isn't an object 2) the parent directory doesn't exist 3)
     *         the named folder to create already exists (and NOT ignoring existing folder)
     */
    public abstract AbstractFolderProxy createTrainingAppsLibUserFolder(String username)
            throws DetailedException, IllegalArgumentException;

    /**
     * Create a folder with the given name in the specified parent directory.
     *
     * @param username information used to authenticate the request.
     * @param parentDirectoryPath the path to the file or directory where the new folder should be placed.
     * The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param name the name of the folder.  The name of the folder can't be the name of another folder in the parent directory.
     * @param ignoreExistingFolder whether or not to ignore if the folder already exists, i.e. don't create the folder if it already exists and don't thrown an exception.
     * @return the proxy representation of the created folder.
     * @throws DetailedException if there was a problem accessing the parent directory or creating the new directory
     * @throws IllegalArgumentException if there was a problem with the method arguments:
     * 1) the parent directory arguments isn't an object
     * 2) the parent directory doesn't exist
     * 3) the named folder to create already exists (and NOT ignoring existing folder)
     */
    public abstract AbstractFolderProxy createFolder(String username, String parentDirectoryPath, String name, boolean ignoreExistingFolder) throws DetailedException, IllegalArgumentException;

    /**
     * Checks whether the specified file exists already.
     *
     * @param username information used to authenticate the request.
     * @param filePath the path to the file to check. The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param isFolder true if the file to check is a folder
     * @return true if the file exists
     * @throws DetailedException if there was a problem accessing the parent directory
     */
    public abstract boolean fileExists(String username, String filePath, boolean isFolder) throws DetailedException;

    /**
     * Gets the contents of the file as a single string
     *
     * @param username information used to authenticate the request.
     * @param file the from which to get the contents
     * @return a String containing the contents of the file
     * @throws IllegalArgumentException if there was a problem with the method arguments
     * @throws DetailedException if there was a severe problem reading the file
     */
    public abstract String readFileToString(String username, FileTreeModel file) throws IllegalArgumentException, DetailedException;

    /**
     * Updates the content of the file with the new content provided.
     * Note: this will write using UTF-8 encoding.
     *
     * @param username Used to authenticate the request.
     * @param fileModel The file to be updated.
     * @param content The new content of the file.
     */
    public void updateFileContents(String username, FileTreeModel fileModel, String content) throws DetailedException {
        updateFileContents(username, fileModel, content, true);
    }

    /**
     * Updates the content of the file with the new content provided.
     * Note: this will write using UTF-8 encoding.
     *
     * @param username Used to authenticate the request.
     * @param fileModel The file to be updated.
     * @param content The new content of the file.
     * @param createBackup Whether or not to backup the original file.
     */
    public void updateFileContents(String username, FileTreeModel fileModel, String content, boolean createBackup) throws DetailedException {
        updateFileContents(username, fileModel.getRelativePathFromRoot(true), content, createBackup, false);
    }

    /**
     * Updates the content of the file with the new content provided.
     * Note: this will write using UTF-8 encoding.
     *
     * @param username Used to authenticate the request.
     * @param filePath The path to the file to be updated.
     * The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param content The new content of the file.
     * @param createBackup Whether or not to backup the original file.
     * @param useAdminPrivilege Whether or not the operation should be performed with admin privileges
     */
    public abstract void updateFileContents(String username, String filePath, String content, boolean createBackup, boolean useAdminPrivilege) throws DetailedException;

     /**
     * Return the list of SIMILE concept names found in the SIMILE configuration file provided.
     * This assumes the SIMILE configuration file is in the course root folder (e.g. Domain folder).
     *
     * @param username information used to authenticate the request.
     * @param simileConfigFile the SIMILE configuration file to read and retrieve concept names from
     * @return List<String> the list of SIMILE concept names from that file
     * @throws FileNotFoundException if the file could not be found
     * @throws IllegalArgumentException if there was a problem with the contents of the file
     * @throws DetailedException if there was a severe problem reading the configuration file
     */
    public abstract List<String> getSIMILEConcepts(String username, FileTreeModel simileConfigFile)
            throws FileNotFoundException, IllegalArgumentException, DetailedException;

    /**
     * Export the collection of courses to a zip file with the given name.
     *
     * @param exportProperties contains all the information required to execute the GIFT course export logic
     * @return DownloadableFileRef the result of the export
     * @throws DetailedException if there was a problem retrieving the GIFT files to export or creating the export zip
     * @throws URISyntaxException if there was a problem build a URI to access the files
     */
    public abstract DownloadableFileRef exportCourses(ExportProperties exportProperties) throws DetailedException, URISyntaxException;
    
    /**
     * Export the course data for the course referenced in the export properties.
     * 
     * @param exportProperties contains information necessary to perform the export such as the course reference.  Currently
     * only a single course is supported by this course data export.
     * @return the information needed by the client in order to download the exported course data (e.g. zip file). Can
     * be null if the zip wasn't created because there was no data found, probably because no attempts have been made
     * to take this course yet.
     * @throws DetailedException if there was a problem with the export properties, a database operation, or the database
     * said there were logs but they couldn't be found.
     * @throws IOException if there was a problem finding a file on disk
     */
    public DownloadableFileRef exportCourseData(ExportProperties exportProperties) throws DetailedException, IOException{
        
        if(exportProperties.getCoursesToExport().isEmpty()){
            throw new DetailedException("Failed to export the course data on behalf of "+exportProperties.getUsername()+".", 
                    "No course was specified", null);
        }
        
        DomainOption course = exportProperties.getCoursesToExport().get(0);
        
        // query UMS db to get domain session output folder(s)
        final DbServicesInterface dbServices = ServicesManager.getInstance().getDbServices();
        CourseRecord courseRecord = dbServices.getCourseByPath(course.getDomainId());
        
        if(courseRecord == null){
            // create the course record - this is useful when the course might have been manually copied to the workspace bypassing
            //                            any course import logic.  Something that is often done in desktop mode.
            courseRecord = dbServices.createCourseRecordIfNeeded(exportProperties.getUsername(), course.getDomainId(), course.isDomainIdWritable());
            if(courseRecord == null){
                throw new DetailedException("Failed to export the course data on behalf of "+exportProperties.getUsername()+" for '"+course.getDomainName()+"'.", 
                        "The course was not found in the database of courses that have been created and imported.", null);
            }
        }
        
        // CHECK: is user the owner of the course
        if(!courseRecord.isPublicOwner() && 
                !courseRecord.getOwnerName().equalsIgnoreCase(exportProperties.getUsername())){
            throw new DetailedException("Failed to export the course data on behalf of "+exportProperties.getUsername()+" for '"+course.getDomainName()+"'.", 
                   "The user "+exportProperties.getUsername()+" is not the owner of the course.", null);
        }
        
        exportProperties.getProgressIndicator().setTaskDescription("Retrieving domain session log file names..");
        exportProperties.getProgressIndicator().setPercentComplete(5);
        
        List<File> dsSpecificFolders = new ArrayList<>();
        List<String> logFileNames;
        try{
            logFileNames = dbServices.getCourseDomainSessionLogFileNames(courseRecord);
        }catch(Exception e){
            throw new DetailedException("Failed to export the course data on behalf of "+exportProperties.getUsername()+" for '"+course.getDomainName()+"'.", 
                    "There was a problem retrieving the list of domain session log file names from the database.", e);
        }
        if(!logFileNames.isEmpty()){
            
            exportProperties.getProgressIndicator().setTaskDescription("Retrieving domain session log files..");
            exportProperties.getProgressIndicator().setPercentComplete(10);
            
            // find domain session output folder for each domain session log
            // Note: make sure it isn't the root output folder.
            File dsOutputFolder = new File(PackageUtil.getDomainSessions());
            DesktopFolderProxy dsOutFolderProxy = new DesktopFolderProxy(dsOutputFolder);
            List<FileProxy> logFiles = new ArrayList<>();
            for(String logFileName : logFileNames){
                FileFinderUtil.getFilesByName(dsOutFolderProxy, logFiles, logFileName);
            }
            
            if(logFiles.isEmpty()){
                // warning - to handle the case when the logs were deleted but the database was never told
                logger.warn("No output files could be found when exporting the course data on behalf of "+exportProperties.getUsername()+" for '"+course.getDomainName()+"'.");
                return null;
            }
            
            float originalTaskProgress = exportProperties.getProgressIndicator().getPercentComplete();
            int currentFileCount = 0;
            for(FileProxy logFileProxy : logFiles){      
                currentFileCount++;
                File logFile = new File(logFileProxy.getFileId());
                dsSpecificFolders.add(logFile.getParentFile());
                float increaseAmt = ((float)currentFileCount / (float)logFiles.size()) * 40;
                exportProperties.getProgressIndicator().setPercentComplete((int)(originalTaskProgress + increaseAmt));
            }
        }else{
            // prevent creating zip, instead just notify the user there is nothing to export
            return null;
        }

        //
        // create zip in temp directory
        //
        
        exportProperties.getProgressIndicator().setTaskDescription("Creating zip with "+dsSpecificFolders.size()+" sessions.");
        exportProperties.getProgressIndicator().setPercentComplete(50);
        
        //create a temporary directory to place database export files that are created
        File tempDbExportDirectory = File.createTempFile("GIFT-CourseDataExport", Long.toString(System.nanoTime()));
        tempDbExportDirectory.delete();
        
        if(!ZipUtils.createDir(tempDbExportDirectory)){
           throw new IOException("Unable to create directory named "+tempDbExportDirectory+".");
        }
        
        //create zip where the export contents will be placed
        File exportZipFile = new File(EXPORT_DIRECTORY.getAbsolutePath() + File.separator + exportProperties.getExportFileName() + Constants.ZIP);
        exportZipFile.deleteOnExit();
        
        //zip logs
        ZipUtils.zipFiles(dsSpecificFolders, exportZipFile);
        
        //build download URL
        URL exportURL = ServicesManager.getExportURL(exportZipFile);
        
        exportProperties.getProgressIndicator().setTaskDescription("Completed exporting course data");
        exportProperties.getProgressIndicator().setPercentComplete(100);
        
        // provide zip URL
        return new DownloadableFileRef(exportURL.toString(), exportZipFile.getPath());
    }

    /**
     * Import the GIFT export zip file that contains courses.
     *
     * @param username the user performing the import and whose account the courses will be associated with
     * @param giftExportZipToImport a GIFT export zip file to import.  This must be a course only export.
     * @param progressIndicator used to notify the user performing the import of progress.  When the import is finished,
     * it will contain the list of courses imported.
     * @param filesToOverwrite used to overwrite files in case of conflict. Can be null.
     * @param courseToNameMap A map of conflicting course paths (i.e. Domain/courseName/courseName.course.xml) to their new names
     * @throws DetailedException if there was a problem reading the zip or importing the files into this GIFT instance
     */
    public abstract void importCourses(String username, File giftExportZipToImport, LoadedProgressIndicator<List<DomainOption>> progressIndicator, List<String> filesToOverwrite, Map<String, String> courseToNameMap) throws DetailedException;

    /**
     * Return the estimated size of the course export.
     *
     * @param exportProperties contains all the information required to execute the GIFT course export logic
     * @return the size in MB of the course export (before compression)
     * @throws DetailedException if there was a problem determining the size of the course export
     * @throws URISyntaxException if there was a problem build a URI to access the files
     */
    public abstract float getCourseExportSize(ExportProperties exportProperties) throws DetailedException, URISyntaxException;

    /**
     * Return the file proxy for the given file path. The file path given should not have the
     * platform specific workspaces root prefixes (e.g. "Workspaces/" or "workspaces/"). Examples
     * of valid paths should start with the public folder or the user folder (e.g.
     * Public/ExampleCourse/courseImage.jpg)
     * @param filename The file path of the file to to retrieve. Should be relative to the root of the
     * location of the workspaces folder
     * @param username The username of the user requesting the file. Used for authentication
     * @return the new file proxy for the specified file
     * @throws IllegalArgumentException if there was a problem with one of the arguments of this method
     * @throws FileNotFoundException if the specified file was unable to be found
     * @throws DetailedException if there was a problem retrieving the file.
     */
    public abstract FileProxy getFile(String filename, String username)
            throws IllegalArgumentException, FileNotFoundException, DetailedException;

    /**
     * Return the file proxy for the given file model.
     *
     * @param file the file model to retrieve a file proxy for.  Can't be null and must exist.
     * @param username used for authentication
     * @return the new file proxy for that file model.
     * @throws IllegalArgumentException if there was a problem with one of the arguments to this method
     * @throws FileNotFoundException if the file could not be found
     * @throws DetailedException if there was a problem retrieving the file
     */
    public FileProxy getFile(FileTreeModel file, String username)
            throws IllegalArgumentException, FileNotFoundException, DetailedException {

        if(file.isDirectory()){
            throw new IllegalArgumentException("The file can't be a directory.");
        }

        String filename = file.getRelativePathFromRoot(true);

        return getFile(filename, username);
    }

    /**
     * Return the number of MegaBytes the user has left to use (i.e. when authoring GIFT courses) based
     * on the disk quota given to them.
     *
     * @param username used for authentication and retrieval of the user's remaining workspace disk quota
     * @return the MegaBytes available to the user.  Long.MAX_VALUE is returned for unlimited.
     * @throws DetailedException if there was a problem retrieving the remaining disk space for the user
     */
    public abstract long getRemainingWorkspacesQuota(String username) throws DetailedException;

    /**
     * Checks a GIFT export zip file to import for file conflicts.
     *
     * @param username the username of the user for whom the operation is being invoked
     * @param giftExportZipToImport a GIFT export zip file to import.  This must be a course only export.
     * @param progressIndicator used to notify the user performing the import of progress
     * @return a map of new files to existing files. Can be null if no conflicts found.
     */
    public abstract Map<File, File> checkForImportConflicts(String username, File giftExportZipToImport, ProgressIndicator progress) throws DetailedException;

    /**
     * Updates the progress indicator.
     *
     * @param progress The progress indicator to update. Can be null.
     * @param description The task description to set. Can be null.
     * @param percentComplete An amount to increase the progress bar by.
     * If negative, the progress bar will be set to 0.
     */
    protected void updateProgress(ProgressIndicator progress, String description, int percentComplete) {
        if(progress != null) {
            if(description != null) {
                progress.setTaskDescription(description);
            }

            if(percentComplete >= 0) {
                progress.increasePercentComplete(percentComplete);
            } else {
                progress.setPercentComplete(0);
            }
        }
    }

    /**
     * Updates the permissions for a single user on the given course
     *
     * @param username the username of the logged in user for authentication
     * @param permissions the permissions to set, one entry per user, can have a null permission enum to clear a user's permissions.
     * @param courseData contains information about the course which permissions are changing
     * @param userSessionId the user session id from the client
     * @param progressIndicator used to provide progress on updating course permissions.  Can be null.  Updates progress from 0 to 100 percent.
     * @throws IllegalArgumentException if any of the arguments are not properly provided.
     * @throws DetailedException if the permission update failed
     */
    public abstract void updateCourseUserPermissions(String username, Set<DomainOptionPermissions> permissions, 
            DomainOption courseData, String userSessionId, ProgressIndicator progressIndicator)
            throws IllegalArgumentException, DetailedException;

    /**
     * Gets the Slide Shows folder within a course folder.
     *
     * @param username The username
     * @param courseFolderName The name of the course folder
     * @return The FileTreeModel of the 'Slide Shows' folder that is created by GIFT when creating the images for a slide show
     * course object. E.g. [Desktop] will be pointing to the 'Slide Shows' folder that GIFT creates to place slide show images in.
     * The ancestor parent file tree model might look like 'workspace/mhoffman/test/' where 'test'
     * is the course folder name.
     * @throws IllegalArgumentException if there was a problem with one of the arguments to this method
     */
    public FileTreeModel getSlideShowsFolder(String username, String courseFolderName) throws IllegalArgumentException {

        if(username == null){
             throw new IllegalArgumentException("The username can't be null.");
         }

        if(courseFolderName == null) {
            throw new IllegalArgumentException("The course folder name can't be null.");
        }

        return getRootTree(username).getModelFromRelativePath(courseFolderName + File.separator + SLIDE_SHOWS_FOLDER_NAME);
    }

    /**
     * Converts a PowerPoint file to a series of images.
     *
     * @param username The username
     * @param browserSessionKey The unique browser identifier of the browser that is making the request
     * @param courseFolderPath The path to the course folder
     * @param newFolderName The name of the folder to store the images in
     * @param pptFilePath The path to the PowerPoint file
     * @param replaceExisting True if the existing Slide Show folder should be replaced by the new folder.
     * @param progress The progress indicator for this operation
     *
     * @return The FileTreeModel of the folder containing the images.
     * @throws DetailedException
     */
    public FileTreeModel convertPptToSlideShow(String username, String browserSessionKey, String courseFolderPath, String newFolderName, String pptFilePath, boolean replaceExisting, ProgressIndicator progress) throws DetailedException {

        String tempFolderPath;
        File tempFolder = null;

        // Get the path to the temp directory where the PowerPoint file is located

        if(!pptFilePath.startsWith("temp")) {
            // The PowerPoint file is already in the user's workspace

            // Create a temp folder where the PowerPoint & converted images will be created
            tempFolderPath = CommonProperties.getInstance().getUploadDirectory() + File.separator + UUID.randomUUID();
            File tempDestinationDir = new File(tempFolderPath);
            tempDestinationDir.mkdir();

            try {
                // Copy the PowerPoint file to the temp directory so it can be converted later

                // Remove "workspaces" string from the beginning of the path
                String workspaceRelativePath = pptFilePath.substring(pptFilePath.indexOf(Constants.FORWARD_SLASH) + 1);

                FileTreeModel pptFileModel = new FileTreeModel(workspaceRelativePath);
                FileProxy pptFileProxy = getFile(pptFileModel, username);
                File tempPptFile = new File(tempFolderPath + File.separator + pptFileProxy.getName());
                FileUtils.copyInputStreamToFile(pptFileProxy.getInputStream(), tempPptFile);
                pptFilePath = tempPptFile.getPath();

                tempFolderPath += File.separator + newFolderName;

            } catch (IllegalArgumentException | IOException e) {
                throw new DetailedException("An error occured while creating the slide show",
                        "There was a problem retrieving the PowerPoint file: " + e.getMessage(), e);
            }

        } else {
            // The PowerPoint has been uploaded to the temp directory. Get the UUID folder

            pptFilePath = pptFilePath.replace(Constants.FORWARD_SLASH, File.separator);
            pptFilePath = CommonProperties.getInstance().getDomainDirectory() + File.separator + pptFilePath;
            tempFolderPath = pptFilePath.substring(0, pptFilePath.lastIndexOf(File.separator) + 1) + newFolderName;
        }

        // Create a path where the PowerPoint images will be created
        tempFolder = new File(tempFolderPath);
        tempFolder.mkdir();
        updateProgress(progress, null, 20);
        
        SlideShow<?,?> ppt = null;
        // change ppt file to slide show depending on which type of file is given, pps, pptx, ppsx
        try
        {
            if (pptFilePath.endsWith(Constants.ppt_show_supported_types[0])){
                ppt = new HSLFSlideShow(new FileInputStream(pptFilePath));
				
            } else{
                ppt = new XMLSlideShow(new FileInputStream(pptFilePath));
            }
            
            Dimension slideSize = ppt.getPageSize();
            List<?> slides = ppt.getSlides();
            
            if(ppt.getSlides().isEmpty()) {
                throw new IllegalArgumentException("The PowerPoint show cannot be empty. Please select a PowerPoint show with at least one slide.");
            }
        
            int count = 1;
            int complete = 100/slides.size();
            for (int i = 0; i < slides.size(); i++) {
                progress.updateSubtaskDescription("Converting slide " + count + " of " + slides.size());
                updateProgress(progress.getSubtaskProcessIndicator(), null, complete);

                BufferedImage img = new BufferedImage(slideSize.width, slideSize.height, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = img.createGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

                graphics.fill(new Rectangle2D.Float(0, 0, slideSize.width, slideSize.height));
                
                int slideNumber = 0;
                Object slide = slides.get(i);
                if(slide instanceof HSLFSlide) {
                    HSLFSlide rawSlide = (HSLFSlide) slide;
                    rawSlide.draw(graphics);
                    slideNumber = rawSlide.getSlideNumber();
                    
                } else if(slide instanceof XSLFSlide){
                    XSLFSlide rawSlide = (XSLFSlide) slide;
                    rawSlide.draw(graphics);
                    slideNumber = rawSlide.getSlideNumber();
                } else {
                    throw new  IllegalArgumentException("An unknown type of PowerPoint file was provided and could not be"
                            + "converted to slides");
                }

                String imgName = "Slide" + (slideNumber < 10 ? "0" : "") + slideNumber + ".png";
                FileOutputStream out = new FileOutputStream(tempFolderPath + File.separator + imgName);
                ImageIO.write(img, "png", out);
                out.close();

                count++;
            }

            String targetPath = courseFolderPath + File.separator + SLIDE_SHOWS_FOLDER_NAME + File.separator + newFolderName;

            if(replaceExisting) {
                deleteFile(username, browserSessionKey, targetPath, null);
            }

            createFolder(username, courseFolderPath, "Slide Shows", true);
            createFolder(username, courseFolderPath + Constants.FORWARD_SLASH + "Slide Shows", newFolderName, true);

            updateProgress(progress, "Copying Slides to the course folder...", 30);
            progress.updateSubtaskDescription("Uploading slides...");
            updateProgress(progress.getSubtaskProcessIndicator(), null, -1);

            count = 1;
            complete = 100/tempFolder.listFiles().length;
                for(File file : tempFolder.listFiles()) {
                    progress.updateSubtaskDescription("Uploading slide " + count + " of " + tempFolder.listFiles().length);
                    updateProgress(progress.getSubtaskProcessIndicator(), null, complete);
                    FileTreeModel fileModel = new FileTreeModel(file.getParent());
                    fileModel = fileModel.getModelFromRelativePath(file.getName());
                    copyDomainFile(username, fileModel.getRelativePathFromRoot(), targetPath, NameCollisionResolutionBehavior.OVERWRITE, null);
                    count++;
                }

                updateProgress(progress.getSubtaskProcessIndicator(), null, complete);
                return getRootTree(username).getModelFromRelativePath(targetPath);

        } catch (Throwable e) {
                throw new DetailedException("There was a problem while creating the slide show.\n\nWas the PowerPoint file you provided saved as a 'PowerPoint 97-2003 Show (.pps, .ppsx, or .pptx)'?\n<b>Note:</b> You can not simply rename the file extension, you must save it as a .pps, .ppsx, or pptx file type in PowerPoint.",
                    "The server threw an error:\n" + e.getMessage(), e);
        } finally {
            if(tempFolder.exists()) {
                try {
                    FileUtils.deleteDirectory(tempFolder.getParentFile());
                } catch (IOException e) {
                    logger.error("Failed to cleanup the temp folder '" + tempFolder.getPath() +"' that was created when converting a slide show for "+username, e);
                }
            }
            
            if (ppt != null) {
                try {
                    ppt.close();
                } catch (IOException e) {
                    logger.error("Failed to close a PowerPoint show when converting a slide show", e);
                }
            }
        }
    }

    /**
     * Unzips an archive (ZIP) file into a folder containing the files extracted from it.
     *
     * @param username The username of the user invoking this method. Used to invoke file operations that require user clearance. Cannot be null.
     * @param courseFolderPath The path to the course folder where the folder containing the extracted files should be placed. Cannot be null.
     * @param newFolderName The name of the course subfolder to place the extracted files in. Cannot be null;
     * @param zipFilePath The path to the archive (ZIP) file being unzipped. Cannot be null.
     * @param progress The progress indicator for this operation. Cannot be null.
     *
     * @return A {@link FileTreeModel} representing the folder the files were extracted to and all the files inside of it.
     * @throws DetailedException if an error occurrs while unzipping the file or moving its extracted files to the new folder
     */
    public FileTreeModel unzipFile(String username, String courseFolderPath, String newFolderName, String zipFilePath, ProgressIndicator progress) throws DetailedException {

        if(username == null){
            throw new IllegalArgumentException("The username of the user invoking this action cannot be null");
        }

        if(courseFolderPath == null){
            throw new IllegalArgumentException("The path to the course folder where the folder containing the extracted files should be placed cannot be null");
        }

        if(zipFilePath == null){
            throw new IllegalArgumentException("The path to the .zip file to extract from cannot be null");
        }

        if(newFolderName == null){
            throw new IllegalArgumentException("The name of the course subfolder where the contents of the .zip should be extracted to cannot be null");
        }

        if(progress == null){
            throw new IllegalArgumentException("The progress indicator for the unzipping operation cannot be null");
        }

        String tempFolderPath;
        File tempFolder = null;

        // Get the path to the temp directory where the zip file is located

        if(!zipFilePath.startsWith("temp")) {
            // The zip file is already in the user's workspace

            // Create a temp folder where the zip and extracted files will be created
            tempFolderPath = CommonProperties.getInstance().getUploadDirectory() + File.separator + UUID.randomUUID();
            File tempDestinationDir = new File(tempFolderPath);
            tempDestinationDir.mkdir();

            try {
                // Copy the zip file to the temp directory so it can be converted later

                // Remove "workspaces" string from the beginning of the path
                String workspaceRelativePath = zipFilePath.substring(zipFilePath.indexOf(Constants.FORWARD_SLASH) + 1);

                FileTreeModel zipFileModel = new FileTreeModel(workspaceRelativePath);
                FileProxy zipFileProxy = getFile(zipFileModel, username);
                File tempZipFile = new File(tempFolderPath + File.separator + zipFileProxy.getName());
                FileUtils.copyInputStreamToFile(zipFileProxy.getInputStream(), tempZipFile);
                zipFilePath = tempZipFile.getPath();

                tempFolderPath += File.separator + newFolderName;

            } catch (IllegalArgumentException | IOException e) {
                throw new DetailedException("An error occured while unzipping the file",
                        "There was a problem retrieving the zip file: " + e.getMessage(), e);
            }

        } else {
            // The zip has been uploaded to the temp directory. Get the UUID folder

            zipFilePath = zipFilePath.replace(Constants.FORWARD_SLASH, File.separator);
            zipFilePath = CommonProperties.getInstance().getDomainDirectory() + File.separator + zipFilePath;
            tempFolderPath = zipFilePath.substring(0, zipFilePath.lastIndexOf(File.separator) + 1) + newFolderName;
        }

        // Create a path where the PowerPoint images will be created
        tempFolder = new File(tempFolderPath);
        tempFolder.mkdir();
        updateProgress(progress, null, 20);

        try {

            if(progress.getSubtaskProcessIndicator() == null){
                progress.setSubtaskProgressIndicator(new ProgressIndicator());
            }

            // Unzip entire zip folder
            progress.updateSubtaskDescription("Extracting");
            ZipUtils.unzipArchive(new File(zipFilePath), tempFolder, progress.getSubtaskProcessIndicator());
            progress.updateSubtaskDescription("Extracted");

            String targetPath = courseFolderPath + File.separator + newFolderName;

            //create the new folder where the unity project will be placed in the course folder (the parent to this new directory)
            AbstractFolderProxy targetFolder = createFolder(username, courseFolderPath, newFolderName, true);

            updateProgress(progress, "Copying extracted files to the course folder...", 30);
            progress.updateSubtaskDescription("Uploading extracted files...");
            updateProgress(progress.getSubtaskProcessIndicator(), null, -1);

            int count = 1;
            int numFiles = FileUtil.countFilesInDirectory(tempFolder);
            int complete = 100 / numFiles;
            for (File file : tempFolder.listFiles()) {

                progress.updateSubtaskDescription("Uploading extracted file " + count + " of " + numFiles);
                progress.setSubtaskProgress(count/numFiles);

                count = copyToWorkspaceFolder(file, targetFolder, progress.getSubtaskProcessIndicator(), count, numFiles);
            }

            updateProgress(progress.getSubtaskProcessIndicator(), null, complete);
            return getRootTree(username).getModelFromRelativePath(targetPath);

        } catch (Throwable e) {
            throw new DetailedException("An error occured while unzipping the file",
                    "There was a problem unzippinng the file: " + e.getMessage(), e);
        } finally {
            if(tempFolder.exists()) {
                try {
                    FileUtils.deleteDirectory(tempFolder.getParentFile());
                } catch (IOException e) {
                    logger.error("Failed to cleanup the temp folder '" + tempFolder.getPath() +"' that was created when unzipping a file for "+username, e);
                }
            }
        }
    }

    /**
     * Copies a {@link File} and any of its potential descendants into the given workspace folder
     *
     * @param file the file to be copied into the workspace folder. If this file is a directory, its descendants will
     * be copied as well. Cannot be null.
     * @param targetWorkspaceFolder the workspace folder where the file should be copied to. Cannot be null.
     * @param an optional progress indicator used to track progress as files are copied. Can be null.
     * @param count the number of files that have been copied so far. This is only used for progress calculation and is ignored otherwise.
     * @param numFiles the total number of files to copy. This is only used for progress calculation and is ignored otherwise.
     * @throws IOException if an exception occurs while copying the file or its descendants
     */
    private int copyToWorkspaceFolder(File file, AbstractFolderProxy workspaceFolder, ProgressIndicator progress, int count, int numFiles) throws IOException{

        if(file == null){
            throw new IllegalArgumentException("The file to copy to the workspace cannot be null.");
        }

        if(workspaceFolder == null){
            throw new IllegalArgumentException("The workspace folder to copy to cannot be null.");
        }

        int filesCopied = count;

        if(file.isDirectory()){

            AbstractFolderProxy newFolder = workspaceFolder.createFolder(file.getName());

            if(file.listFiles() != null){

                for(File child : file.listFiles()){
                    filesCopied = copyToWorkspaceFolder(child, newFolder, progress, filesCopied, numFiles);
                }
            }

        } else {

            if(progress != null){

                //update the progress as files are copied
                progress.setTaskDescription("Uploading extracted file " + filesCopied + " of " + numFiles);
                progress.setPercentComplete(filesCopied * 100/numFiles);
            }

            workspaceFolder.createFile(file, null);

            filesCopied++;
        }

        return filesCopied;
    }

    /**
     * Return the course XML file reference found in the course folder.
     *
     * @param courseFolder the course folder to find the course XML file in
     * @return the reference to the course XML file.
     * @throws DetailedException if there is a problem searching the course folder or when only one course file is not found.
     */
    public FileProxy getCourseXMLFile(AbstractFolderProxy courseFolder) throws DetailedException{

        //find course file
        List<FileProxy> courseFiles = new ArrayList<>();
        try{
            FileFinderUtil.getFilesByExtension(courseFolder, courseFiles, AbstractSchemaHandler.COURSE_FILE_EXTENSION);
        }catch(IOException e){
            throw new DetailedException("Failed to validate course folder.", "There was a problem searching the course folder named '"+courseFolder.getName()+"' for course.xml files : "+e.getMessage()+".", e);
        }

        if(courseFiles.isEmpty()){
            throw new DetailedException("Failed to validate course folder", "The course folder named '"+courseFolder.getName()+"' contains no course.xml files.", null);
        }else if(courseFiles.size() > 1){
            throw new DetailedException("Failed to validate course folder", "The course folder named '"+courseFolder.getName()+"' contains more than one course.xml file, therefore it violates the course folder requirements of having only one course.", null);
        }

        return courseFiles.get(0);
    }

    /**
     * Check whether the given user has permissions to modify and write to the workspace file at the given location
     *
     * @param username The username of the user invoking this method. Used to invoke file operations that require user clearance. Cannot be null.
     * @param filePath the path to the workspace file to check for permissions.
     * The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @return whether or not the given user has write permissions for the file at the given location
     * @throws DetailedException if an exception occurs while retrieving the file or checking if the user can modify it
     */
    public abstract boolean hasWritePermissions(String username, String filePath) throws DetailedException;

    /**
     * Return the folder proxy that represents the folder at the provided file location.
     *
     * @param file the file that represents the folder being returned.
     * @param username used for authentication.
     * @return the new folder proxy that is the folder provided by the file's path.
     */
    public abstract AbstractFolderProxy getFolderFromFile(File file, String username);

    /**
     * Return the folder proxy that represents the folder at the provided path.
     *
     * @param folderPath the path to the directory. The root of the path must be a workspace
     *        sub-folder (e.g. Public, &lt;username&gt;).
     * @param username used for authentication.
     * @return the new folder proxy that is the folder provided by the path.
     */
    public abstract AbstractFolderProxy getFolder(String folderPath, String username);

    /**
     * Removes the workspace from the file path. This is used to get a path relative to the
     * workspace folder. If the path does not contain the workspace folder, then the original path
     * will be returned. (e.g. File path:
     * 'C:\work\GIFT_base\branches\Domain\workspace\Public\TrainingAppsLib\Public\Folder1\abc.txt'
     * will be returned as 'Public\TrainingAppsLib\Public\Folder1\abc.txt'.)
     *
     * @param pathToTrim the path to trim the workspace path from.
     * @param username used for authentication.
     * @return the path of the file relative to the workspace directory. If the file does not exist
     *         within the workspace directory, then the original path is returned.
     */
    public abstract FileTreeModel trimWorkspaceFromPath(String pathToTrim, String username);
    
    /**
     * Remove the workspaces path from the full path provided in order to provide a workspace
     * path that unique identifies the file in the workspaces.<br/>
     * Example: <br/>
     * E:\work\GIFT\ARL SVN\branches\paradataImprove_4473\Domain\workspace\Public\Simple iCAP Course Example\Rule Content\B\Low Motivation Journeyman B - Rule.metadata.xml<br/>
     * to <br/>
     * \Public\Simple iCAP Course Example\Rule Content\B\Low Motivation Journeyman B - Rule.metadata.xml<br/>
     * <br/>
     * \default-domain\workspaces\Public\Simple iCAP Course Example\Rule Content\B\Low Motivation Journeyman B - Rule.metadata.xml<br/>
     * to <br/>
     * \Public\Simple iCAP Course Example\Rule Content\B\Low Motivation Journeyman B - Rule.metadata.xml<br/>
     * 
     * @param fullFilePathToTrim
     * @return
     */
    public abstract String trimWorkspacesPathFromFullFilePath(String fullFilePathToTrim);

    /**
     * Return the training apps lib directory for this GIFT instance. This directory contains
     * training applications.
     *
     * @param username information used to authenticate the request.
     * @return the {@link FileTreeModel} instance. This will be a file not a directory file tree
     *         model representation (meaning the model contains no children).
     */
    public FileTreeModel getTrainingAppsLibraryTreeModel(String username) {

        if (trainingAppsLibTreeModel == null) {
            trainingAppsLibTreeModel = getRootTree(username).getModelFromRelativePath(
                    PUBLIC_WORKSPACE_FOLDER_NAME + File.separator + TRAINING_APPS_LIB_FOLDER_NAME);
        }

        return trainingAppsLibTreeModel;
    }

    /**
     * Return the public folder within the training apps lib directory for this GIFT instance. This
     * directory contains training applications that are not associated with any specific user.
     *
     * @param username information used to authenticate the request.
     * @return the {@link FileTreeModel} instance. This will be a file not a directory file tree
     *         model representation (meaning the model contains no children).
     */
    public FileTreeModel getPublicTrainingAppsLibraryTreeModel(String username) {

        if (publicTrainingAppsLibTreeModel == null) {
            publicTrainingAppsLibTreeModel = getTrainingAppsLibraryTreeModel(username)
                    .getModelFromRelativePath(PUBLIC_TRAINING_APPS_LIB_FOLDER_NAME);
        }

        return publicTrainingAppsLibTreeModel;
    }

    /**
     * Retrieves the user folder path from workspace\Public\TrainingAppsLib based on the provided
     * username.
     *
     * @param username the user attempting to retrieve the TrainingAppsLib user folder.
     * @param createFolder true to create the user folder if it doesn't exist; false to do nothing.
     * @return the folder path to the user folder.
     */
    public FileTreeModel getTrainingAppsLibUserFolder(String username, boolean createFolder) {
        // get training apps lib folder
        FileTreeModel taLibsFolder = getTrainingAppsLibraryTreeModel(username);

        FileTreeModel userFolder;
        // this should only be true if in Desktop Mode
        if (StringUtils.equalsIgnoreCase(username, GIFT_WRAP_DESKTOP_USER)) {
            userFolder = getPublicTrainingAppsLibraryTreeModel(username);

            // check if Public directory exists, if not then create it
            if (createFolder && !fileExists(username, userFolder.getRelativePathFromRoot(true), true)) {
                createFolder(username, taLibsFolder.getRelativePathFromRoot(true), userFolder.getFileOrDirectoryName(),
                        false);
            }
        } else {
            userFolder = taLibsFolder.getModelFromRelativePath(username);

            // check if user directory exists, if not then create it
            if (createFolder && !fileExists(username, userFolder.getRelativePathFromRoot(true), true)) {
                createTrainingAppsLibUserFolder(username);
            }
        }

        return userFolder;
    }

    /**
     * Return the folder proxy that represents the ancestor course folder for the file provided.  If the file provided
     * is a folder it is checked as well.
     *
     * @param filePath the path to the file or directory to start from when searching for an ancestor course folder
     * (which could be the directory provided).The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param username used for authentication
     * @return the new folder proxy that is the course folder to the file provided
     * @throws FileNotFoundException if the file provided could not be found
     * @throws DetailedException if there was a problem retrieving the course folder or the course folder could not be found
     */
    public abstract AbstractFolderProxy getCourseFolder(String filePath, String username) throws FileNotFoundException, DetailedException, DetailedException;
    
    /**
     * Return the workspace directory for this GIFT instance.  This directory contains
     * courses but doesn't include the Imports, Exports, templates or runtime directories.
     * 
     * @param username used for authentication, if needed.  Can't be null or empty.
     * @return the folder proxy instance<br/>
     * Desktop - Domain/workspaces/
     * @throws IOException if there was a problem building the folder proxy
     */
    public abstract AbstractFolderProxy getWorkspaceFolderProxy(String username) throws IOException;

    /**
     * Gets a model of all the files contained by the workspace folder folder at the given path, as well as the folder itself and its parents.
     * The models for the target file and its subfolders will be recursively populated with models for all the files found within them in
     * the file system, while the parent models will only contain files from the path itself. If the file at the given path is not a folder,
     * then only the file itself and its parents will be returned in the result. If no file exists at the given
     * path, then null will be returned.
     * <br/><br/>
     * This method can be used to explore the contents of a particular workspace folder without getting a model of
     * the <i>entire</i> workspace like in {@link #getRootTree(String)} or similar methods, which can help improve
     * performance when a model of the entire workspace is not needed.
     *
     * @param username the username of the user invoking this method. Used to invoke file operations that require user clearance. Cannot be null.
     * @param filePath the path to the workspace file to get a file tree for.
     * The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @return a workspace-relative file tree model of the target file including its parents and all of its children
     * or null, if no file exists at the provided path
     */
    public abstract FileTreeModel getFileTreeByPath(String username, String filePath) throws DetailedException;

    /**
     * Copies a workspace file or folder from the given source path to the workspace location at the given destination path.
     *
     * If source and destination are both files, then the destination file's content will be replaced with the
     * source file's content.
     *
     * If the source and destination are both directories, then the source file and all of its descendant
     * files and folders will be copied to into the destination folder..
     * </br><br/>
     * Example:</br>
     * a/my.pps, b/</br>
     * source = a/my.pps, destination = b/, return = b/my.pps</br>
     *
     * @param username information used to authenticate the request.
     * @param sourcePath the path to the file to copy. Can be a file or directory.
     * The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param destinationPath where to copy the file too.  Can be a file or directory.
     * The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param nameCollisionResolutionBehavior the resolution to occur if there is a naming conflict
     *        while copying the file or folder.
     * @param progress the progress indicator for the copy operation. Can be null.
     * @return String the file name including path of the copied file location
     * @throws FileExistsException if a file with the same file name as the source exists at the destination and overwriting is not enabled
     * @throws DetailedException if there was a severe problem copying the file
     * @throws IllegalArgumentException if there was a problem with the method arguments
     */
    public String copyWorkspaceFile(String username, String sourcePath, String destinationPath, NameCollisionResolutionBehavior nameCollisionResolutionBehavior, ProgressIndicator progress)
            throws FileExistsException, IllegalArgumentException, DetailedException {

        return copyFile(username, sourcePath, destinationPath, nameCollisionResolutionBehavior, progress, false, false);
    }

    /**
     * Uses ADMIN privileges to copy a workspace file or folder from the given source path to the workspace location at the given destination path.
     *
     * If source and destination are both files, then the destination file's content will be replaced with the
     * source file's content.
     *
     * If the source and destination are both directories, then the source file and all of its descendant
     * files and folders will be copied to into the destination folder..
     * </br><br/>
     * Example:</br>
     * a/my.pps, b/</br>
     * source = a/my.pps, destination = b/, return = b/my.pps</br>
     *
     * @param username information used to authenticate the request.
     * @param sourcePath the path to the file to copy. Can be a file or directory.
     * The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param destinationPath where to copy the file too.  Can be a file or directory.
     * The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param nameCollisionResolutionBehavior the resolution to occur if there is a naming conflict
     *        while copying the file or folder.
     * @param progress the progress indicator for the copy operation. Can be null.
     * @return String the file name including path of the copied file location
     * @throws FileExistsException if a file with the same file name as the source exists at the destination and overwriting is not enabled
     * @throws DetailedException if there was a severe problem copying the file
     * @throws IllegalArgumentException if there was a problem with the method arguments
     */
    public String copyWorkspaceFileUsingAdmin(String username, String sourcePath, String destinationPath,
            NameCollisionResolutionBehavior nameCollisionResolutionBehavior, ProgressIndicator progress) {
        return copyFile(username, sourcePath, destinationPath, nameCollisionResolutionBehavior, progress, false, true);
    }

    /**
     * Copies file or folder from the given source path in the Domain folder to the workspace location at the given destination path.
     *
     * If source and destination are both files, then the destination file's content will be replaced with the
     * source file's content.
     *
     * If the source and destination are both directories, then the source file and all of its descendant
     * files and folders will be copied to into the destination folder..
     * </br><br/>
     * Example:</br>
     * a/my.pps, b/</br>
     * source = a/my.pps, destination = b/, return = b/my.pps</br>
     *
     * @param username information used to authenticate the request.
     * @param sourcePath the path to the file to copy. Can be a file or directory.
     * The path must be relative to GIFT's Domain folder (i.e. temp/blah).
     * @param destinationPath where to copy the file too.  Can be a file or directory.
     * The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param nameCollisionResolutionBehavior the resolution to occur if there is a naming conflict
     *        while copying the file or folder.
     * @param progress the progress indicator for the copy operation. Can be null.
     * @param useAdminPrivilege true to use admin privileges to create the document
     * @return String the file name including path of the copied file location
     * @throws FileExistsException if a file with the same file name as the source exists at the destination and overwriting is not enabled
     * @throws DetailedException if there was a severe problem copying the file
     * @throws IllegalArgumentException if there was a problem with the method arguments
     */
    public String copyDomainFile(String username, String sourcePath, String destinationPath, NameCollisionResolutionBehavior nameCollisionResolutionBehavior, ProgressIndicator progress)
            throws FileExistsException, IllegalArgumentException, DetailedException {

        return copyFile(username, sourcePath, destinationPath, nameCollisionResolutionBehavior, progress, true, false);
    }

    /**
     * Copies file or folder from the given source path (in either the workspace or the Domain
     * folder) to the workspace location at the given destination path.
     *
     * If source and destination are both files, then the destination file's content will be
     * replaced with the source file's content.
     *
     * If the source and destination are both directories, then the source file and all of its
     * descendant files and folders will be copied to into the destination folder.. </br>
     * <br/>
     * Example:</br>
     * a/my.pps, b/</br>
     * source = a/my.pps, destination = b/, return = b/my.pps</br>
     *
     * @param username information used to authenticate the request.
     * @param sourcePath the path to the file to copy. Can be a file or directory. The root of the
     *        path must be either a workspace sub-folder (e.g. Public, &lt;username&gt;), if
     *        'isDomainFile' is set to false, or a Domain subfolder (e.g. temp), is 'isDomainFile'
     *        is set to true.
     * @param destinationPath where to copy the file too. Can be a file or directory. The root of
     *        the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param nameCollisionResolutionBehavior the resolution to occur if there is a naming conflict
     *        while copying the file or folder.
     * @param progress the progress indicator for the copy operation. Can be null.
     * @param isDomainFile whether or not to look for the source file in the Domain folder. If
     *        false, the file will be searched for in the workspace instead.
     * @param useAdminPrivilege true to use admin privileges to create the document
     * @return String the file name including path of the copied file location
     * @throws FileExistsException if a file with the same file name as the source exists at the
     *         destination and overwriting is not enabled
     * @throws DetailedException if there was a severe problem copying the file
     * @throws IllegalArgumentException if there was a problem with the method arguments
     */
    protected abstract String copyFile(String username, String sourcePath, String destinationPath,
            NameCollisionResolutionBehavior nameCollisionResolutionBehavior, ProgressIndicator progress, boolean isDomainFile, boolean useAdminPrivilege)
            throws FileExistsException, IllegalArgumentException, DetailedException;

    /**
     * Uses the provided file path to check if there is a name collision in its directory. If there
     * is, increment the name until no conflict occurs.
     *
     * @param filePath the file path to check.
     * @param username information used to authenticate the request
     * @param isFolder true if the filePath references a folder
     * @return the generated unique name of the file or folder.
     */
    protected String generateUniqueName(String filePath, String username, boolean isFolder) {
        FileTreeModel fileModel = FileTreeModel.createFromRawPath(filePath);
        int nameIncrementer = 1;
        final String originalName = fileModel.getFileOrDirectoryName();

        if (isFolder) {
            // keep searching for a unique folder name
            while (fileExists(username, fileModel.getRelativePathFromRoot(), true)) {
                fileModel.setFileOrDirectoryName(originalName + "_" + nameIncrementer++);
}
        } else {
            int firstPeriodLoc = originalName.indexOf('.');
            String nameWithoutExtensions = firstPeriodLoc > 0 ? originalName.substring(0, firstPeriodLoc)
                    : originalName;
            // keep searching for a unique file name
            while (fileExists(username, fileModel.getRelativePathFromRoot(), false)) {
                final String newName = originalName.replace(nameWithoutExtensions,
                        nameWithoutExtensions + "_" + nameIncrementer++);
                fileModel.setFileOrDirectoryName(newName);
            }
        }
        return fileModel.getFileOrDirectoryName();
    }

    /**
     * Retrieves the training application scenario property files.
     *
     * @param username information used to authenticate the request
     * @return the list of training application scenario properties that were found.
     * @throws IOException if there was a problem retrieving the files
     */
    public List<GiftScenarioProperties> getTrainingApplicationScenarioProperties(String username) throws IOException {
        List<GiftScenarioProperties> scenarioProperties = new ArrayList<>();

        /* search in Public and user folder */
        List<File> foldersToSearch = new ArrayList<>();
        foldersToSearch.add(TRAINING_APPS_MAPS_PUBLIC_DIRECTORY);
        foldersToSearch.add(new File(TRAINING_APPS_MAPS_DIRECTORY + File.separator + username));

        DesktopFolderProxy trainingAppsMapsDirectory = new DesktopFolderProxy(TRAINING_APPS_MAPS_DIRECTORY);
        for (File searchFolder : foldersToSearch) {
            if (!searchFolder.exists()) {
                continue;
            }

            /* always searching locally, use desktop folder proxy */
            DesktopFolderProxy searchFolderProxy = new DesktopFolderProxy(searchFolder);

            for (FileProxy file : searchFolderProxy.listFilesByName(null, GiftScenarioProperties.FILENAME)) {

                try {
                    Properties properties = new Properties();
                    properties.load(file.getInputStream());
                    String name = properties.getProperty(GiftScenarioProperties.NAME);
                    String author = properties.getProperty(GiftScenarioProperties.AUTHOR);
                    String description = properties.getProperty(GiftScenarioProperties.DESCRIPTION);
                    String trainingApplicationTypeStr = properties.getProperty(GiftScenarioProperties.TYPE);
                    String scenarioEntryPoint = properties.getProperty(GiftScenarioProperties.ENTRY_POINT);
                    String userStartLocation = properties.getProperty(GiftScenarioProperties.START_LOCATION);

                    TrainingApplicationEnum trainingApplicationType;
                    try {
                        trainingApplicationType = TrainingApplicationEnum.valueOf(trainingApplicationTypeStr);
                    } catch (@SuppressWarnings("unused") EnumerationNotFoundException enfe) {
                        continue;
                    }

                    String relativePath = trainingAppsMapsDirectory.getRelativeFileName(file);
                    FileTreeModel propertyModel = FileTreeModel.createFromRawPath(relativePath);
                    GiftScenarioProperties propertyFile = new GiftScenarioProperties(name, author, description,
                            trainingApplicationType, propertyModel.getParentTreeModel().getRelativePathFromRoot(),
                            scenarioEntryPoint, userStartLocation);
                    propertyFile.setEntryPointFound(new File(TRAINING_APPS_MAPS_DIRECTORY,
                            propertyFile.getScenarioEntryPointPathWithParentPath()).exists());

                    scenarioProperties.add(propertyFile);
                } catch (@SuppressWarnings("unused") Exception e) {
                    // do nothing
                }
            }
        }

        return scenarioProperties;
    }

    /**
     * Retrieves the training application scenario properties.
     *
     * @param folderPath the path of the folder to search for the scenario property file. The root
     *        must be a sub-folder of {@link #TRAINING_APPS_MAPS_DIRECTORY}.
     * @param username information used to authenticate the request
     * @return the training application scenario properties that was found. Can be null if no
     *         properties were found.
     * @throws IOException if there was a problem retrieving the files
     */
    public GiftScenarioProperties getTrainingApplicationScenarioProperty(String folderPath, String username)
            throws IOException {
        GiftScenarioProperties scenarioProperties = null;

        /* search in Public and user folder */
        File folderToSearch = new File(TRAINING_APPS_MAPS_DIRECTORY + File.separator + folderPath);
        if (!folderToSearch.exists()) {
            return scenarioProperties;
        }

        /* always searching locally, use desktop folder proxy */
        DesktopFolderProxy scenarioRootDirectory = new DesktopFolderProxy(folderToSearch);
        for (FileProxy file : scenarioRootDirectory.listFilesByName(null, GiftScenarioProperties.FILENAME)) {

            try {
                Properties properties = new Properties();
                properties.load(file.getInputStream());
                String name = properties.getProperty(GiftScenarioProperties.NAME);
                String author = properties.getProperty(GiftScenarioProperties.AUTHOR);
                String description = properties.getProperty(GiftScenarioProperties.DESCRIPTION);
                String trainingApplicationTypeStr = properties.getProperty(GiftScenarioProperties.TYPE);
                String scenarioEntryPoint = properties.getProperty(GiftScenarioProperties.ENTRY_POINT);
                String userStartLocation = properties.getProperty(GiftScenarioProperties.START_LOCATION);

                TrainingApplicationEnum trainingApplicationType;
                try {
                    trainingApplicationType = TrainingApplicationEnum.valueOf(trainingApplicationTypeStr);
                } catch (@SuppressWarnings("unused") EnumerationNotFoundException enfe) {
                    continue;
                }

                DesktopFolderProxy trainingAppsMapsDirectory = new DesktopFolderProxy(TRAINING_APPS_MAPS_DIRECTORY);
                String relativePath = trainingAppsMapsDirectory.getRelativeFileName(file);
                FileTreeModel propertyModel = FileTreeModel.createFromRawPath(relativePath);
                scenarioProperties = new GiftScenarioProperties(name, author, description, trainingApplicationType,
                        propertyModel.getParentTreeModel().getRelativePathFromRoot(), scenarioEntryPoint, userStartLocation);
                break;
            } catch (@SuppressWarnings("unused") Exception e) {
                // do nothing
            }
        }

        return scenarioProperties;
    }

    /**
     * Retrieves the map tile property files from the provided folder location.
     *
     * @param folderPath the path of the folder to search for the map tile property files. The root
     *        must be a sub-folder of the Training.Apps/maps directory.
     * @param username information used to authenticate the request
     * @return the list of map tile properties that were found within the provided search folder.
     * @throws IOException if there was a problem retrieving the files
     */
    public List<MapTileProperties> getScenarioMapTileProperties(String folderPath, String username) throws IOException {
        List<MapTileProperties> mapTileProperties = new ArrayList<>();

        /* search in Public and user folder */
        File folderToSearch = new File(TRAINING_APPS_MAPS_DIRECTORY + File.separator + folderPath);
        if (!folderToSearch.exists()) {
            return mapTileProperties;
        }

        /* always searching locally, use desktop folder proxy */
        DesktopFolderProxy scenarioRootDirectory = new DesktopFolderProxy(folderToSearch);

        List<FileProxy> imagePropertyFiles = new ArrayList<>();
        FileFinderUtil.getFilesByExtension(scenarioRootDirectory, imagePropertyFiles,
                MapTileProperties.MAPTILE_EXTENSION);

        for (FileProxy file : imagePropertyFiles) {
            try {
                Properties properties = new Properties();
                properties.load(file.getInputStream());
                String imageFile = properties.getProperty(MapTileProperties.IMAGE_FILE);

                String lowerLeftAGL = null;
                if (properties.containsKey(MapTileProperties.LOWER_LEFT_AGL)) {
                    lowerLeftAGL = properties.getProperty(MapTileProperties.LOWER_LEFT_AGL);
                }

                String upperRightAGL = null;
                if (properties.containsKey(MapTileProperties.UPPER_RIGHT_AGL)) {
                    upperRightAGL = properties.getProperty(MapTileProperties.UPPER_RIGHT_AGL);
                }

                String tileCoordinate = null;
                if (properties.containsKey(MapTileProperties.TILE_COORDINATE)) {
                    tileCoordinate = properties.getProperty(MapTileProperties.TILE_COORDINATE);
                }

                String zoomLevel = null;
                if (properties.containsKey(MapTileProperties.ZOOM_LEVEL)) {
                    zoomLevel = properties.getProperty(MapTileProperties.ZOOM_LEVEL);
                }

                String relativePath = scenarioRootDirectory.getRelativeFileName(file);
                FileTreeModel relativeModel = FileTreeModel.createFromRawPath(relativePath);
                FileTreeModel imageFileModel = relativeModel.getParentTreeModel().getModelFromRelativePath(imageFile);

                MapTileProperties propertyFile;

                String imageFromRoot = imageFileModel.getRelativePathFromRoot();
                if (lowerLeftAGL != null && upperRightAGL != null) {
                    MapTileCoordinate lowerLeft = new MapTileCoordinate(lowerLeftAGL);
                    MapTileCoordinate upperRight = new MapTileCoordinate(upperRightAGL);
                    propertyFile = new MapTileProperties(imageFromRoot, lowerLeft,
                            upperRight);
                } else if (tileCoordinate != null && zoomLevel != null) {
                    MapTileCoordinate mapTileCoordinate = new MapTileCoordinate(tileCoordinate);
                    propertyFile = new MapTileProperties(imageFromRoot,
                            Integer.parseInt(zoomLevel), mapTileCoordinate);
                } else {
                    StringBuilder sb = new StringBuilder().append("The ").append(imageFromRoot)
                            .append(" file must define all of one of the two sets of properties: [");

                    StringUtils.join(", ", Arrays.asList(MapTileProperties.IMAGE_FILE, MapTileProperties.LOWER_LEFT_AGL,
                            MapTileProperties.UPPER_RIGHT_AGL), sb);

                    sb.append("]").append(" or [");
                    StringUtils.join(", ", Arrays.asList(MapTileProperties.IMAGE_FILE, MapTileProperties.ZOOM_LEVEL,
                            MapTileProperties.TILE_COORDINATE));
                    sb.append("]");

                    throw new UnsupportedOperationException(sb.toString());
                }

                mapTileProperties.add(propertyFile);
            } catch (Exception e) {
                String errorMsg = String.format("There was a problem processing '%s' as a *.maptile.properties file",
                        file.getFileId());
                logger.error(errorMsg, e);
            }
        }

        return mapTileProperties;
    }
    
    /**
     * Used to populate additional, common, attributes for courses across both Desktop and Server modes.
     * E.g. course source option type, has accessible publish course
     * @param username the user requesting the list of available courses. Shouldn't be null or empty.
     * @param courseOptionsWrapper contains information about the course found for this user.
     */
    protected void completeDomainOptionInfo(String username, CourseOptionsWrapper courseOptionsWrapper){
        

        for(DomainOption domainOption : courseOptionsWrapper.domainOptions.values()){
            
            //
            // Step 1: set the source option type for each course found
            //
            String workspaceFolderName = domainOption.getDomainId().substring(0, domainOption.getDomainId().indexOf(Constants.FORWARD_SLASH));
            
            // Determine the source type based on workspace location
            if(workspaceFolderName.equals(username)){
                domainOption.setCourseSourceOptionType(CourseSourceOption.MY_COURSES);
            }else if(workspaceFolderName.equals(PUBLIC_WORKSPACE_FOLDER_NAME)){
                domainOption.setCourseSourceOptionType(CourseSourceOption.SHOWCASE_COURSES);
            }else{
                // fall through case
                // DESKTOP: basically can be anything that is in another user's workspace because
                // there is no explicit sharing
                domainOption.setCourseSourceOptionType(CourseSourceOption.SHARED_COURSES);
            }
            
            //
            // Step 2: see if this course is used by any published courses
            //
            boolean hasPublishCourse =
                    ServicesManager.getInstance().getDataCollectionServices().doesCourseHaveDataCollectionDataSets(username, domainOption.getDomainId());

            domainOption.setHasAccessiblePublishCourse(hasPublishCourse);
        }

    }

    /**
     * The different resolution options for name collisions when performing file operations.
     *
     * @author sharrison
     */
    public enum NameCollisionResolutionBehavior {
        /**
         * Over-write an existing destination with the source file or folder. If a file, any
         * existing file at the destination with the same name will be overwritten with the contents
         * of the source file. If a folder, any existing folder at the destination with the same
         * name will have its contents merged with source folder, and any conflicting subfiles will
         * be overwritten with the subfiles from the source folder.
         */
        OVERWRITE,
        /** Stop the file operation and fail if a collision occurs */
        FAIL_ON_COLLISION,
        /**
         * Increment the source file or folder name until it is unique and there is no collision at
         * the destination
         */
        GUARANTEE_UNIQUE_NAME;
    }
}
