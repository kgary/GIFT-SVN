/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.Serializable;

/**
 * Contains information about a file that is hosted by a GIFT server and can be 
 * downloaded.
 * 
 * @author nroberts
 */
public class DownloadableFileRef implements Serializable{

	private static final long serialVersionUID = 1L;

	/** The URL from which the file can be downloaded by a client */
	private String downloadUrl;
	
	/** The path from which the file can be found on the server */
	private String locationOnServer;
	
	/**
	 * Public no-arg constructor. Required in order to send this class across GWT RPC. This method should not be invoked directly as it 
	 * does not create an actual reference to a file.
	 */
	public DownloadableFileRef(){
		
	}
	
	/**
	 * Sets the URL from which the file can be downloaded by a client and the path from 
	 * which the file can be found on the server
	 * 
	 * @param downloadUrl The URL from which the file can be downloaded by a client
	 * @param locationOnServer The path from which the file can be found on the server
	 */
	public DownloadableFileRef(String downloadUrl, String locationOnServer){
		
		this.downloadUrl = downloadUrl;
		this.locationOnServer = locationOnServer;
	}

	/**
	 * Gets the URL from which the file can be downloaded by a client
	 * 
	 * @return the URL from which the file can be downloaded by a client
	 */
	public String getDownloadUrl() {
		return downloadUrl;
	}

	/**
	 * Gets the path from which the file can be found on the server
	 * 
	 * @return the path from which the file can be found on the server
	 */
	public String getLocationOnServer() {
		return locationOnServer;
	}
	
	@Override
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("[DownloadableFileRef: ");
		sb.append("downloadURL = ").append(downloadUrl != null ? downloadUrl : "null").append(", ");
		sb.append("locationOnServer = ").append(locationOnServer != null ? locationOnServer : "null");
		sb.append("]");
		
		return sb.toString();
	}
}
