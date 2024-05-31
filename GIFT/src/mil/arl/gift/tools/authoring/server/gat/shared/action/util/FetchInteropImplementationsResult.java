/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.util.ArrayList;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The Class GetInteropImplementationsResult.
 */
public class FetchInteropImplementationsResult extends GatServiceResult {
		
	/** The interop implementations. */
	private ArrayList<String> interopImplementations;
	
	/**
	 * Instantiates a new gets the interop implementations result.
	 */
	public FetchInteropImplementationsResult() {
		super();
	}
	
	/**
	 * Instantiates a new gets the interop implementations result.
	 *
	 * @param interopImplementations the interop implementations
	 */
	public FetchInteropImplementationsResult(ArrayList<String> interopImplementations) {
		super();
		this.interopImplementations = interopImplementations;
	}

	/**
	 * Gets the interop implementations.
	 *
	 * @return the interop implementations
	 */
	public ArrayList<String> getInteropImplementations() {
		return interopImplementations;
	}

	/**
	 * Sets the interop implementations.
	 *
	 * @param interopImplementations the new interop implementations
	 */
	public void setInteropImplementations(ArrayList<String> interopImplementations) {
		this.interopImplementations = interopImplementations;
	}
	
}
