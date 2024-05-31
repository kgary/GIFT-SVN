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
 * Interface defining a request to perform an async copy operation with the server.
 * 
 * @author nblomberg
 *
 */
public interface CopyFileRequest {

	/**
	 * Send an asynchronous copy file request to the server.
	 * 
	 * @param source - The source file to be copied.
	 * @param callback - The callback used to notify when the asynchronous request is completed.
	 */
	public void asyncCopy(FileTreeModel source, final CopyFileCallback callback);
}
