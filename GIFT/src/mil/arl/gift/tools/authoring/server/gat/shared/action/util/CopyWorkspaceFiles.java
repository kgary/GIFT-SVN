/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;
 
import java.util.Map;

import net.customware.gwt.dispatch.shared.Action;
 
/**
 * An action telling the sever to copy a set of files to new locations
 * 
 * @author nroberts
 *
 */
public class CopyWorkspaceFiles implements Action<CopyWorkspaceFilesResult> {
	
	/** The username of the user for which files are being copied */
	private String username;
	
	/** The unique identifier of the browser making the request */
	private String browserSessionKey;
	
	/** A mapping from each source file to its destination */
	private Map<String, String> sourcePathsToTargetPaths;
	
	/** Whether or not to cleanup generated copies if a file fails to be copied */
	private boolean cleanUpOnFailure;
	
	/** Whether or not to overwrite existing files if name conflicts occur */
	private boolean overwriteExisting = false;
	
	/** Whether or not to append a timestamp to the file if name conflicts occur. */
	private boolean appendTimestamp = false;
    
    /**
     * Instantiates a new fetch files by extension.
     */
    public CopyWorkspaceFiles() {
    }
    
    /**
     * Creates a new action specifying the username, the files to copy, and whether to clean up on a failure
     * 
     * @param username the username of the user for which files are being copied
     * @param sourcePathsToTargetPaths a mapping from each source file to its destination
     * @param cleanUpOnFailure whether or not to cleanup generated copies if a file fails to be copied
     */
    public CopyWorkspaceFiles(String username, Map<String, String> sourcePathsToTargetPaths, boolean cleanUpOnFailure) {
        this.username = username;
        this.sourcePathsToTargetPaths = sourcePathsToTargetPaths;
        this.cleanUpOnFailure = cleanUpOnFailure;
    }
    
    /**
     * Creates a new action specifying the username, the files to copy, and whether to clean up on a failure
     * 
     * @param username the username of the user for which files are being copied
     * @param sourcePathsToTargetPaths a mapping from each source file to its destination
     * @param cleanUpOnFailure whether or not to cleanup generated copies if a file fails to be copied
     * @param overwriteExisting whether or not to overwrite existing files if name conflicts occur
     */
    public CopyWorkspaceFiles(String username, Map<String, String> sourcePathsToTargetPaths, boolean cleanUpOnFailure, boolean overwriteExisting) {
        this.username = username;
        this.sourcePathsToTargetPaths = sourcePathsToTargetPaths;
        this.cleanUpOnFailure = cleanUpOnFailure;
        this.setOverwriteExisting(overwriteExisting);
    }

    /**
     * Gets the username of the user for which files are being copied
     * 
     * @return the username of the user for which files are being copied
     */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username of the user for which files are being copied
	 * 
	 * @param username the username of the user for which files are being copied
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * Gets the unique identifier of the browser that is making the request
	 * @return the value of the browser session key, can be null
	 */
	public String getBrowserSessionKey() {
        return browserSessionKey;
    }
	
	/**
	 * Sets the unique identifier of the browser that is making the request
	 * @param browserSessionKey the new value of the browser session key, can be null
	 */
	public void setBrowserSessionKey(String browserSessionKey) {
        this.browserSessionKey = browserSessionKey;
    }

	/**
	 * Gets a mapping from each source file to its destination
	 * 
	 * @return a mapping from each source file to its destination
	 */
	public Map<String, String> getSourcePathsToTargetPaths() {
		return sourcePathsToTargetPaths;
	}

	/**
	 * Sets a mapping from each source file to its destination
	 * 
	 * @param sourcePathsToTargetPaths a mapping from each source file to its destination
	 */
	public void setSourcePathsToTargetPaths(Map<String, String> sourcePathsToTargetPaths) {
		this.sourcePathsToTargetPaths = sourcePathsToTargetPaths;
	}
	
	/**
	 * Whether or not a timestamp should be appended to the copied filename
	 * 
	 * @return True if a timestamp should be appended, false otherwise
	 */
	public boolean shouldAppendTimestamp() {
		return appendTimestamp;
	}

	/**
	 * Sets whether or not a timestamp should be appended to the copied filename
	 * 
	 * @param appendTimestamp True if a timestamp should be appended, false otherwise
	 */
	public void setAppendTimestamp(boolean appendTimestamp) {
		this.appendTimestamp = appendTimestamp;
	}
	
	/**
	 * Gets whether or not to cleanup generated copies if a file fails to be copied
	 * 
	 * @return whether or not to cleanup generated copies if a file fails to be copied
	 */
	public boolean isCleanUpOnFailure() {
		return cleanUpOnFailure;
	}

	/**
	 * Sets whether or not to cleanup generated copies if a file fails to be copied
	 * 
	 * @param cleanUpOnFailure whether or not to cleanup generated copies if a file fails to be copied
	 */
	public void setCleanUpOnFailure(boolean cleanUpOnFailure) {
		this.cleanUpOnFailure = cleanUpOnFailure;
	}

	/**
	 * Gets whether or not to overwrite existing files if name conflicts occur
	 * 
	 * @return whether or not to overwrite existing files if name conflicts occur
	 */
	public boolean isOverwriteExisting() {
		return overwriteExisting;
	}

	/**
	 * Sets whether or not to overwrite existing files if name conflicts occur
	 * 
	 * @param overwriteExisting whether or not to overwrite existing files if name conflicts occur
	 */
	public void setOverwriteExisting(boolean overwriteExisting) {
		this.overwriteExisting = overwriteExisting;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[CopyWorkspaceFiles: ");
        sb.append("username = ").append(username);
        sb.append(", browserSessionKey = ").append(browserSessionKey);
        sb.append(", sourcePathsToTargetPaths = ").append(sourcePathsToTargetPaths);
        sb.append(", cleanUpOnFailure = ").append(cleanUpOnFailure);
        sb.append("]");

        return sb.toString();
    } 
}
