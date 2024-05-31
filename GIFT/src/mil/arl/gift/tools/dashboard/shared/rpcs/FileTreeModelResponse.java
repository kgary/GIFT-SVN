/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import java.util.Set;

import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.io.FileTreeModel;

/**
 * A response containing a double value
 * 
 * @author nroberts
 */
public class FileTreeModelResponse extends RpcResponse {

	/** The file tree model*/
	private FileTreeModel model;
	
	/** A list of paths to files that are locked*/
	private Set<String> lockedfilePaths;
	
	/**
	 * Public no-arg constructor. Required by GWT RPC.
	 */
	public FileTreeModelResponse(){
		
	}
	
	/**
	 * Creates a response containing a file tree model
	 * 
	 * @param isSuccess whether or not the export was successful
	 * @param response error or success response
	 * @param model the file tree model
	 */
	public FileTreeModelResponse(boolean isSuccess, String response, FileTreeModel model){
		this.setIsSuccess(isSuccess);
		this.setResponse(response);
		this.model = model;
	}

	/**
	 * Gets the file tree model
	 * 
	 * @return the file tree model
	 */
	public FileTreeModel getValue() {
		return model;
	}

	/**
	 * Gets the list of locked file paths
	 * 
	 * @return the list of locked file paths
	 */
	public Set<String> getLockedfilePaths() {
		return lockedfilePaths;
	}

	/**
	 * Sets the list of locked file paths
	 * 
	 * @param lockedfilePaths the list of locked file paths
	 */
	public void setLockedfilePaths(Set<String> lockedfilePaths) {
		this.lockedfilePaths = lockedfilePaths;
	}
}
