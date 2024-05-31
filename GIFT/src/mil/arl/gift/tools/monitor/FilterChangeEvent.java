/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.util.EventObject;

/**
 * Used to notify FilterChangeListeners when a MessageFilter has changed.
 * @author cragusa
 *
 */
public class FilterChangeEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	/**
	 * @param source the MessageFilter that changed.
	 */
	public FilterChangeEvent(Object source) {
		super(source);
	}
}
