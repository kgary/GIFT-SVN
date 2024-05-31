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
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.io.ExportProperties;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DownloadableFileRef;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.tools.services.ServicesManager;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object that handles requests to export courses and course data and monitors their progress on a per-user basis
 * 
 * @author nroberts
 */
public class ExportManager {
	
	private static Logger logger = LoggerFactory.getLogger(ExportManager.class);
	
	/** A mapping from each username to the progress of their currently running export process, if one exists */
	private ConcurrentHashMap<String, ProgressIndicator> usernameToProgress = new ConcurrentHashMap<String, ProgressIndicator>();
	
	/** Singleton instance of this class*/
	private static ExportManager singleton = null;
	
	/** 
	 * Creates a new export manager
	 */
	private ExportManager(){
		
	}
	
	/**
	 * Export the course data for the course specified.
	 * 
	 * @param username the user requesting to export the course data
	 * @param course information about the course to export
	 * @return a URL that supports downloading the course data zip file.  Will be null if there were no files to zip.
	 * @throws DetailedException  if there was a problem with the export properties, a database operation, or the database
	 * said there were logs but they couldn't be found.
	 * @throws IOException if there was a problem finding a file on disk
	 */
	public DownloadableFileRef exportCourseData(String username, DomainOption course) throws DetailedException, IOException{
	    
	    DownloadableFileRef result = null;
        
        if(logger.isDebugEnabled()){
            logger.debug("Starting export course data for user: " + username + " on "+course.getDomainId());
        }
        
        ProgressIndicator progress = new ProgressIndicator(0, "Overall Course Data Export");
        
        usernameToProgress.put(username, progress);
        
        //construct export file name using current time and user information
        LocalDateTime currentDate = LocalDateTime.now();
        
        // e.g. "course_data_export_3-14_15-33_mhoffman"
        StringBuilder exportFileName = new StringBuilder();
        exportFileName.append("course_data_export_");       
        exportFileName.append(currentDate.getMonthValue());
        exportFileName.append("-");
        exportFileName.append(currentDate.getDayOfMonth());
        exportFileName.append("_");
        exportFileName.append(currentDate.getHour());
        exportFileName.append("-");
        exportFileName.append(currentDate.getMinute());
        exportFileName.append("_");
        exportFileName.append(username);
        
        List<DomainOption> courses = new ArrayList<>();
        courses.add(course);
        
        ExportProperties exportProps = new ExportProperties(username, courses, exportFileName.toString(), progress);
        exportProps.setCoursesOnly(false);
        exportProps.setShouldExportUserData(true);
        
        result = ServicesManager.getInstance().getFileServices().exportCourseData(exportProps);
        
        if(result != null){
            final DownloadableFileRef referencetoExportResult = result;
            
            Thread deleteExportFileThread = new Thread("Delete Course Data Export File"){
                
                @Override
                public void run(){
                    try {
                        
                        synchronized(this){
                            //wait 30 minutes and then delete the export file if it still exists
                            wait(1800000);
                        }
                        
                        deleteExportFile(referencetoExportResult);
                        
                    } catch (InterruptedException e) {
                        logger.warn("An exception occurred while waiting to delete a course data export file.", e);
                    }
                }
            };
            
            deleteExportFileThread.start();
        }
        
        return result;
	}

	/**
	 * Creates an export file from the specified list of courses and returns the location of the generated file
	 * 
	 * @param username the username of the user for which the export is being executed
	 * @param coursesToExport the list of courses to export
	 * @return the result of the export
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	public DownloadableFileRef export(String username, List<DomainOption> coursesToExport) throws IOException, URISyntaxException{
		
	    DownloadableFileRef result = null;
		
		if(logger.isDebugEnabled()){
		    logger.debug("Starting export for user: " + username);
		}
		
		ProgressIndicator progress = new ProgressIndicator(0, "Overall Export");
		
		usernameToProgress.put(username, progress);
		
		//construct export file name using current time and user information
		LocalDateTime currentDate = LocalDateTime.now();
		
		// e.g. "course_export_2021-3-14_15-33_mhoffman"
		StringBuilder exportFileName = new StringBuilder();
		exportFileName.append("course_export_");		
		exportFileName.append(currentDate.getYear());
		exportFileName.append("-");
		exportFileName.append(currentDate.getMonthValue());
		exportFileName.append("-");
		exportFileName.append(currentDate.getDayOfMonth());
		exportFileName.append("_");
		exportFileName.append(currentDate.getHour());
		exportFileName.append("-");
		exportFileName.append(currentDate.getMinute());
		exportFileName.append("_");
		exportFileName.append(username);
		
		result = ServicesManager.getInstance().getFileServices().exportCourses(
		        new ExportProperties(username, coursesToExport, exportFileName.toString(), progress));
		
		final DownloadableFileRef referencetoExportResult = result;
		
		Thread deleteExportFileThread = new Thread("Delete Course Export File"){
			
			@Override
			public void run(){
				try {
					
				    synchronized(this){
    					//wait 30 minutes and then delete the export file if it still exists
    					wait(1800000);
				    }
					
					deleteExportFile(referencetoExportResult);
					
				} catch (InterruptedException e) {
					logger.warn("An exception occurred while waiting to delete a course export file.", e);
				}
			}
		};
		
		deleteExportFileThread.start();
		
		return result;
	}
	
	/**
	 * Gets the progress of the export being run for the specified user
	 * 
	 * @param username the username of the user to get the progress for
	 * @return the progress of the export, can be null if there is no progress tracking for that user
	 */
	public ProgressIndicator getExportProgress(String username){
		return usernameToProgress.get(username);
	}
	
	/**
	 * Cancels the export being run for the given user, if one exists
	 * 
	 * @param username the username of the user for whome to cancel the export
	 */
	public void cancelExport(String username){
		
		if(username == null){
			throw new IllegalArgumentException("Username cannot be null");
		}
		
		ProgressIndicator progress = usernameToProgress.get(username);
		if(progress != null){
		    // found a NPE on MCOE server one time, 
		    progress.setShouldCancel(true);
		}
	}
	
	/**
	 * Deletes the file generated by an export
	 * 
	 * @param result the result of the export used to get the file's location
	 * @return whether or not the delete was successful
	 */
	public boolean deleteExportFile(DownloadableFileRef result){
		
		boolean success = true;
		
		if(result == null || result.getLocationOnServer() == null){
			success = false;
			
		} else {
			
			File file = new File(result.getLocationOnServer());
			
			if(file.isFile()){	
				try{
					FileUtils.forceDelete(file);
					
				} catch (IOException e) {
					logger.warn("An exception occurred while trying to delete an export file at '" + result.getLocationOnServer() + "'", e);
				}
				
				success = true;
				
			} else if(file.isDirectory()){
				
				try {
					FileUtils.deleteDirectory(file);
					
				} catch (IOException e) {
					logger.warn("An exception occurred while trying to delete an export file at '" + result.getLocationOnServer() + "'", e);
				}
				success = true;
				
			} else {
				success = false;
			}
		}
		
		return success;
	}
	
	/**
	 * Calculates the size of an export using the given information
	 * 
	 * @param username the username of the user for whom the calculation is being down
	 * @param coursesToExport the courses to be included in the export
	 * @return the predicted size of the export file in MB
	 * @throws IllegalArgumentException if any arguments are null or if the list of courses to export is empty
	 * @throws IOException if there was a problem determining the size of the course export
	 * @throws URISyntaxException if there was a problem build a URI to access the files
	 */
	public double getExportSize(String username, List<DomainOption> coursesToExport) throws IllegalArgumentException, IOException, URISyntaxException{
		
		if(logger.isDebugEnabled()){
		    logger.debug("Starting export for user: " + username);
		}
		
		ProgressIndicator progress = new ProgressIndicator(0, "Calculating Size");
		
		//construct export file name using current time and user information
		LocalDateTime currentDate = LocalDateTime.now();
		
		StringBuilder exportFileName = new StringBuilder();
		exportFileName.append("export_");		
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
		
		return ServicesManager.getInstance().getFileServices().getCourseExportSize(new ExportProperties(username, coursesToExport, exportFileName.toString(), progress));
	}
	
	/**
	 * Gets this class' singleton instance
	 * 
	 * @return the singleton instance
	 */
	public static ExportManager getInstance(){
		
		if(singleton == null){
			singleton = new ExportManager();
		}
		
		return singleton;
	}
}
