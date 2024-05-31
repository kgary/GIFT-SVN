/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.io.AsyncOperationManager;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileExistsException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.net.nuxeo.QuotaExceededException;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.CreateSlideShow;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.CreateSlideShowResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.UnzipFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.UnzipFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyWorkspaceFilesResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgress.ProgressType;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import mil.arl.gift.tools.services.file.AbstractFileServices.NameCollisionResolutionBehavior;

/**
 * This class handles requests to perform file operations and monitor progress on a per-user basis.
 * 
 * @author bzahid
 */
public class FileOperationsManager {

	private static Logger logger = LoggerFactory.getLogger(FileOperationsManager.class.getName());
	
	/** A date formatter used to help generate names for extraction folders when unzipping files */
	private static final FastDateFormat UNZIP_FOLDER_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd_HH-mm-ss", null, null);
	
	/** A mapping from each username to the progress of their currently running delete process, if one exists */
	private ConcurrentHashMap<String, ProgressIndicator> usernameToDeleteProgress = new ConcurrentHashMap<String, ProgressIndicator>();
	
	/** A mapping from each username to the progress of their currently running copy process, if one exists */
	private ConcurrentHashMap<String, ProgressIndicator> usernameToCopyProgress = new ConcurrentHashMap<String, ProgressIndicator>();

	/** A mapping from each username to the progress of their currently running create slide show process, if one exists */
	private ConcurrentHashMap<String, ProgressIndicator> usernameToSlideShowProgress = new ConcurrentHashMap<String, ProgressIndicator>();
	
	/** 
	 * A mapping from each username to the progress of their currently running create slide show process, if one exists. The server
	 * call invoking the operation works asynchronously, so a {@link LoadedProgressIndicator} is used to pass the result when
	 * the operation finishes.
	 */
	private ConcurrentHashMap<String, LoadedProgressIndicator<UnzipFileResult>> usernameToUnzipProgress = new ConcurrentHashMap<String, LoadedProgressIndicator<UnzipFileResult>>();
    
	/** Singleton instance of this class. */
	private static FileOperationsManager instance = null;
	
	/**
	 * Creates a new file operations manager
	 */
	private FileOperationsManager() {
		
	}
	
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return the singleton instance of this class.
	 */
	public static FileOperationsManager getInstance() {
		if(instance == null) {
			instance = new FileOperationsManager();
		}
		
		return instance;
	}
	
	/**
	 * Deletes the specified files in the user's workspace.
	 * 
	 * @param username The user performing the operation
	 * @param browserSessionKey The unique identifier of the browser that is performing the operation
	 * @param workspacePaths The paths to delete
	 * @return GatServiceResult
	 */
	public GatServiceResult deleteWorkspaceFiles(String username, String browserSessionKey, List<String> workspacePaths) {
		
		ProgressIndicator progress = new ProgressIndicator(0, "Initializing request...");
		GatServiceResult result = new GatServiceResult();
				
		usernameToDeleteProgress.put(username, progress);
		
		try{
			
			progress.setTaskDescription("Locating file on the server...");
			progress.increasePercentComplete(10);
			
			StringBuilder errorMsg = new StringBuilder();
			
			boolean oneSucceeded = false;
			
			for(String path: workspacePaths){
				
				try{			
					
					boolean success = ServicesManager.getInstance().getFileServices().deleteFile(
							username, browserSessionKey, path, progress);
					
					if(success){
						
						if(!oneSucceeded){
							oneSucceeded = true;
						}
						
					}
				
				} catch(Exception thrown){
					//If the cause of this exception is an IOException, then it should have been thrown because
					//the user tried to delete something they do not have permission to, so we can add it to the error message. 
					errorMsg.append("Could not delete ").append(path).append("." 
							+ ((thrown.getCause() != null && thrown.getCause() instanceof IOException) ? 
									" You may not have permission to delete this file or folder." : ""));
					result.setErrorDetails(thrown.getMessage());
					result.setErrorStackTrace(DetailedException.getFullStackTrace(thrown));
				}
				
			}
			
			String errorString = errorMsg.toString();
			
			if(errorString.isEmpty()){
				
				result.setSuccess(true);
				
			} else {
				
				if(oneSucceeded){
					errorString = errorString + "All other files were successfully deleted.";
				}
				
				result.setSuccess(false);
				result.setErrorMsg(errorString);
			}
			
			
		} catch (Exception thrown){
			
			logger.warn("deleteFiles - An exception occurred while deleting files for user '" + username + "'.", thrown);
			
			result.setSuccess(false);
			result.setErrorMsg("Could not delete file");
			result.setErrorDetails(thrown.getMessage());
			result.setErrorStackTrace(DetailedException.getFullStackTrace(thrown));
		}
		
		usernameToDeleteProgress.remove(username);
		
		return result;
	}

	/**
	 * Copies the specified files in the user's workspace.
	 * 
	 * @param username The user performing the operation.
	 * @param browserSessionKey The unique identifier of the browser that is performing the operation
	 * @param sourcePathsToTargetPaths A map of source file paths to target file paths.
	 * @return GatServiceResult
	 */
	public CopyWorkspaceFilesResult copyWorkspaceFiles(String username, String browserSessionKey, Map<String, String> sourcePathsToTargetPaths, boolean overwriteExisting, boolean cleanUpOnFailure) {
		
		AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
        ProgressIndicator progress = new ProgressIndicator(0, "Initiating request...");
        CopyWorkspaceFilesResult result = new CopyWorkspaceFilesResult();
        
        usernameToCopyProgress.put(username, progress);
        
		try{	
			
			progress.setTaskDescription("Locating file on the server...");
			progress.setPercentComplete(10);
			
			List<String> copiedFiles = new ArrayList<String>();
			
			try{
			
				for(String sourcePath : sourcePathsToTargetPaths.keySet()){
					
					progress.setPercentComplete(20);
					
					String targetPath = sourcePathsToTargetPaths.get(sourcePath);
			
					progress.setPercentComplete(30);
					
					String copiedPath = "";
					
					try{
                        NameCollisionResolutionBehavior collisionResolution = overwriteExisting
                                ? NameCollisionResolutionBehavior.OVERWRITE
                                : NameCollisionResolutionBehavior.FAIL_ON_COLLISION;
						 copiedPath = fileServices.copyWorkspaceFile(username, sourcePath, targetPath, collisionResolution, progress);
						 copiedFiles.add(copiedPath);
					    
					} catch(@SuppressWarnings("unused") FileExistsException e){
						result.addCopyFailure(sourcePath, targetPath);
					}
					
					// Get the file tree model of the copiedPath and add it to the result.
					if(copiedPath != null){
						
						FileTreeModel copiedModel = FileTreeModel.createFromRawPath(copiedPath);
						result.addCopiedFile(copiedModel);
					}
					
				}				
				
				if(result.getCopyFailures().isEmpty()){
					result.setSuccess(true);
					
				} else {
					
					String exceptionMessage;
					
					if(sourcePathsToTargetPaths.size() == 1){
						exceptionMessage = "Could not copy a file because it already exists at its target location.";
						
					} else {
						exceptionMessage = "Could not copy files because one or more files already exists at their target locations.";
					}
					
					throw new Exception(exceptionMessage);
				}
				
			} catch(Exception thrown){
				
				if(cleanUpOnFailure){
				
					try{
						
						progress.setPercentComplete(0);
						progress.setTaskDescription("Operation Failed. Performing cleanup...");
						
						for(String filePath : copiedFiles){
							
							progress.setPercentComplete(50);
							fileServices.deleteFile(username, browserSessionKey, filePath, null);
							progress.setPercentComplete(100);
						}
						
					} catch(Exception subThrown){
						logger.warn("copyFiles - An exception occurred while cleaning up after failure.", subThrown);
					}
				}
				
				throw thrown;
			}
			
		} catch(DetailedException de){
			logger.warn("copyFiles - An exception occurred while copying files for user '" + username + "'.", de);
			
			result.setSuccess(false);
			
			//This if else block handles files being copied into the public workspace in server mode. This way,
			//if a very large file is attempted to be copied to Public workspace, then access issues will be the problem, not quota exceeded
			if(de.getCause() != null && de.getCause() instanceof IOException){
				result.setErrorMsg("Failed to copy the file. You may not have write access to the destination workspace.");
			}
			else if(de.getCause() != null && de.getCause() instanceof QuotaExceededException){
				result.setErrorMsg(((QuotaExceededException) de.getCause()).getReason());
			}
			else{
				result.setErrorMsg(de.getReason());
			}
			
			result.setErrorDetails(de.getMessage());
			result.setErrorStackTrace(DetailedException.getFullStackTrace(de));
			
		} catch (Exception thrown){
			
			logger.warn("copyFiles - An exception occurred while copying files for user '" + username + "'.", thrown);
			
			result.setSuccess(false);
			result.setErrorMsg("An error occurred while copying files.");
			result.setErrorDetails(thrown.getMessage());
			result.setErrorStackTrace(DetailedException.getFullStackTrace(thrown));
		}
		
		// reset progress for the next operation
		usernameToCopyProgress.remove(username);
		
		return result;
	}
	
	public CreateSlideShowResult createSlideShow(String username, String browserSessionKey, CreateSlideShow action) {
		
		CreateSlideShowResult result = new CreateSlideShowResult();
		ProgressIndicator progress = new ProgressIndicator();
		
		try {
			
		    /*
		     * E.g. [Desktop] will be pointing to the 'Slide Shows' folder that GIFT creates to place slide show images in.
		     * The ancestor parent file tree model might look like 'workspace/mhoffman/test/' where 'test'
		     * is the course folder name.
		     */
			FileTreeModel slideShowFolder = ServicesManager.getInstance().getFileServices().getSlideShowsFolder(username, action.getCourseFolderPath());
			
			/* usually null if the slide show course object has never had a slide show uploaded for it, meaning a sub-folder to 'Slide Shows'
			 * folder was not created yet with the slide show course object name.  In this case slidesFolder will be set later in this method.
			 * When not null, E.g. [Desktop] will be pointing to the <course object name> for the slide show 
			 * course object with ancestor parent file tree model like 'workspace/mhoffman/test/Slide Shows/' where 'test'
			 * is the course folder name and 'Slide Shows' is the fixed folder name that GIFT creates when creating slide shows images. */
			FileTreeModel slidesFolder = slideShowFolder.getModelFromRelativePath(action.getCourseObjectName(), false);
			
			progress.setTaskDescription("Creating Slide Show...");
			progress.increasePercentComplete(10);
			
			if(!(slidesFolder != null && action.shouldCopyExistingSlideShow() && slidesFolder.getRelativePathFromRoot(true).equals(action.getPptFilePath()))) {
				// If the Slide Show doesn't already exist in the course folder, copy it
						
				if(slidesFolder != null && !action.shouldReplaceExisting()) {
					// Report a Slide Show name conflict
					
					usernameToSlideShowProgress.remove(username);
					
					result.setSuccess(false);
					result.setHasNameConflict(true);
					result.setNameConflict(slidesFolder.getFileOrDirectoryName());
					return result;
					
				} 
				
				usernameToSlideShowProgress.put(username, progress);
				
				if(action.shouldCopyExistingSlideShow()) { 
					
					// Make sure the Slide Shows folder exists before copying the existing slide show folder to it
					ServicesManager.getInstance().getFileServices().createFolder(
							username, 
							action.getCourseFolderPath(),
							"Slide Shows", 
							true);
					
					// Copy the target folder to the Slide Shows directory
					String targetPath = slideShowFolder.getRelativePathFromRoot(true);
					
					String pptFilePath = action.getPptFilePath();
					
					if(pptFilePath == null || pptFilePath.isEmpty()){
						throw new DetailedException("An error occurred while copying the slide show. The original slide show"
								+ "needed to copy the slides could not be found.", 
								"The path to the slide show being copied cannot be null or empty.",
								null);
						
					} else if(pptFilePath.endsWith(Constants.FORWARD_SLASH)){
						
						//remove trailing slashes from the PowerPoint file path for file operations
						pptFilePath = pptFilePath.substring(0, pptFilePath.lastIndexOf(Constants.FORWARD_SLASH));
					}
					
					if(!pptFilePath.endsWith(action.getCourseObjectName())) {
						// Rename the folder if necessary
						targetPath += Constants.FORWARD_SLASH + action.getCourseObjectName();
					}
					
					progress.increasePercentComplete(10);
					HashMap<String, String> sourceToTargetMap = new HashMap<String, String>();
					sourceToTargetMap.put(pptFilePath, targetPath);
					
					progress.setTaskDescription("Copying Slides...");
					progress.increasePercentComplete(10);
					CopyWorkspaceFilesResult copyResult = FileOperationsManager.getInstance().copyWorkspaceFiles(username, browserSessionKey, sourceToTargetMap, action.shouldReplaceExisting(), true);
					if(copyResult.isSuccess()) {
						slidesFolder = ServicesManager.getInstance().getFileServices().getSlideShowsFolder(username, action.getCourseFolderPath()).
								getModelFromRelativePath(action.getCourseObjectName(), true, true);
						progress.increasePercentComplete(30);
						
					} else {
						
						result.setSuccess(false);
						result.setErrorDetails(copyResult.getErrorDetails());
						result.setErrorMsg("An error occurred while copying the Slide Show folder. " + copyResult.getErrorMsg());
						result.setErrorStackTrace(copyResult.getErrorStackTrace());
						
						return result;
					}
	
				} else {
					
					// Convert the slides into images
					progress.increasePercentComplete(10);
					progress.setTaskDescription("Converting PowerPoint file...");
					/* E.g. [Desktop] will be pointing to the <course object name> for the slide show 
					 * course object with ancestor parent file tree model like 'workspace/mhoffman/test/Slide Shows/' where 'test'
					 * is the course folder name and 'Slide Shows' is the fixed folder name that GIFT creates when creating slide shows images. */
					slidesFolder = ServicesManager.getInstance().getFileServices().convertPptToSlideShow(
							username, browserSessionKey, action.getCourseFolderPath(), action.getCourseObjectName(), action.getPptFilePath(), action.shouldReplaceExisting(), progress);
					progress.increasePercentComplete(10);
				}
			}
			
			// Add the copied images to the relative paths list for the lesson material course object
			ArrayList<String> slideShowFiles = new ArrayList<String>();
			for(FileTreeModel file : slidesFolder.getSubFilesAndDirectories()) {
				if(!file.isDirectory()) {
					// Need a path relative to the course folder
				    // e.g. 'mhoffman/test\Slide Shows\slide show' where 
				    //  'test' is the course folder, 'Slide Shows' is the fixed GIFT created folder, 'slide show' is the course object named folder
					String relativePath = file.getRelativePathFromRoot(true);
					relativePath = relativePath.substring(action.getCourseFolderPath().length() + 1);
					slideShowFiles.add(relativePath);
				}
			}
			
			progress.setPercentComplete(100); 
			result.setSuccess(true);
			result.setRelativeSlidePath(slideShowFiles);
			result.setSlidesFolderModel(slidesFolder);
			
		} catch (DetailedException e) {
			result.setSuccess(false);
			result.setErrorMsg(e.getReason());
			result.setErrorDetails(e.getDetails());
			result.setErrorStackTrace(e.getErrorStackTrace());
			
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorMsg("An error occurred while creating the slide show.");
			result.setErrorDetails(e.getMessage());
			result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
		}
		
		usernameToSlideShowProgress.remove(username);
		return result;
	}
	
	/**
	 * Retrieves a user's progress for a type of file operation.
	 * 
	 * @param progressType The type of progress to retrieve.
	 * @param username The user performing the operation.
	 * @return the progress indicator for the task.
	 */
	public ProgressIndicator getProgress(ProgressType progressType, String username) {
		
		switch(progressType) {	
		
		case DELETE:
			return usernameToDeleteProgress.get(username);
			
		case SLIDE_SHOW:
			return usernameToSlideShowProgress.get(username);
			
		case UNZIP:
			
			ProgressIndicator progress = usernameToUnzipProgress.get(username);
			
			if(progress.isComplete() || progress.getException() != null){
				
				//remove the mapping once the unzip operation completes
				usernameToUnzipProgress.remove(username);
			}
			
			return progress;
			
		default:
			return null;
		}
	}

	/**
	 * Unzips an archive (ZIP) file to a course folder based on the given action
	 * 
	 * @param action the action conting the details of the unzipping operation
	 * @return the result of the operation containing a model of the extracted files
	 */
	public GatServiceResult unzipFile(final UnzipFile action) {
		
		if(action == null){
			throw new IllegalArgumentException("The action containing the details of the unzipping operation cannot be null");
		}
		
		final String username = action.getUsername();
		GatServiceResult result = new GatServiceResult();
		
		final LoadedProgressIndicator<UnzipFileResult> progress = new LoadedProgressIndicator<UnzipFileResult>();
			
		progress.setTaskDescription("Unzipping Archive File...");
		progress.increasePercentComplete(10);
		
		usernameToUnzipProgress.put(username, progress);
		
		//need to handle the unzip asynchronously so the server call doesn't time out. The results will be passed in the progress updates.
		Runnable operation = new Runnable() {
			
			@Override
			public void run() {
				
				try {
				
					//construct extraction folder name using current time information
					
					StringBuilder extractFolderName = new StringBuilder();
					
					String zipFileName = action.getZipFilePath().substring(
							action.getZipFilePath().lastIndexOf("/") + 1, 
							action.getZipFilePath().lastIndexOf(".")
					);
					
					extractFolderName.append(zipFileName);		
		    		extractFolderName.append("_");		
		    		extractFolderName.append(UNZIP_FOLDER_FORMAT.format(new Date()));
					
		    		// unzip the file and extract its contents
					FileTreeModel extractionFolder = ServicesManager.getInstance().getFileServices().unzipFile(
							username, 
							action.getCourseFolderPath(), 
							extractFolderName.toString(), 
							action.getZipFilePath(), 
							progress
					);
					
					progress.setPercentComplete(100); 
					
					UnzipFileResult payload = new UnzipFileResult();
					payload.setSuccess(true);
					payload.setUnzippedFolderModel(extractionFolder);
					
					progress.setPayload(payload);
					progress.setComplete(true);
				
				} catch (DetailedException e) {
					progress.setException(e);
					
				} catch (Exception e) {
					
					progress.setException(
						new DetailedException(
							"An error occurred while extracting the contents of the ZIP archive.", 
							e.getMessage(), 
							e
						)
					);
				}
			}
		};
		
		AsyncOperationManager.getInstance().startAsyncOperation("unzipFileThread-" + username, operation);
		
		result.setSuccess(true);
		
		return result;
	}
}
