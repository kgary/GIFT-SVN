/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;
 
import net.customware.gwt.dispatch.shared.Action;
 
/**
 * An action that initiates logic to move a file from the Domain folder to some location in the workspace folder
 */
public class MoveDomainFileToWorkspaceLocation implements Action<MoveDomainFileToWorkspaceLocationResult> {
	
	/** The username of the user this action is being executed for. Needed for authentication. */
	private String username;

	/** The path to the file in the Domain folder, relative to the Domain folder */
	private String domainFilePath;
	
	/** The location in the workspace folder where the file should be moved */
	private String workspaceLocation;
	
	/** Whether or not to overwrite a file at the target location if a name conflict occurs */
	boolean overwriteExisting = false;
	
	/**
	 * Default public constructor needed for serialization. Does not create a valid action.
	 */
	public MoveDomainFileToWorkspaceLocation(){
		
	}
	
	/**
	 * Creates a new action to initiate logic to move a file from the Domain folder to some location in the workspace folder
	 * 
	 * @param username the username of the user this action is being executed for
	 * @param domainFilePath the path to the file in the Domain folder
	 * @param workspaceLocation  the location in the workspace folder where the file should be moved
	 */
	public MoveDomainFileToWorkspaceLocation(String username, String domainFilePath, String workspaceLocation){
		this.domainFilePath = domainFilePath;
		this.workspaceLocation = workspaceLocation;
		this.username = username;
	}
	
	/**
	 * Creates a new action to initiate logic to move a file from the Domain folder to some location in the workspace folder
	 * 
	 * @param username the username of the user this action is being executed for
	 * @param domainFilePath the path to the file in the Domain folder
	 * @param workspaceLocation  the location in the workspace folder where the file should be moved
	 * @param overwriteExisiting whether or not to overwrite a file at the target location if a name conflict occurs
	 */
	public MoveDomainFileToWorkspaceLocation(String username, String domainFilePath, String workspaceLocation, boolean overwriteExisiting){
		this.domainFilePath = domainFilePath;
		this.workspaceLocation = workspaceLocation;
		this.username = username;
		this.overwriteExisting = overwriteExisiting;
	}
	
	/**
	 * Gets the path to the file in the Domain folder
	 * 
	 * @return the path to the file in the Domain folder
	 */
	public String getDomainFilePath(){
		return domainFilePath;
	}
	
	/**
	 * Gets the location in the workspace folder where the file should be moved
	 * 
	 * @return the location in the workspace folder where the file should be moved
	 */
	public String getWorkspaceLocation(){
		return workspaceLocation;
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
	 * Gets whether or not to overwrite a file at the target location if a name conflict occurs
	 * 
	 * @return whether or not to overwrite a file at the target location if a name conflict occurs
	 */
	public boolean getOverwriteExisiting(){
		return overwriteExisting;
	}
	
	@Override
    public String toString(){
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("[MoveDomainFileToWorkspaceLocation: ");
	    sb.append("username = ").append(getUsername());
	    sb.append(", overwriteExisting = ").append(getOverwriteExisiting());
	    sb.append(", workspaceLocation = ").append(getWorkspaceLocation());
	    sb.append(", domainFilePath = ").append(getDomainFilePath());
	    sb.append("]");
	    return sb.toString();
	}
}
