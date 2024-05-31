/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.file;

/**
 * A callback that invokes logic when a folder has been created
 * 
 * @author nroberts
 */
public interface CreateFolderCallback {

	/**
	 * Executes logic when a folder has been created
	 */
	public void onFolderCreated();
	
	/**
	 * Executes logic when folder creation fails
	 * 
	 * @param reason a message describing the reason the folder creation failed
	 */
	public void onFailure(String reason);
	
	/**
	 * Executes logic when folder creation fails
	 * 
	 * @param thrown the exception thrown which caused the failure
	 */
	public void onFailure(Throwable thrown);
}
