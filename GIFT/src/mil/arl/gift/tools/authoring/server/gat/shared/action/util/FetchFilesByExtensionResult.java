/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.util.List;
import java.util.Map;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The Class FetchFilesByExtensionResult.
 */
public class FetchFilesByExtensionResult extends GatServiceResult {
	
	/** The files. */
	private Map<String, List<String>> files;

    /**
     * Instantiates a new fetch files by extension result.
     */
    public FetchFilesByExtensionResult() {
        super();
    }
    
    /**
     * Instantiates a new fetch files by extension result.
     *
     * @param files the files
     */
    public FetchFilesByExtensionResult(Map<String, List<String>> files){
    	this.setFiles(files);
    }

	/**
	 * Gets the files.
	 *
	 * @return the files
	 */
	public Map<String, List<String>> getFiles() {
		return files;
	}

	/**
	 * Sets the files.
	 *
	 * @param files the files
	 */
	public void setFiles(Map<String, List<String>> files) {
		this.files = files;
	}

}
