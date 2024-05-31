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
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;

/**
 * A custom button used to kill modules
 * 
 * @author kcorbett
 */
public class KillModuleButton extends AbstractModuleActionButton {
    
    /**
     * Kills one or more modules passed to button when clicked.
     * 
     * @param modules A list of moduleTypeEnum to be stopped. 
     * Cannot be null or empty.
     */
    public KillModuleButton(final List<ModuleTypeEnum> modules) {
        super(modules);
        
        setDisableReason("No modules can be killed because there are no modules running");
    }

    @Override
    public void onModuleAction(ClickEvent event) {
        
        UiManager.getInstance().displayConfirmDialog(
                "Kill Confirmation", 
                "Are you sure you want to kill all modules?", 
                new ConfirmationDialogCallback() {
                    
                    @Override
                    public void onDecline() {
                        //Nothing to do
                    }
                    
                    @Override
                    public void onAccept() {
                        UiManager.getInstance().getDashboardService().killModules(
                                UiManager.getInstance().getUserName(),
                                UiManager.getInstance().getSessionId(), modules,
                                null,
                                new AsyncCallback<GenericRpcResponse<Void>>() {
                                
                                    @Override
                                    public void onSuccess(GenericRpcResponse<Void> result) {
                                        
                                        if(!result.getWasSuccessful()) {
                                            UiManager.getInstance().displayDetailedErrorDialog("Failed to Kill Module(s)", 
                                                    result.getException());
                                            
                                            return;
                                        }
                                    }
                                
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        UiManager.getInstance().displayDetailedErrorDialog("Failed to Kill Modules", 
                                                "GIFT could not kill the specified modules(s) because an unexpected error occured", 
                                                "An exception was thrown while killing " + modules + ": " + caught, 
                                                null, null);
                                    }
                            });
                    }
                });
    }
    
    
}