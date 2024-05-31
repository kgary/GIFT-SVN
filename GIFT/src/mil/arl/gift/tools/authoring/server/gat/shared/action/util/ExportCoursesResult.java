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
 * A result containing information about the export task
 * 
 * @author bzahid
 */
public class ExportCoursesResult extends GatServiceResult {

	/** The download location of the exported file */
	private String downloadUrl;
	
	/** The location of the exported file  on the server*/
	private String locationOnServer;

	/**
	 * Instantiates a new result.
	 */
	public ExportCoursesResult() {
		super();
	}
	
	/**
	 * Sets the download URL of the exported file.
	 * 
	 * @param url the download url of the exported file
	 */
	public void setDownloadUrl(String url) {
		downloadUrl = url;
	}
	
	/**
	 * Gets the download url for the exported file.
	 * 
	 * @return the download url for the exported file
	 */
	public String getDownloadUrl() {
		return downloadUrl;
	}
	
	/**
	 * Gets the location of the exported file.
	 * 
	 * @return the location of the exported file on the server
	 */
	public String getLocationOnServer() {
		return locationOnServer;
	}

	/**
	 * Sets the location of the exported file.
	 * 
	 * @param locationOnServer the location of the exported file on the server
	 */
	public void setLocationOnServer(String locationOnServer) {
		this.locationOnServer = locationOnServer;
	}
}
