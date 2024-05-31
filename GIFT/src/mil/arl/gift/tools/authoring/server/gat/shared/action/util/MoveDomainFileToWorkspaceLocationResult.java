/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The result of a {@link MoveDomainFileToWorkspaceLocation} action.
 * 
 * @author nroberts
 */
public class MoveDomainFileToWorkspaceLocationResult extends GatServiceResult {

	/**
	 * A file tree model representing the final location of the moved file
	 */
	private FileTreeModel movedLocationModel;
	
	/** A mapping of files that were not moved to the locations where they were supposed to be copied to */
	private Map<String, String> moveFailures = new HashMap<String, String>();
	
	/**
	 * Default public constructor required for serialization.
	 */
	public MoveDomainFileToWorkspaceLocationResult(){
		super();
	}
	
	/**
	 * Creates a result containing the final location of a file that was moved
	 * 
	 * @param movedLocationModel a file tree model representing the final location of the moved file
	 */
	public MoveDomainFileToWorkspaceLocationResult(FileTreeModel movedLocationModel){
		this.movedLocationModel = movedLocationModel;
	}

	/**
	 * Gets the file tree model representing the final location of the moved file
	 * 
	 * @return the file tree model representing the final location of the moved file
	 */
	public FileTreeModel getMovedLocationModel() {
		return movedLocationModel;
	}
	
	/**
	 * Sets the file tree model representing the final location of the moved file
	 * 
	 * @param movedLocationModel the file tree model representing the final location of the moved file
	 */
	public void setMovedLocationModel(FileTreeModel movedLocationModel) {
		this.movedLocationModel = movedLocationModel;
	}

	/**
	 * Gets the mapping of files that were not moved because of name conflicts to the locations where they were supposed to be copied to
	 * 
	 * @return the mapping of files that were not moved to the locations where they were supposed to be copied to
	 */
	public Map<String, String> getMoveFailures() {
		return moveFailures;
	}

	/**
	 * Adds an indication that the specified source file could not be moved to its destination because a file with the same name 
	 * already exists
	 * 
	 * @param sourceFilePath the path to the source file
	 * @param targetFilePath the path to the destination
	 */
	public void addMoveFailure(String sourceFilePath, String targetFilePath){
		moveFailures.put(sourceFilePath, targetFilePath);
	}
}
