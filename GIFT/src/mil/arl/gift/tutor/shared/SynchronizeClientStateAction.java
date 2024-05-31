/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * An action letting the server know that the client was unable to handle certain actions and has fallen out of sync
 * with its server-side session.
 *
 * @author nroberts
 */
public class SynchronizeClientStateAction extends AbstractWidgetAction implements IsSerializable {

    /**
     * Default Constructor
     *
     * Required for GWT
     */
    public SynchronizeClientStateAction() {
        super(ActionTypeEnum.SYNCHRONIZE_CLIENT_STATE, null);
    }

    /**
     * Constructor
     *
     * @param widgetId The widget ID of the widget to close
     */
    public SynchronizeClientStateAction(String widgetId) {
        super(ActionTypeEnum.SYNCHRONIZE_CLIENT_STATE, widgetId);
    }
    
    @Override
    public String toString(){
        return "[SynchronizeClientStateAction: "+super.toString()+"]";
    }
}
