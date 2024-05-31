/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.SurveyCheckRequest;
import mil.arl.gift.common.course.CourseFileValidationException;
import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.course.dkf.DKFValidationException;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.ContentProxyInterface;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.ExportProperties;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.PlatformUtils;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.ZipUtils;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.api.message.codec.json.survey.SurveyContextJSON;
import mil.arl.gift.net.nuxeo.DocumentExistsException;
import mil.arl.gift.net.nuxeo.QuotaExceededException;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.UMSHibernateUtil;
import mil.arl.gift.ums.db.survey.Surveys;

/**
 * This class contains logic to export a collection of courses.
 * 
 * @author mhoffman
 *
 */
public class ExportCourseUtil {
    
    private static Logger logger = LoggerFactory.getLogger(ExportCourseUtil.class);
    
    public static final FastDateFormat BACKUP_FILE_TIME_FORMAT = FastDateFormat.getInstance("MMddyy-HHmmss", null, null);    
    /** 
     * directories and files used for exporting 
     */
    private static final String EXPORT_RESOURCES_DIRECTORY = "data" + File.separator + "exportResources"; 

    private static final String GIFT_EXPORT_README_FILENAME = "README.txt";
    private static final String GIFT_EXPORT_README_FILENAME_RESOURCE = EXPORT_RESOURCES_DIRECTORY + File.separator + "GIFTExport.README.txt";
    
    private static final String DOMAIN_EXPORT_README_FILENAME_RESOURCE = EXPORT_RESOURCES_DIRECTORY + File.separator + "DomainExport.README.txt";   
    
    /** name of the hibernate configuration file to use for the export logic that will setup an embedded UMS db connection */
    private static final String EXPORTING_CONFIG_FILE = ".." + File.separator + "data" + File.separator + "exportResources" + File.separator + "exporting.ums.hibernate.cfg.xml";
    
    /** the export script file path for Windows*/
    private static final String EXEC_BAT = "scripts" + File.separator + "dev-tools" + File.separator + "export.bat";
    
    /** the export script file path for Linux*/
    private static final String EXEC_BAT_LINUX = "scripts" + File.separator + "dev-tools" + File.separator + "export.sh";
    
    /** 
     * Total percentage contribution for each task 
     * Note: should total 100
     */
    //required steps...
    private static final int GATHER_COURSES = 25;
    private static final int COPY_TEMP_DIRECTORY_TOTAL_PROGRESS = 25;
    private static final int GENERATE_LAUNCH_SCRIPT_TOTAL_PROGRESS = 5;
    private static final int ZIP_OUTPUT_DIRECTORY_TOTAL_PROGRESS = 25;
    private static final int CLEANUP_TOTAL_PROGRESS = 10;
    private static final int EXPORT_DATABASE_TOTAL_PROGRESS = 10;
    
    /**
     * subtask status entries
     */
    private static final String CONFIGURING_DB  = "Configuring/Exporting Database Entries...";
    private static final String CHECKING_COURSES    =" Checking Courses...";
    private static final String COPY_TO_TEMP    = "Copying to temporary directory...";
    private static final String COPY_RESOURCES  = "Copying resource files...";
    private static final String CREATING_ZIP    = "Creating zip file...";
    private static final String CLEANING_UP     = "Cleaning up...";
    private static final String EXPORT_SUCCESSFUL   = "GIFT Export Successful!";
    
    private static final int TOTAL_PROGRESS = 100;
    private static final int ZERO = 0;
    
    /** 
     * Collection of files created during the export process.
     * This is used for removing files after a failed or successful export. 
     * 
     * Key: file created
     * Value: boolean whether or not the file should be deleted at the end of a successful export.
     */
    private Map<File, Boolean> createdFiles = new HashMap<>();
    
    /**
     * Collection of pre-existing files that were renamed during the export process.
     * This is used for reverting the renames after a failed export or successful export.
     * 
     * Key: renamed file created
     * Value: original file
     */
    private Map<File, File> renamedFiles = new HashMap<>();
    
    /** used for showing progress updates to user */
    private int totalFileCount = 0;
    private int currentFileCount = 0;
    
    public ExportCourseUtil() {}
    
    /**
     * Return the total size in MB of the course folders to export.
     * 
     * @param courseFolders collection of course folders to export
     * @return the size in MB of those course folders
     * @throws IOException if there was a problem accessing the course folders or determining their file sizes
     */
    public float getCourseFoldersSize(List<AbstractFolderProxy> courseFolders) throws IOException{
        
        float sizeMB = 0;
        for(AbstractFolderProxy folder : courseFolders){
            sizeMB += folder.getSize();
        }
        
        return sizeMB;
    }

    /**
     * Export the specified courses to a zip file.<br/>
     * The structures of the zip is as follows:<br/>
     * README.txt<br/>
     * Domain/
     * <ul>
     *    <li>course folder name</li>
     *    <li>course folder name</li>
     * </ul>
     * GIFT/
     * <ul>
     *    <li> contains core GIFT files needing to run a GIFT instance</li>
     *    <li>only included if exporting more than just courses</li>
     * </ul>
     * Training.Apps/
     * <ul>
     *    <li>contains training application software and supporting files</li>
     *    <li>only included if exporting more than just courses</li>
     * </ul>
     * 
     * @param courseFolders the course folders to include in the export
     * @param exportProperties used to configure the export
     * @param outputFile the zip file to write the export too. Note: If this file already exists it will be deleted.
     * @throws IOException
     */
    public void export(List<AbstractFolderProxy> courseFolders, 
            ExportProperties exportProperties, File outputFile) throws IOException{
        
        ProgressIndicator progressIndicator = exportProperties.getProgressIndicator();
        
        DomainContent domainContent = new DomainContent();
        for(AbstractFolderProxy courseFolder : courseFolders){
            domainContent.addExportFile(courseFolder);
        }

        File tempDirectory = null;
        
        StringBuffer exportOutputStringBuffer = new StringBuffer();
        boolean exportCreated = false;
        UUID errorId = UUID.randomUUID();
        DetailedException error = null;
        
        try{
        
            ////////////////////////////////////////////////////////////////////////////////////////////////
            //
            // Retrieve current UMS hibernate settings
            //
            String configFileName = "ums" + File.separator + "ums.hibernate.cfg.xml";
            Configuration hibernateConfig = new Configuration();            
            File hibernateConfigFile = new File(PackageUtil.getConfiguration() + File.separator + configFileName);

            if (!hibernateConfigFile.exists()) {
                throw new FileNotFoundException("The Hibernate config file '" + configFileName + "' could not be found.");
            }

            hibernateConfig.configure(hibernateConfigFile);
            ////////////////////////////////////////////////////////////////////////////////////////////////
            
            if(logger.isInfoEnabled()){
                logger.info("Exporting to " + outputFile.getAbsolutePath());
            }
            
            // Copy GIFT into a temp directory with the domain content that was selected.
            File giftRoot = new File("..");
            tempDirectory = File.createTempFile("GIFT", Long.toString(System.nanoTime()));
            tempDirectory.delete();
            
            if(!ZipUtils.createDir(tempDirectory)){
               throw new IOException("Unable to create directory named "+tempDirectory+".");
            }
            
            //delete the temp directory even upon success
            createdFiles.put(tempDirectory, true);
            
            //check if user has selected the cancel button
            if(tryCancel(progressIndicator)){
                return;
            }
            
            //mapping of course folder XML files (e.g. course.xml) to database export temp file
            //used to copy that temp file to the same location as the exported XML file
            Map<FileProxy, File> surveyRefSrcToExportFile = new HashMap<>();
            
            if(exportProperties.isCoursesOnly()){
                // Export the appropriate database entries (not the entire database)
                //Note: this has to be done before GIFT is copied to the temporary directory
                
                if(logger.isInfoEnabled()){
                    logger.info("Configuring/Exporting Database Entries");
                }
                progressIndicator.updateSubtaskDescription(CONFIGURING_DB);
                
                //create a temporary directory to place database export files that are created
                File tempDbExportDirectory = File.createTempFile("GIFT-DdExports", Long.toString(System.nanoTime()));
                tempDbExportDirectory.delete();
                
                if(!ZipUtils.createDir(tempDbExportDirectory)){
                   throw new IOException("Unable to create directory named "+tempDbExportDirectory+".");
                }
                
                //delete the temp directory even upon success
                createdFiles.put(tempDbExportDirectory, true);
                
                exportDatabase(createdFiles, renamedFiles, surveyRefSrcToExportFile, tempDbExportDirectory, domainContent, exportProperties.isCoursesOnly());
                progressIndicator.setSubtaskProgress(TOTAL_PROGRESS);
                
                //check if user has selected the cancel button
                if(tryCancel(progressIndicator)){
                    return;
                }               
                
                progressIndicator.increasePercentComplete(EXPORT_DATABASE_TOTAL_PROGRESS);
            }
            
            ////////////////////////////////////////////////////////////////////////////////////////////////
            //
            // Copy appropriate GIFT files into the temp directory
            //

            //gather course resources
            progressIndicator.setSubtaskProgress(ZERO);
            progressIndicator.updateSubtaskDescription(CHECKING_COURSES);
            List<File> domainResources = getExternalDomainResources(domainContent, progressIndicator);
            
            if(logger.isInfoEnabled()){
                logger.info("Copying GIFT into temp directory " + tempDirectory.getAbsolutePath());
            }
            progressIndicator.setSubtaskProgress(ZERO);
            progressIndicator.updateSubtaskDescription(COPY_TO_TEMP);
            ExportFileFilter filter = new ExportFileFilter(exportProperties.isCoursesOnly(), exportProperties.shouldExportUserData(), domainResources);
            
            //copy core GIFT files according to filter
            beginFileTask(giftRoot);
            int origPercentComplete = progressIndicator.getPercentComplete();
            copyDirectory(giftRoot, tempDirectory, filter, progressIndicator, origPercentComplete);
            
            endDirectoryTask();
            beginFileTask(courseFolders);
            
            //copy course folders to temp Domain folder
            File tempDomainDirectory = new File(tempDirectory + File.separator + "Domain");
            origPercentComplete = progressIndicator.getPercentComplete();
            for(AbstractFolderProxy courseFolder : courseFolders){
                copyDirectory(courseFolder, courseFolder, tempDomainDirectory, surveyRefSrcToExportFile, progressIndicator, origPercentComplete);
            }
            
            endDirectoryTask();

            // removes the LTI provider sensitive information from the course xml in the temp directory.
            removeLtiProviders(tempDomainDirectory, domainContent);
            
            //check if user has selected the cancel button
            if(tryCancel(progressIndicator)){
                return;
            }

            //should already be 100 but just to make sure
            progressIndicator.setSubtaskProgress(TOTAL_PROGRESS);

            ////////////////////////////////////////////////////////////////////////////////////////////////

            // check if user has selected the cancel button
            if (tryCancel(progressIndicator)) {
                return;
            }
            
            if(!exportProperties.isCoursesOnly()){
                //export entire database
                
                configureDerbyDatabase(tempDirectory, exportProperties.isCoursesOnly());                
                
                progressIndicator.increasePercentComplete(EXPORT_DATABASE_TOTAL_PROGRESS);
            }  
            
            //Check if user data should be removed from the GIFT database(s)
            //Note: if exporting domain content only, don't clear UMS user data because the db being
            //      used is the one associated with the GIFT instance running this export tool
            if(!exportProperties.shouldExportUserData() && !exportProperties.isCoursesOnly()){
                clearUMSUserData(tempDirectory);
            }

            
            //check if user has selected the cancel button
            if(tryCancel(progressIndicator)){
                return;
            }
            
            // Generate the launch script
            if(logger.isInfoEnabled()){
                logger.info("Copying resource files");
            }
            progressIndicator.setSubtaskProgress(ZERO);
            progressIndicator.updateSubtaskDescription(COPY_RESOURCES);
            copyResources(tempDirectory, domainContent, exportProperties.isCoursesOnly());
            
            progressIndicator.setSubtaskProgress(TOTAL_PROGRESS);
            progressIndicator.increasePercentComplete(GENERATE_LAUNCH_SCRIPT_TOTAL_PROGRESS);           
            
            if(outputFile.exists()){
                //delete the old export file since the user has chosen to over-write it in a previous step                
                outputFile.delete();
            }
                        
            // Zip into output file
            if(logger.isInfoEnabled()){
                logger.info("Zipping into output file " + outputFile.getAbsolutePath());
            }
            File exportScript;

            progressIndicator.setSubtaskProgress(ZERO);
            progressIndicator.updateSubtaskDescription(CREATING_ZIP);
            
            //Determine export script to use based on OS
            if(PlatformUtils.getFamily() == PlatformUtils.SupportedOSFamilies.UNIX) {
                exportScript = new File(EXEC_BAT_LINUX);
            } else {
            	exportScript = new File(EXEC_BAT);
            }
            String base = tempDirectory.getAbsolutePath();
            String outputParam = outputFile.getAbsolutePath();
            
            //check if user has selected the cancel button
            if(tryCancel(progressIndicator)){
                return;
            }
            
            //delete the output zip upon failure, not success
            createdFiles.put(outputFile, false);            
            
            String command = "\"" + exportScript.getPath() + "\" \"" + base + "\" \"" + outputParam + "\"";
            
            if(logger.isInfoEnabled()){
                logger.info("Executing: " + command);
            }
            
            //determine process to use based on OS
            ProcessBuilder builder;
            if(PlatformUtils.getFamily() == PlatformUtils.SupportedOSFamilies.UNIX) {
                builder = new ProcessBuilder("bash", "-c", command);
            } else {
                builder = new ProcessBuilder(command);
            }
            
            builder.redirectErrorStream(true);
            Process process = builder.start();          
            
            // wait until zip is done
            process.waitFor();
            
            progressIndicator.setSubtaskProgress(TOTAL_PROGRESS);
            
            InputStream stdout = process.getInputStream ();
            BufferedReader reader = new BufferedReader (new InputStreamReader(stdout));
            String line;
            exportOutputStringBuffer.append("export.bat output:");
            while ((line = reader.readLine ()) != null) {
                exportOutputStringBuffer.append("\n").append(line);
            }

            
            //check if user has selected the cancel button
            if(tryCancel(progressIndicator)){
                return;
            }
            
            exportCreated = true;
        
        }catch (DocumentExistsException documentExists){
            logger.error(errorId + " - Caught exception while trying to export course(s).", documentExists);
            error = new DetailedException("Unable to export the course because a file already exists.", "An exception was thrown with the message: "+documentExists.getMessage()+".\n\nError Id: "+errorId, documentExists);
        }catch (QuotaExceededException quotaExceeded){
            logger.error(errorId + " - Caught exception while trying to export course(s).", quotaExceeded);
            error = new DetailedException("Unable to export the course because you have reached your disk quota.", "An exception was thrown with the message: "+quotaExceeded.getMessage()+".\n\nError Id: "+errorId, quotaExceeded);
        }catch (DetailedException detailedException){
            logger.error(errorId + " - Caught exception while trying to export course(s).", detailedException);
            error = detailedException;
        }catch(OutOfMemoryError me){
        	//I think there may be a size limit to the course you can export:
        	//When I upload a course ~>90MB I get an OutOfMemoryError, but not on smaller course folders
        	logger.error(errorId + " - Caught exception while trying to export course for .", me);
            error = new DetailedException(me.getCause() == null ? "The Course may be too large to Export" : me.getCause().getMessage(), "An exception was thrown with the message: "+me.getMessage()+".\n\nError Id: "+errorId, me);
        }
        catch (Throwable e) {     
            logger.error(errorId + " - Caught exception while trying to export course for .",e);
            error = new DetailedException(e.getCause() == null || e.getCause().getMessage() == null || e.getCause().getMessage().isEmpty() ? "There was a problem while exporting the course" : e.getCause().getMessage(), "The exception message reads:\n"+e.getMessage()+".\n\nError Id: "+errorId, e);
        }        
        progressIndicator.increasePercentComplete(ZIP_OUTPUT_DIRECTORY_TOTAL_PROGRESS);
        progressIndicator.setSubtaskProgress(ZERO);
        progressIndicator.updateSubtaskDescription(CLEANING_UP);
        
        //check if user has selected the cancel button
        if(tryCancel(progressIndicator)){
            return;
        }
        
        beginFilesTask(createdFiles.keySet());
        try{
            performCleanup(progressIndicator, exportCreated);
        }catch(IOException e) {
            logger.error(errorId + " - Caught exception while trying to perform the cleanup.", e);            

            if(CommonProperties.getInstance().getDeploymentMode() != DeploymentModeEnum.SERVER){
                JOptionPane.showMessageDialog(null, "An error occurred while trying to perform clean, therefore not all the files created maybe deleted. " +
                        "Please check the log for more details.", "GIFT Cleanup Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        endDirectoryTask();
        
        //check if user has selected the cancel button
        if(tryCancel(progressIndicator)){
            return;
        }
        
        if(error == null){
            if(logger.isInfoEnabled()){
                logger.info(exportOutputStringBuffer.toString());
            }
            progressIndicator.updateSubtaskDescription(EXPORT_SUCCESSFUL);
        }else{
            
            if(CommonProperties.getInstance().getDeploymentMode() == DeploymentModeEnum.SERVER){
                progressIndicator.updateSubtaskDescription("GIFT Export Course Failed!  \nPlease contact the system administrator if you need more assistance.\n Error Id: "+errorId);
                throw error;
            }else{
                progressIndicator.updateSubtaskDescription("GIFT Export Course Failed!  \nCheck your dashboard log.\n Error Id: "+errorId);             
                throw error;
            } 
        }
        
        progressIndicator.setPercentComplete(TOTAL_PROGRESS);
        progressIndicator.setSubtaskProgress(TOTAL_PROGRESS);

    }
    
    
    /**
     * Removes sensitive LTI provider information from the course xml.
     * 
     * @param tempDirectory the location of the temp directory. Can't be null.
     * @param content the content the user has chosen to export. Can't be null.
     * @throws URISyntaxException if there is a problem accessing the rootFolder and navigating to it's parent.
     * @throws IOException if there is a problem trying to access the files within the content.
     * @throws JAXBException if there is a problem writing the new course xml to the temp directory.
     * @throws SAXException if there is a problem writing the new course xml to the temp directory.
     */
    private void removeLtiProviders(File tempDirectory, DomainContent content)
            throws IOException, URISyntaxException, SAXException, JAXBException {
        
        for (ContentProxyInterface file : content.getExportFiles()) {
            // can be a course file or a directory
            if (file.isDirectory()) {
                
                if(file instanceof AbstractFolderProxy) {
                    AbstractFolderProxy folderProxy = (AbstractFolderProxy)file;
                    
                    // find course(s)
                    List<FileProxy> courseFiles = new ArrayList<>();
                    FileFinderUtil.getFilesByExtension(folderProxy, courseFiles, AbstractSchemaHandler.COURSE_FILE_EXTENSION);

                    if (courseFiles.isEmpty()) {
                        continue;
                    }

                    for (FileProxy courseFile : courseFiles) {
                        DomainCourseFileHandler dcfh = new DomainCourseFileHandler(courseFile, folderProxy, false);
                        dcfh.scrubLtiProviders();

                        File destFile = new File(tempDirectory.getAbsolutePath() + File.separator + 
                                folderProxy.getName() + File.separator + 
                                folderProxy.getRelativeFileName(courseFile));
                        AbstractSchemaHandler.writeToFile(dcfh.getCourse(), destFile, true);
                    }
                }

            }
        }
    }
    
    /**
     * Parses the Domain content being exported to get resources from the GIFT folder that the
     * Domain content uses.  An example of a resource is survey images that a course might use. 
     * 
     * @param content the content the user has chosen to export
     * @param progressIndicator used to provide progress to the user
     * @return an ArrayList of the resources to export.
     * @throws IOException if there was a problem retrieving courses
     * @throws URISyntaxException if there was a problem constructing URIs to do the retrieval
     * @throws DKFValidationException if there was a problem building DKF objects
     * @throws FileValidationException if there was a problem validating a GIFT XML file against its schema
     * @throws CourseFileValidationException if there was a problem building course objects
     */
    private List<File> getExternalDomainResources(DomainContent content, ProgressIndicator progressIndicator) throws IOException, URISyntaxException, FileValidationException, DKFValidationException, CourseFileValidationException {
        
        ArrayList<File> domainResources = new ArrayList<File>();
        
        int index = 1;
        int originalPercent = progressIndicator.getPercentComplete();
        for(ContentProxyInterface file : content.getExportFiles()){
            
            //can be a course file or a directory
            if(file.isDirectory() && file instanceof AbstractFolderProxy){
                //looking for courses...
                
                //
                // find course(s)
                //
                List<FileProxy> courseFiles = new ArrayList<>();
                FileFinderUtil.getFilesByExtension((AbstractFolderProxy)file, courseFiles, AbstractSchemaHandler.COURSE_FILE_EXTENSION); 
                
                if (courseFiles.isEmpty()) {
                    continue;
                }
                
                for(FileProxy courseFile : courseFiles){
                    AbstractFolderProxy courseFolder = ((AbstractFolderProxy)file);
                    parseCourseForResources(courseFolder, courseFile, domainResources);
                }
                
            }
            
            float subtaskPercent = ((float)index++/(float)content.getExportFiles().size()) * TOTAL_PROGRESS;
            progressIndicator.setSubtaskProgress((int)subtaskPercent);
            progressIndicator.setPercentComplete((int) (originalPercent + ((subtaskPercent / TOTAL_PROGRESS) * GATHER_COURSES)));
        }
        
        return domainResources;
    }
    
    /**
     * Parses a course file for resources that are used by the course
     * 
     * @param courseFolder the course folder for the course being parsed
     * @param courseFile the course file being parsed
     * @param domainResources a pass-by-reference holder for the resources that are found.
     *        This ArrayList must be instantiated before being passed to this method.
     * @throws IOException if there was a problem parsing the course or course related files
     * @throws DKFValidationException if there was a problem building DKF objects
     * @throws FileValidationException if there was a problem validating a GIFT XML file against its schema
     * @throws CourseFileValidationException if there was a problem building course objects
     */
    private void parseCourseForResources(AbstractFolderProxy courseFolder, FileProxy courseFile, ArrayList<File> domainResources) throws IOException, FileValidationException, DKFValidationException, CourseFileValidationException {
        
        if (domainResources == null) {
            throw new IllegalArgumentException("The parameter domainResources cannot be null. It is used as a pass-by-reference variable"
                    + " and needs to be instantiated before being passed into this method.");
        }
        
        DomainCourseFileHandler dcfh = new DomainCourseFileHandler(courseFile, courseFolder, true);
        CourseValidationResults validationResults = dcfh.checkCourse(true, null);
        if(validationResults.hasCriticalIssue() || validationResults.hasImportantIssues()){
            throw new CourseFileValidationException("Failed to validate", "There was one or more issues validating.", courseFile.getName(), validationResults.getFirstError());
        }
        
        //piggy back on the survey validation request logic to gather survey references used in this course
        List<SurveyCheckRequest> surveyChecks = dcfh.buildSurveyValidationRequest();
        
        //keep track of ids already exported to avoid exporting the same survey context more than once for a course
        List<Integer> exportedSurveyContexts = new ArrayList<>();
        
        for(SurveyCheckRequest check : surveyChecks){
            
            int surveyContextId = check.getSurveyContextId();
            
            if(exportedSurveyContexts.contains(surveyContextId)){
                //already exported this survey context's resources
                continue;
            }
            
            // Get image references for this survey context
            try{
                SurveyContext sContext = Surveys.getSurveyContext(dcfh.getSurveyContextId());
                for(SurveyContextSurvey scs : sContext.getContextSurveys()){
                    Survey survey = scs.getSurvey();
                    Surveys.getSurveyImageReferences(survey, domainResources);
                }
                
                exportedSurveyContexts.add(dcfh.getSurveyContextId());
            }catch(Exception e){
                throw new CourseFileValidationException("Failed to export the surveys for the course named '"+courseFile.getName()+"'.", 
                        "There was an exception thrown when retrieving the surveys from the database.", courseFile.getFileId(), e);
            }
        }     
        
        // Any other resources used by the course should be searched for here
        
        if(logger.isInfoEnabled()){
            logger.info("Finished gathering resources for the course '"+courseFile.getName()+"'.");
        }
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
     * Begins a file task.  For now this means calculating the total number of files that will be processed.
     * 
     * @param dir The file to calculate the number of files that will be processed.
     * @throws IOException 
     */
    private void beginFileTask(List<AbstractFolderProxy> courseFolders) throws IOException {
        
        for(AbstractFolderProxy folder : courseFolders){            
            totalFileCount += folder.listFiles(null).size();
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
     * Return whether a cancel export operation has taken place.
     *  
     * @param progressIndicator used to show export progress to the user 
     * @return boolean - true iff the export process is being or has been canceled
     * @throws IOException 
     */
    private boolean tryCancel(ProgressIndicator progressIndicator) throws IOException {
        
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
     * Exports the necessary database entries for the GIFT export to use.
     * 
     * @param createdFiles mapping of files that were created because of the export.  Needed when performing a cleanup of this export process.
     * @param renamedFiles mapping of files that were renamed because of the export.  Needed when performing a cleanup of this export process.  
     * @param surveyRefSrcToExportFile populated by this method and contains GIFT XML course folder files that reference database entries (i.e. surveys)
     * as the key and the values are the corresponding temporary files that contain the survey export
     * @param tempDatabaseExportDirectory the temporary directory to create database export files
     * @param content contains the course folders to export
     * @param domainContentOnly whether only course folders and their assets (e.g. surveys) should be exported and not a GIFT instance that can be executed.
     * @throws IOException if there was a severe problem exporting the database content needed for the tutor export
     * @throws DetailedException for other errors such as FileValidationException or common Exceptions
     */
    private void exportDatabase(
            Map<File, Boolean> createdFiles, 
            Map<File, File> renamedFiles, 
            Map<FileProxy, File> surveyRefSrcToExportFile,
            File tempDatabaseExportDirectory,
            DomainContent content, 
            boolean domainContentOnly) throws Throwable {
            
        //Db entries cases:
        //  MySQL full - delete old derby db and extract newly created derby zip
        //  MySQL + domainContentOnly - get survey contexts of courses and dkfs, write as json strings to single file per course and dkf file with similar name
        //  Derby full - nothing to do, the current derby db will be copied as is
        //  Derby + domainContentOnly - get survey contexts of courses and dkfs, write as json strings to single file per course and dkf file with similar name

        if(domainContentOnly){
            
            //
            // get survey instances needed based on domain content
            //
            
            List<ContentProxyInterface> createdContentFiles = new ArrayList<>();
            
            for(ContentProxyInterface courseFolder : content.getExportFiles()){
                                            
                List<FileProxy> courseFiles = new ArrayList<>();
                FileFinderUtil.getFilesByExtension((AbstractFolderProxy)courseFolder, courseFiles, AbstractSchemaHandler.COURSE_FILE_EXTENSION); 
                for(FileProxy courseFile : courseFiles){
                    //call function to handle survey refs for course file, and add all image references that the surveys use
                    File exportDbFile = exportCourseDBReferences((AbstractFolderProxy)courseFolder, createdFiles, renamedFiles, courseFile, createdContentFiles, tempDatabaseExportDirectory);
                    
                    //map the entry for use later when the course file is copied to the temp folder
                    //containing the exported files
                    surveyRefSrcToExportFile.put(courseFile, exportDbFile);
                }

            }

            //add newly created export files to list of domain content files to export
            content.addExportFiles(createdContentFiles);
            
        }
    }
    
    /**
     * This method exports all database references (e.g. survey context, surveys, etc.) to one or more files
     * based on the references made starting at the course file provided.
     * 
     * @param courseFolder the course folder containing the course file
     * @param createdFiles mapping of files that were created because of the export.  Needed when performing a cleanup of this export process.
     * @param renamedFiles mapping of files that were renamed because of the export.  Needed when performing a cleanup of this export process.      
     * @param courseFile the course file to export all database references for
     * @param createdContentFiles collection of files created as part of the export db references process
     * @param tempDatabaseExportDirectory the temporary directory to create database export files
     * @throws IOException if there was a server error exporting database references for this course
     * @throws DKFValidationException if there was a problem building DKF objects
     * @throws FileValidationException if there was a problem validating a GIFT XML file against its schema
     * @throws CourseFileValidationException if there was a problem building course objects
     * @throws DetailedException for other errors such as FileValidationException or common Exceptions
     */
    private File exportCourseDBReferences(AbstractFolderProxy courseFolder, Map<File, Boolean> createdFiles, Map<File, File> renamedFiles, 
            FileProxy courseFile, List<ContentProxyInterface> createdContentFiles, File tempDatabaseExportDirectory) throws DetailedException, IOException, FileValidationException, DKFValidationException, CourseFileValidationException{

        DomainCourseFileHandler dcfh = new DomainCourseFileHandler(courseFile, courseFolder, false);
        CourseValidationResults validationResults = dcfh.checkCourse(false, null);
        if(validationResults.hasCriticalIssue() || validationResults.hasImportantIssues()){
            throw new CourseFileValidationException("Failed to validate", "There was one or more issues validating.", courseFile.getName(), validationResults.getFirstError());
        }
        
        //create export file based on course file name
        String courseExportFileName = courseFile.getName().replace(Constants.XML, FileUtil.SURVEY_REF_EXPORT_SUFFIX);

        File courseExportFile = new File(tempDatabaseExportDirectory + File.separator + courseExportFileName);
        
        //need to delete this file since it is in a different temp directory than the main GIFT content being exported
        createdFiles.put(courseExportFile, true);
        
        try{

	        //piggy back on the survey validation request logic to gather survey references used in this course
	        List<SurveyCheckRequest> surveyChecks = dcfh.buildSurveyValidationRequest();        
	        
	        //keep track of ids already exported to avoid exporting the same survey context more than once for a course
	        List<Integer> exportedSurveyContexts = new ArrayList<>();
        
	        if(surveyChecks.isEmpty() && dcfh.getSurveyContextId() != null){
	            //there are no transitions that use a survey context but the course has been associated with
	            //a survey context, therefore need to make sure the survey context is exported.
	            
	            if(logger.isInfoEnabled()){
	                logger.info("Exporting course level survey context "+dcfh.getSurveyContextId()+" referenced in the course '"+courseFile.getName()+"'.");
	            }
	            
	           
	            exportSurveyContext(dcfh.getSurveyContextId(), courseExportFile, createdContentFiles);
	            exportedSurveyContexts.add(dcfh.getSurveyContextId());
	        }
	        
	        for(SurveyCheckRequest check : surveyChecks){
	            
	            int surveyContextId = check.getSurveyContextId();
	            
	            if(exportedSurveyContexts.contains(surveyContextId)){
	                //already exported this survey context
	                continue;
	            } 
	            
	            if(logger.isInfoEnabled()){
	                logger.info("Exporting survey context "+dcfh.getSurveyContextId()+" used somewhere in the course '"+courseFile.getName()+"'.");
	            }
	            
	            exportSurveyContext(surveyContextId, courseExportFile, createdContentFiles);         
	
	            exportedSurveyContexts.add(surveyContextId);
	        }
        
        } catch(Exception e){
        	
        	throw new DetailedException(
        			"Failed to export survey context for the course named '" + courseFolder.getName()  + "' due to an unexpected error.", 
        			"Unable to export survey context for the '" + courseFolder.getName() + "' course. It's likely there was a problem with either "
        					+ "finding the survey context in the database or gathering its resources for the export.", 
        			e
        	);
        }
        
        return courseExportFile;
    }
    
    /**
     * Export the survey context represented by the survey context id provided.
     * 
     * @param surveyContextId the unique id of the survey context being exported
     * @param courseExportFile the file containing the representation of the survey context(s) being exported for this course
     * @param createdContentFiles collection of files created as part of the export db references process
     * @throws IOException if there was a server error exporting the survey context for this course
     * @throws DetailedException if the file can not be found
     */
    private void exportSurveyContext(int surveyContextId, File courseExportFile, List<ContentProxyInterface> createdContentFiles) throws IOException, DetailedException{
        
        SurveyContext sContext;
        try{
            sContext = Surveys.getSurveyContext(surveyContextId);
            
            if(sContext == null){
                throw new IOException("There was a problem converting the survey context with id of "+surveyContextId+". Does the survey context exist?");
            }
        }catch(Exception e){
            throw new IOException("Failed to export the surveys for the course because an exception thrown when retrieving the surveys from the database.", e);
        }
        
        // append to the course export file since the importer 
        // will update survey references of all course transitions if necessary
        try{
        	exportSurveyContext(sContext, courseExportFile, true);            
        }catch(FileNotFoundException fe){
            //ERROR
        	logger.error("Caught exception when trying to export survey context", fe);
            throw new DetailedException("There was a problem exporting the survey context. Check your survey references and try again.","Caught File Not Found Exception when trying to export survey context", fe);
        }
        catch(MessageEncodeException me){
        	throw new IOException(me.getMessage());
        }
    }
    
    /**
     * Cleanup the GIFT that performed the export.
     * 
     * Delete files and directories created during the export process.
     * 
     * @param progressIndicator used to show export progress to the user
     * @param successfulExport whether the cleanup is being performed as a result of a successful export or not.
     * @throws IOException if there was a problem deleting a file
     */
    private void performCleanup(ProgressIndicator progressIndicator, boolean successfulExport) throws IOException{
        
        int currentProgress = progressIndicator.getPercentComplete();
        for(File file : createdFiles.keySet()) {
            
            boolean deleteAlways = createdFiles.get(file);
            if(deleteAlways || !successfulExport){
            
                if(file.isDirectory()) {
                    deleteDirectory(file, progressIndicator, currentProgress);
                } else if(file.isFile()) {
                    FileUtils.deleteQuietly(file);
                }
            }
        }
        
        for(File renamedFile : renamedFiles.keySet()){          
            renamedFile.renameTo(renamedFiles.get(renamedFile));
        }
    }
    
    /**
     * Clear all user data from the exported UMS database (not the source UMS db).
     * 
     * @param tempDirectory the temp directory where the export is being built
     * @throws IOException if there was a severe problem during the clearing of the user data
     * @throws ConfigurationException 
     */
    private void clearUMSUserData(File tempDirectory) throws IOException, ConfigurationException{
        
        //start embedded derby UMS db instance using the export temp folder location as the db location
        ExportingUMSDatabaseManager exportingUMSMgr = new ExportingUMSDatabaseManager(tempDirectory);
        
        //call UMS db mgr clear user data method to handle erasing the appropriate table's rows
        exportingUMSMgr.clearUserData();
    }
    
    /**
     * Copies resource files into the GIFT root directory.
     * 
     * @param giftRoot The GIFT (exported) root directory
     * @param content The selected domain folder root level files selected for export 
     * @param domainContentOnly whether only course folders and their assets (e.g. surveys) should be in the export (instead of a runnable GIFT instance)
     * @throws IOException if there was a problem copying files
     */
    private void copyResources(File giftRoot, DomainContent content, boolean domainContentOnly) throws IOException {
        
        File readmeSrc;
        File readmeDest;        
        
        if(domainContentOnly){
            
            //README for Domain only export
            readmeSrc = new File(DOMAIN_EXPORT_README_FILENAME_RESOURCE);
            readmeDest = new File(giftRoot.getAbsolutePath() + File.separator + GIFT_EXPORT_README_FILENAME);
            
        }else{                      
            //README for full GIFT export
            readmeSrc = new File(GIFT_EXPORT_README_FILENAME_RESOURCE);
            readmeDest = new File(giftRoot.getAbsolutePath() + File.separator + GIFT_EXPORT_README_FILENAME);
        }

        
        FileUtils.copyFile(readmeSrc, readmeDest);
        
        // Generate a quick list of exported domain content
        PrintWriter readmeWriter = new PrintWriter(new FileOutputStream(readmeDest, true));
        readmeWriter.println();
        readmeWriter.println();
        for(ContentProxyInterface file : content.getExportFiles()) {
            readmeWriter.append(file.getFileId());
            
            if(file instanceof AbstractFolderProxy){
                readmeWriter.append(File.separator);
            }
            
            readmeWriter.println();
        }
        readmeWriter.close();
    }
    
    /**
     * Makes sure the UMS and LMS hibernate configuration files are setup for using derby.
     * 
     * @param giftTempRoot the temp location of the exported GIFT.  This is where to do file manipulations at.
     * @param domainContentOnly whether only course folders and their assets (e.g. surveys) should be in the export (instead of a runnable GIFT instance)
     * @throws FileNotFoundException if the derby database was not found
     */
    private void configureDerbyDatabase(File giftTempRoot, boolean domainContentOnly) throws FileNotFoundException{        
        
        //Config Cases:
        //  Derby + !domainContentOnly - nothing to do, configs are the way they should be
        
        if(!domainContentOnly){
        
            //location of the derby database data
            File derbyDatabaseDir = new File(giftTempRoot.getAbsolutePath() + File.separator + "GIFT" + File.separator + "data" + File.separator + "derbyDB");
                
            // If Derby is set to be used but there is no DerbyDb folder,
            // Derby is possibly misconfigured and so an error is thrown
            if (!derbyDatabaseDir.exists()) {
                throw new FileNotFoundException("The derby database is configured to be used but it does not exist.");
            }

        }
    }
    
    /**
     * Exports a survey context to the export file provided.
     * 
     * @param sContext Survey context object to export.
     * @param exportFile the file to export (i.e. write) too
     * @param append whether to append the survey context export to the existing export file contents
     * @throws FileNotFoundException 
     */
    private void exportSurveyContext(SurveyContext sContext, File exportFile, boolean append) throws FileNotFoundException {
        
        // Convert to JSON
        SurveyContextJSON json = new SurveyContextJSON();
        JSONObject obj = new JSONObject();
        
        try {
            json.encode(obj, sContext);
            
            PrintWriter writer = new PrintWriter(exportFile);
            
            if(append){
                writer.append(obj.toJSONString());
            }else{
                writer.println(obj.toJSONString());
            }
            writer.close();
            
        } catch(MessageEncodeException e) {
            logger.error("Caught exception when trying to export survey context", e);
            throw e;
        } catch(FileNotFoundException e) {
            logger.error("Caught exception when trying to export survey context", e);
            throw new DetailedException("There was a problem exporting the survey context. Check your survey references and try again.","Caught File Not Found Exception when trying to export survey context", e);
        }
        
    }
    
    /**
     * Recursively copies src to dest
     * 
     * @param src The source directory to copy from.  Can't be null and must exist.
     * @param dest The destination directory to copy to.  Can't be null and must exist.
     * @param filter used to filter core GIFT files that need to be copied from the source folder
     * @param currentFileCount running total of files copied, used for progress indication 
     * @param totalFileCount how many files need to be copied
     * @param progressIndicator used to show export progress to the user
     * @param originalTaskProgress original value of the progress indicator's completed percent used in incremental calculations based on file operations
     * performed in this method.
     */
    private void copyDirectory(File src, File dest, 
            FileFilter filter, ProgressIndicator progressIndicator, float originalTaskProgress) {
        
        // If source doesn't exist and is not a directory, return.
        if(!(src.exists() && src.isDirectory())) {
            logger.error("Could not copy " + src.getAbsolutePath() + " source directory doesn't exist or path is not a directory");
            return;
        }
        
        // If destination doesn't exist, make a directory, if that fails, return.
        if(!dest.exists() && !dest.mkdir()) {
            logger.error("Could not copy " + src.getAbsolutePath() + " destination directory doesn't exist or could not be created");
            return;
        }
        
        // At this point source and destination should be valid
        File[] files = src.listFiles(filter);
        for(File f : files) {
            if(f.isDirectory()) {
                copyDirectory(f, new File(dest.getAbsolutePath() + File.separator + f.getName()), filter, progressIndicator, originalTaskProgress);
            } else if(f.isFile()) {
                try {
                    currentFileCount++;
                    File d = new File(dest.getAbsolutePath() + File.separator + f.getName());
                    
                    float subtaskProgressValue = ((float)currentFileCount / (float)totalFileCount);  //the absolute decimal amount completed for these file operations
                    progressIndicator.setSubtaskProgress((int)(subtaskProgressValue * TOTAL_PROGRESS)); //set the percent completed for these file operations
                    float increaseAmt = ((float)progressIndicator.getSubtaskProgress() / TOTAL_PROGRESS) * COPY_TEMP_DIRECTORY_TOTAL_PROGRESS;  //the percent completed to add to the original percent
                    progressIndicator.setPercentComplete((int)(originalTaskProgress + increaseAmt));
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Copying " + f.getAbsolutePath() + " to " + d.getAbsolutePath());
                    }
                    FileUtils.copyFile(f, d);
                    
                    if(tryCancel(progressIndicator)) return;
                } catch(IOException e) {
                    logger.error("Caught exception while trying to copy " + f.getAbsolutePath() + " to " + dest.getAbsolutePath() + File.separator + f.getName(), e);
                }
            }
        }
    }
    
    /**
     * Recursively copies src to dest
     * 
     * @param originalSrcDirectory the original starting directory of the recursive method call (e.g. course folder), can't be null
     * @param currentSrc The source directory being copied in this recursive method call (e.g. course folder then sub-folders)
     * @param destFolder The destination directory to copy to
     * @param surveyRefSrcToExportFile populated by this method and contains GIFT XML course folder files that reference database entries (i.e. surveys)
     * as the key and the values are the corresponding temporary files that contain the survey export
     * @param progressIndicator used to show export progress to the user
     * @param originalTaskProgress original value of the progress indicator's completed percent used in incremental calculations based on file operations
     * performed in this method.
     * @throws IOException if there was a problem copying files
     */
    private void copyDirectory(AbstractFolderProxy originalSrcDirectory,
            ContentProxyInterface currentSrc, 
            File destFolder, 
            Map<FileProxy, File> surveyRefSrcToExportFile, 
            ProgressIndicator progressIndicator, float originalTaskProgress) throws IOException {
        
        // If source doesn't exist and is not a directory, return.
        if(!(currentSrc.isDirectory())) {
            logger.error("Could not copy " + currentSrc.getFileId() + " source directory doesn't exist or path is not a directory");
            return;
        }
        
        // If destination doesn't exist, make a directory, if that fails, return.
        if(!destFolder.exists() && !destFolder.mkdir()) {
            logger.error("Could not copy " + currentSrc.getFileId() + " destination directory doesn't exist or could not be created");
            return;
        }
        
        // At this point source and destination should be valid
        List<FileProxy> files = ((AbstractFolderProxy)currentSrc).listFiles(null);
        for(FileProxy file : files) {
            
            if(file.isDirectory()) {
                copyDirectory(originalSrcDirectory, 
                        file,
                        new File(destFolder.getAbsolutePath() + File.separator + file.getFileId()), 
                        surveyRefSrcToExportFile, progressIndicator, originalTaskProgress);
            } else {
                try {
                    currentFileCount++;                    
                    
                    // include the relative path to this possibly nested file being copied
                    File dest = new File(destFolder.getAbsolutePath() + File.separator + 
                            originalSrcDirectory.getName() + File.separator + 
                            originalSrcDirectory.getRelativeFileName(file));
                    
                    float subtaskProgressValue = ((float)currentFileCount / (float)totalFileCount);  //the absolute decimal amount completed for these file operations
                    progressIndicator.setSubtaskProgress((int)(subtaskProgressValue * TOTAL_PROGRESS)); //set the percent completed for these file operations
                    float increaseAmt = ((float)progressIndicator.getSubtaskProgress() / TOTAL_PROGRESS) * COPY_TEMP_DIRECTORY_TOTAL_PROGRESS;  //the percent completed to add to the original percent
                    progressIndicator.setPercentComplete((int)(originalTaskProgress + increaseAmt));
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Copying " + file.getFileId() + " to " + dest.getAbsolutePath());
                    }
                    FileUtils.copyInputStreamToFile(file.getInputStream(), dest);
                    
                    //does this file have a db export file
                    File tempExportFile = surveyRefSrcToExportFile.get(file);
                    if(tempExportFile != null){
                        //copy the export file to the same directory as the destination file 
                        
                        //backup if there is already an export of the same file name
                        File destExportFileToCheck = new File(dest.getParentFile() + File.separator + tempExportFile.getName());                       
                        if(destExportFileToCheck.exists()){
                            //backup the existing file
                            File backup = new File(destExportFileToCheck.getAbsolutePath() + "."+BACKUP_FILE_TIME_FORMAT.format(new Date())+".EXPORT.BACKUP");
                            FileUtils.copyFile(destExportFileToCheck, backup);
                            renamedFiles.put(backup, destExportFileToCheck);
                        }
                        
                        FileUtils.copyFileToDirectory(tempExportFile, dest.getParentFile());
                    }
                    
                    if(tryCancel(progressIndicator)) return;
                } catch(IOException e) {
                    logger.error("Caught exception while trying to copy " + file.getFileId() + " to " + destFolder.getAbsolutePath() + File.separator + file.getFileId(), e);
                }
            }
        }
    }
    
    /**
     * Recursively deletes directory files in order to display progress to the user.
     * 
     * @param dir The directory to delete
     * @param progressIndicator used to show export progress to the user
     * @throws IOException if there was a problem deleting the directory after deleting all descendant files
     */
    private void deleteDirectory(File dir, ProgressIndicator progressIndicator, int originalProgress) throws IOException {
        
        if(!(dir.exists() && dir.isDirectory())) {
            logger.error("Could not delete " + dir.getAbsolutePath() + " directory doesn't exist or path is not a directory");
            return;
        }
        
        File[] files = dir.listFiles();
        for(File f : files) {
            if(f.isDirectory()) {
                deleteDirectory(f, progressIndicator, originalProgress);
            } else if(f.isFile()) {
                currentFileCount++;
                progressIndicator.setSubtaskProgress((int)(((float)currentFileCount / (float)totalFileCount) * TOTAL_PROGRESS));
                progressIndicator.setPercentComplete((progressIndicator.getSubtaskProgress() / (TOTAL_PROGRESS / CLEANUP_TOTAL_PROGRESS)) + originalProgress);
                
                if(logger.isInfoEnabled()){
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
     * This extension of the UMS hibernate util class allows the export logic to over-ride the connection url
     * property value(s) in order to redirect the connection to use the exported temporary folder location.
     * 
     * @author mhoffman
     *
     */
    private static class ExportingUMSHibernateUtil extends UMSHibernateUtil{
        
        private File tempDirectory;
        
        /**
         * Class constructor - use a different hibernate config file setup for embedded UMS database connection
         * @throws ConfigurationException 
         */
        public ExportingUMSHibernateUtil(File tempDirectory) throws ConfigurationException{
            super(EXPORTING_CONFIG_FILE);

            this.tempDirectory = tempDirectory;
        }
        
        @Override
        protected SessionFactory buildSessionFactory(boolean createSchema) throws ConfigurationException{
            
            //use the temporary folder location for the database path
            config.setProperty(CONNECTION_URL, "jdbc:derby:"+tempDirectory.getAbsolutePath()+ File.separator + "GIFT" + File.separator + "data" + File.separator + "derbyDb" + File.separator + "GiftUms");
            config.setProperty("hibernate.connection.url", "jdbc:derby:"+tempDirectory.getAbsolutePath()+ File.separator + "GIFT" + File.separator + "data" + File.separator + "derbyDb" + File.separator + "GiftUms");
            
            return super.buildSessionFactory(createSchema);
        }
    }
    
    /**
     * This extension of the UMS database manager allows the export logic to over-ride the default hibernate
     * util class that is used to configure the hibernate connection.
     * 
     * @author mhoffman
     *
     */
    private static class ExportingUMSDatabaseManager extends UMSDatabaseManager{
        
        public ExportingUMSDatabaseManager(File tempDirectory) throws ConfigurationException{
            super(new ExportingUMSHibernateUtil(tempDirectory));
        }
    }

}
