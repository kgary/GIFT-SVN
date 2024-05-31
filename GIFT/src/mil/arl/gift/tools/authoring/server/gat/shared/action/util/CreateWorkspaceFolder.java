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
 * An action that initiates logic to create a folder in the workspace folder
 */
public class CreateWorkspaceFolder implements Action<GatServiceResult> {
	
	/** The username of the user this action is being executed for. Needed for authentication. */
	private String username;

	
	/** The location of the parent folder in the workspace folder where the new folder should be created */
	private String parentWorkspaceLocation;
	
	/** The name of the folder to create */
	private String folderName;
	
	/** Whether or not prexisting folders should be overwritten */
	private boolean ignoreExistingFolder;
	
	/**
	 * Default public constructor needed for serialization. Does not create a valid action.
	 */
	public CreateWorkspaceFolder(){
		
	}
	
	/**
	 * Creates a new action to create a folder in the workspace folder
	 * 
	 * @param username the username of the user this action is being executed for
	 * @param parentWorkspaceLocation the directory to create the folder in.  This will use the entire path of the tree include all ancestors.  The root of the 
     * tree must be relative to the workspace directory (i.e. Domain/workspace/, /default-domain/workspaces/) but not include it.
	 * @param folderName name the name of the folder
	 * @param ignoreExistingFolder whether or not to ignore if the folder already exists, i.e. don't create the folder if it already exists
	 */
	public CreateWorkspaceFolder(String username, String parentWorkspaceLocation, String folderName, boolean ignoreExistingFolder){
		this.username = username;
		this.parentWorkspaceLocation = parentWorkspaceLocation;
		this.folderName = folderName;
		this.ignoreExistingFolder = ignoreExistingFolder;
	}
	
    
	/**
	 * Gets the username of the user this action is being executed for
	 * 
	 * @return the username of the user this action is being executed for
	 */
	public String getUsername(){
		return username;
	}

	/**
	 * Gets the location of the parent folder in the workspace folder where the new folder should be created
	 * 
	 * @return the location of the parent folder in the workspace folder where the new folder should be created
	 */
	public String getParentWorkspaceLocation() {
		return parentWorkspaceLocation;
	}

	/**
	 * Gets the name of the folder to create
	 * 
	 * @return the name of the folder to create
	 */
	public String getFolderName() {
		return folderName;
	}

	/**
	 * Gets whether or not prexisting folders should be overwritten
	 * 
	 * @return whether or not prexisting folders should be overwritten
	 */
	public boolean ignoreExistingFolder() {
		return ignoreExistingFolder;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[CreateWorkspaceFolder: ");
        sb.append("username = ").append(username);
        sb.append(", parentWorkspaceLocation = ").append(parentWorkspaceLocation);
        sb.append(", folderName = ").append(folderName);
        sb.append(", ignoreExistingFolder = ").append(ignoreExistingFolder);
        sb.append("]");

        return sb.toString();
    } 
}
