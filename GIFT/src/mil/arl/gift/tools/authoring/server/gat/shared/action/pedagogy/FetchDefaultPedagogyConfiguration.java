/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.pedagogy;
 
import generated.ped.EMAP;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import net.customware.gwt.dispatch.shared.Action;
 
/**
 * A request asking the dispatch service to get the pedagogical configuration template object
 * 
 * @author nroberts
 */
public class FetchDefaultPedagogyConfiguration implements Action<GenericGatServiceResult<EMAP>> {
	
	/** The user name. */
	private String userName;
    
    /** 
     * Default public constructor. 
     */
    public FetchDefaultPedagogyConfiguration() {
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
        sb.append("[FetchDefaultPedagogyConfiguration: ");
        sb.append("userName = ").append(userName);
        sb.append("]");

        return sb.toString();
    } 
}
