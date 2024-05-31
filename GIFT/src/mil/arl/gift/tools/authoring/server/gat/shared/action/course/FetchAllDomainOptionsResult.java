/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import java.util.ArrayList;
import java.util.Collection;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * Object type returned in response to a GetCourseOverviewModels action.
 * 
 * @author bzahid
 */
public class FetchAllDomainOptionsResult extends GatServiceResult {
		
	private ArrayList<DomainOption> domainOptionsList;
	
	/**
	 * Class constructor for serialization only.
	 */
	public FetchAllDomainOptionsResult() {
	}
		
	/**
	 * Returns a list of domain options.
	 * @return The list of domain options
	 */
	public ArrayList<DomainOption> getDomainOptionsList() {
		return domainOptionsList;
	}
	
	/**
	 * Returns a list of domain options.
	 * @return The list of domain options
	 */
	public void setDomainOptionsList(Collection<DomainOption> collection) {
		this.domainOptionsList = new ArrayList<DomainOption>(collection);
	}
}
