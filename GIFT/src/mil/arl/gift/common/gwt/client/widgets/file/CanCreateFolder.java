/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.file;

/**
 * An object that can create a folder
 * 
 * @author nroberts
 */
public interface CanCreateFolder {
	
	/**
	 * Creates a folder with the specified name in the specified parent folder. 
	 * 
	 * When invoked by a {@link FileSaveAsDialog}, the path of the the parent folder given will be relative to the starting folder of the dialog.
	 * 
	 * @param parentFolderPath the relative path of the parent folder the new folder should be created in
	 * @param folderName the name of the folder to create
	 * @param callback a callback that handles the result
	 */
	public void createFolder(String parentFolderPath, String folderName, CreateFolderCallback callback);
}
