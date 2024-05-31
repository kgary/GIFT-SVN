/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services.file.desktop;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEvent;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import generated.course.TrainingApplicationWrapper;
import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.DomainOption.DomainOptionPermissions;
import mil.arl.gift.common.TrainingAppCourseObjectWrapper;
import mil.arl.gift.common.course.CourseFileAccessDetails;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.AbstractSchemaHandler.FileType;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.CourseListFilter;
import mil.arl.gift.common.io.CourseListFilter.CourseSourceOption;
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
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.ProhibitedUserException;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.io.Version;
import mil.arl.gift.common.metadata.MetadataSearchResult;
import mil.arl.gift.common.metadata.MetadataSearchResult.QuadrantResultSet;
import mil.arl.gift.common.metadata.MetadataWrapper;
import mil.arl.gift.common.metadata.MetadataWrapper.ContentTypeEnum;
import mil.arl.gift.common.metadata.QuadrantRequest;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.domain.DomainCourseFileHandler.CourseOptionsWrapper;
import mil.arl.gift.domain.knowledge.metadata.MetadataFileFinder;
import mil.arl.gift.domain.knowledge.metadata.MetadataFileSearchResult;
import mil.arl.gift.domain.knowledge.metadata.MetadataSchemaHandler;
import mil.arl.gift.domain.knowledge.metadata.MetadataSearchCriteria;
import mil.arl.gift.net.api.message.codec.json.survey.QuestionJSON;
import mil.arl.gift.net.api.message.codec.json.survey.SurveyContextJSON;
import mil.arl.gift.tools.authoring.common.conversion.AbstractConversionWizardUtil;
import mil.arl.gift.tools.authoring.common.conversion.AbstractLegacySchemaHandler;
import mil.arl.gift.tools.authoring.common.conversion.UnsupportedVersionException;
import mil.arl.gift.tools.authoring.common.util.DomainKnowledgeUtil;
import mil.arl.gift.tools.export.ExportCourseUtil;
import mil.arl.gift.tools.importer.ImportCourseUtil;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.ServicesProperties;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import mil.arl.gift.tools.services.file.FileLockManager;

/**
 * This class contains GIFT file system services for desktop deployment.  Desktop deployment files
 * are stored on the native OS file system (e.g. Windows) versus a content management system.
 * 
 * @author mhoffman
 *
 */
public class DesktopFileServices extends AbstractFileServices{

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DesktopFileServices.class);
    
    /** the folder which is the root folder for authoring in desktop mode (normally Domain/workspace/) */
    private static DesktopFolderProxy workspaceFolderProxy = null;
    
    /** the folder where training application course objects not associated with courses are located (normally Domain/workspace/Public/TrainingAppsLib/)*/
    private static DesktopFolderProxy trainingAppsLibFolderProxy = null;
    
    /**
     * Name of the default directory that needs to be removed when retrieving training app objects. 
     * Note: must match mil.arl.gift.tools.authoring.server.gat.client.wrap.widgets.CourseObjectItemEditor#DEFAULT_ASSESSMENT_NAME
     */
    private static final String DEFAULT_DKF_ASSESSMENT_FOLDER_NAME = "default_dkf_assessment";
    
    @Override
    public AbstractFolderProxy getWorkspaceFolderProxy(String username){
        
        if(workspaceFolderProxy == null){
            //this logic is here so if there is an exception the caller will be notified each method call
            workspaceFolderProxy = new DesktopFolderProxy(new File(ServicesProperties.getInstance().getWorkspaceDirectory()));
        }
        
        return workspaceFolderProxy;
    }

    /**
     * Return the training apps lib directory for this GIFT instance. This directory contains
     * training applications.
     * 
     * @return the folder proxy instance
     */
    private DesktopFolderProxy getTrainingAppsLibraryFolderProxy() {
        
        if (trainingAppsLibFolderProxy == null) {
            trainingAppsLibFolderProxy = new DesktopFolderProxy(
                    new File(ServicesProperties.getInstance().getWorkspaceDirectory() + File.separator
                            + getTrainingAppsLibraryTreeModel(null).getRelativePathFromRoot(true)));
        }
        
        return trainingAppsLibFolderProxy;
    }

    @Override
    public void getCourses(String username, CourseOptionsWrapper courseOptionsWrapper, CourseListFilter courseListFilter,
            boolean validateLogic, ProgressIndicator progressIndicator) 
                    throws IllegalArgumentException, FileNotFoundException, 
                    FileValidationException, ProhibitedUserException {

        boolean includeAll = courseListFilter == null || courseListFilter.getCourseSourceOptions().isEmpty();
        boolean includeShowcase = false, includeUser = false;
        if(!includeAll && courseListFilter != null) {
            includeShowcase = courseListFilter
                    .getCourseSourceOptions()
                    .contains(CourseSourceOption.SHOWCASE_COURSES);
            includeUser = courseListFilter
                    .getCourseSourceOptions()
                    .contains(CourseSourceOption.MY_COURSES);
            //Don't worry about checking for shared courses here since in desktop
            //mode every user has file access to all course files any way
        }

        try {
            if (includeAll) {
                
                ProgressIndicator subtaskProgressIndicator = null;
                if(progressIndicator != null){
                    progressIndicator.setTaskDescription(ALL_COURSES_PROGRESS_DESC);
                    subtaskProgressIndicator = new ProgressIndicator();
                    progressIndicator.setSubtaskProgressIndicator(subtaskProgressIndicator);
                }
                
                DomainCourseFileHandler.getAllCourses(courseOptionsWrapper, getWorkspaceFolderProxy(username),
                        getWorkspaceFolderProxy(username), null, validateLogic, true, username, subtaskProgressIndicator);
                
                if(progressIndicator != null){
                    progressIndicator.setPercentComplete(COURSES_PERCENT_COMPLETE);
                }
            } else {
                if (includeShowcase) {
                    
                    ProgressIndicator subtaskProgressIndicator = null;
                    if(progressIndicator != null){
                        progressIndicator.setTaskDescription(SHOWCASE_COURSES_PROGRESS_DESC);
                        subtaskProgressIndicator = new ProgressIndicator();
                        progressIndicator.setSubtaskProgressIndicator(subtaskProgressIndicator);
                    }
                    
                    Path showcasePath = Paths.get(ServicesProperties.getInstance().getWorkspaceDirectory(),
                            PUBLIC_WORKSPACE_FOLDER_NAME);
                    DesktopFolderProxy showcaseFolderProxy = new DesktopFolderProxy(showcasePath.toFile());
                    DomainCourseFileHandler.getAllCourses(courseOptionsWrapper, getWorkspaceFolderProxy(username),
                            showcaseFolderProxy, null, validateLogic, true, username, subtaskProgressIndicator);
                    
                    if(progressIndicator != null){
                        progressIndicator.setPercentComplete(SHOWCASE_COURSES_PERCENT_COMPLETE);
                }
                }

                if (includeUser) {
                    
                    ProgressIndicator subtaskProgressIndicator = null;
                    if(progressIndicator != null){
                        progressIndicator.setTaskDescription(YOUR_COURSES_PROGRESS_DESC);
                        subtaskProgressIndicator = new ProgressIndicator();
                        progressIndicator.setSubtaskProgressIndicator(subtaskProgressIndicator);
                    }
                    
                    Path userPath = Paths.get(ServicesProperties.getInstance().getWorkspaceDirectory(), username);
                    DesktopFolderProxy userFolderProxy = new DesktopFolderProxy(userPath.toFile());
                    DomainCourseFileHandler.getAllCourses(courseOptionsWrapper, getWorkspaceFolderProxy(username),
                            userFolderProxy, null, validateLogic, true, username, subtaskProgressIndicator);
                }
                
                if(progressIndicator != null){
                    progressIndicator.setPercentComplete(COURSES_PERCENT_COMPLETE);
                }
            }
            
            completeDomainOptionInfo(username, courseOptionsWrapper);
                
        } catch (Exception e) {
            throw new DetailedException("Failed to retrieve all courses in the workspace.",
                    "An exception was thrown while retrieving all courses of " + e.getMessage(), e);
        }
    
    } 
    
    @Override
    public void getCourse(String username, String courseId,
            CourseOptionsWrapper courseOptionsWrapper, boolean validateLogic,
            boolean validateSurveyReferences, boolean failOnFirstSchemaError, ProgressIndicator progressIndicator)
            throws IllegalArgumentException, FileNotFoundException,
            DetailedException, ProhibitedUserException {

        // gather all the courses in order to find the course specified
        DomainCourseFileHandler.getCourse(courseId, courseOptionsWrapper, getWorkspaceFolderProxy(username), validateLogic, failOnFirstSchemaError, username, progressIndicator);

        DomainOption course = courseOptionsWrapper.domainOptions.get(courseId);
        if(course == null){
            return;  //there must have been an error in parsing/validating the course, 
                     //therefore the course file resides in the parseFailedFiles collection
        }        
    }
    
    /**
     * Copy the server located course folder into the desktop destination folder where it will be executed upon.
     *  
     * @param username used for authentication in retrieving the course folder from the server
     * @param courseId unique id of the course.  e.g. mhoffman/4879 test new1 - Copy/4879 test new1 - Copy.course.xml
     * @param courseOptionsWrapper contains information about all courses known to this user
     * @param progressIndicator used to indicate progress in copying the course folder to the destination folder.  Can't be null
     * @param destinationFolder where to place the course folder
     * @param returnCourseFolderId whether or not to return the course folder path relative to the destination folder.  If false
     * the course id (i.e. path) relative to the destination folder is returned.
     * @param registerToDelete used to specify whether or not the copied course folder should be set for deletion on shutdown
     * @param checkCourseVersion true to check the course file's version and attempt to upconvert the
     *        course files if it is not the latest schema version. This can be a pricey operation so
     *        only enable this in certain cases (e.g. LTI). Note: experiment courses and dashboard
     *        courses have already been upconverted by this point.
     * @return the path to the course XML file (i.e. the course id) directly under the course folder now in the destination folder
     */
    private String copyCourseFolder(String username, String courseId,
            CourseOptionsWrapper courseOptionsWrapper, final ProgressIndicator progressIndicator, File destinationFolder, boolean returnCourseFolderId, boolean registerToDelete, boolean checkCourseVersion){
        
        //gather all the courses in order to find the course specified
        try{
            progressIndicator.setTaskDescription(VALIDATE_COURSE_PROGRESS_DESC);
            DomainCourseFileHandler.getCourse(courseId, courseOptionsWrapper, getWorkspaceFolderProxy(username), false, true, null, progressIndicator);
        }catch(FileNotFoundException fnf){
            throw new DetailedException("Unable to find the course folder for the course '"+courseId+"'.", 
                    "An error was thrown while trying to get the course folder contents : "+fnf.getMessage(), fnf);
        }
        
        if(progressIndicator.shouldCancel()){
            return null;
        }
            
        DomainOption course = courseOptionsWrapper.domainOptions.get(courseId);
        if(course == null){
            //there must have been an error in parsing/validating the course, 
            //therefore the course file resides in the parseFailedFiles collection
            throw new DetailedException("Failed to find the course with id of '"+courseId+"'.",
                    "There were "+courseOptionsWrapper.domainOptions.size()+" valid courses and '"+courseId+"' was not one of them.  Perhaps the course is not valid and therefore can't be loaded", null);  
        }
        
        //
        // copy the course folder
        //
        DomainCourseFileHandler handler = courseOptionsWrapper.courseFileNameToHandler.get(courseId);
        DesktopFolderProxy courseFolder = (DesktopFolderProxy) handler.getCourseFolder();
        
        File destinationCourseFolder = new File(destinationFolder + File.separator + courseFolder.getFolder().getName());  //because FileUtils doesn't include the source directory in the copy
        destinationCourseFolder.mkdirs();
        if(registerToDelete) {
            FileUtil.registerFileToDeleteOnShutdown(destinationCourseFolder);
        }
            
        progressIndicator.setTaskDescription("Loading");
        final int totalFileCnt = FileUtil.countFilesInDirectory(courseFolder.getFolder());
        try{
            if (checkCourseVersion) {
                // look for a course.xml file in the course directory
                List<FileProxy> files = courseFolder.listFiles(null, AbstractSchemaHandler.COURSE_FILE_EXTENSION);
                if (files != null && !files.isEmpty()) {
                    AbstractConversionWizardUtil.updateCourseToLatestVersion(files.get(0), courseFolder, true);
                }
            }

            FileUtils.copyDirectory(courseFolder.getFolder(), destinationCourseFolder, new FileFilter() {
                
                int currentFileCnt = 1;
                
                @Override
                public boolean accept(File pathname) {
                    
                    if (pathname.isDirectory()){
                        return !pathname.getName().equals(Constants.SVN);                    
                    }
                    
                    // These are int values involved, so changing the order of the multiply by 100 to avoid the division of ints getting truncated.
                    int percentComplete = (currentFileCnt++ * 100/totalFileCnt);
                    logger.debug("FileFilter - accept: " + pathname + ", progress: " + percentComplete + ", currentFileCnt: " + currentFileCnt + ", totalFileCnt: " + totalFileCnt );
                    progressIndicator.setPercentComplete(percentComplete);
                    return true;
                }
            });
        } catch (IOException | URISyntaxException e) {
            try{
                FileUtils.deleteDirectory(destinationCourseFolder);
            }catch(IOException innerIO){
                logger.error("Failed to cleanup '"+destinationCourseFolder+"' after a failed copy course operation.", innerIO);
            }
            
            throw new DetailedException("Failed to load the course '"+courseId+"'.", 
                    "There was an error while copying the course folder to the GIFT runtime location : "+e.getMessage(), e);
        }

        if(progressIndicator.shouldCancel()){
            try{
                FileUtil.delete(destinationCourseFolder);
            }catch(IOException e){
                logger.error("Failed to delete destination folder of '"+destinationCourseFolder+"'.", e);
            }
            return null;
        }
        
        progressIndicator.setTaskDescription("Loaded");
        
        //return the new domain id which is the path to the course.xml file in it's runtime folder
        FileProxy origCourseFile = courseOptionsWrapper.courseFileNameToFileMap.get(courseId);
        File newCourseXML = new File(destinationCourseFolder.getPath() + File.separator + origCourseFile.getName());
        if(returnCourseFolderId){
            return FileFinderUtil.getRelativePath(destinationFolder.getParentFile(), newCourseXML.getParentFile());
        }else{
            return FileFinderUtil.getRelativePath(COURSE_RUNTIME_DIR, newCourseXML);
        }
    }

    @Override
    public String loadExperiment(String username, String experimentId, String sourceCourseXmlPath,
            CourseOptionsWrapper courseOptionsWrapper, final ProgressIndicator progressIndicator)
            throws IllegalArgumentException, DetailedException, URISyntaxException {

        final String subFolder = experimentId + File.separator + UUID.randomUUID();
        File experimentFolder = new File(RUNTIME_EXPERIMENT_DIR, subFolder);

        /* return just the copied folder */
        boolean returnCourseFolderId = true;
        /* Delete the runtime folder on shutdown */
        boolean registerToDelete = true;
        /* experiments have already been converted by this point; do not allow
         * conversion here */
        boolean checkCourseVersion = false;

        /* Prepend the experiment id so that the returned value is relative to
         * the runtime experiments folder */
        return experimentId + File.separator + copyCourseFolder(username, sourceCourseXmlPath, courseOptionsWrapper,
                progressIndicator, experimentFolder, returnCourseFolderId, registerToDelete, checkCourseVersion);
    }

    @Override
    public String loadCourse(String username, String courseId,
            CourseOptionsWrapper courseOptionsWrapper, final ProgressIndicator progressIndicator, String runtimeRootFolderName) 
                    throws IllegalArgumentException, DetailedException, URISyntaxException {

        File destinationFolder = getCourseRuntimeFolder(runtimeRootFolderName);
        
        // want to return the new course xml
        boolean returnCourseFolderId = false;
        // the copied course folder should be set for deletion on shutdown
        boolean registerToDelete = true;
        // regular courses have already been converted by this point; do not allow conversion here
        boolean checkCourseVersion = false;

        return copyCourseFolder(username, courseId, courseOptionsWrapper, progressIndicator, destinationFolder, returnCourseFolderId, registerToDelete, checkCourseVersion);
    }

    @Override
    public String loadLTICourse(String username, String courseId, CourseOptionsWrapper courseOptionsWrapper,
            final ProgressIndicator progressIndicator, String runtimeRootFolderName)
            throws IllegalArgumentException, DetailedException, URISyntaxException {

        File destinationFolder = getCourseRuntimeFolder(runtimeRootFolderName);

        // want to return the new course xml
        boolean returnCourseFolderId = false;
        // the copied course folder should be set for deletion on shutdown
        boolean registerToDelete = true;
        // LTI courses may not be the latest schema; allow conversion if they are out of date
        boolean checkCourseVersion = true;

        return copyCourseFolder(username, courseId, courseOptionsWrapper, progressIndicator, destinationFolder, returnCourseFolderId, registerToDelete, checkCourseVersion);
    }

    @Override
    public FileTreeModel getUsersWorkspace(String username) throws DetailedException {
        
        if(username == null || username.length() == 0){
            throw new IllegalArgumentException("The username can't be null or empty.");
        }
        
        //build file tree model with user's workspace at the root (e.g. mhoffman/{course folder(s)})
        DesktopFolderProxy workspaceFolder = (DesktopFolderProxy) getWorkspaceFolderProxy(username);
        File usersWorkspaceFolder = new File(workspaceFolder.getFileId() + File.separator + username);
        
        // its possible that a users workspace directory might not exist when the following happens:
        // 1. the user is in the UMS db
        // 2. gift is in desktop deployment and the login page enters OFFLINE mode
        // 3. the user is selected from the login drop down of known gift users
        // 4. this gift instances has never actually had this user successfully login with ONLINE authenticate 
        //    which would have created that user's workspace
        if(!usersWorkspaceFolder.exists()){
            usersWorkspaceFolder.mkdir();
        }
        
        FileTreeModel userworkspaceModel = getFileTree(usersWorkspaceFolder);
        
        //add 'workspace' parent to the user's workspace tree
        FileTreeModel workspace = new FileTreeModel(workspaceFolder.getName());
        workspace.addSubFileOrDirectory(userworkspaceModel);
        
        return userworkspaceModel;
    }
    

    @Override
    public MetadataSearchResult getMetadataContentFileTree(String username,
            String courseFolderPath, Map<MerrillQuadrantEnum, QuadrantRequest> quadrantToRequest) throws DetailedException, URISyntaxException {
        
        //
        //build the metadata search information to use for each quadrant
        //
        Map<MerrillQuadrantEnum, MetadataSearchCriteria> quadrantSearchCriteria = new HashMap<MerrillQuadrantEnum, MetadataSearchCriteria>(quadrantToRequest.size());
        for(MerrillQuadrantEnum quadrant : quadrantToRequest.keySet()){
            
            QuadrantRequest request = quadrantToRequest.get(quadrant);
            
            generated.metadata.PresentAt presentAt = new generated.metadata.PresentAt();
            presentAt.setRemediationOnly(request.shouldIncludeRemediationContent() ? generated.metadata.BooleanEnum.TRUE : generated.metadata.BooleanEnum.FALSE);
            if(quadrant != null){
                presentAt.setMerrillQuadrant(quadrant.getName());
            }
            
            MetadataSearchCriteria criteria = new MetadataSearchCriteria(presentAt);
            for(String conceptName : request.getRequiredConcepts()){
                
                generated.metadata.Concept concept = new generated.metadata.Concept();
                concept.setName(conceptName);
                criteria.addConcept(concept);
            }
            
            criteria.setExcludeRuleExampleContent(request.shouldExcludeRuleExampleContent());
            
            //don't want metadata to be filtered by GIFT logic, we want all metadata for the quadrant and the concepts specified
            criteria.setFilterMetadataByGIFTLogic(false);
            
            //allow various subsets of the requested concepts to be used to find metadata (i.e. a returned metadata doesn't
            //have to have every concept but at least 1 and no concepts other than the ones specified)
            criteria.setAnySubsetOfRequired(true);
            
            quadrantSearchCriteria.put(quadrant, criteria);
        }

        String targetCourseFolderPath = FileTreeModel.correctFilePath(courseFolderPath);
        
        //get the course folder proxy
        File courseFolderFile = new File( getWorkspaceFolderProxy(username).getFileId() + File.separator + targetCourseFolderPath);
       
        AbstractFolderProxy courseFolderProxy = new DesktopFolderProxy(courseFolderFile);
        
        //get the metadata under the course folder for the search criteria
        Map<MerrillQuadrantEnum, MetadataFileSearchResult> fileSearchResultsMap;
        try{
            fileSearchResultsMap = MetadataFileFinder.findFiles(courseFolderProxy, quadrantSearchCriteria);
        }catch(IOException e){
            throw new DetailedException("Failed to find all metadata files in the course folder.", 
                    "An error was thrown while searching '"+courseFolderProxy.getFileId()+"' for metadata files: "+e.getMessage()+".", e);
        }
        
        //
        // build a map to return of metadata for the search results for each quadrant
        //
        MetadataSearchResult result = new MetadataSearchResult();
        for(MerrillQuadrantEnum quadrant : fileSearchResultsMap.keySet()){
            
            MetadataFileSearchResult searchResult = fileSearchResultsMap.get(quadrant);
            Map<FileProxy, generated.metadata.Metadata> quadrantFiles = searchResult.getMetadataFilesMap();
            
            Map<String, MetadataWrapper> metadataWrappers = new HashMap<>();
            for(FileProxy metadataFileProxy : quadrantFiles.keySet()){
            
                String metadataFilename;
                try{
                    metadataFilename = workspaceFolderProxy.getRelativeFileName(metadataFileProxy);
                }catch(Exception e){
                    throw new DetailedException("Failed to convert the metadata file name into a course folder relative file name.", 
                            "An error was thrown while creating a course folder relative file name for '"+metadataFileProxy.getFileId()+"'.\n\nThe error reads:\n"+e.getMessage()+".", e);
                }
                
                generated.metadata.Metadata metadata = quadrantFiles.get(metadataFileProxy);
                String displayName = MetadataSchemaHandler.getDisplayName(metadata);
                if(displayName == null){
                    displayName = metadataFilename;
                }
                
                ContentTypeEnum contentTypeEnum = null;
                try {
                    contentTypeEnum = getContentType(username, courseFolderProxy, metadata);
                } catch (@SuppressWarnings("unused") IllegalArgumentException | UnsupportedVersionException | IOException e) {
                    // best effort - for now
                    logger.error("Failed to determine the content type for '"+metadataFilename+"' on behalf of "+username);
                }

                MetadataWrapper wrapper = 
                        new MetadataWrapper(metadataFilename, displayName, MetadataSchemaHandler.isRemediationOnly(metadata), false, contentTypeEnum);
                wrapper.setMetadata(metadata);

                metadataWrappers.put(metadataFilename, wrapper);
            }
            
            QuadrantResultSet resultSet = new QuadrantResultSet(metadataWrappers);
            result.add(quadrant, resultSet);
        }

        
        return result;
    }
    
    /**
     * Populates the file tree model specified based on the file name provided.  For example if the model
     * is: "hemorrhage control/" and the file name is "a/b/c/my.course.xml" the resulting file tree will look like:
     * "hemorrhage control/a/b/c/my.course.xml". If a directory in the tree doesn't exist it will be added.
     * 
     * @param directoryTreeModel the tree model to add too.  Can't be null.
     * @param fileNameWithPath the relative path from the directory tree model to a file, including the filename.  
     * @throws DetailedException if there was a problem with the file name.
     */
    @SuppressWarnings("unused")
    private void populateFileTreeModel(FileTreeModel directoryTreeModel, String fileNameWithPath) throws DetailedException{
        
        StringTokenizer tokenizer = new StringTokenizer(fileNameWithPath, Constants.BACKWARD_SLASH, true);
        
        String fileOrDirName, nextToken = null;
        FileTreeModel currentParent = directoryTreeModel;
        while(tokenizer.hasMoreTokens()){
            
            //will be a folder or file name
            fileOrDirName = tokenizer.nextToken();
            
            if(tokenizer.hasMoreElements()){
                //will be a forward slash if currently on a folder or null if reached a file
                nextToken = tokenizer.nextToken();
            }else{
                nextToken = null;
            }
            
            if(!currentParent.hasChildFileOrDirectoryName(fileOrDirName)){
                //the node doesn't exist so need to create it
                
                if(nextToken == null){
                    //the child is a file
                    FileTreeModel newChild = new FileTreeModel(fileOrDirName);
                    currentParent.addSubFileOrDirectory(newChild);
                }else{
                    //the child is a folder
                    FileTreeModel newChild = new FileTreeModel(fileOrDirName, new ArrayList<>());
                    currentParent.addSubFileOrDirectory(newChild);
                    currentParent = newChild;
                }
                
            }else{
                //the node does exist, get it and set as next parent
                currentParent = currentParent.getChildByName(fileOrDirName);
            }
            
        }//end while
    }

    @Override
    public FileTreeModel getRootTree(String username) {
        return getFileTree(((DesktopFolderProxy) getWorkspaceFolderProxy(username)).getFolder());
    }
    
    @Override
    public FileTreeModel getFileTree(String username, FileType fileType) throws IllegalArgumentException{
        return getFileTree(((DesktopFolderProxy) getWorkspaceFolderProxy(username)).getFolder(), AbstractSchemaHandler.getFileExtension(fileType));
    }
    
    /**
     * Return the File tree containing all descendant directories as well as files within those
     * directories that match the extension of the file extension(s) specified.
     * 
     * @param file when this is a folder, the tree model will contain it as the root and all descendant folders
     * will be represented.  If a file is found to match one of the extensions it will also be included in the model
     * at the appropriate location.  If this is a file the model returned will be null if the file doesn't match one
     * of the extensions.  Can't be null.
     * @param extensions file extensions (e.g. ".course.xml") to filter on (i.e. include files with those extension(s)).  If empty, all files will be included in the returned model
     * @return a file tree model representative of the file provided filtering on (i.e. including) the file extension(s) specified (if any)
     * @throws IllegalArgumentException if a parameter specified is not valid
     */
    private FileTreeModel getFileTree(File file, String... extensions) throws IllegalArgumentException{
        
        if(file == null){
            throw new IllegalArgumentException("The folder can't be null.");
        }
            
        List<FileTreeModel> subFileModels = new ArrayList<FileTreeModel>();
        
        if(file.isDirectory()){
            //checking a directory
            
            File[] files = file.listFiles(FileFinderUtil.getSVNFolderAndExtensionsFileFilter());
            
            for(File subFile : files){
                
                FileTreeModel subFileModel = getFileTree(subFile, extensions);
                
                //the subFile will be null when it is a file (not a directory) that doesn't satisfy the extension
                if(subFileModel != null){
                    subFileModels.add(subFileModel);
                }
            }
        
            //add the directory with its descendants
            return new FileTreeModel(file.getName(), subFileModels);
        
        } else {
            //checking a file
            
            if(extensions != null && extensions.length > 0){
                //see if the file matches one of the extensions
            
                boolean hasIncludedExtension = false;
                
                for(String extensition : extensions){
                    
                    if(file.getName().endsWith(extensition)){
                        hasIncludedExtension = true;
                        break;
                    }
                }
                
                if(hasIncludedExtension){
                    //file matches one extension
                    FileTreeModel model = new FileTreeModel(file.getName());
                    return model;
                } 
                
            } else {
                return new FileTreeModel(file.getName());
            }
            
        }
        
        //the file is not a directory and doesn't match one of the extensions
        return null;
    }

    /**
     * Return the file tree containing all descendant directories as well as files within those
     * directories that are not GIFT authorable files (e.g. dkf.xml).  This is useful for showing 
     * the files in a file management UI that can't be necessarily managed through GIFT authoring tools.
     * 
     * @param file when this is a folder, the tree model will contain it as the root and all descendant folders
     * will be represented.  If a file is found to not be a GIFT file type it will also be included in the model
     * at the appropriate location.  Can't be null.
     * @return model representative of the file provided not including GIFT file types.
     */
    private FileTreeModel getNonGIFTFilesFileTree(File file){
        
        if(file == null){
            throw new IllegalArgumentException("The folder can't be null.");
        }
            
        List<FileTreeModel> subFileModels = new ArrayList<FileTreeModel>();
        
        if(file.isDirectory()){
            //checking a directory
            
            File[] files = file.listFiles(FileFinderUtil.IGNORE_SVN_AND_GIFT_FILES_FILE_FILTER);
            
            for(File subFile : files){
                
                //ignore GIFT managed slide show directory                
                String folderRelativeFileName = subFile.getAbsolutePath().substring(file.getAbsolutePath().length()+1);
                if(folderRelativeFileName.startsWith(SLIDE_SHOWS_FOLDER_NAME)){
                    continue;
                }
                
                FileTreeModel subFileModel = getNonGIFTFilesFileTree(subFile);
                subFileModels.add(subFileModel);
            }
        
            //add the directory with its descendants
            return new FileTreeModel(file.getName(), subFileModels);
        }else{
            return new FileTreeModel(file.getName());        
        }
    }

    @Override
    public FileTreeModel getFileTree(String username, String... extensions) throws IllegalArgumentException {
        return getFileTree(((DesktopFolderProxy) getWorkspaceFolderProxy(username)).getFolder(), extensions);
    }
    
    @Override
    public void unlockFile(String username, String browserSessionKey, String workspaceRelativePath) throws IllegalArgumentException {
        FileLockManager.getInstance().unlock(username, browserSessionKey, workspaceRelativePath);
    }
    
    @Override
    public void unlockAllFiles(String username, String browserSessionKey) throws IllegalArgumentException {
        FileLockManager.getInstance().unlockAll(username, browserSessionKey);
    }

    @Override
    public boolean isLockedFile(String username, String workspaceRelativePath) throws IllegalArgumentException {
        return FileLockManager.getInstance().isLocked(workspaceRelativePath, username);
    }
    
    @Override
    public CourseFileAccessDetails lockFile(String username, String browserSessionKey, String workspaceRelativePath, boolean initialAcquisition) throws DetailedException {
        
        boolean writePermissions = hasWritePermissions(username, workspaceRelativePath);
        return FileLockManager.getInstance().lock(workspaceRelativePath, username, browserSessionKey, writePermissions,
                initialAcquisition);
    }

    @Override
    public boolean deleteFile(String username, String browserSessionKey, String originalFilePath, ProgressIndicator progress, boolean updateCourseFolderLastModifiedDate) throws DetailedException {
        
        if(logger.isDebugEnabled()){
            logger.debug("Attempting to delete "+originalFilePath);
        }
        
        boolean deleted = false;
        boolean shouldUpdateCourseFolder = updateCourseFolderLastModifiedDate;
        
        String fileName = FileTreeModel.correctFilePath(originalFilePath);
        
        FileTreeModel fileModel = FileTreeModel.createFromRawPath(fileName);
        
        File fileToDelete = new File(getWorkspaceFolderProxy(username).getFileId() + File.separator + fileName);
        
        if(fileToDelete.isDirectory()){
        	
        	//populate a file tree for the target file, relative to the workspace folder
        	FileTreeModel populatedFileModel = getFileTree(fileToDelete);
        	
        	List<FileTreeModel> populatedFileModelList = new ArrayList<>();
        	populatedFileModelList.add(populatedFileModel);
        	
        	fileModel.getParentTreeModel().setSubFilesAndDirectories(populatedFileModelList);
        	fileModel = populatedFileModel;
            
            List<FileTreeModel> lockedFiles = new ArrayList<>();
            
            //get locks on all descendant files            
            updateProgress(progress, "Locking files...", 0);
            if(!lockAllFiles(username, browserSessionKey, fileModel, lockedFiles)){
                throw new DetailedException("Failed to acquire the lock on all files before deleting folder.",
                        "All the files under the folder '"+fileModel.getFileOrDirectoryName()+"' must be available before they can be deleted.", null);
            }
            updateProgress(progress, null, 20);
            
            //delete the directory
            boolean deleteFailed = false;
            File dirToDelete = new File(getWorkspaceFolderProxy(username).getFileId() + File.separator + fileName);
            DetailedException delayedException = null;
            if(dirToDelete.exists()){
                try{
                    
                    updateProgress(progress, "Deleting " + dirToDelete, 0);
                    
                    /* check to see if the course folder should be updated */
                    if (shouldUpdateCourseFolder) {
                    try{
	                    DesktopFolderProxy courseFolder = (DesktopFolderProxy) getCourseFolder(fileName, username);
                            if (courseFolder != null && dirToDelete.getCanonicalPath()
                                    .equals(courseFolder.getFolder().getCanonicalPath())) {
	                    
                                // we're deleting a course folder, so we don't need to update its
                                // modification date
                        shouldUpdateCourseFolder = false;
                    }
                    } catch(@SuppressWarnings("unused") Exception e){
                    	
                            // if the file being deleted is not in a course folder, don't bother
                            // updating the last modified date
                        shouldUpdateCourseFolder = false;
                    }
                    }
                    
                    FileUtils.deleteDirectory(dirToDelete);
                    
                    updateProgress(progress, null, 20);
                    
                }catch(IOException e){
                    //don't throw this now because we want to make sure files are unlocked before leaving this method
                    delayedException = new DetailedException("Failed to delete '"+dirToDelete+"'.", 
                            "There was an error while trying to delete the directory : "+e.getMessage(), e);
                }
            }else{
                //the directory doesn't exist
                deleteFailed = true;
            }
            
            //clean up the lock manager
            updateProgress(progress, "Cleaning up...", 0);
            for(FileTreeModel lockedFile : lockedFiles){
                unlockFile(username, browserSessionKey, lockedFile.getRelativePathFromRoot());
            }
            updateProgress(progress, null, 20);
            
            if(deleteFailed){
                deleted = false;
            }else if(delayedException != null){
                //files have been unlocked, safe to exit this method now
                throw delayedException;
            }else{
                updateProgress(progress, null, 100);
                deleted = true;
            }
            
        }else{
        
            if(!isLockedFile(username, fileName)){
                
                //Checks to see if the file exists on the system
                if(!fileToDelete.exists()) {
                    return false;
                }
                
                updateProgress(progress,"Deleting " + fileModel.getFileOrDirectoryName(), 0);
                                
                try {
                    Files.delete(fileToDelete.toPath());
                    deleted = true;
                    
                } catch (IOException e) {                   
                    throw new DetailedException("Unable to delete '" + fileName + "'.",
                            "The file system has prevented this file from being deleted.", e);
                }
                
                updateProgress(progress, null, 100);
                
            }else{
                logger.error("Unable to delete "+fileName+" because it is locked.");
            }
        }
        
        if(deleted && shouldUpdateCourseFolder){
            
            try{
            //get the course folder
            	DesktopFolderProxy courseFolder = (DesktopFolderProxy) getCourseFolder(fileName, username);
                
            //update course folder date modified
            //Note: we have to explicitly update the course folder date modified because it is not guaranteed
            //      that the file system will update it in all file operations for any nesting of files.
            courseFolder.getFolder().setLastModified(System.currentTimeMillis());
                
            }catch(@SuppressWarnings("unused") Exception e){
            	
            	//if the file being deleted is not in a course folder, don't bother updating the last modified date
                logger.debug("No course folder was found for " + fileName + ", so the course folder's last modified date"
                		+ "will not be updated.");
        }
        }
        
        return deleted;
    }
    
    @Override
    public boolean renameTrainingAppsLibFolder(String username, String filePath, String newName) {
        FileTreeModel file = FileTreeModel.createFromRawPath(filePath);
        
        File fileToRename = new File(
                getWorkspaceFolderProxy(username).getFileId() + File.separator + file.getRelativePathFromRoot());

        if (!fileToRename.exists()) {
            throw new IllegalArgumentException("The filePath provided cannot be found.");
        } else if (!fileToRename.isDirectory()) {
            throw new IllegalArgumentException("The filePath does not point to a directory.");
        }

        final String originalName = fileToRename.getName();

        // update inner file names first
        File[] childFilesToUpdate = fileToRename.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(originalName + ".");
            }
        });

        for (File childFile : childFilesToUpdate) {
            FileTreeModel childModel = file.getModelFromRelativePath(childFile.getName());
            String childNewName = childModel.getFileOrDirectoryName().replaceFirst(originalName, newName);
            renameFile(username, childModel.getRelativePathFromRoot(), childNewName, false);
        }

        return renameFile(username, filePath, newName, false);
    }

    @Override
    public boolean renameFile(String username, String filePath, String newName, boolean updateCourseFolderLastModifiedDate) {
        logger.debug("Attempting to rename "+filePath);
        boolean success = false;

        FileTreeModel file = FileTreeModel.createFromRawPath(filePath);
        
        File fileToMove = new File(getWorkspaceFolderProxy(username).getFileId() + File.separator + file.getRelativePathFromRoot());
        
        String newFilePath = file.getParentTreeModel().getRelativePathFromRoot() + File.separator + newName;
        File newFile = new File(getWorkspaceFolderProxy(username).getFileId() + File.separator + newFilePath);

        if(fileToMove.exists()) {

            try {
                
                if(fileToMove.getPath().equalsIgnoreCase(newFile.getPath())) {
                    
                    // Files.move does nothing if the paths are equal(case INsensitive), so in this case, File.renameTo should be used
                    
                    fileToMove.renameTo(newFile);
                    
                } else {
                    
                    Files.move(fileToMove.toPath(), newFile.toPath());
                }
                
                success = true;
                
                // exit early if not updating the last modified date for the course folder.
                if (!updateCourseFolderLastModifiedDate) {
                    return success;
                }

                //get the course folder
                DesktopFolderProxy courseFolder;
                try{
                    courseFolder = (DesktopFolderProxy) getCourseFolder(newFilePath, username);
                }catch(IOException e){
                    throw new DetailedException("Failed to update the course folder date modified.", 
                            "Failed to retrieve course folder of the destination where the file '"+file.getFileOrDirectoryName()+"' is being renamed because "+e.getMessage()+
                            ". This can cause the course validation logic to be bypassed if you have validated the course successfully since the last date modified was set on the course folder.",
                            e);
                }
                
                //update course folder date modified
                //Note: we have to explicitly update the course folder date modified because it is not guaranteed
                //      that the file system will update it in all file operations for any nesting of files.
                courseFolder.getFolder().setLastModified(System.currentTimeMillis());
                
            } catch (FileAlreadyExistsException e) { 
                throw new DetailedException("Cannot rename the file '" 
                        + file.getRelativePathFromRoot() + "'.  A file with the name '" + newName + "' already exists.", 
                        e.getMessage(), e);
            } catch (Exception e) {
                throw new DetailedException("An error occurred while trying to rename the file '" + file.getRelativePathFromRoot() + "'.", 
                        e.getMessage(), e);
            }
        } else {
            throw new DetailedException("An error occurred while trying to rename the file.",  
                    "The file '" + file.getRelativePathFromRoot() + "' was not found.", null);
        }

        return success;
    }

    @Override
    public UnmarshalledFile unmarshalFile(String username, String filePath) 
            throws DetailedException, FileNotFoundException, IllegalArgumentException, UnsupportedVersionException {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Unmarshalling "+filePath);
        }
        
        String targetFilePath = FileTreeModel.correctFilePath(filePath);
        
        FileType fileType = AbstractSchemaHandler.getFileType(targetFilePath);
        
        File fileToUnmarshal = new File(((DesktopFolderProxy) getWorkspaceFolderProxy(username)).getFolder().getAbsolutePath() + File.separator + targetFilePath);
        
        if(fileToUnmarshal.isDirectory()){
            throw new IllegalArgumentException("The file can't be a directory.");
        }
        
        FileProxy fileProxy = new FileProxy(fileToUnmarshal);
        
        return AbstractLegacySchemaHandler.getUnmarshalledFile(fileProxy, fileType);
    }
    
    @Override
    public boolean marshalToFile(String username, Serializable generatedObject, String file, String version, boolean useParentAsCourse) 
            throws IllegalArgumentException, FileNotFoundException{
        
        if (logger.isDebugEnabled()) {
            logger.debug("Marshalling to "+file);
        }
        
        FileType fileType = AbstractSchemaHandler.getFileType(file);
        File fileToMarshal = new File(((DesktopFolderProxy) getWorkspaceFolderProxy(username)).getFolder().getAbsolutePath() + File.separator + file);
        List<ValidationEvent> validationEvents = null;
        
        if(fileType == FileType.QUESTION_EXPORT || fileType == FileType.SURVEY_EXPORT){
            //create a survey or question export file
            
            JSONObject obj = new JSONObject();
            try{                
                if (fileType == FileType.QUESTION_EXPORT) {
                QuestionJSON json = new QuestionJSON();
                json.encode(obj, generatedObject);
                } else {
                    SurveyContextJSON json = new SurveyContextJSON();
                    json.encode(obj, generatedObject);
                }
            }catch(Exception e){
                throw new DetailedException("There was a problem creating the survey or question export file '" + file + "'.",
                        "The Survey or Question could not be encoded due to an exception, there the file '" + fileToMarshal.getAbsolutePath() + "' could not be created.  The error message reads: " + e.getMessage(),
                        e);
            }
            
            try {
                FileUtils.write(fileToMarshal, obj.toJSONString());
            } catch (IOException e) {
                throw new DetailedException("There was a problem creating the survey or question export file '" + file + "'.",
                        "Failed to create the file '" + fileToMarshal.getAbsolutePath() + "' because " + e.getMessage(),
                        e);
            }
            
        }else if (fileType == FileType.XTSP_JSON) {
        	// Create an xTSP file
        	
        	try {
        		if (generatedObject instanceof String) {
        			String output = (String) generatedObject;
        			FileUtils.write(fileToMarshal, output);
        		}
        	} catch (Exception e) {
    			throw new DetailedException("There was a problem updating the xTSP file '" + file + "'.",
                        "The xTSP could not be encoded due to an exception, there the file '" + fileToMarshal.getAbsolutePath() + "' was not properly updated.  The error message reads: " + e.getMessage(),
                        e);
        	}
        }else{
        if(version == null || version.equals(Version.getInstance().getCurrentSchemaVersion())) {
    			// this is the current GIFT version
            try{
                validationEvents = AbstractSchemaHandler.writeToFile(generatedObject, fileToMarshal, true);
            }catch(IOException | SAXException | JAXBException e){
                throw new DetailedException("Failed to write the XML file.", 
                        "There was a problem writing '"+fileToMarshal+"' : "+e.getMessage(), e);
            }
        } else {
            	// This is a backup file of a previous version of GIFT, retrieve the corresponding schema file and generated class
            	AbstractConversionWizardUtil cWizard = AbstractConversionWizardUtil.getConversionWizardForVersion(version);
            	Class<?> schemaRoot = cWizard.getPreviousSchemaRoot(fileType);
            	File schemaFile = cWizard.getPreviousSchemaFile(fileType);
            	
            	try{
            	    validationEvents = AbstractSchemaHandler.writeToFile(generatedObject, schemaRoot, new FileOutputStream(fileToMarshal) ,schemaFile, true);
            }catch(IOException | SAXException | JAXBException e){
                throw new DetailedException("Failed to write the XML file.", 
                        "There was a problem writing '"+fileToMarshal+"' : "+e.getMessage(), e);
            }
        }
        }
        
        
        //get the course folder
        DesktopFolderProxy courseFolder;
        try{
        if (useParentAsCourse) {
            courseFolder = new DesktopFolderProxy(fileToMarshal.getParentFile());
        } else {
            courseFolder = (DesktopFolderProxy) getCourseFolder(file, username);
        }
        }catch(IOException e){
            throw new DetailedException("Failed to update the course folder date modified.", 
                    "Failed to retrieve course folder of the destination where the file '" + file
                            + "' is being written because " + e.getMessage()
                            + ". This can cause the course validation logic to be bypassed if you have validated the course successfully since the last date modified was set on the course folder.",
                    e);
    }
    
        /* update course folder date modified Note: we have to explicitly update the course
         * folder date modified because it is not guaranteed that the file system will update it
         * in all file operations for any nesting of files. */
        courseFolder.getFolder().setLastModified(System.currentTimeMillis());
        
        return validationEvents == null || validationEvents.isEmpty();
    }
    
    @Override
    public String readFileToString(String username, FileTreeModel file) throws IllegalArgumentException, DetailedException{
        
        String filename = file.getRelativePathFromRoot(true);     

        File fileToRead = new File(((DesktopFolderProxy) getWorkspaceFolderProxy(username)).getFolder().getAbsolutePath() + File.separator + filename);
        
        if(!fileToRead.exists()){
            throw new DetailedException("The file '"+fileToRead+"' was not found.",
                    "Failed to read the file specified to a string because the file doesn't exist.", null);
        }
        
        try{
            return FileUtils.readFileToString(fileToRead);
        }catch(IOException e){
            throw new DetailedException("Failed to read '"+fileToRead+"'.", 
                    "Failed to read the file specified to a string because an error was thrown : "+e.getMessage(), e);
        }
    }
    
    @Override
    public void updateFileContents(String username, String filePath, String content, boolean createBackup, boolean useAdminPrivilege) throws DetailedException {
        
        File sourceFile = new File(((DesktopFolderProxy) getWorkspaceFolderProxy(username)).getFolder().getAbsolutePath() + File.separator + filePath);
        
        if(createBackup) {
            File backupFile = new File(sourceFile.getPath() + ".bak");
            try {
                FileUtils.copyFile(sourceFile, backupFile);
            } catch (IOException e) {
                throw new DetailedException("There was a problem creating a backup of the file '" + filePath + "'.",
                        "Failed to backup the file '" + filePath + "' because " + e.getMessage(),
                        e);
            }
        }
        
        try {
            //without UTF-8 encoding gift xml files (like Logic puzzle tutorial course xml) would write incorrect characters
            //which would cause unmarshalling issues (#3045)
            FileUtils.write(sourceFile, content, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new DetailedException("There was a problem updating the file '" + filePath + "'.",
                    "Failed to update the file '" + filePath + "' because " + e.getMessage(),
                    e);
        }
        
        //get the course folder
        DesktopFolderProxy courseFolder;
        try{
            courseFolder = (DesktopFolderProxy) getCourseFolder(filePath, username);
        }catch(IOException e){
            throw new DetailedException("Failed to update the course folder date modified.", 
                    "Failed to retrieve course folder of the destination where the file '"+filePath+"' is being written because "+e.getMessage()+
                    ". This can cause the course validation logic to be bypassed if you have validated the course successfully since the last date modified was set on the course folder.",
                    e);
        }
        
        //update course folder date modified
        //Note: we have to explicitly update the course folder date modified because it is not guaranteed
        //      that the file system will update it in all file operations for any nesting of files.
        courseFolder.getFolder().setLastModified(System.currentTimeMillis());
        
    }
    
    @Override
    public List<String> getSIMILEConcepts(String username, FileTreeModel simileConfigFile) 
            throws FileNotFoundException, IllegalArgumentException, DetailedException{
        
        String filename = simileConfigFile.getRelativePathFromRoot(true);  
        File fileToRead = new File(((DesktopFolderProxy) getWorkspaceFolderProxy(username)).getFolder().getAbsolutePath() + File.separator + filename);
        
        try{
            return DomainKnowledgeUtil.getSIMILEConcepts(fileToRead.getAbsolutePath(), new FileInputStream(fileToRead));
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve the SIMILE concepts from the ixs file.", 
                    "There was an error while retrieving the concepts from the SIMILE ixs file of '"+fileToRead.getAbsolutePath()+"' : "+e.getMessage(), e);
        }
    }

    @Override
    public DownloadableFileRef exportCourses(ExportProperties exportProperties) throws DetailedException, URISyntaxException {
        
        //create zip where the export contents will be placed
        File exportZipFile = new File(EXPORT_DIRECTORY.getAbsolutePath() + File.separator + exportProperties.getExportFileName() + ".zip");
        exportZipFile.deleteOnExit();
        
        DesktopFolderProxy domainFolder = (DesktopFolderProxy) getWorkspaceFolderProxy(exportProperties.getUsername());
        
        //convert domain options into course folders
        List<AbstractFolderProxy> courseFolders = new ArrayList<>();
        for(DomainOption domainOption : exportProperties.getCoursesToExport()){
            
            String courseFileName = domainOption.getDomainId();
            try{
                AbstractFolderProxy courseFolder = domainFolder.getParentFolder(domainFolder.getRelativeFile(courseFileName));
                courseFolders.add(courseFolder);
            }catch(IOException e){
                throw new DetailedException("Failed to retrieve a course folder.", 
                        "There was a problem retrieving the course folder for the course file of '"+courseFileName+"' : "+e.getMessage(), e);
            }
        }
        
        ExportCourseUtil exportCourseUtil = new ExportCourseUtil();
        try{
            exportCourseUtil.export(courseFolders, exportProperties, exportZipFile);
        
            URL exportURL = ServicesManager.getExportURL(exportZipFile);
        
            return new DownloadableFileRef(exportURL.toString(), exportZipFile.getPath());
        }catch(IOException e){
            
            exportZipFile.delete();
            throw new DetailedException("Failed to export courses.", 
                    "There was an error while exporting courses :"+e.getMessage(), e);
        }
    }

    @Override
    public void importCourses(String username, File giftExportZipToImport,
            LoadedProgressIndicator<List<DomainOption>> progressIndicator, List<String> filesToOverwrite, Map<String, String> courseToNameMap) throws DetailedException {
        
        DesktopFolderProxy workspaceFolder = (DesktopFolderProxy) getWorkspaceFolderProxy(username);
        File usersWorkspaceFolder = new File(workspaceFolder.getFileId() + File.separator + username);
        
        // its possible that a users workspace directory might not exist when the following happens:
        // 1. the user is in the UMS db
        // 2. gift is in desktop deployment and the login page enters OFFLINE mode
        // 3. the user is selected from the login drop down of known gift users
        // 4. this gift instances has never actually had this user successfully login with ONLINE authenticate 
        //    which would have created that user's workspace
        if(!usersWorkspaceFolder.exists()){
            usersWorkspaceFolder.mkdir();
        }
        
        DesktopFolderProxy usersWorkspaceFolderProxy = new DesktopFolderProxy(usersWorkspaceFolder);
        
        ImportCourseUtil importUtil = new ImportCourseUtil();
        importUtil.importCourses(username, usersWorkspaceFolderProxy, giftExportZipToImport, progressIndicator, filesToOverwrite, courseToNameMap);
    }

    @Override
    public float getCourseExportSize(ExportProperties exportProperties)
            throws DetailedException, URISyntaxException {
        
        DesktopFolderProxy domainFolder = (DesktopFolderProxy) getWorkspaceFolderProxy(exportProperties.getUsername());
        
        //convert domain options into course folders
        List<AbstractFolderProxy> courseFolders = new ArrayList<>();
        for(DomainOption domainOption : exportProperties.getCoursesToExport()){
            
            String courseFileName = domainOption.getDomainId();
            try{
                AbstractFolderProxy courseFolder = domainFolder.getParentFolder(domainFolder.getRelativeFile(courseFileName));
                courseFolders.add(courseFolder);
            }catch(IOException e){
                throw new DetailedException("Failed to retrieve a course folder.", 
                        "There was a problem retrieving the course folder for the course file of '"+courseFileName+"' : "+e.getMessage(), e);
            }
        }
        
        ExportCourseUtil exportCourseUtil = new ExportCourseUtil();
        try{
            return exportCourseUtil.getCourseFoldersSize(courseFolders);
        }catch(IOException e){
            throw new DetailedException("Failed to calculate the course export file size.", 
                    "There was a problem while calculating the file size of the course export : "+e.getMessage(), e);
        }
    }

    @Override
    public AbstractFolderProxy createFolder(String username, String parentDirectoryPath,
            String name, boolean ignoreExistingFolder) throws DetailedException, IllegalArgumentException {
        
        String parentDirectoryName = FileTreeModel.correctFilePath(parentDirectoryPath);

        //the directory must reside in the domain directory because we are running desktop mode
        File parentDirectoryFile = new File(getWorkspaceFolderProxy(username).getFileId() + File.separator + parentDirectoryName); 
        
        if(!parentDirectoryFile.exists()){
            throw new IllegalArgumentException("The directory '"+parentDirectoryFile+"' doesn't exist.");
            
        } else if(!parentDirectoryFile.isDirectory()){
            throw new IllegalArgumentException("The parent directory '"+parentDirectoryFile+"' must be a directory.");
        }

        File newDirectory = new File(parentDirectoryFile.getAbsolutePath() + File.separator + name);
        if(newDirectory.exists()){
            
            if(ignoreExistingFolder){
                return new DesktopFolderProxy(newDirectory);
            }
            
            throw new IllegalArgumentException("Can't create a directory '"+newDirectory+"' that already exist.");
        }
        
        newDirectory.mkdir();        
        
        return new DesktopFolderProxy(newDirectory);
    }
    
    @Override
    public AbstractFolderProxy createTrainingAppsLibUserFolder(String username)
            throws DetailedException, IllegalArgumentException {

        File userFolder = new File(getTrainingAppsLibraryFolderProxy().getFileId() + File.separator + username);
        if (userFolder.exists()) {
            throw new IllegalArgumentException("Can't create a directory '" + userFolder + "' that already exist.");
        }

        userFolder.mkdir();

        return new DesktopFolderProxy(userFolder);
    }

    @Override
    public boolean fileExists(String username, String originalFilePath, boolean isFolder) throws DetailedException {
        
        if(originalFilePath == null){
            throw new IllegalArgumentException("The path to the file can't be null.");
        }
        
        String filePath = FileTreeModel.correctFilePath(originalFilePath);

        //the directory must reside in the domain directory because we are running desktop mode
        File parentDirectoryFile = new File(((DesktopFolderProxy) getWorkspaceFolderProxy(username)).getFolder().getAbsolutePath() + File.separator + filePath);  
        
        if(parentDirectoryFile.exists()){
            //check if the user wants to know if the file is an existing folder
            return !isFolder || (isFolder && parentDirectoryFile.isDirectory());
        }
        
        return false;
    }

    @Override
    public FileProxy getFile(String filename, String username) throws IllegalArgumentException, FileNotFoundException, DetailedException {
        if(filename == null) { 
            throw new IllegalArgumentException("The filename for getFile cannot be null");
        }
        
        File diskFile = new File(((DesktopFolderProxy) getWorkspaceFolderProxy(username)).getFolder().getAbsolutePath() + File.separator + filename);        
        return new FileProxy(diskFile);
    }
    
    @Override
    public AbstractFolderProxy getFolderFromFile(File file, String username) {
        return new DesktopFolderProxy(file);
    }

    @Override
    public AbstractFolderProxy getFolder(String folderPath, String username) {
        String correctedFolderPath = FileTreeModel.correctFilePath(folderPath);
        File diskFile = new File(
                ((DesktopFolderProxy) getWorkspaceFolderProxy(username)).getFolder().getAbsolutePath() + File.separator + correctedFolderPath);
        if (!diskFile.isDirectory()) {
            throw new IllegalArgumentException("The file is not a directory.");
        }

        
        return new DesktopFolderProxy(diskFile);
    }

    @Override
    public FileTreeModel trimWorkspaceFromPath(String pathToTrim, String username) {
        String filePath = FileTreeModel.correctFilePath(pathToTrim);

        String workspaceFolderPath = FileTreeModel.correctFilePath(getWorkspaceFolderProxy(username).getFileId());

        // has the workspace preprended, remove it
        if (filePath.startsWith(workspaceFolderPath)) {
            filePath = filePath.substring(workspaceFolderPath.length());
        }

        return FileTreeModel.createFromRawPath(filePath);
    }
    
    @Override
    public String trimWorkspacesPathFromFullFilePath(String fullFilePathToTrim){
        
        if(fullFilePathToTrim.startsWith(getWorkspaceFolderProxy(null).getFileId())){
            return fullFilePathToTrim.substring(getWorkspaceFolderProxy(null).getFileId().length());
        }
        
        return fullFilePathToTrim;
    }

    @Override
    public AbstractFolderProxy getCourseFolder(String filePath,
            String username) throws FileNotFoundException, DetailedException, DetailedException {        
        
        String filename = FileTreeModel.correctFilePath(filePath);
        
        File diskFile = new File(((DesktopFolderProxy) getWorkspaceFolderProxy(username)).getFolder().getAbsolutePath() + File.separator + filename);  
        File courseFolder;
        try {
            // process directory
            if (diskFile.isDirectory()) {
                // check if specified directory exists
        if(!diskFile.exists()){
                    throw new FileNotFoundException("The directory " + diskFile + " doesn't exist.");
        }
        
                courseFolder = FileFinderUtil.findAncestorCourseFolder(diskFile);
            } else { // process file
                // check if parent directory of file exists
                if (!diskFile.getParentFile().exists()) {
                    throw new FileNotFoundException(
                            "The file's parent directory " + diskFile.getParentFile() + " doesn't exist.");
                }

                courseFolder = FileFinderUtil.findAncestorCourseFolder(diskFile.getParentFile());
            }
        }catch(IOException e){
            throw new DetailedException("Failed to find the course folder.", 
                    "There was a problem while searching for the course folder starting at '"+diskFile+"' : "+e.getMessage(), e);
        }
        
        if(courseFolder == null){
            throw new DetailedException("Failed to find a course folder.", 
                    "Failed to find a course folder starting at '"+diskFile+"' and searching up the file tree.", null);
        }
        
        return new DesktopFolderProxy(courseFolder);
    }

    @Override
    public long getRemainingWorkspacesQuota(String username) {
        return Long.MAX_VALUE;
    }

    @Override
    public List<String> getMetadataForContent(String username,
            FileTreeModel content, MerrillQuadrantEnum quadrant)
            throws DetailedException, URISyntaxException {
        
        //
        // search for metadata files in the same directory as the content
        //
        
        //get the content's parent folder proxy
        String relativeFolderName = content.getParentTreeModel().getRelativePathFromRoot(true);
        File contentFolderFile = new File( getWorkspaceFolderProxy(username).getFileId() + File.separator + relativeFolderName);
       
        AbstractFolderProxy contentFolderProxy = new DesktopFolderProxy(contentFolderFile);
        
        FileProxy contentFileProxy;
        try {
            contentFileProxy = getFile(content, username);
        } catch (IllegalArgumentException e) {
            throw new DetailedException("Failed to retrieve the metadata files for content.", "There was a problem with the content file provided of '"+content.getRelativePathFromRoot(true)+"'.\n\n"+e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new DetailedException("Failed to retrieve the metadata files for content.", "There was a problem with the content file provided of '"+content.getRelativePathFromRoot(true)+"'.\n\n"+e.getMessage(), e);
        }
        
        List<FileProxy> metadataFiles = MetadataFileFinder.findFilesForContent(contentFolderProxy, contentFileProxy, quadrant);
        
        //build a list of the metadata file names
        List<String> metadataFilenames = new ArrayList<>();
        for(FileProxy metadataFileProxy : metadataFiles){
            
            String metadataFilename;
            try{
                metadataFilename = workspaceFolderProxy.getRelativeFileName(metadataFileProxy);
            }catch(Exception e){
                throw new DetailedException("Failed to convert the metadata file name into a course folder relative file name.", 
                        "An error was thrown while creating a course folder relative file name for '"+metadataFileProxy.getFileId()+"'.\n\nThe error reads:\n"+e.getMessage()+".", e);
            }
            
            metadataFilenames.add(metadataFilename);
        }
        
        return metadataFilenames;
    }
    
    @Override
    public Map<FileTreeModel, TrainingAppCourseObjectWrapper> getTrainingAppCourseObjects(String username, ProgressIndicator progressIndicator)
            throws DetailedException {
        
        //find the files
        List<FileProxy> trainingAppFiles = new ArrayList<>();
        try{
            DesktopFolderProxy searchFolder;
            
            if(progressIndicator != null){
                progressIndicator.setTaskDescription(GET_SHOWCASE_TA_OBJECTS_PROGRESS_DESC);
                progressIndicator.setPercentComplete(GET_SHOWCASE_TA_OBJECTS_PROGRESS_PERC);
            }
                
            // search Public folder
            File publicFolder = new File(((DesktopFolderProxy) getWorkspaceFolderProxy(username)).getFolder().getAbsolutePath() + File.separator
                    + getPublicTrainingAppsLibraryTreeModel(username).getRelativePathFromRoot(true));
            if (publicFolder.exists()) {
                searchFolder = new DesktopFolderProxy(publicFolder);
                FileFinderUtil.getFilesByExtension(searchFolder, trainingAppFiles,
                        AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);
            }
            
            if(progressIndicator != null){
                progressIndicator.setTaskDescription(GET_USERNAME_TA_OBJECTS_PROGRESS_DESC);
                progressIndicator.setPercentComplete(GET_USERNAME_TA_OBJECTS_PROGRESS_PERC);
            }
               
            // search user folder
            if (StringUtils.isNotBlank(username)) {
                File userFolder = new File(getTrainingAppsLibraryFolderProxy().getFileId() + File.separator + username);
                if (userFolder.exists()) {
                    searchFolder = new DesktopFolderProxy(userFolder);
                    FileFinderUtil.getFilesByExtension(searchFolder, trainingAppFiles,
                            AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);
                }
            }

        }catch(Exception e){
            throw new DetailedException("Failed to retrieve the training application course objects.",
                    "There was an exception while searching the training application lib folder. The error reads:\n"
                            + e.getMessage(),
                    e);
        }        
        
        if(progressIndicator != null){
            progressIndicator.setTaskDescription(CHECK_TA_OBJECTS_PROGRESS_DESC);
            progressIndicator.setPercentComplete(CHECK_TA_OBJECTS_PROGRESS_PERC);
        }
        
        //parse the files
        Map<FileTreeModel, TrainingAppCourseObjectWrapper> trainingAppObjects = new HashMap<>();
        for(FileProxy file : trainingAppFiles){
            
            DesktopFolderProxy courseObjectFolder = null;
            generated.course.TrainingApplication trainingApp = null;
            DetailedException validationException = null;
            //get generated class instance from XML file
            try {
                courseObjectFolder = (DesktopFolderProxy) getWorkspaceFolderProxy(username)
                        .getParentFolder(file);
                List<String> updatedFiles = AbstractConversionWizardUtil.updateTrainingAppToLatestVersion(file, courseObjectFolder, false);
                if(!updatedFiles.isEmpty()){
                    if(progressIndicator != null){
                        progressIndicator.setTaskDescription(UPCONVERTING_TA_OBJECTS_PROGRESS_DESC);
                    }
                }                
                
                UnmarshalledFile uFile = AbstractSchemaHandler.parseAndValidate(
                        generated.course.TrainingApplicationWrapper.class, file.getInputStream(),
                        AbstractSchemaHandler.TRAINING_APP_ELEMENT_SCHEMA_FILE, false);
                TrainingApplicationWrapper taWrapper = (TrainingApplicationWrapper) uFile.getUnmarshalled();
                trainingApp = taWrapper.getTrainingApplication();
                
            } catch (Exception e) {
                logger.error(
                    "Caught exception while trying to parse the training application XML file of " + file + ".", e);
                
                String details = e.getMessage();
                if(details == null){
                    details = "An exception was thrown when reading '"+file+"'.";
                }
                validationException = new FileValidationException(
                    "There was a problem while parsing the training application XML file during validation.",
                    details, file.getFileId(), e);
            } 
            
            if(courseObjectFolder == null){
                throw new DetailedException("Failed to get the training application course object folder.", 
                        "There was an exception while retrieving the parent folder for training application XML file of '"
                                + file + "'.",
                                validationException);
            }
            
            try {

                String courseObjFileId = courseObjectFolder.getFileId();
                final String taLibFileId = getTrainingAppsLibraryFolderProxy().getFileId();
                if (courseObjFileId.startsWith(taLibFileId)) {
                    courseObjFileId = courseObjFileId.substring(taLibFileId.length());
                }
            
                FileTreeModel fileModel = getTrainingAppsLibraryTreeModel(username)
                        .getModelFromRelativePath(courseObjFileId);
            
                // a default directory was found, remove it. The default name must match
                // mil.arl.gift.tools.authoring.server.gat.client.wrap.widgets.CourseObjectItemEditor#DEFAULT_ASSESSMENT_NAME
                if (StringUtils.equals(courseObjectFolder.getName(), DEFAULT_DKF_ASSESSMENT_FOLDER_NAME)) {
                    deleteFile(username, username, fileModel.getRelativePathFromRoot(true), null, false);
                    continue;
                }
                
                ContentTypeEnum contentTypeEnum = null;
                try {
                    contentTypeEnum = getContentType(trainingApp);
                } catch (@SuppressWarnings("unused") Exception e) {
                    // best effort - for now
                    logger.error("Failed to determine the content type for '"+courseObjFileId+"' on behalf of "+username);
                }
                
                if(validationException != null){
                    trainingAppObjects.put(fileModel,
                            new TrainingAppCourseObjectWrapper(courseObjectFolder.getName(), validationException, contentTypeEnum));
                }else{
                    trainingAppObjects.put(fileModel, new TrainingAppCourseObjectWrapper(trainingApp, contentTypeEnum));
                }
            }catch(Exception e){
                throw new DetailedException("Failed to get the training application course object folder.", 
                        "There was an exception while building the file tree model for training application XML file of '"
                                + file + "'.  The error reads:\n" + e.getMessage(),
                        e);
            }            
            
            if(progressIndicator != null){
                progressIndicator.setPercentComplete(progressIndicator.getPercentComplete() + 30/trainingAppFiles.size());
            }
            
        }
        
        return trainingAppObjects;
    }

    @Override
    public FileTreeModel getMediaFiles(String username, String courseFolderPath) {

        File courseFolderFile = new File(((DesktopFolderProxy) getWorkspaceFolderProxy(username)).getFolder().getAbsolutePath() + File.separator + courseFolderPath); 
        
        return getNonGIFTFilesFileTree(courseFolderFile);
    }

    @Override
    public Map<File, File> checkForImportConflicts(String username, File giftExportZipToImport, ProgressIndicator progress) throws DetailedException {        
        ImportCourseUtil importUtil = new ImportCourseUtil();
        
        // convert the root workspace directory into the sub user's directory
        DesktopFolderProxy workspaceRoot = (DesktopFolderProxy) getWorkspaceFolderProxy(username);
        File userWorkspace = new File(workspaceRoot.getFolder().getAbsolutePath() + File.separator + username);
        return importUtil.checkForConflicts(giftExportZipToImport, new DesktopFolderProxy(userWorkspace), progress);    
    }

    @Override
    public Date getCourseFolderLastModified(String courseFolderPath, String username) throws IOException {
        
        File courseFolderFile = new File(courseFolderPath);
        if(!courseFolderFile.exists()){
            //try adding the workspace path to the path provided
            courseFolderFile = new File(((DesktopFolderProxy) getWorkspaceFolderProxy(username)).getFolder().getAbsolutePath() + File.separator + courseFolderPath); 
        }
        
        if(courseFolderFile.exists()){
            return new Date(courseFolderFile.lastModified());
        }
        
        return null;
    }

	@Override
	public boolean hasWritePermissions(String username, String filePath) {
		
		//users always have permission to write in Desktop mode
		return true;
	}

	@Override
	public FileTreeModel getFileTreeByPath(String username, String filePath) {
		
		String fileName = FileTreeModel.correctFilePath(filePath);
	        
        FileTreeModel fileModel = FileTreeModel.createFromRawPath(fileName);
        
        File file = new File(getWorkspaceFolderProxy(username).getFileId() + File.separator + fileName);
        
        if(file.isDirectory()){
        	
        	//populate a file tree for the target file
        	FileTreeModel populatedFileModel = getFileTree(file);
        	
        	List<FileTreeModel> populatedFileModelList = new ArrayList<>();
        	populatedFileModelList.add(populatedFileModel);
        	
        	fileModel.getParentTreeModel().setSubFilesAndDirectories(populatedFileModelList);
        	
        	return populatedFileModel;	
        	
        } else if(file.exists()){
        	return fileModel;
        	
        } else {
        	return null;
        }
	}

	@Override
    public String copyFile(String username, String originalSourcePath, String originalDestinationPath,
            NameCollisionResolutionBehavior nameCollisionResolutionBehavior, ProgressIndicator progress,
            boolean isDomainFile, boolean useAdminPrivilege) throws FileExistsException, IllegalArgumentException, DetailedException {
		
		String sourcePath = FileTreeModel.correctFilePath(originalSourcePath);
        String destinationPath = FileTreeModel.correctFilePath(originalDestinationPath);
        
        //the file must reside in the domain directory because we are running desktop mode
        File sourceFile;
        if(isDomainFile){
            sourceFile = new File(DOMAIN_DIRECTORY.getAbsolutePath() + File.separator + sourcePath);
        } else {
            sourceFile = new File(getWorkspaceFolderProxy(username).getFileId() + File.separator + sourcePath);
        }
        
        if(sourceFile.isDirectory()){
            return copyFolderToFolder(username, sourceFile, destinationPath, nameCollisionResolutionBehavior, progress);
        } else {
            // copy a file
            File destinationFile = new File(getWorkspaceFolderProxy(username).getFileId() + File.separator + destinationPath);
            
            updateProgress(progress, COPY_FILE_PROGRESS_DESC, 0);
            if (destinationFile.isDirectory()) {
                return copyFileToFolder(username, sourceFile, destinationPath, nameCollisionResolutionBehavior,
                        progress);
            } else {
                return copyFileToFile(username, sourceFile, destinationPath, nameCollisionResolutionBehavior, progress);
            }
        }
    }

    /**
     * Copy the source folder to the destination folder.
     * 
     * @param username information used to authenticate the request.
     * @param sourceFolder the source folder to copy
     * @param destinationPath the folder to copy the source folder to
     * @param nameCollisionResolutionBehavior the resolution to occur if there is a naming conflict
     *        while copying the file or folder.
     * @param progress the progress indicator for the copy operation. Can be null.
     * @return String the folder name including path of the copied folder location
     */
    @SuppressWarnings("fallthrough")
    private String copyFolderToFolder(String username, File sourceFolder, String destinationPath,
            NameCollisionResolutionBehavior nameCollisionResolutionBehavior, ProgressIndicator progress) {
        	FileTreeModel destinationModel = FileTreeModel.createFromRawPath(destinationPath);
        if (!sourceFolder.isDirectory()) {
            throw new IllegalArgumentException("The source file must be referencing a directory.");
        }
        	
        String targetFilename = sourceFolder.getName();

        File destinationParentFolder = new File(
                getWorkspaceFolderProxy(username).getFileId() + File.separator + destinationPath);
            File destinationFolder = null;
             
             updateProgress(progress, null, 30);
             
             if(!destinationParentFolder.exists()){
                 //there was a name conflict, and the user is creating a new directory

            destinationParentFolder = new File(getWorkspaceFolderProxy(username).getFileId() + File.separator
                    + destinationModel.getParentTreeModel().getRelativePathFromRoot());
            destinationFolder = new File(destinationParentFolder.getAbsolutePath() + File.separator
                    + destinationModel.getFileOrDirectoryName());
             } else {
            destinationFolder = new File(destinationParentFolder.getAbsolutePath() + File.separator + targetFilename);
             }
             
             updateProgress(progress, "Copying folder...", 50);
            
             //does the destination folder already have the source folder in it?
             if(!destinationFolder.exists()){
                 try{
                FileUtils.copyDirectory(sourceFolder, destinationFolder);
                     updateProgress(progress, null, 90);
                 }catch(IOException e){
                throw new DetailedException("Failed to copy the folder '" + sourceFolder + "'.",
                        "Failed to copy the folder as a new folder in '" + destinationFolder + "' because "
                                + e.getMessage() + ".",
                        e);
                 }
                 
             } else {
                 
            switch (nameCollisionResolutionBehavior) {
            case GUARANTEE_UNIQUE_NAME:
                targetFilename = generateUniqueName(
                        destinationModel.getRelativePathFromRoot() + File.separator + targetFilename, username, true);
                destinationFolder = new File(
                        destinationParentFolder.getAbsolutePath() + File.separator + targetFilename);
                //intentional drop-through to overwrite (performs copy)
            case OVERWRITE:
                     try{
                    FileUtils.copyDirectory(sourceFolder, destinationFolder);
                         updateProgress(progress, null, 90);
                     }catch(IOException e){
                    throw new DetailedException("Failed to copy the folder '" + sourceFolder + "'.",
                            "Failed to copy and merge the folder into the existing folder of '" + destinationFolder
                                    + "' because " + e.getMessage() + ".",
                            e);
                     }
                break;
            case FAIL_ON_COLLISION:
                //intentional drop-through to default
            default:
                throw new FileExistsException(destinationPath + "/" + targetFilename,
                        "A folder named '" + targetFilename + "' already exists in " + destinationPath + "; therefore, "
                                + sourceFolder.getAbsolutePath() + " could not be copied to that location.",
                        "A folder named '" + targetFilename + "' was detected in the destination folder " + "'"
                                + destinationPath + "' during a copy operation in which overwriting was disabled. "
                                + "Since overwriting was disabled, the source folder '" + sourceFolder.getAbsoluteFile()
                                + "' was not copied to its destination.",
                             null);
                 }
             }
             
             //get the course folder
             File courseFolder;
             try{
                 courseFolder = ((DesktopFolderProxy) getCourseFolder(destinationPath, username)).getFolder();
             }catch(@SuppressWarnings("unused") Exception e){
                 
            // When copying a course, we may potentially copy one of the course's subfolders before
            // its course.xml is copied over, in which case
            // the call to get the course folder will fail since the folder doesn't actually have a
            // course.xml yet. To deal with this, we
            // use the subfolder's parent instead, since we just need to update the last modified
            // date.
                 courseFolder = destinationParentFolder;
             }
                 
             //update course folder date modified
        // Note: we have to explicitly update the course folder date modified because it is not
        // guaranteed
             //      that the file system will update it in all file operations for any nesting of files.
             courseFolder.setLastModified(System.currentTimeMillis());
             
             updateProgress(progress, null, 100);
        return destinationPath + File.separator + targetFilename;
    }
        	
    /**
     * Copy the source file to the destination folder.
     * 
     * @param username information used to authenticate the request.
     * @param sourceFile the source file to copy
     * @param destinationPath the folder to copy the source file to
     * @param nameCollisionResolutionBehavior the resolution to occur if there is a naming conflict
     *        while copying the file or folder.
     * @param progress the progress indicator for the copy operation. Can be null.
     * @return String the file name including path of the copied file location
     */
    private String copyFileToFolder(String username, File sourceFile, String destinationPath,
            NameCollisionResolutionBehavior nameCollisionResolutionBehavior, ProgressIndicator progress) {
        	File destinationFile = new File(getWorkspaceFolderProxy(username).getFileId() + File.separator + destinationPath);
        if (sourceFile.isDirectory()) {
            throw new IllegalArgumentException("The source file must be referencing a single file.");
        }
            
        String targetFilename = sourceFile.getName();
                
                File possibleConflictFile = new File(destinationFile, sourceFile.getName());

                //check if the source is already in the destination, if so don't copy the file
                if(!possibleConflictFile.exists()){     
                    
                    try{
                        FileUtils.copyFileToDirectory(sourceFile, destinationFile);
                        updateProgress(progress, COPY_FILE_PROGRESS_DESC, 90);
                        
                    }catch(IOException e){
                        throw new DetailedException("Failed to copy the file '"+sourceFile+"'.", 
                        "Failed to copy the file as a new file in '" + destinationFile + "' because " + e.getMessage()
                                + ".",
                        e);
                    }
                
                }else {
                        try{
                switch (nameCollisionResolutionBehavior) {
                case GUARANTEE_UNIQUE_NAME:
                    targetFilename = generateUniqueName(destinationPath + File.separator + targetFilename, username,
                            false);
                    destinationFile = new File(destinationFile, targetFilename);
                    FileUtils.copyFile(sourceFile, destinationFile);
                    updateProgress(progress, COPY_FILE_PROGRESS_DESC, 90);
                    break;
                case OVERWRITE:
                            FileUtils.copyFileToDirectory(sourceFile, destinationFile);
                            updateProgress(progress, COPY_FILE_PROGRESS_DESC, 90);
                    break;
                case FAIL_ON_COLLISION:
                    //intentional drop-through to default
                default:
                    /* this will not be caught by the try/catch */
                    throw new FileExistsException(destinationPath + "/" + targetFilename,
                            "A file named '" + targetFilename + "' already exists in " + destinationPath
                                    + "; therefore, " + sourceFile.getAbsolutePath()
                                    + " could not be copied to that location.",
                            "A file named '" + targetFilename + "' was detected in the destination folder " + "'"
                                    + destinationPath + "' during a copy operation in which overwriting was disabled. "
                                    + "Since overwriting was disabled, the source file '" + sourceFile.getAbsolutePath()
                                    + "' was not copied to its destination.",
                            null);
                }
                        }catch(IOException e){
                            throw new DetailedException("Failed to copy the file '"+sourceFile+"'.", 
                        "Failed to copy the file as an new version of that file in '" + destinationFile + "' because "
                                + e.getMessage() + ".",
                        e);
                        }
                    }
                
                //get the course folder
                File courseFolder;
                try{
                    courseFolder = ((DesktopFolderProxy) getCourseFolder(destinationPath, username)).getFolder();
                }catch(@SuppressWarnings("unused") Exception e){
                    
            // When copying a course, we may potentially copy one of the course's files before its
            // course.xml is copied over, in which case
            // the call to get the course folder will fail since the folder doesn't actually have a
            // course.xml yet. To deal with this, we
                    // use the file's parent instead, since we just need to update the last modified date.
                    courseFolder = destinationFile;
                }
                    
                //update course folder date modified
        // Note: we have to explicitly update the course folder date modified because it is not
        // guaranteed
                //      that the file system will update it in all file operations for any nesting of files.
                courseFolder.setLastModified(System.currentTimeMillis());
                
                //build file tree from parent folder the source file was copied to
                updateProgress(progress, null, 100);
        return destinationPath + Constants.FORWARD_SLASH + targetFilename;
    }
                
    /**
     * Copy the source file to the destination file.
     * 
     * @param username information used to authenticate the request.
     * @param sourceFile the source file to copy
     * @param destinationPath the folder to copy the source file to
     * @param nameCollisionResolutionBehavior the resolution to occur if there is a naming conflict
     *        while copying the file or folder.
     * @param progress the progress indicator for the copy operation. Can be null.
     * @return String the file name including path of the copied file location
     */
    @SuppressWarnings("fallthrough")
    private String copyFileToFile(String username, File sourceFile, String destinationPath,
            NameCollisionResolutionBehavior nameCollisionResolutionBehavior, ProgressIndicator progress) {
        FileTreeModel destinationModel = FileTreeModel.createFromRawPath(destinationPath);
        if (sourceFile.isDirectory()) {
            throw new IllegalArgumentException("The source file must be referencing a single file.");
        } else if (destinationModel.isDirectory()) {
            throw new IllegalArgumentException("The destination file must be referencing a single file.");
        }
                
        String targetFilename = destinationModel.getFileOrDirectoryName();

        File destinationFile = new File(
                getWorkspaceFolderProxy(username).getFileId() + File.separator + destinationModel.getRelativePathFromRoot());
                if(!destinationFile.exists()){
                    
                    try{
                        FileUtils.copyFile(sourceFile, destinationFile);  
                        updateProgress(progress, COPY_FILE_PROGRESS_DESC, 90);
                    }catch(IOException e){
                        throw new DetailedException("Failed to copy the file '"+sourceFile+"'.", 
                        "Failed to copy the file as a new file named '" + destinationFile + "' because "
                                + e.getMessage() + ".",
                        e);
                    }
                
                } else {
                    
            switch (nameCollisionResolutionBehavior) {
            case GUARANTEE_UNIQUE_NAME:
                targetFilename = generateUniqueName(destinationFile.getAbsolutePath(), username, false);
                destinationModel.setFileOrDirectoryName(targetFilename);
                destinationFile = new File(getWorkspaceFolderProxy(username).getFileId() + File.separator
                        + destinationModel.getRelativePathFromRoot());
                // intentional drop-through to overwrite (performs copy)
            case OVERWRITE:
                        try{
                            FileUtils.copyFile(sourceFile, destinationFile);
                            updateProgress(progress, COPY_FILE_PROGRESS_DESC, 90);
                        }catch(IOException e){
                            throw new DetailedException("Failed to copy the file '"+sourceFile+"'.", 
                            "Failed to copy the file as an new version of that file named '" + destinationFile
                                    + "' because " + e.getMessage() + ".",
                            e);
                        }
                break;
            case FAIL_ON_COLLISION:
                // intentional drop-through to default
            default:
                throw new FileExistsException(destinationPath,
                        "A file already exists at " + destinationPath + "; therefore, " + sourceFile.getAbsolutePath()
                                + " could not be copied to " + "that location.",
                        "A file was detected at the destination '" + destinationPath
                                + "' during a copy operation in which overwriting was "
                                + "disabled. Since overwriting was disabled, the source file '"
                                + sourceFile.getAbsolutePath() + "' was not copied to " + "its destination.",
                                null);
                    }
                }
                
                //get the course folder
                File courseFolder;
                try{
                    courseFolder = ((DesktopFolderProxy) getCourseFolder(destinationPath, username)).getFolder();
                }catch(@SuppressWarnings("unused") Exception e){
                    
            // When copying a course, we may potentially copy one of the course's files before its
            // course.xml is copied over, in which case
            // the call to get the course folder will fail since the folder doesn't actually have a
            // course.xml yet. To deal with this, we
                    // use the file's parent instead, since we just need to update the last modified date.
                    if(destinationFile.getParentFile() != null){
                        courseFolder = destinationFile.getParentFile();
                        
                    } else {
                        courseFolder = destinationFile;
                    }
                }
                    
                //update course folder date modified
        // Note: we have to explicitly update the course folder date modified because it is not
        // guaranteed
                //      that the file system will update it in all file operations for any nesting of files.
                courseFolder.setLastModified(System.currentTimeMillis());
                
                updateProgress(progress, null, 100);
        return destinationModel.getRelativePathFromRoot();
		
	}

    @Override
    public void updateCourseUserPermissions(String username, Set<DomainOptionPermissions> permissions, DomainOption courseData, 
            String userSessionId, ProgressIndicator progressIndicator)
            throws IllegalArgumentException, DetailedException {
        // do nothing. Desktop Files do not support permission sharing.
        
        if(progressIndicator != null) {
            progressIndicator.setPercentComplete(100);
        }
    }
}
