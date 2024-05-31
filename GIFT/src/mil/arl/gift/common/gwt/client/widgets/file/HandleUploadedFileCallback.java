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
 * A callback invoked once an uploaded file has been either successfully or unsuccessfully handled
 * 
 * @author nroberts
 */
public interface HandleUploadedFileCallback {

	/**
	 * Invokes logic on a successful request to handle an uploaded file
	 * 
	 * @param file a file tree model representing the final location of the uploaded file after it has been handled
	 */
	public void onSuccess(FileTreeModel file);
	
	/**
	 * Invokes logic on a failed request to handle an uploaded file
	 * 
	 * @param reason the reason the request failed
	 */
	public void onFailure(String reason);
	
	/**
	 * Invokes logic on a failed request to handle an uploaded file
	 * 
	 * @param thrown an error or exception that was thrown by the request
	 */
	public void onFailure(Throwable thrown);
}
