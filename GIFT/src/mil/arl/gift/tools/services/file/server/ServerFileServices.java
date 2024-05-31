/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services.file.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.json.simple.JSONObject;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
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
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DownloadableFileRef;
import mil.arl.gift.common.io.ExportProperties;
import mil.arl.gift.common.io.ExternalFileSystemInterface;
import mil.arl.gift.common.io.FileExistsException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileProxyPermissions;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.ProhibitedUserException;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.io.Version;
import mil.arl.gift.common.io.ZipUtils;
import mil.arl.gift.common.metadata.MetadataSearchResult;
import mil.arl.gift.common.metadata.MetadataSearchResult.QuadrantResultSet;
import mil.arl.gift.common.metadata.MetadataWrapper.ContentTypeEnum;
import mil.arl.gift.common.metadata.MetadataWrapper;
import mil.arl.gift.common.metadata.QuadrantRequest;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.domain.DomainCourseFileHandler.CourseOptionsWrapper;
import mil.arl.gift.domain.knowledge.metadata.MetadataFileFinder;
import mil.arl.gift.domain.knowledge.metadata.MetadataFileSearchResult;
import mil.arl.gift.domain.knowledge.metadata.MetadataSchemaHandler;
import mil.arl.gift.domain.knowledge.metadata.MetadataSearchCriteria;
import mil.arl.gift.net.api.message.codec.json.survey.QuestionJSON;
import mil.arl.gift.net.api.message.codec.json.survey.SurveyContextJSON;
import mil.arl.gift.net.nuxeo.DocumentExistsException;
import mil.arl.gift.net.nuxeo.NuxeoInterface;
import mil.arl.gift.net.nuxeo.NuxeoInterface.DocumentEntityType;
import mil.arl.gift.net.nuxeo.NuxeoInterface.NuxeoDocumentConnection;
import mil.arl.gift.net.nuxeo.QuotaExceededException;
import mil.arl.gift.net.nuxeo.WritePermissionException;
import mil.arl.gift.tools.authoring.common.conversion.AbstractConversionWizardUtil;
import mil.arl.gift.tools.authoring.common.conversion.AbstractLegacySchemaHandler;
import mil.arl.gift.tools.authoring.common.conversion.UnsupportedVersionException;
import mil.arl.gift.tools.authoring.common.util.DomainKnowledgeUtil;
import mil.arl.gift.tools.export.ExportCourseUtil;
import mil.arl.gift.tools.importer.ImportCourseUtil;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import mil.arl.gift.tools.services.file.FileLockManager;
import mil.arl.gift.tools.services.file.FileServices;

/**
 * This class contains GIFT file system services for server deployment.  Server deployment files
 * are stored in a content management system versus on the native OS file system (e.g. Windows).
 * 
 * @author mhoffman
 *
 */
public class ServerFileServices extends AbstractFileServices{
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ServerFileServices.class);
    
    /** The Nuxeo interface instance */
    private static final NuxeoInterface nuxeoInterface = FileServices.nuxeoInterface;

    /**
     * the folder where training application course objects not associated with courses are located
     * (normally /default-domain/Workspaces/Public/TrainingAppsLib/)
     */
    private static Document trainingAppsLibFolderDocument = null;
    
    /**
     * Return the training apps lib directory for this GIFT instance. This directory contains
     * training applications.
     * 
     * @param username information used to authenticate the request.
     * @return the folder proxy instance
     */
    private ServerFolderProxy getTrainingAppsLibraryFolder(String username) {

        if (trainingAppsLibFolderDocument == null) {
            try {
                trainingAppsLibFolderDocument = nuxeoInterface.getTrainingAppsLibFolder(username);
            } catch (IOException ioe) {
                throw new DetailedException("Unable to find the training apps lib folder.",
                        "The training apps lib folder doesn't exist.", ioe);
            }
        }

        return new ServerFolderProxy(trainingAppsLibFolderDocument, username, nuxeoInterface);
    }

    @Override
    public void getCourses(String username,
            CourseOptionsWrapper courseOptionsWrapper, CourseListFilter courseListFilter, 
            boolean validateLogic, ProgressIndicator progressIndicator)
            throws IllegalArgumentException, FileNotFoundException,
            DetailedException, ProhibitedUserException {
        
        boolean includeAll = courseListFilter == null || courseListFilter.getCourseSourceOptions().isEmpty();
        boolean includeShowcase = false, includeUser = false, includeShared = false;
        if(!includeAll && courseListFilter != null) {
            includeShowcase = courseListFilter
                    .getCourseSourceOptions()
                    .contains(CourseSourceOption.SHOWCASE_COURSES);
            includeUser = courseListFilter
                    .getCourseSourceOptions()
                    .contains(CourseSourceOption.MY_COURSES);
            includeShared = courseListFilter
                    .getCourseSourceOptions()
                    .contains(CourseSourceOption.SHARED_COURSES);
        }
        
        Document rootFolderDocument = getUserRootFolder(username);
        ServerFolderProxy rootFolderProxy = new ServerFolderProxy(rootFolderDocument, username, nuxeoInterface);
        
        try{
            if (includeAll) {
                
                ProgressIndicator subtaskProgressIndicator = null;
                if(progressIndicator != null){
                    progressIndicator.setTaskDescription(ALL_COURSES_PROGRESS_DESC);
                    subtaskProgressIndicator = new ProgressIndicator();
                    progressIndicator.setSubtaskProgressIndicator(subtaskProgressIndicator);
                }
                DomainCourseFileHandler.getAllCourses(courseOptionsWrapper, rootFolderProxy, rootFolderProxy, null,
                        validateLogic, true, username, subtaskProgressIndicator);
                
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
                    
                    Document showcaseFolderDocument = nuxeoInterface.getPublicFolder(username);
                    ServerFolderProxy showcaseFolderProxy = new ServerFolderProxy(showcaseFolderDocument, username,
                            nuxeoInterface);
                    DomainCourseFileHandler.getAllCourses(courseOptionsWrapper, rootFolderProxy, showcaseFolderProxy,
                            null, validateLogic, true, username, subtaskProgressIndicator);
                    
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
                    
                    Document userFolderDocument = nuxeoInterface.getUserPrivateFolder(username);
                    ServerFolderProxy userFolderProxy = new ServerFolderProxy(userFolderDocument, username,
                            nuxeoInterface);
                    DomainCourseFileHandler.getAllCourses(courseOptionsWrapper, rootFolderProxy, userFolderProxy, null,
                            validateLogic, true, username, subtaskProgressIndicator);
                    
                }

                if(progressIndicator != null){
                    progressIndicator.setPercentComplete(YOUR_COURSES_PERCENT_COMPLETE);
                }

                if (includeShared) {
                    
                    ProgressIndicator subtaskProgressIndicator = null;
                    if(progressIndicator != null){
                        progressIndicator.setTaskDescription(SHARED_COURSES_PROGRESS_DESC);
                        subtaskProgressIndicator = new ProgressIndicator();
                        progressIndicator.setSubtaskProgressIndicator(subtaskProgressIndicator);
                    }
                    
                    Iterable<String> pathsToExclude = Arrays.asList(NuxeoInterface.DEFAULT_WORKSPACE_ROOT + username,
                            NuxeoInterface.DEFAULT_WORKSPACE_ROOT + PUBLIC_WORKSPACE_FOLDER_NAME);
                    DomainCourseFileHandler.getAllCourses(courseOptionsWrapper, rootFolderProxy, rootFolderProxy,
                            pathsToExclude, validateLogic, true, username, subtaskProgressIndicator);
                }
                
                if(progressIndicator != null){
                    progressIndicator.setPercentComplete(COURSES_PERCENT_COMPLETE);
                }
            }
            
            completeDomainOptionInfo(username, courseOptionsWrapper);
                
        } catch (Exception e) {
            throw new DetailedException("Failed to retrieve all courses in the user's root folder.",
                    "An exception was thrown while retrieving all courses of " + e.getMessage(), e);
        }
    
    }    
    
    @Override
    public void getCourse(String username, String courseId,
            CourseOptionsWrapper courseOptionsWrapper, boolean validateLogic,
            boolean validateSurveyReferences, boolean failOnFirstSchemaError, ProgressIndicator progressIndicator)
            throws IllegalArgumentException, FileNotFoundException,
            DetailedException, ProhibitedUserException {
        
        Document rootFolderDocument = getUserRootFolder(username);
        
        ServerFolderProxy rootFolderProxy = new ServerFolderProxy(rootFolderDocument, username, nuxeoInterface);
        DomainCourseFileHandler.getCourse(courseId, courseOptionsWrapper, rootFolderProxy, 
                validateLogic, failOnFirstSchemaError, username, progressIndicator);       
    }
    
    @Override
    public Date getCourseFolderLastModified(String courseFolderPath, String username) throws IOException{
        return nuxeoInterface.getLastModified(courseFolderPath, username);
    }
    
    /**
     * Copy the server located course folder into the desktop destination folder where it will be executed upon.
     * 
     * @param username used for authentication in retrieving the course folder from the server
     * @param courseId unique id of the course
     * @param courseOptionsWrapper contains information about all courses known to this user
     * @param progressIndicator used to indicate progress in copying the course folder to the destination folder. Can't be null.
     * @param destinationFolder where to place the course folder
     * @param returnCourseFolderId whether or not to return the course folder path relative to the
     *        destination folder. If false the course id (i.e. path) relative to the destination
     *        folder is returned.
     * @param checkCourseVersion true to check the course file's version and attempt to upconvert the
     *        course files if it is not the latest schema version. This can be a pricey operation so
     *        only enable this in certain cases (e.g. LTI). Note: experiment courses and dashboard
     *        courses have already been upconverted by this point.
     * @return the path to the course folder now in the destination folder
     */
    private String copyCourseFolder(String username, String courseId,
            CourseOptionsWrapper courseOptionsWrapper,
            ProgressIndicator progressIndicator, File destinationFolder, boolean returnCourseFolderId, boolean checkCourseVersion){
        
        //
        // 1) get the course folder on the server
        //
        
        //start with the user's root folder (/default-domain/workspaces/)
        Document rootFolderDocument = getUserRootFolder(username);
        
        ServerFolderProxy rootFolderProxy = new ServerFolderProxy(rootFolderDocument, username, nuxeoInterface);
        try{
            // Get only the specific folder to copy.
            progressIndicator.setTaskDescription(VALIDATE_COURSE_PROGRESS_DESC);
            DomainCourseFileHandler.getCourse(courseId, courseOptionsWrapper, rootFolderProxy, false, true, username, progressIndicator);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve all courses in the user's root folder.", 
                    "An exception was thrown while retrieving all courses of "+e.getMessage(), e);
        }
        
        //check if the user canceled the course load operation
        if(progressIndicator.shouldCancel()){
            return null;
        }
        
        //retrieve the course file handler to get it's course folder reference
        DomainCourseFileHandler handler = courseOptionsWrapper.courseFileNameToHandler.get(courseId);
        if(handler == null){
            throw new DetailedException("Failed to retrieve the course.", 
                    "Could not find the course file handler for '"+courseId+"'.", null);
        }
        ServerFolderProxy courseFolder = (ServerFolderProxy) handler.getCourseFolder();
        
        //update label shown to user
        progressIndicator.setTaskDescription("Loading");
        
        //
        // 2) download the course export as a zip created by Nuxeo
        //
        
        File destinationZip = new File(destinationFolder + File.separator + courseId + ".zip");
        progressIndicator.updateSubtaskDescription("Exporting course folder");
        
        //used to update the progress indicator based on export progress
        Thread monitor = new Thread("Export monitor - "+username+":"+courseId){
            
            @Override
            public void run(){
               
                ProgressIndicator subProgressIndicator = progressIndicator.getSubtaskProcessIndicator();
                while(subProgressIndicator.getPercentComplete() < 100){
                    
                    progressIndicator.setPercentComplete(subProgressIndicator.getPercentComplete() / 2);
                    try {
                        sleep(100);  //just picked 100 ms
                    } catch (@SuppressWarnings("unused") InterruptedException e) {
                        //don't care
                    }
                }
            }
        };
        monitor.start();
        
        //download the export zip locally
        try{
            if (checkCourseVersion) {
                // look for a course.xml file in the course directory
                List<FileProxy> files = courseFolder.listFiles(null, AbstractSchemaHandler.COURSE_FILE_EXTENSION);
                if (files != null && !files.isEmpty()) {
                    AbstractConversionWizardUtil.updateCourseToLatestVersion(files.get(0), courseFolder, true);
                }
            }
            nuxeoInterface.exportFolder(courseFolder.getFolder().getId(), destinationZip, username, progressIndicator.getSubtaskProcessIndicator());
        }catch(Throwable t){
            throw new DetailedException("There was a problem getting the course from the server.", 
                    "An error was thrown by the Nuxeo interface when trying to export and download the course to the GIFT runtime location: "+t.getMessage(), t);
        }finally{
            //stop the monitor
            progressIndicator.setSubtaskProgress(100);
        }
        
        progressIndicator.setPercentComplete(50);
        progressIndicator.setSubtaskProgressIndicator(new ProgressIndicator()); //set new in order to reset to 0 in case the monitor thread is still running (we don't want to join and wait)
        
        if(progressIndicator.shouldCancel()){
            destinationZip.delete();  //cleanup local downloaded zip that is no longer wanted
            return null;
        }
        
        //
        // 3) unzip the course export zip
        //
        progressIndicator.updateSubtaskDescription("Unzipping course folder");
        
        //used to update the progress indicator based on unzip progress
        monitor = new Thread("Unzip monitor - "+username+":"+courseId){
            
            @Override
            public void run(){
               
                ProgressIndicator subProgressIndicator = progressIndicator.getSubtaskProcessIndicator();
                while(subProgressIndicator.getPercentComplete() < 100){
                    
                    progressIndicator.increasePercentComplete(subProgressIndicator.getPercentComplete() / 2);
                    try {
                        sleep(100); //just picked 100 ms
                    } catch (@SuppressWarnings("unused") InterruptedException e) {
                        //don't care
                    }
                }
            }
        };
        monitor.start();
        
        try{
            ZipUtils.unzipArchive(destinationZip, destinationFolder, progressIndicator.getSubtaskProcessIndicator());
        }catch(Throwable t){
            throw new DetailedException("There was a problem downloading the course  from the server.", 
                    "An error was thrown when trying to unzip the archive containing the course downloaded from Nuxeo: "+t.getMessage(), t);
        }finally{
            //stop the monitor
            progressIndicator.setSubtaskProgress(100);
        }
        
        progressIndicator.setTaskDescription("Loaded");
        progressIndicator.setPercentComplete(100);        
        
        if(progressIndicator.shouldCancel()){
            //cleanup unwanted files created locally to this point
            try{
                destinationZip.delete();
                FileUtil.delete(destinationFolder);
            }catch(IOException e){
                logger.error("Failed to delete destination zip file of '"+destinationZip+"' and destination folder of '"+destinationFolder+"'.", e);
            }
            return null;
        }
        
        //return the new domain id which is the path to the course.xml file in it's runtime folder
        File newCourseXML = new File(destinationFolder.getPath() + File.separator + courseFolder.getName() + File.separator + courseOptionsWrapper.courseFileNameToFileMap.get(courseId).getName());
        if(returnCourseFolderId){
            return FileFinderUtil.getRelativePath(destinationFolder.getParentFile(), newCourseXML.getParentFile());
        }else{
            return FileFinderUtil.getRelativePath(COURSE_RUNTIME_DIR, newCourseXML);
        }
    }

    @Override
    public String loadExperiment(String username, String experimentId, String sourceCourseId,
            CourseOptionsWrapper courseOptionsWrapper, final ProgressIndicator progressIndicator)
            throws IllegalArgumentException, DetailedException, URISyntaxException {

        final String subFolder = experimentId + File.separator + UUID.randomUUID();
        File experimentFolder = new File(RUNTIME_EXPERIMENT_DIR, subFolder);

        /* return just the copied folder */
        boolean returnCourseFolderId = true;
        /* experiments have already been converted by this point; do not allow
         * conversion here */
        boolean checkCourseVersion = false;

        /* Prepend the experiment id so that the returned value is relative to
         * the runtime experiments folder */
        return experimentId + File.separator + copyCourseFolder(username, sourceCourseId, courseOptionsWrapper,
                progressIndicator, experimentFolder, returnCourseFolderId, checkCourseVersion);
    }

    @Override
    public String loadCourse(String username, String courseId,
            CourseOptionsWrapper courseOptionsWrapper,
            ProgressIndicator progressIndicator, String runtimeRootFolderName)
            throws IllegalArgumentException, DetailedException, URISyntaxException {
        
        File destinationFolder = getCourseRuntimeFolder(runtimeRootFolderName);  

        // want to return the new course xml
        boolean returnCourseFolderId = false;
        // regular courses have already been converted by this point; do not allow conversion here
        boolean checkCourseVersion = false;

        return copyCourseFolder(username, courseId, courseOptionsWrapper, progressIndicator, destinationFolder,
                returnCourseFolderId, checkCourseVersion);
    }

    @Override
    public String loadLTICourse(String username, String courseId, CourseOptionsWrapper courseOptionsWrapper,
            ProgressIndicator progressIndicator, String runtimeRootFolderName)
            throws IllegalArgumentException, DetailedException, URISyntaxException {

        File destinationFolder = getCourseRuntimeFolder(runtimeRootFolderName);

        // want to return the new course xml
        boolean returnCourseFolderId = false;
        // LTI courses may not be the latest schema; allow conversion if they are out of date
        boolean checkCourseVersion = true;

        return copyCourseFolder(username, courseId, courseOptionsWrapper, progressIndicator, destinationFolder,
                returnCourseFolderId, checkCourseVersion);
    }

    @Override
    public FileTreeModel getUsersWorkspace(String username) throws DetailedException {
        
        Document rootFolderDocument = getUserRootFolder(username);
        try{
            Document usersWorkspaceDocument = nuxeoInterface.getFolderByName(rootFolderDocument.getPath(), username, username);
            return getFileTree(username, usersWorkspaceDocument);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve the user's workspace folder.", 
                    "There was an error when trying to retrieve '"+username+"' workspace folder: "+e.getMessage()+".", e);
        }
    }    

    @Override
    public MetadataSearchResult getMetadataContentFileTree(String username,
            String courseFolderPath, Map<MerrillQuadrantEnum, QuadrantRequest> quadrantToRequest) throws DetailedException, URISyntaxException {
        
        //
        // build the metadata search information to use for each quadrant
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
        AbstractFolderProxy courseFolderProxy = null;
        try {
            Document courseFolderDocument = nuxeoInterface.getDocumentByName(targetCourseFolderPath, username);
            courseFolderProxy = new ServerFolderProxy(courseFolderDocument, username, nuxeoInterface);
            
        } catch (IOException e) {
            throw new DetailedException("Failed to retrieve the course folder.", 
                    "There was an error when trying to retrieve the course folder at '"+targetCourseFolderPath +"': "+e.getMessage()+".", e);
        }
        
        //get the metadata under the course folder for the search criteria
        Map<MerrillQuadrantEnum, MetadataFileSearchResult> fileSearchResultsMap;
        try{
            fileSearchResultsMap = MetadataFileFinder.findFiles(courseFolderProxy, quadrantSearchCriteria);
        }catch(IOException e){
            throw new DetailedException("Failed to find all metadata files in the course folder.", 
                    "An error was thrown while searching '"+courseFolderProxy.getFileId()+"' for metadata files: "+e.getMessage()+".", e);
        }
        
        Document rootFolderDocument = getUserRootFolder(username);
        
        ServerFolderProxy rootFolderProxy = new ServerFolderProxy(rootFolderDocument, username, nuxeoInterface);
        
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
                    metadataFilename = rootFolderProxy.getRelativeFileName(metadataFileProxy);
                }catch(Exception e){
                    throw new DetailedException("Failed to convert the metadata file name into a course folder relative file name.", 
                            "An error was thrown while creating a course folder relative file name for '"+metadataFileProxy.getFileId()+"'.\n\nThe error reads:\n"+e.getMessage()+".", e);
                }
                
                generated.metadata.Metadata metadata = quadrantFiles.get(metadataFileProxy);
                String displayName = MetadataSchemaHandler.getDisplayName(metadata);
                if(displayName == null){
                    //give it something
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


    @Override
    public FileTreeModel getRootTree(String username) throws DetailedException {        
        
        Document rootFolderDocument = getUserRootFolder(username);
        return getFileTree(username, rootFolderDocument);
    }
    
    @Override
    public FileTreeModel getFileTree(String username, FileType fileType) throws IllegalArgumentException, DetailedException{
        
        Document rootFolderDocument = getUserRootFolder(username);
        return getFileTree(username, rootFolderDocument, AbstractSchemaHandler.getFileExtension(fileType));
    }

    @Override
    public FileTreeModel getFileTree(String username, String... extensions) throws IllegalArgumentException, DetailedException { 
        
        Document rootFolderDocument = getUserRootFolder(username);
        return getFileTree(username, rootFolderDocument, extensions);
    }
    
    /**
     * Return the File tree containing all descendant directories as well as files within those
     * directories that match the extension of the file extension(s) specified.
     * 
     * @param username used to retrieve the documents this user has read access too
     * @param rootFolderDocument the root folder to look for documents in all descendant folders. Can't be null.
     * @param extensions file extensions (e.g. ".course.xml") to filter on.  If empty, all files will be included in the returned model
     * @return a file tree model representative of the file provided filtering on the file extension(s) specified (if any)
     * @throws IllegalArgumentException if a parameter specified is not valid
     * @throws DetailedException if there was a problem accessing the documents for the user or building the tree
     */
    private FileTreeModel getFileTree(String username, Document rootFolderDocument, String... extensions) 
            throws IllegalArgumentException, DetailedException{
        
        if(rootFolderDocument == null){
            throw new IllegalArgumentException("The root folder document can't be null.");
        }
        
        Documents documents = new Documents();
        
        //
        // Grab all files (not folders)
        //
        try{
            if(extensions != null && extensions.length > 0){
                //need to filter by extension(s)
            
                for(String extension : extensions){
                    Documents documentsForExtension = nuxeoInterface.getDocumentsAndFoldersByPath(rootFolderDocument.getPath(), extension, username);
                    
                    for(Document document : documentsForExtension){
                        documents.add(document);
                    }
                }
                
            }else{            
                documents = nuxeoInterface.getDocumentsAndFoldersByPath(rootFolderDocument.getPath(), null, username);
            }
        }catch(IOException e){
            throw new DetailedException("Failed to build a file tree.", 
                    "Failed to find the descendant files and folders under '"+rootFolderDocument+" : "+e.getMessage(), e);
        }
        
        //
        // Build tree
        //
        return getFileTree(username, rootFolderDocument, documents);

    }
    
    /**
     * Return the file tree containing all descendant directories as well as files within those
     * directories that are not GIFT authorable files (e.g. dkf.xml).  This is useful for showing 
     * the files in a file management UI that can't be necessarily managed through GIFT authoring tools.
     * 
     * @param username  used to retrieve the documents this user has read access too
     * @param rootFolderDocument the root folder to look for documents in all descendant folders. Can't be null.
     * @return a file tree model representative of the documents found as ancestors of the root folder and that are NOT GIFT authorable XML files.
     * The root of the tree model will match the root folder provided.  The returned tree root will have ancestor node information removed.
     */
    private FileTreeModel getNonGIFTFilesFileTree(String username, Document rootFolderDocument){
        
        if(rootFolderDocument == null){
            throw new IllegalArgumentException("The root folder document can't be null.");
        }
        
        Documents documents = new Documents();
        
        //
        // Grab all files (not folders)
        //
        try{
                      
            Documents candidateDocuments = nuxeoInterface.getDocumentsAndFoldersByPath(rootFolderDocument.getPath(), null, username);
            
            //post process
            for(Document document : candidateDocuments){
                
                try{
                    AbstractSchemaHandler.getFileType(document.getTitle());
                }catch(@SuppressWarnings("unused") IllegalArgumentException e){
                    //Not a GIFT XML file
                    
                    //is this a slide show image managed by GIFT?
                    String courseFolderRelativeFileName = document.getPath().substring(rootFolderDocument.getPath().length()+1);
                    if(courseFolderRelativeFileName.startsWith(SLIDE_SHOWS_FOLDER_NAME)){
                        continue;
                    }
                    
                    documents.add(document); 
                    continue;
                }
                

            }

        }catch(IOException e){
            throw new DetailedException("Failed to build a file tree.", 
                    "Failed to find the descendant files and folders under '"+rootFolderDocument+" : "+e.getMessage(), e);
        }
        
        //
        // Build tree
        //
        FileTreeModel rootNode = getFileTree(username, rootFolderDocument, documents);
        
        // the root of the tree will be "/default-domain/workspaces/" but need to return
        // the the document folder path as the root without any parent information
        String docPath = rootFolderDocument.getPath();
        docPath = docPath.substring(NuxeoInterface.DEFAULT_WORKSPACE_ROOT.length());
        rootNode = rootNode.getModelFromRelativePath(docPath, false, true); //returns a tree at the document folder level but still with ancestor information
        rootNode.detatchFromParent(); //remove ancestor information
        return rootNode;
    }
    
    /**
     * Return the file tree containing all documents provided, maintaining their folder structure (i.e. file path)
     * to the root folder.
     *  
     * @param username used to retrieve the documents this user has read access too
     * @param rootFolderDocument the root folder to look for documents in all descendant folders. Can't be null.
     * This could folder like "/default-domain/workspaces/" or "/default-domain/workspaces/ssmith/change a tire/".
     * @param documents those documents to include in the file tree.  These have paths starting with "/default-domain/workspaces/".
     * @return a file tree model representative of the documents provided.  The root node will be "workspaces" and is gauranteed
     * to have the folder provided (e.g. root folder of "/default-domain/workspaces/ssmith/change a tire/" will result in a returned
     * tree model of "workspaces/ssmith/change a tire/(any descendants)").
     */
    private FileTreeModel getFileTree(String username, Document rootFolderDocument, Documents documents){
        
        //sort by full file path
        documents.list().sort(new Comparator<Document>() {

            @Override
            public int compare(Document document1, Document document2) {
                return document1.getPath().compareTo(document2.getPath());                
            }
        });
        
        //build the root node of the tree, will not be shown in file paths  
        //Note: the resulting value must start at "workspaces" for the upcoming leaf node logic to work correctly using file path comparisions
        FileTreeModel rootDirectory;
        if(nuxeoInterface.isWorkspace(rootFolderDocument)){
            //if the root folder is the workspace folder ("/default-domain/workspaces/") than the tree model will start at "workspaces"
            rootDirectory = new FileTreeModel(rootFolderDocument.getTitle(), new ArrayList<>());
        }else{
            // need to build the tree model to the "workspace" root node
            // i.e. convert the path "/default-domain/workspaces/ssmith/change a tire/" to the tree 
            // "workspaces/ssmith/change a tire/"
            StringTokenizer pathTokenizer = new StringTokenizer(rootFolderDocument.getPath(), Constants.FORWARD_SLASH);
            
            String value = pathTokenizer.nextToken(); // "default-domain", don't need
            value = pathTokenizer.nextToken(); // "workspaces", do need as root tree node
            rootDirectory = new FileTreeModel(value, new ArrayList<>());
            FileTreeModel nextParent = rootDirectory;
            while(pathTokenizer.hasMoreTokens()){  //start with index 2 which should be a workspace folder (e.g. Public) (index 0 is "default-domain", 1 is "workspaces")
                
                FileTreeModel currentChild = new FileTreeModel(pathTokenizer.nextToken(), new ArrayList<>());
                nextParent.addSubFileOrDirectory(currentChild);
                nextParent = currentChild;
            }
        }
        
        //build the tree path for each document
        for(Document document : documents){
            
            //parse the path keeping the forward slash to know when dealing with a folder
            String path = document.getPath();
            
            if(nuxeoInterface.isFolder(document) || nuxeoInterface.isWorkspace(document)){
                path = path + "/";
            }
            
            populateFileTreeModel(rootDirectory, path);

        }//end for
        
        return rootDirectory;
    }
    
    /**
     * Populates the file tree model specified based on the file name provided.  For example if the model
     * is: "/default-domain/workspaces/" and the file name is "a/b/c/my.course.xml" the resulting file tree will look like:
     * "/default-domain/workspaces/a/b/c/my.course.xml". If a directory in the tree doesn't exist it will be added.
     * 
     * @param rootDirectory the tree model to add too.  Can't be null.  The node must be "workspaces".
     * @param fileNameWithPath the absolute path to a file, including the filename, or a directory when ending with a "/".  Must start with "/default-domain/workspaces/".
     * @return FileTreeModel the leaf file model in the tree that represents the file/directory at the end of the fileNameWithPath value.
     * @throws DetailedException if there was a problem with the file name.
     */
    private FileTreeModel populateFileTreeModel(FileTreeModel rootDirectory, String fileNameWithPath) throws DetailedException{
        
        StringTokenizer tokenizer = new StringTokenizer(fileNameWithPath, Constants.FORWARD_SLASH, true);
        if(tokenizer.countTokens() <= 4){
            //the first 4 tokens should be "default-domains", "/", "workspaces", "/"
            //of which we want to ignore (remove) from file paths shown to user
            throw new DetailedException("Failed to populate the file tree model for a directory.",
                    "Found a document with an incorrect path of '"+fileNameWithPath+"'.  The path must start with '/default-domain/workspaces/'.", null);
        }else if(!tokenizer.nextToken().equals(Constants.FORWARD_SLASH) || !tokenizer.nextToken().equals("default-domain") ||
                !tokenizer.nextToken().equals(Constants.FORWARD_SLASH) || !tokenizer.nextToken().equals("workspaces") || 
                !tokenizer.nextToken().equals(Constants.FORWARD_SLASH)){
            throw new DetailedException("Failed to populate the file tree model for a directory.",
                    "Found a document with an incorrect path of '"+fileNameWithPath+"'.  The path must start with '/default-domain/workspaces/'.", null);
        }
        
        String fileOrDirName, nextToken = null;
        FileTreeModel currentParent = rootDirectory;
        FileTreeModel leaf = null;
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
                    leaf = newChild;
                }else{
                    //the child is a folder
                    FileTreeModel newChild = new FileTreeModel(fileOrDirName, new ArrayList<>());
                    currentParent.addSubFileOrDirectory(newChild);
                    currentParent = newChild;
                    leaf = newChild;
                }
                
            }else{
                //the node does exist, get it and set as next parent
                currentParent = currentParent.getChildByName(fileOrDirName);
                leaf = currentParent;
            }
            
        }//end while
        
        return leaf;
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
        
        boolean writeAccess = hasWritePermissions(username, workspaceRelativePath);
        return FileLockManager.getInstance().lock(workspaceRelativePath, username, browserSessionKey, writeAccess,
                initialAcquisition);
    }
    
    @Override
    public boolean renameFile(String username, String filePath, String newName, boolean updateCourseFolderLastModifiedDate) throws DetailedException {
        
        FileTreeModel file = FileTreeModel.createFromRawPath(filePath);
        String parentPath = file.getParentTreeModel().getRelativePathFromRoot();
        String currentName = file.getFileOrDirectoryName();
        
        try {
            nuxeoInterface.renameDocument(parentPath, currentName, newName, username);
        } catch (DocumentExistsException e) {
            throw new DetailedException("An error occurred while attempting to rename the file. The file '" + newName + "' may already exist", e.getMessage(), e);
        } catch (DocumentException e) {
            throw new DetailedException("An error occurred while attempting to rename the file. The file may not exist.", e.getMessage(), e);
        }
        
        return true;
    }

    @Override
    public boolean deleteFile(String username, String browserSessionKey, String originalFilePath, ProgressIndicator progress, boolean updateCourseFolderLastModifiedDate) throws DetailedException {
        
        updateProgress(progress, "Preparing to delete " + originalFilePath, 0);
        
        String filename = FileTreeModel.correctFilePath(originalFilePath);
        
        FileTreeModel fileModel = FileTreeModel.createFromRawPath(filename);
        
        updateProgress(progress, null, 10);
        
        Document rootFolderDocument = getUserRootFolder(username);
        updateProgress(progress, null, 10);
        
        Document document;
        try{
            if(nuxeoInterface.documentExists(rootFolderDocument.getPath(), filename, true, username)){
                document = nuxeoInterface.getDocumentByName(rootFolderDocument.getPath(), filename, username);
                updateProgress(progress, null, 10);
            }else{
                //file doesn't exist
                updateProgress(progress, null, 100);
                return false;
            }
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve the the document.", 
                    "Failed to retrieve the document '"+filename+"' because "+e.getMessage(),
                    e);
        }
        
        
        //Retrieve the course folder in order to update the last modified date value later on
        DocumentEntityType documentEntityType = null;
        if (updateCourseFolderLastModifiedDate) {
        try{
            ServerFolderProxy courseFolderProxy = (ServerFolderProxy) getCourseFolder(filename, username);
                Document courseFolderDocument = courseFolderProxy.getFolder();
                documentEntityType = courseFolderDocument != null ? new DocumentEntityType(courseFolderDocument) : null;
        }catch(IOException e){
            throw new DetailedException("Failed to delete the file.", 
                        "Failed to retrieve course folder of the destination where the file '" + filename
                                + "' is being deleted because " + e.getMessage(),
                    e);
        }
        }
        
        if(nuxeoInterface.isFolder(document)){
            
            List<FileTreeModel> lockedFiles = new ArrayList<>();
            
            //populate a file tree for the target file, relative to the workspace folder
            FileTreeModel populatedFileModel = getFileTree(username, document);
            
            fileModel = populatedFileModel.getModelFromRelativePath(filename, false);
            
            //get lock for all descendant files         
            updateProgress(progress, "Locking files...", 0);
            
            if(!lockAllFiles(username, browserSessionKey, fileModel, lockedFiles)){
                throw new DetailedException("Failed to acquire the lock on all files before deleting folder.",
                        "All the files under the folder '"+fileModel.getFileOrDirectoryName()+"' must be available before they can be deleted.", null);
            }
            updateProgress(progress, null, 10);
            
            //delete the directory
            try{
                
                updateProgress(progress, "Deleting " + fileModel.getFileOrDirectoryName(), 0);
                nuxeoInterface.deleteWorkspaceFolder(document.getPath(), documentEntityType, username);
                updateProgress(progress, null, 10);
                
            }catch(Exception e){                
                logger.error("Caught exception while trying to delete the directory "+filename, e);
                
                //clean up lock manager
                for(FileTreeModel lockedFile : lockedFiles){
                    unlockFile(username, browserSessionKey, lockedFile.getRelativePathFromRoot());
                }
            
                throw new DetailedException("Failed to delete the directory " + filename, e.getMessage(), e);
            }
            
            //clean up the lock manager
            updateProgress(progress, "Cleaning up...", 0);
            for(FileTreeModel lockedFile : lockedFiles){
                unlockFile(username, browserSessionKey, lockedFile.getRelativePathFromRoot());
            }
            updateProgress(progress, null, 10);
            
            updateProgress(progress, null, 100);
                return true;
            
        }else{
            try{
                updateProgress(progress, "Deleting " + fileModel.getFileOrDirectoryName(), 10);
                nuxeoInterface.deleteDocument(document.getId(), documentEntityType, username);
                updateProgress(progress, null, 100);
            }catch(IOException e){
                throw new DetailedException("Failed to delete the document.", 
                        "There was a problem while attempting to delete '"+document.getPath()+"' : "+e.getMessage(), e);
            }
        }
        
        return true;
    }

    @Override
    public UnmarshalledFile unmarshalFile(String username, String filePath) 
            throws FileNotFoundException, IllegalArgumentException, UnsupportedVersionException, DetailedException {
        
        if(logger.isDebugEnabled()){
            logger.debug("Unmarshalling "+ filePath);
        }
        
        String targetFilePath = FileTreeModel.correctFilePath(filePath);
        
        FileType fileType = AbstractSchemaHandler.getFileType(targetFilePath);
        
        FileProxy fileProxy = getFile(targetFilePath, username);
        return AbstractLegacySchemaHandler.getUnmarshalledFile(fileProxy, fileType);
    }
    
    @Override
    public boolean marshalToFile(String username, Serializable serializableObject,
            String filePath, String version, boolean useParentAsCourse) throws IllegalArgumentException, DetailedException, WritePermissionException {
        
        if(logger.isDebugEnabled()){
            logger.debug("Marshalling to "+filePath);
        }
        
        FileTreeModel file = FileTreeModel.createFromRawPath(filePath);
        
        String filename = file.getFileOrDirectoryName();
        String parentDirectory = file.getParentTreeModel().getRelativePathFromRoot();
        FileType fileType = AbstractSchemaHandler.getFileType(filename); 
        
        Document rootFolderDocument = getUserRootFolder(username);
        String rootFolderPath = rootFolderDocument.getPath() + Constants.FORWARD_SLASH + parentDirectory;
        DocumentEntityType documentEntityType;
        try{
            documentEntityType = nuxeoInterface.getDocumentEntityByName(rootFolderPath, filename, username);
        }catch(IOException e){
            throw new DetailedException("Failed to save the file.", 
                    "Failed to check whether the file '"+filename+"' exists because "+e.getMessage(),
                    e);
        }
        
        //Retrieve the course folder in order to update the last modified date later on
        DocumentEntityType courseFolderDocumentEntityType;
        try{
            String courseFolderName, courseFolderParentPath;
            if (useParentAsCourse || serializableObject instanceof generated.course.Course) {
                //when creating a new course the course.xml won't exist at this point but the GIFT rule is that a course folder
                //contains a course.xml file as a direct decendant so we can assume the parent to the course.xml is the course folder here.
                courseFolderName = file.getParentTreeModel().getFileOrDirectoryName();
                courseFolderParentPath = file.getParentTreeModel().getParentTreeModel().getRelativePathFromRoot();
                courseFolderDocumentEntityType =  nuxeoInterface.getFolderEntityByName(courseFolderParentPath, courseFolderName, username);

                if(courseFolderDocumentEntityType == null){
                    throw new IOException("Failed to find the course folder named '"+courseFolderName+"' for "+username);
                }
            }else{
                ServerFolderProxy courseFolderProxy = (ServerFolderProxy)getCourseFolder(filePath, username);
                courseFolderDocumentEntityType = new DocumentEntityType(courseFolderProxy.getFolder());
            }
        }catch(IOException e){
            throw new DetailedException("Failed to save the file.", 
                    "Failed to retrieve the course folder for the file being saved of '"+filename+"' because "+e.getMessage(),
                    e);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        List<ValidationEvent> validationEvents = null;
        
        //
        //write the contents to a stream
        //
        
        if(fileType == FileType.QUESTION_EXPORT || fileType == FileType.SURVEY_EXPORT){
            //create a survey or question export file
            
            JSONObject obj = new JSONObject();
            try{                
                if (fileType == FileType.QUESTION_EXPORT) {
                QuestionJSON json = new QuestionJSON();
                json.encode(obj, serializableObject);
                } else {
                    SurveyContextJSON json = new SurveyContextJSON();
                    json.encode(obj, serializableObject);
                }
            }catch(Exception e){
                throw new DetailedException("There was a problem creating the survey or question export file '" + filename + "'.",
                        "The Survey or Question could not be encoded due to an exception, there the file '" + rootFolderPath+Constants.FORWARD_SLASH+filename + "' could not be created.  The error message reads: " + e.getMessage(),
                        e);
            }
            
            try{
                outputStream.write(obj.toJSONString().getBytes());
            }catch(IOException e){
                throw new DetailedException("Failed to write the survey or question export file.", 
                        "There was a problem writing '"+rootFolderPath+Constants.FORWARD_SLASH+filename+"' : "+e.getMessage(), e);
            }
          
        }else if (fileType == FileType.XTSP_JSON) {
        	try{
        		if (serializableObject instanceof String) {
        			String output = (String) serializableObject;
        			outputStream.write(output.getBytes());
        		}
            }catch(IOException e){
                throw new DetailedException("Failed to write the xTSP file.", 
                        "There was a problem writing '"+rootFolderPath+Constants.FORWARD_SLASH+filename+"' : "+e.getMessage(), e);
            }
        }else{
        
        
            if(version == null || version.equals(Version.getInstance().getCurrentSchemaVersion())) {
                // this is the current GIFT version
                try{
                    validationEvents = AbstractSchemaHandler.writeToFile(serializableObject, outputStream, fileType, true);
                }catch(SAXException | JAXBException | IOException e){
                    throw new DetailedException("Failed to write the XML file.", 
                            "There was a problem writing '"+rootFolderPath+Constants.FORWARD_SLASH+filename+"' : "+e.getMessage(), e);
                }
            } else {
                // This is a backup file of a previous version of GIFT, retrieve the corresponding schema file and generated class
                try{
                    AbstractConversionWizardUtil cWizard = AbstractConversionWizardUtil.getConversionWizardForVersion(version);         
                    validationEvents = AbstractSchemaHandler.writeToFile(serializableObject, cWizard.getPreviousSchemaRoot(fileType), outputStream,  cWizard.getPreviousSchemaFile(fileType), true);
                }catch(SAXException | JAXBException | IOException e){
                    throw new DetailedException("Failed to write the XML file.", 
                            "There was a problem writing '"+rootFolderPath+Constants.FORWARD_SLASH+filename+"' : "+e.getMessage(), e);
                }
            }
        }
                
        if(documentEntityType == null){
            //create the document, because it doesn't exist, with the contents to marshal
            try{
                String courseDocEntityTypePath = courseFolderDocumentEntityType != null ? courseFolderDocumentEntityType.getPath() : null;
                nuxeoInterface.createDocument(rootFolderPath, filename, courseDocEntityTypePath, new ByteArrayInputStream(outputStream.toByteArray()), username);
            } catch (WritePermissionException e){
                throw e;
                
            } catch(IOException e){
                throw new DetailedException("Failed to create the new XML document.", 
                        "There was a problem creating the document '"+rootFolderPath+Constants.FORWARD_SLASH+filename+"' : "+e.getMessage(), e);
            }
            return validationEvents == null || validationEvents.isEmpty();
        }else{
            //update the nuxeo document's content with the stream
            try{
                nuxeoInterface.updateDocumentFile(documentEntityType, courseFolderDocumentEntityType, new ByteArrayInputStream(outputStream.toByteArray()), username); 
            }catch(IOException e){
                throw new DetailedException("Failed to update the pre-existing XML document.", 
                        "There was a problem updating the document '"+rootFolderPath+Constants.FORWARD_SLASH+filename+"' : "+e.getMessage(), e);
            }
            return validationEvents == null || validationEvents.isEmpty();
        }
    }
    
    /**
     * Delete the folder provided.
     * Note: this will update the course folder last modified date which has implications when it comes to the 
     * frequency of course validation.
     * 
     * @param parentFolder the parent folder to the folder being deleted. Can contain the prefix '/default-domain/workspaces' or 'Workspaces'.
     * @param folderToDelete the folder to delete.  Can contain the prefix '/default-domain/workspaces' or 'Workspaces'.
     * @param username used to authenticate the deletion
     * @throws IOException if there was a problem deleting the folder.
     */
    void deleteFolder(ServerFolderProxy parentFolder, Document folderToDelete, String username) throws IOException{
        
        DocumentEntityType courseFolderType;
        try{
            ServerFolderProxy courseFolderProxy = findAncestorCourseFolder(parentFolder);
            courseFolderType = new DocumentEntityType(courseFolderProxy.getFolder());
            
        }catch(@SuppressWarnings("unused") Exception e){
            
            //if we can't find the ancestor course folder, just skip updating its last modified date
            courseFolderType = null;
        }
        
        nuxeoInterface.deleteFolder(folderToDelete.getId(), courseFolderType, username);
    }
    
    /**
     * Create and return a new Nuxeo document instance for the local desktop file provided. i.e.
     * upload the contents of the file into Nuxeo and return the handle to that entry in nuxeo.
     * 
     * @param sourceParentFolder the pre-existing nuxeo folder the file will be uploaded under, i.e.
     *        the folder where the new document will be created. Can't be null.
     * @param source the local instance of the file. Can't be null.
     * @param filename the desired name for the created document INCLUDING the file extension. If
     *        null or empty, the name of the source will be used. (e.g. 'myCreatedDocument.txt')
     * @param username used for authentication. Can't be null.
     * @return the newly created nuxeo document
     * @throws IOException if there was a problem finding the course folder or creating the document
     *         in Nuxeo
     */
    Document createDocument(ServerFolderProxy sourceParentFolder, File source, String filename, String username) throws IOException{
        
        String courseFolderPath;
        try{
            ServerFolderProxy courseFolder = findAncestorCourseFolder(sourceParentFolder);
            courseFolderPath = courseFolder.getFileId();
            
        }catch(@SuppressWarnings("unused") Exception e){
            
            //if we can't find the ancestor course folder, just skip updating its last modified date
            courseFolderPath = null;
        }
        
        if (StringUtils.isBlank(filename)) {
            filename = source.getName();
        }

        Document document = nuxeoInterface.createDocument(sourceParentFolder.getFileId(), filename, courseFolderPath, new FileInputStream(source), username);

        return document;
    }
    
    @Override
    public String readFileToString(String username, FileTreeModel file) throws IllegalArgumentException, DetailedException{
        
        String filename = file.getRelativePathFromRoot(true);
        
        Document rootFolderDocument = getUserRootFolder(username);
        
        DocumentEntityType documentEntityType;
        try{
            documentEntityType = nuxeoInterface.getDocumentEntityByName(rootFolderDocument.getPath(), filename, username);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve the the document.", 
                    "Failed to retrieve the document '"+filename+"' because "+e.getMessage(),
                    e);
        }
        
        try{
            NuxeoDocumentConnection documentConnection = nuxeoInterface.getDocumentConnection(documentEntityType, username);
            
            InputStream inputStream = documentConnection.getInputStream();
            String fileAsString = IOUtils.toString(inputStream, "UTF-8"); 
            
            return fileAsString;
        }catch(IOException e){
            throw new DetailedException("Failed to read '"+documentEntityType+"'.", 
                    "Failed to read the file specified to a string because an error was thrown : "+e.getMessage(), e);
        }
    }

    @Override
    public void updateFileContents(String username, String filePath, String content, boolean createBackup, boolean useAdminPrivilege) throws DetailedException {
        
        FileTreeModel fileModel = FileTreeModel.createFromRawPath(filePath);
        
        String filename = fileModel.getFileOrDirectoryName();
        String parentDirectory = fileModel.getParentTreeModel().getRelativePathFromRoot();
        Document rootFolderDocument = getUserRootFolder(username);
        String rootFolderPath = rootFolderDocument.getPath() + Constants.FORWARD_SLASH + parentDirectory;
        DocumentEntityType documentEntityType;
        
        try{
            // locate the file
            Document document = nuxeoInterface.getDocumentByName(rootFolderPath, filename, username);
            documentEntityType = new DocumentEntityType(document);
        }catch(IOException e){
            throw new DetailedException("There was a problem retrieving the file '" + filename + "'.", 
                    "Failed to retrieve the document '"+rootFolderPath+Constants.FORWARD_SLASH+filename+"' because "+e.getMessage(),
                    e);
        }
        
        if(createBackup) {
            try {
                // create a backup file
                String backupPath = filename + FileUtil.BACKUP_FILE_EXTENSION;
                AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
                FileTreeModel backupFileModel = fileModel.getParentTreeModel().getModelFromRelativePath(backupPath, true, false);
                if (useAdminPrivilege) {
                    fileServices.copyWorkspaceFileUsingAdmin(username, fileModel.getRelativePathFromRoot(),
                            backupFileModel.getRelativePathFromRoot(), NameCollisionResolutionBehavior.OVERWRITE, null);
                } else {
                    fileServices.copyWorkspaceFile(username, fileModel.getRelativePathFromRoot(),
                            backupFileModel.getRelativePathFromRoot(), NameCollisionResolutionBehavior.OVERWRITE, null);
                }
                
            } catch(Exception e) {
                throw new DetailedException("There was a problem creating a backup file of '"  + filename + "'.",
                        "Failed to backup the file '"+rootFolderPath+Constants.FORWARD_SLASH+filename+"' because " + e.getMessage(),
                        e);
            }
        }
        
        //Retrieve the course folder in order to update the last modified date value later on
        DocumentEntityType courseFolderDocumentEntityType;
        try{
            ServerFolderProxy courseFolderProxy = (ServerFolderProxy) getCourseFolder(filePath, username);
            courseFolderDocumentEntityType =  new DocumentEntityType(courseFolderProxy.getFolder());
        }catch(IOException e){
            throw new DetailedException("Failed to update the file contents.", 
                    "Failed to retrieve the course folder of the file being update of '"+filename+"' because "+e.getMessage(),
                    e);
        }
        
        try{
            // update file content
            nuxeoInterface.updateDocumentFile(documentEntityType, courseFolderDocumentEntityType, new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))), username, useAdminPrivilege);
        }catch(IOException e){
            throw new DetailedException("There was a problem updating the file '" + filename + "'.", 
                    "Failed to update the document '"+rootFolderPath+Constants.FORWARD_SLASH+filename+"' because "+e.getMessage(), e);
        }
    }
    
    @Override
    public List<String> getSIMILEConcepts(String username, FileTreeModel simileConfigFile)
            throws FileNotFoundException, IllegalArgumentException, DetailedException {
        
        String filename = simileConfigFile.getRelativePathFromRoot(true);
        
        Document rootFolderDocument = getUserRootFolder(username);
        
        DocumentEntityType documentEntityType;
        try{
            documentEntityType = nuxeoInterface.getDocumentEntityByName(rootFolderDocument.getPath(), filename, username);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve the the document.", 
                    "Failed to retrieve the document '"+filename+"' because "+e.getMessage(),
                    e);
        }
        
        try{
            NuxeoDocumentConnection documentConnection = nuxeoInterface.getDocumentConnection(documentEntityType, username);
            
            InputStream inputStream = documentConnection.getInputStream();
            List<String> concepts = DomainKnowledgeUtil.getSIMILEConcepts(documentEntityType.getPath(), inputStream);
            return concepts;
            
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve the SIMILE concepts from the ixs file.", 
                    "There was an error while retrieving the concepts from the SIMILE ixs file of '"+filename+"' : "+e.getMessage(), e);
        }        
        
    }
    
    /**
     * Copies files from the desktop for a copy folder operation from existing desktop documents into new nuxeo documents.
     * 
     * @param files the desktop files to copy from (includes files and folders)
     * @param sourceFolderName the name of the folder being copied (path not included in name) 
     * @param uploadRelativeSourceFolderName the upload directory path to the folder being copied
     * @param destinationParentFolderNuxeo the existing nuxeo folder to copy the folder into
     * @param destinationCourseFolderNuxeo the path to the course folder. Can't be null or empty. Can include the default workspace path of "/default-domain/workspaces/". 
     * This is used to update the last modified date of the course folder in Nuxeo since Nuxeo doesn't handle this automatically.
     * @param username used for authentication
     * @param createFolders whether or not to create folders in this method call.  This is useful when trying to do a two pass
     * operation where folders are created first followed by files in those folders.
     * @param createFiles whether or not to create file in this method call.  This is useful when trying to do a two pass
     * operation where folders are created first followed by files in those folders - due to the create document logic not
     * being able to create ancestor folders that don't exist.
     * @throws DocumentExistsException caused when creating a new folder that already exists (but shouldn't happen since there is a check before creating a folder)
     * @throws MalformedURLException if there was a problem constructing the URL for the document
     * @throws QuotaExceededException Thrown if file upload causes quota to be exceeded
     * @throws IOException May be thrown if a server error occurs
     */
    private void copyDesktopFolderFiles(File[] files, String sourceFolderName, String uploadRelativeSourceFolderName, 
            Document destinationParentFolderNuxeo, Document destinationCourseFolderNuxeo, String username, boolean createFolders, boolean createFiles) throws DocumentExistsException, MalformedURLException, QuotaExceededException, IOException{

        for(File file : files){
            
            //Need the name of parent folder to this file and that string should start with the source folder being copied
            //e.g. source directory = "C:\...\Domain\temp\<uniquefoldername>\subdirectoryA", file = "C:\...\Domain\temp\<uniquefoldername>\somecourse\subdirectoryA\ABC.dkf.xml" and destination parent = "/default-domain/workspaces/<username>/mycourse/subdirectoryB" then
            //the path prefix = "/default-domain/workspaces/<username>/mycourse/subdirectoryB/subdirectoryA/".
            String uploadFolderFilename = StringUtils.removeStart(file.getAbsolutePath(), UPLOAD_DIRECTORY.getAbsolutePath());  //get "<uniquefoldername>\subdirectoryA\ABC.dkf.xml"
            String pathSuffix = uploadFolderFilename.substring(uploadFolderFilename.indexOf(sourceFolderName) + sourceFolderName.length());  //strips "<uniquefoldername>\subdirectoryA" leaving path to file/folder within folder being copied

            //convert windows file separator (\) to unix file separator (/)
            pathSuffix.replace(Constants.BACKWARD_SLASH, Constants.FORWARD_SLASH);            
            
            //make path suffix include the folder name being copied
            if(pathSuffix.startsWith(Constants.FORWARD_SLASH)){
                pathSuffix = sourceFolderName + pathSuffix;
            }else{
                pathSuffix = sourceFolderName + Constants.FORWARD_SLASH + pathSuffix;
            }
            
            String workspacePath = destinationParentFolderNuxeo.getPath() + Constants.FORWARD_SLASH + pathSuffix;
            if(file.isDirectory() && !nuxeoInterface.documentExists(workspacePath, file.getName(), true, username)){
               
                if(createFolders){
                    nuxeoInterface.createWorkspaceFolder(workspacePath, file.getName(), username, true);
                }
            }else if(createFiles){
            
                try(InputStream inputStream = new FileInputStream(file)){
                    String courseFolderPath = destinationCourseFolderNuxeo != null ? destinationCourseFolderNuxeo.getPath() : null;
                    nuxeoInterface.createDocument(workspacePath, file.getName(), courseFolderPath, inputStream, username);
                }

            }
        }

    }
    
    /**
     * Copies documents for a copy folder operation from existing nuxeo documents into new documents.
     * 
     * @param documents the documents to copy from (includes files and folders)
     * @param sourceFolderName the name of the folder being copied (path not included in name) 
     * @param workspaceRelativeSourceFolderName the workspace path to the folder being copied
     * @param destinationParentFolderNuxeo the existing nuxeo folder to copy the folder into
     * @param destinationCourseFolder the path to the course folder. Can't be null or empty. Can include the default workspace path of "/default-domain/workspaces/". 
     * This is used to update the last modified date of the course folder in Nuxeo since Nuxeo doesn't handle this automatically.
     * @param username used for authentication
     * @param createFolders whether or not to create folders in this method call.  This is useful when trying to do a two pass
     * operation where folders are created first followed by files in those folders.
     * @param createFiles whether or not to create file in this method call.  This is useful when trying to do a two pass
     * operation where folders are created first followed by files in those folders - due to the create document logic not
     * being able to create ancestor folders that don't exist.
     * @throws DocumentExistsException caused when creating a new folder that already exists (but shouldn't happen since there is a check before creating a folder)
     * @throws MalformedURLException if there was a problem constructing the URL for the document
     * @throws QuotaExceededException Thrown if file upload causes quota to be exceeded
     * @throws IOException May be thrown if a server error occurs
     */
    private void copyNuxeoFolderDocuments(Documents documents, String sourceFolderName, String workspaceRelativeSourceFolderName, 
            Document destinationParentFolderNuxeo, Document destinationCourseFolder, String username, boolean createFolders, boolean createFiles) throws DocumentExistsException, MalformedURLException, QuotaExceededException, IOException{
        
        for(Document document : documents){
            
            //Need the name of parent folder to this file and that string should start with the source folder being copied
            //e.g. source directory = "/default-domain/workspaces/Public/somecourse/subdirectoryA", 
            //     document name = "/default-domain/workspaces/Public/somecourse/subdirectoryA/ABC.dkf.xml" 
            //     destination parent = "/default-domain/workspaces/<username>/mycourse/subdirectoryB" 
            // then
            //     path suffix = "/default-domain/workspaces/<username>/mycourse/subdirectoryB/subdirectoryA/".
            String parentFolderName = document.getPath().substring(0, document.getPath().lastIndexOf(Constants.FORWARD_SLASH));
            String pathSuffix = parentFolderName.substring(parentFolderName.indexOf(workspaceRelativeSourceFolderName) + workspaceRelativeSourceFolderName.length());

            if(pathSuffix.startsWith(Constants.FORWARD_SLASH)){
                pathSuffix = sourceFolderName + pathSuffix;
            }else{
                pathSuffix = sourceFolderName + Constants.FORWARD_SLASH + pathSuffix;
            }
            
            String workspacePath = destinationParentFolderNuxeo.getPath() + Constants.FORWARD_SLASH + pathSuffix;
            if(nuxeoInterface.isFolder(document)){
                
                if(createFolders && !nuxeoInterface.documentExists(workspacePath, document.getTitle(), true, username)){
                    nuxeoInterface.createWorkspaceFolder(workspacePath, document.getTitle(), username, true);
                }
                
            }else if(createFiles){
            
                NuxeoDocumentConnection connection = nuxeoInterface.getDocumentConnection(document, username);
                try(InputStream inputStream = connection.getInputStream()){
                    String courseFolderPath = destinationCourseFolder != null ? destinationCourseFolder.getPath() : null;
                    nuxeoInterface.createDocument(workspacePath, document.getTitle(), courseFolderPath, inputStream, username);
                }

            }
        }

    }

    @Override
    public DownloadableFileRef exportCourses(ExportProperties exportProperties) throws DetailedException, URISyntaxException {
        
        //create zip where the export contents will be placed
        File exportZipFile = new File(EXPORT_DIRECTORY.getAbsolutePath() + File.separator + exportProperties.getExportFileName() + ".zip");
        
        String username = exportProperties.getUsername();
        
        Document rootFolderDocument = getUserRootFolder(username);
        
        ServerFolderProxy rootFolder = new ServerFolderProxy(rootFolderDocument, username, nuxeoInterface);
        
        //convert domain options into course folders
        List<AbstractFolderProxy> courseFolders = new ArrayList<>();
        for(DomainOption domainOption : exportProperties.getCoursesToExport()){
            
            String courseFileName = domainOption.getDomainId();
            try{
                AbstractFolderProxy courseFolder = rootFolder.getParentFolder(rootFolder.getRelativeFile(courseFileName));
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

        Document rootFolderDocument = getUserRootFolder(username);  
        Document usersWorkspaceDocument;
        try{
            usersWorkspaceDocument = nuxeoInterface.getFolderByName(rootFolderDocument.getPath(), username, username);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve the user's workspace folder.", 
                    "There was an error when trying to retrieve '"+username+"' workspace folder: "+e.getMessage()+".", e);
        }
        
        ServerFolderProxy usersWorkspaceFolder = new ServerFolderProxy(usersWorkspaceDocument, username, nuxeoInterface);
        
        ImportCourseUtil importUtil = new ImportCourseUtil();
        importUtil.importCourses(username, usersWorkspaceFolder, giftExportZipToImport, progressIndicator, filesToOverwrite, courseToNameMap);
    }

    @Override
    public float getCourseExportSize(ExportProperties exportProperties)
            throws DetailedException, URISyntaxException {
        
        String username = exportProperties.getUsername();
        
        Document rootFolderDocument = getUserRootFolder(username);
        
        ServerFolderProxy rootFolder = new ServerFolderProxy(rootFolderDocument, username, nuxeoInterface);
        
        //convert domain options into course folders
        List<AbstractFolderProxy> courseFolders = new ArrayList<>();
        for(DomainOption domainOption : exportProperties.getCoursesToExport()){
            
            String courseFileName = domainOption.getDomainId();
            try{
                AbstractFolderProxy courseFolder = rootFolder.getParentFolder(rootFolder.getRelativeFile(courseFileName));
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
        
        //the child directory path including that root folder path
        String parentDirectoryName = FileTreeModel.correctFilePath(parentDirectoryPath);    
        
        Document parentDirectoryDocument;
        try{
            parentDirectoryDocument = nuxeoInterface.getDocumentByName(parentDirectoryName, username);
        }catch(IOException ioe){
            throw new DetailedException("Unable to find the directory to create the file in of '"+parentDirectoryName+"'.",
                    "The directory "+parentDirectoryName+" doesn't exist.", ioe);
        }
        
        if(!nuxeoInterface.isFolder(parentDirectoryDocument) && !nuxeoInterface.isWorkspace(parentDirectoryDocument)){
            throw new IllegalArgumentException("The parent directory must be a directory.");
        }
        
        String newDirectoryName = parentDirectoryDocument.getPath() + Constants.FORWARD_SLASH + name;
        try{
            Document newDirectoryDocument = nuxeoInterface.getDocumentByName(newDirectoryName, username);
            
            if(newDirectoryDocument != null){
                
                if(ignoreExistingFolder){
                    return new ServerFolderProxy(newDirectoryDocument, username, nuxeoInterface);
                }
                
                throw new IllegalArgumentException("Can't create a directory '"+newDirectoryName+"' that already exist.");
            }
            
        }catch(@SuppressWarnings("unused") Exception e){
            //acceptable because this should mean the directory doesn't exist yet
        }
        
        Document createdDocument;
        try{
            createdDocument = nuxeoInterface.createWorkspaceFolder(parentDirectoryName, name, username, false);
        }catch(IOException e){
            throw new DetailedException("Failed to create the folder named '"+parentDirectoryName+ Constants.FORWARD_SLASH + name+"'.", 
                    "An IO exception occurred while trying to create the folder in '"+parentDirectoryName+ Constants.FORWARD_SLASH + name, e);
        }
        
        return new ServerFolderProxy(createdDocument, username, nuxeoInterface);
    }
    
    @Override
    public AbstractFolderProxy createTrainingAppsLibUserFolder(String username)
            throws DetailedException, IllegalArgumentException {

        AbstractFolderProxy trainingAppsLibDirectory = getTrainingAppsLibraryFolder(username);
        String newDirectoryName = trainingAppsLibDirectory.getFileId() + Constants.FORWARD_SLASH + username;
        try {
            Document newDirectoryDocument = nuxeoInterface.getDocumentByName(newDirectoryName, username);
            if (newDirectoryDocument != null) {
                throw new IllegalArgumentException(
                        "Can't create a training apps lib user directory that already exist.");
            }
        } catch (@SuppressWarnings("unused") Exception e) {
            // acceptable because this should mean the directory doesn't exist yet
        }

        Document createdDocument;
        try {
            createdDocument = nuxeoInterface.createTrainingAppsLibUserFolder(username);
        } catch (IOException e) {
            throw new DetailedException("Failed to create the folder named '" + username + "'.",
                    "An IO exception occurred while trying to create the folder '" + username + "'.", e);
        }

        return new ServerFolderProxy(createdDocument, username, nuxeoInterface);
    }

    @Override
    public boolean fileExists(String username, String filePath, boolean isFolder) throws DetailedException {
        
        if(filePath == null){
            throw new IllegalArgumentException("The path to the file can't be null.");
        }
        
        FileTreeModel file = FileTreeModel.createFromRawPath(filePath);
          
        //the workspaces folder of Nuxeo
        Document rootFolderDocument = getUserRootFolder(username);
        
        //the file path excluding the workspace root folder
        String parentDirectoryName = rootFolderDocument.getPath() + Constants.FORWARD_SLASH + file.getParentTreeModel().getRelativePathFromRoot();
        
        try{
            return nuxeoInterface.documentExists(parentDirectoryName, file.getFileOrDirectoryName(), isFolder, username); 
        }catch(IOException e){
            throw new DetailedException("Failed to determine whether the file exists.", 
                    "An IO exception occurred while checking if '"+parentDirectoryName + Constants.FORWARD_SLASH + file.getFileOrDirectoryName()+"' already exists.", e);
        }    
    }

    @Override
    public FileProxy getFile(String filename, String username) {
        
        if(filename == null) { 
            throw new IllegalArgumentException("The filename for getFile cannot be null");
        }
        
        Document document;
        try{
            Document rootFolderDocument = getUserRootFolder(username);
            document = nuxeoInterface.getDocumentByName(rootFolderDocument.getPath(), filename, username);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve the the document.", 
                    "Failed to retrieve the document '"+filename+"' because "+e.getMessage(),
                    e);
        }
        
        ExternalFileSystemInterface externalFileSystemInterface;
        try{
            externalFileSystemInterface = nuxeoInterface.getDocumentConnection(document, username);        
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve the connection for the document.", 
                    "Failed to retrieve the connection for '"+document.getPath()+"' because "+e.getMessage(),
                    e);
        }
        
        FileProxyPermissions permissions = nuxeoInterface.getPermissions(document);
        return new FileProxy(document.getTitle(), document.getPath(), externalFileSystemInterface, permissions);
    }
    
    @Override
    public AbstractFolderProxy getFolderFromFile(File file, String username) {
        return getFolder(file.getPath(), username);
    }

    @Override
    public AbstractFolderProxy getFolder(String folderPath, String username) {
        try {
            String targetFilePath = FileTreeModel.correctFilePath(folderPath);
            Document folderDoc = nuxeoInterface.getDocumentByName(targetFilePath, username);
            return new ServerFolderProxy(folderDoc, username, nuxeoInterface);
        } catch (IOException e) {
            throw new DetailedException("Failed to retrieve the the folder.",
                    "Failed to retrieve the folder '" + folderPath + "' because " + e.getMessage(), e);
        }
    }

    @Override
    public FileTreeModel trimWorkspaceFromPath(String pathToTrim, String username) {
        String filePath = FileTreeModel.correctFilePath(pathToTrim);

        Document userRoot = getUserRootFolder(username);
        AbstractFolderProxy workspaceFolder = getFolder(userRoot.getPath(), username);
        String workspaceFolderPath = FileTreeModel.correctFilePath(workspaceFolder.getFileId());

        // has the workspace preprended, remove it
        if (filePath.startsWith(workspaceFolderPath)) {
            filePath = filePath.substring(workspaceFolderPath.length());
        }

        return FileTreeModel.createFromRawPath(filePath);
    }    

    @Override
    public String trimWorkspacesPathFromFullFilePath(String fullFilePathToTrim){
        
        if(fullFilePathToTrim.startsWith(nuxeoInterface.getDefaultWorkspacePath())){
            return fullFilePathToTrim.substring(nuxeoInterface.getDefaultWorkspacePath().length());
        }
        
        return fullFilePathToTrim;
    }

    @Override
    public AbstractFolderProxy getCourseFolder(String filePath,
            String username) throws FileNotFoundException, DetailedException {
        
        String targetFilePath = FileTreeModel.correctFilePath(filePath);
        
        Document rootFolderDocument = getUserRootFolder(username);
        Document usersWorkspaceDocument;
        
        FileTreeModel file = FileTreeModel.createFromRawPath(targetFilePath);
        
        try{
            String parentFolderPath = "";
            
            if(file.getParentTreeModel() != null){
                parentFolderPath = file.getParentTreeModel().getRelativePathFromRoot();
            }
            
            if(nuxeoInterface.documentExists(parentFolderPath, file.getFileOrDirectoryName(), true, username)){
                
                //the file exists in the CMS, so check if it is a directory or not
                usersWorkspaceDocument = nuxeoInterface.getDocumentByName(targetFilePath, username);
                
                if(!nuxeoInterface.isFolder(usersWorkspaceDocument)){
                    
                    if(!parentFolderPath.isEmpty()){
                        
                        //the file is not a directory, therefore return its parent
                        usersWorkspaceDocument = nuxeoInterface.getDocumentByName(parentFolderPath, username);
                        
            }else{
                        //the file has NO parent, so it can't possibly have an ancestor course folder
                        throw new IOException("No parent folder exists for " + targetFilePath + "; therefore, it can't have an ancestor course folder.");
                    }
                }
                
                }else{
                
                if(!parentFolderPath.isEmpty()){
                    
                    //the file is not a directory, therefore return its parent
                    usersWorkspaceDocument = nuxeoInterface.getDocumentByName(parentFolderPath, username);
                    
                } else {
                    
                    //the file has NO parent, so it can't possibly have an ancestor course folder
                    throw new IOException("No parent folder exists for " + targetFilePath + "; therefore, it can't have an ancestor course folder.");
                }
            }
                
        }catch(IOException e){
            throw new DetailedException("Failed to find the course folder.", 
                    "There was an error while trying to find the course folder for the file/folder '"+rootFolderDocument.getPath()+Constants.FORWARD_SLASH+targetFilePath+"' : "+e.getMessage(), e);
        }
        
        try{
            findAncestorCourseFolder(new ServerFolderProxy(usersWorkspaceDocument, username, nuxeoInterface));
        }catch(Exception e){
            throw new DetailedException("Failed to find the ancestor course folder.", e.getMessage(), e);
        }
        
        return new ServerFolderProxy(usersWorkspaceDocument, username, nuxeoInterface);
    }
    
    /**
     * Recursively search up the file tree starting in the directory provided until a course.xml
     * file is found.  The parent folder to the course file is a course folder that is returned
     * by this method.  This method will not search into folders that are found as the intent
     * is to find an ancestor folder that contains a course file.
     * 
     * The search will stop once the workspace folder has been reached.
     * 
     * @param directory the directory to start searching in, followed by it's parent folder.
     * @return the first ancestor course folder found starting with the provided directory.  Null can be returned if
     * no course folder was found.
     * @throws DetailedException if there was a problem searching
     * @throws URISyntaxException if there was a problem constructing URIs to retrieve documents 
     */
    public static ServerFolderProxy findAncestorCourseFolder(ServerFolderProxy directory) throws DetailedException, URISyntaxException{
        
        if(!directory.isDirectory()){
            throw new IllegalArgumentException("The directory value must be a directory.");
        }
        
        try{
            //look for a course.xml file in the current directory
            List<FileProxy> files = directory.listFiles(null, AbstractSchemaHandler.COURSE_FILE_EXTENSION);
            for(FileProxy file : files){
                
                ServerFolderProxy parentFolder = (ServerFolderProxy) directory.getParentFolder(file);
                if(parentFolder.getFileId().equals(directory.getFileId())){
                    // the file is in the directory provided to this parameter and not a subdirectory which is
                    // important because we are only trying to search direct children of the directory provided, 
                    // not all descendant course files
    
                    return directory;
                }
            }
    
            //didn't find a course.xml file in this directory, check the parent directory
            ServerFolderProxy parent = directory.getParentFolder(directory);
            if(parent.getFileId().equals(NuxeoInterface.DEFAULT_WORKSPACE_ROOT)){
                //reached the end of searching
                return null;
            }else{
                return findAncestorCourseFolder(parent);
            }
        }catch(IOException e){
            throw new DetailedException("Failed to find an ancestor course folder.", 
                    "There was a problem while searching for an ancestor course folder of '"+directory.getFileId()+"' : "+e.getMessage(), e);
            
        }
    }
    
    /**
     * Return the user's root folder Nuxeo document.
     * 
     * @param username used for authentication.  Can't be null.
     * @return the new document representing that user's root folder in Nuxeo
     * @throws DetailedException if there was a problem retrieving that folder's document instance
     */
    private Document getUserRootFolder(String username) throws DetailedException{
        
        try{
            return nuxeoInterface.getUserRootFolder(username);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve the user's root folder.", 
                    "An exception was thrown while retrieving the user's root folder document from Nuxeo of "+e.getMessage(), e);
        }
    }
    
    @Override
    public ServerFolderProxy getWorkspaceFolderProxy(String username) throws IOException {
        
        Document workspaces = nuxeoInterface.getUserRootFolder(username);
        ServerFolderProxy workspaceFolderProxy = new ServerFolderProxy(workspaces, username, nuxeoInterface);
        
        return workspaceFolderProxy;
    }

    /**
     * Return the user's private root folder Nuxeo document (username workspace).
     * 
     * @param username used for authentication.  Can't be null.
     * @return the new document representing that user's root folder in Nuxeo
     * @throws DetailedException if there was a problem retrieving that folder's document instance
     */
    private Document getUserPrivateRootFolder(String username) throws DetailedException{
        
        try{
            return nuxeoInterface.getUserPrivateFolder(username);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve the user's root folder.", 
                    "An exception was thrown while retrieving the user's root folder document from Nuxeo of "+e.getMessage(), e);
        }
    }

    @Override
    public long getRemainingWorkspacesQuota(String username) throws DetailedException {

        try{
            long bytes = nuxeoInterface.getRemainingWorkspaceQuota(username, username);
            float megaBytes = FileUtil.byteToMb(bytes);
            return (long)megaBytes;
        }catch(Exception e){
            throw new DetailedException("Failed to retrieve the user's remaining disk space.", 
                    "An exception was thrown while retrieving the user's remaining disk space of "+e.getMessage(), e);
        }
    }

    @Override
    public List<String> getMetadataForContent(String username,
            FileTreeModel content, MerrillQuadrantEnum quadrant)
            throws DetailedException, URISyntaxException {
        
        //
        // search for metadata files in the same directory as the content
        //
        
        //get the content's parent folder proxy
        Document rootFolderDocument = getUserRootFolder(username);
        ServerFolderProxy rootFolderProxy = new ServerFolderProxy(rootFolderDocument, username, nuxeoInterface);
        String relativeFolderName = content.getParentTreeModel().getRelativePathFromRoot(true);
        
        Document contentFolderDocument;
        try{
            contentFolderDocument = nuxeoInterface.getFolderByName(rootFolderProxy.getFileId(), relativeFolderName, username);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve the content's parent folder.", 
                    "There was an error when trying to retrieve parent folder of '"+relativeFolderName+"'.\n\n"+e.getMessage(), e);
        }
        
        AbstractFolderProxy contentFolderProxy = new ServerFolderProxy(contentFolderDocument, username, nuxeoInterface);
        
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
                metadataFilename = rootFolderProxy.getRelativeFileName(metadataFileProxy);
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
        
        AbstractFolderProxy trainingAppsLibFolder = getTrainingAppsLibraryFolder(username);

        // find the files
        List<FileProxy> trainingAppFiles = new ArrayList<>();
        try {
            ServerFolderProxy searchFolder;
            
            if(progressIndicator != null){
                progressIndicator.setTaskDescription(GET_SHOWCASE_TA_OBJECTS_PROGRESS_DESC);
                progressIndicator.setPercentComplete(GET_SHOWCASE_TA_OBJECTS_PROGRESS_PERC);
            }

            // search Public folder
            final FileTreeModel publicTrainingAppsLibraryTreeModel = getPublicTrainingAppsLibraryTreeModel(username);
            String parentDirectoryPath = publicTrainingAppsLibraryTreeModel.getParentTreeModel()
                    .getRelativePathFromRoot(true);
            if (nuxeoInterface.documentExists(parentDirectoryPath,
                    publicTrainingAppsLibraryTreeModel.getFileOrDirectoryName(), true, username)) {
                Document publicFolder = nuxeoInterface.getDocumentByName(parentDirectoryPath,
                        publicTrainingAppsLibraryTreeModel.getFileOrDirectoryName(), username);
                searchFolder = new ServerFolderProxy(publicFolder, username, nuxeoInterface);
                FileFinderUtil.getFilesByExtension(searchFolder, trainingAppFiles,
                        AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);
            }
            
            if(progressIndicator != null){
                progressIndicator.setTaskDescription(GET_USERNAME_TA_OBJECTS_PROGRESS_DESC);
                progressIndicator.setPercentComplete(GET_USERNAME_TA_OBJECTS_PROGRESS_PERC);
            }
    
            // search user folder
            if (StringUtils.isNotBlank(username)
                    && nuxeoInterface.documentExists(trainingAppsLibFolder.getFileId(), username, true, username)) {
                Document userFolder = nuxeoInterface.getDocumentByName(trainingAppsLibFolder.getFileId(), username,
                        username);
                searchFolder = new ServerFolderProxy(userFolder, username, nuxeoInterface);
                FileFinderUtil.getFilesByExtension(searchFolder, trainingAppFiles,
                        AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);
            }
        } catch (Exception e) {
            throw new DetailedException("Failed to retrieve the training applications.",
                    "An exception was thrown while retrieving the training applications. Message: " + e.getMessage(),
                    e);
        }
        
        if(progressIndicator != null){
            progressIndicator.setTaskDescription(CHECK_TA_OBJECTS_PROGRESS_DESC);
            progressIndicator.setPercentComplete(CHECK_TA_OBJECTS_PROGRESS_PERC);
        }        
        
        // parse the files
        Map<FileTreeModel, TrainingAppCourseObjectWrapper> trainingAppObjects = new HashMap<>();
        for (FileProxy file : trainingAppFiles) {

            AbstractFolderProxy courseObjectFolder = null;
            generated.course.TrainingApplication trainingApp = null;
            DetailedException validationException = null;
            // get generated class instance from XML file
            try {
                courseObjectFolder = trainingAppsLibFolder.getParentFolder(file);
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
                if (details == null) {
                    details = "An exception was thrown when reading '" + file + "'.";
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
                final String taLibFileId = trainingAppsLibFolder.getFileId();
                if (courseObjFileId.startsWith(taLibFileId)) {
                    courseObjFileId = courseObjFileId.substring(taLibFileId.length());
                }

                FileTreeModel fileModel = getTrainingAppsLibraryTreeModel(username)
                        .getModelFromRelativePath(courseObjFileId);
                
                ContentTypeEnum contentTypeEnum = null;
                try {
                    contentTypeEnum = getContentType(trainingApp);
                } catch (@SuppressWarnings("unused") Exception e) {
                    // best effort - for now
                    logger.error("Failed to determine the content type for '"+courseObjFileId+"' on behalf of "+username);
                }

                if (validationException != null) {
                    trainingAppObjects.put(fileModel,
                            new TrainingAppCourseObjectWrapper(courseObjectFolder.getName(), validationException, contentTypeEnum));
                } else {
                    trainingAppObjects.put(fileModel, new TrainingAppCourseObjectWrapper(trainingApp, contentTypeEnum));
                }
            } catch (Exception e) {
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
    public boolean renameTrainingAppsLibFolder(String username, String filePath, String newName) {
        FileTreeModel trainingAppSubFolder = getRootTree(username).getModelFromRelativePath(filePath);

        final String originalName = trainingAppSubFolder.getFileOrDirectoryName();
        try {
            if (!nuxeoInterface.documentExists(trainingAppSubFolder.getParentTreeModel().getRelativePathFromRoot(),
                    trainingAppSubFolder.getFileOrDirectoryName(), true, username)) {
                throw new IllegalArgumentException("The filePath provided cannot be found.");
    }
    
            Document subFolderDoc = nuxeoInterface.getDocumentByName(trainingAppSubFolder.getRelativePathFromRoot(),
                    username);
            if (!nuxeoInterface.isFolder(subFolderDoc)) {
                throw new IllegalArgumentException("The filePath does not point to a directory.");
            }

            ServerFolderProxy subFolder = new ServerFolderProxy(subFolderDoc, username, nuxeoInterface);

            // update inner file names first
            List<FileProxy> files = subFolder.listFiles(null);
            for (FileProxy childFile : files) {
                String childFileName = childFile.getName();
                if (childFileName.startsWith(originalName + ".")) {
                    childFileName = childFileName.replaceFirst(originalName, newName);
                    renameFile(username, childFile.getFileId(), childFileName, false);
                }
            }
        } catch (Exception e) {
            throw new DetailedException("Failed to rename the training application user sub-folder.",
                    "An exception was thrown while renaming the training application user sub-folder. Message: "
                            + e.getMessage(),
                    e);
        }

        return renameFile(username, filePath, newName, false);
    }
    
    @Override
    public FileTreeModel getMediaFiles(String username,
            String courseFolderPath) {
        
        Document rootFolderDocument = getUserRootFolder(username);
        ServerFolderProxy rootFolderProxy = new ServerFolderProxy(rootFolderDocument, username, nuxeoInterface);
        
        Document contentFolderDocument;
        try{
            contentFolderDocument = nuxeoInterface.getFolderByName(rootFolderProxy.getFileId(), courseFolderPath, username);
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve the course folder.", 
                    "There was an error when trying to retrieve course folder of '"+courseFolderPath+"'.\n\n"+e.getMessage(), e);
        }        
        
        return getNonGIFTFilesFileTree(username, contentFolderDocument);
    }
    
    @Override
    public Map<File, File> checkForImportConflicts(String username, File giftExportZipToImport, ProgressIndicator progress) throws DetailedException { 
        
         Document rootFolderDocument = getUserRootFolder(username);  
         Document usersWorkspaceDocument;
         try{
             usersWorkspaceDocument = nuxeoInterface.getFolderByName(rootFolderDocument.getPath(), username, username);
         }catch(IOException e){
             throw new DetailedException("Failed to retrieve the user's workspace folder.", 
                     "There was an error when trying to retrieve '"+username+"' workspace folder: "+e.getMessage()+".", e);
         }
         
         ServerFolderProxy usersWorkspaceFolder = new ServerFolderProxy(usersWorkspaceDocument, username, nuxeoInterface);
        
        ImportCourseUtil importUtil = new ImportCourseUtil();
        return importUtil.checkForConflicts(giftExportZipToImport, usersWorkspaceFolder, progress);    
    }

    @Override
    public boolean hasWritePermissions(String username, String filePath) throws DetailedException{
        
        try {
            
            String targetFilePath = FileTreeModel.correctFilePath(filePath);
            
            if(!nuxeoInterface.documentExists(null, targetFilePath, false, username)){
                //search up the file path to determine inherited permissions
                
                //get parent folder to provided file
                if(targetFilePath.contains(Constants.FORWARD_SLASH)){
                    targetFilePath = targetFilePath.substring(0, targetFilePath.lastIndexOf(Constants.FORWARD_SLASH));
                    
                    while(!nuxeoInterface.documentExists(null, targetFilePath, true, username)){
                        //parent folder doesn't exist - if a bad file path is provided this will eventually error out
                        
                        if(!targetFilePath.contains(Constants.FORWARD_SLASH)){
                            break;
                        }
                        
                        targetFilePath = targetFilePath.substring(0, targetFilePath.lastIndexOf(Constants.FORWARD_SLASH));
                        
                    }
                }
            }

            Document document = nuxeoInterface.getDocumentByName(targetFilePath, username);
            
            return nuxeoInterface.hasWritePermissions(document, username);
            
        } catch (Exception e) {

            throw new DetailedException(
                    "Failed to determine whether user '" + username + "' has permission to modify '" + filePath, 
                    "There was an error when trying to retrieve the document for '" + filePath, 
                    e);
        }
    }

    @Override
    public FileTreeModel getFileTreeByPath(String username, String filePath) {
        
        String filename = FileTreeModel.correctFilePath(filePath);
        
        Document rootFolderDocument = getUserRootFolder(username);
        
        Document document;
        try{
            if(nuxeoInterface.documentExists(rootFolderDocument.getPath(), filename, true, username)){
                document = nuxeoInterface.getDocumentByName(rootFolderDocument.getPath(), filename, username);
            }else{
                return null;
            }
        }catch(IOException e){
            throw new DetailedException("Failed to retrieve the the document.", 
                    "Failed to retrieve the document '"+filename+"' because "+e.getMessage(),
                    e);
        }
        
        if(nuxeoInterface.isFolder(document)){
            
            //populate a file tree for the target file, relative to the workspace folder
            FileTreeModel populatedFileModel = getFileTree(username, document);
            
            FileTreeModel targetFileModel = populatedFileModel.getModelFromRelativePath(filename, false);
            
            //we want a model relative to the workspace folder, so don't include the workspace folder itself in the returned model
            FileTreeModel currentModel = targetFileModel;
            
            while(currentModel.getParentTreeModel() != null){
                
                if(currentModel.getParentTreeModel().getParentTreeModel() == null){
                    currentModel.detatchFromParent();
                    break;
                }
                
                currentModel = currentModel.getParentTreeModel();
            }
            
            return targetFileModel;
            
        } else {
            return FileTreeModel.createFromRawPath(filename);
        }
    }

    @Override
    public String copyFile(String username, String originalSourcePath, String originalDestinationPath, NameCollisionResolutionBehavior nameCollisionResolutionBehavior,
            ProgressIndicator progress, boolean isDomainFile, boolean useAdminPrivilege)
            throws FileExistsException, IllegalArgumentException, DetailedException {
        
        boolean overwriteExistingFile = NameCollisionResolutionBehavior.OVERWRITE == nameCollisionResolutionBehavior;
        String sourcePath = FileTreeModel.correctFilePath(originalSourcePath);
        String destinationPath = FileTreeModel.correctFilePath(originalDestinationPath);
        
        FileTreeModel sourceModel = FileTreeModel.createFromRawPath(sourcePath);
        FileTreeModel destinationModel = FileTreeModel.createFromRawPath(destinationPath);
        
        //include the parent folder (e.g. [for Desktop] 'workspace', 'temp') (e.g. [for Server] 'workspaces')
        String sourceFilename = sourcePath;
        
        if(isDomainFile){
            
            //copy a file/folder from the Domain directory
            File sourceFile = new File(DOMAIN_DIRECTORY.getAbsolutePath() + File.separator + sourceFilename); 
            
              if(sourceFile.isDirectory()){
                  
                //copy a folder
                String copiedFileName = sourceModel.getFileOrDirectoryName();
                  
                  //does the destination parent folder exist?
                  String workspaceRelativeDestinationParentFolderName = destinationPath;
                  Document destinationParentFolderNuxeo;
                  try{
                      destinationParentFolderNuxeo = nuxeoInterface.getDocumentByName(workspaceRelativeDestinationParentFolderName, username);
                      updateProgress(progress, null, 10);
                  }catch(IOException e){
                      // The destination folder doesn't exist and needs to be created. This happens when the user 
                      // is prompted to rename the file to be copied. 

                      copiedFileName = destinationModel.getFileOrDirectoryName();
                      workspaceRelativeDestinationParentFolderName = destinationModel.getParentTreeModel().getRelativePathFromRoot();
                      
                      try {
                          destinationParentFolderNuxeo =  nuxeoInterface.getDocumentByName(workspaceRelativeDestinationParentFolderName, username);
                      } catch (IOException ioe) {
                          throw new DetailedException("Failed to retrieve the destination folder of '"+workspaceRelativeDestinationParentFolderName+"' to copy the folder to.", 
                              "The destination folder to place the folder being copied must already exist.  There was an IO exception when trying to retrieve the destination document: "+e.getMessage(),
                                  ioe);
                      }
                  }
                  
                  //does the destination parent folder contain the source folder already?
                  boolean isSourceInDestination;
                  try{
                      
                      updateProgress(progress, "Checking for existing file...", 0);
                      isSourceInDestination = nuxeoInterface.documentExists(workspaceRelativeDestinationParentFolderName, copiedFileName, true, username);
                      updateProgress(progress, null, 5);
                  
                  }catch(IOException e){
                      throw new DetailedException("Failed to determine whether the destionation folder already contains the folder being copied.", 
                              "An IO exception occurred while checking if '"+workspaceRelativeDestinationParentFolderName + Constants.FORWARD_SLASH + sourceModel.getFileOrDirectoryName()+"' already exists.", e);
                  }
                  
                  if(!isSourceInDestination || overwriteExistingFile){  
                      
                          if(!isSourceInDestination){
                              //create the source folder in the destination folder since it doesn't exist
                              try{
                                  
                                  updateProgress(progress, "Creating new folder...", 0);
                                  nuxeoInterface.createWorkspaceFolder(workspaceRelativeDestinationParentFolderName, copiedFileName, username, false);
                                  updateProgress(progress, null, 5);
                              
                              }catch(IOException e){
                                  throw new DetailedException("Failed to create the folder named '"+copiedFileName+"'.", 
                                          "An IO exception occurred while trying to create the folder in '"+workspaceRelativeDestinationParentFolderName, e);
                              }
                          }
                  }else{
                      
                      throw new FileExistsException(
                              workspaceRelativeDestinationParentFolderName + Constants.FORWARD_SLASH + sourceModel.getFileOrDirectoryName(), 
                              "A folder named '" + sourceModel.getFileOrDirectoryName() + "' already exists in " + workspaceRelativeDestinationParentFolderName + ""
                                      + "; therefore, " + sourcePath + " could not be copied to that location.",
                              "A folder named '" + sourceModel.getFileOrDirectoryName() + "' was detected in the destination folder "
                                      + "'" + workspaceRelativeDestinationParentFolderName + "' during a copy operation in which overwriting was disabled. "
                                      + "Since overwriting was disabled, the source folder '" + sourcePath + "' was not copied to its destination.",
                              null);
                  }
                  
                  //Retrieve the course folder in order to update the last modified date value later on
                  Document courseFolderDocument;
                  try{
                      ServerFolderProxy courseFolderProxy = (ServerFolderProxy) getCourseFolder(destinationPath, username);
                      courseFolderDocument =  courseFolderProxy.getFolder();
                  }catch(@SuppressWarnings("unused") Exception e){
                      
                      // When copying a course, we may potentially copy one of the course's subfolders before its course.xml is copied over, in which case
                      // the call to get the course folder will fail since the folder doesn't actually have a course.xml yet. To deal with this, we 
                      // won't bother trying to update the subfolder's containing course folder if we can't find it.
                      courseFolderDocument = destinationParentFolderNuxeo;
                  }
                  
                  try{
                      //get java.io.Files of source folder
                      File[] files = sourceFile.listFiles();
      
                      //
                      // first create folders - note the folders can come in any order, including descendant folders before ancestors.
                      //
                      updateProgress(progress, "Copying folders...", 0);
                      copyDesktopFolderFiles(files, sourceModel.getFileOrDirectoryName(), sourcePath, destinationParentFolderNuxeo, courseFolderDocument, username, true, false);
                      updateProgress(progress, null, 10);
                      
                      //
                      // second create files - creating a new document doesn't automatically create the ancestor folder structure, hence
                      //                       why folders were created first (above method call).
                      //
                      updateProgress(progress, "Copying files...", 0);
                      copyDesktopFolderFiles(files, sourceModel.getFileOrDirectoryName(), sourcePath, destinationParentFolderNuxeo, courseFolderDocument, username, false, true);
                      updateProgress(progress, null, 100);
      
                    
                  }catch(QuotaExceededException qe){
                      logger.error("Caught quota exceeded exception while creating new files for a copy folder operation on '"+sourcePath+"' to '"+destinationPath+"'. ",qe);
                      
                      //attempting rollback
                      //Note: currently unable to cleanup files that were over-written during the copy operation since we don't keep any backups of the original files
                      //      therefore we have no solution for a merge of an existing folder
                      try{
                          if(!isSourceInDestination){
                              //this method created the source folder in the destination folder so we can just delete it and along with it, any descendant files that were also created
                              nuxeoInterface.deleteWorkspaceFolder(destinationParentFolderNuxeo.getPath() + Constants.FORWARD_SLASH + sourceModel.getFileOrDirectoryName(), null, username);
                          }
                      }catch(Exception subexception){ 
                          //trying our best to cleanup, nothing to do if there is an exception
                          logger.error("Failed to delete the new folder being created during cleanup of failed copy folder - '"+destinationParentFolderNuxeo.getPath() + Constants.FORWARD_SLASH + sourceModel.getFileOrDirectoryName()+"'.", subexception);
                      }
                      
                      //push exception up for handling
                      throw new DetailedException(qe.getReason(), qe.getDetails(), qe);
                  }catch(Exception e){
                      String message = "while creating new files for a copy folder operation on '"+sourcePath+"' to '"+destinationPath+"'. ";
                      if (e instanceof QuotaExceededException) {
                          if (logger.isInfoEnabled()) {
                              logger.info("Quota exceeded " + message);
                          }
                      } else {
                          logger.error("Caught exception " + message,e);
                      }
                      
                      //attempting rollback
                      //Note: currently unable to cleanup files that were over-written during the copy operation since we don't keep any backups of the original files
                      //      therefore we have no solution for a merge of an existing folder
                      try{
                          if(!isSourceInDestination){
                              
                              //this method created the source folder in the destination folder so we can just delete it and along with it, any descendant files that were also created
                              updateProgress(progress, "An error occurred. Performing cleanup...", -1);
                              nuxeoInterface.deleteWorkspaceFolder(destinationParentFolderNuxeo.getPath() + Constants.FORWARD_SLASH + sourceModel.getFileOrDirectoryName(), null, username);
                              updateProgress(progress, null, 100);                
                          }
                      }catch(Exception subexception){ 
                          //trying our best to cleanup, nothing to do if there is an exception
                          logger.error("Failed to delete the new folder being created during cleanup of failed copy folder - '"+destinationParentFolderNuxeo.getPath() + Constants.FORWARD_SLASH + sourceModel.getFileOrDirectoryName()+"'.", subexception);
                      }
                      
                      //push exception up for handling
                      throw new DetailedException("Failed to copy the folder and it's contents to '"+workspaceRelativeDestinationParentFolderName+"'.", 
                              "An exception was thrown while copying the folder "+sourcePath+" of "+e.getMessage(), e);
                  }   
                  
                  String destinationFolderName = destinationPath;
                  return destinationFolderName + File.separator + sourceModel.getFileOrDirectoryName();
                  
              } else if(sourceFile.exists()){

                  //copy a file
                  InputStream inputStream = null;
                  try{
                      try{
                          inputStream = new FileInputStream(sourceFile);
                          
                      }catch(FileNotFoundException fnf){
                          throw new DetailedException("Unable to read the file to copy of '"+sourceFile+"'.",
                                  "The source "+sourceModel+" resulted in the path of '"+sourceFile+"' which doesn't exist.\n\n"
                                          + "Is there a special character in the file name that maybe isn't being encoded correctly in GIFT?  If so, remove those characters and try again.", fnf);
                      }
                      
                      return copyFileStream(username, inputStream, sourcePath, destinationPath, nameCollisionResolutionBehavior, progress, useAdminPrivilege);
                  
                  } catch(Throwable t){           
                      
                      //Austin's Note: Upon testing, the inputStream should be null if the move failed due to moving a large file,
                      //This handles if they have access to the workspace or if the file is just too large to move to the destination
                      if(inputStream == null && t instanceof DetailedException && !(((DetailedException)t).getCause() instanceof IOException)){
                          if (destinationModel.getRootFileTreeModel().getFileOrDirectoryName().equals(PUBLIC_WORKSPACE_FOLDER_NAME)){
                              throw new DetailedException("Failed to create the document because there was a problem while communicating with the server.",
                                      "Failed to create document named '"+destinationPath + Constants.FORWARD_SLASH + sourceModel.getFileOrDirectoryName()+"' because an IO exception was thrown by the Nuxeo interface",
                                      t);
                          }else{
                              throw new QuotaExceededException("You are running low on disk space. Please delete some files from your workspace before attempting to upload this file again.",
                                  "Failed to create the document because the disk space quota has been exceeded.", 
                                  t);
                          }
                      }
                      
                      throw t;
                      
                  }finally{
                      
                      if(inputStream != null){
                          try{
                              inputStream.close();
                          }catch(IOException e){
                              logger.error("Failed to close the input stream when copying '"+sourceModel+"' to '"+destinationModel+"' for "+username, e);
                          }
                      }

                  } 
                  
              } else {
                  throw new DetailedException("Unable to find the file to copy of '"+sourceFile+"'.",
                          "The source "+sourceModel+" resulted in the path of '"+sourceFile+"' which doesn't exist.\n\n"
                                  + "Is there a special character in the file name that maybe isn't being encoded correctly in GIFT?  If so, remove those characters and try again.", null);
              }
            
        } else {
            
            Document sourceDocument;
            try{
                sourceDocument = nuxeoInterface.getDocumentByName(sourcePath, username);
                updateProgress(progress, null, 10);
                
            }catch(IOException ioe){
                
                throw new DetailedException("Unable to find the file to copy of '"+sourcePath+"'.",
                        "The source "+sourceModel+" resulted in the path of '"+sourcePath+"' which doesn't exist.\n\n"
                                + "Is there a special character in the file name that maybe isn't being encoded correctly in GIFT?  If so, remove those characters and try again.", ioe);
            }
            
            if(nuxeoInterface.isFolder(sourceDocument) || nuxeoInterface.isWorkspace(sourceDocument)){
                
                //copy a folder
                String copiedFileName = sourceModel.getFileOrDirectoryName();
                
                //does the destination parent folder exist?
                String workspaceRelativeDestinationParentFolderName = destinationPath;
                Document destinationParentFolderNuxeo;
                try{
                    destinationParentFolderNuxeo = nuxeoInterface.getDocumentByName(workspaceRelativeDestinationParentFolderName, username);
                    updateProgress(progress, null, 10);
                }catch(IOException e){
                    // The destination folder doesn't exist and needs to be created. This happens when the user 
                    // is prompted to rename the file to be copied. 

                    copiedFileName = destinationModel.getFileOrDirectoryName();
                    workspaceRelativeDestinationParentFolderName = destinationModel.getParentTreeModel().getRelativePathFromRoot();
                    
                    try {
                        destinationParentFolderNuxeo =  nuxeoInterface.getDocumentByName(workspaceRelativeDestinationParentFolderName, username);
                    } catch (IOException ioe) {
                        throw new DetailedException("Failed to retrieve the destination folder of '"+workspaceRelativeDestinationParentFolderName+"' to copy the folder to.", 
                            "The destination folder to place the folder being copied must already exist.  There was an IO exception when trying to retrieve the destination document: "+e.getMessage(),
                                ioe);
                    }
                }
                
                //does the destination parent folder contain the source folder already?
                boolean isSourceInDestination;
                try{
                    
                    updateProgress(progress, "Checking for existing file...", 0);
                    if (NameCollisionResolutionBehavior.GUARANTEE_UNIQUE_NAME == nameCollisionResolutionBehavior) {
                        copiedFileName = generateUniqueName(
                                workspaceRelativeDestinationParentFolderName + File.separator + copiedFileName,
                                username, true);
                    }
                    isSourceInDestination = nuxeoInterface.documentExists(workspaceRelativeDestinationParentFolderName, copiedFileName, true, username);
                    updateProgress(progress, null, 5);
                
                }catch(IOException e){
                    throw new DetailedException("Failed to determine whether the destionation folder already contains the folder being copied.", 
                            "An IO exception occurred while checking if '"+workspaceRelativeDestinationParentFolderName + Constants.FORWARD_SLASH + sourceModel.getFileOrDirectoryName()+"' already exists.", e);
                }
                
                if(!isSourceInDestination || overwriteExistingFile){  
                    
                        if(!isSourceInDestination){
                            //create the source folder in the destination folder since it doesn't exist
                            try{
                                
                                updateProgress(progress, "Creating new folder...", 0);
                                nuxeoInterface.createWorkspaceFolder(workspaceRelativeDestinationParentFolderName, copiedFileName, username, false);
                                updateProgress(progress, null, 5);
                            
                            }catch(IOException e){
                                throw new DetailedException("Failed to create the folder named '"+copiedFileName+"'.", 
                                        "An IO exception occurred while trying to create the folder in '"+workspaceRelativeDestinationParentFolderName, e);
                            }
                        }
                }else{
                    
                    throw new FileExistsException(
                            workspaceRelativeDestinationParentFolderName + Constants.FORWARD_SLASH + sourceModel.getFileOrDirectoryName(), 
                            "A folder named '" + sourceModel.getFileOrDirectoryName() + "' already exists in " + workspaceRelativeDestinationParentFolderName + ""
                                    + "; therefore, " + sourcePath + " could not be copied to that location.",
                            "A folder named '" + sourceModel.getFileOrDirectoryName() + "' was detected in the destination folder "
                                    + "'" + workspaceRelativeDestinationParentFolderName + "' during a copy operation in which overwriting was disabled. "
                                    + "Since overwriting was disabled, the source folder '" + sourcePath + "' was not copied to its destination.",
                            null);
                }
                
                //Retrieve the course folder in order to update the last modified date value later on
                Document courseFolderDocument;
                try{
                    ServerFolderProxy courseFolderProxy = (ServerFolderProxy) getCourseFolder(destinationPath, username);
                    courseFolderDocument =  courseFolderProxy.getFolder();
                }catch(@SuppressWarnings("unused") Exception e){
                    
                    // When copying a course, we may potentially copy one of the course's subfolders before its course.xml is copied over, in which case
                    // the call to get the course folder will fail since the folder doesn't actually have a course.xml yet. To deal with this, we 
                    // won't bother trying to update the subfolder's containing course folder if we can't find it.
                    courseFolderDocument = destinationParentFolderNuxeo;
                }
                
                try{
                    //get all descendant documents under the source folder
                    updateProgress(progress, "Gathering files...", 0);          
                    Documents sourceFolderNuxeoDescendants = nuxeoInterface.getDocumentsAndFoldersByPath(sourcePath, null, username);
                    updateProgress(progress, null, 5);
                    
                    //
                    // first create folders - note the folders can come in any order, including descendant folders before ancestors.
                    //
                    updateProgress(progress, "Copying folders...", 0);
                    copyNuxeoFolderDocuments(sourceFolderNuxeoDescendants, copiedFileName, sourcePath, destinationParentFolderNuxeo, courseFolderDocument, username, true, false);
                    updateProgress(progress, null, 10);
                    
                    //
                    // second create files - creating a new document doesn't automatically create the ancestor folder structure, hence
                    //                       why folders were created first (above method call).
                    //
                    updateProgress(progress, "Copying files...", 0);
                    copyNuxeoFolderDocuments(sourceFolderNuxeoDescendants, copiedFileName, sourcePath, destinationParentFolderNuxeo, courseFolderDocument, username, false, true);
                    updateProgress(progress, null, 100);
                    
                }catch(QuotaExceededException qe){
                    logger.error("Caught quota exceeded exception while creating new files for a copy folder operation on '"+sourcePath+"' to '"+destinationPath+"'. ",qe);
                    
                    //attempting rollback
                    //Note: currently unable to cleanup files that were over-written during the copy operation since we don't keep any backups of the original files
                    //      therefore we have no solution for a merge of an existing folder
                    try{
                        if(!isSourceInDestination){
                            //this method created the source folder in the destination folder so we can just delete it and along with it, any descendant files that were also created
                            nuxeoInterface.deleteWorkspaceFolder(destinationParentFolderNuxeo.getPath() + Constants.FORWARD_SLASH + sourceModel.getFileOrDirectoryName(), null, username);
                        }
                    }catch(Exception subexception){ 
                        //trying our best to cleanup, nothing to do if there is an exception
                        logger.error("Failed to delete the new folder being created during cleanup of failed copy folder - '"+destinationParentFolderNuxeo.getPath() + Constants.FORWARD_SLASH + sourceModel.getFileOrDirectoryName()+"'.", subexception);
                    }
                    
                    //push exception up for handling
                    throw new DetailedException(qe.getReason(), qe.getDetails(), qe);
                }catch(Exception e){
                    String message = "while creating new files for a copy folder operation on '"+sourcePath+"' to '"+destinationPath+"'. ";
                    if (e instanceof QuotaExceededException) {
                        if (logger.isInfoEnabled()) {
                            logger.info("Quota exceeded " + message);
                        }
                    } else {
                        logger.error("Caught exception " + message,e);
                    }
                    
                    //attempting rollback
                    //Note: currently unable to cleanup files that were over-written during the copy operation since we don't keep any backups of the original files
                    //      therefore we have no solution for a merge of an existing folder
                    try{
                        if(!isSourceInDestination){
                            
                            //this method created the source folder in the destination folder so we can just delete it and along with it, any descendant files that were also created
                            updateProgress(progress, "An error occurred. Performing cleanup...", -1);
                            nuxeoInterface.deleteWorkspaceFolder(destinationParentFolderNuxeo.getPath() + Constants.FORWARD_SLASH + sourceModel.getFileOrDirectoryName(), null, username);
                            updateProgress(progress, null, 100);                
                        }
                    }catch(Exception subexception){ 
                        //trying our best to cleanup, nothing to do if there is an exception
                        logger.error("Failed to delete the new folder being created during cleanup of failed copy folder - '"+destinationParentFolderNuxeo.getPath() + Constants.FORWARD_SLASH + sourceModel.getFileOrDirectoryName()+"'.", subexception);
                    }
                    
                    //push exception up for handling
                    throw new DetailedException("Failed to copy the folder and it's contents to '"+workspaceRelativeDestinationParentFolderName+"'.", 
                            "An exception was thrown while copying the folder "+sourcePath+" of "+e.getMessage(), e);
                }   
                
                String destinationFolderName = destinationPath;
                return destinationFolderName + File.separator + copiedFileName;
                
            } else {
            
                //copy a file
                InputStream inputStream = null;
                
                try{
                    try{
                        NuxeoDocumentConnection connection = nuxeoInterface.getDocumentConnection(sourceDocument, username);
                        inputStream = connection.getInputStream();
                    }catch(IOException ioe){
                        throw new DetailedException("Unable to retrieve the file to copy of '"+sourcePath+"'.",
                                "The source "+sourceModel+" resulted in the path of '"+sourcePath+"' which can't be retrieved but does exist.", ioe);
                    }
                    
                    return copyFileStream(username, inputStream, sourcePath, destinationPath, nameCollisionResolutionBehavior, progress, useAdminPrivilege);
                    
                }catch(Throwable t){           
                    
                    //Austin's Note: Upon testing, the inputStream should be null if the move failed due to moving a large file,
                    //This handles if they have access to the workspace or if the file is just too large to move to the destination
                    if(inputStream == null && t instanceof DetailedException && !(((DetailedException)t).getCause() instanceof IOException)){
                        if (destinationModel.getRootFileTreeModel().getFileOrDirectoryName().equals(PUBLIC_WORKSPACE_FOLDER_NAME)){
                            throw new DetailedException("Failed to create the document because there was a problem while communicating with the server.",
                                    "Failed to create document named '"+destinationPath + Constants.FORWARD_SLASH + sourceModel.getFileOrDirectoryName()+"' because an IO exception was thrown by the Nuxeo interface",
                                    t);
                        }else{
                            throw new QuotaExceededException("You are running low on disk space. Please delete some files from your workspace before attempting to upload this file again.",
                                "Failed to create the document because the disk space quota has been exceeded.", 
                                t);
                        }
                    }
                    
                    throw t;
                    
                }finally{
                    
                    if(inputStream != null){
                        try{
                            inputStream.close();
                        }catch(IOException e){
                            logger.error("Failed to close the input stream when copying '"+sourceModel+"' to '"+destinationModel+"' for "+username, e);
                        }
                    }
    
                }
            }
        }
    }
    
    /**
     * Copies the contents of the input stream of the file at the given source path into to the workspace file at the given destination path.  
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
     * @param sourceStream the input steam of the file being copied that will be used to write the contents of the source
     * file into the destination file
     * @param sourcePath the path to the file to copy. Can be a file or directory. 
     * The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param destinationPath where to copy the file too.  Can be a file or directory.  
     * The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param nameCollisionresolutionBehavior the resolution to occur if there is a naming conflict
     *        while copying the file or folder.
     * @param progress the progress indicator for the copy operation. Can be null.
     * @param useAdminPrivilege True to to create the file with admin privileges.
     * @return String the file name including path of the copied file location
     * @throws FileExistsException if a file with the same file name as the source exists at the destination and overwriting is not enabled 
     * @throws DetailedException if there was a severe problem copying the file
     * @throws IllegalArgumentException if there was a problem with the method arguments
     */
    private String copyFileStream(String username, InputStream sourceStream, String sourcePath, String destinationPath,
            NameCollisionResolutionBehavior nameCollisionResolutionBehavior, ProgressIndicator progress, boolean useAdminPrivilege)
            throws DetailedException, IllegalArgumentException, FileExistsException {
        
        String createdFileName = null;

        String sourceFilename = sourcePath;

        FileTreeModel sourceModel = FileTreeModel.createFromRawPath(sourcePath);
        FileTreeModel destinationModel = FileTreeModel.createFromRawPath(destinationPath);

        String workspaceDestinationPath = destinationPath;

        String destinationParentPath = destinationModel.getParentTreeModel().getRelativePathFromRoot();

        // check if a file or folder document exists at the destination. If one
        // exists, overwrite the document accordingly
        try {

            // Retrieve the course folder in order to update the last modified
            // date value later on
            DocumentEntityType courseFolderDocumentEntityType;
            try {
                ServerFolderProxy courseFolderProxy = (ServerFolderProxy) getCourseFolder(destinationPath, username);
                courseFolderDocumentEntityType = new DocumentEntityType(courseFolderProxy.getFolder());

            } catch (@SuppressWarnings("unused") Exception e) {

                // When copying a course, we may potentially copy one of the
                // course's files before its course.xml is copied over, in which
                // case
                // the call to get the course folder will fail since the folder
                // doesn't actually have a course.xml yet. To deal with this, we
                // will fall back to alternatives where appropriate.
                courseFolderDocumentEntityType = null;
            }

            String courseFolderPath;
            if (courseFolderDocumentEntityType != null) {

                // get the path to destination course folder so its last
                // modified date can be updated
                courseFolderPath = courseFolderDocumentEntityType.getPath();

            } else {

                // if no course folder is found, update the last modified date
                // of the parent folder instead
                courseFolderPath = destinationParentPath;
            }

            if (nuxeoInterface.documentExists(destinationParentPath, destinationModel.getFileOrDirectoryName(),
                    false, username)) {

                // overwrite the destination file with the source file if
                // directed to do so
                Document destinationDocument;
                try {

                    updateProgress(progress, "Checking for existing file...", 0);
                    destinationDocument = nuxeoInterface.getDocumentByName(workspaceDestinationPath, username);

                } catch (IOException e) {
                    throw new DetailedException("Failed to retrieve the document '" + destinationPath + "'.",
                            "There was an IO exception when trying to retrieve the destination document: "
                                    + e.getMessage(),
                            e);
                }

                updateProgress(progress, null, 5);

                String uniqueName = destinationModel.getFileOrDirectoryName();
                    try {
                        updateProgress(progress, "Copying file...", 0);
                    switch (nameCollisionResolutionBehavior) {
                    case GUARANTEE_UNIQUE_NAME:
                        uniqueName = generateUniqueName(destinationParentPath + File.separator + uniqueName, username,
                                false);
                        nuxeoInterface.createDocument(destinationParentPath, uniqueName, courseFolderPath, sourceStream,
                                username, useAdminPrivilege);
                        break;
                    case OVERWRITE:
                        nuxeoInterface.createDocument(destinationParentPath, destinationModel.getFileOrDirectoryName(),
                                courseFolderPath, sourceStream, username, useAdminPrivilege);
                        break;
                    case FAIL_ON_COLLISION:
                    default:
                        throw new FileExistsException(destinationPath,
                                "A file already exists at " + destinationPath + "; therefore, " + sourceFilename
                                        + " could not be copied to " + "that location.",
                                "A file was detected at the destination '" + destinationPath
                                        + "' during a copy operation in which overwriting was "
                                        + "disabled. Since overwriting was disabled, the source file '" + sourceFilename
                                        + "' was not copied to " + "its destination.",
                                null);
                    }
                        updateProgress(progress, null, 100);
                    } catch (QuotaExceededException qe) {
                        throw new DetailedException(
                                "Failed to create the document because the disk space quota has been exceeded.",
                                "Failed to create document named '" + destinationDocument.getPath()
                                        + Constants.FORWARD_SLASH + destinationModel.getFileOrDirectoryName() + "'.",
                                qe);
                    } catch (IOException ioe) {
                        throw new DetailedException(
                                "Failed to create the document because there was a problem while communicating with the server.",
                                "Failed to create document named '" + destinationDocument.getPath()
                                        + Constants.FORWARD_SLASH + destinationModel.getFileOrDirectoryName()
                                        + "' because an IO exception was thrown by the Nuxeo interface: "
                                        + ioe.getMessage(),
                                ioe);
                    }

                createdFileName = destinationParentPath + Constants.FORWARD_SLASH + uniqueName;

            } else if (nuxeoInterface.documentExists(destinationParentPath, destinationModel.getFileOrDirectoryName(), true,
                    username)) {

                // copy the source file to the destination folder
                Document destinationDocument;
                try {
                    updateProgress(progress, "Checking for existing file...", 0);
                    destinationDocument = nuxeoInterface.getDocumentByName(workspaceDestinationPath, username);
                } catch (IOException e) {
                    throw new DetailedException("Failed to retrieve the document '" + destinationPath + "'.",
                            "There was an IO exception when trying to retrieve the destination document: "
                                    + e.getMessage(),
                            e);
                }

                updateProgress(progress, null, 5);

                String uniqueName = sourceModel.getFileOrDirectoryName();
                try {
                    // check if a file with the same name as source is already
                    // in the destination folder, if so only overwrite it if
                    // directed to do so
                    if (!nuxeoInterface.documentExists(workspaceDestinationPath, sourceModel.getFileOrDirectoryName(),
                            false, username)) {

                        updateProgress(progress, COPY_FILE_PROGRESS_DESC, 0);
                        nuxeoInterface.createDocument(destinationDocument.getPath(),
                                sourceModel.getFileOrDirectoryName(), courseFolderPath, sourceStream, username, useAdminPrivilege);
                        updateProgress(progress, null, 100);

                    } else {

                            updateProgress(progress, COPY_FILE_PROGRESS_DESC, 0);
                        switch (nameCollisionResolutionBehavior) {
                        case GUARANTEE_UNIQUE_NAME:
                            uniqueName = generateUniqueName(workspaceDestinationPath + File.separator
                                    + uniqueName, username, false);
                            nuxeoInterface.createDocument(destinationDocument.getPath(), uniqueName, courseFolderPath,
                                    sourceStream, username, useAdminPrivilege);
                            break;
                        case OVERWRITE:
                            nuxeoInterface.createDocument(destinationDocument.getPath(),
                                    sourceModel.getFileOrDirectoryName(), courseFolderPath, sourceStream, username,
                                    useAdminPrivilege);
                            break;
                        case FAIL_ON_COLLISION:
                        default:
                            throw new FileExistsException(destinationPath + "/" + sourceModel.getFileOrDirectoryName(),
                                    "A file named '" + sourceModel.getFileOrDirectoryName() + "' already exists in "
                                            + destinationPath + "; therefore, " + sourceFilename
                                            + " could not be copied to that location.",
                                    "A file named '" + sourceModel.getFileOrDirectoryName()
                                            + "' was detected in the destination folder " + "'" + destinationPath
                                            + "' during a copy operation in which overwriting was disabled. "
                                            + "Since overwriting was disabled, the source file '" + sourceFilename
                                            + "' was not copied to " + "its destination.",
                                    null);
                        }
                        updateProgress(progress, null, 100);
                    }
                } catch (QuotaExceededException qe) {
                    throw new QuotaExceededException(
                            "You are running low on disk space. Please delete some files from your workspace before attempting to upload this file again.",
                            "Failed to create the document because the disk space quota has been exceeded.", qe);
                    
                } catch (FileExistsException ioe) {
                    
                    //let FileExistsExeptions bubble up so the caller can receive them and potentially prompt for an overwrite
                    throw ioe;
                    
                } catch (IOException ioe) {
                    throw new DetailedException(
                            "Failed to create the document because there was a problem while communicating with the server.",
                            "Failed to create document named '" + destinationDocument.getPath()
                                    + Constants.FORWARD_SLASH + sourceModel.getFileOrDirectoryName()
                                    + "' because an IO exception was thrown by the Nuxeo interface",
                            ioe);
                } catch (DetailedException de) {
                    throw new DetailedException("Failed to create the document: " + de.getReason(),
                            "Failed to create document named '" + destinationDocument.getPath()
                                    + Constants.FORWARD_SLASH + sourceModel.getFileOrDirectoryName() + "' : "
                                    + de.getMessage(),
                            de);
                }

                // build file tree from
                String destinationFolderName = destinationModel.getRelativePathFromRoot();
                createdFileName = destinationFolderName + Constants.FORWARD_SLASH + uniqueName;

            } else {
                try {

                    updateProgress(progress, "Copying file...", 0);

                    nuxeoInterface.createDocument(destinationParentPath, destinationModel.getFileOrDirectoryName(),
                            courseFolderPath, sourceStream, username, useAdminPrivilege);
                    updateProgress(progress, null, 100);
                    createdFileName = destinationModel.getRelativePathFromRoot();

                } catch (QuotaExceededException qe) {
                    throw new DetailedException(
                            "Failed to create the document because the disk space quota has been exceeded.",
                            "Failed to create document named '" + destinationParentPath + Constants.FORWARD_SLASH
                                    + destinationModel.getFileOrDirectoryName() + "'.",
                            qe);
                } catch (IOException ioe) {
                    throw new DetailedException(
                            "Failed to create the document because there was a problem while communicating with the server.",
                            "Failed to create document named '" + destinationParentPath + Constants.FORWARD_SLASH
                                    + destinationModel.getFileOrDirectoryName()
                                    + "' because an IO exception was thrown by the Nuxeo interface: "
                                    + ioe.getMessage(),
                            ioe);
                }
            }
        } catch (IOException e) {
            throw new DetailedException(
                    "Failed to determine whether the destination exists as a file or folder or doesn't exist at all.",
                    "An IO exception occurred while checking the destionation of '" + destinationParentPath
                            + Constants.FORWARD_SLASH + destinationModel.getFileOrDirectoryName() + "'.",
                    e);
        }
        
        return createdFileName;
    }

    @Override
    public void updateCourseUserPermissions(String username, Set<DomainOptionPermissions> permissions, DomainOption courseData, String userSessionId, ProgressIndicator progressIndicator)
            throws IllegalArgumentException, DetailedException {

        try {
            // get workspaces/username folder
            Document usersWorkspaceDocument = getUserPrivateRootFolder(username);

            // get workspaces/username/course folder
            Document courseFolderDoc = nuxeoInterface.getFolderByName(usersWorkspaceDocument.getPath(), courseData.getDomainName(), username);
            
            Document courseXMLDoc = nuxeoInterface.getDocumentByName(courseData.getDomainId(), username);

            nuxeoInterface.setUserCoursePermissions(courseFolderDoc, courseXMLDoc, permissions, progressIndicator);

        } catch (IOException e) {
            throw new DetailedException("Failed to update the course permissions",
                    "There was a server side exception when updating the permissions for the course named '+domainName+' with new permissions of:\n"+permissions+".\n\nError reads:\n"+e.toString(),
                        e);
        }
    }
}