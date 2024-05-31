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
 * An action to close a widget
 *
 * @author jleonard
 */
public class CloseAction extends AbstractWidgetAction implements IsSerializable {

    /**
     * Default Constructor
     *
     * Required for GWT
     */
    public CloseAction() {
        super(ActionTypeEnum.CLOSE, null);
    }

    /**
     * Constructor
     *
     * @param widgetId The widget ID of the widget to close
     */
    public CloseAction(String widgetId) {
        super(ActionTypeEnum.CLOSE, widgetId);
    }
    
    @Override
    public String toString(){
        return "[CloseAction: "+super.toString()+"]";
    }
}
