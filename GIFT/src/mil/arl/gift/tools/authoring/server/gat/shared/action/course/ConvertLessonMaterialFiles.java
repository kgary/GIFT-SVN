/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import generated.course.LessonMaterialFiles;
import net.customware.gwt.dispatch.shared.Action;

/**
 * An action for converting a LessonMaterialFiles object to a LessonMaterialList
 * 
 * @author nroberts
 */
public class ConvertLessonMaterialFiles implements Action<ConvertLessonMaterialFilesResult> {

	private String username;
	
	private LessonMaterialFiles files;
	
	private String courseFolderPath;
	
    /**
     * No-arg constructor. Needed for serialization.
     */
    public ConvertLessonMaterialFiles() {
    	
    }

    /**
     * Creates a new action to get the list of files corresponding to a Merrill quadrant
     * 
     * @param username the username of the user making the request
     * @param files the lesson material files to convert
     * @param courseFolderPath the location of the course folder
     */
    public ConvertLessonMaterialFiles(String username, LessonMaterialFiles files, String courseFolderPath) {
    	this.username = username;
    	this.files = files;
    	this.courseFolderPath = courseFolderPath;
    }
    
    /**
     * Gets the username of the user making the request
     * 
     * @return the username
     */
	public String getUsername() {
		return username;
	}

	/**
	 * Gets the lesson material files to convert
	 * 
	 * @return the lesson material files to convert
	 */
	public LessonMaterialFiles getFiles() {
		return files;
	}

	/**
	 * Gets the location of the course folder
	 * 
	 * @return the location of the course folder
	 */
	public String getCourseFolderPath() {
		return courseFolderPath;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[ConvertLessonMaterialFiles: ");
        sb.append("username = ").append(username);
        sb.append(", files").append(files.toString());
        sb.append(", courseFolderPath").append(courseFolderPath);
        sb.append("]");

        return sb.toString();
    } 
}
