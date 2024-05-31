/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.dkf;

import net.customware.gwt.dispatch.shared.Action;

/**
 * The Class FetchSimileConcepts.
 */
public class FetchSimileConcepts  implements Action<FetchSimileConceptsResult>{
	
	/** The configuration file path. */
	private String configurationFilePath;
	
	/** The user name. */
	private String userName;

	/**
	 * Gets the configuration file path.
	 *
	 * @return the configuration file path
	 */
	public String getConfigurationFilePath() {
		return configurationFilePath;
	}

	/**
	 * Sets the configuration file path.
	 *
	 * @param configurationFilePath the new configuration file path
	 */
	public void setConfigurationFilePath(String configurationFilePath) {
		this.configurationFilePath = configurationFilePath;
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
	public void setUserName(String userName) {
		this.userName = userName;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[FetchSimileConcepts: ");
        sb.append("configurationFilePath = ").append(configurationFilePath);
        sb.append(", userName = ").append(userName);
        sb.append("]");

        return sb.toString();
    } 
}
