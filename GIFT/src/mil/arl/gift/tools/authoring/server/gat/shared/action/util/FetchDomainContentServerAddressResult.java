/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * Class containing the domain content server address.
 * 
 * @author bzahid
 */
public class FetchDomainContentServerAddressResult extends GatServiceResult {
	
	/** The domain content server address. */
	private String domainContentServerAddress;

	/**
	 * Default constructor requried by GWT.
	 */
	public FetchDomainContentServerAddressResult() {
		super();
	}
	
	/**
	 * Initializes a new result.
	 */
	public FetchDomainContentServerAddressResult(String domainContentServerAddress) {
		super();
		this.domainContentServerAddress = domainContentServerAddress;
	}
	
	/**
	 * Gets the domain content server address.
	 * 
	 * @return the domain content server address
	 */
	public String getDomainContentServerAddress() {
		return domainContentServerAddress;
	}

	/**
	 * Sets the domain content server address.
	 * 
	 * @param domainContentServerAddress the address to set.
	 */
	public void setDomainContentServerAddress(String domainContentServerAddress) {
		this.domainContentServerAddress = domainContentServerAddress;
	}
}
