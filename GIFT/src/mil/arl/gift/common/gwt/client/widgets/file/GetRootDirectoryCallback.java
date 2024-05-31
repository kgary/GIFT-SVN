/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.file;

import mil.arl.gift.common.io.FileTreeModel;

/**
 * A callback that invokes logic once a call has been made to the server to get a root directory for a hierarchy of files. 
 * 
 * @author nroberts
 */
public interface GetRootDirectoryCallback {

	/**
	 * Invokes logic on a successful request to get the root directory
	 * 
	 * @param rootFile the root directory requested
	 */
	public void onSuccess(FileTreeModel rootFile);
	
	/**
	 * Invokes logic on a successful request to get the root directory. An optional starting directory may be specified for operations that need 
	 * to begin in a specific directory
	 * 
	 * @param rootFile the root directory requested
	 * @param startFile the directory logic should start in
	 */
	public void onSuccess(FileTreeModel rootFile, FileTreeModel startFile);
	
	/**
	 * Invokes logic on a failed request to get the root directory
	 * 
	 * @param reason the reason the request failed
	 */
	public void onFailure(String reason);
	
	/**
	 * Invokes logic on a failed request to get the root directory
	 * 
	 * @param thrown an error or exception that was thrown by the request
	 */
	public void onFailure(Throwable thrown);
}
