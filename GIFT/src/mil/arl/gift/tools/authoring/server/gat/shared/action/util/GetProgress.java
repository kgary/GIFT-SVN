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
 * An action that retrieves progress from the server for 
 * delete, copy, and move file operations.
 * 
 * @author bzahid
 */
public class GetProgress implements Action<GetProgressResult> {

	/** Enum used to determine the type of progress to retrieve from the server. */
	public static enum ProgressType {
		DELETE, 
		SLIDE_SHOW, 
		UNZIP;
	}
	
	private ProgressType progressType = ProgressType.DELETE;
	
	/** The user performing this operation. */
	private String userName;

	/**
	 * Default constructor.
	 */
	public GetProgress() {
		super();
	}
	
	/**
	 * Initializes a new action for the specified task.
	 * 
	 * @param progressType The task this action should perform.<br/>
	 * Task.DELETE will retrieve delete progress.
	 * Task.COPY will retrieve copy progress.
	 */
	public GetProgress(ProgressType progressType) {
		super();
		this.progressType = progressType;
	}

	/**
	 * Accessor to get the username
	 * 
	 * @return - The name of the user for this operation.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Accessor to set the username.
	 * 
	 * @param user - The name of the user for this operation.
	 */
	public void setUserName(String user) {
		userName = user;
	}

	/**
	 * Gets the progress type.
	 * 
	 * @return the type of progress to retrieve.
	 */
	public ProgressType getTask() {
		return progressType;
	}
	
	/**
	 * Sets the progress type.
	 * 
	 * @param progressType - the type of progress to retrieve
	 * specified by ProgressType.DELETE, ProgressType.COPY, or ProgressType.MOVE 
	 */
	public void setTask(ProgressType progressType) {
		this.progressType = progressType;
	}
	
    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[GetProgress: ");
        sb.append("progressType = ").append(progressType);
        sb.append(", userName = ").append(userName);
        sb.append("]");

        return sb.toString();
    } 
	
}
