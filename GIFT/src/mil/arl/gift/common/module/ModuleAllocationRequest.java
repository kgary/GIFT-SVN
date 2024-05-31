/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.enums.ModuleTypeEnum;

/**
 * This class contains the necessary information for a module to request the use of another module.  In addition
 * the requesting module provides information about the modules already allocated to it.
 * 
 * @author mhoffman
 *
 */
public class ModuleAllocationRequest {

    /** info about the requesting module */
    private ModuleStatus requestor;
    
    /** collection of modules already allocated to the requesting module */
    private Map<ModuleTypeEnum, ModuleStatus> allocatedModules;
    
    /**
     * Class constructor 
     * 
     * @param requestor - info about the requesting module
     */
    public ModuleAllocationRequest(ModuleStatus requestor){
        setRequestorInfo(requestor);
    }
    
    private void setRequestorInfo(ModuleStatus requestor){
        
        if(requestor == null){
            throw new IllegalArgumentException("The requestor status can't be null");
        }
        
        this.requestor = requestor;
    }
    
    /**
     * Return information about the requesting module
     * 
     * @return ModuleStatus
     */
    public ModuleStatus getRequestorInfo(){
        return requestor;
    }
    
    /**
     * Set the collection of modules already allocated to the requesting module
     * 
     * @param allocatedModulesList - collection of modules already allocated
     */
    public void setAllocatedModule(List<ModuleStatus> allocatedModulesList){
        
        if(allocatedModulesList == null){
            throw new IllegalArgumentException("The list of allocated modules can't be null");
        }
        
        //build map
        if(allocatedModules == null){
            allocatedModules = new HashMap<ModuleTypeEnum, ModuleStatus>();
        }
        
        allocatedModules.clear();
        for(ModuleStatus status : allocatedModulesList){
            allocatedModules.put(status.getModuleType(), status);
        }

    }
    
    /**
     * Set the collection of modules already allocated to the requesting module
     * 
     * @param allocatedModules - collection of modules already allocated
     */
    public void setAllocatedModule(Map<ModuleTypeEnum, ModuleStatus> allocatedModules){
        
        if(allocatedModules == null){
            throw new IllegalArgumentException("The list of allocated modules can't be null");
        }
        
        this.allocatedModules = allocatedModules;
    }
    
    /**
     * Get the collection of modules already allocated to the requesting module
     * 
     * @return Map<ModuleTypeEnum, ModuleStatus> - won't be null
     */
    public Map<ModuleTypeEnum, ModuleStatus> getAllocatedModules(){
        return allocatedModules;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ModuleAllocationRequest: ");
        sb.append("requestor = ").append(requestor);
        
        sb.append(", allocatedModules = {");
        for(ModuleStatus mStatus : allocatedModules.values()){
            sb.append(mStatus.toString()).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        
        return sb.toString();
    }
}
