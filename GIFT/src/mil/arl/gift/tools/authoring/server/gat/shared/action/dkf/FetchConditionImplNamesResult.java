/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.dkf;

import java.util.List;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The Class FetchConditionImplementationResult.
 */
public class FetchConditionImplNamesResult extends GatServiceResult {

	/** The class names. */
	private List<String> classNames;
	

	/**
	 * Gets the class names.
	 *
	 * @return the class names
	 */
	public List<String> getClassNames() {
		return classNames;
	}
	
	/**
	 * Sets the class names.
	 *
	 * @param list the new class names
	 */
	public void setClassNames(List<String> list) {
		this.classNames = list;
	}	
}
