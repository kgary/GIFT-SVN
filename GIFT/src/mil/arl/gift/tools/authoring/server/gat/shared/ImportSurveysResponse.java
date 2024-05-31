/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

import java.util.Map;

import mil.arl.gift.common.gwt.client.RpcResponse;

/**
 * A response containing the results of an import.
 * 
 * @author bzahid
 */
public class ImportSurveysResponse extends RpcResponse {

	/** Whether or not this survey import will cause a filename conflict */
	private boolean hasFilenameConflicts = false;
	
	/** A map of original to new filenames used to resolve filename conflicts during the import. */
	private Map<String, String> conflictsList;
	
	/** The name of the survey to be imported. */
	private String surveyName;
	
	/** The id of the survey that was imported. */
	private int surveyId;
	
	/** A path to the temporary file created during the import. */
	private String tempFileToDelete;
	
	/**
	 * Class constructor.
	 */
	public ImportSurveysResponse(){ 
		super();
	}
	
	/**
	 * Sets whether or not the import has filename conflicts.
	 * @param hasFilenameConflicts true if there are conflicts, false otherwise.
	 */
	public void setHasFilenameConflicts(boolean hasFilenameConflicts) {
		this.hasFilenameConflicts = hasFilenameConflicts;
	}
	
	/**
	 * Gets whether or not the import has filename conflicts.
	 * @return true if there are conflicts, false otherwise.
	 */
	public boolean getHasFilenameConflicts() {
		return hasFilenameConflicts;
	}
	
	/**
	 * Set the map of original to new filenames used to resolve filename conflicts during the import.
	 * @param conflictsList a map of original to new filenames.
	 */
	public void setConflictsList(Map<String, String> conflictsList) {
		this.conflictsList = conflictsList;
	}
	
	/**
	 * Gets the map of original to new filenames used to resolve filename conflicts during the import.
	 * @return a map of original to new filenames. Can be null.
	 */
	public Map<String, String> getConflictsList() {
		return conflictsList;
	}
	
	/**
	 * Sets the name of the survey that was imported.
	 * @param surveyName The name of the survey.
	 */
	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}
	
	/**
	 * Gets the name of the survey that was imported.
	 * @return The name of the survey.
	 */
	public String getSurveyName() {
		return surveyName;
	}
	
	/**
	 * Sets the id of the survey that was imported.
	 * @param surveyId The id of the survey.
	 */
	public void setSurveyId(int surveyId) {
		this.surveyId = surveyId;
	}
	
	/**
	 * Gets the id of the survey that was imported.
	 * @return The id of the survey.
	 */
	public int getSurveyId() {
		return surveyId;
	}
	
	/**
	 * Sets the path to the temporary file created during the import.
	 * @param tempFile The path to the temporary file created during the import.
	 */
	public void setTempFileToDelete(String tempFile){
		tempFileToDelete = tempFile;
	}
	
	/**
	 * Gets the path to the temporary file created during the import.
	 * @return The path to the temporary file created during the import.
	 */
	public String getTempFileToDelete() {
		return tempFileToDelete;
	}
}
