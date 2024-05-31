/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.shared.messages.monitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.enums.ModuleTypeEnum;

/**
 * An update indicating that the status of the GIFT modules has changed
 * 
 * @author nroberts
 */
public class ModuleStatusUpdate implements WebMonitorUpdate {

    private static final long serialVersionUID = 1L;
    
    /** A mapping from each module type to the queue names associated with it */
    private Map<ModuleTypeEnum, List<String>> moduleToQueueNames = new HashMap<>();

    /**
     * A default constructor required for RPC serialization
     */
    private ModuleStatusUpdate() {
        
    }

    /**
     * Creates a new module status update
     * 
     * @param mapping from each module type to the queue names associated with it. Cannot be null.
     */
    public ModuleStatusUpdate(Map<ModuleTypeEnum, List<String>> moduleToQueueNames) {
        this();
        
        if(moduleToQueueNames == null) {
            throw new IllegalArgumentException("A module status update must contain a map of module statuses ");
        }
        
        this.moduleToQueueNames = moduleToQueueNames;
    }

    /**
     * Gets the status of each of the modules and their associated queues
     * 
     * @return the module status. Cannot be null.
     */
    public Map<ModuleTypeEnum, List<String>> getModuleToQueueNames() {
        return moduleToQueueNames;
    }
}
