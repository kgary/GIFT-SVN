/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.importer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import generated.course.Course;
import mil.arl.gift.common.CourseRecord;
import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.DomainOption.DomainOptionRecommendation;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.DomainOptionRecommendationEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.AbstractSchemaHandler.FileType;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.domain.DomainCourseFileHandler.CourseOptionsWrapper;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.ConversionIssueList;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.io.Version;
import mil.arl.gift.common.io.ZipUtils;
import mil.arl.gift.net.nuxeo.DocumentExistsException;
import mil.arl.gift.net.nuxeo.QuotaExceededException;
import mil.arl.gift.tools.authoring.common.conversion.AbstractConversionWizardUtil;
import mil.arl.gift.tools.authoring.common.conversion.LatestVersionException;
import mil.arl.gift.tools.authoring.common.conversion.UnsupportedVersionException;
import mil.arl.gift.ums.db.UMSDatabaseManager;

/**
 * This class contains logic to import courses into this GIFT instance.
 *  
 * @author mhoffman
 *
 */
public class ImportCourseUtil {
    
    private static Logger logger = LoggerFactory.getLogger(ImportCourseUtil.class);
    private static boolean isInfoEnabled = logger.isInfoEnabled();
    
    /** 
     * directories and files used for importing 
     */
    private static final String IMPORT_RESOURCES_DIRECTORY = "data" + File.separator + "importResources";    
    private static final String DOMAIN_IMPORT_README_FILENAME = "DomainImport.README.txt";
    private static final String DOMAIN_IMPORT_README_FILEPATH = IMPORT_RESOURCES_DIRECTORY + File.separator + DOMAIN_IMPORT_README_FILENAME;
    private static final String GIFT_EXPORT_JAR_DIRECTORY = "GIFT" + File.separator + "bin";
    private static final String GIFT_EXPORT_JAR = GIFT_EXPORT_JAR_DIRECTORY + File.separator + "gift-domain.jar";

    //need to make sure the path has the appropriate operating system file separator and not the fixed slashes from 
    //the properties file in order to do file path comparisons
    private static final String SURVEY_WEB_RESOURCES_DIRECTORY = CommonProperties.getInstance().getSurveyImageUploadDirectory().replace(Constants.FORWARD_SLASH, File.separator);

    
    private static final String EXPORT_COMPARISON_IMG = "images/help/domainComparison.png";
    
    /** Total percentage contribution for each task */
    private static final int SETUP_FOR_IMPORT = 5;
    private static final int CONVERSION_TOTAL_PROGRESS = 25;
    private static final int IMPORT_SURVEYS_TOTAL_PROGRESS = 10;
    private static final int IMPORT_DOMAIN_DIR_TOTAL_PROGRESS = 45;
    private static final int IMPORT_DOMAIN_RESOURCES_TOTAL_PROGRESS = 5;
    private static final int CLEANUP_TOTAL_PROGRESS = 10;
    private static final int TOTAL_PROGRESS = 100;
    
    /** Total percentage contribution for the check for conflicts subtask */
    private static final int TEMP_DIR_CREATED_PROGRESS = 10;
    private static final int EXPORT_UNZIPPED_PROGRESS = 50;
    private static final int IMAGE_CONFLICTS_RETRIEVED_PROGRESS = 20;
    private static final int COURSE_CONFLICTS_RETRIEVED_PROGRESS = 20;
    
    /** 
     * Collection of temporary files created during the export process.  These files are not 
     * the files imported into the GIFT instance.
     * This is used for removing files after a failed or successful export. 
     * 
     * Key: file created
     * Value: boolean whether or not the file should be deleted at the end of a successful export.
     */
    private Map<File, Boolean> createdFiles = new HashMap<>();
    
    /** 
     * Maps conflicting course paths to their new names. The keys are paths are relative to the Domain folder in the 
     * course export zip file (i.e. "Domain/CourseName/CourseName.course.xml") and the values are the names that the 
     * course should be renamed to (i.e. "NewCourseName")
     */
    private Map<String, String> courseToNameMap;
    
    /** collection of course folders that were created during the import process */
    private List<AbstractFolderProxy> createdCourseFolders = new ArrayList<>();
        
    /** the workspace where course folders will be created upon importing */
    private AbstractFolderProxy workspaceFolder;
    
    /** used for showing progress updates to user */
    private int totalFileCount = 0;
    private int currentFileCount = 0;
    
    /** used to write entries to an import summary readme file */
    private PrintWriter readmeWriter;

    public ImportCourseUtil(){
        
    }

    /**
     * Import the courses contained in the zip file into the root folder specified.
     * 
     * @param username the user importing the courses.
     * @param usersWorkspaceFolder the user's workspace folder to import the course(s) to.  Can't be null.
     * @param giftExportZipToImport a GIFT export containing courses and course assets (e.g. surveys) to import 
     * @param progressIndicator used to show the import progress to the user. Can't be null.  Will contain the
     * collection of courses that were created during this import.  This will included courses that were imported but failed
     * schema parsing.
     * @param filesToOverwrite a list of existing filenames that should be overwritten upon import. If null, any conflicting files will be overwritten.
     * @param courseToNameMap A map containing conflicting course paths to their new names. The keys are paths are relative to the Domain folder in the 
     * course export zip file (i.e. "Domain/CourseName/CourseName.course.xml") and the values are the names that the  course should be renamed to 
     * (i.e. "NewCourseName")
     * @param 
     * @throws DetailedException if there was a problem importing
     */
    public void importCourses(String username, AbstractFolderProxy usersWorkspaceFolder, File giftExportZipToImport, 
    		LoadedProgressIndicator<List<DomainOption>> progressIndicator, List<String> filesToOverwrite, Map<String, String> courseToNameMap) 
            throws DetailedException{

        if(usersWorkspaceFolder == null){
            throw new IllegalArgumentException("The workspace folder can't be null.");
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Starting course import for "+username+" from import file of "+giftExportZipToImport);
        }
        
        String importSummary = null;
        StringBuilder conversionSummary = new StringBuilder();
        
        //create import summary README
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy HH-mm-ss");
        String summaryFileName = DOMAIN_IMPORT_README_FILENAME.replace(".txt", "_" + df.format(new Date())) + ".txt";
        
        UUID errorId = UUID.randomUUID(); //create now for use at anypoint during this import
        DetailedException error = null;
        
        this.workspaceFolder = usersWorkspaceFolder;
        this.courseToNameMap = courseToNameMap;
        
        List<DomainOption> courses = new ArrayList<>(1);

        try {
            
            //unzip file to temp directory to determine if the file can be successfully unzip and
            //to setup a reference to the files for the domain selection and summary pages.
            File tempDirectory = File.createTempFile("GIFT", Long.toString(System.nanoTime())); 
            FileUtil.registerFileToDeleteOnShutdown(tempDirectory);
            
            //delete directory in case it exist                    
            tempDirectory.delete();
            
            if(!ZipUtils.createDir(tempDirectory)) {
                throw new Exception("Unable to create directory named "+tempDirectory+".");
            }
            
            // Unzip entire zip folder
            progressIndicator.updateSubtaskDescription("Extracting");
            ZipUtils.unzipArchive(giftExportZipToImport, tempDirectory, progressIndicator.getSubtaskProcessIndicator());
            progressIndicator.updateSubtaskDescription("Extracted");

            // Check if this is a course export or a full GIFT export
            File tempDomainJar = new File(tempDirectory.getCanonicalPath() + File.separator + GIFT_EXPORT_JAR);
            
            if(tempDomainJar.exists()) {
            	FileUtils.forceDelete(tempDirectory);
            	
            	throw new DetailedException("The file you selected is a complete GIFT export and cannot be uploaded. " + 
            			"Please select a Course export or a Domain Content Only export to upload.",
                        "To determine whether or not your export is a Course export or a full GIFT export, you can " +
                        "open the archive and view the contents of the Domain folder. Below is a comparison of the " +
                        "Domain folders of a Course export and a complete GIFT export: <br/><br/><img src=\"" +
            			EXPORT_COMPARISON_IMG+ "\" style=\"margin: auto; display:block;\" alt=\"Comparison of the " +
            			"Domain folders located in GIFT export files.\"><br/<br/>For developer:<br/>" + "Error Id: " + errorId, null);
            }
            
            // Store the imported Domain folder location
            File tempDomainFolder = new File(tempDirectory.getCanonicalPath() + File.separator + "Domain");            
            
            if(!tempDomainFolder.exists()){
                throw new DetailedException("Failed to find the 'Domain' folder to import in '"+giftExportZipToImport.getName()+"'.",
                        "The most common problem is that the GIFT import zip is not correctly structured.  The first level of the zip must contain"+
                                " the 'Domain' folder (it can also contain the 'GIFT' folder).  These folders can't be in a subdirectory in the zip.\n\n"+
                                "For developer:\n"
                                + "the file '"+tempDomainFolder+"' doesn't exist\n"
                                + "Error Id: "+errorId, null);
            }
            
            // Check if there is a GIFT folder in the export.
            File tempGIFTFolder = new File(tempDirectory + File.separator + "GIFT");                                
            
            createdFiles.put(tempDomainFolder.getParentFile(), true);
            
            //copy default README, and start appending to it...
            File summaryFile = new File(tempDirectory + File.separator + summaryFileName);
            summaryFile.createNewFile();
            FileUtils.copyFile(new File(DOMAIN_IMPORT_README_FILEPATH), summaryFile);
            readmeWriter = new PrintWriter(new FileOutputStream(summaryFile, true));
            
            //mention imported file name in summary README
            readmeWriter.println();
            readmeWriter.println();
            readmeWriter.append("Import file: ").append(giftExportZipToImport.getName());
            
            //mention domains being imported by retrieving list of domains in temp Domain directory
            readmeWriter.println();
            readmeWriter.println();
            readmeWriter.append("Domain content: ");
            readmeWriter.println();
            
            String[] filenames = tempDomainFolder.list();
            for(int i = 0; i < filenames.length; i++) {
                readmeWriter.append(filenames[i]);
                readmeWriter.println();
            }
             
            progressIndicator.increasePercentComplete(SETUP_FOR_IMPORT);
            
            //
            // Convert GIFT files
            // 1. Courses
            // 2. DKFs
            // 3. Metadata
            // 4. Training app refs
            // 5. Sensor configs
            // 6. Learner configs
            // 7. Pedagogy configs
            //
            logger.info("Checking and automatically converting domain files accordingly");
            progressIndicator.updateSubtaskDescription("Converting Domain Files...");

            // mention import issues
            readmeWriter.println();
            readmeWriter.println();
            readmeWriter.append("Import Issues:");

            // 1. Convert course files
            List<FileProxy> convertedCourses = checkAndConvertCourses(tempDomainFolder);
            if (!convertedCourses.isEmpty()) {
                // update summary

                conversionSummary.append("Converted Courses:\n");
                for (int i = 0; i < convertedCourses.size(); i++) {

                    FileProxy file = convertedCourses.get(i);
                    conversionSummary.append("\t").append(file.getFileId());
                    if ((i + 1) < convertedCourses.size()) {
                        conversionSummary.append("\n");
                    }
                }
            }

            // 2. Convert dkf files
            DesktopFolderProxy tempDomainFolderProxy = new DesktopFolderProxy(tempDomainFolder);
            List<FileProxy> convertedDKFs = checkAndConvertDKFs(tempDomainFolderProxy);
            if(!convertedDKFs.isEmpty()){
                //update summary
                
                conversionSummary.append("Converted DKFs:\n");
                for(int i = 0; i < convertedDKFs.size(); i++){
                    
                    FileProxy file = convertedDKFs.get(i);
                    conversionSummary.append("\t").append(file.getFileId());
                    if((i+1) < convertedDKFs.size()){
                        conversionSummary.append("\n");
                    }
                }
            }
             
            // 3. Convert metadata files
            List<FileProxy> convertedMetadatas = checkAndConvertMetadatas(tempDomainFolderProxy);
            if(!convertedMetadatas.isEmpty()){
                //update summary
                
                conversionSummary.append("Converted Metadatas:\n");
                for(int i = 0; i < convertedMetadatas.size(); i++){
                    
                    FileProxy file = convertedMetadatas.get(i);
                    conversionSummary.append("\t").append(file.getFileId());
                    if((i+1) < convertedMetadatas.size()){
                        conversionSummary.append("\n");
                    }
                }
            }
            
            // 4. Convert training app refs
            List<FileProxy> convertedTrainingAppRefs = checkAndConvertTrainingAppRefs(tempDomainFolderProxy);
            if(!convertedTrainingAppRefs.isEmpty()){
                //update summary
                
                conversionSummary.append("Converted Training Application References:\n");
                for(int i = 0; i < convertedTrainingAppRefs.size(); i++){
                    
                    FileProxy file = convertedTrainingAppRefs.get(i);
                    conversionSummary.append("\t").append(file.getFileId());
                    if((i+1) < convertedTrainingAppRefs.size()){
                        conversionSummary.append("\n");
                    }
                }
            }
            
            // 5. Convert sensor configs
            List<FileProxy> convertedSensorConfigs = checkAndConvertSensorConfigs(tempDomainFolderProxy);
            if(!convertedSensorConfigs.isEmpty()){
                //update summary
                
                conversionSummary.append("Converted Sensor Configurations:\n");
                for(int i = 0; i < convertedSensorConfigs.size(); i++){
                    
                    FileProxy file = convertedSensorConfigs.get(i);
                    conversionSummary.append("\t").append(file.getFileId());
                    if((i+1) < convertedSensorConfigs.size()){
                        conversionSummary.append("\n");
                    }
                }
            }
            
            // 6. Convert learner configs
            List<FileProxy> convertedLearnerConfigs = checkAndConvertLearnerConfigs(tempDomainFolderProxy);
            if(!convertedLearnerConfigs.isEmpty()){
                //update summary
                
                conversionSummary.append("Converted Learner Configurations:\n");
                for(int i = 0; i < convertedLearnerConfigs.size(); i++){
                    
                    FileProxy file = convertedLearnerConfigs.get(i);
                    conversionSummary.append("\t").append(file.getFileId());
                    if((i+1) < convertedLearnerConfigs.size()){
                        conversionSummary.append("\n");
                    }
                }
            }
            
            // 7. Convert pedagogy configs
            List<FileProxy> convertedPedagogyConfigs = checkAndConvertPedagogyConfigs(tempDomainFolderProxy);
            if(!convertedPedagogyConfigs.isEmpty()){
                //update summary
                
                conversionSummary.append("Converted Pedagogy Configurations:\n");
                for(int i = 0; i < convertedPedagogyConfigs.size(); i++){
                    
                    FileProxy file = convertedPedagogyConfigs.get(i);
                    conversionSummary.append("\t").append(file.getFileId());
                    if((i+1) < convertedPedagogyConfigs.size()){
                        conversionSummary.append("\n");
                    }
                }
            }
            
             if(conversionSummary.length() != 0){
                 
                 //
                 // write summary of conversion changes to disk
                 //            
                 readmeWriter.println();
                 readmeWriter.println();
                 readmeWriter.append("The following domain files where automatically backed up and then converted to the latest versions.\n")
                         .append("If there are problems with the course(s), please check the course in the GIFT authoring tool.\n").append(conversionSummary.toString());
                 readmeWriter.println();
             }
             
             progressIndicator.increasePercentComplete(CONVERSION_TOTAL_PROGRESS);
             
             //get imported GIFT folder, which contains all resources used by the imported Domain content
             Map<String, String> movedSurveyImages = null;            
             if (tempGIFTFolder.exists()) {
                 // Import all resources
                 progressIndicator.updateSubtaskDescription("Importing external Domain resources...");
                 beginFileTask(tempGIFTFolder);
                 movedSurveyImages = importResources(tempGIFTFolder, username, filesToOverwrite);
                 endDirectoryTask();
                 
                 progressIndicator.increasePercentComplete(IMPORT_DOMAIN_RESOURCES_TOTAL_PROGRESS);
             }
            
             if(logger.isInfoEnabled()){
                 logger.info("Importing Surveys and updating domain files accordingly");
             }
             progressIndicator.updateSubtaskDescription("Importing Surveys...");
             
             //import survey context(s) found in the importing domain directory and update course/dkf references accordingly
             importSummary = CourseDBImporter.importSurveyExports(username, tempDomainFolderProxy, movedSurveyImages, usersWorkspaceFolder);
             
             progressIndicator.increasePercentComplete(IMPORT_SURVEYS_TOTAL_PROGRESS);
             
             if(logger.isInfoEnabled()){
                 logger.info("Importing to " + usersWorkspaceFolder.getFileId());
             }
             progressIndicator.updateSubtaskDescription("Importing Domain content...");
              
             //copy domain content to domain folder
             beginFileTask(tempDomainFolder);
             StringBuffer errorBuffer = new StringBuffer();
             importDomainDirectory(tempDomainFolder, usersWorkspaceFolder, errorBuffer, progressIndicator, true);
             endDirectoryTask();
             
             progressIndicator.increasePercentComplete(IMPORT_DOMAIN_DIR_TOTAL_PROGRESS);
             
             //TODO: what to do about folder name collisions?  The folder names can be used in references to domain content files.
             //...right now log it
             if(errorBuffer.length() > 0){
                 logger.error("There was a problem importing the domain directory:\n"+errorBuffer.toString());
                 readmeWriter.println();
                 readmeWriter.append("Automatically backed up the following existing files for you because of filename collisions due to importing:");
                 readmeWriter.println();
                 readmeWriter.append(errorBuffer.toString());
             }
             
             //
             // write summary of those changes to disk and mention summary file to user
             //            
             readmeWriter.println();
             readmeWriter.println();
             readmeWriter.append(importSummary);
             readmeWriter.println();
             
             //finished writing to README
             readmeWriter.close();
             
             copySummaryToCourseFolders(summaryFile);
             
             //check if user has selected the cancel button
             if(tryCancel(progressIndicator)){
                 return;
             }
             
             //
             // Build the course list that was imported
             //
             try{
                 CourseOptionsWrapper courseOptionsWrapper = new CourseOptionsWrapper();
                 List<FileProxy> courseFiles = new ArrayList<>();
                 for(AbstractFolderProxy courseFolder : createdCourseFolders){
                     FileFinderUtil.getFilesByExtension(courseFolder, courseFiles, AbstractSchemaHandler.COURSE_FILE_EXTENSION);
                 }

                 AbstractFolderProxy workspacesFolder = workspaceFolder.getParentFolder(null);
                 for(FileProxy courseFileProxy : courseFiles){                     
                     DomainOption course = DomainCourseFileHandler.buildDomainOption(courseFileProxy, workspacesFolder, courseOptionsWrapper, false, false, username, null);
                     
                     // create UMS db entry for this new course
                     CourseRecord insertedRecord = new CourseRecord(course.getDomainId(), username);
                     CourseRecord courseRecord = UMSDatabaseManager.getInstance().createCourseRecord(insertedRecord);
                     if (courseRecord == null) {
                         throw new DetailedException("Failed to create the course record in the database for the course '"+course.getDomainId()+"'.", 
                                 "The course record could not be created.", null);
                     }
                     
                     // make sure a 'course tile' published course exists for all courses that are created (in this case imported).  This way
                     // the data for those that take this course can be analyzed using the publish course page.
                     UMSDatabaseManager.getInstance().createDefaultDataCollectionItemIfNeeded(username, courseRecord);
                 }
                 
                 courses.addAll(courseOptionsWrapper.domainOptions.values());
                 
                 //alphabetizes the course tiles for courses that could be parsed
                 Collections.sort(courses);
                 
                 //create options for course that failed to parse so they still show up in the course tile list
                 for(String courseFilename : courseOptionsWrapper.parseFailedFiles){
                     File courseFile = new File(courseFilename);
                     
                     //remove the path and course.xml suffix to only show the course file name on the course tile
                     String trimmedCourseFileName = courseFile.getName().substring(0, courseFile.getName().indexOf(AbstractSchemaHandler.COURSE_FILE_EXTENSION));
                     DomainOption domainItem = new DomainOption(trimmedCourseFileName, courseFilename, null, username);
                     DomainOptionRecommendation domainOptionRecommendation = new DomainOptionRecommendation(DomainOptionRecommendationEnum.UNAVAILABLE_OTHER);
                     domainOptionRecommendation.setReason("There was an issue validating this course that will need to be resolved using the course creator.");
                     domainOptionRecommendation.setDetails("Please use the course creator's validation tool to help resolve validation issues with this course.  "+
                     "If you don't have permission to edit this course than notify the course owner about the issue you are seeing.  "+
                             "You may also use the forums at gifttutoring.org for additional help on fixing this course.");
                     domainItem.setDomainOptionRecommendation(domainOptionRecommendation);
                     courses.add(domainItem);
                 }
             }catch(Exception e){
                 
                 logger.error("There was a problem building the course list to return when importing course(s) for '"+username+"'.", e);
                 //clear the courses list that will be returned to force the dashboard
                 //to refresh the entire course list
                 courses.clear();
             }
             
        }catch (DocumentExistsException documentExists){
            logger.error(errorId + " - Caught exception while trying to import for "+username+".", documentExists);
            error = new DetailedException("Unable to import the course because a file already exists.", "An exception was thrown with the message "+documentExists.getMessage()+".\n\nError Id: "+errorId, documentExists);
        }catch (QuotaExceededException quotaExceeded){
            logger.error(errorId + " - Caught exception while trying to import for "+username+".", quotaExceeded);
            error = new DetailedException("You are running low on disk space. Please delete some files from your workspace before attempting to upload this file again.",
                    "Failed to create the document because the disk space quota has been exceeded.", quotaExceeded);
        }catch (DetailedException detailedException){
            logger.error(errorId + " - Caught exception while trying to import course for "+username+".", detailedException);
            error = detailedException;
        } catch (Throwable e) {       
            logger.error(errorId + " - Caught exception while trying to import course for "+username+".", e);
            error = new DetailedException("Unable to import the course because a general exception was thrown.", "The exception message reads:\n"+e.getMessage()+".\n\nError Id: "+errorId, e);
        }finally{
            
            if(readmeWriter != null){
                readmeWriter.close();
            }
        }
        
        progressIndicator.updateSubtaskDescription("Cleaning up...");
        
        //check if user has selected the cancel button
        if(tryCancel(progressIndicator)){
            return;
        }   
        
        //cleanup temp folder
        beginFilesTask(createdFiles.keySet());
        performCleanup(progressIndicator, error == null);
        endDirectoryTask();
        
        //check if user has selected the cancel button
        if(tryCancel(progressIndicator)){
            return;
        }

        if(error == null){
            progressIndicator.updateSubtaskDescription("GIFT Domain Import Successful!\n\nPlease check out the summary of the import at\n"+summaryFileName);
        }else{
            //some id that the user should see and could report to technical staff to identify the errors in the log for that event
            logger.error(errorId + " - The import failed for "+username+".  Check the this log for errors.");
            
            if(CommonProperties.getInstance().getDeploymentMode() == DeploymentModeEnum.SERVER){
                progressIndicator.updateSubtaskDescription("GIFT Import Course Failed!  \nPlease contact the system administrator if you need more assistance.\n Error Id: "+errorId);
                throw error;
            }else{
                progressIndicator.updateSubtaskDescription("GIFT Import Course Failed!  \nCheck import tool log in GIFT/output/logger/tools/ for more information.\n Error Id: "+errorId);             
                throw error;
            } 
        }
        
        progressIndicator.setPayload(courses);
        progressIndicator.setPercentComplete(TOTAL_PROGRESS);
        progressIndicator.setSubtaskProgress(TOTAL_PROGRESS);
    }
    
    /**
     * Copy the provided summary file to each of the course folders.
     * 
     * @param summaryFile the file containing a summary of the import
     * @throws IOException if there was a problem copying the summary file to the course folders
     */
    private void copySummaryToCourseFolders(File summaryFile) throws IOException{
        
        if(!summaryFile.exists()){
            throw new FileNotFoundException("The summary file "+summaryFile+" doesn't exist.");
        }
        
        for(AbstractFolderProxy courseFolder : createdCourseFolders){
            logger.debug("Copying summary file '"+summaryFile+"' to course folder "+courseFolder);
            courseFolder.createFile(summaryFile, null);            
        }
    }
    
    /**
     * This method is used to import specific files from an exported GIFT folder.
     * <b>Currently the only files that are specifically imported from the GIFT folder are survey images</b>.
     * If this changes in the future, methods should be added within this method and this javadoc should be updated.
     * 
     * @param tempGiftFolder the GIFT folder that has been imported
     * @param username the user performing the import
     * @param filesToOverwrite a list of existing filenames that should be overwritten upon import.
     * @return a mapping from each survey image's original file location to the location it was moved to. 
     * Will be empty if no image files were moved.
     */
    private Map<String, String> importResources(File tempGiftFolder, String username, List<String> filesToOverwrite) {
        
        // Import survey images
        File importedSurveyImages = new File(tempGiftFolder + File.separator + SURVEY_WEB_RESOURCES_DIRECTORY);
        return importSurveyImages(importedSurveyImages, username, filesToOverwrite);
    }
    
    /**
     * Copies survey images from an exported GIFT folder to this GIFT's survey image directory.
     * 
     * @param src The path of the unzipped, exported GIFT folder
     * @param username the user performing the import
     * @param filesToOverwrite a list of existing filenames that should be overwritten upon import.  Can be null.
     * @return a mapping from each survey image's original file location to the location it was moved to. 
     * Will be empty if no image files were moved.
     */
    private Map<String, String> importSurveyImages(File src, String username, List<String> filesToOverwrite) {
    	
    	Map<String, String> movedSurveyImages = new HashMap<String, String>();
        
        boolean overwriteAll = false;
        File[] files = src.listFiles();
        
        if(filesToOverwrite != null && files.length == filesToOverwrite.size()) {
        	overwriteAll = true;
        	
        } else if (filesToOverwrite == null) {
        	overwriteAll = true;
        }
        
        if(files == null){
            return movedSurveyImages;
        }
        
        for (File newImage : files) {
            // Create the file path that this image will be written to
        	String path = newImage.getPath();
        	path = path.substring(path.indexOf(SURVEY_WEB_RESOURCES_DIRECTORY));
        	
            File imageDest = new File(path);
            
            if(newImage.isDirectory()) {
            	
                //go into the directory and import images
            	importSurveyImages(newImage, username, filesToOverwrite);
            	continue;
            	
            }else if(imageDest.getParent().endsWith(SURVEY_WEB_RESOURCES_DIRECTORY)){
            	
            	if(imageDest.exists()){
            		
            		//don't over-write protected images at the root of the survey images directory
            		//(This mainly exists to prevent users from overwriting the images used in the self assessment surveys)
                    logger.debug("Not over-writing the protected root level survey image of '"+imageDest+" during a course import for "+username+".");   
            	
            	} else {
            	
	                // generate a UUID folder wrapping this image file so it can be imported properly
	                String uuid = UUID.randomUUID().toString();
	                int attempts = 0;
	                
	                // try to generate a new uuid up to five times if it is not unique
	                while(new File(imageDest.getAbsoluteFile() + uuid).exists() && attempts < 5) {
	                	uuid = UUID.randomUUID().toString();
	                	attempts++;
	                }
	                
	                if(attempts == 5) {
	                	logger.warn("Made 5 attempts at generating a new UUID for file : " + newImage.getName());
	                }
	                
	                File imageDir = new File(imageDest.getParent() + File.separator + uuid);
	                 
	                if(imageDir.mkdirs()){
	                	
	                	//move the image file into the generated folder
	                	File movedImage = new File(imageDir.getAbsolutePath() + File.separator + newImage.getName());
	                	
	                	try {
	                		
							Files.move(newImage.toPath(), movedImage.toPath());
							
							importSurveyImages(imageDir, username, filesToOverwrite);
							
							//save a mapping from the original image to its moved location so survey references can be updated.
							String originalPath = src.getParentFile().getName() + Constants.FORWARD_SLASH + src.getName() + 
							        path.substring(
							                path.indexOf(SURVEY_WEB_RESOURCES_DIRECTORY) + SURVEY_WEB_RESOURCES_DIRECTORY.length()
							        ).replace("\\", Constants.FORWARD_SLASH);
							String newPath = src.getParentFile().getName() + Constants.FORWARD_SLASH + src.getName() + movedImage.getAbsolutePath().substring(
                                        movedImage.getAbsolutePath().indexOf(SURVEY_WEB_RESOURCES_DIRECTORY)    
                                        + SURVEY_WEB_RESOURCES_DIRECTORY.length()
							        ).replace("\\", Constants.FORWARD_SLASH);
							movedSurveyImages.put(originalPath, newPath);
							
							if(logger.isInfoEnabled()){
							    logger.info("Adding survey image mapping of '"+originalPath+"' to '"+newPath+"' in order to update any survey's that reference that image.");
							}
							
						} catch (IOException e) {
							
							logger.warn("Unable to move imported image " + newImage.getName() + " to generated UUID folder.", e);
							
							if(imageDir.exists()){
								
								//cleanup if necessary
								imageDir.delete();
							}
							
							continue;
						}
	                	
	                } else {
	                	logger.warn("Unable to create unique directory for " + newImage.getName());
	                }
            	}
                
                continue;
            }
            
            if (imageDest.exists() && !overwriteAll) {
                
                if(filesToOverwrite != null && !filesToOverwrite.contains(newImage.getName())) {
                	// don't create a new file
                	continue;
                }
                
            }
            
            // Copy the image to the survey images directory.
            try {
                // Log the file copy
                if (imageDest.exists()) {
                    logger.info("While importing survey images, user has chosen to overwrite the existing image '" + imageDest.getName() +
                            "' in the '" + imageDest.getParent() + "' directory.");
                }else {
                    logger.info("Copying the survey image '" + newImage.getName() + "' to the '" + imageDest.getParent() + "' directory.");
                }
                
                FileUtils.copyFile(newImage, imageDest);
                
            } catch (IOException e) {
                logger.error("Caught exception while trying to copy " + newImage.getAbsolutePath() + " to " + imageDest.getAbsolutePath() + File.separator + newImage.getName(), e);
            }
        }
        
        return movedSurveyImages;
    }
    
    /**
     * Creates a map of survey images that will result in name conflicts if the source directory is imported.
     * If no conflicts are found, the map will be empty.
     * 
     * @param sourceDir The directory containing survey images to be imported
     * @return a map of new to existing survey images. If no conflicts are found, this map will be empty.
     */
    private Map<File, File> getImageConflicts(File sourceDir) {
        
    	Map<File, File> conflicts = new HashMap<File, File>();
    	
    	if(sourceDir != null) {
	        for (File newImage : sourceDir.listFiles()) {
	        	
	        	if(newImage.isDirectory()) {
	        		// If this is a directory, check for images
	        		conflicts.putAll(getImageConflicts(newImage));
	        		
	        	} else {
	        		// Check if this image is in its own folder
	        		
	        		String imagePath = newImage.getPath();
	        		imagePath = imagePath.substring(imagePath.indexOf(SURVEY_WEB_RESOURCES_DIRECTORY));
	        		
		            // Create the file path that this image will be written to
		            File imageDest = new File(imagePath);
		            
		            if (imageDest.exists()) { 
		                
		                if(imageDest.getParent().endsWith(SURVEY_WEB_RESOURCES_DIRECTORY)){
		                    logger.debug("Ignoring survey image import conflict with root level image of '"+imageDest+"'.");
		                }else{
		                    conflicts.put(newImage, imageDest);
		                }
		            }
	        	}
	        }
    	}
    	
        return conflicts;
    }
    
    /**
     * Creates a map of courses that will result in name conflicts if the source directory is imported.
     * If no conflicts are found, the map will be empty.
     * 
     * @param domainDir The Domain directory containing courses to be imported
     * @param workspaceFolder the user's workspace folder to to check for course conflicts in. Can't be null.
     * @return a map of conflicting courses where the key is the course.xml file and the value is the course folder name. 
     * This map will be empty if no conflicts are found.
     */
    private Map<File, File> getCourseConflicts(File domainDir, AbstractFolderProxy workspaceFolder) throws DetailedException {

        if(workspaceFolder == null){
            throw new IllegalArgumentException("The workspace folder can't be null.");
        }

        Map<File, File> conflicts = new HashMap<File, File>();
        
        if(domainDir != null && domainDir.exists()) {
            
            // Collect the existing course names in the user's workspace

            List<String> existingCourses = new ArrayList<String>();
            try {
                List<FileProxy> courseFiles = workspaceFolder.listFiles(null, AbstractSchemaHandler.COURSE_FILE_EXTENSION);
                for (FileProxy course : courseFiles) {

                    // Extract the course name from the path "username/CourseName/CourseName.course.xml"

                    int extIndex = course.getFileId().lastIndexOf(AbstractSchemaHandler.COURSE_FILE_EXTENSION);
                    int nameIndex = Math.max(course.getFileId().lastIndexOf(Constants.FORWARD_SLASH), course.getFileId().lastIndexOf(File.separator)) + 1;
                    String courseName = course.getFileId().substring(nameIndex, extIndex);
                    existingCourses.add(courseName);

                }
            } catch (IOException e) {
                throw new DetailedException("An error occurred while gathering courses from the workspace folder.", e.getMessage(), e);
            }

            // Search the Domain folder being imported for courses to compare with existing course names

            for (File candidateCourse : domainDir.listFiles()) {

                if(candidateCourse.isDirectory()) {

                    // If a folder is found, search the folder for course.xml files. There should only be one course per folder.

                    File[] courseFiles = candidateCourse.listFiles(new FilenameFilter() {

                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith(AbstractSchemaHandler.COURSE_FILE_EXTENSION);
                        }

                    });

                    if(courseFiles != null && courseFiles.length > 0) {
                    	
                    	//get the name of the course being imported
                    	File courseFile = courseFiles[0];
                        
                    	try{
                    		String importCourseName = null;
	                        FileProxy file = new FileProxy(courseFile);
	                        
	                        try{
	                        	
	                        	AbstractConversionWizardUtil conversionUtil = AbstractConversionWizardUtil.getConversionUtil(file, false);
	                            
	                            if(conversionUtil != null) {
	                                //conversion needed
	                                //Note: unmarshal even if there is a schema validation in order to support disabled (incomplete) course objects [#3570]
	                                UnmarshalledFile newFile = conversionUtil.convertFile(FileType.COURSE, file, false, false);
	                            
	                                if(newFile == null){
	                                    throw new DetailedException("Failed to convert the contents of the file '"+file+"'.",
	                                            "Attempted to convert that file starting at the GIFT schema version "+conversionUtil.getWorkingVersion()+" to the latest schema version of "+Version.getInstance().getCurrentSchemaVersion()+" but failed to convert it's contents.", null);
	                                }
	 
	            		            Course course = (Course) newFile.getUnmarshalled();
	            	            		
	                                importCourseName = course.getName();
	                            }
	                            
	                        } catch (@SuppressWarnings("unused") LatestVersionException e) {
		
	            				Course course = (Course) AbstractSchemaHandler.parseAndValidate(file.getInputStream(), FileType.COURSE, false).getUnmarshalled();
	            				
	            				importCourseName = course.getName();
	                        }
	                        
	                        if(importCourseName != null){
	                        	
	                        	for(String courseFolderName : existingCourses){
	                        	
		                        	if(importCourseName.toLowerCase().equals(courseFolderName.toLowerCase())) { // using toLowerCase for case insensitivity
	                                	
	                                	// If the candidate course name is already in use, record the conflict using the name of the existing course folder
	                                	conflicts.put(courseFiles[0], new File(courseFolderName));
	                                }
	                        	}
	                        }
	                        
	                    } catch (UnsupportedVersionException e) {
	                        //the file is pre-version 2.0 and can't be used.
	                        throw new DetailedException("Unable to up convert the file '"+courseFile.getName()+"'.", "Found an unsupported version with exception : "+e.getMessage(), e);
	                    } catch (SAXException | JAXBException | FileNotFoundException e){
	                        throw new DetailedException("There was a problem parsing the file '"+courseFile.getName()+"'.", "An exception was thrown with the message "+e.getMessage()+".", e);
	                    } catch (IOException e){                
	                        throw new DetailedException("There was a problem reading/writing the contents of the file '"+courseFile.getName()+"'.", "An exception was thrown with the message "+e.getMessage()+".", e);    
	                    } catch (Exception e){
	                        throw new DetailedException("There was a general problem when converting the file "+courseFile.getName()+"'.", "An exception was thrown with the message "+e.getMessage()+".", e);
	                    }
                    }   		
                }
            }
        }

        return conflicts;
    }
    
    /**
     * Checks a GIFT export file for conflicts with existing courses in the user's workspace and conflicting image files in the surveyWebResources folder.
     * 
     * @param giftExportZip the GIFT export file to check for conflicts.
     * @param workspaceFolder the user's workspace folder to to check for course conflicts in. Can be null if checking for 
     * course conflicts isn't necessary.  This should not be the root workspace directory but the username directory.
     * @param progress the progress indicator    
     * @return a map of conflicting files (new to existing). Can be null or empty.
     */
    public Map<File, File> checkForConflicts(File giftExportZip, AbstractFolderProxy workspaceFolder, ProgressIndicator progress) throws DetailedException {
    	
    	progress.updateSubtaskDescription("Checking existing files for conflicts.");
    	
        File tempDirectory;
        
        try {
            
            tempDirectory = File.createTempFile("GIFT", Long.toString(System.nanoTime()));
            
        } catch (Exception e) {

            logger.error("Caught exception while checking for import conflicts: error creating temp folder.", e);
            
            throw new DetailedException("An error occurred while checking the imported files for potential conflicts.", 
                     "Caught exception while creating a temporary folder for the GIFT export file. " + e.getMessage(), e);
        } 
        
        String resourcesDir = "GIFT" + File.separator + SURVEY_WEB_RESOURCES_DIRECTORY;
        resourcesDir = resourcesDir.replace("\\", "/");
        
        FileUtil.registerFileToDeleteOnShutdown(tempDirectory);
        
        //delete directory in case it exists                    
        tempDirectory.delete();
        
        // create the directory         
        tempDirectory.mkdir();
        
        progress.increaseSubtaskProgress(TEMP_DIR_CREATED_PROGRESS);
        
        try {
            
            // Unzip the surveyWebResources directory so it can be searched for conflicts
            ZipUtils.unzipFolder(giftExportZip, tempDirectory, resourcesDir);
            
            // Unzip the Domain directory so courses can be checked for conflicts
            ZipUtils.unzipFolder(giftExportZip, tempDirectory, "Domain");
            
        } catch (Exception e) {
            
            logger.error("Caught exception while checking for import conflicts: error unzipping file.", e);
            
            throw new DetailedException("An error occurred while checking the imported files for potential conflicts.", 
                     "Caught exception while unzipping the GIFT export file into a temporary folder. " + e.getMessage(), e);
        }
        
        progress.increaseSubtaskProgress(EXPORT_UNZIPPED_PROGRESS);
        
        // Check if this is a course export or a full GIFT export
        
        File tempDomainJar = null;
        
        try{
            
            ZipUtils.unzipFolder(giftExportZip, tempDirectory, GIFT_EXPORT_JAR_DIRECTORY.replace("\\", "/"));               
            tempDomainJar = new File(tempDirectory.getCanonicalPath() + File.separator + GIFT_EXPORT_JAR);
            
        } catch (Exception e) {
            logger.error("Error determining whether export a course export or full GIFT export.", e);
        }
        
        if(tempDomainJar != null && tempDomainJar.exists()) {
            throw new DetailedException("The file you selected is a complete GIFT export and cannot be uploaded. " + 
                    "Please select a Course export or a Domain Content Only export to upload.",
                    "To determine whether or not your export is a Course export or a full GIFT export, you can " +
                    "open the archive and view the contents of the Domain folder. Below is a comparison of the " +
                    "Domain folders of a Course export and a complete GIFT export: <br/><br/><img src=\"" +
                    EXPORT_COMPARISON_IMG+ "\" style=\"margin: auto; display:block;\" alt=\"Comparison of the " +
                    "Domain folders located in GIFT export files.\">",
                    null);
        }
        
        Map<File, File> conflictMap = new HashMap<File, File>();
        
        if(workspaceFolder != null){
        
        	// Check for conflicting courses
	        File domainDir = new File(tempDirectory, "Domain");
	        
	        conflictMap.putAll(getCourseConflicts(domainDir, workspaceFolder));
	        
        }
        progress.increaseSubtaskProgress(COURSE_CONFLICTS_RETRIEVED_PROGRESS);
                
        // Check for conflicting images
        File imagesDir = new File(tempDirectory, resourcesDir);
        
        if(imagesDir.exists()) {
        	
        	conflictMap.putAll(getImageConflicts(imagesDir));
                        
        }
        progress.increaseSubtaskProgress(IMAGE_CONFLICTS_RETRIEVED_PROGRESS);
        
        if(!conflictMap.isEmpty()){
        	
        	FileUtils.deleteQuietly(tempDirectory);
        	return conflictMap;
        }
    		    	
    	progress.increaseSubtaskProgress(TOTAL_PROGRESS);
    	FileUtils.deleteQuietly(tempDirectory);
    	
    	return null;
    }
    
    /**
     * Recursively copies src to dest
     * 
     * @param src The source directory
     * @param rootFolder the root of the course folders.  Where to import course folders to.
     * @param errorBuffer contains errors collected along the way while importing the course folders
     * @param progressIndicator used to notify the user of import progress
     * @param isInitialCall whether this call to this method is the initial call.  Used to collect course folders versus collection all folders
     * that are created in the set of recursive calls to this method.  Obviously a better way to do this would be to re-factor this method.
     * @throws DetailedException if there was a problem importing a course folder
     */
    private void importDomainDirectory(File src, 
            AbstractFolderProxy rootFolder, StringBuffer errorBuffer, ProgressIndicator progressIndicator, 
            boolean isInitialCall) throws DetailedException {
        
        // If source doesn't exist and is not a directory, return.
        if(!(src.exists() && src.isDirectory())) {
            logger.error("Could not copy " + src.getAbsolutePath() + " source directory doesn't exist or path is not a directory");
            return;
        }
        
        // At this point source and destination should be valid
        File[] files = src.listFiles();
        for(File f : files) {
            if(f.isDirectory()) {
                
                //get the destination folder in GIFT based on the temp folder name, create it and use it for future import logic here
                AbstractFolderProxy newSubfolder;
                try{
                    newSubfolder = rootFolder.createFolder(f.getName());
                } catch(IOException e) {
                    throw new DetailedException("There was a problem while trying to create the directory '" + f.getName() + "' in '" + rootFolder.getFileId()+"'.", 
                            "An exception was thrown with the message "+e.getMessage()+".",
                            e);
                }
//                File newFolder = new File(rootFolder.getFileId() + File.separator + f.getName());
//                newFolder.mkdir();
//                AbstractFolderProxy newSubfolder = new DesktopFolderProxy(newFolder);
                if(isInitialCall){
                    createdCourseFolders.add(newSubfolder);
                }
                
                try{
                    importDomainDirectory(f, newSubfolder, errorBuffer, progressIndicator, false);
                } catch(DetailedException e) {
                    throw new DetailedException( e.getReason(), e.getDetails(),
                            e);
                }

            } else if(f.isFile()) {
                try {
                	
                	if(f.getAbsolutePath().endsWith(FileUtil.SURVEY_REF_EXPORT_SUFFIX)){
                		
                		//don't import survey export files, since they should have already been handled by this point
                		continue;
                	}
                    
                    currentFileCount++;
                    progressIndicator.setSubtaskProgress((int)(((float)currentFileCount / (float)totalFileCount) * TOTAL_PROGRESS));
                    progressIndicator.setPercentComplete((progressIndicator.getSubtaskProgress() / (TOTAL_PROGRESS / IMPORT_DOMAIN_DIR_TOTAL_PROGRESS)) + SETUP_FOR_IMPORT);  
                    
                    rootFolder.createFile(f, null);
                    
//                  if(d.exists()){
//                      //backup existing file
//                      File backup = new File(d.getAbsolutePath() + BACKUP_SUFFIX);
//                      FileUtils.copyFile(d, backup);
//                      errorBuffer.append("\nBacking up ").append(d).append(" to ").append(backup).append(".");
//                  }                        
                  
                    if(isInfoEnabled){
                        logger.info("Copying " + f.getAbsolutePath() + " to " + rootFolder);
                    }
                    
                    if(tryCancel(progressIndicator)) return;
                } catch(IOException e) {
                    throw new DetailedException("There was a problem while trying to copy '" + f.getName() + "' to '" + rootFolder.getFileId() + File.separator + f.getName()+"'.", 
                            "An exception was thrown with the message "+e.getMessage()+".",
                            e);
                }
            }
        }
    }
    
    /**
     * Begins a file related task involving one or more files. 
     * 
     * @param files collection of files about to perform a task on
     */
    private void beginFilesTask(Collection<File> files){
        
        int cnt = 0;
        for(File file : files){
            
            if(file.isDirectory()){
                cnt += FileUtil.countFilesInDirectory(file);
            }else{
                cnt++;
            }
        }
        
        totalFileCount = cnt;
    }
    
    /**
     * Begins a file task.  For now this means calculating the total number of files that will be processed.
     * 
     * @param dir The file to calculate the number of files that will be processed.
     */
    private void beginFileTask(File dir) {
        
        if(dir.isDirectory()){
            totalFileCount = FileUtil.countFilesInDirectory(dir);
        }else{
            totalFileCount = 1;
        }
    }
    
    /**
     * Ends a directory task and resets class variables.
     */
    private void endDirectoryTask() {
        currentFileCount = 0;
        totalFileCount = 0;
    }
    
    /**
     * Check whether the course(s) being imported need to be converted.  If they do then automatically
     * convert them while backing up the original versions.
     *  
     * @param tempDomainDirectory - the directory containing the domain content being imported 
     * @return collection of course files that were converted.  Can be empty but not null.
     * @throws DetailedException if there was a problem converting a course, backing up the original file or writing the converted
     * course to disk.
     */
    private List<FileProxy> checkAndConvertCourses(File tempDomainDirectory) throws DetailedException{
        
        List<FileProxy> files = new ArrayList<>();
        DesktopFolderProxy tempDomain = new DesktopFolderProxy(tempDomainDirectory);
        try{
            FileFinderUtil.getFilesByExtension(tempDomain, files, AbstractSchemaHandler.COURSE_FILE_EXTENSION);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve all course.xml files when importing", "An exception was thrown with the message "+e.getMessage()+".", e);
        }
        
        return convertFiles(files, FileType.COURSE);
        
    }
    
    /**
     * Write any conversion issues to the summary file.
     * 
     * @param conversionIssueList if null or there are no issues then nothing will be written to 
     * the import summary
     */
    private void writeConversionIssuesToSummary(ConversionIssueList conversionIssueList){
        
        if(conversionIssueList == null || conversionIssueList.isEmpty()){
            return;
        }
        
        Set<Map.Entry<String, HashMap<String, Integer>>> set = conversionIssueList.entrySet();
        Iterator<Map.Entry<String, HashMap<String, Integer>>> i = set.iterator();
        
        // Info has been stored in issueList like so:
        // LinkedHashMap<"file name", HashMap<"element name", "occurrences">>
        Map.Entry<String, HashMap<String, Integer>> entry;
        
        // Iterate through each file name
        while (i.hasNext()) {
            entry = i.next();
            
            Iterator<Map.Entry<String, Integer>> messageIterator = entry.getValue().entrySet().iterator();
            
            // For the current file name, iterate though every element stored in the HashMap
            while (messageIterator.hasNext()) {
                Map.Entry<String, Integer> pairs = messageIterator.next();
                readmeWriter.println();
                readmeWriter.append(pairs.getKey()).append("\n").append("Occurrences within file: ").append(pairs.getValue().toString());
            }

        }
    }
    
    /**
     * Check whether the DKFs(s) being imported need to be converted.  If they do then automatically
     * convert them while backing up the original versions.
     *  
     * @param tempDomainDirectory - the directory containing the domain content being imported 
     * @return collection of DKFs that were converted.  Can be empty but not null.
     * @throws DetailedException if there was a problem converting a DKF, backing up the original file or writing the converted
     * DKF to disk.
     */
    private List<FileProxy> checkAndConvertDKFs(DesktopFolderProxy tempDomainDirectory) throws DetailedException{
        
        List<FileProxy> files = new ArrayList<>();
        try{
            FileFinderUtil.getFilesByExtension(tempDomainDirectory, files, AbstractSchemaHandler.DKF_FILE_EXTENSION);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve all DKFs when importing", "An exception was thrown with the message "+e.getMessage()+".", e);
        }
        
        return convertFiles(files, FileType.DKF);
        
    }
    
    /**
     * Check whether the Metadata(s) being imported need to be converted.  If they do then automatically
     * convert them while backing up the original versions.
     *  
     * @param tempDomainDirectory - the directory containing the domain content being imported 
     * @return collection of Metadatas that were converted.  Can be empty but not null.
     * @throws DetailedException if there was a problem converting a Metadata, backing up the original file or writing the converted
     * Metadata to disk.
     */
    private List<FileProxy> checkAndConvertMetadatas(DesktopFolderProxy tempDomainDirectory) throws DetailedException{
        
        List<FileProxy> files = new ArrayList<>();
        try{
            FileFinderUtil.getFilesByExtension(tempDomainDirectory, files, AbstractSchemaHandler.METADATA_FILE_EXTENSION);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve all Metadatas when importing", "An exception was thrown with the message "+e.getMessage()+".", e);
        }
        
        return convertFiles(files, FileType.METADATA);
        
    }
    
    /**
     * Check whether the TrainingAppRef(s) being imported need to be converted.  If they do then automatically
     * convert them while backing up the original versions.
     *  
     * @param tempDomainDirectory - the directory containing the domain content being imported 
     * @return collection of TrainingAppRefs that were converted.  Can be empty but not null.
     * @throws DetailedException if there was a problem converting a TrainingAppRef, backing up the original file or writing the converted
     * TrainingAppRef to disk.
     */
    private List<FileProxy> checkAndConvertTrainingAppRefs(DesktopFolderProxy tempDomainDirectory) throws DetailedException{
        
        List<FileProxy> files = new ArrayList<>();
        try{
            FileFinderUtil.getFilesByExtension(tempDomainDirectory, files, AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve all training app references when importing", "An exception was thrown with the message "+e.getMessage()+".", e);
        }
        
        return convertFiles(files, FileType.TRAINING_APP_REFERENCE);
        
    }
    
    /**
     * Check whether the Sensor Configuration(s) being imported need to be converted.  If they do then automatically
     * convert them while backing up the original versions.
     *  
     * @param tempDomainDirectory - the directory containing the domain content being imported 
     * @return collection of sensor configurations that were converted.  Can be empty but not null.
     * @throws DetailedException if there was a problem converting a sensor configurations, backing up the original file or writing the converted
     * sensor configurations to disk.
     */
    private List<FileProxy> checkAndConvertSensorConfigs(DesktopFolderProxy tempDomainDirectory) throws DetailedException{
        
        List<FileProxy> files = new ArrayList<>();
        try{
            FileFinderUtil.getFilesByExtension(tempDomainDirectory, files, AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve all sensor configurations when importing", "An exception was thrown with the message "+e.getMessage()+".", e);
        }
        
        return convertFiles(files, FileType.SENSOR_CONFIGURATION);
        
    }
    
    /**
     * Check whether the Learner Configuration(s) being imported need to be converted.  If they do then automatically
     * convert them while backing up the original versions.
     *  
     * @param tempDomainDirectory - the directory containing the domain content being imported 
     * @return collection of Learner Configurations that were converted.  Can be empty but not null.
     * @throws DetailedException if there was a problem converting a learner configurations, backing up the original file or writing the converted
     * learner configurations to disk.
     */
    private List<FileProxy> checkAndConvertLearnerConfigs(DesktopFolderProxy tempDomainDirectory) throws DetailedException{
        
        List<FileProxy> files = new ArrayList<>();
        try{
            FileFinderUtil.getFilesByExtension(tempDomainDirectory, files, AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve all learner configurations when importing", "An exception was thrown with the message "+e.getMessage()+".", e);
        }
        
        return convertFiles(files, FileType.LEARNER_CONFIGURATION);
        
    }
    
    /**
     * Check whether the Pedagogy Configuration(s) being imported need to be converted.  If they do then automatically
     * convert them while backing up the original versions.
     *  
     * @param tempDomainDirectory - the directory containing the domain content being imported 
     * @return collection of Pedagogy Configurations that were converted.  Can be empty but not null.
     * @throws DetailedException if there was a problem converting a pedagogy configurations, backing up the original file or writing the converted
     * pedagogy configurations to disk.
     */
    private List<FileProxy> checkAndConvertPedagogyConfigs(DesktopFolderProxy tempDomainDirectory) throws DetailedException{
        
        List<FileProxy> files = new ArrayList<>();
        try{
            FileFinderUtil.getFilesByExtension(tempDomainDirectory, files, AbstractSchemaHandler.EMAP_PEDAGOGICAL_CONFIG_FILE_EXTENSION);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve all pedagogy configurations when importing", "An exception was thrown with the message "+e.getMessage()+".", e);
        }
        
        return convertFiles(files, FileType.EMAP_PEDAGOGICAL_CONFIGURATION);
        
    }
    
    /**
     * Helper method used to convert files upon importing a course. Called by each
     * file type, such as learner config and course files. This method should be called
     * using a list of the type of file to be converted, such as a list of sensor configs
     * and FileType.SENSOR_CONFIGURATION to convert ALL the sensor congifs in the course. 
     * 
     * @param files - list of files to be converted. All should be of the same type
     * @param type - the type of the list of files being converted
     * 
     * @return a list of converted files
     */
    private List<FileProxy> convertFiles(List<FileProxy> files, FileType type){
        
        List<FileProxy> convertedFiles = new ArrayList<>();
        
        for(FileProxy file : files){
            
            try {
                AbstractConversionWizardUtil conversionUtil = AbstractConversionWizardUtil.getConversionUtil(file, false);
                
                if (conversionUtil != null) {
                    //conversion needed
                    //Note: unmarshal even if there is a schema validation in order to support disabled (incomplete) course objects [#3570]
                    UnmarshalledFile newFile = conversionUtil.convertFile(type, file, false, false);
                
                    if(newFile == null){
                        throw new DetailedException("Failed to convert the contents of the file '"+file+"'.",
                                "Attempted to convert that file starting at the GIFT schema version "+conversionUtil.getWorkingVersion()+" to the latest schema version of "+Version.getInstance().getCurrentSchemaVersion()+" but failed to convert it's contents.", null);
                    }
                    
                    //backup original file (with non .xml file extension)
                    String origFilename = file.getFileId();
                    File backup = new File(origFilename + Constants.BACKUP_SUFFIX);
                    FileUtils.copyInputStreamToFile(file.getInputStream(), backup);
                    
                    if(type == FileType.COURSE) {
		            	// Rename the course if necessary
                    	
		            	Course course = (Course) newFile.getUnmarshalled();
	            		
	                    //Note: even though the contents are converted there maybe elements that need to be authored by the user
	                    //      therefore the contents are NOT schema valid but we still want the file to be written
		            	String finalCoursePath = renameCourse(course, file);
                        AbstractSchemaHandler.writeToFile(newFile.getUnmarshalled(), new File(finalCoursePath), true);
                    } else {
                        AbstractSchemaHandler.writeToFile(newFile.getUnmarshalled(), new File(file.getFileId()), true);
                    }
                    
                    convertedFiles.add(file);
                    
                    //write conversion issues to summary
                    ConversionIssueList issues = conversionUtil.getConversionIssueList();
                    writeConversionIssuesToSummary(issues);
                    
                }
            } catch (LatestVersionException e) {
                //nothing to convert
            	if(type == FileType.COURSE) {
                	// Check to see if there is an existing course with the same name
            		
					try {
						// Rename the course if necessary
						
						Course course = (Course) AbstractSchemaHandler.parseAndValidate(file.getInputStream(), FileType.COURSE, false).getUnmarshalled();
						
                        //Note: even though the contents are converted there maybe elements that need to be authored by the user
                        //      therefore the contents are NOT schema valid but we still want the file to be written
		            	String finalCoursePath = renameCourse(course, file);
		            	AbstractSchemaHandler.writeToFile(course, new File(finalCoursePath), true);
	                	
					} catch (Exception e1) {
						throw new DetailedException("There was a problem parsing the file '"+file.getName()+"'.", "An exception was thrown with the message "+e.getMessage()+".", e1);
					}
                }
            	
            } catch (UnsupportedVersionException e) {
                //the file is pre-version 2.0 and can't be used.
                throw new DetailedException("Unable to up convert the file '"+file.getName()+"'.", "Found an unsupported version with exception : "+e.getMessage(), e);
            } catch (SAXException | JAXBException | FileNotFoundException e){
                throw new DetailedException("There was a problem parsing the file '"+file.getName()+"'.", "An exception was thrown with the message "+e.getMessage()+".", e);
            } catch (IOException e){                
                throw new DetailedException("There was a problem reading/writing the contents of the file '"+file.getName()+"'.", "An exception was thrown with the message "+e.getMessage()+".", e);    
            } catch (Exception e){
                throw new DetailedException("There was a general problem when converting the file "+file.getName()+"'.", "An exception was thrown with the message "+e.getMessage()+".", e);
            }
        }
        
        return convertedFiles;
    }
        
    /**
     * Renames a course.xml file as well as the course folder and any survey exports associated with the course.
     * <br>
     * This method checks to see if the course file path is present in the courseToNameMap. If so, the matching name will be 
     * used to update the course. If there is no matching key, the course object name will be used to rename the course. 
     * 
     * @param course The serializable course object. 
     * @param file The file representation of the course object.<br/>
     * For course imports the absolute path might look like<br/>
     * E:\work\GIFT\temp\GIFT145208715350629022331817333020175000\Domain\SE Sandbox - React to Contact - Desert (Adaptive ExCon Playback)\SE Sandbox - React to Contact - Desert (Adaptive ExCon Playback).course.xml<br/>
     * Notice the course folder and course xml have different names ('Playback').
     * @return Will either be the original absolute path if no rename happened in this method or
     * the new path to the course file that has been renamed<br/>
     * E.g. E:\work\GIFT\temp\GIFT38169247527347547971820376692429100\Domain\SE Sandbox - React to Contact - Desert (Adaptive ExCon Playback)\SE Sandbox - React to Contact - Desert (Adaptive ExCon Playback).course.xml
     * @throws DetailedException if there was an error renaming a the course file or updating the survey export files.
     */
    private String renameCourse(Course course, FileProxy file) throws DetailedException {
       
        StringBuilder log = new StringBuilder();
        log.append("renameCourse: ")
            .append(file.getFileId())
            .append("\n");
        
    	int extIndex = file.getFileId().lastIndexOf(AbstractSchemaHandler.COURSE_FILE_EXTENSION);
    	int nameIndex = file.getFileId().lastIndexOf(File.separator) + 1;
    	
    	String currentFolderPath = file.getFileId().substring(0, nameIndex - 1);
    	int folderNameIndex = currentFolderPath.lastIndexOf(File.separator) + 1;
    	
    	String userFolderPath =  currentFolderPath.substring(0, folderNameIndex);
    	
    	String finalCourseName = null;
    	String originalCourseName = finalCourseName = file.getFileId().substring(nameIndex, extIndex);
    	
    	String domainRelativePath = file.getFileId();
    	domainRelativePath = domainRelativePath.substring(domainRelativePath.indexOf("Domain"));
    	
    	if(courseToNameMap != null && courseToNameMap.containsKey(domainRelativePath)) {
    		finalCourseName = courseToNameMap.get(domainRelativePath);
    		
    	}  else if(course.getName() != null){
    		
    		//if the course object has a name assigned to it, save the course folder and XML file under that name
    		finalCourseName = course.getName();
    		
    	} else {
    		
    		//otherwise, save them using the name of the XML file
    		finalCourseName = originalCourseName;
    	}
    	
    	log.append("Updating course name from '")
    	    .append(originalCourseName)
    	    .append("' to '")
    	    .append(finalCourseName)
    	    .append("'\n");
    	
    	course.setName(finalCourseName);
    	
    	//save new object to original file name if the course name changed
    	String newCourseXml =  finalCourseName + AbstractSchemaHandler.COURSE_FILE_EXTENSION;
    	String oldCourseXml = originalCourseName + AbstractSchemaHandler.COURSE_FILE_EXTENSION;
    	if(!finalCourseName.equals(originalCourseName)) {
    		try {    		    
    		    
    		    String oldCoursePath = currentFolderPath + File.separator + oldCourseXml;
    		    String newCoursePath = currentFolderPath + File.separator + newCourseXml;
    			File newCourseFolder = new File(userFolderPath + finalCourseName);
    			
    			log.append("Renaming course file from '")
    			    .append(oldCoursePath)
    			    .append("' to '")
    			    .append(newCoursePath)
    			    .append("'\n");
    			
    			Files.move(new File(oldCoursePath).toPath(), new File(newCoursePath).toPath());
    			
    			log.append("Renaming course folder from '")
    			    .append(currentFolderPath)
    			    .append("' to '")
    			    .append(newCourseFolder)
    			    .append("'\n");
    			
    			Files.move(new File(currentFolderPath).toPath(), newCourseFolder.toPath());
    			renameSurveyExports(newCourseFolder, originalCourseName, finalCourseName);
    			
    		} catch (IOException e) {
    			throw new DetailedException(
    			        "An error occurred while trying to rename the file '" + oldCourseXml + "' to '" + newCourseXml + "'", 
    			        e.getMessage(), 
    			        e);
    		} catch (DetailedException e) {
    		    throw e;
    		}
    	}
    	

        if(logger.isInfoEnabled()) {
            logger.debug(log.toString());
        }
    	
    	String finalCoursePath = userFolderPath + finalCourseName + File.separator + finalCourseName + AbstractSchemaHandler.COURSE_FILE_EXTENSION;
    	return finalCoursePath;
    }
    
    /**
     * Renames course.surveys.export file
     * @param courseFolderPath The path to the course folder
     * @param oldCourseName The original course name
     * @param newCourseName The new course name
     * @throws DetailedException if there was an error renaming the file
     */
    private void renameSurveyExports(File courseFolderPath, final String oldCourseName, String newCourseName) throws DetailedException {
    	File surveyExport = new File(courseFolderPath + File.separator + oldCourseName + CourseDBImporter.COURSE_SURVEY_REF_EXPORT_SUFFIX);
    	if(surveyExport.exists()) {
    		File newSurveyExport = new File(courseFolderPath + File.separator + newCourseName + CourseDBImporter.COURSE_SURVEY_REF_EXPORT_SUFFIX);
    		try {
    			Files.move(surveyExport.toPath(), newSurveyExport.toPath());
    		} catch (IOException e) {
    			throw new DetailedException(
    			        "An error occurred while trying to rename the file '" + surveyExport.getName() + "' to " + "'" + newSurveyExport.getName() + "'", 
    			        e.getMessage(), 
    			        e);
    		}
    	}
    }
    
    /**
     * Return whether a cancel export operation has taken place.
     *  
     * @param progressIndicator used to show export progress to the user 
     * @return boolean - true iff the export process is being or has been canceled
     * @throws DetailedException 
     */
    private boolean tryCancel(ProgressIndicator progressIndicator) throws DetailedException {
        
        if(!progressIndicator.shouldCancel()) {
            return false;
        }
        
//        if(cancelDone) {
//            return true;
//        }
        
        performCleanup(progressIndicator, false);
//        cancelDone = true;
        return true;
    }
    
    /**
     * Delete files and directories created during the export process.
     * 
     * @param progressIndicator used to notify the user of import progress
     * @param successfulExport whether the cleanup is being performed as a result of a successful export or not.
     */
    private void performCleanup(ProgressIndicator progressIndicator, boolean successfulExport){
        
        //delete temp files
        for(File file : createdFiles.keySet()) {
            
            boolean deleteAlways = createdFiles.get(file);
            if(deleteAlways || !successfulExport){
            
                if(file.isDirectory()) {
                    deleteDirectory(file, progressIndicator);
                } else if(file.isFile()) {
                    FileUtils.deleteQuietly(file);
                }
            }
        }
        
        if(!successfulExport){
            //delete created course folders
            for(AbstractFolderProxy courseFolder : createdCourseFolders){
                try{
                    workspaceFolder.deleteFolder(courseFolder.getName());
                }catch(IOException e){
                    logger.error("Failed to cleanup failed import because there was an error while deleting the created folder of '"+courseFolder.getFileId()+"'.", e);
                }
            }
        }
    }
    
    /**
     * Recursively deletes directory files in order to display progress to the user.
     * 
     * @param dir The directory to delete
     * @param progressIndicator used to show export progress to the user
     */
    private void deleteDirectory(File dir, ProgressIndicator progressIndicator) {
        
        if(!(dir.exists() && dir.isDirectory())) {
            logger.error("Could not delete " + dir.getAbsolutePath() + " directory doesn't exist or path is not a directory");
            return;
        }
        
        File[] files = dir.listFiles();
        for(File f : files) {
            if(f.isDirectory()) {
                deleteDirectory(f, progressIndicator);
            } else if(f.isFile()) {
                currentFileCount++;
                int progressSoFar = SETUP_FOR_IMPORT;
                progressIndicator.setSubtaskProgress((int)(((float)currentFileCount / (float)totalFileCount) * TOTAL_PROGRESS));
                progressIndicator.setPercentComplete((progressIndicator.getSubtaskProgress() / (TOTAL_PROGRESS / CLEANUP_TOTAL_PROGRESS)) + progressSoFar);
                
                if(isInfoEnabled){
                    logger.info("Deleting " + f.getAbsolutePath());
                }
                
                if(!FileUtils.deleteQuietly(f)) {
                    logger.error("Could not delete " + f.getAbsolutePath());
                }
            }
        }
        
        //now that all files have been deleted, delete the directory
        dir.delete();
    }
}
