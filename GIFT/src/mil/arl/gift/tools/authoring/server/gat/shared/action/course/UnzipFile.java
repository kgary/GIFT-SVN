/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * An {@link Action} that extracts a .zip file into a course subfolder containing its contents
 * 
 * @author nroberts
 */
public class UnzipFile implements Action<GatServiceResult> {
	
	/** The path the .zip file to extract from */
	private String zipFilePath;
	
	/** The path to the course folder where the contents of the .zip should be extracted to */
	private String courseFolderPath;
	
	/** The username of the user invoking this action */
	private String username;
	
	/**
	 * Class constructor for serialization
	 */
	private UnzipFile() {
		
	}
	
	/**
	 * Creates a new action that will prompt the server to extract a .zip file into a course sub folder containing its contents
	 * 
	 * @param username the username of the user invoking this action. Used to invoke file operations that require user clearance. Cannot be null.
	 * @param courseFolderPath the path to the course folder where the contents of the .zip should be extracted to. Cannot be null.
	 * @param zipfilePath the path the .zip file to extract from. Cannot be null.
	 */
	public UnzipFile(String username, String courseFolderPath, String zipFilePath){
		this();
		
		if(username == null){
			throw new IllegalArgumentException("The username of the user invoking this action cannot be null");
		}
		
		if(courseFolderPath == null){
			throw new IllegalArgumentException("The path to the course folder where the contents of the .zip should be extracted to cannot be null");
		}
		
		if(zipFilePath == null){
			throw new IllegalArgumentException("The path to the .zip file to extract from cannot be null");
		}
		
		this.username = username;
		this.courseFolderPath = courseFolderPath;
		this.zipFilePath = zipFilePath;
	}
	
	/**
	 * Gets the path to the course folder where the contents of the .zip should be extracted to
	 * 
	 * @return The path to the course folder
	 */
	public String getCourseFolderPath() {
		return courseFolderPath;
	}
	
	/**
	 * Gets the path to the .zip file to extract from
	 * 
	 * @return The path to the .zip file
	 */
	public String getZipFilePath() {
		return zipFilePath;
	}
	
	/**
	 * Gets the username of the user invoking this action
	 * 
	 * @return The username
	 */
	public String getUsername() {
		return username;
	}
	
	@Override
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		sb.append("[UnzipFile username='")
			.append(username)
			.append("', zipFilePath='")
			.append(zipFilePath)
			.append("', courseFolderPath='")
			.append(courseFolderPath)
			.append("']");
		
		return sb.toString();
	}
}
