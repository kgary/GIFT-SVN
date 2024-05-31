/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.learner;
 
import generated.learner.LearnerConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import net.customware.gwt.dispatch.shared.Action;
 
/**
 * A request asking the dispatch service to get the learner configuration template object
 * 
 * @author nroberts
 */
public class FetchDefaultLearnerConfiguration implements Action<GenericGatServiceResult<LearnerConfiguration>> {
	
	/** The user name. */
	private String userName;
    
    /** 
     * Default public constructor. 
     */
    public FetchDefaultLearnerConfiguration() {
        super();
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
        sb.append("[FetchDefaultLearnerConfiguration: ");
        sb.append("userName = ").append(userName);
        sb.append("]");

        return sb.toString();
    } 
}
