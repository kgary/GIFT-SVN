/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services.file;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.io.AsyncOperationManager;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.ServicesProperties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object that handles requests to import courses and monitors their progress on a per-user basis
 * 
 * @author nroberts
 */
public class ImportManager {
	
	private static Logger logger = LoggerFactory.getLogger(ImportManager.class);
	
	/** A mapping from each username to the status of their currently running import process, if one exists */
	private ConcurrentHashMap<String, LoadedProgressIndicator<List<DomainOption>>> usernameToImportStatus = new ConcurrentHashMap<String, LoadedProgressIndicator<List<DomainOption>>>();
	
	private static final String SURVEY_WEB_RESOURCES_DIRECTORY = ServicesProperties.getInstance().getSurveyImageUploadDirectory();
	
	/** Singleton instance of this class*/
	private static ImportManager singleton = null;
	
	/** 
	 * Creates a new export manager
	 */
	private ImportManager(){
		
		//start the asynchronous operation manager, since we need to pass the import progress/results asynchronously
		AsyncOperationManager.getInstance();
	}
	
	/**
	 * Imports an export file at the given location for the given user.
	 * 
	 * @param username the username of the user for whom the import is being executed
	 * @param exportFileLocation the server-side location of the export file to import
	 * @param filesToOverwrite a list of filenames that determines which existing survey 
	 * images will be overwritten by imported survey images. Can be null.
	 * @throws Exception if there was a problem reading the zip or importing the files into this GIFT instance
	 */
	public void importCourses(final String username, final String exportFileLocation, final List<String> filesToOverwrite, final Map<String, String> courseToNameMap) throws Exception{
		
	    if(logger.isInfoEnabled()){
	        logger.debug("Starting import thread for user " + username + " on import file "+exportFileLocation);
	    }

		// Launch the import in a separate thread so that we don't leave the client waiting for a response. The results of this
		// operation will be passed to clients as they poll for this operation. Handling the results asynchronously like this allows
		// us to avoid timing out the browser on long-running RPC operations.
        Runnable importOperation = new Runnable() {

            @Override

            public void run(){
            	
            	LoadedProgressIndicator<List<DomainOption>> progress = new LoadedProgressIndicator<List<DomainOption>>();
            	progress.setPercentComplete(0);
            	progress.setTaskDescription("Overall Import");
        		
        		usernameToImportStatus.put(username, progress);
        		
        		File exportFileZip = null;
        		try {
        		
            		//construct export file name using current time and user information
            		LocalDateTime currentDate = LocalDateTime.now();
            		
            		StringBuilder importFileName = new StringBuilder();
            		importFileName.append("import_");		
            		importFileName.append(currentDate.getYear());
            		importFileName.append("-");
            		importFileName.append(currentDate.getMonthValue());
            		importFileName.append("-");
            		importFileName.append(currentDate.getDayOfMonth());
            		importFileName.append("_");
            		importFileName.append(currentDate.getHour());
            		importFileName.append("-");
            		importFileName.append(currentDate.getMinute());
            		importFileName.append("-");
            		importFileName.append(currentDate.getSecond());
            		importFileName.append("_");
            		importFileName.append(username);
            		
            		exportFileZip = new File(CommonProperties.getInstance().getImportDirectory() + exportFileLocation);
            		
            		int timeout = 60000;
            		
            		/* Wait a maximum of 60 seconds if the user's export file has not finished being copied 
            		 * to the server by common.gwt.server.FileServlet.java */
            		synchronized(exportFileZip){
            		
            			while(timeout > 0){
            				
            				if(exportFileZip.exists()){
            					break;
            					
            				} else {
            					
            					int waitTime = 1000;
            					
            					try {
            						
            						exportFileZip.wait(waitTime);
            						
            						timeout -= waitTime;
            						
            					} catch (@SuppressWarnings("unused") InterruptedException e) {
            						logger.warn("An exception occurred while waiting for export file '" + exportFileLocation + "' to be uploaded for user '" + username + "'");
            						
            					}
            				}
            			}
            		}
            		
            		if(!exportFileZip.exists()){	            					
            			
            			logger.warn("Failed to import export at location '" + exportFileLocation + "' for user '" + username + "'.");
            			throw new IllegalArgumentException("The location of the export file to import does not point to an existing file.");			
            		}
            		
            		try{
            			
            		    ServicesManager.getInstance().getFileServices().importCourses(username, exportFileZip, progress, filesToOverwrite, courseToNameMap);
            		    
            			//let any clients that are polling for this import's progress know what the import is complete
            			progress.setComplete(true);
            			
            		} finally {
            			deleteImportFile(CommonProperties.getInstance().getImportDirectory() + exportFileLocation);
            		}
            		
        		} catch (Throwable t) {
        			
                    logger.error("Caught exception running thread: ", t);
                    
                    // A result MUST be returned for the operation to be considered 'finished'.
                    progress.setComplete(true); 
                    
                    String filename;
                    if(exportFileZip == null){
                        filename = exportFileLocation;
                    }else{
                        filename = exportFileZip.getName();
                    }
                    
                    progress.setException(
	                   new DetailedException(
	                    		"The server had a problem importing the GIFT course export file "+filename, 
	                    		"This was the first problem encountered during the import process and it needs to be solved before the import can be tried again.\n\nPlease try to fix the course export or if you need help please use the forums at gifttutoring.org.", 
	                    		t
	                    )
                    );       		
        		}
            }
        };
        
        AsyncOperationManager.getInstance().startAsyncOperation("importCourseThread-" + username,  importOperation);
	}

	/**
	 * Imports an export file at the given location for the given user.
	 * 
	 * @param username the username of the user for whom the import is being executed
	 * @param exportFileLocation the server-side location of the export file to import
	 * @throws Exception if there was a problem reading the zip or importing the files into this GIFT instance
	 */
	public void importCourses(String username, final String exportFileLocation) throws Exception{
		importCourses(username, exportFileLocation, null, null);		
	}
	
	/**
	 * Checks an export file for survey image conflicts upon import.
	 * 
	 * @param username the username of the user performing the import
	 * @param exportFileLocation the server-side location of the export file to import
	 * @return a map of file conflicts. Can be null or empty.
	 */
	public Map<File, File> checkForConflicts(String username, String exportFileLocation) {
		
		File exportFileZip = new File(CommonProperties.getInstance().getImportDirectory() + exportFileLocation);
		
		LoadedProgressIndicator<List<DomainOption>> progress = new LoadedProgressIndicator<List<DomainOption>>();
        progress.setPercentComplete(0);
        progress.setTaskDescription("Overall Import");
		
		usernameToImportStatus.put(username, progress);
		
		int timeout = 60000;

		/* Wait a maximum of 60 seconds if the user's export file has not finished being copied 
		 * to the server by common.gwt.server.FileServlet.java */
		synchronized(exportFileZip){

			while(timeout > 0){

				if(exportFileZip.exists()){
					break;

				} else {

					int waitTime = 1000;

					try {

						exportFileZip.wait(waitTime);
						timeout -= waitTime;

					} catch (@SuppressWarnings("unused") InterruptedException e) {
						logger.warn("An exception occurred while waiting for export file '" + exportFileLocation + "' to be uploaded for user '" + username + "'");

					}
				}
			}
		}

		if(!exportFileZip.exists()) {

			logger.warn("Failed to import the export file at location '" + exportFileLocation + "' for user '" + username + "'.");
			throw new IllegalArgumentException("The location of the GIFT course export to import does not point to an existing file.  Does the file '"+exportFileLocation+"' exist?\n\n"
			        + "Are there special characters in the file name that GIFT isn't handling correctly?  If so, remove those characters and try again.");			
		}

		return ServicesManager.getInstance().getFileServices().checkForImportConflicts(username, exportFileZip, progress);
	}
	
	/**
	 * Gets the status of the import being run for the specified user
	 * 
	 * @param username the username of the user to get the status for
	 * @return the status of the export.  Returns null if there is no import progress object
	 * for the username. When the import is finished, it will contain the list of courses imported.
	 */
    public LoadedProgressIndicator<List<DomainOption>> getImportStatus(String username){
		
	    LoadedProgressIndicator<List<DomainOption>> importStatus = usernameToImportStatus.get(username);
		
		if(importStatus != null){
		    
            if(importStatus.isComplete()){
                
                //clear out the import status once the operation is complete to prepare for further imports
                usernameToImportStatus.remove(username);
            }
			
            return importStatus;
		}
		
		return null;
	}
	
	/**
	 * Cancels the import being run for the given user, if one exists
	 * 
	 * @param username the username of the user for whom to cancel the import
	 */
	public void cancelImport(String username){
		
		if(username == null){
			throw new IllegalArgumentException("Username cannot be null");
		}
		
		LoadedProgressIndicator<List<DomainOption>> importStatus = usernameToImportStatus.get(username);
		importStatus.setShouldCancel(true);
	}
	
	/**
	 * Deletes the file used to generate an import
	 * 
	 * @param exportFileLocation the server-side location of the file to delete
	 * @return whether or not the delete was successful
	 */
	public boolean deleteImportFile(String exportFileLocation){
		
		boolean success = true;
		
		if(exportFileLocation == null || !exportFileLocation.endsWith(".zip")){
			success = false;
			
		} else {
			
			File file = new File(exportFileLocation);
			
			if(file.getParent() != CommonProperties.getInstance().getImportDirectory()) {
				try {
					// delete the unique directory created for the file on the server
					FileUtils.deleteDirectory(file.getParentFile());
				} catch (IOException e) {
					logger.warn("An exception occurred while trying to delete the file at '" + exportFileLocation + "'", e);
				}
				success = true;
			} else {
				if(file.isFile()){	
					try{
						FileUtils.forceDelete(file);
						
					} catch (IOException e) {
						logger.warn("An exception occurred while trying to delete an import file at '" + exportFileLocation + "'", e);
					}
					
					success = true;
					
				} else if(file.isDirectory()){
					
					try {
						FileUtils.deleteDirectory(file);
						
					} catch (IOException e) {
						logger.warn("An exception occurred while trying to delete an export file at '" + exportFileLocation + "'", e);
					}
					success = true;
					
				} else {
					success = false;
				}
			}
		}
		
		return success;
	}
	
	/**
	 * Gets this class' singleton instance
	 * 
	 * @return the singleton instance
	 */
	public static ImportManager getInstance(){
		
		if(singleton == null){
			singleton = new ImportManager();
		}
		
		return singleton;
	}
	
	/**
	 * Creates and formats an overwrite file prompt with details about the conflicting files.
	 *  
	 * @param newFile - the file that may overwrite an existing file
	 * @param existingFile - the existing file that can be overwritten
	 * @return a message with information about the files.
	 */
    public static String createOverwritePrompt(File newFile, File existingFile) {
		
    	String destPath = "";
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
		
		// If this image is in a folder, include the folder name in the message
		
		String folder = existingFile.getPath().replace(SURVEY_WEB_RESOURCES_DIRECTORY + File.separator, "");
		if(!folder.equals(newFile.getName())) {
			destPath = "<i>"  + folder.replace(File.separator + newFile.getName(), "") + "</i> ";
		}		
		
		String msg = 
					"The destination folder " + destPath +
					"already contains the file:<br><br>" +
					"<b>" + existingFile.getName() + "</b><br>" +
					"Size: " + FileUtil.byteToKb(existingFile.length()) + " KB<br>" +
					"Date modified: " + sdf.format(existingFile.lastModified()) + "<br><br>" +
					"Would you like to replace it with this file?<br><br>" +

					"<b>" + newFile.getName() + "</b><br>" +
					"Size: " +  FileUtil.byteToKb(newFile.length()) + " KB<br>" +
					"Date modified: " + sdf.format(newFile.lastModified());
		
		return msg;
    }
}
