/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server.webmonitor.message;

import java.util.List;

import mil.arl.gift.common.enums.MessageTypeEnum;

/**
 * Defines the interface used to notify listeners that MessageFilter has changed.
 * 
 * @author nroberts
 */
public interface FilterChangeListener {
	
	/**
	 * Callback that a FilterChangeListener receives when a FilterChangeEvent occurs.
	 *  
	 * @param event the event indicating that the filter was changed. Cannot be null.
	 */
	void filterChanged(FilterChangeEvent event);
	
	/**
	 * Indicates that the list of available or selected message type choices for the filter
	 * has changed
	 * 
	 * @param allChoices all of the available message types to pick from. Cannot be null.
	 * @param selectedChoices the message types to display. Cannot be null.
	 */
	void filterChoicesChanged(List<MessageTypeEnum> allChoices, List<MessageTypeEnum> selectedChoices);
}
