/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * An action that deletes an exported file
 * 
 * @author bzahid
 */
public class DeleteExportFile implements Action<GatServiceResult> {
	
	String userName;
	String downloadUrl;
	String locationOnServer;
	
	/**
	 * Instantiates a new action.
	 */
	public DeleteExportFile() {
		super();
	}
	
	/**
	 * Gets the username this action is being executed for.
	 * 
	 * @return the username this action is being executed for
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the username this action is being executed for.
	 * 
	 * @param userName the username this action is being executed for
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Gets the url of the export file
	 * 
	 * @return the url of the export file
	 */
	public String getDownloadUrl() {
		return downloadUrl;
	}

	/**
	 * Sets the url of the export file
	 * 
	 * @param downloadUrl the url of the export file
	 */
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	/**
	 * Gets the location of the export file on the server
	 * 
	 * @return the location of the export file
	 */
	public String getLocationOnServer() {
		return locationOnServer;
	}

	/**
	 * Sets the location of the export file
	 * 
	 * @param locationOnServer the location of the export file on the server
	 */
	public void setLocationOnServer(String locationOnServer) {
		this.locationOnServer = locationOnServer;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[DeleteExportFile: ");
        sb.append("userName = ").append(userName);
        sb.append(", downloadUrl = ").append(downloadUrl);
        sb.append(", locationOnServer = ").append(locationOnServer);
        sb.append("]");

        return sb.toString();
    } 
}
