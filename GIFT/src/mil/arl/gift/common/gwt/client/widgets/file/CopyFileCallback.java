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
 * Interface defining a copy file callback operation.
 * 
 * @author nblomberg
 *
 */
public interface CopyFileCallback {

	/**
	 * Invokes logic on a successful request to copy the file
	 * 
	 * @param file - the FileTreeModel for the newly copied file.
	 */
	public void onSuccess(FileTreeModel file);
	
	/**
	 * Invokes logic on a failed request to copy the file.
	 * 
	 * @param reason - the reason the request failed
	 */
	public void onFailure(String reason);
	
	/**
	 * Invokes logic on a failed request to copy the file.
	 * 
	 * @param thrown - an error or exception that was thrown by the request
	 */
	public void onFailure(Throwable thrown);
}
