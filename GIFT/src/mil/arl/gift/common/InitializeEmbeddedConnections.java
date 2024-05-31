/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The a message used to initialize/enable specific embedded applications for a lesson/domain-session.
 * 
 * @author tflowers
 */
public class InitializeEmbeddedConnections {
	
	/** The URLs of the embedded applications to be initialized/enabled */
	private List<String> urls;

	/**
	 * Creates a new instance of this message with the given embedded application URLs
	 * 
	 * @param urls the URLs used to host the embedded applications at runtime. Can't be null.  Currently
     * can't be empty.
	 */
	public InitializeEmbeddedConnections(Collection<String> urls) {
		
		//Verifies that the collection of urls has at least one url
		if(urls == null || urls.isEmpty()) {
			throw new IllegalArgumentException("The parameter urls must not be null or empty");
		}
		
		this.urls = new ArrayList<String>(urls);
		
	}
	
	/**
	 * Gets the URLs for the embedded applications to be initialized/enabled.
	 * 
	 * @return the list of embedded apllication URLs. Cannot be null or emtpy
	 */
	public List<String> getUrls() {
		return urls;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer
			.append("[InitializeEmbeddedConnections: ")
			.append("{ ");
		
		for(String url : urls) {
			buffer
				.append(url)
				.append(",");
		}
		
		buffer
			.append("}")
			.append("]");
		
		return buffer.toString();
	}
}
