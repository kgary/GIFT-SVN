/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event.course;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * An event indicating that the server just finished saving a scenario file to its file system
 * 
 * @author nroberts
 */
public class ScenarioSavedEvent extends GenericEvent {
	
    /** The workspace-relative path to the file that was saved */
	private String filePath;
	
	/**
	 * Creates a new event indicating that the scenario file with the given path has finished saving
	 * 
	 * @param filePath the workspace-relative path to the file that was saved. Cannot be null.
	 */
	public ScenarioSavedEvent(String filePath){
		this.filePath = filePath;
	}

	/**
	 * Gets the workspace-relative path to the scenario file that was saved
	 * 
	 * @return the path to the saved scenario file. Will not be null.
	 */
	public String getFilePath() {
		return filePath;
	}
}
