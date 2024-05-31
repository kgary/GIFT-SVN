
/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import mil.arl.gift.net.api.message.Message;

/**
 * Interface defining a MessageFilter.
 * @author cragusa
 *
 */
public interface MessageFilter {
	
	/**
	 * Tests a message against the filter.
	 * @param msg the message to test
	 * @return true if msg passes the filter, otherwise false
	 */
    boolean acceptMessage(Message msg);
	
	/**
	 * Adds a listener to the MessageFilter.
	 * @param l
	 */
	void addFilterChangeListener(FilterChangeListener l);
	
	/**
	 * Removes a listener from the MessageFilter.
	 * @param l
	 */
	void removeFilterChangeListener(FilterChangeListener l);

}
