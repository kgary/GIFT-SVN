/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for retrieving the domain options. Domain options will be checked for validation.
 *
 * @author bzahid
 *
 */
public class FetchAllDomainOptions implements Action<FetchAllDomainOptionsResult>{

	/** The user name. */
	private String userName;
	
    /**
     * No-arg constructor. Needed for serialization.
     */
	public FetchAllDomainOptions() {
	}
	
	/**
	 * Gets the user name.
	 * @return User name.
	 */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * Sets the user name.
	 * @param userName User name.
	 */
	public void setUserName(String username) {
		this.userName = username;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[FetchAllDomainOptions: ");
        sb.append("userName = ").append(userName);
        sb.append("]");

        return sb.toString();
    } 
}
