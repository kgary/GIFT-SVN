/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.module;

import mil.arl.gift.common.module.ModuleStatusMonitor.StatusReceivedInfo;

/**
 * Interface defining the method required by classes needing to receive
 * notification of ModuleStatus changes.
 * @author cragusa
 *
 */
public interface ModuleStatusListener {

	/**
	 * A ModuleStatus instance was added.  This will only be called on a listener that was added
	 * before the first time a module is discovered.  If the listener can't be guaranteed to be
	 * added before the first module status is received, then the {@link #moduleStatusChanged(long, ModuleStatus)}
	 * should be used instead.
	 * 
	 * @param sentTime - the time at which the status was created/sent
	 * @param status the ModuleStatus object that was added.
	 */
	void moduleStatusAdded(long sentTime, ModuleStatus status); 
	
	/**
	 * Notification that a ModuleStatus object has changed.
	 * 
	 * @param sentTime - the time at which the status was created/sent
	 * @param status the new module status
	 */
	void moduleStatusChanged(long sentTime, ModuleStatus status);
	
	/**
	 * Notification that a ModuleStatus object was removed. 
	 * 
	 * @param status The ModuleStatus object that was removed.
	 */
	void moduleStatusRemoved(StatusReceivedInfo status); 
}
