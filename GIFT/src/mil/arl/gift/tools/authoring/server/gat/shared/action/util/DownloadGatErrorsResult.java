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
 * A result containing the download url of the error log.
 * 
 * @author bzahid
 */
public class DownloadGatErrorsResult extends GatServiceResult {

	/** The url of the log file*/
	private String downloadUrl;
	
	/** The download location of the log file*/
	private String relativeFilePath;
	
	/** 
	 * Instantiates a new result.
	 */
	public DownloadGatErrorsResult() {
		super();
	}
	
	/**
	 * Sets the url of the log file.
	 * 
	 * @param downloadUrl the url of the log file
	 */
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
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
	 * Gets the path to the downloaded file
	 * 
	 * @return the Domain-relative path to the download file
	 */
	public String getRelativeFilePath() {
		return relativeFilePath;
	}

	/**
	 * Sets the path to the downloaded file
	 * 
	 * @param relativeFilePath the Domain-relative path to the download file
	 */
	public void setRelativeFilePath(String relativeFilePath) {
		this.relativeFilePath = relativeFilePath;
	}
}
