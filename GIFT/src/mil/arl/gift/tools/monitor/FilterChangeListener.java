/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

/**
 * Defines the interface used to notify listeners that MessageFilter has changed.
 * @author cragusa
 *
 */
public interface FilterChangeListener {
	
	/**
	 * Callback that a FilterChangeListener receives when a FilterChangeEvent occurs. 
	 * @param event 
	 */
	void filterChanged(FilterChangeEvent event);
}
