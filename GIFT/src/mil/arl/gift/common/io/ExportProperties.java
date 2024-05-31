/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.util.List;

import mil.arl.gift.common.DomainOption;

/**
 * Properties to be used by exporting logic in the services layer
 * 
 * @author nroberts
 */
public class ExportProperties {

	/** The username of the user for whom the export is being made */
	private String username;
	
	/** The domain options of the courses to export */
	private List<DomainOption> coursesToExport;
	
	/** The name of the file to be exported to */
	private String exportFileName;
	
	/** An indicator of the export's current progress */
	private ProgressIndicator progressIndicator;
	
	/** Whether or not only courses should be included in the export*/
	private boolean coursesOnly = true;
	
	/** Whether or not user data should be included in the export*/
	private boolean shouldExportUserData = false;
	
	/** 
	 * Creates properties to be used by exporting logic in the services layer
	 * 
	 * @param username the username of the user for whom the export is being made.  can't be null.
	 * @param coursesToExport the domain options of the courses to export.  can't be null or empty.
	 * @param exportFileName the name of the file to be exported to.  can't be null.
	 * @param progressIndicator an indicator of the export's current progress.  can't be null.
	 * @throws IllegalArgumentException if any arguments are null or if the list of courses to export is empty
	 */
	public ExportProperties(String username, List<DomainOption> coursesToExport, String exportFileName, ProgressIndicator progressIndicator) throws IllegalArgumentException{
		
		if(username == null){
			throw new IllegalArgumentException("Username cannot be null");
		}
		
		if(coursesToExport == null){
			throw new IllegalArgumentException("The list of courses to export cannot be null");
		}
		
		if(coursesToExport.isEmpty()){
			throw new IllegalArgumentException("The list of courses to export cannot be empty");
		}
		
		if(exportFileName == null){
			throw new IllegalArgumentException("Export file name cannot be null");
		}
		
		if(progressIndicator == null){
			throw new IllegalArgumentException("Progress indicator cannot be null");
		}
		
		this.username = username;
		this.coursesToExport = coursesToExport;
		this.exportFileName = exportFileName;
		this.progressIndicator = progressIndicator;
	}

	/**
	 * Gets whether or not only courses should be included in the export
	 * 
	 * @return whether or not only courses should be included in the export
	 */
	public boolean isCoursesOnly() {
		return coursesOnly;
	}

	/**
	 * Sets whether or not only courses should be included in the export
	 * 
	 * @param coursesOnly whether or not only courses should be included in the export
	 */
	public void setCoursesOnly(boolean coursesOnly) {
		this.coursesOnly = coursesOnly;
	}

	/**
	 * Gets whether or not user data should be included in the export
	 * 
	 * @return whether or not user data should be included in the export
	 */
	public boolean shouldExportUserData() {
		return shouldExportUserData;
	}

	/**
	 * Sets whether or not user data should be included in the export
	 * 
	 * @param shouldExportUserData whether or not user data should be included in the export
	 */
	public void setShouldExportUserData(boolean shouldExportUserData) {
		this.shouldExportUserData = shouldExportUserData;
	}

	/**
	 * Gets the username of the user for whom the export is being made
	 * 
	 * @return the username of the user for whom the export is being made
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Gets the domain options of the courses to export
	 * 
	 * @return the domain options of the courses to export
	 */
	public List<DomainOption> getCoursesToExport() {
		return coursesToExport;
	}

	/**
	 * Gets the name of the file to be exported to
	 * 
	 * @return the name of the file to be exported to
	 */
	public String getExportFileName() {
		return exportFileName;
	}

	/**
	 * @return the progressIndicator
	 */
	public ProgressIndicator getProgressIndicator() {
		return progressIndicator;
	}

	@Override
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		sb.append("[ExportProperties: ");
		sb.append("username = ").append(username).append(", ");
		sb.append("coursesToExport = ").append("[");
		
		for(DomainOption course : coursesToExport){
			sb.append(course.toString());
		}
		
		sb.append("], ");	
		sb.append("exportFileName = ").append(exportFileName).append(", ");
		sb.append("progressIndicator = ").append(progressIndicator).append(", ");
		sb.append("coursesOnly =  ").append(coursesOnly).append(", ");
		sb.append("shouldExportUserData =  ").append(shouldExportUserData);
		sb.append("]");
		
		return sb.toString();
	}
}
