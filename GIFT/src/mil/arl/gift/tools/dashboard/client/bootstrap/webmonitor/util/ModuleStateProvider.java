/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.gwt.client.util.AbstractProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.ModuleStateProvider.ModuleStatusChangeHandler;

/**
 * A provider that provides module status informationt to any registered handlers
 * 
 * @author nroberts
 */
public class ModuleStateProvider extends AbstractProvider<ModuleStatusChangeHandler> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ModuleStateProvider.class.getName());

    /** The instance of this class */
    private static ModuleStateProvider instance = null;

    /** A mapping from each module to the queues associated with it */
    private Map<ModuleTypeEnum, List<String>> moduleTypeToQueues = new HashMap<>();

    /**
     * Singleton constructor
     */
    private ModuleStateProvider() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }
    }

    /**
     * Accessor to the provider singleton object. If it doesn't exist yet it
     * will be created.
     * 
     * @return the instance to the provider singleton object.
     */
    public static ModuleStateProvider getInstance() {
        if (instance == null) {
            instance = new ModuleStateProvider();
        }

        return instance;
    }

    /**
     * Updates the module statuses stored by this provider and sends and notifies all
     * registered handlers that said statuses have changed
     * 
     * @param moduleTypeToQueues the new module statuses. Cannot be null.
     */
    public void setModuleStatuses(final Map<ModuleTypeEnum, List<String>> moduleTypeToQueues) {
        boolean changed = !this.moduleTypeToQueues.equals(moduleTypeToQueues);
        this.moduleTypeToQueues.clear();
        this.moduleTypeToQueues.putAll(moduleTypeToQueues);

        if (changed) {
            /* Notify handlers */
            executeHandlers(new SafeHandlerExecution<ModuleStatusChangeHandler>() {
                @Override
                public void execute(ModuleStatusChangeHandler handler) {
                    handler.onModuleStatusChanged(ModuleStateProvider.this.moduleTypeToQueues);
                }
            });
        }
    }
    
    /**
     * Gets the module statuses stored by this provider, i.e. a mapping from each module type
     * to the queues associated with it
     * 
     * @return the current module statuses. Will not be null, but can be empty.
     */
    public Map<ModuleTypeEnum, List<String>> getModuleStatuses(){
        return moduleTypeToQueues;
    }

    /**
     * A handler used to handle module status updates
     * 
     * @author nroberts
     */
    public interface ModuleStatusChangeHandler {
        
        /**
         * Notifies this listener that the given module statuses have changed
         * 
         * @param moduleTypeToQueues the new module statuses. Canot be null.
         */
        void onModuleStatusChanged(Map<ModuleTypeEnum, List<String>> moduleTypeToQueues);
    }
}
