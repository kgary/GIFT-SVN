/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;
 
import java.util.List;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import net.customware.gwt.dispatch.shared.Action;
 
/**
 * An action telling the server to delete a set of workspace files
 * 
 * @author nroberts
 *
 */
public class DeleteWorkspaceFiles implements Action<GatServiceResult> {
	
	private String username;
	private String browserSessionKey;
	private List<String> workspacePaths;
	private boolean cleanUpOnFailure;
    
    /**
     * Instantiates a new fetch files by extension.
     */
    public DeleteWorkspaceFiles() {
    }
    
    /**
     * Creates a request with populated parameters
     * @param username the name of the user who is making the request
     * @param browserSessionKey the unique identifier of the browser making the request
     * @param workspacePaths the workspace paths to delete
     * @param cleanUpOnFailure flag indicating whether or not to clean up on failure, currently does nothing
     */
    public DeleteWorkspaceFiles(String username, String browserSessionKey, List<String> workspacePaths, boolean cleanUpOnFailure) {
        this.username = username;
        this.browserSessionKey = browserSessionKey;
        this.workspacePaths = workspacePaths;
        this.cleanUpOnFailure = cleanUpOnFailure;
    }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * Gets the value of the unique identifier for the browser that is making the request
	 * @return the value of the browser session key, can be null
	 */
	public String getBrowserSessionKey() {
        return browserSessionKey;
    }
	
	/**
	 * Sets the value of the unique identifier for the browser that is making the request
	 * @param browserSessionKey the new value of the browser session key, can be null
	 */
	public void setBrowserSessionKey(String browserSessionKey) {
        this.browserSessionKey = browserSessionKey;
    }

	public boolean isCleanUpOnFailure() {
		return cleanUpOnFailure;
	}

	public void setCleanUpOnFailure(boolean cleanUpOnFailure) {
		this.cleanUpOnFailure = cleanUpOnFailure;
	}

	public List<String> getWorkspacePaths() {
		return workspacePaths;
	}

	public void setWorkspacePaths(List<String> workspacePaths) {
		this.workspacePaths = workspacePaths;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[DeleteWorkspaceFiles: ");
        sb.append("username = ").append(username);
        sb.append(", browserSessionKey = ").append(browserSessionKey);
        sb.append(", workspacePaths = ").append(workspacePaths);
        sb.append(", # workspace paths = ").append((workspacePaths != null ? workspacePaths.size() : "null"));
        if (workspacePaths != null) {
        	sb.append(" : [");
        	for(int i=0; i < workspacePaths.size(); i++) {
        		if (i > 0) {
        			sb.append(", ");
        		}
        		sb.append(workspacePaths.get(i));
        	}
        	sb.append("]");
        }
        sb.append(", cleanUpOnFailure = ").append(cleanUpOnFailure);
        sb.append("]");

        return sb.toString();
    } 
}
