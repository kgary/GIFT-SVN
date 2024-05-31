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
 * An action that retrieves the remaining disk space for a user workspace.
 * 
 * @author bzahid
 */
public class GetRemainingDiskSpace implements Action<GetRemainingDiskSpaceResult> {
	
	/** The user to retrieve disk space for. */
	private String userName;
	
	/**
	 * Default constructor.
	 */
	public GetRemainingDiskSpace() {
		super();
	}

	/**
	 * Initializes a new action.
	 * 
	 * @param userName The user to retrieve the remaining disk space for.
	 */
	public GetRemainingDiskSpace(String userName) {
		this.userName = userName;
	}

	/**
	 * Gets the username
	 * 
	 * @return the username
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the username
	 * 
	 * @param userName the username
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[GetRemainingDiskSpace: ");
        sb.append("userName = ").append(userName);
        sb.append("]");

        return sb.toString();
    } 
	
}
