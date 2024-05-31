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
 * An action that cancels a user's ongoing import task, if one exists
 * 
 * @author nroberts
 */
public class CancelImport implements Action<GatServiceResult> {
	
    private String userName;
    
    /**
     * Instantiates a new action
     */
    public CancelImport() {
        super();
    }
    
	/**
	 * Accessor to get the username
	 * 
	 * @return - The name of the user for this cancel operation.
	 */
	public String getUserName() {
        return userName;
    }
    
	/**
	 * Accessor to set the username.
	 * 
	 * @param user - The name of the user for this cancel operation.
	 */
    public void setUserName(String user) {
        userName = user;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[CancelImport: ");
        sb.append("userName = ").append(userName);
        sb.append("]");

        return sb.toString();
    } 
}
