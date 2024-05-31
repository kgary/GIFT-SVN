/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.status;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.tools.dashboard.client.UiManager;

/**
 * A custom button used to launch modules
 * 
 * @author kcorbett 
 */

public class LaunchModuleButton extends AbstractModuleActionButton {
    
    
    /**
     * Starts one or more modules passed to button when clicked.
     * 
     * @param modules A list of moduleTypeEnum to be launched. 
     * Cannot be null or empty.
     */
    public LaunchModuleButton(final List<ModuleTypeEnum> modules) {
        super(modules);
        
        setDisableReason(modules.size() > 1
                ? "These modules cannot be launched because they are already running"
                : "This module cannot be launched because it is already running");
    }

    @Override
    public void onModuleAction(ClickEvent event) {
        
        UiManager.getInstance().getDashboardService().launchModules(
                UiManager.getInstance().getUserName(),
                UiManager.getInstance().getSessionId(), modules,
                new AsyncCallback<GenericRpcResponse<Void>>() {
                
                    @Override
                    public void onSuccess(GenericRpcResponse<Void> result) {
                        
                        if(!result.getWasSuccessful()) {
                            UiManager.getInstance().displayDetailedErrorDialog("Failed to Launch Module(s)", 
                                    result.getException());
                            
                            return;
                        }
                    }
                
                    @Override
                    public void onFailure(Throwable caught) {
                        UiManager.getInstance().displayDetailedErrorDialog("Failed to Launch Modules", 
                                "GIFT could not launch the specified modules(s) because an unexpected error occured", 
                                "An exception was thrown while launching " + modules + ": " + caught, 
                                null, null);
                    }
            });
    }
}
