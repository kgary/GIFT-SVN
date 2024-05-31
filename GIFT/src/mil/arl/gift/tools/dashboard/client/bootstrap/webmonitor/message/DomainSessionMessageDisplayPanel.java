/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.message;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.AbstractMessageUpdate;

/**
 * A panel that displays message traffic for a specific domain session
 * 
 * @author nroberts
 */
public class DomainSessionMessageDisplayPanel extends AbstractMessageDisplayPanel {

    /** The ID of the domain session that this panel monitors */
    private int domainSessionId;
    

    /**
     * Creates a new panel to display messages for a specific domain session
     */
    public DomainSessionMessageDisplayPanel(final int domainSessionId) {
        super();
        
        this.domainSessionId = domainSessionId;  
        
        entityMarkingLabel.setVisible(true);
        entityMarkingText.setVisible(true);
        entityMarkingTooltip.setVisible(true);
        
        entityMarkingText.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                String textValue = event.getValue();
                
                UiManager.getInstance().getDashboardService().entityFilter(
                        textValue,
                        UiManager.getInstance().getSessionId(),
                        domainSessionId, //cannot refer to domainSessionId in an enclosing scope
                        new AsyncCallback<GenericRpcResponse<Void>>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                UiManager.getInstance().displayDetailedErrorDialog("Failed to Filter Entity Markings", 
                                        "GIFT could not toggle the listening value for the module", 
                                        "An exception was thrown while filtering entity markings : " + caught, 
                                        null, null);  
                                
                            }

                            @Override
                            public void onSuccess(GenericRpcResponse<Void> result) {
                                if(!result.getWasSuccessful()) {
                                    UiManager.getInstance().displayDetailedErrorDialog("Failed to Filter Entity Markings", 
                                            result.getException());
                                    
                                    return;
                                }
                                
                            }
                            
                        }
                        );
                
            }
            
        });
    }
    

    @Override
    protected boolean isAccepted(AbstractMessageUpdate update) {
        
        /* 
         * For some absolutely baffling reason, just doing the == here doesn't work, since it 
         * throws a type casting error if the domain session ID is null.
         */
        return update.getDomainSessionId() != null 
                && domainSessionId == update.getDomainSessionId();
    }

    @Override
    public Integer getDomainSessionId() {
        return domainSessionId;
    }
}
