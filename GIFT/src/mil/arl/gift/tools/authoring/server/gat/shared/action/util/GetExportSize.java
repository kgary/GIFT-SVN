/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.util.List;

import mil.arl.gift.common.DomainOption;
import net.customware.gwt.dispatch.shared.Action;

/**
 * An action that gets the size of the export
 * 
 * @author bzahid
 */
public class GetExportSize implements Action<GetExportSizeResult> {

	private String userName;
	private List<DomainOption> selectedDomainOptions;
	
	/**
     * Instantiates a new action
     */
	public GetExportSize() {
		super();
	}
	
	/**
	 * Accessor to get the username
	 * 
	 * @return - The name of the user for this export operation.
	 */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * Accessor to set the username.
	 * 
	 * @param user - The name of the user for this export operation.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	/**
	 * Get the list of domain options to export
	 * 
	 * @return the domain options to export
	 */
	public List<DomainOption> getSelectedDomainOptions() {
		return selectedDomainOptions;
	}
	
	/**
	 * Sets the list of domain options to export 
	 * 
	 * @param domainOptions the domain options to export
	 */
	public void setSelectedDomainOptions(List<DomainOption> domainOptions) {
		selectedDomainOptions = domainOptions;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[GetExportSize: ");
        sb.append("userName = ").append(userName);
        sb.append(", # selected domain options = ").append((selectedDomainOptions != null ? selectedDomainOptions.size() : "null"));
        if (selectedDomainOptions != null) {
        	sb.append(" : [");
        	for(int i=0; i < selectedDomainOptions.size(); i++) {
        		if (i > 0) {
        			sb.append(", ");
        		}
        		sb.append(selectedDomainOptions.get(i));
        	}
        	sb.append("]");
        }
        sb.append("]");

        return sb.toString();
    } 
}
