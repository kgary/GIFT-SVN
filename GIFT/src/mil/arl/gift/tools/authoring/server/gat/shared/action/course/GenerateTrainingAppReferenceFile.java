/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import generated.course.TrainingApplicationWrapper;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action that causes a training application reference file to be created and saved to disk.
 * 
 * @author nroberts
 */
public class GenerateTrainingAppReferenceFile implements Action<GenerateTrainingAppReferenceFileResult>{
	
	/** 
	 * The training application wrapper for the file to be generated
	 */
	private TrainingApplicationWrapper taWrapper;

	/**
	 * The name of the file the training app reference file is being generated for
	 */
	private String targetFilename;
	
	/** The user name. */
	private String userName;

	/**
	 * No-arg constructor. Needed for serialization
	 */
	public GenerateTrainingAppReferenceFile() {
	}

	/**
	 * Gets the training application wrapper for the file to be generated
	 * 
	 * @return the training application wrapper for the file to be generated
	 */
	public TrainingApplicationWrapper getTrainingAppWrapper() {
		return taWrapper;
	}

	/**
	 * Sets the training application wrapper for the file to be generated
	 * 
	 * @param taWrapper the training application wrapper for the file to be generated
	 */
	public void setTrainingAppWrapper(TrainingApplicationWrapper taWrapper) {
		this.taWrapper = taWrapper;
	}

	/**
	 * Gets the name of the file the training application reference file is being generated for
	 * 
	 * @return the name of the file the training application reference file is being generated for
	 */
	public String getTargetFilename() {
		return targetFilename;
	}

	/**
	 * Sets the name of the file the training application reference file is being generated for
	 * 
	 * @param targetFilename the name of the file the training application reference file is being generated for
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
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[GenerateTrainingAppReferenceFile: ");
        sb.append("taWrapper = ").append(taWrapper);
        sb.append(", targetFilename").append(targetFilename);
        sb.append(", userName = ").append(userName);
        sb.append("]");

        return sb.toString();
    } 
}
