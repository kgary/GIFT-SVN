/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api;

import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.module.ModuleStatusMonitor.StatusReceivedInfo;

/**
 * Interface defining the method required by classes needing to receive
 * notification of allocated module changes.  An allocated module is a module that has been
 * allocated to another module during a user session.
 * 
 * @author cragusa
 *
 */
public interface AllocatedModuleListener {
	
	/**
	 * Notification that a connection to an allocated module has been removed. 
	 * 
	 * @param userId - the user id for a user session where the module was allocated to another module.
	 * @param moduleType - the module type whose connection is being removed.
	 * @param lastStatus - the last status from that module
	 */
	void allocatedModuleRemoved(int userId, ModuleTypeEnum moduleType, StatusReceivedInfo lastStatus); 
}
