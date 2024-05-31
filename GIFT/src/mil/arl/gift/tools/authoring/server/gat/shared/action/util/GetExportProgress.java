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
 * An action that gets the progress of a user's ongoing export task, if one exists
 * 
 * @author bzahid
 */
public class GetExportProgress implements Action<GetProgressResult> {
	
    private String userName;
    
    /**
     * Instantiates a new action
     */
    public GetExportProgress() {
        super();
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

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[GetExportProgress: ");
        sb.append("userName = ").append(userName);
        sb.append("]");

        return sb.toString();
    } 
}
