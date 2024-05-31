/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import generated.course.LessonMaterialList;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action that causes a lesson material reference file to be created and saved to disk.
 * 
 * @author nroberts
 */
public class GenerateLessonMaterialReferenceFile implements Action<GenerateLessonMaterialReferenceFileResult>{
	
	/** 
	 * The lesson material list for the file to be generated
	 */
	private LessonMaterialList lessonMaterialList;

	/**
	 * The name of the file the training app reference file is being generated for
	 */
	private String targetFilename;
	
	/** The user name. */
	private String userName;

	/**
	 * No-arg constructor. Needed for serialization
	 */
	public GenerateLessonMaterialReferenceFile() {
	}

	/**
	 * Gets the lesson material list for the file to be generated
	 * 
	 * @return the lesson material list for the file to be generated
	 */
	public LessonMaterialList getLessonMaterialList() {
		return lessonMaterialList;
	}

	/**
	 * Sets the lesson material list for the file to be generated
	 * 
	 * @param taWrapper the lesson material list for the file to be generated
	 */
	public void setLessonMaterialList(LessonMaterialList taWrapper) {
		this.lessonMaterialList = taWrapper;
	}

	/**
	 * Gets the name of the file the lesson material reference file is being generated for
	 * 
	 * @return the name of the file the lesson material reference file is being generated for
	 */
	public String getTargetFilename() {
		return targetFilename;
	}

	/**
	 * Sets the name of the file the lesson material reference file is being generated for
	 * 
	 * @param targetFilename the name of the file the lesson material reference file is being generated for
	 */
	public void setTargetFilename(String targetFilename) {
		this.targetFilename = targetFilename;
	}

	/**
	 * Gets the user name.
	 * @return User name.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name.
	 * @param userName User name.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[GenerateLessonMaterialReferenceFile : lessonMaterialList=");
        builder.append(lessonMaterialList);
        builder.append(", targetFilename=");
        builder.append(targetFilename);
        builder.append(", userName=");
        builder.append(userName);
        builder.append("]");
        return builder.toString();
    }
	
}
