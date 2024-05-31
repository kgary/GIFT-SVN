/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.file;

/**
 * An object that performs logic on an uploaded file once it is placed on the server
 * 
 * @author nroberts
 */
public interface CanHandleUploadedFile {

	/**
	 * Handles the uploaded file at the given Domain-relative file path
	 * 
	 * @param uploadFilePath the server-side location of the uploaded file, relative to the Domain folder.  Includes the file name
	 * as it is on the server.
	 * @param fileName the name of the file provided by the user.  Won't be null or empty.  Can be useful for display
	 * purposes.  Also useful in case the server changed the uploaded file name.
	 * @param callback a callback that should be invoked once the uploaded file has been handled
	 */
	public void handleUploadedFile(final String uploadFilePath, final String fileName, HandleUploadedFileCallback callback);
}
