/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.module;

import mil.arl.gift.common.enums.ModuleStateEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;

/**
 * This class contains the status of a module
 * 
 * @author mhoffman
 *
 */
public class ModuleStatus {

    /** the module's name sending the status (doesn't have to be unique among all modules, even of the same module type) 
     * This is currently configurable from module property files.
     * e.g. Pedagogical_Module
     */
    private String moduleName;

    /** the module's queue name (i.e. unique message bus address to send messages too that are for this module) 
     * This is usually the address to communicate with this module.  Right now
     * this includes the {module name + module's IP address + "Inbox"}.
     */
    private String queueName;

    /** the module's type of the module reporting this status */
    private ModuleTypeEnum moduleType = null;
    
    /** the module state for this status */
    private ModuleStateEnum moduleState = ModuleStateEnum.UNKNOWN;

    /**
     * Class constructor - set various attributes
     * 
     * @param moduleName the module name sending the status.  This is currently configurable from module property files.
     *  (doesn't have to be unique among all modules, even of the same module type). Can't be null.
     * e.g. Pedagogical_Module
     * @param queueName the module's queue name.  This is usually the address to communicate with this module.  Right now
     * this includes the {module name + module's IP address + "Inbox"}. Can't be null.
     * e.g. Pedagogical_Queue:10.3.82.1:Inbox
     * @param moduleType the enumerated module's type. Can't be null. 
     */
    public ModuleStatus(String moduleName, String queueName, ModuleTypeEnum moduleType){
        
        if(moduleName == null){
            throw new IllegalArgumentException("The module name can't be null.");
        }
        
        if(queueName == null){
            throw new IllegalArgumentException("The queue name can't be null.");
        }
        
        if(moduleType == null){
            throw new IllegalArgumentException("The module type can't be null.");
        }
        
        this.moduleName = moduleName;
        this.queueName = queueName;  
        this.moduleType = moduleType;   
    }
    
    /**
     * Set the module state value for this status.
     * 
     * @param state the module state for this status. Can't be null.
     */
    public void setModuleState(ModuleStateEnum state){
        
        if(state == null){
            throw new IllegalArgumentException("The state can't be null.");
        }
        
        this.moduleState = state;
    }
    
    /**
     * Return the module state for this status
     * 
     * @return the enumerated state.  Won't be null.  Default is ModuleStateEnum.UNKNOWN.
     */
    public ModuleStateEnum getState(){
        return moduleState;
    }

    /**
     * Return the module name that is sending the status
     * 
     * @return the module name sending the status.  This is currently configurable from module property files.
     *  (doesn't have to be unique among all modules, even of the same module type). Won't be null.
     * e.g. Pedagogical_Module
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Return the module's queue name
     * 
     * @return the module's queue name.  This is usually the address to communicate with this module.  Right now
     * this includes the {module name + module's IP address + "Inbox"}. Won't be null.
     * e.g. Pedagogical_Queue:10.3.82.1:Inbox
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Return the module type
     * 
     * @return the enumerated module type in the architecture. Won't be null.
     */
    public ModuleTypeEnum getModuleType(){
        return moduleType;
    }
    
    @Override
    public boolean equals(Object other){        
        return other != null && other instanceof ModuleStatus && ((ModuleStatus)other).getQueueName().equals(this.getQueueName());
    }
    
    @Override
    public int hashCode(){
        return getQueueName().hashCode();
    }
    
    /**
     * Create a deep copy of this class
     * 
     * @return a new module status instance
     */
    public ModuleStatus copy(){
        return new ModuleStatus(new String(getModuleName()), new String(getQueueName()), getModuleType());
    }
    
    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[ModuleStatus: ");
        sb.append("moduleName = ").append(getModuleName());
        sb.append(", queueName = ").append(getQueueName());
        sb.append(", moduleType = ").append(getModuleType());
        sb.append(", moduleState = ").append(getState());
        sb.append("]");

        return sb.toString();
    }
}
