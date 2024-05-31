/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.util.List;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * A result containing a list of strings
 * 
 * @author nroberts
 */
public class StringListResult extends GatServiceResult {
	
	private List<String> strings;

    /**
	 * Class constructor
	 * For serialization only.
	 */
	public StringListResult() {
		
    }

	/**
	 * Gets the list of strings
	 * 
	 * @return the list of strings
	 */
	public List<String> getStrings() {
		return strings;
	}

	/**
	 * Sets the list of strings
	 * 
	 * @param strings the list of strings
	 */
	public void setStrings(List<String> strings) {
		this.strings = strings;
	}	
    
	 @Override
	 public String toString() {
	     StringBuilder builder = new StringBuilder();
	     builder.append("[StringListResult: strings=");
	     builder.append(strings);
	     builder.append(", ");
	     builder.append(super.toString());
	     builder.append("]");
	     return builder.toString();
	 }
}
