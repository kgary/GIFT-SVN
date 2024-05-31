/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.module;

import java.util.HashMap;
import java.util.Map;

//TODO: move this map logic to UMS module since the timer logic has been refactored
/**
 * This class is responsible for checking whether modules are still sending
 * module status messages.  If a module is determined to be timed out, it will
 * be removed from the last status container.
 * 
 * @author mhoffman
 *
 */
public class ModuleStatusCollectionHelper{
    
	/** mapping of unique module address to it's last status */
	private Map<String, ModuleStatus> moduleAddressToLastStatus = new HashMap<String, ModuleStatus>();
	
	/**
	 * Class constructor
	 */
	public ModuleStatusCollectionHelper(){
		
	}
	
	/**
	 * A module is no longer active and needs to be removed from this collection
	 * 
	 * @param address - the module's address whose connection is no longer active
	 */
	public void removeModuleStatus(String address){
	    moduleAddressToLastStatus.remove(address);
	}
	
	/**
	 * A module status update was received, update the last module status information
	 *
	 * @param moduleStatus - a module's status information
	 */
	public void receivedModuleStatus(ModuleStatus moduleStatus){
		
		//update last status
	    moduleAddressToLastStatus.put(moduleStatus.getQueueName(), moduleStatus);
	}
	
	/**
	 * Return the mapping of unique module address to it's last status 
	 * 
	 * @return Map<String, ModuleStatus>
	 */
	public Map<String, ModuleStatus> getModulesStatus(){
		return moduleAddressToLastStatus;
	}

}
