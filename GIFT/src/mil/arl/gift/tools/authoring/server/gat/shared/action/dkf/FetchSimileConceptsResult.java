/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.dkf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The Class FetchSimileConceptsResult.
 */
public class FetchSimileConceptsResult extends GatServiceResult {

	/** The concept names. */
	private HashSet<String> concepts = new HashSet<String>();
	
	/**
	 * Gets the concept names.
	 *
	 * @return the concept names
	 */
	public ArrayList<String> getConcepts() {
		ArrayList<String> names = new ArrayList<String>(concepts);
		Collections.sort(names);
		return names;
	}
	
	/**
	 * Adds the concept names.
	 *
	 * @param concepts the concept names
	 */
	public void addConcepts(Collection<String> concepts) {
		this.concepts.addAll(concepts);
	}
	
}
