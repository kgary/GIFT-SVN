/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.ta;

/**
 * A callback to be invoked whenever the PowerPoint file has been changed
 * 
 * @author nroberts
 */
public interface PowerPointFileChangedCallback {

	/**
	 * Performs logic whenever the PowerPoint file is changed
	 * 
	 * @param newFilePath the path to the new PowerPoint file selected
	 */
	public void onFileChanged(String newFilePath);
}
