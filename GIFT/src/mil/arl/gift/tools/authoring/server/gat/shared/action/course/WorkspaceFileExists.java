/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * 
 * Action that verifies a workspace file exists on the file system
 * 
 * @author cpadilla
 *
 */
public class WorkspaceFileExists implements Action<GatServiceResult> {
    
    /** The directory of the location of the file */
    private String filePath;
    
    /** Username of the user accessing the file system */
    private String username;
    
    /**
     * Constructor
     */
    public WorkspaceFileExists() {
        
    }
    
    /**
     * Constructor
     * 
     * @param username the value to initialize the username field to (cannot be empty or null)
     * @param filePath the value to initialize the filePath field to (cannot be empty or null)
     * @throws IllegalArgumentException if either of the parameters are empty or null
     */
    public WorkspaceFileExists(String username, String filePath) throws IllegalArgumentException {
        if(username.isEmpty() || username == null) {
            throw new IllegalArgumentException("The username cannot be empty or null");
        }
        if(filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("The filePath cannot be empty or null");
        }
        
        this.username = username;
        this.filePath = filePath;
    }
    
    /**
     * The getter for the filePath
     * @return the value of filePath (cannot be null or empty)
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * The getter for the username field
     * @return the value of username (cannot be null or empty)
     */
    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[WorkspaceFileExists: filePath=");
        builder.append(filePath);
        builder.append(", username=");
        builder.append(username);
        builder.append("]");
        return builder.toString();
    }
}
