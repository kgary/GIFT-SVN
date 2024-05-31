/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.file;

/**
 * A callback that executes logic once a file list has been loaded or failed to load
 * 
 * @author nroberts
 */
public interface FileListLoadedCallback {

	/** 
	 * Executes logic once a file list has been loaded
	 */
	public void onFileListLoaded();
	
	/** 
	 * Executes logic once a file list fails to load
	 */
	public void onFailure();
}
