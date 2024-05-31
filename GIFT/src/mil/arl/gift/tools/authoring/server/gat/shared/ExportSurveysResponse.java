/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

import mil.arl.gift.common.gwt.client.RpcResponse;

/**
 * An rpc response containing information about a survey export
 * 
 * @author bzahid
 */
public class ExportSurveysResponse extends RpcResponse {

	/** The download url for the survey export file */
	private String downloadUrl;
	
	/** The temporary file to delete after the download is finished */
	private String tempFileToDelete;
	
	/**
	 * Class constructor.
	 */
	public ExportSurveysResponse() {
		super();
	}
	
	/**
	 * Sets the download url for the survey export file.
	 * @param downloadUrl The download url.
	 */
	public void setExportDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	
	/**
	 * Gets the download url for the survey export file.
	 * @return the download url.
	 */
	public String getExportDownloadUrl() {
		return downloadUrl;
	}
		
	/**
	 * Sets the path to the temporary file created during the export.
	 * @param tempFile The path to the temporary file created during the export.
	 */
	public void setTempFileToDelete(String tempFile) {
		tempFileToDelete = tempFile;
	}
	
	/**
	 * Gets the path to the temporary file created during the export.
	 * @return The path to the temporary file created during the export.
	 */
	public String getTempFileToDelete() {
		return tempFileToDelete;
	}
}
