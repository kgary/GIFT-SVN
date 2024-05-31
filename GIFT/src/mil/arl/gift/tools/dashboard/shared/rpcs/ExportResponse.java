/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import mil.arl.gift.common.gwt.client.DetailedRpcResponse;
import mil.arl.gift.common.io.DownloadableFileRef;

/**
 * A response containing the result of an export
 * 
 * @author nroberts
 */
public class ExportResponse extends DetailedRpcResponse {
	
	/** The result of an export */
	private DownloadableFileRef exportResult;
	
	/**
	 * No-arg public constructor. Required by GWT RPC.
	 */
	public ExportResponse(){
		
	}
	
	/**
	 * Creates a response containing the result of an export
	 * 
	 * @param isSuccess whether or not the export was successful
	 * @param response error or success response
	 * @param exportResult the result of the export.  Can be null if nothing was produced.  This can
	 * be useful for when there isn't an error, just nothing to export (e.g. no course data to export because
	 * no one has taken the course yet)
	 */
	public ExportResponse(boolean isSuccess, String response, DownloadableFileRef exportResult){
		this.setIsSuccess(isSuccess);
		this.setResponse(response);
		this.exportResult = exportResult;
	}

	/**
	 * Gets the result of the export
	 * 
	 * @return the result of the export. Can be null if nothing was produced.  This can
     * be useful for when there isn't an error, just nothing to export (e.g. no course data to export because
     * no one has taken the course yet)
	 */
	public DownloadableFileRef getExportResult() {
		return exportResult;
	}

}
