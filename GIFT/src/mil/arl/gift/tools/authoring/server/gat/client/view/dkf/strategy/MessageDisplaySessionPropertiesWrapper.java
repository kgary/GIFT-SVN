/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy;

import generated.dkf.BooleanEnum;
import generated.dkf.Message;
import generated.dkf.Message.DisplaySessionProperties;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.DisplaySessionPropertiesWrapper;

/**
 * A widget used to edit domain session properties display settings for a message
 * 
 * @author nroberts
 */
public class MessageDisplaySessionPropertiesWrapper extends DisplaySessionPropertiesWrapper<Message.DisplaySessionProperties> {

    /**
     * A new instance of this widget
     */
    public MessageDisplaySessionPropertiesWrapper() {
        super();
    }

    @Override
    protected void refresh() {
        boolean isRequestState = value != null && BooleanEnum.TRUE.equals(value.getRequestUsingSessionState());
        requestStateBox.setValue(isRequestState);
        
        String providerUrl = GatClientUtility.getServerProperties().getPropertyValue(ServerProperties.EXTERNAL_STRATEGY_PROVIDER_URL);
        if(StringUtils.isBlank(providerUrl) && !isRequestState) {
            
            /* If there is no configured strategy provider, hide the checkbox to request from said provider.*/
            requestStateBox.setVisible(false);
        
        } else {
            
            /* Show the checkbox if there is a configured strategy provider OR if requesting from the provider was already enabled.
             * The latter is in case the user no longer wants to request from the provider */
            requestStateBox.setVisible(true);
        }
    }

    @Override
    protected void onRequestStateChaged(Boolean shouldRequest) {
        
        if(shouldRequest) {
            
            DisplaySessionProperties properties = value;
            if(properties == null) {
                properties = new DisplaySessionProperties();
            }
            
            properties.setRequestUsingSessionState(BooleanEnum.TRUE);
            
            setValue(properties, true);
            
        } else {
            setValue(null, true);
        }
    }
}
