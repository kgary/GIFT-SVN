/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The result of a {@link ValidateMediaSemanticsFile} action.
 * 
 * @author bzahid
 */
public class ValidateMediaSemanticsResult extends GatServiceResult {

	/** Whether or not the media semantics files are valid. */
	private boolean isValidFile;
	
	/**
	 * Class constructor.
	 */
	public ValidateMediaSemanticsResult() {
		super();
	}

	/**
	 * Returns whether or not the media semantics files are valid.
	 * 
	 * @return true if the files are valid, false otherwise.
	 */
	public boolean isValidFile() {
		return isValidFile;
	}

	/**
	 * Sets whether or not the media semantics files are valid.
	 * 
	 * @param isValidFile true if the files are valid, false otherwise.
	 */
	public void setValidFile(boolean isValidFile) {
		this.isValidFile = isValidFile;
	}
	
	
}
