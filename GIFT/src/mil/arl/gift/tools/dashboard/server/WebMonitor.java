/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;


import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.ModuleStateEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.module.ModuleStatusListener;
import mil.arl.gift.common.module.ModuleStatusMonitor;
import mil.arl.gift.common.module.ModuleStatusMonitor.StatusReceivedInfo;

/**
 * This is a web based version of the Monitor class that is responsible for listenting to the status of the activemq modules.
 * This can be used to listen for status changes in the modules, and update any connected clients that may be showing
 * module status.
 * 
 * @author nblomberg
 *
 */
public class WebMonitor implements ModuleStatusListener {

    /** Instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(WebMonitor.class);

    /** Constructor
     * 
     * @param userWebSession session this class is processing for
     */
    public WebMonitor() {

        ModuleStatusMonitor.getInstance().addListener(this);
    }

    @Override
    public void moduleStatusAdded(long sentTime, ModuleStatus status) {
       
        if(!WebMonitorModule.getInstance().getModuleQueues().containsKey(status.getModuleName())) {
            WebMonitorModule.getInstance().getModuleQueues().put(status.getModuleName(), new HashSet<String>());
        }
        WebMonitorModule.getInstance().getModuleQueues().get(status.getModuleName()).add(status.getQueueName());
        
        if(logger.isDebugEnabled()){
            logger.debug("adding module status module name ("+status.getModuleType()+"), queue name ("+status.getQueueName()+"), state ("+status.getState()+")");
        }
      
        WebMonitorModule.getInstance().getModuleStatusMap().put(status.getModuleType(),status.getState());
    }

    @Override
    public void moduleStatusChanged(long sentTime, ModuleStatus status) {
        // Do nothing here
    }

    @Override
    public void moduleStatusRemoved(StatusReceivedInfo status) {
        String moduleName = status.getModuleStatus().getModuleName();

        HashSet<String> queues = WebMonitorModule.getInstance().getModuleQueues().get(moduleName);
        if(queues != null) {
            queues.remove(status.getModuleStatus().getQueueName());
        }
        
        // Remove the module from the module status map.
        Map<ModuleTypeEnum, ModuleStateEnum> moduleStatusMap =  WebMonitorModule.getInstance().getModuleStatusMap();
        moduleStatusMap.remove(status.getModuleStatus().getModuleType());
        if(logger.isDebugEnabled()){
            logger.debug("module status removed for module: "+ status.getModuleStatus().getModuleName());
        }
    }

}
